package org.justheare.paperJJK.network;

/**
 * Plugin Messaging 패킷 ID 정의
 * CLIENT_MOD_SPECIFICATION.md 3.2절 참조
 *
 * 클라이언트 모드와 동일한 ID 사용
 */
public class PacketIds {
    // Client → Server: Skill Usage Packets
    public static final byte SKILL_RCT = 0x01;              // RCT healing start/end
    public static final byte SKILL_SIMPLE_DOMAIN = 0x02;    // Simple Domain charge start/end
    public static final byte SKILL_TECHNIQUE = 0x03;        // Technique slot 1-4 charge/cast
    public static final byte SKILL_REVERSE_TECHNIQUE = 0x04; // Reverse technique (Z + slot) charge/cast
    public static final byte SKILL_TERMINATE = 0x05;        // Terminate active skill
    public static final byte SKILL_CONTROL = 0x06;          // Control active skill
    public static final byte SKILL_CONFIG = 0x07;           // Configure skill settings
    public static final byte SKILL_DISTANCE = 0x09;         // Adjust skill spawn distance (scroll)
    public static final byte DOMAIN_EXPANSION = 0x08;       // Domain expansion start/end

    // Server → Client
    public static final byte TECHNIQUE_FEEDBACK = 0x10;     // Technique success/failure feedback
    public static final byte DOMAIN_VISUAL = 0x11;          // Domain visual effects
    public static final byte CE_UPDATE = 0x12;              // Cursed energy update
    public static final byte TECHNIQUE_COOLDOWN = 0x13;     // Cooldown notification
    public static final byte PARTICLE_EFFECT = 0x14;        // Particle effects
    public static final byte SCREEN_EFFECT = 0x15;          // Screen effects

    // Bidirectional
    public static final byte HANDSHAKE = 0x20;

    /**
     * Skill Action Type (action field in skill packets)
     */
    public static class SkillAction {
        public static final byte START = 0x01;      // Start charging/activation
        public static final byte END = 0x02;        // End/release/cast
    }

    /**
     * Technique Slot (slot field in SKILL_TECHNIQUE packets)
     */
    public static class TechniqueSlot {
        public static final byte SLOT_1 = 0x01;  // X key
        public static final byte SLOT_2 = 0x02;  // C key
        public static final byte SLOT_3 = 0x03;  // V key
        public static final byte SLOT_4 = 0x04;  // B key
    }

    /**
     * Domain Expansion Flags (flags field in DOMAIN_EXPANSION packet)
     */
    public static class DomainFlags {
        public static final byte NORMAL = 0x00;          // Normal domain with barrier
        public static final byte NO_BARRIER = 0x01;      // No barrier domain (Shift + R)
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
