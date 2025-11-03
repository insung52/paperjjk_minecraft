package org.justheare.paperJJK;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;

public class Cw_kamutoke extends Jujut{
    public Cw_kamutoke(Jobject jobject, String spe_name, String type, boolean rct, int power, int time, char target) {
        super(jobject, spe_name, type, rct, power, time, target);
        this.time=100;
        //user.getWorld().playSound(user, Sound.ENTITY_LIGHTNING_BOLT_THUNDER,5F, 0.5F);
        location = user.getLocation().add(0,1,0);
        location.getWorld().setStorm(true);

        location.getWorld().setThundering(true);
        location.getWorld().setWeatherDuration(20*60);
        location.getWorld().setThunderDuration(20*60);
        location.add(location.getDirection().clone().multiply(5));
        for(double r=3; r<60; r++){
            location.add(location.getDirection());
            //location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,location,1,0.1,0.1,0.1,0);
            if(!location.getBlock().isPassable()){
                break;
            }
            ArrayList<Entity> tentities = (ArrayList<Entity>) location.getNearbyEntities(2,2,2);
            tentities.remove(user);
            if(!tentities.isEmpty()){
                break;
            }
        }
        location.getWorld().playSound(location,Sound.ENTITY_LIGHTNING_BOLT_THUNDER,5F,0.5F);
    }

    @Override
    public void run() {
        if(time>20){
            location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,location,(100-time),4,4,4,0,null,true);
        }
        if(time<20){
            Location tl = location.clone().add((Math.random()-0.5)*4,(Math.random()-0.5)*4,(Math.random()-0.5)*4);
            location.getWorld().spawnEntity(tl, EntityType.LIGHTNING_BOLT);
            if(time%4==0){
                tl.createExplosion(user,3,PaperJJK.rule_breakblock,PaperJJK.rule_breakblock);
            }
        }
        if(time>0) {
            time--;
        }
        if(time == 0){
            disables();
        }
    }
}
