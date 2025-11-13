package org.justheare.paperJJK.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.justheare.paperJJK.*;

import java.util.logging.Logger;

/**
 * Plugin Messaging packet handler
 * Communicates with client mod via paperjjk:main channel
 *
 * New design: Client sends pre-processed skill packets instead of raw key inputs
 */
public class JPacketHandler implements PluginMessageListener {
    private final Plugin plugin;
    private final Logger logger;
    public static final String CHANNEL = "paperjjk:main";

    public JPacketHandler(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    /**
     * Main packet reception handler
     */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals(CHANNEL)) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            byte packetId = in.readByte();

            logger.info(String.format("[Packet Received] Player: %s, Packet ID: 0x%02X, Size: %d bytes",
                    player.getName(), packetId, message.length));

            // Route to appropriate handler based on packet ID
            switch (packetId) {
                case PacketIds.SKILL_RCT -> handleRCTSkill(player, in);
                case PacketIds.SKILL_SIMPLE_DOMAIN -> handleSimpleDomainSkill(player, in);
                case PacketIds.SKILL_TECHNIQUE -> handleTechniqueSkill(player, in);
                case PacketIds.SKILL_REVERSE_TECHNIQUE -> handleReverseTechniqueSkill(player, in);
                case PacketIds.SKILL_TERMINATE -> handleSkillTerminate(player, in);
                case PacketIds.SKILL_CONTROL -> handleSkillControl(player, in);
                case PacketIds.SKILL_CONFIG -> handleSkillConfig(player, in);
                case PacketIds.SKILL_DISTANCE -> handleSkillDistance(player, in);
                case PacketIds.DOMAIN_EXPANSION -> handleDomainExpansion(player, in);
                case PacketIds.HANDSHAKE -> handleHandshake(player, in);
                default -> logger.warning(String.format("Unknown packet ID: 0x%02X (Player: %s)",
                        packetId, player.getName()));
            }
        } catch (Exception e) {
            logger.severe(String.format("Error processing packet (Player: %s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * Get Jplayer from Player UUID
     */
    private Jplayer getJplayer(Player player) {
        for (Jobject obj : PaperJJK.jobjects) {
            if (obj instanceof Jplayer jp && jp.user.getUniqueId().equals(player.getUniqueId())) {
                return jp;
            }
        }
        return null;
    }

    // ========== Skill Packet Handlers ==========

    /**
     * SKILL_RCT (0x01) - RCT healing start/end
     * Packet format: [packetId(1)] [action(1)] [slot(1)] [timestamp(8)]
     */
    private void handleRCTSkill(Player player, ByteArrayDataInput in) {
        byte action = in.readByte();
        byte slot = in.readByte(); // Always 0 for RCT
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[RCT] Jplayer not found: %s", player.getName()));
            return;
        }

        switch (action) {
            case PacketIds.SkillAction.START -> {
                logger.info(String.format("[RCT START] %s: Healing started", player.getName()));
                onRCTStart(player, jplayer);
            }
            case PacketIds.SkillAction.END -> {
                logger.info(String.format("[RCT END] %s: Healing stopped", player.getName()));
                onRCTEnd(player, jplayer);
            }
        }
    }

    /**
     * SKILL_SIMPLE_DOMAIN (0x02) - Simple Domain charge start/end
     * Packet format: [packetId(1)] [action(1)] [slot(1)] [timestamp(8)]
     */
    private void handleSimpleDomainSkill(Player player, ByteArrayDataInput in) {
        byte action = in.readByte();
        byte slot = in.readByte(); // Always 0 for Simple Domain
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Simple Domain] Jplayer not found: %s", player.getName()));
            return;
        }

        switch (action) {
            case PacketIds.SkillAction.START -> {
                logger.info(String.format("[Simple Domain START] %s: Charging started", player.getName()));
                // TODO: Start simple domain charging logic
            }
            case PacketIds.SkillAction.END -> {
                logger.info(String.format("[Simple Domain END] %s: Charging complete", player.getName()));
                // TODO: Complete simple domain
            }
        }
    }

    /**
     * SKILL_TECHNIQUE (0x03) - Technique slot charge/cast
     * Packet format: [packetId(1)] [action(1)] [slot(1)] [timestamp(8)]
     */
    private void handleTechniqueSkill(Player player, ByteArrayDataInput in) {
        byte action = in.readByte();
        byte slot = in.readByte();
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Technique] Jplayer not found: %s", player.getName()));
            return;
        }

        String skillId = jplayer.getSlotSkill(slot);
        if (skillId == null) {
            logger.warning(String.format("[Technique] Invalid slot: %d (Player: %s)", slot, player.getName()));
            return;
        }

        // DEBUG: Print all slot configurations
        logger.info(String.format("[DEBUG] Player %s slot config: 1=%s, 2=%s, 3=%s, 4=%s",
            player.getName(),
            jplayer.slot1Skill,
            jplayer.slot2Skill,
            jplayer.slot3Skill,
            jplayer.slot4Skill));

        switch (action) {
            case PacketIds.SkillAction.START -> {
                // Check if there's ANY existing skill for this slot (charging or active)
                Jujut existingJujut = findAnyJujutForSlot(jplayer, slot);

                if (existingJujut != null) {
                    // Skill already exists for this slot
                    if (existingJujut.charging) {
                        // Already charging, ignore
                        logger.info(String.format("[Technique] %s: Slot %d (%s) already charging, ignoring",
                            player.getName(), slot, skillId));
                        return;
                    } else if (existingJujut.canRecharge()) {
                        // Active and rechargeable, start recharge
                        existingJujut.startRecharge();
                        logger.info(String.format("[Technique RECHARGE START] %s: Slot %d (%s) recharging",
                            player.getName(), slot, skillId));
                        return;
                    } else {
                        // Active but not rechargeable, ignore (don't create new skill)
                        logger.info(String.format("[Technique] %s: Slot %d (%s) already active and not rechargeable, ignoring",
                            player.getName(), slot, skillId));
                        return;
                    }
                }

                // No existing skill, create new one
                Jujut jujut = JujutFactory.createJujut(skillId, jplayer, 100, 100);
                if (jujut != null) {
                    jplayer.jujuts.add(jujut);
                    jujut.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                        PaperJJK.jjkplugin, jujut, 1, 1);
                    logger.info(String.format("[Technique START] %s: Slot %d (%s) charging started",
                        player.getName(), slot, skillId));
                } else {
                    logger.warning(String.format("[Technique] Failed to create skill: %s (Player: %s)",
                        skillId, player.getName()));
                }
            }
            case PacketIds.SkillAction.END -> {
                // Stop charging and cast/activate the skill
                Jujut jujut = findChargingJujutForSlot(jplayer, slot);
                if (jujut != null) {
                    jujut.stopCharging();
                    logger.info(String.format("[Technique CAST] %s: Slot %d (%s) activated/recharged",
                        player.getName(), slot, skillId));
                } else {
                    logger.info(String.format("[Technique END] %s: No charging skill found for slot %d",
                        player.getName(), slot));
                }
            }
        }
    }

    /**
     * Find ANY Jujut (charging or active) for a specific slot
     */
    private Jujut findAnyJujutForSlot(Jplayer jplayer, byte slot) {
        String skillId = jplayer.getSlotSkill(slot);
        if (skillId == null) return null;

        for (Jujut jujut : jplayer.jujuts) {
            if (jujut.skillId.equals(skillId)) {
                return jujut;
            }
        }
        return null;
    }

    /**
     * Find active (not charging) rechargeable Jujut for a specific slot
     */
    private Jujut findActiveJujutForSlot(Jplayer jplayer, byte slot) {
        String skillId = jplayer.getSlotSkill(slot);
        if (skillId == null) return null;

        for (Jujut jujut : jplayer.jujuts) {
            if (jujut.skillId.equals(skillId) && jujut.active && !jujut.charging) {
                return jujut;
            }
        }
        return null;
    }

    /**
     * Find currently charging Jujut for a specific slot
     */
    private Jujut findChargingJujutForSlot(Jplayer jplayer, byte slot) {
        String skillId = jplayer.getSlotSkill(slot);
        if (skillId == null) return null;

        for (Jujut jujut : jplayer.jujuts) {
            if (jujut.skillId.equals(skillId) && jujut.charging) {
                return jujut;
            }
        }
        return null;
    }

    /**
     * SKILL_REVERSE_TECHNIQUE (0x04) - Reverse technique (Z + slot) charge/cast
     * Packet format: [packetId(1)] [action(1)] [slot(1)] [timestamp(8)]
     */
    private void handleReverseTechniqueSkill(Player player, ByteArrayDataInput in) {
        byte action = in.readByte();
        byte slot = in.readByte();
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Reverse Technique] Jplayer not found: %s", player.getName()));
            return;
        }

        // Get the base skill and find its reverse
        String baseSkillId = jplayer.getSlotSkill(slot);
        if (baseSkillId == null) {
            logger.warning(String.format("[Reverse Technique] Invalid slot: %d (Player: %s)", slot, player.getName()));
            return;
        }

        String reverseSkillId = JujutFactory.getReverseSkillId(baseSkillId);
        if (reverseSkillId == null) {
            logger.warning(String.format("[Reverse Technique] Skill %s has no reverse (Player: %s, Slot: %d)",
                baseSkillId, player.getName(), slot));
            player.sendMessage("§cThis skill cannot be reversed!");
            return;
        }

        switch (action) {
            case PacketIds.SkillAction.START -> {
                // Create reverse skill (aka cannot be recharged)
                Jujut jujut = JujutFactory.createJujut(reverseSkillId, jplayer, 100, 100);
                if (jujut != null) {
                    jplayer.jujuts.add(jujut);
                    jujut.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                        PaperJJK.jjkplugin, jujut, 1, 1);
                    logger.info(String.format("[Reverse Technique START] %s: Slot %d (%s -> %s) charging",
                        player.getName(), slot, baseSkillId, reverseSkillId));
                } else {
                    logger.warning(String.format("[Reverse Technique] Failed to create skill: %s (Player: %s)",
                        reverseSkillId, player.getName()));
                }
            }
            case PacketIds.SkillAction.END -> {
                // Stop charging and cast
                Jujut jujut = findChargingReverseTechnique(jplayer, reverseSkillId);
                if (jujut != null) {
                    logger.info(String.format("[Reverse Technique CAST] %s: Slot %d (%s) released",
                        player.getName(), slot, reverseSkillId));
                    // maintick() will handle the charging=false transition
                }
            }
        }
    }

    /**
     * Find currently charging reverse technique Jujut
     */
    private Jujut findChargingReverseTechnique(Jplayer jplayer, String reverseSkillId) {
        for (Jujut jujut : jplayer.jujuts) {
            if (jujut.skillId.equals(reverseSkillId) && jujut.charging) {
                return jujut;
            }
        }
        return null;
    }

    /**
     * SKILL_TERMINATE (0x05) - Terminate active skill
     * Packet format: [packetId(1)] [slot(1)] [timestamp(8)]
     */
    private void handleSkillTerminate(Player player, ByteArrayDataInput in) {
        byte slot = in.readByte();
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Skill Terminate] Jplayer not found: %s", player.getName()));
            return;
        }

        String skillId = jplayer.getSlotSkill(slot);
        if (skillId == null) {
            logger.warning(String.format("[Skill Terminate] Invalid slot: %d (Player: %s)", slot, player.getName()));
            return;
        }

        // Find and disable all active/charging skills for this slot
        boolean terminated = false;
        for (int i = jplayer.jujuts.size() - 1; i >= 0; i--) {
            Jujut jujut = jplayer.jujuts.get(i);
            if (jujut.skillId.equals(skillId)) {
                jujut.disables();
                terminated = true;
                logger.info(String.format("[Skill TERMINATE] %s: Terminated %s (Slot %d)",
                    player.getName(), skillId, slot));
            }
        }

        if (!terminated) {
            logger.info(String.format("[Skill TERMINATE] %s: No active skill found for slot %d (%s)",
                player.getName(), slot, skillId));
        }
    }

    /**
     * SKILL_CONTROL (0x06) - Control active skill
     * Packet format: [packetId(1)] [slot(1)] [controlData...] [timestamp(8)]
     */
    private void handleSkillControl(Player player, ByteArrayDataInput in) {
        byte slot = in.readByte();
        // TODO: Read control data (depends on implementation)
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Skill Control] Jplayer not found: %s", player.getName()));
            return;
        }

        logger.info(String.format("[Skill CONTROL] %s: Controlling slot %d", player.getName(), slot));
        // TODO: Control active skill
    }

    /**
     * SKILL_CONFIG (0x07) - Configure skill settings
     * Packet format: [packetId(1)] [slot(1)] [configData...] [timestamp(8)]
     */
    private void handleSkillConfig(Player player, ByteArrayDataInput in) {
        byte slot = in.readByte();
        // TODO: Read config data (depends on implementation)
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Skill Config] Jplayer not found: %s", player.getName()));
            return;
        }

        logger.info(String.format("[Skill CONFIG] %s: Configuring slot %d", player.getName(), slot));
        // TODO: Save skill configuration
    }

    /**
     * SKILL_DISTANCE (0x09) - Adjust skill spawn distance
     * Packet format: [packetId(1)] [slot(1)] [scrollDelta(1)] [timestamp(8)]
     * scrollDelta: positive for scroll up (increase), negative for scroll down (decrease)
     */
    private void handleSkillDistance(Player player, ByteArrayDataInput in) {
        byte slot = in.readByte();
        byte scrollDelta = in.readByte(); // +1 for up, -1 for down
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Skill Distance] Jplayer not found: %s", player.getName()));
            return;
        }

        String direction = scrollDelta > 0 ? "increased" : "decreased";
        logger.info(String.format("[Skill DISTANCE] %s: Slot %d scroll %s (delta: %d)",
            player.getName(), slot, direction, scrollDelta));
        // TODO: Update skill spawn distance for the player's active skill based on scroll delta
        // Each skill can have its own distance range and step size
    }

    /**
     * DOMAIN_EXPANSION (0x08) - Domain expansion start/end
     * Packet format: [packetId(1)] [action(1)] [flags(1)] [timestamp(8)]
     */
    private void handleDomainExpansion(Player player, ByteArrayDataInput in) {
        byte action = in.readByte();
        byte flags = in.readByte();
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Domain Expansion] Jplayer not found: %s", player.getName()));
            return;
        }

        boolean noBarrier = (flags & PacketIds.DomainFlags.NO_BARRIER) != 0;

        switch (action) {
            case PacketIds.SkillAction.START -> {
                logger.info(String.format("[Domain START] %s: %s", player.getName(),
                    noBarrier ? "No Barrier Domain" : "Normal Domain"));
                // TODO: Start domain expansion
            }
            case PacketIds.SkillAction.END -> {
                logger.info(String.format("[Domain END] %s: Domain cancelled", player.getName()));
                // TODO: End domain expansion
            }
        }
    }

    /**
     * HANDSHAKE (0x20) - Client-server handshake
     */
    private void handleHandshake(Player player, ByteArrayDataInput in) {
        try {
            String modVersion = in.readUTF();
            logger.info(String.format("[Handshake] Player: %s, Mod Version: %s",
                player.getName(), modVersion));
            // TODO: Version check and response
        } catch (Exception e) {
            logger.warning(String.format("[Handshake] Failed to read version from %s", player.getName()));
        }
    }

    // ========== Skill Logic Handlers ==========

    private void onRCTStart(Player player, Jplayer jplayer) {
        jplayer.set_reversecursing(true);
    }

    private void onRCTEnd(Player player, Jplayer jplayer) {
        jplayer.set_reversecursing(false);
    }
}
