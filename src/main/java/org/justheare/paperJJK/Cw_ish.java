package org.justheare.paperJJK;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Cw_ish extends Jujut {

    public Cw_ish(Jobject jobject, String spe_name, String type, boolean rct, int power, int time, char target) {
        super(jobject, spe_name, type, rct, power, time, target);
        this.time = 10;
        jobject.ish_depence=true;
        user.getWorld().playSound(user, Sound.ITEM_TRIDENT_THROW,1F, 0.7F);


        /*for(int r=0; r<10; r++){
            location=location.add(location.getDirection().multiply(0.5));
            location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,location,1,0.01,0.01,0.01,0);
            PaperJJK.getjujuts()
        }
        */
    }
    @Override
    public void run(){
        if(time>0){
            time--;
            if(user instanceof Player player){
                location.getWorld().spawnParticle(Particle.SWEEP_ATTACK,player.getEyeLocation().add(player.getEyeLocation().getDirection()),1,0.1,0.1,0.1,2);
            }
            else {
                location.getWorld().spawnParticle(Particle.SWEEP_ATTACK,user.getLocation().add(0,1,0).add(user.getLocation().getDirection()),1,0.1,0.1,0.1,2);
            }
        }
        if(time == 0){
            disables();
        }
        if(time>5){
            //jobject.user.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,jobject.user.getLocation(),1,0.01,0.01,0.01,0);
        }
        if(time==5){
            jobject.ish_depence=false;
        }
    }
}
