package org.justheare.paperJJK;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.justheare.paperJJK.PaperJJK.getjobject;
import static org.justheare.paperJJK.PaperJJK.is_black_flash;

public class Jobject {
    public boolean ish_depence=false;
    public Domain domain;
    public Jdomain_innate innate_domain;
    public boolean can_simple_domain = false;
    public boolean simple_domain_type = true;
    public Player player;
    public double black_flash_num=0.01;
    public double basic_black_flash_num=0.01;
    public double zone_black_flash_num = 0.4;
    public int black_flash_count = 0;
    public int black_flash_tick=0;
    public int max_curseenergy=5;
    public int curseenergy=1;
    public int max_cursecurrent=1;
    public int cursecurrent=0;
    public boolean reversecurse=false;
    public boolean is_reversecurse(){
        return reversecurse;
    }
    public boolean reversecursing=false;
    public void set_reversecursing(boolean b){
        reversecursing=b;
    }
    public boolean reversecurse_out=false;
    public boolean reversecursing_out=false;
    public LivingEntity reversecursing_out_entity;
    public boolean cursespirit=false;
    public boolean blocked=true;
    public boolean can_air_surface=false;
    public Entity user;
    public UUID uuid;
    public String naturaltech="";
    public ArrayList<Jujut> jujuts=new ArrayList<Jujut>();
    public Entity cursedentity;
    public int infinity_stun_tick=0;    // 모든 행동 불가
    public int cursed_tech_block_tick=0;    // 술식 사용 불가
    public boolean Physical_Gifted = false;
    public void attack_effector(Entity victim){
        if(naturaltech.equals("infinity")&&user instanceof LivingEntity living){
            if(Math.random()<0.3){
                double distances = living.getEyeLocation().distance(victim.getLocation());
                living.getWorld().spawnParticle(
                        Particle.FLASH,
                        living.getEyeLocation().add(living.getEyeLocation().getDirection().multiply(distances*0.7)),
                        1,
                        1.0, 1.0, 1.0,
                        1.0,
                        Color.fromARGB(10, 0, 0, 255)
                );
            }

        }
    }
    public void reverseCurse(int tick){
        if (this instanceof Jplayer jplayer && reversecursing) {
            if(player.isSneaking()){
                if (curseenergy > 0) {
                    if(tick%10==0){
                        jplayer.player.getWorld().playSound(jplayer.player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.3F, 0.7f);
                        jplayer.player.getWorld().playSound(jplayer.player, Sound.BLOCK_CONDUIT_AMBIENT, 0.3F, 1.0f);
                    }
                    jplayer.player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, jplayer.player.getLocation(), 10, 0.5, 0.5, 0.5, 0.5,Color.WHITE);

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
                    jplayer.player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,20, 1));
                }
            }
            else {
                jplayer.reversecursing=false;
            }
        }
    }
    public void black_flash(){
        black_flash_tick--;

        if(black_flash_tick>=20*59){
            double tick = (20*60 - black_flash_tick)*1.0;

            Particle.Spell spell = new Particle.Spell(Color.BLACK,1.0f);
            user.getWorld().spawnParticle(Particle.INSTANT_EFFECT, user.getLocation(), 40* black_flash_count, tick, tick, tick, 0.5,spell);
            spell = new Particle.Spell(Color.fromARGB(255, 128, 0, 0),1.0f);
            user.getWorld().spawnParticle(Particle.INSTANT_EFFECT, user.getLocation(), 40* black_flash_count, tick, tick, tick, 0.5,spell);

            user.getWorld().spawnParticle(
                    Particle.FLASH,
                    user.getLocation(),
                    3* black_flash_count,
                    tick, tick, tick,
                    10.0,
                    Color.fromARGB(128, 255, 50, 50)
            );
            user.getWorld().spawnParticle(
                    Particle.FLASH,
                    user.getLocation(),
                    3* black_flash_count,
                    tick, tick, tick,
                    10.0,
                    Color.fromARGB(128, 255, 255, 255)
            );

        }

        if (curseenergy < max_curseenergy) {
            curseenergy += max_curseenergy / 1000000 + 1;
        }
        //Particle.DustOptions dust=new Particle.DustOptions(Color.BLACK, 1);
        user.getWorld().spawnParticle(Particle.ENTITY_EFFECT, user.getLocation(), 1, 0.5, 0.5, 0.5, 0.5,Color.BLACK);
        if(black_flash_num>basic_black_flash_num){
            black_flash_num = Math.max(black_flash_num-0.002,basic_black_flash_num);
        }
        else {
            black_flash_count =0;
        }
        //PaperJJK.log(black_flash_num + "");
        if(black_flash_num>basic_black_flash_num&&black_flash_tick%55==40){

            player.playSound(player, Sound.BLOCK_CONDUIT_AMBIENT, (float) (black_flash_num/zone_black_flash_num)*1.2F, 1.5f);
        }
    }
    public boolean use_domain(String[] values,boolean cancel){
        if(!cancel){
            if (innate_domain.level < 2) {
                player.sendMessage("not enough");
                return false;
            }
            else if(curseenergy<50000){
                player.sendMessage("not enough cursed energy");
                return false;
            }
            else{

                if(values[0].equals("1")){
                    return innate_domain.drow_expand(Integer.parseInt(values[1]));
                }
                else if(values[0].equals("0")){
                    return innate_domain.build_expand(Integer.parseInt(values[1]));
                }
            }
        }
        else{
            if(values[0].equals("1")){

                return innate_domain.undrow_expand();
            }
            else if(values[0].equals("0")){
                return innate_domain.destroy_expand();
            }
        }
        return false;
    }
    public int getremaincurrent(){
        return max_cursecurrent-cursecurrent;
    }
    public static List<String> ish_unblockable = new ArrayList<>(Arrays.asList(
            "aka",
            "murasaki"
    ));

    public boolean damaget(LivingEntity victim,char type, double damage, boolean physics,String spe_name, boolean sure_hit){
        if (physics && user instanceof LivingEntity living) {
            boolean bf = is_black_flash(living,victim);
            if(bf){
                damage=Math.pow(damage,1.5);
            }
        }
        else {
            victim.addScoreboardTag("cursed");
        }
        Jobject v_jobject = getjobject(victim);
        if(v_jobject!=null){
            if(v_jobject.naturaltech.equals("mahoraga")){
                if(!v_jobject.jujuts.isEmpty() && v_jobject.jujuts.get(0) instanceof Mahoraga mahoraga){
                    double power = mahoraga.pre_adapt(spe_name,"damage",1);
                    damage = damage * power;
                    if(power<=0){
                        damage=0;
                    }
                }
            }
            else if(v_jobject.naturaltech.equals("infinity")){
                for (Jujut jujut : v_jobject.jujuts) {
                    if (jujut instanceof Infinity_passive ip) {
                        if(sure_hit){

                        }
                        else{

                            return false;
                        }
                        //event.setCancelled(ip.defence(attacker));
                    }
                }
            }
            else if(v_jobject.naturaltech.equals("physical_gifted") && !ish_unblockable.contains(spe_name)){
                if(!victim.getEquipment().getItemInMainHand().isEmpty()) {
                    if (PaperJJK.getItemTag(victim.getEquipment().getItemInMainHand()).equals("cw_ish")) {
                        for (Jujut jujut : v_jobject.jujuts) {
                            if (jujut instanceof PhysicalGifted ph && ph.spe_name.equals("reflex")) {
                                if(sure_hit){

                                }
                                else{
                                    ph.interact=true;
                                    ph.interact_value+=1;
                                    return false;
                                }
                                //event.setCancelled(ip.defence(attacker));
                            }
                        }
                    }
                }

            }
        }


        if(sure_hit){
            //victim.addScoreboardTag("sure_hit");
        }
        victim.damage(damage,user);
        //victim.damage(damage, DamageSource.builder(DamageType.SONIC_BOOM).build());
        return false;
    }
    public void jbasic(){
        if(user instanceof LivingEntity living && !naturaltech.equals("physical_gifted")){
            living.setMaxHealth(20+Math.pow(max_curseenergy-5,0.3));
            user.sendMessage("health seted");
        }
    }
    public Jobject(Entity entity){
        user=entity;
        if(entity != null) {
            uuid = entity.getUniqueId();
        }
        new Domain(this);
        //jujuts=new Jujut[10];
    }
    public void setvalues(String name, int mce, int mcc, int rs, int rso){
        if(mce>1000&&player!=null){
            player.setAllowFlight(true);
        }
        if(naturaltech.isEmpty()){
            if (name.contains("infinity")) {
                can_simple_domain = true;
                simple_domain_type = true;
                blocked = false;
                max_curseenergy = mce;
                max_cursecurrent = mcc;
                reversecurse = rs == 1;
                reversecurse_out = rso == 1;
                naturaltech = "infinity";
                can_air_surface = true;
                user.sendMessage(ChatColor.LIGHT_PURPLE + "the Infinity.");
                innate_domain = new Infinity_domain(this);
                jbasic();
                if(this instanceof Jplayer jplayer){
                    jplayer.initializeSlots();
                }
            }
            else if (name.contains("mizushi")) {
                can_simple_domain = true;
                simple_domain_type = false;
                domain=new Mizushi_domainp(this);
                blocked = false;
                max_curseenergy = mce;
                max_cursecurrent = mcc;
                reversecurse = rs == 1;
                reversecurse_out = rso == 1;
                naturaltech = "mizushi";
                can_air_surface = true;
                user.sendMessage(ChatColor.RED + "the Mizushi.");
                innate_domain = new Mizushi_domain(this);
                jbasic();
                if(this instanceof Jplayer jplayer){
                    jplayer.initializeSlots();
                }
            }
            else if (name.contains("mahoraga")){
                blocked=false;
                max_curseenergy=mce;
                max_cursecurrent=mcc;
                reversecurse = rs ==1;
                reversecurse_out = rso ==1 ;
                naturaltech = "mahoraga";
                can_air_surface = true;
                jbasic();

            }
            else if(name.contains("physical_gifted")){
                curseenergy=0;
                can_simple_domain = false;
                simple_domain_type = false;
                blocked = false;
                max_curseenergy = mce;
                max_cursecurrent = mcc;
                reversecurse = rs == 0;
                reversecurse_out = rso == 0;
                naturaltech = "physical_gifted";
                can_air_surface = true;
                user.sendMessage(ChatColor.LIGHT_PURPLE + "Physical gifted.");
                innate_domain = new Jdomain_innate(this);
                jbasic();
                if(this instanceof Jplayer jplayer){
                    jplayer.initializeSlots();
                }
            }
        }
        else if(!naturaltech.isEmpty()&&user.isSneaking()){
            if(reversecurse){
                reversecursing=true;
            }
        }
    }
    public int usejujut(String name, String spe_name, String type, boolean rct, int power, int duration, char target, @Nullable Entity victim){
        if(name.contains(naturaltech)){
            if(naturaltech.equals("infinity")) {
                if(type.equals("ao")) {
                    if (rct) {
                        if (reversecurse) {
                            Infinity infinity = new Infinity(this, spe_name, type, true, power, duration, target);
                            infinity.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, infinity, 1, 1);
                            jujuts.add(infinity);
                            return infinity.power;
                        } else {
                            user.sendMessage(ChatColor.RED + "you can't use reverse cursed technic");
                        }
                    } else {
                        Infinity infinity = new Infinity(this, spe_name, type, false, power, duration, target);
                        infinity.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, infinity, 1, 1);
                        jujuts.add(infinity);
                        return infinity.power;
                    }
                }
                else if(type.equals("passive")){
                    if(rct){
                    }
                    else {
                        Infinity_passive infinityPassive = new Infinity_passive(this, spe_name, type, false,power,duration,target);
                        infinityPassive.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, infinityPassive, 1, 1);
                        jujuts.add(infinityPassive);
                        return infinityPassive.power;
                    }
                }
            }
            if(naturaltech.equals("mizushi")){
                if(type.equals("kai")){
                    if(rct){
                        if(victim!=null){
                            Mizushi mizushi = new Mizushi(this,spe_name,type,true,power, Math.min(duration, 50),target);
                            mizushi.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,mizushi,1,1);
                            jujuts.add(mizushi);
                            mizushi.j_entities.add(victim);
                            mizushi.direction = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(player.getEyeLocation().distance(victim.getLocation())-0.5)).add( victim.getLocation().multiply(-1) ).toVector();
                            return mizushi.power;
                        }
                    }
                    else{
                        Mizushi mizushi = new Mizushi(this,spe_name,type,false,power,duration,target);
                        mizushi.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,mizushi,1,1);
                        jujuts.add(mizushi);
                        return mizushi.power;
                    }
                }
                else if(type.equals("fuga")){
                    Mizushi_fuga fuga =new Mizushi_fuga(this,spe_name,type,false,power,duration,target);
                    fuga.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,fuga,1,1);
                    jujuts.add(fuga);
                    return 100;
                }
            }
            if(naturaltech.equals("mahoraga")){
                if(type.equals("mahoraga")){
                    Mahoraga mahoraga = new Mahoraga(this, spe_name,type,false,power,duration,target);
                    mahoraga.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,mahoraga,1,1);
                    jujuts.add(mahoraga);
                    // 저장된 적응 데이터 로드
                    JData.loadMahoragaAdaptData(mahoraga, this.uuid);
                }
            }
            if(naturaltech.equals("physical_gifted")){
                if(type.equals("dash")){
                    PhysicalGifted physicalGifted = new PhysicalGifted(this,spe_name,type,false,power,duration,target);
                    physicalGifted.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,physicalGifted, 1 ,1);
                    jujuts.add(physicalGifted);
                    return physicalGifted.power;
                }
            }
        }
        return 10;
    }
    public int usecw(String name,boolean rct){
        if(name.equals("ish")){
            Cw_ish cw_ish = new Cw_ish(this,"ish","ish",rct,0,10,'a');
            cw_ish.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,cw_ish,1,1);
            return 40;
        }
        else if(name.equals("kamutoke")){
            Cw_kamutoke cw_kamutoke = new Cw_kamutoke(this,"kamutoke","kamutoke",rct,0,10,'a');
            cw_kamutoke.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,cw_kamutoke,1,1);
            return 200;
        }
        return 10;
    }
    public boolean disablejujut(String spe_name){
        for(int r=jujuts.size()-1; r>=0; r--){
            if(jujuts.get(r).spe_name.equals(spe_name)){
                jujuts.get(r).disables();
                return true;
            }
        }
        return false;
    }
    public boolean disablejujut(){
        for(int r=0; r<jujuts.size(); r++){
            jujuts.get(r).disables();
        }
        return true;
    }
    public boolean fixjujut(String spe_name, boolean sneak){
        for(int r=0; r<jujuts.size(); r++){
            if(jujuts.get(r).spe_name.equals(spe_name)){
                if(sneak){
                    disablejujut(spe_name);
                    PaperJJK.log(spe_name);
                    return true;
                }
                else {
                    if(jujuts.get(r).fixable){
                        jujuts.get(r).fixed=!jujuts.get(r).fixed;
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean scroll(String spe_name,int count){
        for(int r=jujuts.size()-1; r>=0; r--){
            if(jujuts.get(r).spe_name.equals(spe_name)){

                return jujuts.get(r).scroll(count);
            }
        }
        return false;
    }
    public void distribute(){

    }
}
