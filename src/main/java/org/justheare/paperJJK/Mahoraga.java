package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Objects;

public class Mahoraga extends Jujut{
    ArrayList<String> adapt_list =new ArrayList<String>();
    ArrayList<Double> adapt_list_power =new ArrayList<Double>();
    ArrayList<Integer> adapt_list_num =new ArrayList<Integer>();
    //ArrayList<String> adapt_wait_list = new ArrayList<String>();
    int jump_time = 0;
    int adapt_anim = 0;
    BlockDisplay wheel_center;

    ArrayList<BlockDisplay> wheel_line = new ArrayList<BlockDisplay>();
    ArrayList<BlockDisplay> wheel_circle = new ArrayList<BlockDisplay>();
    ArrayList<BlockDisplay> wheel_out = new ArrayList<BlockDisplay>();

    public Mahoraga(Jobject jobject, String spe_name, String type, boolean rct, int power, int time, char target) {
        super(jobject, spe_name, type, rct, power, time, target);
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        // 명령어 문자열 생성
        //String command = "execute as " + user.getUniqueId() + " at @s run attribute @s minecraft:scale base set 2.1";
        //Bukkit.dispatchCommand(console, command);
        LivingEntity entity = (LivingEntity) user;
        AttributeInstance scaleAttr = entity.getAttribute(Attribute.SCALE);
        AttributeInstance frAttr = entity.getAttribute(Attribute.FOLLOW_RANGE);
        AttributeInstance shAttr = entity.getAttribute(Attribute.STEP_HEIGHT);
        if (scaleAttr != null) {
            scaleAttr.setBaseValue(2.1);
        }
        if (frAttr != null) {
            frAttr.setBaseValue(100);
        }
        if (shAttr != null) {
            shAttr.setBaseValue(5);
        }
        float v1=user.getYaw();
        float v2=user.getPitch();
        user.setRotation(0,0);
        BlockDisplay wheel = user.getWorld().spawn(user.getLocation(), BlockDisplay.class, (display) -> {
            display.setTransformation(new Transformation(display.getTransformation().getTranslation().add(-0.25F,1-0.2F,-0.25F), new AxisAngle4f(0,0,0,0), new Vector3f(0.5F,0.5F,0.5F), new AxisAngle4f(0,0,0,0)));
            display.setBlock(Material.GOLD_BLOCK.createBlockData());
            display.setRotation(0,0);
        });
        wheel_center = wheel;
        wheel.setPersistent(true);
        user.addPassenger(wheel);

        for(int r=0; r<8; r++){
            int finalR = r;
            float angle = (float) (finalR * Math.PI / 4);
            wheel = user.getWorld().spawn(user.getLocation(), BlockDisplay.class, (display) -> {
                display.setTransformation(new Transformation(display.getTransformation().getTranslation().add(0,1,0), new AxisAngle4f(angle,0,1,0), new Vector3f(2F,0.1F,0.1F), new AxisAngle4f(0,0,0,0)));
                display.setBlock(Material.GOLD_BLOCK.createBlockData());
            });
            wheel.setPersistent(true);
            user.addPassenger(wheel);
            wheel_line.add(wheel);

            wheel = user.getWorld().spawn(user.getLocation(), BlockDisplay.class, (display) -> {
                float x = (float) ((2) * Math.cos(angle));
                float z = (float) ((2) * Math.sin(angle));
                display.setTransformation(new Transformation(display.getTransformation().getTranslation().add(x-0.25F,1-0.2f,z-0.25F), new AxisAngle4f(0,0,1,0), new Vector3f(0.5F,0.5F,0.5F), new AxisAngle4f(0,0,0,0)));
                display.setBlock(Material.GOLD_BLOCK.createBlockData());
            });
            wheel.setPersistent(true);
            user.addPassenger(wheel);
            wheel_out.add(wheel);
        }
        int numBlocks = 100;  // 배치할 블록의 개수
        float radius = 1.5f;    // 원의 반지름
        for (int i = 0; i < numBlocks; i++) {
            float angle = (float) (2 * Math.PI * i / numBlocks);
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            wheel = user.getWorld().spawn(user.getLocation(), BlockDisplay.class, (display) -> {
                display.setTransformation(new Transformation(
                        display.getTransformation().getTranslation().add(x, 1, z),
                        new AxisAngle4f(0, 0, 1, 0),
                        new Vector3f(0.1F, 0.1F, 0.1F),
                        new AxisAngle4f(0, 0, 0, 0)
                ));
                display.setBlock(Material.GOLD_BLOCK.createBlockData());
            });
            wheel.setPersistent(true);
            user.addPassenger(wheel);
            wheel_circle.add(wheel);
        }
        user.setRotation(v1,v2);

        this.time=0;
    }
    public double pre_adapt (String target,String type,double power){
        boolean check = false;

        for(int r=0; r<adapt_list.size(); r++){
            if(adapt_list.get(r).equalsIgnoreCase(target)){
                check=true;
                if(Objects.equals(type, "damage")){
                    if(target.equals("ENTITY_ATTACK")||target.equals("ENTITY_SWEEP_ATTACK")||target.equals("PROJECTILE")){
                        adapt_list_num.set(r,adapt_list_num.get(r)+80);
                        power-=power*adapt_list_power.get(r)/20.0;

                    }
                    else if(target.equals("LIGHTNING")||target.equals("LAVA")||target.equals("FIRE")||target.equals("FIRE_TICK")||target.equals("CONTACT")||target.equals("SUFFOCATION")||target.equals("HOT_FLOOR")){
                        adapt_list_num.set(r,adapt_list_num.get(r)+40);
                        power-=power*adapt_list_power.get(r)/7.0;
                        //PaperJJK.log(String.valueOf(adapt_list_num.get(r)));

                    }
                    else if(target.equals("ao")){
                        power-=power*adapt_list_power.get(r)/7.0;
                    }
                    else if(target.equals("aka")){
                        adapt_list_num.set(r,adapt_list_num.get(r)+50);
                        power-=power*adapt_list_power.get(r)/7.0;
                    }
                    else if(target.equals("infinity_passive")){
                        power-=power*adapt_list_power.get(r)/7.0;
                    }
                    else {
                        //PaperJJK.log(String.valueOf(adapt_list_num.get(r)));
                        adapt_list_num.set(r,adapt_list_num.get(r)+80);
                        power-=power*adapt_list_power.get(r)/7.0;
                    }
                }
                else if(type=="curse"){
                    if(target=="ao"){
                        adapt_list_num.set(r,adapt_list_num.get(r)+10);
                        power-=power*adapt_list_power.get(r)/7.0;
                    }
                    else if(target.equals("aka")){
                        power-=power*adapt_list_power.get(r)/7.0;
                    }
                    else if(target.equals("infinity_passive")){
                        adapt_list_num.set(r,adapt_list_num.get(r)+5);
                        power-=power*adapt_list_power.get(r)/7.0;
                    }
                    else if(target.equals("infinity_domain")){
                        adapt_list_num.set(r,adapt_list_num.get(r)+10);
                        power-=power*adapt_list_power.get(r)/7.0;
                    }

                }

                return power;
            }
        }
        if(!check){
            adapt_list_num.add(0);
            adapt_list.add(target);
            adapt_list_power.add(0.0);
        }
        return power;
    }
    public void adapt(String target, Double power){
        jobject.curseenergy=jobject.max_curseenergy;
        adapt_anim=11;
        for(int r =0; r<adapt_list.size(); r++){
            adapt_list_num.set(r, adapt_list_num.get(r)/2);
        }
        user.getWorld().playSound(user.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN,10F,0.8F);
        user.getWorld().playSound(user.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER,10F,0.8F);
        user.getWorld().playSound(user.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_DETECT_PLAYER,10F,0.7F);
        user.getWorld().playSound(user.getLocation(), Sound.BLOCK_PISTON_CONTRACT,10F,0.5F);
        PaperJJK.log(target);
        jobject.infinity_stun_tick=0;
        if(user instanceof LivingEntity living){
            PaperJJK.restoreMobAI(living);
            if(target.equals("FIRE")||target.equals("FIRE_TICK")||target.equals("LAVA")||target.equals("HOT_FLOOR")){
                if(power>5){
                    living.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,999999,1,false));
                }
            }
            if(target.equals("ENTITY_ATTACK")||target.equals("ENTITY_SWEEP_ATTACK")||target.equals("PROJECTILE")){

                LivingEntity entity = (LivingEntity) user;
                AttributeInstance scaleAttr = entity.getAttribute(Attribute.SCALE);
                assert scaleAttr != null;
                if(scaleAttr.getBaseValue()<10){
                    scaleAttr.setBaseValue(scaleAttr.getBaseValue() * 1.02);
                    AttributeInstance attackAttr = entity.getAttribute(Attribute.ATTACK_DAMAGE);
                    if (attackAttr != null) {
                        attackAttr.setBaseValue(attackAttr.getBaseValue()+0.02);
                    }
                    AttributeInstance mhAttr = entity.getAttribute(Attribute.MAX_HEALTH);
                    if (mhAttr != null) {
                        mhAttr.setBaseValue(mhAttr.getBaseValue()+2);
                    }
                }

            }
            living.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH,5,100,false));
            living.setFireTicks(0);

            //living.setVisualFire(false);
            living.setFreezeTicks(0);
            living.removePotionEffect(PotionEffectType.POISON);
            living.removePotionEffect(PotionEffectType.WITHER);
            living.removePotionEffect(PotionEffectType.SLOWNESS);
            living.removePotionEffect(PotionEffectType.LEVITATION);
            living.removePotionEffect(PotionEffectType.WEAKNESS);
        }

    }
    @Override
    public void run(){
        if(user.isDead()){
            for(BlockDisplay wheel : wheel_line){
                wheel.remove();
            }
            for(BlockDisplay wheel : wheel_out){
                wheel.remove();
            }
            for(BlockDisplay wheel : wheel_circle){
                wheel.remove();
            }
            wheel_center.remove();
        }
        if(adapt_anim>0){
            adapt_anim--;
            double animt=10-adapt_anim;

            for(Entity wheel:wheel_line){
                //wheel.setRotation(wheel.getYaw()+4.5f,0);
            }
            for(int r=0; r<wheel_out.size(); r++){
                float angle = (float) ((r+animt/10) * Math.PI / 4);
                float x = (float) ((2) * Math.cos(angle));
                float z = (float) ((2) * Math.sin(angle));
                wheel_out.get(r).setTransformation(new Transformation(wheel_center.getTransformation().getTranslation().add(x,0,z), new AxisAngle4f(0,0,1,0), new Vector3f(0.5F,0.5F,0.5F), new AxisAngle4f(0,0,0,0)));
                wheel_line.get(r).setTransformation(new Transformation(wheel_line.get(r).getTransformation().getTranslation(), new AxisAngle4f(-angle,0,1,0), new Vector3f(2F,0.1F,0.1F), new AxisAngle4f(0,0,0,0)));


            }
        }
        if(jobject.infinity_stun_tick==0){
            time++;
        }
        if(time==100){
            if(m_target!=null){
                if(!m_target.isDead()){
                    jump_time=20;
                    LivingEntity entity = (LivingEntity) user;
                    AttributeInstance scaleAttr = entity.getAttribute(Attribute.SCALE);
                    assert scaleAttr != null;
                    user.getLocation().add(0,entity.getEyeHeight(),0).createExplosion(user, (float) (2+scaleAttr.getValue()*0.5),false,PaperJJK.rule_breakblock);
                }
            }
            time=0;
        }
        if(jump_time>0&&jobject.infinity_stun_tick==0){
            jump_time--;
            if(Math.random()<0.25){
                LivingEntity entity = (LivingEntity) user;
                AttributeInstance scaleAttr = entity.getAttribute(Attribute.SCALE);
                assert scaleAttr != null;
                user.getLocation().add(0,3,0).createExplosion(user, (float) (2+scaleAttr.getValue()*0.5),false,PaperJJK.rule_breakblock);
                user.setVelocity(m_target.getLocation().add(((LivingEntity)user).getEyeLocation().multiply(-1)).toVector().normalize().multiply(4));
            }
            /*
            if(m_target.getLocation().distance(user.getLocation())>3){


            }
            else{
                jump_time=0;
                user.getLocation().add(0,1,0).createExplosion(user,4,false);
            }
             */

        }
        if(user instanceof LivingEntity living){
            if(living.getHealth()<=0||living.isDead()){
                disables();
            }
        }
        jobject.curseenergy=jobject.max_curseenergy;
        for(int n =0; n<adapt_list.size(); n++){
            adapt_list_num.set(n,adapt_list_num.get(n)+1);
            if(adapt_list_num.get(n)>=30*100){
                adapt_list_num.set(n,0);
                adapt_list_power.set(n,adapt_list_power.get(n)+1.0);
                adapt(adapt_list.get(n),adapt_list_power.get(n));
            }
        }
    }
}
