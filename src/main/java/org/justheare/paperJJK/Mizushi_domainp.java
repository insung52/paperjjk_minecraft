package org.justheare.paperJJK;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Mizushi_domainp extends Domain{
    public Mizushi_domainp(Jobject jobject){
        super(jobject);
        level=5;
    }

    void set_expand_location(Location target_location, boolean no_border, int radius, String target){
        super.set_expand_location(target_location,no_border,radius,target);
        Mizushi_effect effect = new Mizushi_effect(this,(target.equals("u")));
        this.effect = effect;
        effect.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, effect, 1, 1);
        expand_location.getWorld().playSound(expand_location,Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 5F, 0.5F);
        for(Player targetplayer : expand_location.getNearbyPlayers(radius)){
            targetplayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,40,1,false));
        }
    }
    void stop_expand(){
        super.stop_expand();
    }
}

class Mizushi_effect extends Domain_effect{
    Particle.DustOptions dark_dust2=new Particle.DustOptions(Color.fromRGB(60,0,0), 0.5F);
    public float breakblock(Location break_location,int breakpower){
        Block break_block=break_location.getBlock();
        float rr=break_block.getType().getHardness();
        if(rr>-1) {
            if (rr < breakpower || break_block.isLiquid()) {
                break_block.setType(Material.AIR);
            }
        }
        return rr;
    }
    int speed=200000;
    int fire=0;
    double tick1=0;
    double tick2=0;
    double rx,ry,rz;
    boolean first=true;
    int current_radius=1;
    ArrayList<LivingEntity> fuga_hit;
    void set_special(){
        fuga_hit = new ArrayList<LivingEntity>();
        special=true;
        current_radius=1;
        domain.expand_location.getWorld().playSound(domain.expand_location,Sound.BLOCK_END_PORTAL_SPAWN, 80F, 0.5F);
        domain.jobject.player.setCooldown(Material.WRITTEN_BOOK,300);
    }
    Mizushi_effect(Domain domain,boolean onground){
        this.domain=domain;
        this.onground=onground;
    }
    @Override
    public void run() {
        super.run();
        if(domain.delay==1){
            domain.expand_location.getWorld().playSound(domain.expand_location,Sound.ENTITY_ENDER_DRAGON_AMBIENT, 80F, 0.5F);
        }
        if(special){
            for(int r=0; r<20000; r++){
                rx = Math.sin(tick1 / current_radius / 4 * Math.PI) * Math.sin(tick2 / current_radius / 4 * Math.PI) * current_radius;
                ry = Math.cos(tick2 / current_radius / 4 * Math.PI) * current_radius;
                rz = Math.cos(tick1 / current_radius / 4 * Math.PI) * Math.sin(tick2 / current_radius / 4 * Math.PI) * current_radius;
                if(onground&&rx<0){
                    rx=-rx;
                }
                Location tlocation = domain.expand_location.clone().add(ry, rx-4-Math.random(), rz);
                if(tlocation.getBlock().isEmpty()){
                    if(Math.random()>Math.pow(current_radius*1.0/200.0,0.3)){
                        tlocation.getWorld().spawnParticle(Particle.FLAME, tlocation, 1, 1, 1, 1, 0.1, null, true);
                    }

                    /*if(tlocation.clone().add(0,-1,0).getBlock().isSolid()){
                        tlocation.getBlock().setType(Material.FIRE);
                    }*/
                }
                else if(Math.random()>0.2){
                    tlocation.createExplosion(domain.jobject.player,5,true);
                }
                tick1+=1.2;
                if (tick1 >= ((onground)?(current_radius * 4):(current_radius*8))) {
                    tick1 = 0;
                    tick2+=3;
                    if (tick2 >= ((onground)?(current_radius * 4):(current_radius*8))) {
                        current_radius+=3;
                        tick1=0;
                        tick2=0;
                        ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) domain.expand_location.getNearbyLivingEntities(current_radius);
                        tentities.remove(domain.jobject.user);
                        tentities.removeAll(fuga_hit);
                        for(LivingEntity tentity : tentities){
                            if(tentity.getLocation().distance(domain.expand_location)>current_radius){
                                continue;
                            }
                            if(onground){
                                if(tentity.getLocation().getY()<domain.expand_location.getY()-5){
                                    continue;
                                }
                            }
                            fuga_hit.add(tentity);
                            tentity.setFireTicks(4444);
                            domain.jobject.damaget(tentity,'j',domain.expand_radius*4,false,"fuga",false);
                        }
                        if (current_radius >= domain.expand_radius) {
                            special=false;
                            current_radius=domain.expand_radius;
                            domain.stop_expand();
                            break;
                        }
                        if(current_radius%4==0){
                            break;
                        }
                    }
                }
            }
        }
        else if(domain.expand_built){
            if(domain.is_no_border){
                if(first){
                    //PaperJJK.log("running");
                    for (int r = 0; r < speed; r++) {
                        rx = Math.sin(tick1 / current_radius / 4 * Math.PI) * Math.sin(tick2 / current_radius / 4 * Math.PI) * current_radius+(Math.random()-0.5);
                        ry = Math.cos(tick2 / current_radius / 4 * Math.PI) * current_radius+(Math.random()-0.5);
                        rz = Math.cos(tick1 / current_radius / 4 * Math.PI) * Math.sin(tick2 / current_radius / 4 * Math.PI) * current_radius+(Math.random()-0.5);
                        if(onground&&rx<1){
                            rx=1;
                        }
                        Location tlocation = domain.expand_location.clone().add(ry, rx-4, rz);
                        if(tlocation.getBlock().isLiquid()){
                            tlocation.getBlock().setType(Material.AIR);
                        }
                        else if(!tlocation.getBlock().isEmpty()){
                            if(tlocation.getBlock().getType().getHardness()<4){
                                if(tlocation.getBlock().getType().getHardness()>-1){
                                    tlocation.getBlock().setType(Material.AIR);
                                }
                            }
                            else if(Math.random()<=0.1/tlocation.getBlock().getType().getHardness()){
                                tlocation.getBlock().setType(Material.AIR);
                            }
                        }
                        else if(Math.random()>Math.pow(current_radius*1.0/200.0,0.01)){
                            Particle.DustOptions dust=new Particle.DustOptions(Color.WHITE, (float) (200 - current_radius) /50);
                            tlocation.getWorld().spawnParticle(Particle.DUST, tlocation, 1, 0, 0, 0, 1, dust, true);
                        }
                        tick1+=0.7;
                        if (tick1 >= ((onground)?(current_radius * 4):(current_radius*8))) {
                            tick1 = 0;
                            tick2+=0.7;
                            if (tick2 >= ((onground)?(current_radius * 4):(current_radius*8))) {
                                current_radius++;
                                tick1=0;
                                tick2=0;
                                if(current_radius%4==0){
                                    break;
                                }
                                if (current_radius >= domain.expand_radius) {
                                    first=false;
                                    break;
                                }
                            }
                        }
                    }
                }
                for(int r=0; r<Math.pow(current_radius,2.5)/60; r++){
                    Vector r_vector = new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5).normalize();
                    Vector l_vector = new Vector(Math.random()-0.5,onground?(Math.random()/2):(Math.random()-0.5),Math.random()-0.5).normalize().multiply(Math.pow(Math.random(),0.35)*current_radius);
                    Location ss_location = domain.expand_location.clone().add(l_vector);
                    for (double rr = -9; rr <=  9; rr+=1) {
                        //t_location.clone().add(direction.multiply(r));
                        Location s_location= ss_location.clone().add(r_vector.multiply(rr/5));
                        if(s_location.getBlock().isLiquid()){
                            s_location.getBlock().setType(Material.AIR);
                        }
                        else if(!s_location.getBlock().isEmpty()){
                            if(s_location.getBlock().getType().getHardness()<5){
                                if(s_location.getBlock().getType().getHardness()>-1){
                                    s_location.getBlock().setType(Material.AIR);
                                }
                            }
                            else if(Math.random()<=1/s_location.getBlock().getType().getHardness()){
                                s_location.getBlock().setType(Material.AIR);
                            }
                        }
                        s_location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, s_location, 1, 0, 0, 0, 0, null, false);
                        s_location.getWorld().spawnParticle(Particle.DUST, s_location, 1, 0, 0, 0, 0, dark_dust2, false);
                    }
                    if(Math.random()>0.1){
                        ss_location.getWorld().playSound(ss_location, Sound.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 0.5F, 0.5F);
                        ss_location.getWorld().playSound(ss_location, Sound.ITEM_TRIDENT_RIPTIDE_3, SoundCategory.PLAYERS, 0.5F, 0.7F);
                    }
                }


            }
            tick++;
            if(tick>=10){
                tick=0;
                List<Entity> tentities = domain.expand_location.getWorld().getEntities();
                tentities.remove(domain.jobject.user);
                for(Entity tentity : tentities){
                    if(tentity instanceof BlockDisplay){
                        continue;
                    }
                    if(tentity.getLocation().distance(domain.expand_location)<current_radius){
                        if(onground){
                            if(tentity.getLocation().getY()<domain.expand_location.getY()-5){
                                continue;
                            }
                        }
                        Mizushi mizushi = new Mizushi(domain.jobject,"","",true,1,20,'a');
                        mizushi.show=false;
                        mizushi.sure_hit=true;
                        mizushi.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,mizushi,1,1);
                        domain.jobject.jujuts.add(mizushi);
                        mizushi.j_entities.add(tentity);
                        mizushi.direction = new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5).normalize();
                    }
                }
            }
        }
    }
}