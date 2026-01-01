package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class Infinity_domain extends Jdomain_innate{
    public Infinity_domain(Jobject owner) {
        super(owner);
        range = 30;
        level=9;
        innate_border = Material.BARRIER;
    }
    void start_effect(){
        Infinity_effector effector = new Infinity_effector(this);
        this.effector=effector;
        effector.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, effector, 1, 1);
    }
    void expand_effect(boolean nb){
        if(nb){
            owner.user.getWorld().playSound(owner.user.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 5F, 0.5F);
            for(Player targetplayer : owner.user.getLocation().getNearbyPlayers(nb_range)){
                targetplayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,40,1,false));
            }
        }
        else {
            owner.user.getWorld().playSound(owner.user.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 5F, 0.5F);
        }
    }
    void tp_effect(){
        effector.tick=0;
        //owner.user.getWorld().playSound(owner.user.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 5F, 0.5F);
    }
}
class Infinity_effector extends Jdomain_effector{
    Infinity_domain domain;

    Infinity_effector(Infinity_domain domain) {
        super(domain);
        this.domain=domain;
    }
    public void effect_tick(){
        if(tick==5){
            domain.location.getWorld().playSound(domain.location, Sound.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 2F, 0.7F);
            domain.location.getWorld().playSound(domain.location, Sound.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS,2F, 1.3F);
        }
        if(tick%400==10){
            domain.location.getWorld().playSound(domain.location, Sound.AMBIENT_BASALT_DELTAS_LOOP, SoundCategory.BLOCKS, 2F, 1.5F);
        }
        if(domain.attacker==null){
            ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) domain.location.getNearbyLivingEntities(domain.range+1);
            ArrayList<LivingEntity> tee=new ArrayList<>();
            for(LivingEntity living : tentities){
                if(living.getLocation().distance(domain.location)<= domain.range+1){
                    living.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,5,2));
                    if(living.equals(domain.owner.user)){
                        living.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,20,2));
                    }
                    else {
                        if(living instanceof BlockDisplay){
                            continue;
                        }
                        Jobject jobject = PaperJJK.getjobject(living);
                        if(jobject!=null){
                            jobject.infinity_stun_tick+=10;
                            if(jobject.naturaltech.equals("mahoraga")&&jobject.jujuts.get(0) instanceof Mahoraga mahoraga){
                                if(mahoraga.pre_adapt("infinity_domain","curse",1)<0){
                                    jobject.infinity_stun_tick=0;
                                    tee.add(living);
                                    continue;
                                }
                            }
                            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,jobject.infinity_stun_tick,3));
                            living.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,jobject.infinity_stun_tick,1));
                            if(jobject.user instanceof Player player){
                                player.setCooldown(Material.WRITTEN_BOOK,jobject.infinity_stun_tick);
                            }
                            else {
                                // AI Goal만 제거, 물리는 유지
                                living.setAI(false);
                            }
                        }
                        else {
                            // AI Goal만 제거, 물리는 유지
                            living.setAI(false);
                        }
                    }
                    tee.add(living);
                }
            }
            domain.domain_targets=tee;
        }
    }
}
