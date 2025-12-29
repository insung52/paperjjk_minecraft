package org.justheare.paperJJK;

import org.bukkit.Location;

public class Jdomain {
    Location location;
    Jobject owner;
    int range=30;
    public int level=0;
    Jdomain_Builder originbuilder;
    boolean isbuilt=false;
    boolean isbuilding=false;
    boolean special;
    int current_radius;
    int tp_delay=0;
    public Jdomain(Jobject owner){
        this.owner = owner;
    }

    void build_finished(){
        isbuilt=true;
        isbuilding=false;
        owner.user.sendMessage("innate domain build finished.");
        owner.user.teleport(location.clone().add(0,range+1,0));
        tp_delay=5;
    }
    void destroy_finished(){
        isbuilding=false;
        owner.user.sendMessage("innate domain destroy finished.");
    }

}
