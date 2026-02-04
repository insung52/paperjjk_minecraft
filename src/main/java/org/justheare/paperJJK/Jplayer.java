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

    // Skill slot configuration (X, C, V, B keys)
    public String slot1Skill = "";       // X key
    public String slot2Skill = "";       // C key
    public String slot3Skill = "";       // V key
    public String slot4Skill = "";       // B key

    // Domain expansion configuration
    public int normalDomainRange = 30;      // 일반 영역전개 범위 (5~50)
    public int noBarrierDomainRange = 50;   // 무변부여 영역전개 범위 (5~200)

    public Jplayer(Entity entity) {
        super(entity);
        player= (Player) user;

        // Slots will be initialized after naturaltech is loaded from JData
    }

    /**
     * Initialize slot skills based on natural technique
     */
    public void initializeSlots() {
        // Skip if already configured
        if (!slot1Skill.isEmpty()) return;

        switch (naturaltech.toLowerCase()) {
            case "infinity":
                slot1Skill = "infinity_ao";
                slot2Skill = "infinity_aka";
                slot3Skill = "infinity_passive";
                slot4Skill = "infinity_ao";
                break;
            case "mizushi":
                slot1Skill = "mizushi_kai";
                slot2Skill = "mizushi_hachi";
                slot3Skill = "mizushi_fuga";
                slot4Skill = "mizushi_kai";
                break;
            case "physical_gifted":
                slot1Skill = "dash";
                slot2Skill = "reflex";
                slot3Skill = "dash";
                slot4Skill = "dash";
                break;
            default:
                // Default to infinity if unknown
                slot1Skill = "";
                slot2Skill = "";
                slot3Skill = "";
                slot4Skill = "";
                break;
        }

        PaperJJK.log("[Jplayer] Initialized slots for " + naturaltech +
                     ": " + slot1Skill + ", " + slot2Skill + ", " + slot3Skill + ", " + slot4Skill);
    }

    /**
     * Get the skill ID configured for a specific slot
     * @param slot Slot number (1-4)
     * @return Skill ID or null if invalid slot
     */
    public String getSlotSkill(byte slot) {
        return switch (slot) {
            case 1 -> slot1Skill;
            case 2 -> slot2Skill;
            case 3 -> slot3Skill;
            case 4 -> slot4Skill;
            default -> null;
        };
    }

    /**
     * Set the skill ID for a specific slot
     * @param slot Slot number (1-4)
     * @param skillId Skill ID to set
     * @return true if successful
     */
    public boolean setSlotSkill(byte slot, String skillId) {
        if (!JujutFactory.isValidSkillId(skillId)) {
            return false;
        }

        switch (slot) {
            case 1 -> slot1Skill = skillId;
            case 2 -> slot2Skill = skillId;
            case 3 -> slot3Skill = skillId;
            case 4 -> slot4Skill = skillId;
            default -> { return false; }
        }
        return true;
    }
    int air_surface_tick=0;
    public void air_surface(){
        if(player.isFlying()) {
            if (air_surface_tick == 0 && (!player.getGameMode().equals(GameMode.CREATIVE) &&!player.getGameMode().equals(GameMode.SPECTATOR)   )) {
                player.setFlying(false);
                player.setAllowFlight(false);
                air_surface_tick = 5;
                if(curseenergy>0) {
                    PaperJJK.potionpower(player,PotionEffectType.SPEED,curseenergy/5000.0, (black_flash_tick>0?2:0),true);
                    PaperJJK.potionpower(player,PotionEffectType.JUMP_BOOST,curseenergy/5000.0, (black_flash_tick>0?1:0), true);
                    PaperJJK.potionpower(player,PotionEffectType.STRENGTH,curseenergy, (black_flash_tick>0?1:0),true);
                    if(Math.pow(getremaincurrent() + 1, 0.5) / 5>3){
                        player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(3));
                    }
                    else{
                        player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(Math.pow(getremaincurrent()+1,0.5)/5));
                    }
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 3F, 1.5F);
                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 2, 2, 2, 0.3);
                    curseenergy -= getremaincurrent() / 10 + 2;
                    if (can_air_surface) {
                        player.setAllowFlight(true);
                    }
                }
                if(naturaltech.equals("physical_gifted")){
                    PaperJJK.potionpower(player,PotionEffectType.SPEED,40000,0,true);
                    PaperJJK.potionpower(player,PotionEffectType.STRENGTH,100000000,0,true);
                    PaperJJK.potionpower(player,PotionEffectType.JUMP_BOOST,40000, 0,true);
                    player.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(4));
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 3F, 1.5F);
                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 2, 2, 2, 0.3);
                    if (can_air_surface) {
                        player.setAllowFlight(true);
                    }
                }
            }
        }
        if (can_air_surface) {
            if (!player.isOnGround() && player.isSneaking() && (curseenergy>100||naturaltech.equals("physical_gifted"))) {
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
