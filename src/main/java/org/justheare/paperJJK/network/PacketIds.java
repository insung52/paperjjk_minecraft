package org.justheare.paperJJK.network;

/**
 * Plugin Messaging 패킷 ID 정의
 * CLIENT_MOD_SPECIFICATION.md 3.2절 참조
 *
 * 클라이언트 모드와 동일한 ID 사용
 */
public class PacketIds {
    // Client → Server
    public static final byte KEYBIND_PRESS = 0x01;

    // Server → Client
    public static final byte TECHNIQUE_USE = 0x02;
    public static final byte DOMAIN_VISUAL = 0x03;
    public static final byte CE_UPDATE = 0x04;
    public static final byte TECHNIQUE_COOLDOWN = 0x05;
    public static final byte PARTICLE_EFFECT = 0x06;
    public static final byte SCREEN_EFFECT = 0x07;

    // Bidirectional
    public static final byte HANDSHAKE = 0x08;

    /**
     * Keybind Key IDs (keyId field in KEYBIND_PRESS packet)
     */
    public static class KeyBinds {
        public static final byte DOMAIN_EXPANSION = 0x01;  // R - Domain Expansion
        public static final byte BARRIER_TECHNIQUE = 0x02;  // G - Barrier Technique
        public static final byte RCT = 0x03;  // Z - Reverse Cursed Technique (RCT)
        public static final byte TECHNIQUE_SLOT_1 = 0x04;  // X - Technique Slot 1
        public static final byte TECHNIQUE_SLOT_2 = 0x05;  // C - Technique Slot 2
        public static final byte TECHNIQUE_SLOT_3 = 0x10;  // V - Technique Slot 3
        public static final byte TECHNIQUE_SLOT_4 = 0x11;  // B - Technique Slot 4
    }

    /**
     * Key State (keyState field in KEYBIND_PRESS packet)
     */
    public static class KeyState {
        public static final byte RELEASED = 0;
        public static final byte PRESSED = 1;
        public static final byte HELD = 2;
    }

    /**
     * Technique Failure Reason (reason field in TECHNIQUE_USE packet)
     */
    public static class FailureReason {
        public static final byte SUCCESS = 0x00;
        public static final byte INSUFFICIENT_CE = 0x01;  // Insufficient cursed energy
        public static final byte ON_COOLDOWN = 0x02;  // On cooldown
        public static final byte BLOCKED = 0x03;  // Blocked state
    }

    /**
     * Domain Action Type (action field in DOMAIN_VISUAL packet)
     */
    public static class DomainAction {
        public static final byte CREATE = 0x01;  // Start creation animation
        public static final byte COMPLETE = 0x02;  // Complete (show barrier)
        public static final byte DESTROY = 0x03;  // Destruction animation
    }

    /**
     * Domain Type (domainType field in DOMAIN_VISUAL packet)
     */
    public static class DomainType {
        public static final int MIZUSHI = 1;  // Malevolent Shrine (Fuga)
        public static final int INFINITY = 2;  // Unlimited Void
        public static final int OTHER = 3;  // Other innate domains
    }
}
