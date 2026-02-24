package org.justheare.paperJJK;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhysicalGifted extends Jujut{
    public PhysicalGifted(Jobject jobject, String spe_name, String type, boolean rct, int power, int time, char target) {
        super(jobject, spe_name, type, rct, power, time, target);
        fixable = false;
        this.time = 20;
        efficiency = 0;
        this.use_power=5;
        charging = false;

        setcurrent(0,40);
    }
    // 스킬 1 : 돌진기
    // 스킬 2 : 1초간 다가오는 투사체, 공격 이벤트 등 모든 공격을 튕겨내거나 회피 또는 데미지 감소, 추가로 근접한 적들에게 약한 데미지를 준다. 너무 강한 공격을 방어하면 강제 종료된다. Ish 주구를 들고있을 경우 술식 무효화 효과도 적용한다. //쿨타임 3초.
    // 스킬 3 :
    @Override
    public void run() {
        if(spe_name.equals("dash")) {
            if (time == 20) {
                RayTraceResult rayTraceResult = ((LivingEntity) user).rayTraceBlocks(50);
                if (rayTraceResult != null) {
                    //PaperJJK.log("ray found");
                    Block targetblock = rayTraceResult.getHitBlock();
                    location = null;
                    for (int r = 1; r < 4; r++) {
                        if (targetblock.getRelative(0, r, 0).isPassable() && targetblock.getRelative(0, r + 1, 0).isPassable()) {
                            //targetblock.getRelative(0,r-1,0).setType(Material.GLOWSTONE);
                            location = targetblock.getLocation().add(0.5, r, 0.5);
                            ((LivingEntity) user).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 7, 1));
                            //PaperJJK.log("up found");
                            break;
                        }
                    }
                    if (location == null) {
                        //PaperJJK.log("up not found");
                        Vector offset = getVector(rayTraceResult);
                        if (offset != null) {
                            //PaperJJK.log("offset found");
                            location = targetblock.getLocation().add(0.5, 0, 0.5).add(offset);
                            ((LivingEntity) user).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 7, 1));
                        } else {
                            if (user instanceof Player player) {
                                //PaperJJK.log("offset not found");
                                player.sendActionBar(
                                        Component.text("too far!", NamedTextColor.RED)
                                );
                            }
                            location = null;
                        }
                    }
                } else {
                    //PaperJJK.log("ray not found");
                    if (user instanceof Player player) {
                        player.sendActionBar(
                                Component.text("too far!", NamedTextColor.RED)
                        );
                    }
                    location = null;
                }
            } else if (time == 18) {
                if (location != null) {
                    location.setDirection(user.getLocation().getDirection());
                    user.teleport(location);
                }
            }
        }
        else if(spe_name.equals("reflex")){
            List<Entity> entities= user.getNearbyEntities(2,3,2);
            if(!entities.isEmpty()){

            }
            for(Entity entity : entities){
                if(entity instanceof LivingEntity living){
                    interact = true;
                    living.damage(PaperJJK.physical_attack_damage((LivingEntity) user)/4,user);
                    //((LivingEntity) user).attack(living);
                    user.getWorld().spawnParticle(Particle.SWEEP_ATTACK,living.getLocation().add(0,living.getHeight()/2,0),1,0.1,0.1,0.1,1);
                }
            }
            if(interact){
                user.getWorld().spawnParticle(Particle.SWEEP_ATTACK,location,1,0.1,0.1,0.1,1);
                user.getWorld().playSound(user, Sound.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.1F, (float) Math.min(1.5F+interact_value/10.0,2.0F));
                user.getWorld().playSound(user, Sound.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundCategory.PLAYERS, 0.1F, 2.0F);
                interact = false;
                interact_value = 0;
            }
        }
        maintick();
    }

    @Nullable
    private static Vector getVector(RayTraceResult rayTraceResult) {
        BlockFace face = rayTraceResult.getHitBlockFace();
        assert face != null;
        Vector offset = switch (face) {
            case NORTH -> new Vector(0, 0, -0.8);
            case SOUTH -> new Vector(0, 0, 0.8);
            case WEST -> new Vector(-0.8, 0, 0);
            case EAST -> new Vector(0.8, 0, 0);
            case DOWN -> new Vector(0,-2,0);
            default -> null;
        };
        return offset;
    }

    public String toname(){
        return ChatColor.GRAY+spe_name+ChatColor.WHITE+" *"+use_power+" , "+time/20;
    }
}
