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
     * TECHNIQUE_FEEDBACK (0x10) - 술식 사용 결과 전송
     */
    public static void sendTechniqueResult(Player player, boolean success, int techniqueId,
                                           byte reason, String message) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.TECHNIQUE_FEEDBACK);
            out.writeBoolean(success);
            out.writeInt(techniqueId);
            out.writeByte(reason);
            out.writeUTF(message);

            player.sendPluginMessage(player.getServer().getPluginManager().getPlugin("PaperJJK"),
                    CHANNEL, out.toByteArray());

            logger.info(String.format("[Packet Send] TECHNIQUE_FEEDBACK → %s: success=%b, id=%d, reason=0x%02X, msg=%s",
                    player.getName(), success, techniqueId, reason, message));
        } catch (Exception e) {
            logger.severe(String.format("TECHNIQUE_FEEDBACK packet send failed (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * DOMAIN_VISUAL (0x11) - Domain visual effects
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

            logger.info(String.format("[Packet Send] DOMAIN_VISUAL → %s: action=%d, type=%d, radius=%d",
                    player.getName(), action, domainType, radius));
        } catch (Exception e) {
            logger.severe(String.format("DOMAIN_VISUAL packet send failed (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * CE_UPDATE (0x12) - Cursed energy update
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

            logger.fine(String.format("[Packet Send] CE_UPDATE → %s: %d/%d, technique=%s, blocked=%b",
                    player.getName(), currentCE, maxCE, technique, blocked));
        } catch (Exception e) {
            logger.severe(String.format("CE_UPDATE packet send failed (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * TECHNIQUE_COOLDOWN (0x13) - Cooldown info
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

            logger.fine(String.format("[Packet Send] TECHNIQUE_COOLDOWN → %s: slot=%d, %d/%d",
                    player.getName(), techniqueSlot, cooldownTicks, maxCooldown));
        } catch (Exception e) {
            logger.severe(String.format("TECHNIQUE_COOLDOWN packet send failed (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * PARTICLE_EFFECT (0x14) - Custom particles
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

            logger.fine(String.format("[Packet Send] PARTICLE_EFFECT → %s: type=%d, loc=(%.1f,%.1f,%.1f)",
                    player.getName(), effectType, location.getX(), location.getY(), location.getZ()));
        } catch (Exception e) {
            logger.severe(String.format("PARTICLE_EFFECT packet send failed (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * SCREEN_EFFECT (0x15) - Screen effects
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

            logger.info(String.format("[Packet Send] SCREEN_EFFECT → %s: type=%d, intensity=%.2f, duration=%d",
                    player.getName(), effectType, intensity, duration));
        } catch (Exception e) {
            logger.severe(String.format("SCREEN_EFFECT packet send failed (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }

    /**
     * HANDSHAKE (0x20) - Handshake response
     */
    public static void sendHandshake(Player player) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketIds.HANDSHAKE);
            out.writeInt(1); // Protocol version
            out.writeUTF("PaperJJK-1.0.0"); // Plugin version
            out.writeInt(0x07); // Supported features (HUD + Particles + Shaders)

            player.sendPluginMessage(player.getServer().getPluginManager().getPlugin("PaperJJK"),
                    CHANNEL, out.toByteArray());

            logger.info(String.format("[Packet Send] HANDSHAKE → %s: protocol=1, version=PaperJJK-1.0.0",
                    player.getName()));
        } catch (Exception e) {
            logger.severe(String.format("HANDSHAKE packet send failed (%s): %s",
                    player.getName(), e.getMessage()));
            e.printStackTrace();
        }
    }
}
