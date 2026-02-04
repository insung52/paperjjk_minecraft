package org.justheare.paperJJK;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.justheare.paperJJK.network.PacketIds;

import java.util.ArrayList;
import java.util.UUID;

public class Mizushi_domain extends Jdomain_innate{

    ArrayList<LivingEntity> fuga_hit;
    private UUID domainId;  // Unique domain ID for client rendering
    private long lastSyncTime = 0;  // Last sync packet time
    void expand_effect(boolean nb){
        if(nb){
            owner.user.getWorld().playSound(owner.user.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 5F, 0.5F);
            for(Player targetplayer : owner.user.getLocation().getNearbyPlayers(nb_range)){
                targetplayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,40,1,false));
            }
        }
        else {
            owner.user.getWorld().playSound(owner.user.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 5F, 0.5F);
        }
    }
    void tp_effect(){
        owner.user.getWorld().playSound(owner.user.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 5F, 0.5F);
    }
    void set_special(){
        current_radius=0;
        fuga_hit = new ArrayList<LivingEntity>();
        special=true;
        nb_location.getWorld().playSound(nb_location,Sound.BLOCK_END_PORTAL_SPAWN, 80F, 0.5F);
        owner.player.setCooldown(Material.WRITTEN_BOOK,300);
    }

    public Mizushi_domain(Jobject owner) {
        super(owner);
        level=10;
        range=30;
        innate_border = Material.BEDROCK;
    }
    void start_effect(){
        Mizushi_effector effector = new Mizushi_effector(this);
        this.effector=effector;
        effector.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, effector, 1, 1);
    }

    @Override
    public boolean undrow_expand() {
        // Send END packet before destroying domain
        sendDomainEndPacket();
        return super.undrow_expand();
    }

    /**
     * Send START packet to clients for domain visualization
     * Called when barrier-less domain expansion begins
     */
    void sendDomainStartPacket() {
        if (owner.player == null || nb_location == null) return;

        domainId = UUID.randomUUID();
        lastSyncTime = System.currentTimeMillis();

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(PacketIds.DOMAIN_VISUAL);
        out.writeByte(PacketIds.DomainVisualAction.START);
        out.writeInt(PacketIds.DomainType.NO_BARRIER);
        out.writeDouble(nb_location.getX());
        out.writeDouble(nb_location.getY());
        out.writeDouble(nb_location.getZ());
        out.writeInt(nb_range);
        out.writeInt(0x3C3C3C);  // Dark gray color
        out.writeFloat(30.0f);   // Expansion speed: 4 blocks/tick * 20 ticks/sec = 80 blocks/sec
        out.writeLong(domainId.getMostSignificantBits());
        out.writeLong(domainId.getLeastSignificantBits());

        sendPacketToNearbyPlayers(out.toByteArray(), nb_range);
        PaperJJK.log("[Mizushi Domain] Sent START packet: domainId=" + domainId + ", maxRadius=" + nb_range);
    }

    /**
     * Send SYNC packet to clients (every 3 seconds)
     * Corrects client-side drift
     */
    void sendDomainSyncPacket() {
        if (owner.player == null || domainId == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSyncTime < 200) return;  // Only sync every 3 seconds

        lastSyncTime = currentTime;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(PacketIds.DOMAIN_VISUAL);
        out.writeByte(PacketIds.DomainVisualAction.SYNC);
        out.writeLong(domainId.getMostSignificantBits());
        out.writeLong(domainId.getLeastSignificantBits());
        out.writeFloat((float) current_radius);

        sendPacketToNearbyPlayers(out.toByteArray(), nb_range);
        PaperJJK.log("[Mizushi Domain] Sent SYNC packet: domainId=" + domainId + ", currentRadius=" + current_radius);
    }

    /**
     * Send END packet to clients when domain is destroyed
     */
    void sendDomainEndPacket() {
        if (owner.player == null || domainId == null) return;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(PacketIds.DOMAIN_VISUAL);
        out.writeByte(PacketIds.DomainVisualAction.END);
        out.writeLong(domainId.getMostSignificantBits());
        out.writeLong(domainId.getLeastSignificantBits());

        sendPacketToNearbyPlayers(out.toByteArray(), nb_range + 50);  // Extended range for cleanup
        PaperJJK.log("[Mizushi Domain] Sent END packet: domainId=" + domainId);
    }

    /**
     * Send packet to all players within range
     */
    private void sendPacketToNearbyPlayers(byte[] data, double range) {
        if (owner.player == null) return;

        Location center = nb_location != null ? nb_location : owner.player.getLocation();
        for (Player player : center.getNearbyPlayers(1000)) {
            player.sendPluginMessage(PaperJJK.jjkplugin, "paperjjk:main", data);
        }
    }
}
class Mizushi_effector extends Jdomain_effector{
    Mizushi_domain domain;
    double tick1=0;
    double tick2=0;
    double rx,ry,rz;
    // Sphere surface cache variables (normal mode)
    java.util.List<Vector> shellBlockList = null;  // Current shell's block offsets
    int shellBlockIndex = 0;  // Progress through current shell
    // Sphere surface cache variables (special mode - fuga)
    java.util.List<Vector> shellBlockList_special = null;
    int shellBlockIndex_special = 0;
    boolean startPacketSent = false;
    Particle.DustOptions dark_dust2=new Particle.DustOptions(Color.fromRGB(60,0,0), 1F);
    public void effect_tick(){

        if(domain.no_border_on){
            // Send START packet when expansion begins
            if(!startPacketSent && domain.current_radius >= 1) {
                domain.sendDomainStartPacket();
                startPacketSent = true;
            }
            if(tick>20){
                if(domain.special){
                    // === SPECIAL MODE (FUGA) - PROCESS UP TO 3 SHELLS PER TICK ===
                    for (int shellCount = 0; shellCount < 8; shellCount++) {
                        // Stop if reached max radius
                        if (domain.current_radius >= domain.nb_range) {
                            domain.special = false;
                            domain.current_radius = domain.nb_range;
                            domain.undrow_expand();
                            break;
                        }

                        // Initialize shell block list if needed (new radius or first time)
                        if (shellBlockList_special == null || shellBlockIndex_special >= shellBlockList_special.size()) {
                            java.util.Set<Vector> offsets = PaperJJK.getSphereSurfaceOffsets(domain.current_radius);
                            shellBlockList_special = new java.util.ArrayList<>(offsets);
                            shellBlockIndex_special = 0;
                        }

                        // Process up to 20000 blocks per tick (same as original speed)
                        int blocksProcessed = 0;
                        while (blocksProcessed < 500000 && shellBlockIndex_special < shellBlockList_special.size()) {
                            Vector offset = shellBlockList_special.get(shellBlockIndex_special);

                            double y_offset = offset.getY();
                            double x_offset = offset.getX();
                            double z_offset = offset.getZ();

                            // If onground, skip blocks below ground (rx<0 in original code)
                            if (domain.onground && y_offset < 0) {
                                shellBlockIndex_special++;
                                blocksProcessed++;
                                continue;
                            }

                            // Get block location (original: add(ry, rx-4-Math.random(), rz))
                            Location tlocation = domain.nb_location.clone().add(x_offset, y_offset - 4 - Math.random(), z_offset);

                            // === TODO: PARTICLE/EXPLOSION/DAMAGE LOGIC ===
                            // Original logic:
                            // - if empty block: spawn FLAME particle (with probability based on radius)
                            // - else: 80% chance to create explosion
                            //
                            // Placeholder for user to implement:
                            if (tlocation.getBlock().isEmpty()) {
                                // TODO: Particle spawning
                                if(Math.random()>Math.pow(domain.current_radius*1.0/200.0,0.3)){
                                    tlocation.getWorld().spawnParticle(Particle.FLAME, tlocation, 1, 1, 1, 1, 0.1, null, true);
                                }
                            } else {
                                // TODO: Explosion
                                if(tlocation.getBlock().isLiquid()){
                                    tlocation.getBlock().setType(Material.FIRE);
                                }
                                else if(Math.random()<0.5){
                                    float hn = tlocation.getBlock().getType().getHardness();
                                    if(hn>-1&&Math.random()<10.0/hn){
                                        tlocation.getBlock().setType(Material.FIRE);
                                    }
                                }
                                if(Math.random()<0.01){
                                    tlocation.createExplosion(domain.owner.user,7,PaperJJK.rule_breakblock,PaperJJK.rule_breakblock);
                                }
                            }


                            shellBlockIndex_special++;
                            blocksProcessed++;
                        }

                        // Check if current shell is complete
                        if (shellBlockIndex_special >= shellBlockList_special.size()) {
                            domain.current_radius++;
                            shellBlockList_special = null;  // Force regeneration for next radius

                            // === TODO: DAMAGE ENTITIES WHEN SHELL COMPLETES ===
                            // Original logic: damage all entities in radius when shell completes
                            //
                            // Placeholder for user to implement:
                            ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) domain.nb_location.getNearbyLivingEntities(domain.current_radius);
                            tentities.remove(domain.owner.user);
                            tentities.removeAll(domain.fuga_hit);
                            for(LivingEntity tentity : tentities){
                                if(tentity.getLocation().distance(domain.nb_location)>domain.current_radius) continue;
                                if(domain.onground && tentity.getLocation().getY()<domain.nb_location.getY()-5) continue;
                                domain.fuga_hit.add(tentity);
                                tentity.setFireTicks(4444);
                                domain.owner.damaget(tentity,'j',domain.nb_range*4,false,"fuga",false);
                            }

                            if (domain.current_radius >= domain.nb_range) {
                                domain.special = false;
                                domain.current_radius = domain.nb_range;
                                domain.undrow_expand();
                                break;
                            }

                            if (domain.current_radius % 4 == 0) {
                                break;  // Break inner loop, continue to next tick
                            }
                        } else {
                            // Current shell not complete, stop processing more shells this tick
                            break;
                        }
                    }
                }
                else{
                    if(domain.current_radius < domain.nb_range){
                        //PaperJJK.log("running");

                        // === PROCESS UP TO 3 SHELLS PER TICK ===
                        for (int shellCount = 0; shellCount < 6; shellCount++) {
                            // Stop if reached max radius
                            if (domain.current_radius >= domain.nb_range) {
                                break;
                            }

                            // Initialize shell block list if needed (new radius or first time)
                            if (shellBlockList == null || shellBlockIndex >= shellBlockList.size()) {
                                java.util.Set<Vector> offsets = PaperJJK.getSphereSurfaceOffsets(domain.current_radius);
                                shellBlockList = new java.util.ArrayList<>(offsets);
                                shellBlockIndex = 0;
                            }

                            // Process all blocks in current shell (or up to 'speed' limit)
                            int blocksProcessed = 0;
                            while (blocksProcessed < 200000 && shellBlockIndex < shellBlockList.size()) {

                                Vector offset = shellBlockList.get(shellBlockIndex);

                                // Apply random offset (same as original code)
                                double y_offset = offset.getY();
                                double x_offset = offset.getX();
                                double z_offset = offset.getZ();

                                // If onground, clamp Y to prevent going too far underground
                                if (domain.onground && y_offset < 1) {
                                    shellBlockIndex++;
                                    blocksProcessed++;
                                    continue;
                                }

                                // Get block location (original: add(ry, rx-4, rz))
                                Location tlocation = domain.nb_location.clone().add(x_offset, y_offset - 4, z_offset);

                                // === BLOCK BREAKING LOGIC (230~244) - UNCHANGED ===
                                if(PaperJJK.rule_breakblock){
                                    if(tlocation.getBlock().isLiquid()){
                                        tlocation.getBlock().setType(Material.AIR);
                                    }
                                    else if(!tlocation.getBlock().isEmpty()){
                                        if(tlocation.getBlock().getType().getHardness()<=1){
                                            if(tlocation.getBlock().getType().getHardness()>-1){
                                                tlocation.getBlock().setType(Material.AIR);
                                            }
                                        }
                                        else if(Math.random()<=Math.pow(1/tlocation.getBlock().getType().getHardness(),0.3)){
                                            tlocation.getBlock().setType(Material.AIR);
                                        }
                                    }
                                }
                                // Particle rendering replaced by client-side BufferBuilder sphere
                                // Old particle code (lines 147-150):
                                /*
                                if(tlocation.getBlock().isPassable()&&Math.random()>Math.pow(domain.current_radius*1.0/200.0,0.01)){
                                    Particle.DustOptions dust=new Particle.DustOptions(Color.WHITE, (float) (200 - domain.current_radius) /50);
                                    tlocation.getWorld().spawnParticle(Particle.DUST, tlocation, 1, 0, 0, 0, 1, dust, true);
                                }
                                */

                                shellBlockIndex++;
                                blocksProcessed++;
                            }

                            // Check if current shell is complete
                            if (shellBlockIndex >= shellBlockList.size()) {
                                domain.current_radius++;
                                shellBlockList = null;  // Force regeneration for next radius
                            } else {
                                // Current shell not complete, stop processing more shells this tick
                                break;
                            }
                        }

                        // Send SYNC packet every tick (same as original)
                        domain.sendDomainSyncPacket();
                    }
                    for(int r=0; r<30+Math.pow(domain.current_radius,2.3)/40; r++){
                        Vector r_vector = new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5).normalize();
                        Vector l_vector = new Vector(Math.random()-0.5,domain.onground?(Math.random()/2):(Math.random()-0.5),Math.random()-0.5).normalize().multiply(Math.pow(Math.random(),0.35)*domain.current_radius);
                        Location ss_location = domain.nb_location.clone().add(l_vector);
                        for (double rr = -9; rr <=  9; rr+=1) {
                            //t_location.clone().add(direction.multiply(r));
                            Location s_location= ss_location.clone().add(r_vector.multiply(rr/5));
                            if(PaperJJK.rule_breakblock){
                                if(s_location.getBlock().isLiquid()){
                                    s_location.getBlock().setType(Material.AIR);
                                }
                                else if(!s_location.getBlock().isEmpty()){
                                    float hn = s_location.getBlock().getType().getHardness();
                                    if(hn>-1&&Math.random()<1.0/hn){
                                        s_location.getBlock().setType(Material.AIR);
                                    }
                                }
                            }

                            s_location.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, s_location, 1, 0, 0, 0, 0, null, false);
                            s_location.getWorld().spawnParticle(Particle.DUST, s_location, 1, 0, 0, 0, 0, dark_dust2, false);
                        }
                        if(Math.random()>0.95){
                            ss_location.getWorld().playSound(ss_location, Sound.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 0.2F, 0.2F);
                            ss_location.getWorld().playSound(ss_location, Sound.ITEM_TRIDENT_RIPTIDE_3, SoundCategory.PLAYERS, 0.5F, 0.7F);
                        }
                    }
                    if(tick%10==0){
                        ArrayList<Entity> tentities = (ArrayList<Entity>) domain.nb_location.getNearbyEntities(domain.nb_range,domain.nb_range,domain.nb_range);
                        for(Entity living : tentities){
                            if(living.equals(domain.owner.user)){
                                continue;
                            }
                            if(living instanceof BlockDisplay){
                                continue;
                            }
                            if(living.getLocation().distance(domain.nb_location)< domain.current_radius){
                                Jobject jobject = PaperJJK.getjobject(living);
                                if(jobject!=null && jobject.user instanceof Player player && SimpleDomainManager.isActive(player)){
                                    // Simple domain is active - ignore sure-hit effect
                                    SimpleDomainManager.decreasePower(player, domain.level*4);
                                    continue;
                                }
                                Mizushi mizushi = new Mizushi(domain.owner,"","",true,1, (int) (Math.random() * 7)+5,'a');
                                mizushi.show=false;
                                mizushi.sure_hit=true;
                                mizushi.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,mizushi,1,1);
                                domain.owner.jujuts.add(mizushi);
                                mizushi.j_entities.add(living);
                                mizushi.direction = new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5).normalize();
                            }

                        }
                    }
                }
            }
            else if(tick==1){
            }
            else if(tick==20){
                domain.nb_location.getWorld().playSound(domain.nb_location,Sound.ENTITY_ENDER_DRAGON_AMBIENT, 80F, 0.5F);
                if(domain.attack_target!=null){
                    domain.owner.user.getWorld().playSound(domain.owner.user.getLocation(),Sound.ENTITY_ENDER_DRAGON_AMBIENT, 80F, 0.5F);
                }
            }
        }

        else if(domain.attacker==null){
            if(tick%10==0){
                ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) domain.location.getNearbyLivingEntities(domain.range+1);
                ArrayList<LivingEntity> tee=new ArrayList<>();
                for(LivingEntity living : tentities){
                    if(living.getLocation().distance(domain.location)<= domain.range+1){
                        if(living.equals(domain.owner.user)){
                            continue;
                        }
                        else {
                            if(living instanceof BlockDisplay){
                                continue;
                            }
                            Jobject jobject = PaperJJK.getjobject(living);
                            if(jobject!=null && jobject.user instanceof Player player && SimpleDomainManager.isActive(player)){
                                // Simple domain is active - ignore sure-hit effect
                                SimpleDomainManager.decreasePower(player, domain.level*4);
                                tee.add(living);
                                continue;
                            }
                            if(jobject!=null && jobject.naturaltech.equals("physical_gifted")){
                                continue;
                            }
                            if(living.getLocation().distance(domain.location)< domain.range){
                                Mizushi mizushi = new Mizushi(domain.owner,"","",true,1,20,'a');
                                mizushi.show=false;
                                mizushi.sure_hit=true;
                                mizushi.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin,mizushi,1,1);
                                domain.owner.jujuts.add(mizushi);
                                mizushi.j_entities.add(living);
                                mizushi.direction = new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5).normalize();
                            }
                        }
                        tee.add(living);
                    }
                }
                domain.domain_targets=tee;
            }

        }
    }
    Mizushi_effector(Mizushi_domain domain) {
        super(domain);
        this.domain = domain;
    }
}