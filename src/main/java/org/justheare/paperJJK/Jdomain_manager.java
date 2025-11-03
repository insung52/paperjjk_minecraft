package org.justheare.paperJJK;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;

public class Jdomain_manager implements Runnable{
    int task_num;
    Jdomain_expand expand;
    int break_count=0;
    int level_diff=0;
    int level_timeout=0;
    int tick=0;
    Jdomain_manager (Jdomain_expand expand){
        this.expand=expand;
    }
    @Override
    public void run() {
        //0~10
        //3 can expand domain
        if(level_diff!=0){
            level_timeout+= level_diff*level_diff*level_diff;
            if(level_timeout>20000){
                expand.owner.innate_domain.attacker.user.sendMessage("your domain pushed out!");
                expand.owner.innate_domain.attacker.innate_domain.destroy_expand();
                level_diff=0;
                level_timeout=0;
            }
            else if(level_timeout<-20000){
                expand.owner.user.sendMessage("your domain pushed out!");
                expand.owner.innate_domain.destroy_expand();
                level_diff=0;
                level_timeout=0;
            }
        }
        if(expand.owner.user instanceof LivingEntity living){
            if(living.getHealth()<living.getMaxHealth()*0.5){
                living.sendMessage("too low health!");
                expand.owner.innate_domain.destroy_expand();
            }

        }
        break_count=0;
        for(Block block : expand.expand_builder.saved_blocks){
            if(block!=null){
                if(!block.getType().equals(Material.OBSIDIAN)&&block.getY()>=-64){
                    break_count++;
                }
            }
        }
        //PaperJJK.log(break_count+" "+expand.range*5);
        if(break_count> expand.range*5){//50 : 3/10, 10 : 7/10
            expand.owner.user.sendMessage("border destroyed!");
            expand.owner.innate_domain.destroy_expand();
        }
        tick++;
        if(tick>=5){
            tick=0;
            ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) expand.location.getNearbyLivingEntities(expand.range);
            ArrayList<LivingEntity> tee=new ArrayList<>();
            for(LivingEntity living : tentities){
                if(living.getLocation().distance(expand.location)>=expand.range-1){
                    continue;
                }
                tee.add(living);
            }
            expand.translate(tee, expand.location,expand.range,expand.owner.innate_domain.location,expand.owner.innate_domain.range);
        }
    }
}
