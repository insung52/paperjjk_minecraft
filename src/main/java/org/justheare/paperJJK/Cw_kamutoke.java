package org.justheare.paperJJK;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

public class Cw_kamutoke extends Jujut{
    public Cw_kamutoke(Jobject jobject, String spe_name, String type, boolean rct, int power, int time, char target) {
        super(jobject, spe_name, type, rct, power, time, target);
        this.time=40;
        user.getWorld().playSound(user, Sound.ENTITY_LIGHTNING_BOLT_THUNDER,5F, 0.5F);
        location = user.getLocation().add(0,1,0);
        for(double r=3; r<60; r++){
            location = location.add(location.getDirection().multiply(1));
            location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,location,1,0.1,0.1,0.1,0);
            if(!location.getBlock().isPassable()){
                break;
            }
            ArrayList<Entity> tentities = (ArrayList<Entity>) location.getNearbyEntities(2,2,2);
            tentities.remove(user);
            if(!tentities.isEmpty()){
                break;
            }
        }
    }

    @Override
    public void run() {
        if(time<20){
            Location tl = location.clone().add((Math.random()-0.5)*2,(Math.random()-0.5)*2,(Math.random()-0.5)*2);
            location.getWorld().spawnEntity(tl, EntityType.LIGHTNING_BOLT);
            if(time%4==0){
                tl.createExplosion(user,5,true);
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
