package org.justheare.paperJJK.network;

import org.justheare.paperJJK.PaperJJK;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages skill descriptions (hardcoded in code)
 * Updated via plugin updates, no external files needed
 */
public class SkillDescriptionManager {
    private static final Map<String, SkillInfo> SKILL_DEFINITIONS = new HashMap<>();

    /**
     * Initialize skill descriptions (load from code)
     */
    public static void init() {
        // Infinity skills
        register("infinity_passive", "무한 (無限)",
            "[즉시 사용] 무한급수를 이용한 완벽한 방어. 모든 공격을 완전히 차단합니다. [스킬 단축키] 를 길게 눌러 주력 재충전할 수 있습니다.",
            "초당 100 (최대)");

        register("infinity_ao", "창 (蒼)",
            "[충전 후 사용] 무한의 인력을 발동하여 적을 끌어당깁니다. \nT + [스킬 단축키] 로 시선 고정 및 해제 상태를 토글할 수 있습니다. \n[스킬 단축키] 를 길게 눌러 주력 재충전 및 \n동시에 마우스 스크롤로 발동 위치를 이동할 수 있습니다.",
            "초당 2000 (최대)");

        register("infinity_aka", "혁 (赫)",
            "[충전 후 사용] 무한의 척력을 발동하여 적을 밀어냅니다. \n창 (蒼) 과 충돌 시 murasaki 를 발사합니다. ",
            "초당 2000 (최대)");

        // Mizushi skills
        register("mizushi_kai", "해 (解)",
            "[충전 후 사용] 참격을 날립니다. \n조준선 이동 방향에 따라서 참격의 각도가 정해집니다.",
            "초당 8000 (최대)");

        register("mizushi_hachi", "팔 (捌)",
            "[즉시 사용] 앞의 상대를 난도질합니다. \n상대와의 주력량 차이에 따라 피해가 결정됩니다.",
            "4000");

        register("mizushi_fuga", "부가 (賦河)",
            "[충전 후 사용] 불의 화살을 발사합니다. \n해 (解) 와 팔 (捌) 이 적중된 상대에게 강한 피해를 입힙니다. \n다수 대상에게 효과가 분산됩니다.",
            "초당 20000 (최대)");

        PaperJJK.log("[SkillDescriptions] Loaded " + SKILL_DEFINITIONS.size() + " skill definitions");
    }

    /**
     * Register a skill definition
     */
    private static void register(String skillId, String displayName, String description, String requiredCE) {
        SKILL_DEFINITIONS.put(skillId, new SkillInfo(skillId, displayName, description, requiredCE));
    }

    /**
     * Get skill info by ID
     */
    public static SkillInfo getSkillInfo(String skillId) {
        return SKILL_DEFINITIONS.get(skillId);
    }

    /**
     * Skill information data class
     */
    public static class SkillInfo {
        public final String skillId;
        public final String displayName;
        public final String description;
        public final String requiredCE;

        public SkillInfo(String skillId, String displayName, String description, String requiredCE) {
            this.skillId = skillId;
            this.displayName = displayName;
            this.description = description;
            this.requiredCE = requiredCE;
        }
    }
}
