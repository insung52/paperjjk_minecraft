package org.justheare.paperJJK;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Mizushi_fuga extends Jujut{

    public Mizushi_fuga(Jobject jobject, String spe_name, String type, boolean rct, int power, int time, char target) {
        super(jobject, spe_name, type, rct, power, time, target);
        use_power=5;
        fixable=false;
        setcurrent(10,100);
        this.time=100;
        delay=20;
    }
    void hit(){
        location.createExplosion(user,(float) (use_power/25+2),true);

        if(jobject.innate_domain.isexpanded&&jobject.innate_domain.no_border_on&&jobject.innate_domain.nb_location.distance(location)<jobject.innate_domain.nb_range){
            jobject.innate_domain.set_special();
        }
        else {

            List<Entity> entities = (List<Entity>) location.getNearbyEntities(Math.pow(use_power,0.3)+1,Math.pow(use_power,0.3)+1,Math.pow(use_power,0.3)+1);
            entities.remove(user);
            for(Entity tentity : entities){
                if(tentity.getScoreboardTags().contains("hachi")&&tentity.getScoreboardTags().contains("kai")){
                    if(tentity instanceof LivingEntity living){
                        jobject.damaget(living,'j',use_power*4/entities.size(),false,"fuga",false);
                        location.getWorld().spawnParticle(Particle.FLAME, location, (int) (use_power/3+30), 1, 1, 1, 1, null, true);
                        location.getWorld().spawnParticle(Particle.LAVA, location, (int) (use_power/3+30), 1, 1, 1, 1, null, true);
                        location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER,location,1,0,0,0,0,null,false);
                    }
                    tentity.setFireTicks(4444);
                }
                else{
                    tentity.setFireTicks(40);
                }

            }
        }
    }

    @Override
    public void charged() {
        super.charged();
        if(user instanceof Player player){
            player.setCooldown(Material.WRITTEN_BOOK,40);
        }
    }

    @Override
    public void run(){
        maintick();
        if(delay>0||charging){
            jobject.player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,200,1));
            location = jobject.player.getEyeLocation();

                s_location = location.clone();
                s_location.setPitch(0);
                s_location.setYaw(location.getYaw() + 90);
                Vector r_vector = s_location.getDirection();    //y 가 제거된 오른쪽 벡터
                Vector h_vector = r_vector.clone().crossProduct(location.getDirection()); //머리 벡터
                Vector rr_vector= location.getDirection().clone().rotateAroundAxis(h_vector,Math.PI/2);
                Vector n_vector = h_vector.clone().rotateAroundAxis(location.getDirection(), -Math.PI / 6);    //기울인 머리
                Vector l_vector = location.getDirection().clone().rotateAroundAxis(h_vector, Math.PI / 6);     //완성 왼쪽 벡터
            if(Math.random()>0.8){
                location.getWorld().playSound(location.clone().add(l_vector), Sound.BLOCK_FIRE_AMBIENT,2F,1F);
            }
            for (double r = -5; r <= 5; r += 0.3) {
                if(Math.random()*100<=use_power){
                    location.getWorld().spawnParticle(Particle.FLAME,location.clone().add(rr_vector.clone().multiply(-0.7)).add(location.clone().getDirection().multiply((r)/2)).add(h_vector.clone().multiply(-0.6)),1,0.01,0.01,0.01,0.001,null,true    );
                    location.getWorld().spawnParticle(Particle.FLAME, location.clone().add(l_vector.clone().multiply(2.5).add(n_vector.clone().multiply(r)).add(l_vector.clone().multiply(-Math.pow(r, 2) / 10))), 1, 0.04, 0.04, 0.04, 0.02, null, true);
                }

            }

        }
        if(time==99){
            location.getWorld().playSound(location.add(location.getDirection()),Sound.ITEM_FIRECHARGE_USE,3F,0.5F);
        }
        if(time==97){
            direction = location.getDirection().normalize().multiply(1.5);
        }
        if(time<97){
            for(int r=0; r<5; r++){
                direction.add(new Vector(0,-0.009,0));
                location.add(direction);
                location.getWorld().spawnParticle(Particle.FLAME, location, (int) (use_power/10+1), 1, 1, 1, 0, null, true);
                if(location.getBlock().isSolid()){
                    hit();
                    time=0;
                    break;
                }

                else {
                    ArrayList<Entity> tltl= (ArrayList<Entity>) location.getNearbyEntities(1, 1, 1);
                    tltl.remove(user);
                    if(!tltl.isEmpty()){
                        hit();
                        time=0;
                        break;
                    }

                }
            }
        }
    }
    public String toname(){

        return ChatColor.RED+"fuga"+ChatColor.WHITE+" *"+use_power+" , "+time/20;

    }
}
