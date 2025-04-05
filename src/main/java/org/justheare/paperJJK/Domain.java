package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Domain {
    int delay=20;
    Jobject jobject;
    int level=0;
    Location origin_location;
    int origin_radius;
    DomainBuilder origin_builder;
    boolean origin_building=false;
    boolean origin_built=false;
    Location expand_location;
    int expand_radius;
    DomainBuilder expand_builder;
    boolean expand_building=false;
    boolean expand_built=false;
    boolean is_expand_built(){
        return expand_built;
    }
    boolean is_no_border=false;
    String expand_target;
    Block[][] blocks=new Block[2][];
    Material[][] materials=new Material[2][];
    Domain_effect effect;
    public Domain(Jobject jobject){
        this.jobject=jobject;
        level=0;
    }
    void set_origin_location(Location target_location){
        origin_location=target_location;
        origin_building=true;
    }
    void created(){
        if(origin_building){
            origin_built=true;
            origin_building=false;
        }
        else if(expand_building){
            expand_built=true;
            expand_building=false;
        }
    }
    void set_expand_location(Location target_location, boolean no_border, int radius, String target){
        jobject.player.sendMessage("domain expand...");
        this.expand_radius=radius;
        expand_location=target_location;
        is_no_border=no_border;
        expand_target=target;
        delay=20;
    }
    void stop_expand(){
        expand_built=false;
        effect.stop();
    }


}
class Domain_effect implements Runnable{
    boolean special = false;
    boolean onground;
    Domain domain;
    int tick=0;
    int tasknum;
    void set_special(){

    }
    @Override
    public void run() {
        if(domain.jobject.curseenergy<10000){
            domain.stop_expand();
            domain.jobject.player.sendMessage("not enough cursed energy!");
        }
        if(domain.delay>0){
            domain.delay--;
        }
        if(domain.delay==0) {
            domain.expand_built=true;
        }
    }
    void stop(){
        Bukkit.getScheduler().cancelTask(tasknum);
    }
}

class DomainBuilder implements Runnable{
    boolean isorigin;
    boolean isbuild;
    Location location;
    int speed=100;
    double tick1=0;
    double tick2=0;
    Domain domain;
    int radius;
    Material border_material;
    double rx,ry,rz;
    int block_count=0;
    public DomainBuilder(Domain domain, boolean isorigin, boolean isbuild, int radius, Material border_material,Location location){
        this.domain=domain;
        this.radius=radius;
        this.border_material=border_material;
        this.location=location;
        this.isorigin=isorigin;
        this.isbuild=isbuild;
        if(isbuild){
            domain.blocks[(isorigin)?1:0]=new Block[radius*radius*70];
            domain.materials[(isorigin)?1:0]=new Material[radius*radius*70];
        }
    }
    @Override
    public void run() {
        for(int r=0; r<speed; r++){
            rx=Math.sin(tick1 /radius/4*Math.PI)*Math.sin(tick2 /radius/4*Math.PI)*radius;
            ry=Math.cos(tick2 /radius/4*Math.PI)*radius;
            rz=Math.cos(tick1 /radius/4*Math.PI)*Math.sin(tick2 /radius/4*Math.PI)*radius;
            Location tlocation=location.add(rx,ry,rz);
            if(isbuild){
                domain.blocks[(isorigin)?1:0][block_count]=tlocation.getBlock();
                domain.materials[(isorigin)?1:0][block_count]=tlocation.getBlock().getType();
                tlocation.getBlock().setType(Material.BARRIER);
                block_count++;
                tick1++;
                if(tick1==radius*8){
                    tick1=0;
                    tick2++;
                    if(tick2==radius*8){
                        domain.created();
                        block_count--;
                        break;
                    }
                }
            }

        }

    }
}