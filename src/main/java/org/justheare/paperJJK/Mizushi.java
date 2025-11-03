package org.justheare.paperJJK;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Mizushi extends Jujut{
    Particle.DustOptions dust=new Particle.DustOptions(Color.RED, 0.3F);
    Particle.DustOptions dark_dust=new Particle.DustOptions(Color.fromRGB(60,0,0), 0.3F);
    Particle.DustOptions dark_dust2=new Particle.DustOptions(Color.fromRGB(60,0,0), 1F);
    boolean kai_hit = false;
    boolean sure_hit=false;
    public Mizushi(Jobject jobject, String spe_name, String type, boolean rct, int power, int time, char target) {
        super(jobject, spe_name, type, rct, power, time, target);
        kai_hit = false;
        if(rct){
            j_entities=new ArrayList<>();
            charging=false;
            use_power=power;
            fixable=false;
            setcurrent(4,100);
            this.time=time;

        }
        else{
            if(user instanceof Player player){
                player.setCooldown(Material.WRITTEN_BOOK,0);
            }
            use_power=5;
            fixable=false;
            this.time=17;
            setcurrent(4,100);
            j_entities=new ArrayList<Entity>();
        }
    }
    @Override
    public void charged(){
        if(reversecurse){
            if(user instanceof Player player){
                player.setCooldown(Material.WRITTEN_BOOK,40);
            }
        }
        else{
            if(user instanceof Player player){
                player.setCooldown(Material.WRITTEN_BOOK,0);
            }
        }
        if(user instanceof Player player){
            location = player.getEyeLocation();
        }
        else {
            location = user.getLocation();
        }
    }
    @Override
    public void run() {
        if(reversecurse){
            maintick();
            if(time<6){
                for(Entity target : j_entities){
                    for(int rr=0; rr<3; rr++){
                        Vector r_offset = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).multiply(2);
                        Vector line = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5).normalize();
                        //line = line.clone().subtract(location.getDirection().multiply(line.dot(location.getDirection()))).normalize();
                        for (double r = -5; r <= 5; r += 0.5) {
                            //target.getWorld().spawnParticle(Particle.DUST, target.getLocation().clone().add(direction).add(r_offset).add(line.clone().multiply(r / 3)), 2, 0, 0, 0, 0, dust, true);
                            target.getWorld().spawnParticle(Particle.DUST, target.getLocation().clone().add(direction).add(r_offset).add(line.clone().multiply(r / 3)), 2, 0, 0, 0, 0, dark_dust, false);

                        }
                    }
                }
            }
            if(time==0){
                for(Entity target : j_entities){
                    Jobject targetj = PaperJJK.getjobject(target);
                    target.getWorld().playSound(target.getLocation(), Sound.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.5F, 0.5F);
                    if(targetj==null){
                        if(target instanceof LivingEntity living){
                            jobject.damaget(living,'j',Math.pow(jobject.curseenergy,0.10)+use_power/10,false,"hachi",sure_hit);
                            living.addScoreboardTag("hachi");
                        }
                        else if(!target.getType().equals(EntityType.BLOCK_DISPLAY)){
                            target.remove();
                        }

                    }
                    else{
                        if(Math.pow(targetj.curseenergy,0.2)<5){

                            jobject.damaget((LivingEntity) targetj.user,'j',Math.pow(jobject.curseenergy,0.15)+use_power/10,false,"hachi",false);
                        }
                        else{
                            jobject.damaget((LivingEntity) targetj.user,'j',(Math.pow(jobject.curseenergy,0.15)-Math.pow(targetj.curseenergy,0.05))+use_power/10,false,"hachi",sure_hit);
                        }
                        //PaperJJK.log(Math.pow(jobject.curseenergy,0.2)+" "+Math.pow(targetj.curseenergy,0.2)+" d");

                        targetj.user.addScoreboardTag("hachi");
                    }
                }
            }
            /*
            if(j_entities.size()>0){
                for(Entity target : j_entities){

                }
            }*/
        }
        else {
            maintick();
            if(!charging){
                if(time==16){
                    if(user instanceof Player player){
                        t_location = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1));
                    }
                    else {
                        t_location = user.getLocation();
                    }
                    direction = t_location.getDirection().clone().add(location.getDirection().clone().multiply(-1));
                    if(direction.length()<0.001){
                        direction = new Vector(Math.random()-0.5,Math.random()-0.5, Math.random()-0.5);
                    }
                    t_location.setDirection(t_location.getDirection().add(new Vector(Math.random()-0.5,Math.random()-0.5, Math.random()-0.5).multiply(0.1)));
                    direction = direction.normalize().subtract(t_location.getDirection().clone().multiply(direction.dot(t_location.getDirection())));
                    t_location.getWorld().playSound(t_location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.1F, 0.8F);
                }
                if(time<=16){
                    for(int rp=0; rp<10; rp++){
                        t_location.add(t_location.getDirection().normalize().multiply(0.4));
                        for (double r = ((double) -use_power / 8 )*(17-time)/7-1; r <= ((double) use_power / 8)*(17-time)/7+1; r+=0.5) {
                            boolean sps=false;
                            //t_location.clone().add(direction.multiply(r));
                            s_location= t_location.clone().add(direction.clone().multiply(r));

                            List<Entity> targets = (List<Entity>) s_location.getNearbyEntities(1, 1, 1);
                            targets.remove(user);
                            if(targets.size()>0){
                                sps=true;
                            }
                            targets.removeAll(j_entities);

                            if(!targets.isEmpty()){
                                if(!kai_hit){
                                    kai_hit=true;
                                    s_location.getWorld().playSound(s_location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 6F, 0.6F);
                                    s_location.getWorld().playSound(s_location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.9F, 0.6F);
                                }
                                for (Entity target : targets){
                                    target.setVelocity(target.getVelocity().add(t_location.getDirection().clone().multiply(0.1)));
                                    if(target instanceof LivingEntity ltarget){
                                        j_entities.add(ltarget);
                                        jobject.damaget(ltarget, 'j', Math.pow(jobject.curseenergy,0.11) + use_power-3, false,"kai",false);
                                        ltarget.addScoreboardTag("kai");

                                    }
                                    else if(!target.getType().equals(EntityType.BLOCK_DISPLAY)){
                                        target.remove();
                                    }
                                }
                            }

                            Block jlb=s_location.getBlock();
                            BlockData db = jlb.getBlockData();

                            if(jlb.isLiquid()){
                                breakblock(s_location, (int) use_power);
                                sps=true;
                            }
                            else if(!jlb.isEmpty()){
                                if(!kai_hit){
                                    kai_hit=true;
                                    s_location.getWorld().playSound(s_location, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 6F, 0.6F);
                                    s_location.getWorld().playSound(s_location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.9F, 0.6F);
                                }
                                if(Math.random()>0.9){
                                    s_location.getWorld().playSound(s_location, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, SoundCategory.PLAYERS, 0.2F, 0.6F);
                                }

                                use_power-=Math.pow(breakblock(s_location, (int) use_power),1.3)/5;
                                sps=true;
                            }

                            if(sps){
                                t_location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, s_location, 1, 0, 0, 0, 0, null, true);
                                t_location.getWorld().spawnParticle(Particle.DUST, s_location, 1, 0, 0, 0, 0, dark_dust2, true);
                                t_location.getWorld().spawnParticle(Particle.BLOCK, s_location, 8, 0.5, 0.5, 0.5, 1, db, false);
                            }
                        }
                    }
                }
            }
        }
    }
    public String toname(){
        if(reversecurse){
            return ChatColor.DARK_RED+"hachi"+ChatColor.WHITE+" *"+use_power+" , "+time/20;
        }
        else{
            return ChatColor.RED+"kai"+ChatColor.WHITE+" *"+use_power+" , "+time/20;
        }
    }
}
