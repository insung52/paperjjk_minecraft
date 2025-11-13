package org.justheare.paperJJK;

/**
 * Factory class for creating Jujut instances from skill IDs
 * Converts string skill identifiers to appropriate Jujut objects
 */
public class JujutFactory {

    /**
     * Create a Jujut instance from a skill ID
     * @param skillId Skill identifier (e.g., "infinity_ao", "mizushi_kai")
     * @param jplayer Player object
     * @param power Power/charge amount
     * @param time Duration
     * @return Jujut instance or null if skill ID is invalid
     */
    public static Jujut createJujut(String skillId, Jplayer jplayer, int power, int time) {
        if (skillId == null || skillId.isEmpty()) {
            return null;
        }

        Jujut jujut = null;

        switch (skillId.toLowerCase()) {
            // ===== Infinity Skills =====
            case "infinity_passive":
                jujut = new Infinity_passive(jplayer, "passive", "infinity", false, power, time, 'a');
                jujut.rechargeable = false;
                jujut.skillId = skillId;
                break;

            case "infinity_ao":
                jujut = new Infinity(jplayer, "ao", "infinity", false, power, time, 'a');
                jujut.rechargeable = true;  // ao can be recharged
                jujut.skillId = skillId;
                break;

            case "infinity_aka":
                jujut = new Infinity(jplayer, "aka", "infinity", true, power, time, 'a');
                jujut.rechargeable = false;  // aka cannot be recharged
                jujut.skillId = skillId;
                break;

            // ===== Mizushi Skills =====
            case "mizushi_kai":
                jujut = new Mizushi(jplayer, "kai", "mizushi", false, power, time, 'a');
                jujut.rechargeable = false;
                jujut.skillId = skillId;
                break;

            case "mizushi_hachi":
                jujut = new Mizushi(jplayer, "hachi", "mizushi", true, power, time, 'a');
                jujut.rechargeable = false;
                jujut.skillId = skillId;
                break;

            case "mizushi_fuga":
                jujut = new Mizushi(jplayer, "fuga", "mizushi", false, power, time, 'a');
                jujut.rechargeable = false;
                jujut.skillId = skillId;
                break;

            default:
                PaperJJK.log(
                    String.format("Unknown skill ID: %s", skillId)
                );
                return null;
        }

        return jujut;
    }

    /**
     * Check if a skill ID is valid
     */
    public static boolean isValidSkillId(String skillId) {
        if (skillId == null) return false;

        return switch (skillId.toLowerCase()) {
            case "infinity_passive", "infinity_ao", "infinity_aka",
                 "mizushi_kai", "mizushi_hachi", "mizushi_fuga" -> true;
            default -> false;
        };
    }

    /**
     * Get the reverse skill ID for a given skill
     * (e.g., "infinity_ao" -> "infinity_aka", "mizushi_kai" -> "mizushi_hachi")
     * @return Reverse skill ID or null if no reverse exists
     */
    public static String getReverseSkillId(String skillId) {
        if (skillId == null) return null;

        return switch (skillId.toLowerCase()) {
            case "infinity_ao" -> "infinity_aka";
            case "mizushi_kai" -> "mizushi_hachi";
            default -> null;  // No reverse for passive, aka, hachi, fuga
        };
    }

    /**
     * Check if a skill can be reversed (has a reverse version)
     */
    public static boolean isReversible(String skillId) {
        return getReverseSkillId(skillId) != null;
    }
}
