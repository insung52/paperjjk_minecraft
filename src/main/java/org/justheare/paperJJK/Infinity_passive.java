package org.justheare.paperJJK;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Infinity_passive extends Jujut{
    List<Location> j_entities_location;
    public boolean defence(Entity attacker){
        if(true){
            return true;
        }
        return false;
    }
    public Infinity_passive(Jobject jobject, String spe_name, String type, boolean rct, int power, int time, char target) {
        super(jobject, spe_name, type, rct, power, time, target);
        max_power=5;
        use_power=3;
        j_entities=new ArrayList<Entity>();
        j_entities_location=new ArrayList<Location>();
        fixable=true;
        setcurrent(1,100);
        user.getWorld().playSound(((Player)user).getEyeLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,1,1);
        //user.getWorld().setBiome(location,Biome.);
    }
    public void defending(Entity entity,char type){
        Defending defending=new Defending(this,type,entity,entity.getVelocity().length());
        defending.task_num= Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, defending, 1, 1);
    }
    public void defending(Entity entity,char type,double velocity){
        Defending defending=new Defending(this,type,entity,velocity);
        defending.task_num=Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, defending, 1, 1);
    }
    int tick=0;
    int ticks=0;
    @Override
    public void run() {
        if(tick>0){
            tick--;
        }
        user.setFireTicks(0);
        user.setVisualFire(false);
        ticks++;
        location=((Player) user).getEyeLocation();
        List<Entity> targets = (List<Entity>) location.getNearbyEntities(use_power, use_power, use_power);
        for(Entity entity:targets){

            if(entity.equals(user)){
                continue;
            }
            Jobject tj = PaperJJK.getjobject(entity);
            if(tj!=null){
                if(tj.ish_depence){
                    user.getWorld().playSound(user, Sound.BLOCK_GLASS_BREAK, 2F, 0.7F);
                    ((Player) user).setCooldown(Material.WRITTEN_BOOK,20);
                    disables();
                }
            }
            if(entity.getLocation().distance(location)<use_power+entity.getHeight()+entity.getWidth()){
                if(!j_entities.contains(entity)){
                    j_entities.add(entity);
                    j_entities_location.add(entity.getLocation());
                    if(entity instanceof LivingEntity){
                        //defending(entity, 'd',8);
                    }
                    else {
                        //defending(entity, 'd');
                    }
                }
            }
        }
        boolean defend=false;
        for(int r=0; r<j_entities.size(); r++){
            Entity entity=j_entities.get(r);
            double entity_distance=entity.getLocation().add(0,entity.getHeight()/2,0).distance(location);
            if(j_entities.get(r) instanceof LivingEntity li){
                if(li.getHealth()<=0){
                    j_entities.remove(r);
                    j_entities_location.remove(r);
                    continue;
                }
            }
            if(entity_distance<use_power+entity.getHeight()/3+entity.getWidth()/3){
                double power=1;
                Jobject tj = PaperJJK.getjobject(entity);
                if(tj!=null && tj.naturaltech.equals("mahoraga")){
                    Mahoraga mjujut = (Mahoraga) tj.jujuts.get(0);
                    power = mjujut.pre_adapt("infinity_passive","curse",1);
                    if(power<=0||Math.random()>power){
                        continue;
                    }
                }
                if(entity_distance<use_power+entity.getHeight()/3+entity.getWidth()/3-1){
                    if(entity_distance<use_power+entity.getHeight()/3+entity.getWidth()/3-3){   //dmg
                        if (ticks%3==0&&entity instanceof LivingEntity living) {
                            jobject.damaget(living, 'j', Math.pow(entity_distance+0.5,-0.5)+1, false,"infinity_passive",false);
                        }
                    }
                    //s
                    entity.setVelocity(d_location(location,entity.getLocation()).normalize().multiply(0.3*power));
                    j_entities_location.set(r,entity.getLocation());
                    if(tick==0&&entity instanceof LivingEntity){
                        if(!defend){
                            defending(entity,'p',0);
                            defend = true;
                        }
                        //PaperJJK.log(entity_distance+"1");
                        tick= (int) (entity_distance*4);
                    }
                }
                else if(!(entity instanceof Player)){   //d
                    entity.setVelocity(entity.getVelocity().clone().multiply(0.2*power));
                }
                if(!(entity instanceof LivingEntity)) {
                    entity.setRotation(j_entities_location.get(r).getYaw(), j_entities_location.get(r).getPitch());
                }
            }
            else{
                j_entities.remove(r);
                j_entities_location.remove(r);
            }
        }
        //location.getWorld().spawnParticle(Particle.T);
    }
    public void disabled(){
        user.getWorld().playSound(user.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1, 2);
    }
    public boolean scroll(int count){
        if(count>0){
            if(use_power<5){
                use_power++;
            }
        }
        else{
            if(use_power>1){
                use_power--;
            }
        }
        return true;
    }
    public String toname(){
        return ChatColor.AQUA+"passive"+ChatColor.WHITE+" *"+use_power;
    }
}

class Defending implements Runnable{
    int task_num;
    Infinity_passive ip;
    char type;
    Entity entity;
    double yaw;
    double pitch;
    double step;
    double phistep;
    double theta=0;
    double phi=0;
    Location location;
    double speed;
    Defending(Infinity_passive ip,char type,Entity entity,double speed){
        this.ip=ip;
        this.type=type;
        this.entity=entity;
        this.speed=speed;
        location=ip.location;
        yaw=location.setDirection(ip.d_location(location,entity.getLocation())).getYaw();
        pitch=location.getPitch();
        step = Math.PI / ip.use_power/4;
        phistep = step / Math.sin(theta == 0 || theta == Math.PI ? step : theta)/2;
    }
    @Override
    public void run() {
        try {
            location = ip.location;
            if (type == 'd') {
                if (theta >= Math.PI * (speed / 14.0)) {
                    Bukkit.getScheduler().cancelTask(task_num);
                } else {
                    //PaperJJK.log(phi+" ");
                    //phistep = step / Math.sin(theta == 0 || theta == Math.PI ? step : theta);

                    double sinTheta = Math.sin(theta);
                    phistep = (theta == 0 || theta == Math.PI) ? step : step / (sinTheta == 0 ? 1e-8 : sinTheta);

                    for (double phi = 0; phi < 2 * Math.PI; phi += phistep) {

                        double x = ip.use_power * Math.sin(theta) * Math.cos(phi);
                        double y = ip.use_power * Math.sin(theta) * Math.sin(phi);
                        double z = ip.use_power * Math.cos(theta);
                        //b_location.getWorld().spawnParticle(Particle.REDSTONE, b_location.clone().add(new Vector(x,y,z)), 1, 0.1, 0.1, 0.1, 0.5, dust, true);
                        Location tlocation = location.clone().add(new Vector(x, y, z).rotateAroundY(-Math.toRadians(yaw)).rotateAroundX(Math.toRadians(pitch) * Math.cos(Math.toRadians(yaw))).rotateAroundZ(Math.toRadians(pitch) * Math.sin(Math.toRadians(yaw))));
                        //location.getWorld().spawnParticle(Particle.REDSTONE, tlocation, 1, 0.1, 0.1, 0.1, 0.5, dust, true);
                        tlocation.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tlocation, 1, 0.01, 0.01, 0.01, 0);
                    }
                    theta += step;
                }
            } else if (type == 'l') {
                if (theta >= Math.PI * (0.8)) {
                    Bukkit.getScheduler().cancelTask(task_num);
                } else {
                    phistep = step / Math.sin(theta == 0 || theta == Math.PI ? step : theta);
                    for (double phi = 0; phi < 2 * Math.PI; phi += phistep) {
                        double x = ip.use_power * Math.sin(theta) * Math.cos(phi);
                        double y = ip.use_power * Math.sin(theta) * Math.sin(phi);
                        double z = ip.use_power * Math.cos(theta);
                        //b_location.getWorld().spawnParticle(Particle.REDSTONE, b_location.clone().add(new Vector(x,y,z)), 1, 0.1, 0.1, 0.1, 0.5, dust, true);
                        Location tlocation = location.clone().add(new Vector(x, y, z).rotateAroundY(-Math.toRadians(yaw)).rotateAroundX(Math.toRadians(pitch) * Math.cos(Math.toRadians(yaw))).rotateAroundZ(Math.toRadians(pitch) * Math.sin(Math.toRadians(yaw))));
                        //location.getWorld().spawnParticle(Particle.REDSTONE, tlocation, 1, 0.1, 0.1, 0.1, 0.5, dust, true);
                        tlocation.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tlocation, 1, 0.01, 0.01, 0.01, 0);
                    }
                    theta += step;
                }
            } else if (type == 'p') {
                if (theta >= Math.PI * (0.6)) {
                    Bukkit.getScheduler().cancelTask(task_num);
                } else {
                    phistep = step / Math.sin(theta == 0 || theta == Math.PI ? step : theta);
                    for (double phi = 0; phi < 2 * Math.PI; phi += phistep) {
                        double x = ip.use_power / 2 * Math.sin(theta) * Math.cos(phi);
                        double y = ip.use_power / 2 * Math.sin(theta) * Math.sin(phi);
                        double z = ip.use_power / 2 * Math.cos(theta);
                        //b_location.getWorld().spawnParticle(Particle.REDSTONE, b_location.clone().add(new Vector(x,y,z)), 1, 0.1, 0.1, 0.1, 0.5, dust, true);
                        Location tlocation = location.clone().add(new Vector(x, y, z).rotateAroundY(-Math.toRadians(yaw)).rotateAroundX(Math.toRadians(pitch) * Math.cos(Math.toRadians(yaw))).rotateAroundZ(Math.toRadians(pitch) * Math.sin(Math.toRadians(yaw))));
                        //location.getWorld().spawnParticle(Particle.REDSTONE, tlocation, 1, 0.1, 0.1, 0.1, 0.5, dust, true);
                        tlocation.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tlocation, 1, 0.01, 0.01, 0.01, 0);
                    }
                    theta += step;
                }
            }
        }
        catch (Exception e){
            PaperJJK.log( e.getClass().getSimpleName() + " - " + e.getMessage());
            Bukkit.getScheduler().cancelTask(task_num);
        }
    }
}