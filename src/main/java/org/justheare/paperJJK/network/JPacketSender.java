package org.justheare.paperJJK.network;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * 서버 → 클라이언트 패킷 전송 유틸리티
 */
public class JPacketSender {
    private static final Logger logger = Logger.getLogger("PaperJJK-Network");
    private static final String CHANNEL = JPacketHandler.CHANNEL;

    /**
     * TECHNIQUE_USE (0x02) - 술식 사용 결과 전송
     */
    public static void sendTechniqueResult(Player player, boolean success, int techniqueId,
                                           byte reason, String message) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.TECHNIQUE_USE);
            out.writeBoolean(success);
            out.writeInt(techniqueId);
            out.writeByte(reason);
            out.writeUTF(message);

            player.sendPluginMessage(player.getServer().getPluginManager().getPlugin("PaperJJK"),
                    CHANNEL, out.toByteArray());

            logger.info(String.format("[패킷 전송] TECHNIQUE_USE → %s: success=%b, id=%d, reason=0x%02X, msg=%s",
                    player.getName(), success, techniqueId, reason, message));
        } catch (Exception e) {
            logger.severe(String.format("TECHNIQUE_USE 패킷 전송 실패 (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * DOMAIN_VISUAL (0x03) - 도메인 시각 효과 전송
     */
    public static void sendDomainVisual(Player player, byte action, int domainType,
                                        Location center, int radius, int colorRGB, int duration) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.DOMAIN_VISUAL);
            out.writeByte(action);
            out.writeInt(domainType);
            out.writeDouble(center.getX());
            out.writeDouble(center.getY());
            out.writeDouble(center.getZ());
            out.writeInt(radius);
            out.writeInt(colorRGB);
            out.writeInt(duration);

            player.sendPluginMessage(player.getServer().getPluginManager().getPlugin("PaperJJK"),
                    CHANNEL, out.toByteArray());

            logger.info(String.format("[패킷 전송] DOMAIN_VISUAL → %s: action=%d, type=%d, radius=%d",
                    player.getName(), action, domainType, radius));
        } catch (Exception e) {
            logger.severe(String.format("DOMAIN_VISUAL 패킷 전송 실패 (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * CE_UPDATE (0x04) - 주술력 정보 업데이트
     */
    public static void sendCEUpdate(Player player, int currentCE, int maxCE, float regenRate,
                                    String technique, boolean blocked) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.CE_UPDATE);
            out.writeInt(currentCE);
            out.writeInt(maxCE);
            out.writeFloat(regenRate);
            out.writeUTF(technique);
            out.writeBoolean(blocked);

            player.sendPluginMessage(player.getServer().getPluginManager().getPlugin("PaperJJK"),
                    CHANNEL, out.toByteArray());

            logger.fine(String.format("[패킷 전송] CE_UPDATE → %s: %d/%d, 술식=%s, blocked=%b",
                    player.getName(), currentCE, maxCE, technique, blocked));
        } catch (Exception e) {
            logger.severe(String.format("CE_UPDATE 패킷 전송 실패 (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * TECHNIQUE_COOLDOWN (0x05) - 쿨다운 정보
     */
    public static void sendTechniqueCooldown(Player player, byte techniqueSlot,
                                             int cooldownTicks, int maxCooldown) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.TECHNIQUE_COOLDOWN);
            out.writeByte(techniqueSlot);
            out.writeInt(cooldownTicks);
            out.writeInt(maxCooldown);

            player.sendPluginMessage(player.getServer().getPluginManager().getPlugin("PaperJJK"),
                    CHANNEL, out.toByteArray());

            logger.fine(String.format("[패킷 전송] TECHNIQUE_COOLDOWN → %s: slot=%d, %d/%d",
                    player.getName(), techniqueSlot, cooldownTicks, maxCooldown));
        } catch (Exception e) {
            logger.severe(String.format("TECHNIQUE_COOLDOWN 패킷 전송 실패 (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * PARTICLE_EFFECT (0x06) - 커스텀 파티클
     */
    public static void sendParticleEffect(Player player, byte effectType, Location location,
                                          float velocityX, float velocityY, float velocityZ,
                                          float scale, int colorRGB, int lifetime) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.PARTICLE_EFFECT);
            out.writeByte(effectType);
            out.writeDouble(location.getX());
            out.writeDouble(location.getY());
            out.writeDouble(location.getZ());
            out.writeFloat(velocityX);
            out.writeFloat(velocityY);
            out.writeFloat(velocityZ);
            out.writeFloat(scale);
            out.writeInt(colorRGB);
            out.writeInt(lifetime);

            player.sendPluginMessage(player.getServer().getPluginManager().getPlugin("PaperJJK"),
                    CHANNEL, out.toByteArray());

            logger.fine(String.format("[패킷 전송] PARTICLE_EFFECT → %s: type=%d, loc=(%.1f,%.1f,%.1f)",
                    player.getName(), effectType, location.getX(), location.getY(), location.getZ()));
        } catch (Exception e) {
            logger.severe(String.format("PARTICLE_EFFECT 패킷 전송 실패 (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * SCREEN_EFFECT (0x07) - 화면 효과
     */
    public static void sendScreenEffect(Player player, byte effectType, float intensity,
                                        int duration, byte[] additionalData) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.SCREEN_EFFECT);
            out.writeByte(effectType);
            out.writeFloat(intensity);
            out.writeInt(duration);
            out.writeInt(additionalData.length);
            out.write(additionalData);

            player.sendPluginMessage(player.getServer().getPluginManager().getPlugin("PaperJJK"),
                    CHANNEL, out.toByteArray());

            logger.info(String.format("[패킷 전송] SCREEN_EFFECT → %s: type=%d, intensity=%.2f, duration=%d",
                    player.getName(), effectType, intensity, duration));
        } catch (Exception e) {
            logger.severe(String.format("SCREEN_EFFECT 패킷 전송 실패 (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * HANDSHAKE (0x08) - 핸드셰이크 응답
     */
    public static void sendHandshake(Player player) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.HANDSHAKE);
            out.writeInt(1); // 프로토콜 버전
            out.writeUTF("PaperJJK-1.0.0"); // 플러그인 버전
            out.writeInt(0x07); // 지원 기능 (HUD + 파티클 + 셰이더)

            player.sendPluginMessage(player.getServer().getPluginManager().getPlugin("PaperJJK"),
                    CHANNEL, out.toByteArray());

            logger.info(String.format("[패킷 전송] HANDSHAKE → %s: 프로토콜=1, 버전=PaperJJK-1.0.0",
                    player.getName()));
        } catch (Exception e) {
            logger.severe(String.format("HANDSHAKE 패킷 전송 실패 (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }
}
