package org.justheare.paperJJK;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.justheare.paperJJK.network.JPacketSender;

import java.util.*;

public class Infinity extends Jujut{
    // Explosion system fields - Directional Energy Grid (Ray-tracing style)
    private double[][] energyGrid = null;  // [theta_index][phi_index] - energy per direction
    private static final int ENERGY_RESOLUTION = 32;   // 16x16 = 256 directions
    private static final double SAMPLE_DENSITY = 2.3;  // Samples per unit surface area
    boolean murasaki=false;
    boolean unlimit_m=false;
    int soundtick=0;
    boolean aoEffectActive=false;   // Track if AO packet effect is active
    boolean akaEffectActive=false;  // Track if AKA packet effect is active
    boolean murasakiEffectActive=false;  // Track if MURASAKI packet effect is active
    private final String uniqueId;  // Unique ID for this skill instance
    @Override
    public void disabled() {
        location.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 3, 2);

        // Send END packet when AO effect stops
        if(aoEffectActive && user instanceof Player player) {
            JPacketSender.sendInfinityAoEnd(player, uniqueId);
            aoEffectActive = false;
        }

        // Send END packet when AKA effect stops
        if(akaEffectActive && user instanceof Player player) {
            JPacketSender.sendInfinityAkaEnd(player, uniqueId);
            akaEffectActive = false;
        }

        // Send END packet when MURASAKI effect stops
        if(murasakiEffectActive && user instanceof Player player) {
            JPacketSender.sendInfinityMurasakiEnd(player, uniqueId);
            murasakiEffectActive = false;
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

        // Generate unique ID for this skill instance
        // Format: INFINITY_<type>_<timestamp>_<nanoTime>
        String effectType = rct ? "AKA" : "AO";
        this.uniqueId = "INFINITY_" + effectType + "_" + System.currentTimeMillis() + "_" + System.nanoTime();
    }
    @Override
    public void run() {
        if(soundtick%5==0){
            if(charging || recharging){
                location.getWorld().playSound(location, Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, (float) use_power /60, (float) ((float) use_power /100*1.5 + 0.5));
            }
        }
        if(!reversecurse){
            // Send START packet when AO begins (only once)
            if(!aoEffectActive && user instanceof Player player) {
                // Scale usepower (1-100) to strength (0.1-5.0)
                float strength = (float) (0.1 * 0.049 + 0.051);  // Linear scale: 1->0.1, 100->5.0
                JPacketSender.sendInfinityAoStart(player, location, strength, uniqueId);
                aoEffectActive = true;
            }
            // Send SYNC packet every 10 ticks (0.5 seconds)
            if(soundtick%5==0){
                use_power--;
                // Send position/strength update to client
                if(user instanceof Player player) {
                    float strength = (float) (use_power * 0.049 + 0.051);  // Scale 1-100 to 0.1-5.0
                    JPacketSender.sendInfinityAoSync(player, location, strength, uniqueId);
                }
            }
        }
        else {
            // Send START packet when AKA begins (only once)
            if(!akaEffectActive && user instanceof Player player) {
                // Scale usepower (1-100) to strength (0.1-5.0)
                float strength = (float) (0.1 * 0.049 + 0.051);  // Linear scale: 1->0.1, 100->5.0
                JPacketSender.sendInfinityAkaStart(player, location, strength, uniqueId);
                akaEffectActive = true;
            }
            // Send SYNC packet every 10 ticks (0.5 seconds)
            if(soundtick%5==0){
                // Send position/strength update to client
                if(user instanceof Player player) {
                    float strength = (float) (use_power / 3.0 * 0.049 + 0.051);  // Scale 1-100 to 0.1-5.0
                    JPacketSender.sendInfinityAkaSync(player, location, strength, uniqueId);
                }
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
                        me_tick = 0; // Reset explosion tick counter when murasaki starts
                        me_cr=0;
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
                            time = (int) (50 + Math.pow(use_power,3) / 10);
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
    /**
     * Generate uniformly distributed points on a sphere using Fibonacci (Golden Spiral) algorithm
     * This ensures perfect spherical shape regardless of radius
     *
     * @param sampleCount Number of sample points to generate
     * @return List of normalized direction vectors
     */
    private List<Vector> generateFibonacciSphere(int sampleCount) {
        List<Vector> points = new ArrayList<>();
        double goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0;
        double angleIncrement = 2.0 * Math.PI * goldenRatio;

        for (int i = 0; i < sampleCount; i++) {
            double t = (double) i / sampleCount;
            double inclination = Math.acos(1.0 - 2.0 * t);
            double azimuth = angleIncrement * i;

            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);

            points.add(new Vector(x, y, z));
        }

        return points;
    }

    /**
     * Convert 3D direction vector to 2D energy grid indices
     * Uses Equirectangular projection (like Minecraft panorama)
     *
     * @param direction Normalized direction vector
     * @return int[2] - {theta_index, phi_index}
     */
    private int[] directionToGridIndex(Vector direction) {
        // Normalize just in case
        Vector dir = direction.clone().normalize();

        // Spherical coordinates
        double theta = Math.atan2(dir.getZ(), dir.getX()); // Longitude: -π to π
        double phi = Math.acos(Math.max(-1.0, Math.min(1.0, dir.getY()))); // Latitude: 0 to π

        // Map to grid indices
        int thetaIndex = (int) ((theta + Math.PI) / (2.0 * Math.PI) * ENERGY_RESOLUTION);
        int phiIndex = (int) (phi / Math.PI * ENERGY_RESOLUTION);

        // Clamp to valid range
        thetaIndex = Math.max(0, Math.min(ENERGY_RESOLUTION - 1, thetaIndex));
        phiIndex = Math.max(0, Math.min(ENERGY_RESOLUTION - 1, phiIndex));

        return new int[]{thetaIndex, phiIndex};
    }

    /**
     * Convert 2D energy grid indices back to 3D direction vector
     *
     * @param thetaIndex Theta grid index
     * @param phiIndex Phi grid index
     * @return Normalized direction vector
     */
    private Vector gridIndexToDirection(int thetaIndex, int phiIndex) {
        // Map grid indices back to spherical coordinates
        double theta = (thetaIndex + 0.5) / ENERGY_RESOLUTION * 2.0 * Math.PI - Math.PI;
        double phi = (phiIndex + 0.5) / ENERGY_RESOLUTION * Math.PI;

        // Convert to Cartesian
        double x = Math.sin(phi) * Math.cos(theta);
        double y = Math.cos(phi);
        double z = Math.sin(phi) * Math.sin(theta);

        return new Vector(x, y, z);
    }

    /**
     * Weighted grid cell for interpolation
     */
    private static class WeightedGridCell {
        int thetaIdx;
        int phiIdx;
        double weight;

        public WeightedGridCell(int thetaIdx, int phiIdx, double weight) {
            this.thetaIdx = thetaIdx;
            this.phiIdx = phiIdx;
            this.weight = weight;
        }
    }

    /**
     * Get interpolated energy from surrounding grid cells (Bilinear Interpolation)
     * This smooths out the aliasing artifacts from discrete grid mapping
     *
     * @param direction Normalized direction vector
     * @param mode 4 for 2x2 bilinear, 9 for 3x3 smoothing
     * @return List of weighted grid cells to sample
     */
    private List<WeightedGridCell> getInterpolatedGridCells(Vector direction, int mode) {
        List<WeightedGridCell> cells = new ArrayList<>();
        Vector dir = direction.clone().normalize();

        // Convert to continuous grid coordinates
        double theta = Math.atan2(dir.getZ(), dir.getX());
        double phi = Math.acos(Math.max(-1.0, Math.min(1.0, dir.getY())));

        // Continuous grid position (not rounded!)
        double thetaContinuous = (theta + Math.PI) / (2.0 * Math.PI) * ENERGY_RESOLUTION;
        double phiContinuous = phi / Math.PI * ENERGY_RESOLUTION;

        if (mode == 4) {
            // === 2x2 Bilinear Interpolation (빠름) ===
            int thetaFloor = (int) Math.floor(thetaContinuous);
            int phiFloor = (int) Math.floor(phiContinuous);

            // Fractional parts (0.0 ~ 1.0)
            double thetaFrac = thetaContinuous - thetaFloor;
            double phiFrac = phiContinuous - phiFloor;

            // 4 corner cells
            int[][] corners = {
                {thetaFloor, phiFloor},
                {thetaFloor + 1, phiFloor},
                {thetaFloor, phiFloor + 1},
                {thetaFloor + 1, phiFloor + 1}
            };

            // Bilinear weights
            double[] weights = {
                (1.0 - thetaFrac) * (1.0 - phiFrac),  // [0,0]
                thetaFrac * (1.0 - phiFrac),           // [1,0]
                (1.0 - thetaFrac) * phiFrac,           // [0,1]
                thetaFrac * phiFrac                    // [1,1]
            };

            for (int i = 0; i < 4; i++) {
                int ti = corners[i][0] % ENERGY_RESOLUTION;  // Wrap theta (periodic)
                int pi = Math.max(0, Math.min(ENERGY_RESOLUTION - 1, corners[i][1]));  // Clamp phi
                if (ti < 0) ti += ENERGY_RESOLUTION;

                cells.add(new WeightedGridCell(ti, pi, weights[i]));
            }

        } else if (mode == 9) {
            // === 3x3 Gaussian-like Smoothing (더 부드러움) ===
            int thetaCenter = (int) Math.round(thetaContinuous);
            int phiCenter = (int) Math.round(phiContinuous);

            // 3x3 neighborhood
            for (int dt = -1; dt <= 1; dt++) {
                for (int dp = -1; dp <= 1; dp++) {
                    int ti = (thetaCenter + dt) % ENERGY_RESOLUTION;
                    int pi = phiCenter + dp;

                    if (ti < 0) ti += ENERGY_RESOLUTION;
                    if (pi < 0 || pi >= ENERGY_RESOLUTION) continue;

                    // Calculate weight based on distance to cell center
                    double cellTheta = (ti + 0.5) / ENERGY_RESOLUTION * 2.0 * Math.PI - Math.PI;
                    double cellPhi = (pi + 0.5) / ENERGY_RESOLUTION * Math.PI;

                    Vector cellDir = new Vector(
                        Math.sin(cellPhi) * Math.cos(cellTheta),
                        Math.cos(cellPhi),
                        Math.sin(cellPhi) * Math.sin(cellTheta)
                    );

                    // Weight = cosine similarity (angle proximity)
                    double cosSimilarity = dir.dot(cellDir);
                    double weight = Math.max(0, cosSimilarity); // Only positive weights

                    if (weight > 0.01) {  // Skip negligible weights
                        cells.add(new WeightedGridCell(ti, pi, weight));
                    }
                }
            }

            // Normalize weights to sum to 1.0
            double totalWeight = cells.stream().mapToDouble(c -> c.weight).sum();
            if (totalWeight > 0) {
                for (WeightedGridCell cell : cells) {
                    cell.weight /= totalWeight;
                }
            }
        }

        return cells;
    }

    /**
     * Get interpolated energy value for a direction
     */
    private double getInterpolatedEnergy(Vector direction, int mode) {
        List<WeightedGridCell> cells = getInterpolatedGridCells(direction, mode);
        double totalEnergy = 0.0;

        for (WeightedGridCell cell : cells) {
            totalEnergy += energyGrid[cell.thetaIdx][cell.phiIdx] * cell.weight;
        }

        return totalEnergy;
    }

    /**
     * Apply energy loss to interpolated grid cells
     */
    private void applyInterpolatedEnergyLoss(Vector direction, double energyLoss, int mode) {
        List<WeightedGridCell> cells = getInterpolatedGridCells(direction, mode);

        for (WeightedGridCell cell : cells) {
            double cellLoss = energyLoss * cell.weight;
            energyGrid[cell.thetaIdx][cell.phiIdx] = Math.max(0, energyGrid[cell.thetaIdx][cell.phiIdx] - cellLoss);
        }
    }

    /**
     * Modern spherical energy wave explosion
     * Based on the design document: "대규모 현실적 폭발 알고리즘 설계 문서"
     *
     * Key features:
     * - Perfect spherical shape at all radii (Fibonacci sphere sampling)
     * - Sample density proportional to r² (no holes at large radius)
     * - Energy-based propagation with block resistance
     * - Queue-based processing for performance stability
     * - Shell-based expansion over time (not instant)
     */

    int me_tick=0;
    int me_cr=0;
    public void murasaki_explode(){
        // === PHASE 1: Initialize energy grid on first tick ===
        if (me_tick == 0) {
            // Create fresh energy grid
            energyGrid = new double[ENERGY_RESOLUTION][ENERGY_RESOLUTION];

            for (int i = 0; i < ENERGY_RESOLUTION; i++) {
                for (int j = 0; j < ENERGY_RESOLUTION; j++) {
                    energyGrid[i][j] = Math.pow(use_power-19,1.1);
                }
            }

            PaperJJK.log("[Murasaki Explode] Initialized " + ENERGY_RESOLUTION + "x" + ENERGY_RESOLUTION +
                         " energy grid with baseEnergy=" + String.format("%.0f", use_power));
        }
        me_tick++;
        if(me_cr>100){
            disables();
        }
        for (int r = 0; r < 3; r++){
            me_cr++;
            int sampleCount = (int) (SAMPLE_DENSITY * 4.0 * Math.PI * me_cr * me_cr);
            sampleCount = Math.max(sampleCount, 20);
            List<Vector> directions = generateFibonacciSphere(sampleCount);
            for (Vector direction : directions) {
                // Get world position
                Vector offset = direction.clone().multiply(me_cr);
                Location blockLoc = location.clone().add(offset);

                // === USE INTERPOLATION (mode: 4=bilinear, 9=smooth) ===
                int interpolationMode = 9;  // 4개 셀 사용 (빠름)

                // Get interpolated energy from surrounding cells
                double directionEnergy = getInterpolatedEnergy(direction, interpolationMode);
                if (directionEnergy <= 0) {
                    continue; // No energy left in this direction
                }

                // Try to break block
                float blockHardness = breakblock(blockLoc, (int) directionEnergy);

                // Reduce energy based on block resistance
                double energyLoss = Math.max(Math.pow(blockHardness,4.5), 0.001);

                // Apply energy loss to all weighted grid cells (smoothing!)
                applyInterpolatedEnergyLoss(direction, energyLoss, interpolationMode);

                // Visual particle (rare)
                if (Math.random() < 0.0002) {
                    Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 3F);
                    location.getWorld().spawnParticle(Particle.DUST, blockLoc, 1, 0.5, 0.5, 0.5, 0, dust, true);
                }
            }
            int zero_count=0;
            for(int rx=0; rx<ENERGY_RESOLUTION; rx++){
                for(int ry=0; ry<ENERGY_RESOLUTION; ry++){
                    if(energyGrid[rx][ry]<=0.03){
                        zero_count++;
                    }
                    energyGrid[rx][ry]*=0.99;
                }
            }

            if(zero_count>=ENERGY_RESOLUTION*ENERGY_RESOLUTION){
                disables();
                break;
            }
        }
    }
    public void aka(){
        if(target=='a'){
            if(murasaki){
                if(unlimit_m){
                    // Send START_EXPLODE packet when unlimit_m starts (only once)
                    if(time>1&&!murasakiEffectActive && user instanceof Player player) {
                        float initialRadius = 0.1f;  // Small initial radius
                        JPacketSender.sendInfinityMurasakiStartExplode(player, location, initialRadius, uniqueId);
                        murasakiEffectActive = true;
                    }

                    // Send SYNC_RADIUS every tick to update expanding radius
                    if(user instanceof Player player) {
                        // Calculate current tick of explosion (starts at 0, increases)
                        int tick = (int) ((10 + use_power / 10 - time - 1) * 3);
                        // Radius in blocks: r * 0.8 from murasaki_explode
                        // Each tick, r goes from 0 to (maxTime * 3)
                        float currentRadius = tick * 2.8f;  // Actual radius in blocks
                        JPacketSender.sendInfinityMurasakiSyncRadius(player, currentRadius, uniqueId);
                    }

                    murasaki_explode();
                }
                else{
                    // Normal murasaki - send START packet (only once)
                    if(time>1&&!murasakiEffectActive && user instanceof Player player) {
                        float strength = (float) (use_power * 0.049 + 0.051);  // Scale 1-100 to 0.1-5.0
                        JPacketSender.sendInfinityMurasakiStart(player, location, strength, uniqueId);
                        murasakiEffectActive = true;
                    }

                    // Normal murasaki - send SYNC packet every 10 ticks (0.5 seconds)
                    if(soundtick%4==0 && user instanceof Player player) {
                        float strength = (float) (use_power / 2.0 * 0.049 + 0.051);  // Scale 1-100 to 0.1-5.0
                        JPacketSender.sendInfinityMurasakiSync(player, location, strength, uniqueId);
                    }

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
                JPacketSender.sendInfinityAoEnd(player, uniqueId);
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
