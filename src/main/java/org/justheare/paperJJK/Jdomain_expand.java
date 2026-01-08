package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static org.justheare.paperJJK.PaperJJK.getjobject;

public class Jdomain_expand extends Jdomain{
    Jdomain_Builder expand_builder;
    Jdomain_manager manager;
    public Jdomain_expand(Jobject owner, int range) {
        super(owner);
        this.range=range;
        this.location=owner.user.getLocation();
    }
    void translate(ArrayList<LivingEntity> tentities,Location from_location, int from_range, Location to_location, int to_range){
        for(LivingEntity living : tentities){
            living.setPersistent(true);

            Vector distance=living.getLocation().toVector().subtract(from_location.toVector());
            if(distance.length()>=to_range-1){
                distance.normalize().multiply(to_range-2);
            }
            Location tl = to_location.clone().add(distance).setDirection(living.getLocation().getDirection());
            if(living instanceof Player){
                //PaperJJK.log(living.getLocation() + " -> " + tl);
            }
            for(int r=0; r<to_range; r++){
                if(tl.getBlock().isPassable()){
                    break;
                }
                else {
                    tl.add(0,1,0);
                }
            }
            if(!living.getPassengers().isEmpty()){
                List<Entity> passengers = new ArrayList<>(living.getPassengers());
                living.eject();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (Entity passenger : passengers) {
                            living.addPassenger(passenger); // Block Display 다시 태우기
                        }
                    }
                }.runTaskLater(PaperJJK.jjkplugin, 2L); // 2틱 후 다시 태움 (0.1초 후)
            }
            living.teleport(tl);
        }

    }

    void expand(){
        owner.innate_domain.domain_targets=new ArrayList<LivingEntity>();
        owner.innate_domain.isexpanding=true;
        expand_builder=new Jdomain_Builder(this,range, Material.OBSIDIAN,location);
        expand_builder.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, expand_builder, 1, 1);
        Jdomain_manager manager = new Jdomain_manager(this);
        this.manager=manager;
    }
    void destroy(){
        owner.innate_domain.isexpanding=true;
        owner.innate_domain.isexpanded=false;
        expand_builder.build_mode=false;
        expand_builder.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, expand_builder, 1, 1);
        Bukkit.getScheduler().cancelTask(manager.task_num);
    }

    void build_finished(){
        owner.innate_domain.isexpanding=false;
        owner.innate_domain.isexpanded=true;
        isbuilding=false;
        owner.user.sendMessage("domain expansion finished.");
        ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) location.getNearbyLivingEntities(range);
        ArrayList<LivingEntity> tee=new ArrayList<>();
        for(LivingEntity living : tentities){
            if(living.getLocation().distance(location)>=range){
                continue;
            }
            tee.add(living);
            Jobject jobject=getjobject(living);
            if(jobject!=null){
                if(jobject.innate_domain!=null){
                    if(jobject.innate_domain.no_border_on){
                        owner.innate_domain.attacker=jobject;
                        jobject.innate_domain.attack_target=this;

                    }
                }
            }
        }
        translate(tentities, location,range,owner.innate_domain.location,owner.innate_domain.range);

        owner.innate_domain.domain_targets=tentities;
        //범위 내 엔티티 innate 로 이동
        manager.task_num=Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, manager, 1, 1);
    }
    void destroy_finished(){
        owner.innate_domain.isexpanding=false;
        owner.innate_domain.isexpanded=false;
        owner.user.sendMessage("domain destroy finished.");
    }
}
