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
 * Plugin Messaging 패킷 수신 핸들러
 * paperjjk:main 채널로 클라이언트 모드와 통신
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
     * 패킷 수신 메인 처리
     */
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals(CHANNEL)) {
            return;
        }

        try {
            ByteArrayDataInput in = ByteStreams.newDataInput(message);
            byte packetId = in.readByte();

            logger.info(String.format("[패킷 수신] 플레이어: %s, 패킷 ID: 0x%02X, 크기: %d bytes",
                    player.getName(), packetId, message.length));

            switch (packetId) {
                case PacketIds.KEYBIND_PRESS -> handleKeybindPress(player, in);
                case PacketIds.HANDSHAKE -> handleHandshake(player, in);
                default -> logger.warning(String.format("알 수 없는 패킷 ID: 0x%02X (플레이어: %s)",
                        packetId, player.getName()));
            }
        } catch (Exception e) {
            logger.severe(String.format("패킷 처리 중 오류 발생 (플레이어: %s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * KEYBIND_PRESS (0x01) - 키 입력 처리
     */
    private void handleKeybindPress(Player player, ByteArrayDataInput in) {
        byte keyId = in.readByte();
        byte keyState = in.readByte();
        long timestamp = in.readLong();

        // 키 이름 변환 (디버깅용)
        String keyName = getKeyName(keyId);
        String stateName = getStateName(keyState);

        //logger.info(String.format("[키 입력] %s: %s (%s) - 타임스탬프: %d", player.getName(), keyName, stateName, timestamp));

        // Jobject 찾기
        Jplayer jplayer = null;
        for (Jobject obj : PaperJJK.jobjects) {
            if (obj instanceof Jplayer jp && jp.user.getUniqueId().equals(player.getUniqueId())) {
                jplayer = jp;
                break;
            }
        }

        if (jplayer == null) {
            logger.warning(String.format("Jplayer를 찾을 수 없음: %s", player.getName()));
            JPacketSender.sendTechniqueResult(player, false, 0,
                    PacketIds.FailureReason.BLOCKED, "플레이어 데이터를 찾을 수 없습니다");
            return;
        }

        // 키 상태에 따라 처리
        switch (keyState) {
            case PacketIds.KeyState.PRESSED -> {
                // 키 눌림 - KeyStateManager에 등록
                KeyStateManager.onKeyPress(player, keyId);
                //logger.fine(String.format("%s: %s PRESSED", player.getName(), keyName));

                // PRESSED 이벤트 실행 (즉발 술식 등)
                onKeyPressEvent(player, jplayer, keyId);
            }

            case PacketIds.KeyState.RELEASED -> {
                // 키 뗌 - KeyStateManager에서 지속시간 가져오기
                long heldDuration = KeyStateManager.onKeyRelease(player, keyId);
                //logger.fine(String.format("%s: %s RELEASED (지속: %dms)", player.getName(), keyName, heldDuration));

                // RELEASED 이벤트 실행 (차징 발사 등)
                onKeyReleaseEvent(player, jplayer, keyId, heldDuration);
            }

            default -> {
                logger.warning(String.format("알 수 없는 키 상태: %s (%d)", keyName, keyState));
            }
        }
    }

    /**
     * Key PRESSED event handler
     * Instant techniques, charging start, etc.
     */
    private void onKeyPressEvent(Player player, Jplayer jplayer, byte keyId) {
        switch (keyId) {
            case PacketIds.KeyBinds.BARRIER_TECHNIQUE -> {
                // G key: Barrier technique (charging start)
                //logger.info(String.format("[G PRESSED] %s: Barrier charging started", player.getName()));
            }
            case PacketIds.KeyBinds.RCT -> {
                // Z key: RCT (charging start)
                //logger.info(String.format("[Z PRESSED] %s: RCT started", player.getName()));
            }
            // Only handle PRESSED events for necessary keys
        }
    }

    /**
     * Key RELEASED event handler
     * Charged technique release, guard disable, etc.
     */
    private void onKeyReleaseEvent(Player player, Jplayer jplayer, byte keyId, long heldDuration) {
        switch (keyId) {
            case PacketIds.KeyBinds.DOMAIN_EXPANSION -> {
                // R key: Domain expansion (only if not long press)
                logger.info(String.format("[R RELEASED] %s: Duration %dms", player.getName(), heldDuration));
                handleDomainExpansion(player, jplayer);
            }
            case PacketIds.KeyBinds.BARRIER_TECHNIQUE -> {
                // G key: Barrier technique release
                //logger.info(String.format("[G RELEASED] %s: Barrier release (power: %dms)", player.getName(), heldDuration));
                //handleBarrierRelease(player, jplayer, heldDuration);
            }
            case PacketIds.KeyBinds.RCT -> {
                // Z key: RCT end
                //logger.info(String.format("[Z RELEASED] %s: RCT ended", player.getName()));
                //handleRCTRelease(player, jplayer, heldDuration);
            }
            case PacketIds.KeyBinds.TECHNIQUE_SLOT_1 -> {
                // X key: Technique Slot 1 release
                //logger.info(String.format("[X RELEASED] %s: Technique Slot 1 released (power: %dms)", player.getName(), heldDuration));
                //handleTechniqueSlot(player, jplayer, 1, heldDuration);
            }
            case PacketIds.KeyBinds.TECHNIQUE_SLOT_2 -> {
                // C key: Technique Slot 2 release
                //logger.info(String.format("[C RELEASED] %s: Technique Slot 2 released (power: %dms)", player.getName(), heldDuration));
                //handleTechniqueSlot(player, jplayer, 2, heldDuration);
            }
            case PacketIds.KeyBinds.TECHNIQUE_SLOT_3 -> {
                // V key: Technique Slot 3 release
                //logger.info(String.format("[V RELEASED] %s: Technique Slot 3 released (power: %dms)", player.getName(), heldDuration));
                //handleTechniqueSlot(player, jplayer, 3, heldDuration);
            }
            case PacketIds.KeyBinds.TECHNIQUE_SLOT_4 -> {
                // B key: Technique Slot 4 release
                //logger.info(String.format("[B RELEASED] %s: Technique Slot 4 released (power: %dms)", player.getName(), heldDuration));
                //handleTechniqueSlot(player, jplayer, 4, heldDuration);
            }
        }
    }

    // executeKeyAction 메서드 제거됨 (onKeyPressEvent, onKeyReleaseEvent로 대체)

    /**
     * R key - Domain Expansion
     */
    private void handleDomainExpansion(Player player, Jplayer jplayer) {
        if (jplayer.blocked) {
            logger.info(String.format("%s: Domain expansion blocked", player.getName()));
            JPacketSender.sendTechniqueResult(player, false, 100,
                    PacketIds.FailureReason.BLOCKED, "Technique is blocked");
            return;
        }

        if (jplayer.innate_domain != null) {
            logger.info(String.format("%s: Domain expansion activated", player.getName()));
            //jplayer.innate_domain.expand();
            player.sendMessage("§5Domain Expansion!");

            // Send domain visual packet (temporary)
            int domainType = jplayer.naturaltech.equals("MIZUSHI") ?
                    PacketIds.DomainType.MIZUSHI : PacketIds.DomainType.OTHER;
            JPacketSender.sendDomainVisual(player, PacketIds.DomainAction.CREATE,
                    domainType, player.getLocation(), 15, 0xFF6600, 600);
        } else {
            logger.warning(String.format("%s: No innate domain", player.getName()));
            JPacketSender.sendTechniqueResult(player, false, 100,
                    PacketIds.FailureReason.BLOCKED, "No innate domain configured");
        }
    }

    /**
     * G key - Barrier Technique (charged release)
     */
    private void handleBarrierRelease(Player player, Jplayer jplayer, long chargeDuration) {
        int power = (int) Math.min(chargeDuration / 50, 20); // Max 20 (1 second)

        //logger.info(String.format("%s: Barrier technique released (charge: %dms, power: %d)", player.getName(), chargeDuration, power));

        //player.sendMessage(String.format("§eBarrier released! §7(power: %d/20)", power));

        // TODO: Actual barrier technique logic
    }

    /**
     * Z key - Reverse Cursed Technique (RCT release)
     */
    private void handleRCTRelease(Player player, Jplayer jplayer, long heldDuration) {
        //logger.info(String.format("%s: RCT released (duration: %dms)", player.getName(), heldDuration));
        //player.sendMessage("§aRCT ended");

        // TODO: RCT logic
    }

    /**
     * X/C/V/B keys - Technique Slots (charged release)
     */
    private void handleTechniqueSlot(Player player, Jplayer jplayer, int slotNumber, long chargeDuration) {
        int power = (int) Math.min(chargeDuration / 50, 20); // Max 20 (1 second)

        //logger.info(String.format("%s: Technique Slot %d released (charge: %dms, power: %d)", player.getName(), slotNumber, chargeDuration, power));

        //player.sendMessage(String.format("§7Technique Slot %d released §7(power: %d/20)", slotNumber, power));

        // TODO: Technique slot logic
    }

    /**
     * HANDSHAKE (0x08) - Handshake
     */
    private void handleHandshake(Player player, ByteArrayDataInput in) {
        int version = in.readInt();
        String modVersion = in.readUTF();
        int features = in.readInt();

        logger.info(String.format("[Handshake] %s: Protocol=%d, Version=%s, Features=0x%02X",
                player.getName(), version, modVersion, features));

        // Send handshake response
        JPacketSender.sendHandshake(player);
    }

    // ===== Helper Methods =====

    private String getKeyName(byte keyId) {
        return switch (keyId) {
            case PacketIds.KeyBinds.DOMAIN_EXPANSION -> "R (Domain)";
            case PacketIds.KeyBinds.BARRIER_TECHNIQUE -> "G (Barrier)";
            case PacketIds.KeyBinds.RCT -> "Z (RCT)";
            case PacketIds.KeyBinds.TECHNIQUE_SLOT_1 -> "X (Slot1)";
            case PacketIds.KeyBinds.TECHNIQUE_SLOT_2 -> "C (Slot2)";
            case PacketIds.KeyBinds.TECHNIQUE_SLOT_3 -> "V (Slot3)";
            case PacketIds.KeyBinds.TECHNIQUE_SLOT_4 -> "B (Slot4)";
            default -> String.format("Unknown (0x%02X)", keyId);
        };
    }

    private String getStateName(byte keyState) {
        return switch (keyState) {
            case PacketIds.KeyState.RELEASED -> "RELEASED";
            case PacketIds.KeyState.PRESSED -> "PRESSED";
            case PacketIds.KeyState.HELD -> "HELD";
            default -> String.format("Unknown (%d)", keyState);
        };
    }
}
