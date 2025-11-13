package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import org.justheare.paperJJK.network.KeyStateManager;

import java.util.ArrayList;
import java.util.List;

public final class PaperJJK extends JavaPlugin {
    static boolean rule_breakblock=true;
    static boolean rule_hud = true;
    static boolean hud_off = false;
    static PaperJJK jjkplugin;
    public static ArrayList<Jobject> jobjects=new ArrayList<>();
    public static ArrayList<Jdomain_expand> expanded_domains=new ArrayList<>();
    @Override
    public void onEnable() {
        jjkplugin= (PaperJJK) Bukkit.getPluginManager().getPlugin("PaperJJK");

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
                // 키 상태 틱 처리 (매 틱마다)
                KeyStateManager.tickPlayer(jplayer);

                if (tick % 5 == 0) {
                    if (jplayer.reversecursing) {
                        jplayer.reversecursing=false;
                        if (jplayer.curseenergy > 0) {

                            jplayer.player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH,1, (int) Math.pow(jplayer.getremaincurrent(),0.1)));
                            List<PotionEffect> potionEffects = (List<PotionEffect>) jplayer.player.getActivePotionEffects();
                            for (PotionEffect potionEffect : potionEffects) {
                                PotionEffectType pet = potionEffect.getType();
                                if (pet.getCategory().equals(PotionEffectTypeCategory.HARMFUL)) {
                                    jplayer.player.removePotionEffect(potionEffect.getType());
                                    jplayer.player.addPotionEffect(potionEffect.withDuration(potionEffect.getDuration() - jplayer.getremaincurrent() / 100));
                                }
                                //if(pet.equals(PotionEffectType.POISON)||pet.equals(PotionEffectType.WITHER)||pet.equals(PotionEffectType.LEVITATION)||pet.equals(PotionEffectType.BLINDNESS)||pet.equals(PotionEffectType.DARKNESS)||pet.){

                                //}

                            }
                            jplayer.curseenergy -= jplayer.getremaincurrent();

                        } else {
                            jplayer.reversecursing = false;
                            //jplayer.cursecurrent+=jplayer.getremaincurrent();
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