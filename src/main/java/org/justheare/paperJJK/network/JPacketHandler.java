package org.justheare.paperJJK.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.justheare.paperJJK.Jobject;
import org.justheare.paperJJK.Jplayer;
import org.justheare.paperJJK.PaperJJK;

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

        switch (action) {
            case PacketIds.SkillAction.START -> {
                logger.info(String.format("[Technique START] %s: Slot %d charging", player.getName(), slot));
                // TODO: Start technique charging
            }
            case PacketIds.SkillAction.END -> {
                logger.info(String.format("[Technique CAST] %s: Slot %d released", player.getName(), slot));
                // TODO: Cast technique
            }
        }
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

        switch (action) {
            case PacketIds.SkillAction.START -> {
                logger.info(String.format("[Reverse Technique START] %s: Slot %d charging", player.getName(), slot));
                // TODO: Start reverse technique charging
            }
            case PacketIds.SkillAction.END -> {
                logger.info(String.format("[Reverse Technique CAST] %s: Slot %d released", player.getName(), slot));
                // TODO: Cast reverse technique
            }
        }
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

        logger.info(String.format("[Skill TERMINATE] %s: Terminating slot %d", player.getName(), slot));
        // TODO: Terminate active skill
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
