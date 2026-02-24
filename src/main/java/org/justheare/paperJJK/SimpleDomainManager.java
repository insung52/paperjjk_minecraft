package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.justheare.paperJJK.network.JPacketSender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple Domain (간이영역) Manager
 *
 * Manages simple domain charging and power for each player.
 * - Charging: Power increases each tick (max 100%)
 * - Not charging: Power decreases each tick (min 0%)
 * - Active (≥1%): Player is immune to domain sure-hit effects
 */
public class SimpleDomainManager {
    // Player UUID -> SimpleDomainData
    private static final Map<UUID, SimpleDomainData> playerData = new HashMap<>();

    // Configuration
    private static final double CHARGE_RATE = 10.0;    // Power increase per tick when charging (1% per tick = 5 seconds to 100%)
    private static final double BASE_DECAY_RATE = 0.8; // Base power decrease per tick when not charging (0.5% per tick = 10 seconds to 0%)
    private static final double MAX_RADIUS = 10.0;    // Maximum circle radius at 100% power (blocks)
    private static final int EXPANSION_DELAY = 100;
    private static final int MAX_POWER = 230;
    // Particle animation
    private static double particleAngle = 0.0; // Current rotation angle for particle circle
    /**
     * Simple Domain data for a player
     */
    public static class SimpleDomainData {
        public boolean charging = false;
        public double power = 0.0; // 0-100%
        public Location location;

        public SimpleDomainData() {
            this.charging = false;
            this.power = 0.0;
        }
    }

    /**
     * Start charging simple domain for a player
     */
    public static void startCharging(Player player) {
        SimpleDomainData data = getOrCreate(player);
        data.charging = true;
        PaperJJK.log(String.format("[Simple Domain] %s: Charging started", player.getName()));
        if(data.power==0){
            // Fresh start: record caster position and notify client
            data.location = player.getLocation();
            JPacketSender.sendSimpleDomainActivate(player, data.location, data.power, EXPANSION_DELAY, MAX_POWER);
        }
        JPacketSender.sendSimpleDomainActivate(player, data.location, data.power, EXPANSION_DELAY, MAX_POWER);
    }

    /**
     * Stop charging simple domain for a player
     */
    public static void endCharging(Player player) {
        SimpleDomainData data = getOrCreate(player);
        data.charging = false;
        PaperJJK.log(String.format("[Simple Domain] %s: Charging stopped", player.getName()));
        // Notify client so it can start local decay simulation

        JPacketSender.sendSimpleDomainChargingEnd(player, data.power, data.location);

    }
    public static void shutdown(Player player){
        SimpleDomainData data = getOrCreate(player);
        data.charging = false;
        PaperJJK.log(String.format("[Simple Domain] %s: Shutdown", player.getName()));
        if (data.power > 0) {
            data.power = 0.0;
            JPacketSender.sendSimpleDomainDeactivate(player);
        }
    }
    /**
     * Tick all players' simple domain power
     * Called every game tick (20 times per second)
     */
    public static void tick() {
        for (Map.Entry<UUID, SimpleDomainData> entry : playerData.entrySet()) {
            UUID uuid = entry.getKey();
            SimpleDomainData data = entry.getValue();
            double oldPower = data.power;

            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }
            if (data.charging) {
                // Stop charging if player stops sneaking
                if (!player.isSneaking()) {
                    data.charging = false;
                    JPacketSender.sendSimpleDomainChargingEnd(player, data.power, data.location);
                } else {
                    // Charging: Increase power
                    Vector loc_direction = player.getLocation().toVector().add(data.location.toVector().multiply(-1));
                    double speed = Math.min(loc_direction.length(),0.1);
                    data.location.add(loc_direction.normalize().multiply(speed));
                    data.power = Math.min(MAX_POWER, data.power + CHARGE_RATE);
                    Jplayer jplayer = getJplayer(player);
                    if(jplayer!=null){
                        jplayer.curseenergy -= (int) (data.power/10);
                        if(jplayer.curseenergy<=0){
                            data.charging = false;
                            JPacketSender.sendSimpleDomainChargingEnd(player, data.power, data.location);
                        }
                    }

                }
            } else if (data.power > 0) {
                // Not charging: Decrease power
                // Base decay + additional decay from opponent's barrier technique level
                double prevPower = data.power;
                data.power = Math.max(0.0, data.power - BASE_DECAY_RATE);
                if(data.power<EXPANSION_DELAY){
                    data.power = Math.max(0.0, data.power - CHARGE_RATE);
                }
                // Detect transition to 0 and notify client
                if (data.power == 0.0 && prevPower > 0.0) {
                    JPacketSender.sendSimpleDomainDeactivate(player);
                }
            }

            // Log power changes
            if (oldPower != data.power) {
                //PaperJJK.log(String.format("[Simple Domain] %s: %.1f%% (charging: %b)",
                //    player.getName(), data.power, data.charging));
            }

            // Show particle effects if active
            if (data.power >= 1.0) {
                double radius = (Math.max(data.power - EXPANSION_DELAY,0.0) / (MAX_POWER - EXPANSION_DELAY)) * MAX_RADIUS;
                Location ylocation = data.location.clone();
                ylocation.setY(player.getLocation().getY());
                if(player.getLocation().distance(ylocation)>radius){
                    data.power = Math.max(0.0, data.power - BASE_DECAY_RATE*5);
                    JPacketSender.sendSimpleDomainPowerSync(player, data.power);
                }

                showParticleEffect(player, Math.max(data.power - EXPANSION_DELAY,0.0),data.location);

            }
            else {
                // power == 0, domain inactive — nothing to render
            }
        }
    }

    /**
     * Get simple domain power for a player (0-100%)
     */
    public static double getPower(Player player) {
        SimpleDomainData data = playerData.get(player.getUniqueId());
        return data != null ? data.power : 0.0;
    }

    /**
     * Check if simple domain is active (≥1%)
     * Active simple domain blocks domain sure-hit effects
     */
    public static boolean isActive(Player player) {
        return getPower(player) >= EXPANSION_DELAY;
    }
    public static void decreasePower(Player player, int level){
        SimpleDomainData data = playerData.get(player.getUniqueId());
        if (data == null) return;
        double prevPower = data.power;
        data.power = Math.max(0, data.power - (((double) level) / 7.0));
        if (data.power == 0.0 && prevPower > 0.0) {
            JPacketSender.sendSimpleDomainDeactivate(player);
        } else if (data.power != prevPower) {
            JPacketSender.sendSimpleDomainPowerSync(player, data.power);
        }
    }
    /**
     * Check if player is currently charging
     */
    public static boolean isCharging(Player player) {
        SimpleDomainData data = playerData.get(player.getUniqueId());
        return data != null && data.charging;
    }

    /**
     * Cleanup player data (called on disconnect)
     */
    public static void cleanup(Player player) {
        playerData.remove(player.getUniqueId());
        PaperJJK.log(String.format("[Simple Domain] %s: Data cleaned up", player.getName()));
    }

    /**
     * Get or create simple domain data for a player
     */
    private static SimpleDomainData getOrCreate(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), k -> new SimpleDomainData());
    }

    /**
     * Show particle effect for simple domain
     * - Displays white firework particles in a rotating circle
     * - Circle radius scales with power (0-100% → 0-10 blocks)
     * - Circle is drawn at ground level (player's feet)
     */
    private static void showParticleEffect(Player player, double power,Location center) {
        // Get Jplayer to check simple_domain_type
        Jplayer jplayer = getJplayer(player);
        if (jplayer == null) {
            return;
        }

        if(jplayer.simple_domain_type) {
            // Calculate radius based on power (0-100% → 0-MAX_RADIUS blocks)
            double radius = (power / (MAX_POWER - EXPANSION_DELAY)) * MAX_RADIUS;

            // Rotate angle for spinning effect
            particleAngle += Math.PI / 24.0; // Rotate ~9 degrees per tick
            if (particleAngle >= 2 * Math.PI) {
                particleAngle -= 2 * Math.PI;
            }

            // Draw circle with white firework particles
            int particleCount = 8; // More particles for larger circles (minimum 16)

            for (int i = 0; i < particleCount; i++) {
                double angle = particleAngle + (2 * Math.PI * i / particleCount);
                double x = center.getX() + radius * Math.cos(angle);
                double z = center.getZ() + radius * Math.sin(angle);
                double y = center.getY() + 0.1; // Slightly above ground

                Location particleLoc = new Location(center.getWorld(), x, y, z);

                // Spawn white firework particle (no velocity, no offset)
                center.getWorld().spawnParticle(
                        Particle.ELECTRIC_SPARK,
                        particleLoc,
                        1,    // count
                        0.1,  // offsetX
                        0.1,  // offsetY
                        0.1,  // offsetZ
                        0.1   // extra (speed)
                );
            }
        }
        else {
            // 미허갈롱 타입 - 구형 보호막 파티클
            // Calculate radius based on power (0-100% → 0-MAX_RADIUS blocks)
            double radius = (power / MAX_POWER) * MAX_RADIUS / 3;

            // Draw sphere with white particles
            //Location center = player.getLocation().add(0, 1, 0); // Center at player's chest height

            // Use spherical coordinates to create uniform sphere
            // Number of particles depends on radius (larger sphere = more particles)
            int latitudes = Math.max(6, (int)(radius * 3)); // θ divisions
            int longitudes = Math.max(8, (int)(radius * 4)); // φ divisions

            for (int lat = 0; lat < latitudes; lat++) {
                // θ (theta) angle from 0 to π
                double theta = Math.PI * lat / latitudes;
                double sinTheta = Math.sin(theta);
                double cosTheta = Math.cos(theta);

                for (int lon = 0; lon < longitudes; lon++) {
                    // φ (phi) angle from 0 to 2π
                    double phi = 2 * Math.PI * lon / longitudes;
                    double sinPhi = Math.sin(phi);
                    double cosPhi = Math.cos(phi);

                    // Spherical to Cartesian conversion
                    // x = r * sin(θ) * cos(φ)
                    // y = r * cos(θ)
                    // z = r * sin(θ) * sin(φ)
                    double x = center.getX() + radius * sinTheta * cosPhi;
                    double y = center.getY() + radius * cosTheta;
                    double z = center.getZ() + radius * sinTheta * sinPhi;

                    Location particleLoc = new Location(center.getWorld(), x, y, z);

                    // Spawn particle (every other point to reduce lag)
                    if ((lat + lon) % 2 == 0) {
                        center.getWorld().spawnParticle(
                            Particle.ELECTRIC_SPARK,
                            particleLoc,
                            1,
                            0.1,
                            0.1,
                            0.1,
                            0.0
                        );
                    }
                }
            }
        }
    }

    /**
     * Get Jplayer from Player
     */
    private static Jplayer getJplayer(Player player) {
        for (Jobject obj : PaperJJK.jobjects) {
            if (obj instanceof Jplayer jp && jp.user.getUniqueId().equals(player.getUniqueId())) {
                return jp;
            }
        }
        return null;
    }

    /**
     * Find nearby domain that the player is inside
     * Returns the opponent's domain (not player's own domain)
     */
    private static Jdomain_expand findNearbyDomain(Player player) {
        for (Jdomain_expand domain : PaperJJK.expanded_domains) {
            // Skip if this is the player's own domain
            if (domain.owner.user.equals(player)) {
                continue;
            }
            if (domain.owner.innate_domain.domain_targets.contains(player)) {
                return domain;
            }
        }
        return null;
    }
}
