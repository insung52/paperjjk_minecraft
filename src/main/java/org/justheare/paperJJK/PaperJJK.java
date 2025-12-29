package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;
import org.justheare.paperJJK.network.JPacketHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PaperJJK extends JavaPlugin {
    static boolean rule_breakblock=true;
    static boolean rule_hud = true;
    static boolean hud_off = false;
    public static PaperJJK jjkplugin;
    public static ArrayList<Jobject> jobjects=new ArrayList<>();
    public static ArrayList<Jdomain_expand> expanded_domains=new ArrayList<>();

    // Sphere surface coordinate cache (1/8 octave optimization)
    // Maps radius → Set of [dx, dy, dz] where dx≥0, dy≥0, dz≥0
    // Surface definition: (r-1)² < dx² + dy² + dz² ≤ r²
    private static Map<Integer, Set<short[]>> sphereSurfaceCache = new HashMap<>();
    private static final int MAX_CACHED_RADIUS = 200;
    @Override
    public void onEnable() {
        jjkplugin= (PaperJJK) Bukkit.getPluginManager().getPlugin("PaperJJK");

        // Initialize sphere surface coordinate cache
        log("========================================");
        log("  Generating sphere surface cache (1-" + MAX_CACHED_RADIUS + ")...");
        long startTime = System.currentTimeMillis();
        generateSphereSurfaceCache();
        long endTime = System.currentTimeMillis();
        log("  Cache generation complete (" + (endTime - startTime) + "ms)");
        log("  Total cached radii: " + sphereSurfaceCache.size());
        log("========================================");

        // Plugin Messaging 채널 등록 (클라이언트 모드 통신)
        Messenger messenger = getServer().getMessenger();
        JPacketHandler packetHandler = new JPacketHandler(this);
        messenger.registerIncomingPluginChannel(this, JPacketHandler.CHANNEL, packetHandler);
        messenger.registerOutgoingPluginChannel(this, JPacketHandler.CHANNEL);
        log("========================================");
        log("  Plugin Messaging 채널 등록 완료");
        log("  채널: " + JPacketHandler.CHANNEL);
        log("========================================");

        // 데이터 파일 초기화 및 로드
        JData.init(getDataFolder());
        JDomainData.init(getDataFolder());
        JData.loadAllData();

        // 스킬 설명 시스템 초기화 (코드에서 로드)
        org.justheare.paperJJK.network.SkillDescriptionManager.init();

        initEvents();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(jjkplugin,new manage(),1,1);

        // 자동 저장 태스크 (5분마다)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(jjkplugin, () -> {
            JData.saveAllData();
        }, 6000, 6000); // 6000 ticks = 5분

        List<Player> players= (List<Player>) getServer().getOnlinePlayers();
        for(Player player:players){
            // 기존 데이터가 있으면 연결, 없으면 새로 생성
            Jobject existing = null;
            for(Jobject jo : jobjects){
                if(jo.uuid != null && jo.uuid.equals(player.getUniqueId())){
                    existing = jo;
                    break;
                }
            }
            if(existing != null){
                existing.user = player;
                existing.player = player;
            } else {
                jobjects.add(new Jplayer(player));
            }
            //player.setAllowFlight(true);
        }
        getServer().getPluginCommand("jjk").setExecutor(new Jcommand());
        log("PaperJJK started");
    }
    public void initEvents(){
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JEvent(this),this);
    }
    @Override
    public void onDisable() {
        // 서버 종료 시 모든 데이터 저장
        JData.saveAllData();
        log("PaperJJK stopped");
    }
    public static void log(String msg){
        jjkplugin.getLogger().info(msg);
    }
    public static Jobject getjobject(Entity entity){
        for(int r=0; r<jobjects.size(); r++){
            if(jobjects.get(r).user!=null){
                if(jobjects.get(r).user.equals(entity)){
                    return jobjects.get(r);
                }
            }
        }
        return null;
    }
    public static int getjobject_num(Entity entity){
        for(int r=0; r<jobjects.size(); r++){
            if(jobjects.get(r).user.equals(entity)){
                return r;
            }
        }
        return -1;
    }
    public static int getStrengthAmplifier(LivingEntity entity) {
        PotionEffect effect = entity.getPotionEffect(PotionEffectType.STRENGTH);
        if (effect != null) {
            return effect.getAmplifier()+1; // 0 = Strength I
        }
        return 0; // 포션 없음
    }
    public static List<Jujut> getjujuts(){
        List<Jujut> list=new ArrayList<Jujut>();
        for(int r=0; r<jobjects.size(); r++){
            list.addAll(jobjects.get(r).jujuts);
        }
        return list;
    }

    /**
     * 몹의 AI Goal을 제거하되 물리 엔진은 유지
     */
    public static void disableMobAI(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }

        try {
            // Reflection을 통해 NMS Mob 객체 접근
            Method getHandleMethod = mob.getClass().getMethod("getHandle");
            Object nmsMob = getHandleMethod.invoke(mob);

            // goalSelector와 targetSelector 가져오기
            Object goalSelector = nmsMob.getClass().getField("goalSelector").get(nmsMob);
            Object targetSelector = nmsMob.getClass().getField("targetSelector").get(nmsMob);

            // removeAllGoals 메소드 호출
            Method removeAllGoalsMethod = goalSelector.getClass().getMethod("removeAllGoals", java.util.function.Predicate.class);
            removeAllGoalsMethod.invoke(goalSelector, (java.util.function.Predicate<Object>) goal -> true);
            removeAllGoalsMethod.invoke(targetSelector, (java.util.function.Predicate<Object>) goal -> true);

            // 침묵 효과 (소리 방지)
            entity.setSilent(true);

        } catch (Exception e) {
            // Reflection 실패 시 대체 방법
            log("Failed to disable AI via reflection: " + e.getMessage());
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 255, false, false));
            entity.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20, 255, false, false));
            entity.setSilent(true);
        }
    }

    /**
     * 몹의 AI를 복구
     */
    public static void restoreMobAI(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }

        try {
            // Reflection으로 NMS Mob 객체 접근
            Method getHandleMethod = mob.getClass().getMethod("getHandle");
            Object nmsMob = getHandleMethod.invoke(mob);

            // registerGoals 메소드 호출 시도 (기본 AI 재등록)
            try {
                Method registerGoalsMethod = nmsMob.getClass().getDeclaredMethod("registerGoals");
                registerGoalsMethod.setAccessible(true);
                registerGoalsMethod.invoke(nmsMob);
            } catch (NoSuchMethodException e) {
                // registerGoals가 없으면 setAI 사용
                log("no registerGoals");
                mob.setAI(true);
            }

            // 침묵 해제
            mob.setSilent(false);

        } catch (Exception e) {
            // 실패 시 기본 setAI 사용
            log("Failed to restore AI via reflection: " + e.getMessage());
            mob.setAI(true);
            mob.setSilent(false);
        }
    }
    public static double get_calculated_power(Entity entity,String target,double power){
        if(entity instanceof Player player){
            if(player.getGameMode().equals(GameMode.CREATIVE)){
                return 0;
            }
        }
        Jobject jo = PaperJJK.getjobject(entity);
        if(jo!=null&&jo.naturaltech.equals("mahoraga")&&!jo.jujuts.isEmpty()&&jo.jujuts.get(0) instanceof Mahoraga mahoraga){
            return mahoraga.pre_adapt(target,"curse",1);
        }
        else if(jo!=null&&jo.naturaltech.equals("infinity")&&!jo.jujuts.isEmpty()&&jo.jujuts.get(0) instanceof Infinity_passive){
            return 0;
        }
        else {
            return power;
        }
    }
    public static boolean is_black_flash(Entity attacker, Entity victim){
        Jobject attacker_jobject = getjobject(attacker);
        if(attacker_jobject!=null){
            if(attacker_jobject.curseenergy>200){
                if(Math.random()<=attacker_jobject.black_flash_num){
                    attacker_jobject.black_flash_tick=20*60;
                    attacker_jobject.curseenergy+=(attacker_jobject.max_curseenergy-attacker_jobject.curseenergy)*0.2;
                    victim.getLocation().createExplosion(attacker,2,false,rule_breakblock);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate sphere surface coordinate cache for radii 1 to MAX_CACHED_RADIUS
     * Stores only 1/8 octave (dx≥0, dy≥0, dz≥0) for memory efficiency
     * Surface definition: (r-1)² < dx² + dy² + dz² ≤ r²
     */
    private static void generateSphereSurfaceCache() {
        for (int radius = 1; radius <= MAX_CACHED_RADIUS; radius++) {
            Set<short[]> surfaceOffsets = new HashSet<>();

            // Calculate distance bounds
            int rSquared = radius * radius;
            int rMinusOneSquared = (radius - 1) * (radius - 1);

            // Scan only positive octant (1/8 of sphere)
            for (int dx = 0; dx <= radius; dx++) {
                for (int dy = 0; dy <= radius; dy++) {
                    for (int dz = 0; dz <= radius; dz++) {
                        int distSquared = dx * dx + dy * dy + dz * dz;

                        // Check if on surface: (r-1)² < dist² ≤ r²
                        if (distSquared > rMinusOneSquared && distSquared <= rSquared) {
                            surfaceOffsets.add(new short[]{(short) dx, (short) dy, (short) dz});
                        }
                    }
                }
            }

            sphereSurfaceCache.put(radius, surfaceOffsets);
        }
    }

    /**
     * Get all sphere surface offsets for a given radius
     * Returns full sphere by expanding 1/8 octave to all 8 octants
     *
     * @param radius Sphere radius (1-200)
     * @return Set of Vector offsets representing sphere surface blocks
     */
    public static Set<Vector> getSphereSurfaceOffsets(int radius) {
        // Clamp radius to valid range
        if (radius < 1) radius = 1;
        if (radius > MAX_CACHED_RADIUS) radius = MAX_CACHED_RADIUS;

        Set<short[]> octaveOffsets = sphereSurfaceCache.get(radius);
        if (octaveOffsets == null) {
            return new HashSet<>();
        }

        Set<Vector> allOffsets = new HashSet<>();

        // Expand 1/8 octave to all 8 octants using ±x, ±y, ±z
        for (short[] offset : octaveOffsets) {
            int dx = offset[0];
            int dy = offset[1];
            int dz = offset[2];

            // Generate all sign combinations
            // For zero coordinates, we get fewer unique combinations (Set handles duplicates)
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sy = -1; sy <= 1; sy += 2) {
                    for (int sz = -1; sz <= 1; sz += 2) {
                        allOffsets.add(new Vector(dx * sx, dy * sy, dz * sz));
                    }
                }
            }
        }

        return allOffsets;
    }
}
class manage implements Runnable{
    int tick=0;
    double theta = 0;
    double phi = 0;
    double step=Math.PI/(10);
    Location b_location;
    public void potionpower(LivingEntity living,PotionEffectType potionEffectType, double curseenergy, int black_flash){
        double power = Math.log10(curseenergy+1)-2.5+black_flash;
        if(power>0){  //
            living.addPotionEffect(new PotionEffect(potionEffectType,30, (int) power));
        }
    }
    @Override
    public void run() {
        tick++;
        if(tick%20==0){
            for(Jobject jobject : PaperJJK.jobjects){
                if(jobject.user instanceof LivingEntity living){
                    potionpower(living,PotionEffectType.SPEED,jobject.curseenergy/5000, (jobject.black_flash_tick>0?2:0));
                    potionpower(living,PotionEffectType.JUMP_BOOST,jobject.curseenergy/5000, (jobject.black_flash_tick>0?1:0));
                    potionpower(living,PotionEffectType.STRENGTH,jobject.curseenergy, (jobject.black_flash_tick>0?1:0));

                }
            }
        }
        for(int r=0; r<PaperJJK.jobjects.size(); r++){
            Jobject jobject=PaperJJK.jobjects.get(r);
            if(jobject.black_flash_tick>0){
                jobject.black_flash();
            }
            if(jobject.infinity_stun_tick>0){
                jobject.infinity_stun_tick--;
                if(jobject.user instanceof LivingEntity living){
                    if(living.getHealth()<=0){
                        jobject.infinity_stun_tick=0;
                    }
                }
                if(jobject.user instanceof Player player && (player.getGameMode().equals(GameMode.SURVIVAL)||player.getGameMode().equals(GameMode.ADVENTURE))){
                    player.setVelocity(new Vector(0,-1,0));
                }
            }
            else if(jobject instanceof Jplayer jplayer) {
                if (tick % 5 == 0) {
                    if (jplayer.reversecursing) {
                        if(jplayer.player.isSneaking()){
                            if (jplayer.curseenergy > 0) {
                                jplayer.player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH,1, (int) Math.pow(jplayer.getremaincurrent(),0.1)));
                                List<PotionEffect> potionEffects = (List<PotionEffect>) jplayer.player.getActivePotionEffects();
                                for (PotionEffect potionEffect : potionEffects) {
                                    PotionEffectType pet = potionEffect.getType();
                                    if (pet.getCategory().equals(PotionEffectTypeCategory.HARMFUL)) {
                                        jplayer.player.removePotionEffect(potionEffect.getType());
                                        jplayer.player.addPotionEffect(potionEffect.withDuration(potionEffect.getDuration() - jplayer.getremaincurrent() / 100));
                                    }
                                }
                                jplayer.curseenergy -= jplayer.getremaincurrent();
                            }
                        }
                        else {
                            jplayer.reversecursing=false;
                        }
                    }
                    if (jplayer.reversecursing_out) {
                        if (jplayer.curseenergy > 0) {
                            if (jplayer.player.isSneaking()) {
                                boolean cs = false;
                                Jobject jjoo = PaperJJK.getjobject(jplayer.reversecursing_out_entity);
                                if (jjoo != null) {
                                    if (jjoo.cursespirit) {
                                        cs = true;
                                    }
                                }
                                if (cs) {
                                    if (0 > jplayer.reversecursing_out_entity.getHealth() + jplayer.getremaincurrent() / 600) {
                                        jplayer.reversecursing_out_entity.setHealth(jplayer.reversecursing_out_entity.getHealth() - jplayer.getremaincurrent() / 600);

                                    } else {
                                        jplayer.reversecursing_out_entity.setHealth(jplayer.reversecursing_out_entity.getHealth() - jplayer.getremaincurrent() / 600);
                                    }
                                } else {
                                    if (jplayer.reversecursing_out_entity.getMaxHealth() < jplayer.reversecursing_out_entity.getHealth() + jplayer.getremaincurrent() / 600) {
                                        jplayer.reversecursing_out_entity.setHealth(jplayer.reversecursing_out_entity.getMaxHealth());
                                    } else {
                                        jplayer.reversecursing_out_entity.setHealth(jplayer.reversecursing_out_entity.getHealth() + jplayer.getremaincurrent() / 600);
                                    }
                                }
                                jplayer.curseenergy -= jplayer.getremaincurrent();
                            } else {
                                jplayer.reversecursing_out = false;
                                //jplayer.cursecurrent+=jplayer.getremaincurrent();
                            }
                        } else {
                            jplayer.reversecursing_out = false;
                            //jplayer.cursecurrent+=jplayer.getremaincurrent();
                        }
                    }
                }       //반전술식

                jplayer.air_surface();
                //jplayer.player.setAllowFlight(true);
                if (((Jplayer) jobject).air_surface_tick == 0) {
                    if (jobject.curseenergy > 500) {
                        jplayer.player.setAllowFlight(true);
                    }
                }
                if (jobject.can_air_surface) {
                    if (jplayer.air_surface_tick > 0) {
                        jplayer.air_surface_tick--;
                    }
                } else {
                    if (jplayer.air_surface_tick > 0) {
                        if (jplayer.player.isOnGround()) {
                            jplayer.air_surface_tick = 0;

                        }
                    }
                }
            }
            if(jobject instanceof Jplayer jplayer){
                if(PaperJJK.rule_hud){
                    ((Jplayer) jobject).scoboard();
                }
                if(PaperJJK.hud_off){
                    PaperJJK.hud_off=false;
                    ScoreboardManager manager = Bukkit.getScoreboardManager();
                    Scoreboard emptyBoard = manager.getNewScoreboard();
                    jplayer.player.setScoreboard(emptyBoard);
                }
                if (jobject.curseenergy < jobject.max_curseenergy) {
                    jobject.curseenergy += jobject.max_curseenergy / 10000000 + 1;
                    if (jobject.curseenergy < 0) {
                        jobject.disablejujut();
                    }
                }

            }
        }
    }
}