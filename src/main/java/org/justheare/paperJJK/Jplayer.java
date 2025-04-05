package org.justheare.paperJJK;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

public class Jplayer extends Jobject{
    private Scoreboard board;
    private Objective objective;
    private Score score_cursepoweramount;
    private Score score_cursepowercurrent;
    private Score score_naturaltech;
    private Score score_domain_state;
    private Score score_domain_attacking;
    private Score score_domain_depending;
    private Score score_speed;
    private Score[] score_jj=new Score[3000];

    public Jplayer(Entity entity) {
        super(entity);
        player= (Player) user;

    }
    int air_surface_tick=0;
    public void air_surface(){
        if(player.isFlying()) {
            if (air_surface_tick == 0 && (!player.getGameMode().equals(GameMode.CREATIVE) &&!player.getGameMode().equals(GameMode.SPECTATOR)   )) {
                player.setFlying(false);
                player.setAllowFlight(false);
                air_surface_tick = 5;
                if(curseenergy>0) {
                    if(Math.pow(getremaincurrent() + 1, 0.5) / 5>3){
                        player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(3));
                    }
                    else{
                        player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(Math.pow(getremaincurrent()+1,0.5)/5));
                    }


                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 3, 1);
                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 2, 2, 2, 0.3);
                    curseenergy -= getremaincurrent() / 10 + 2;
                    if (can_air_surface) {
                        player.setAllowFlight(true);
                    }
                }
            }
        }
        if (can_air_surface) {
            if (!player.isOnGround() && player.isSneaking()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 5, 0));
                //player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 2));
            }
        }
    }
    public void scoboard(){
        ScoreboardManager scoreboardManager= Bukkit.getScoreboardManager();
        board = scoreboardManager.getNewScoreboard();
        objective = board.registerNewObjective("JJK","3");

        objective.setDisplayName(ChatColor.WHITE+"JJK3_"+player.getName());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for(int r=0; r<jujuts.size(); r++){
            if(jujuts.get(r).show){
                score_jj[r]=objective.getScore(jujuts.get(r).toname());
                score_jj[r].setScore(r);
            }
        }
        score_cursepoweramount=objective.getScore("cursed energy:"+curseenergy);
        score_cursepoweramount.setScore(19);

        objective.getScore("cursed energy current:"+cursecurrent).setScore(18);

        score_naturaltech=objective.getScore("natural technic:"+naturaltech);
        score_naturaltech.setScore(20);
        if(this.innate_domain!=null){
            if(this.innate_domain.isexpanded){
                if(this.innate_domain.no_border_on){
                    score_domain_state =objective.getScore("domain range : "+this.innate_domain.nb_range+" border : false");
                    score_domain_state.setScore(17);
                }
                else {
                    score_domain_state =objective.getScore("domain range : "+this.innate_domain.expanded_domain.range+" border : true");
                    score_domain_state.setScore(17);
                }

                if(this.innate_domain.attacker!=null){
                    score_domain_depending = objective.getScore("depend : "+this.innate_domain.attacker.player.getName());
                    score_domain_depending.setScore(16);
                }

            }
            if(this.innate_domain.attack_target!=null){
                score_domain_attacking = objective.getScore("attack target : "+this.innate_domain.attack_target.owner.player.getName());
                score_domain_attacking.setScore(15);
            }
            //score_speed = objective.getScore("speed : "+this.player.getVelocity().length());
            //score_speed.setScore(14);
        }





        player.setScoreboard(board);
    }
}
