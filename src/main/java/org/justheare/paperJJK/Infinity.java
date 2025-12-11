package org.justheare.paperJJK;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.justheare.paperJJK.network.JPacketSender;

import java.util.ArrayList;
import java.util.List;

public class Infinity extends Jujut{
    boolean murasaki=false;
    boolean unlimit_m=false;
    int soundtick=0;
    boolean aoEffectActive=false;  // Track if AO packet effect is active
    @Override
    public void disabled() {
        location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 3, 2);

        // Send END packet when AO effect stops
        if(aoEffectActive && user instanceof Player player) {
            JPacketSender.sendInfinityAoEnd(player);
            aoEffectActive = false;
        }
    }
    public Infinity(Jobject jobject, String spe_name, String type, boolean rct, int power, int time, char target) {
        super(jobject, spe_name, type, rct, power, time, target);
        location.add(location.getDirection().clone().multiply(distance));
        j_entities=new ArrayList<Entity>();
        j_entities_num=new ArrayList<Integer>();
        max_power=100;
        fixable=true;
        distance=1;
        this.time = 999999;
        if(rct) {
            speed=1;
            this.time = (int) Math.pow(this.time,0.3);
        }
        setcurrent(1,100);
    }
    @Override
    public void run() {
        if(soundtick%5==0){
            if(charging || recharging){
                location.getWorld().playSound(location, Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, (float) use_power /60, (float) ((float) use_power /100*1.5 + 0.5));
            }
        }

        soundtick++;
        maintick();
        if(!fixed){
            s_location=user.getLocation().clone().add(0,1.5,0);
            t_location=user.getLocation().clone().add(0,1.5,0).add(user.getLocation().getDirection().clone().normalize().multiply(distance));
        }
        if(charging && !recharging){
            if(user instanceof Player player){
                location=player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(distance));
            }
            else{
                location=user.getLocation().add(user.getLocation().getDirection().multiply(4));
            }
            Particle.DustOptions dust=new Particle.DustOptions(Color.BLUE, 0.2F);
            if(reversecurse){
                dust=new Particle.DustOptions(Color.RED, 0.2F);
                //location.getWorld().spawnParticle(Particle.PORTAL,location,(int) Math.pow(use_power,0.8),Math.log(use_power)/5, Math.log(use_power)/10, Math.log(use_power)/10,0.1);
                //location.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,location,(int) Math.pow(use_power,0.8),Math.log(use_power)/5, Math.log(use_power)/10, Math.log(use_power)/10,1,dust);
                location.getWorld().spawnParticle(Particle.DUST, location, (int) Math.pow(use_power,0.8), Math.log(use_power)/5, Math.log(use_power)/10, Math.log(use_power)/10, 0, dust, true);
                dust=new Particle.DustOptions(Color.RED, (float) ((Math.pow(use_power,0.5))/10));
                location.getWorld().spawnParticle(Particle.DUST, location, 1, 0.1, 0.1, 0.1, 0.5, dust, true);

                //location.getWorld().spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0.5, dust, true);
            }
            else {
                location.getWorld().spawnParticle(Particle.DUST, location, (int) Math.pow(use_power,0.8), Math.log(use_power)/5, Math.log(use_power)/10, Math.log(use_power)/10, 0, dust, true);
                dust=new Particle.DustOptions(Color.BLUE, (float) ((Math.pow(use_power,0.5))/10));
                location.getWorld().spawnParticle(Particle.DUST, location, 1, 0.1, 0.1, 0.1, 0.5, dust, true);
            }
        }
        else {
            Particle.DustOptions dust=new Particle.DustOptions(Color.BLUE, 0.2F);
            if(distance>500){
                //disables();
            }
            if(reversecurse){
                if(!murasaki){
                    dust=new Particle.DustOptions(Color.RED, (float) ( (Math.pow(use_power,0.9))/17+0.5));
                    location.getWorld().spawnParticle(Particle.DUST, location, (int) Math.pow(use_power,0.5)+5, Math.log(use_power)/3, Math.log(use_power)/3, Math.log(use_power)/3, 0, dust, true);
                    //dust=new Particle.DustOptions(Color.RED, (float) (Math.pow(use_power,0.7))/5);
                    //location.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0.5, dust, true);
                    if(time%5==0){
                        location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 3, 1);
                    }
                }
                aka();
            }
            else {
                //location.getWorld().spawnParticle(Particle.DUST, location, (int) Math.pow(use_power,0.8), Math.log(use_power)/5, Math.log(use_power)/10, Math.log(use_power)/10, 0, dust, true);
                dust=new Particle.DustOptions(Color.BLUE, (float) ((Math.pow(use_power,0.5))/7));
                location.getWorld().spawnParticle(Particle.DUST, location, 1, 0.02, 0.02, 0.02, 0.5, dust, true);
                if(soundtick%5==0){
                    soundtick=0;
                    location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, (float) use_power /10,0.8F);
                }
                ao();

            }
        }
    }
    public void murasaki(){
        List<Jujut> jujuts=PaperJJK.getjujuts();
        for(Jujut jujut:jujuts){
            if(!jujut.charging&&jujut instanceof Infinity&&jujut.user.equals(user)){
                if(!jujut.reversecurse){
                    if(jujut.location.distance(location)<10&&jujut.use_power>=10&&use_power>=10){
                        use_power+=jujut.use_power;
                        murasaki=true;
                        boolean ind=false;
                        for(Jujut jujut1:jujuts){
                            if(jujut1 instanceof Infinity_passive&&jujut1.user.equals(user)){
                                ind=true;
                            }
                        }
                        if(jujut.location.distance(user.getLocation())<10&&!ind){
                            unlimit_m = true;
                            user.getWorld().playSound(location, Sound.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 7F, 1.7F);
                            user.getWorld().playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.PLAYERS, 7F, 1.4F);
                            time = (int) (10 + use_power / 10);
                            List<Entity> targets = (List<Entity>) location.getNearbyEntities(1 + time * 3, 1 + time * 3, 1 + time * 3);
                            for (int r = 0; r < targets.size(); r++) {
                                Entity tentity = targets.get(r);
                                if (tentity.equals(user)) {
                                    jobject.damaget((LivingEntity) user, 'j', Math.pow(use_power, 0.7), false,"murasaki",false);
                                    continue;
                                }
                                Vector d_vector = d_location(tentity.getLocation(), location);
                                if (d_vector.length() <= time * 3 + tentity.getHeight() + tentity.getWidth()) {
                                    //tentity.setVelocity(tentity.getVelocity().add(location.getDirection().clone().multiply(7)));
                                    if (tentity instanceof LivingEntity living) {
                                        jobject.damaget(living, 'j', Math.pow(use_power, 1) * 10 - Math.pow(d_vector.length(), 0.8), false,"murasaki",false);
                                    }
                                }
                            }

                        }
                        else{
                            location.add(location.getDirection().clone().multiply(-1*jujut.use_power/5));
                            user.getWorld().playSound(location,Sound.ITEM_TRIDENT_THUNDER,SoundCategory.PLAYERS, 7F, 1.7F);
                            user.getWorld().playSound(location,Sound.ITEM_TRIDENT_RETURN,SoundCategory.PLAYERS, 7F, 1.6F);
                            time= (int) (10+use_power/2);
                        }
                        jujut.disables();
                        j_entities=new ArrayList<Entity>();
                    }
                }
            }
        }
    }
    public void murasaki_explode(){
        int tick= (int) ((10+use_power/10-time-1)*3);
        for(int r=tick; r<tick+3; r++){
            //PaperJJK.log(r+"");
            double step = Math.PI / Math.pow(r,0.75) / 8;
            for (double theta = 0; theta < Math.PI; theta += step) {
                double phistep = step / Math.sin(theta == 0 || theta == Math.PI ? step : theta)*0.8;
                for (double phi = 0; phi < 2 * Math.PI; phi += phistep) {
                    double x = r*0.8 * Math.sin(theta) * Math.cos(phi);
                    double y = r*0.8 * Math.sin(theta) * Math.sin(phi);
                    double z = r*0.8 * Math.cos(theta);
                    Location tlocation = location.clone().add(new Vector(x, y, z));
                    breakblock(tlocation, (int) (use_power-tick/20));
                    if(Math.random()<0.01){
                        Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 3F);
                        location.getWorld().spawnParticle(Particle.DUST, tlocation, 1, 1, 1, 1, 0.5, dust, true);
                    }
                }
            }
        }
    }
    public void aka(){
        if(target=='a'){
            if(murasaki){
                if(unlimit_m){
                    murasaki_explode();
                }
                else{
                    if(time%2==0){
                        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, (float) (5+use_power*0.1), 0.5F);
                    }
                    for(int r=0; r<10; r++) {
                        if(r%5==0){
                            //location.createExplosion((float) (Math.pow(use_power,0.2)+1));
                        }
                        Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 5F);
                        location.getWorld().spawnParticle(Particle.DUST, location, (int) use_power, Math.pow(use_power, 0.5), Math.pow(use_power, 0.5), Math.pow(use_power, 0.5), 0.5, dust, true);
                        // Use distance as direction multiplier (1 for forward, -1 for backward)
                        double directionMultiplier = Math.abs(distance) < 0.1 ? 1 : Math.signum(distance);
                        location.add(location.getDirection().clone().multiply(0.8 * directionMultiplier));
                        //location.createExplosion(5);
                        double yaw = location.getYaw();
                        double pitch = location.getPitch();
                        double step = Math.PI / (Math.pow(use_power,0.5) * 3.6);
                        for (double theta = 0; theta < Math.PI / 2; theta += step) {
                            double phistep = step / Math.sin(theta == 0 || theta == Math.PI ? step : theta)*0.8;
                            for (double phi = 0; phi < 2 * Math.PI; phi += phistep) {
                                double x = Math.pow(use_power,0.5) * Math.sin(theta) * Math.cos(phi);
                                double y = Math.pow(use_power,0.5) * Math.sin(theta) * Math.sin(phi);
                                double z = Math.pow(use_power,0.5) * Math.cos(theta);

                                //b_location.getWorld().spawnParticle(Particle.REDSTONE, b_location.clone().add(new Vector(x,y,z)), 1, 0.1, 0.1, 0.1, 0.5, dust, true);
                                Location tlocation = location.clone().add(new Vector(x, y, z).rotateAroundY(-Math.toRadians(yaw)).rotateAroundX(Math.toRadians(pitch) * Math.cos(Math.toRadians(yaw))).rotateAroundZ(Math.toRadians(pitch) * Math.sin(Math.toRadians(yaw))));
                                //location.getWorld().spawnParticle(Particle.REDSTONE, tlocation, 1, 0.1, 0.1, 0.1, 0.5, dust, true);
                                breakblock(tlocation, (int) use_power);
                            }
                        }
                    }
                    List<Entity> targets = (List<Entity>) location.getNearbyEntities(1 + Math.pow(use_power, 0.5), 1 + Math.pow(use_power, 0.5), 1 + Math.pow(use_power, 0.5));
                    for (int r = 0; r < targets.size(); r++) {
                        Entity tentity = targets.get(r);
                        if (tentity.equals(user)) {
                            continue;
                        }

                        if (!j_entities.contains(tentity)) {
                            Vector d_vector = d_location(tentity.getLocation(), location);
                            if (d_vector.length() <= Math.pow(use_power, 0.5) + tentity.getHeight() + tentity.getWidth()) {
                                j_entities.add(tentity);
                                //tentity.setVelocity(tentity.getVelocity().add(location.getDirection().clone().multiply(7)));
                                if (tentity instanceof LivingEntity living) {
                                    jobject.damaget(living, 'j', Math.pow(use_power, 1.3)*1, false,"murasaki",false);
                                }
                            }
                        }
                    }
                }

            }
            else {
                //distance+=speed*10;
                //PaperJJK.log(location.getDirection().clone().multiply(speed).length()+" "+distance+" "+speed);
                for (int r = 0; r < 5; r++) {
                    //location.add(d_location(location, t_location).normalize());
                    location.setDirection(location.getDirection().clone().multiply(0.9).add(d_location(location, t_location).normalize().multiply(0.02)));
                    //direction=direction.clone().multiply(1.0).add((d_location(location, t_location).normalize()));
                    location.add(location.getDirection().clone());
                    if (use_power >= 1) {
                        if (!(location.getBlock().isEmpty())) {
                            if(location.getBlock().isLiquid()){
                                use_power-=1;
                            }
                            else {
                                use_power -= Math.log(location.getBlock().getType().getHardness()+5)*5+7;
                            }
                            location.createExplosion(user,(float) ((float) (Math.log(location.getBlock().getType().getHardness()+3)*4)*((use_power+50)/150)*1.5),false,PaperJJK.rule_breakblock);
                        }
                    }
                }
                murasaki();
                if (use_power < 0) {
                    use_power = 0;
                }
                for (int r = 0; r < j_entities.size(); r++) {
                    j_entities_num.set(r,j_entities_num.get(r)-1);
                    double push_power=PaperJJK.get_calculated_power(j_entities.get(r),"aka",1);
                    if(push_power>0&&Math.random()<push_power){
                        j_entities.get(r).setVelocity(j_entities.get(r).getVelocity().add(location.getDirection().clone().multiply(new Vector(1, 0.2, 1)).multiply(0.01+0.1*use_power/100).multiply(push_power)));
                    }
                    if (j_entities.get(r) instanceof LivingEntity living) {
                        if (time % 4 == 0) {
                            jobject.damaget(living, 'j', 2 + Math.pow(use_power, 0.5), false,"aka",false);
                        }
                        if (Math.random() > 0.9) {
                            living.getEyeLocation().createExplosion(user,(float) Math.log10(use_power),false,PaperJJK.rule_breakblock);
                        }
                    } else {
                        if (Math.random() > 0.9) {
                            j_entities.get(r).getLocation().createExplosion(user,(float) Math.log10(use_power / 3),false,PaperJJK.rule_breakblock);
                            //j_entities.remove(r);
                        }
                    }
                    Jobject tj = PaperJJK.getjobject(j_entities.get(r));
                    if(tj!=null){
                        if(tj.ish_depence){
                            use_power--;
                        }
                    }
                    if(j_entities_num.get(r)<0){
                        j_entities.remove(r);
                        j_entities_num.remove(r);
                    }
                }
                List<Entity> targets = (List<Entity>) location.getNearbyEntities(5 + Math.pow(use_power, 0.3), 5 + Math.pow(use_power, 0.3), 5 + Math.pow(use_power, 0.3));
                for (int r = 0; r < targets.size(); r++) {
                    Entity tentity = targets.get(r);
                    if (tentity.equals(user)) {
                        continue;
                    }
                    if (!j_entities.contains(tentity) && j_entities.size() < 50) {
                        Vector d_vector = d_location(tentity.getLocation(), location);
                        if (d_vector.length() <= 2 + Math.pow(use_power, 0.3) + tentity.getHeight() + tentity.getWidth()) {
                            j_entities.add(tentity);
                            j_entities_num.add(10);
                            double push_power=PaperJJK.get_calculated_power(tentity,"aka",1);
                            if(push_power>0&&Math.random()<push_power) {
                                tentity.setVelocity(tentity.getVelocity().add(location.getDirection().clone().multiply(7*push_power)));
                            }
                            if (tentity instanceof LivingEntity living) {
                                jobject.damaget(living, 'j', 10 + Math.pow(use_power, 1), false,"aka",false);
                            }
                        }
                    }
                }
            }
        }
    }
    public void ao(){
        if(target=='a'){
            // Send START packet when AO begins (only once)
            if(!aoEffectActive && user instanceof Player player) {
                // Scale usepower (1-100) to strength (0.1-5.0)
                float strength = (float) (1 * 0.049 + 0.051);  // Linear scale: 1->0.1, 100->5.0
                JPacketSender.sendInfinityAoStart(player, location, strength);
                aoEffectActive = true;
            }

            // Send SYNC packet every 10 ticks (0.5 seconds)
            if(time%5==0){
                use_power--;

                // Send position/strength update to client
                if(user instanceof Player player) {
                    float strength = (float) (use_power * 0.049 + 0.051);  // Scale 1-100 to 0.1-5.0
                    JPacketSender.sendInfinityAoSync(player, location, strength);
                }
            }

            location.add(d_location(location,t_location).normalize().multiply(0.5));
            List<Entity> targets= (List<Entity>) location.getNearbyEntities(5+Math.pow(use_power,0.7),5+Math.pow(use_power,0.7),5+Math.pow(use_power,0.7));
            for(int r=0; r<targets.size(); r++){
                Entity tentity=targets.get(r);
                if(tentity.equals(user)){
                    continue;
                }
                Jobject tj = PaperJJK.getjobject(targets.get(r));
                if(tj!=null){
                    if(tj.ish_depence&&location.distance(targets.get(r).getLocation())<Math.pow(use_power,0.6)+1){
                        //location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 2F, 0.7F);
                        ((Player) user).setCooldown(Material.WRITTEN_BOOK,20);
                        disables();
                    }
                }
                Vector d_vector=d_location(tentity.getLocation(),location);
                if(d_vector.length()<=5+Math.pow(use_power,0.7)){
                    double push_power=PaperJJK.get_calculated_power(tentity,"ao",1);
                    if(push_power>0&&Math.random()<push_power) {
                        tentity.setVelocity(
                                tentity.getVelocity().add(
                                        d_vector.clone().normalize().multiply(
                                                Math.pow(use_power, 0.7) / 2 / Math.pow(d_vector.length() + 3, 1.5)
                                        )
                                ).multiply(push_power)
                        );
                    }
                    if(time%4==0) {
                        if (tentity instanceof LivingEntity living){
                            if (d_vector.length() <= 1 + Math.pow(use_power, 0.3)) {
                                jobject.damaget(living, 'j', Math.pow(use_power, 0.3), false,"ao",false);
                            }
                        }
                        else if((tentity instanceof Item||tentity instanceof FallingBlock) &&Math.random()<0.1){
                            tentity.remove();
                        }
                    }

                }
            }
            int rrrr=0;
            for(double r1=0; r1<use_power; r1++){
                double rr1=Math.random()  * Math.PI *2;
                for(double r2=0; r2<use_power; r2++){
                    double rr2=Math.random()  * Math.PI *2;
                    double rx = Math.sin(rr1) * Math.sin(rr2);
                    double ry = Math.cos(rr2);
                    double rz = Math.cos(rr1) * Math.sin(rr2);
                    Vector rv=new Vector(rx,ry,rz).multiply(Math.pow(use_power,0.6)+1);
                    Location a_location=location.clone().add(rv.clone().multiply(1*Math.pow(Math.random(),3)));
                    @NotNull Material a_blocktype = a_location.getBlock().getType();
                    BlockData a_blockdata=a_location.getBlock().getBlockData();
                    Location b_location=a_location.clone().add(rv.normalize().multiply(-1.2));
                    if(!a_location.getBlock().isEmpty()){
                        if(PaperJJK.rule_breakblock){
                            if(b_location.getBlock().isLiquid()||(b_location.getBlock().getType().getHardness()<use_power/3&&b_location.getBlock().getType().getHardness()>=0)){
                                if(a_location.getBlock().isLiquid()||(a_location.getBlock().getType().getHardness()<use_power/3&&a_location.getBlock().getType().getHardness()>=0)){
                                    rrrr++;
                                    if(rrrr==500){
                                        b_location.getWorld().spawnFallingBlock(b_location, a_blocktype, (byte) 0);
                                        a_location.getBlock().setType(Material.AIR);
                                        rrrr=0;
                                    }
                                    else{
                                        b_location.getBlock().setType(a_blocktype);
                                        b_location.getBlock().setBlockData(a_blockdata);
                                        a_location.getBlock().setType(Material.AIR);

                                    }
                                }
                            }
                        }
                    }
                    else if(Math.random()<0.05){
                        Particle.DustOptions dust=new Particle.DustOptions(Color.BLUE, 1F);
                        location.getWorld().spawnParticle(Particle.DUST, a_location, 1, 0.02, 0.02, 0.02, 0.5, dust, true);
                    }
                }
            }

            // Check if use_power depleted - stop AO effect
            if(use_power <= 0 && aoEffectActive && user instanceof Player player) {
                JPacketSender.sendInfinityAoEnd(player);
                aoEffectActive = false;
            }
        }
    }
    public void charged(){
        if(reversecurse){
            if(user instanceof Player player){
                player.setCooldown(Material.WRITTEN_BOOK,4);
            }
            distance=500;
            fixed=true;
            t_location=user.getLocation().clone().add(0,1.5,0).add(user.getLocation().getDirection().clone().normalize().multiply(distance));
        }
    }
    public boolean scroll(int count){
        if(reversecurse){
            if(!charging) {
                if (count > 0) {
                    distance = 500;
                } else {
                    distance = 0;
                }
            }
        }
        else{
            distance+=count*3;
        }
        return true;
    }
    public String toname(){
        if(reversecurse){
            if(murasaki){
                return ChatColor.DARK_PURPLE+"murasaki"+ChatColor.WHITE+" *"+use_power+" , "+time/20;
            }
            else{
                return ChatColor.RED+"aka"+ChatColor.WHITE+" *"+use_power+" , "+time/20+" , "+(fixed?ChatColor.DARK_GRAY+"fixed":ChatColor.GREEN+"unfixed");
            }
        }
        else{
            return ChatColor.BLUE+"ao"+ChatColor.WHITE+" *"+use_power+" , "+time/20+" , "+(fixed?ChatColor.DARK_GRAY+"fixed":ChatColor.GREEN+"unfixed");
        }
    }
}
