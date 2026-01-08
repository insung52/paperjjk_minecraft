package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

import static org.justheare.paperJJK.PaperJJK.*;

public class Jobject {
    public boolean ish_depence=false;
    public Domain domain;
    public Jdomain_innate innate_domain;
    public boolean can_simple_domain = false;
    public boolean simple_domain_type = true;
    public Player player;
    public double black_flash_num=0.01;
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
    public void black_flash(){
        black_flash_tick--;
        if(black_flash_tick==0){
        }
        else{
            if (curseenergy < max_curseenergy) {
                curseenergy += max_curseenergy / 1000000 + 1;
            }
            //Particle.DustOptions dust=new Particle.DustOptions(Color.BLACK, 1);
            user.getWorld().spawnParticle(Particle.ENTITY_EFFECT, user.getLocation(), 3, 0.5, 0.5, 0.5, 0.5,Color.BLACK);
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

    public boolean damaget(LivingEntity victim,char type, double damage, boolean physics,String spe_name, boolean sure_hit){
        if (physics) {

            boolean bf = is_black_flash(user,victim);
            if(bf){
                damage=Math.pow(damage,1.5);
                //event.setDamage(damage);
            }
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
        }
        victim.addScoreboardTag("cursed");

        if(sure_hit){
            //victim.addScoreboardTag("sure_hit");
        }
        victim.damage(damage,user);
        //victim.damage(damage, DamageSource.builder(DamageType.SONIC_BOOM).build());
        return false;
    }
    public void jbasic(){
        if(user instanceof LivingEntity living){
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
