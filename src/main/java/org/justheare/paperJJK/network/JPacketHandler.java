package org.justheare.paperJJK.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
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
                case PacketIds.DOMAIN_SETTINGS -> handleDomainSettings(player, in);
                case PacketIds.PLAYER_INFO_REQUEST -> handlePlayerInfoRequest(player, in);
                case PacketIds.SKILL_INFO_REQUEST -> handleSkillInfoRequest(player, in);
                case PacketIds.SKILL_BINDING_UPDATE -> handleSkillBindingUpdate(player, in);
                case PacketIds.CLIENT_SETTINGS_UPDATE -> handleClientSettingsUpdate(player, in);
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
                // Check if player can use simple domain
                if (!jplayer.can_simple_domain) {
                    logger.warning(String.format("[Simple Domain] %s: Cannot use simple domain (can_simple_domain = false)", player.getName()));
                    return;
                }

                logger.info(String.format("[Simple Domain START] %s: Charging started", player.getName()));
                org.justheare.paperJJK.SimpleDomainManager.startCharging(player);
            }
            case PacketIds.SkillAction.END -> {
                logger.info(String.format("[Simple Domain END] %s: Charging complete", player.getName()));
                org.justheare.paperJJK.SimpleDomainManager.endCharging(player);
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

        // Validate skill matches player's natural technique
        if (!JujutFactory.canUseSkill(jplayer.naturaltech, skillId)) {
            player.sendMessage("§c이 스킬은 당신의 생득술식과 맞지 않습니다!");
            player.sendMessage("§7현재 설정된 스킬: §e" + skillId);
            player.sendMessage("§7당신의 생득술식: §e" + jplayer.naturaltech);
            player.sendMessage("§6/jjk config 명령어로 스킬을 재설정하세요.");
            logger.warning(String.format("[Technique] Skill mismatch: %s tried to use %s (naturaltech: %s)",
                player.getName(), skillId, jplayer.naturaltech));
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
                // Check if there's a CHARGING skill for this slot
                Jujut chargingJujut = findChargingJujutForSlot(jplayer, slot);
                if (chargingJujut != null) {
                    // Already charging this skill, ignore
                    logger.info(String.format("[Technique] %s: Slot %d (%s) already charging, ignoring",
                        player.getName(), slot, skillId));
                    return;
                }

                // Check for rechargeable skills (only one instance allowed)
                Jujut activeJujut = findActiveJujutForSlot(jplayer, slot);
                if (activeJujut != null && activeJujut.rechargeable) {
                    // Rechargeable skill exists - start recharge instead of creating new
                    if (activeJujut.canRecharge()) {
                        activeJujut.startRecharge();
                        logger.info(String.format("[Technique RECHARGE START] %s: Slot %d (%s) recharging",
                            player.getName(), slot, skillId));
                        return;
                    } else {
                        // Rechargeable but can't recharge right now (already charging)
                        logger.info(String.format("[Technique] %s: Slot %d (%s) cannot recharge right now",
                            player.getName(), slot, skillId));
                        return;
                    }
                }

                // For non-rechargeable skills (kai, aka, etc), always create new instance
                // This allows rapid-fire attacks
                Jujut jujut = JujutFactory.createJujut(skillId, jplayer, 100, 100);
                if (jujut != null) {
                    jplayer.jujuts.add(jujut);
                    jujut.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                        PaperJJK.jjkplugin, jujut, 1, 1);
                    logger.info(String.format("[Technique START] %s: Slot %d (%s) charging started (rapid-fire: %b)",
                        player.getName(), slot, skillId, !jujut.rechargeable));
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

        // Validate base skill matches player's natural technique
        if (!JujutFactory.canUseSkill(jplayer.naturaltech, baseSkillId)) {
            player.sendMessage("§c이 스킬은 당신의 생득술식과 맞지 않습니다!");
            player.sendMessage("§6/jjk config 명령어로 스킬을 재설정하세요.");
            logger.warning(String.format("[Reverse Technique] Skill mismatch: %s tried to reverse %s (naturaltech: %s)",
                player.getName(), baseSkillId, jplayer.naturaltech));
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
     * SKILL_CONTROL (0x06) - Control active skill (T + slot key)
     * Packet format: [packetId(1)] [action(1)] [slot(1)] [timestamp(8)]
     * Toggles skill lock (시선 따라가기 on/off)
     */
    private void handleSkillControl(Player player, ByteArrayDataInput in) {
        byte action = in.readByte();
        byte slot = in.readByte();
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Skill Control] Jplayer not found: %s", player.getName()));
            return;
        }

        String skillId = jplayer.getSlotSkill(slot);
        if (skillId == null) {
            logger.warning(String.format("[Skill Control] Invalid slot: %d (Player: %s)", slot, player.getName()));
            return;
        }

        // Find active skill for this slot
        Jujut jujut = findActiveJujutForSlot(jplayer, slot);
        if (jujut == null) {
            player.sendMessage("§c해당 슬롯에 활성화된 스킬이 없습니다!");
            logger.info(String.format("[Skill Control] No active skill found for slot %d (Player: %s)",
                slot, player.getName()));
            return;
        }

        // Check if skill is fixable
        if (!jujut.fixable) {
            player.sendMessage("§c이 스킬은 위치 고정을 지원하지 않습니다!");
            logger.info(String.format("[Skill Control] Skill %s is not fixable (Player: %s)",
                skillId, player.getName()));
            return;
        }

        // Toggle fixed state
        jujut.fixed = !jujut.fixed;
        String state = jujut.fixed ? "§e고정됨 (시선 따라가기 OFF)" : "§a해제됨 (시선 따라가기 ON)";
        player.sendMessage("§6[" + skillId + "] " + state);
        logger.info(String.format("[Skill CONTROL] %s: %s fixed=%b",
            player.getName(), skillId, jujut.fixed));
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

        String skillId = jplayer.getSlotSkill(slot);
        if (skillId == null) {
            logger.warning(String.format("[Skill Distance] Invalid slot: %d (Player: %s)", slot, player.getName()));
            return;
        }

        // Find the charging skill for this slot
        Jujut jujut = findChargingJujutForSlot(jplayer, slot);
        if (jujut == null) {
            // If not charging, try to find active skill
            jujut = findActiveJujutForSlot(jplayer, slot);
        }

        if (jujut == null) {
            logger.info(String.format("[Skill Distance] No active/charging skill found for slot %d (Player: %s)",
                slot, player.getName()));
            return;
        }

        // Adjust distance based on skill type
        if (jujut instanceof Infinity) {
            adjustInfinityDistance((Infinity) jujut, scrollDelta, skillId);
            String direction = scrollDelta > 0 ? "increased" : "decreased";
            logger.info(String.format("[Skill DISTANCE] %s: %s distance %s to %.1f",
                player.getName(), skillId, direction, ((Infinity) jujut).distance));
        }
        else {
            logger.info(String.format("[Skill Distance] Skill %s does not support distance adjustment",
                skillId));
        }
    }

    /**
     * Adjust Infinity skill distance/direction based on scroll
     */
    private void adjustInfinityDistance(Infinity infinity, byte scrollDelta, String skillId) {
        if (skillId.equals("infinity_ao")) {
            // ao: adjust distance (1 to 20 blocks)
            double step = 0.5;
            infinity.distance += scrollDelta * step;

            // Clamp distance
            if (infinity.distance < 1) infinity.distance = 1;
            if (infinity.distance > 20) infinity.distance = 20;
        } else if (skillId.equals("infinity_aka")) {
            // aka: toggle direction (1 for forward, -1 for backward)
            // Use distance as direction multiplier
            if (scrollDelta != 0) {
                infinity.distance = infinity.distance > 0 ? -1 : 1;
            }
        }
    }

    /**
     * DOMAIN_EXPANSION (0x08) - Domain expansion start/end
     * Packet format: [packetId(1)] [action(1)] [flags(1)] [timestamp(8)]
     *
     * Client sends:
     * - R press: START + NORMAL -> Normal domain expansion (ed build, range 30)
     * - G + R press: START + NO_BARRIER -> No-barrier domain expansion (nb build, range 50)
     * - Shift + R press: END + any flags -> Cancel domain (ed/nb destroy)
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

        // New simplified logic:
        // - R (START + NORMAL): Normal domain expansion (ed build)
        // - G + R (START + NO_BARRIER): No-barrier domain expansion (nb build)
        // - Shift + R (END + any flags): Cancel domain (ed/nb destroy)

        switch (action) {
            case PacketIds.SkillAction.START -> {
                logger.info(String.format("[Domain START] %s: %s", player.getName(),
                    noBarrier ? "No Barrier Domain (G + R)" : "Normal Domain (R)"));

                if (jplayer.innate_domain == null) {
                    player.sendMessage("§c생득영역이 없습니다!");
                    return;
                }

                if (noBarrier) {
                    // G + R: nb build - No Barrier Domain (무변부여)
                    int range = jplayer.noBarrierDomainRange;  // Use player's configured range
                    boolean success = jplayer.innate_domain.drow_expand(range);
                    if (success) {
                        logger.info(String.format("[Domain] No-barrier domain expanded (range: %d)", range));
                    } else {
                    }
                } else {
                    // R: ed build - Normal Domain with Barrier
                    int range = jplayer.normalDomainRange;  // Use player's configured range
                    boolean success = jplayer.innate_domain.build_expand(range);
                    if (success) {
                        logger.info(String.format("[Domain] Normal domain expanded (range: %d)", range));
                    }
                }
            }
            case PacketIds.SkillAction.END -> {
                // Shift + R: Cancel domain regardless of current state
                logger.info(String.format("[Domain END] %s: Domain cancel requested (Shift + R)", player.getName()));

                if (jplayer.innate_domain == null) {
                    //player.sendMessage("§c생득영역이 없습니다!");
                    return;
                }

                // Check if domain is expanded and destroy accordingly
                if (jplayer.innate_domain.isexpanded) {
                    if (jplayer.innate_domain.no_border_on) {
                        // nb destroy - Undraw no-barrier domain
                        boolean success = jplayer.innate_domain.undrow_expand();
                        if (success) {
                            player.sendMessage("§7결계가 없는 영역전개 해제");
                            logger.info("[Domain] No-barrier domain destroyed");
                        }
                    } else {
                        // ed destroy - Destroy normal domain
                        boolean success = jplayer.innate_domain.destroy_expand();
                        if (success) {
                            player.sendMessage("§7영역전개 해제");
                            logger.info("[Domain] Normal domain destroyed");
                        }
                    }
                } else {
                    player.sendMessage("§c영역이 전개되어 있지 않습니다!");
                    logger.info(String.format("[Domain] %s tried to cancel domain but none is expanded", player.getName()));
                }
            }
        }
    }

    /**
     * DOMAIN_SETTINGS (0x0A) - Domain settings request/update
     * Packet format:
     * - REQUEST: [packetId(1)] [action(1)] [timestamp(8)]
     * - UPDATE: [packetId(1)] [action(1)] [normalRange(4)] [noBarrierRange(4)] [timestamp(8)]
     */
    private void handleDomainSettings(Player player, ByteArrayDataInput in) {
        byte action = in.readByte();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[Domain Settings] Jplayer not found: %s", player.getName()));
            return;
        }

        switch (action) {
            case PacketIds.DomainSettingsAction.REQUEST -> {
                // Client requests current settings
                long timestamp = in.readLong();
                logger.info(String.format("[Domain Settings] %s requested settings", player.getName()));
                sendDomainSettingsResponse(player, jplayer);
            }
            case PacketIds.DomainSettingsAction.UPDATE -> {
                // Client sends new settings
                int normalRange = in.readInt();
                int noBarrierRange = in.readInt();
                long timestamp = in.readLong();

                // Validate ranges
                if (normalRange < 5 || normalRange > 50) {
                    player.sendMessage("§c일반 영역전개 범위는 5~50 사이여야 합니다!");
                    logger.warning(String.format("[Domain Settings] %s sent invalid normal range: %d",
                        player.getName(), normalRange));
                    return;
                }
                if (noBarrierRange < 5 || noBarrierRange > 200) {
                    player.sendMessage("§c결계가 없는 영역전개 범위는 5~200 사이여야 합니다!");
                    logger.warning(String.format("[Domain Settings] %s sent invalid no-barrier range: %d",
                        player.getName(), noBarrierRange));
                    return;
                }

                // Update settings
                jplayer.normalDomainRange = normalRange;
                jplayer.noBarrierDomainRange = noBarrierRange;
                JData.saveJobject(jplayer);

                player.sendMessage("§a영역전개 설정이 저장되었습니다!");
                player.sendMessage("§7일반 영역전개 범위: §e" + normalRange + " §7/ 결계가 없는 영역전개 범위: §e" + noBarrierRange);
                logger.info(String.format("[Domain Settings] %s updated settings: normal=%d, noBarrier=%d",
                    player.getName(), normalRange, noBarrierRange));

                // Send confirmation response
                sendDomainSettingsResponse(player, jplayer);
            }
        }
    }

    /**
     * Send domain settings response to client
     */
    private void sendDomainSettingsResponse(Player player, Jplayer jplayer) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.DOMAIN_SETTINGS_RESPONSE);
            out.writeInt(jplayer.normalDomainRange);
            out.writeInt(jplayer.noBarrierDomainRange);
            out.writeLong(System.currentTimeMillis());

            player.sendPluginMessage(plugin, "paperjjk:main", out.toByteArray());
            logger.info(String.format("[Domain Settings] Sent response to %s: normal=%d, noBarrier=%d",
                player.getName(), jplayer.normalDomainRange, jplayer.noBarrierDomainRange));
        } catch (Exception e) {
            logger.severe(String.format("[Domain Settings] Failed to send response to %s: %s",
                player.getName(), e.getMessage()));
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

    /**
     * PLAYER_INFO_REQUEST (0x0B) - Request player info
     * Packet format: [packetId(1)] [timestamp(8)]
     */
    private void handlePlayerInfoRequest(Player player, ByteArrayDataInput in) {
        long timestamp = in.readLong();
        Jplayer jplayer = getJplayer(player);

        if (jplayer == null) {
            logger.warning(String.format("[PlayerInfo] Jplayer not found: %s", player.getName()));
            return;
        }

        logger.info(String.format("[PlayerInfo] Request from %s", player.getName()));

        // Send response packet
        JPacketSender.sendPlayerInfoResponse(player, jplayer);
    }

    /**
     * SKILL_INFO_REQUEST (0x0C) - Request skill description
     * Packet format: [packetId(1)] [skillId(VarInt+UTF8)] [timestamp(8)]
     */
    private void handleSkillInfoRequest(Player player, ByteArrayDataInput in) {
        String skillId = readMinecraftString(in);
        long timestamp = in.readLong();

        logger.info(String.format("[SkillInfo] Request from %s: %s", player.getName(), skillId));

        SkillDescriptionManager.SkillInfo skillInfo = SkillDescriptionManager.getSkillInfo(skillId);

        if (skillInfo == null) {
            logger.warning(String.format("[SkillInfo] Skill not found: %s", skillId));
            // Send empty response
            skillInfo = new SkillDescriptionManager.SkillInfo(
                skillId, "???", "설명을 찾을 수 없습니다.",  "unknown"
            );
        }

        // Send response packet
        JPacketSender.sendSkillInfoResponse(player, skillInfo);
    }

    /**
     * SKILL_BINDING_UPDATE (0x0D) - Update skill slot binding
     * Packet format: [packetId(1)] [slot(1)] [skillId(VarInt+UTF8)] [timestamp(8)]
     */
    private void handleSkillBindingUpdate(Player player, ByteArrayDataInput in) {
        byte slot = in.readByte();
        String skillId = readMinecraftString(in);
        long timestamp = in.readLong();

        Jplayer jplayer = getJplayer(player);
        if (jplayer == null) {
            logger.warning(String.format("[SkillBinding] Jplayer not found: %s", player.getName()));
            return;
        }

        logger.info(String.format("[SkillBinding] Update from %s: slot %d = %s",
            player.getName(), slot, skillId));

        // Update skill slot
        if (!jplayer.setSlotSkill(slot, skillId)) {
            logger.warning(String.format("[SkillBinding] Failed to set slot %d to %s", slot, skillId));
            return;
        }

        // Save to file
        JData.saveJobject(jplayer);

        logger.info(String.format("[SkillBinding] Successfully updated slot %d", slot));
    }

    /**
     * CLIENT_SETTINGS_UPDATE (0x0E) - Update client settings
     * Packet format: [packetId(1)] [postProcessing(1)] [domainEffects(1)] [timestamp(8)]
     */
    private void handleClientSettingsUpdate(Player player, ByteArrayDataInput in) {
        boolean postProcessing = in.readBoolean();
        boolean domainEffects = in.readBoolean();
        long timestamp = in.readLong();

        logger.info(String.format("[ClientSettings] Update from %s: postProcessing=%s, domainEffects=%s",
            player.getName(), postProcessing, domainEffects));

        // Client settings are stored client-side, no server action needed
        // This handler exists for future server-side logic if needed
    }

    // ========== Skill Logic Handlers ==========

    private void onRCTStart(Player player, Jplayer jplayer) {
        jplayer.set_reversecursing(true);
    }

    private void onRCTEnd(Player player, Jplayer jplayer) {
        jplayer.set_reversecursing(false);
    }

    // ========== Utility Methods ==========

    /**
     * Read VarInt from ByteArrayDataInput (Minecraft protocol format)
     */
    private int readVarInt(ByteArrayDataInput in) {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0) break;

            position += 7;

            if (position >= 32) {
                throw new RuntimeException("VarInt is too big");
            }
        }

        return value;
    }

    /**
     * Read String from ByteArrayDataInput (Minecraft protocol format: VarInt length + UTF-8 bytes)
     */
    private String readMinecraftString(ByteArrayDataInput in) {
        int length = readVarInt(in);
        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {
            bytes[i] = in.readByte();
        }

        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }
}
