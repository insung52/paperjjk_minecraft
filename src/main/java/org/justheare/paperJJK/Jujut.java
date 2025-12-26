package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class Jujut implements Runnable{
    public Entity m_target;
    int delay=0;
    boolean show=true;
    public int tasknum;
    String spe_name;
    public Jobject jobject;
    int power;
    int max_power=20000;
    double use_power=1;
    int in_power=1;
    public int time;
    char target;
    int efficiency=50;
    public Location location;
    Location s_location;
    Location t_location;
    public boolean fixed=false;
    public boolean fixable=false;
    public double distance=1;
    double speed=0;
    boolean reversecurse;
    public boolean charging=true;
    public boolean rechargeable=false;  // Can be recharged after initial cast
    public boolean active=false;         // Skill has been cast (not charging anymore)
    public String skillId="";            // Skill identifier (e.g., "infinity_ao")
    Vector direction=new Vector(0,0,0);
    Entity user;
    List<Entity> j_entities;
    List<Integer> j_entities_num;
    public Jujut(Jobject jobject, String spe_name, String type,boolean rct, int power, int time, char target){
        //
        this.spe_name=spe_name;
        this.jobject=jobject;
        this.reversecurse=rct;
        this.power=power;
        this.time=time;
        this.target=target;
        user=jobject.user;
        if(user instanceof Player player){
            location = player.getEyeLocation();
        }
        else {
            location = user.getLocation();
        }
    }
    public void charged(){

    }
    public boolean scroll(int count){
        return false;
    }
    public void setcurrent(int efficiency,int max_power){
        if(max_power<power) power=max_power;
        this.efficiency=efficiency;
        this.max_power=max_power;
        jobject.cursecurrent+=power*efficiency;
    }
    public void disabled(){

    }
    public void disable(){
        jobject.cursecurrent-=power*efficiency;
        time=0;
        Bukkit.getScheduler().cancelTask(tasknum);
    }
    public void disables(){
        jobject.cursecurrent-=power*efficiency;
        jobject.jujuts.remove(this);
        Bukkit.getScheduler().cancelTask(tasknum);
        disabled();
    }
    public float breakblock(Location break_location,int breakpower){
        if(!PaperJJK.rule_breakblock){
            return 1000;
        }
        Block break_block=break_location.getBlock();
        float rr=break_block.getType().getHardness();
        if(break_block.getType().getHardness()>-1) {
            if (rr < breakpower || break_block.isLiquid()) {
                break_block.setType(Material.AIR);
                //return 1;
            }
            return rr;
        }
        else {
            return 1000;
        }
    }
    @Override
    public void run() {
    }
    public void maintick(){
        jobject.curseenergy-= (int) (use_power*efficiency);
        if(charging){
            // Charging continues - power accumulates
            if(use_power<power){
                use_power++;
            }
        }
        else if(delay<=0){
            time--;
        }
        if(delay>0){
            delay--;
        }
        if(time<=0||use_power<1){
            disables();
        }
    }

    /**
     * Start recharging an already active skill
     * @return true if recharge started successfully
     */
    boolean recharging=false;
    public boolean startRecharge() {
        if (!rechargeable || !active || charging || recharging) {
            return false;
        }
        recharging = true;
        charging = true;
        return true;
    }

    /**
     * Stop charging/recharging and activate the skill
     */
    public void stopCharging() {
        if (charging) {
            recharging = false;
            charging = false;
            if (!active) {
                // First time activation
                active = true;
                charged();
            }
            // If already active, this was a recharge - power already accumulated
        }
    }

    /**
     * Check if this skill can be recharged
     */
    public boolean canRecharge() {
        return rechargeable && active && !charging;
    }
    public String toname(){
        return "";
    }
    public List<Entity> getEntitys(double radius, char target){
        if(target=='a'){
            List<Entity> entities= (List<Entity>) location.getNearbyEntities(radius,radius,radius);
            for(int r=0; r<entities.size(); r++){
                if(entities.get(r).getLocation().distance(location)>radius){
                    entities.remove(r);
                    r--;
                }
            }
            return entities;
        }
        return null;
    }
    public Vector d_location(Location start, Location destination){
        return new Vector(destination.getX()-start.getX(),destination.getY()-start.getY(),destination.getZ()-start.getZ());
    }
}
