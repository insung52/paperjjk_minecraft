package org.justheare.paperJJK.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.justheare.paperJJK.PaperJJK;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages skill descriptions loaded from JSON files
 * Provides skill info to clients via packets
 */
public class SkillDescriptionManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, SkillInfo> skillDescriptions = new HashMap<>();
    private static File skillsDirectory;

    /**
     * Initialize skill description system
     * @param dataFolder Plugin data folder
     */
    public static void init(File dataFolder) {
        skillsDirectory = new File(dataFolder, "skills");

        // Create directory if it doesn't exist
        if (!skillsDirectory.exists()) {
            skillsDirectory.mkdirs();
            PaperJJK.log("[SkillDescriptions] Created skills directory: " + skillsDirectory.getPath());
        }

        loadAllDescriptions();
    }

    /**
     * Load all skill descriptions from JSON files
     */
    public static void loadAllDescriptions() {
        skillDescriptions.clear();

        if (!skillsDirectory.exists()) {
            PaperJJK.log("[SkillDescriptions] Skills directory not found");
            return;
        }

        File[] files = skillsDirectory.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            PaperJJK.log("[SkillDescriptions] No skill JSON files found, creating defaults...");
            createDefaultDescriptions();
            return;
        }

        int loaded = 0;
        for (File file : files) {
            try {
                SkillInfo info = loadFromFile(file);
                if (info != null) {
                    skillDescriptions.put(info.skillId, info);
                    loaded++;
                }
            } catch (Exception e) {
                PaperJJK.log("[SkillDescriptions] Failed to load " + file.getName() + ": " + e.getMessage());
            }
        }

        PaperJJK.log("[SkillDescriptions] Loaded " + loaded + " skill descriptions");
    }

    /**
     * Load skill info from JSON file
     */
    private static SkillInfo loadFromFile(File file) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            String skillId = json.get("skillId").getAsString();
            String displayName = json.get("displayName").getAsString();
            String description = json.get("description").getAsString();
            int requiredCE = json.has("requiredCE") ? json.get("requiredCE").getAsInt() : 0;
            int cooldown = json.has("cooldown") ? json.get("cooldown").getAsInt() : 0;
            String type = json.has("type") ? json.get("type").getAsString() : "normal";

            return new SkillInfo(skillId, displayName, description, requiredCE, cooldown, type);
        }
    }

    /**
     * Save skill info to JSON file
     */
    private static void saveToFile(SkillInfo info) {
        File file = new File(skillsDirectory, info.skillId + ".json");

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            JsonObject json = new JsonObject();
            json.addProperty("skillId", info.skillId);
            json.addProperty("displayName", info.displayName);
            json.addProperty("description", info.description);
            json.addProperty("requiredCE", info.requiredCE);
            json.addProperty("cooldown", info.cooldown);
            json.addProperty("type", info.type);

            GSON.toJson(json, writer);
            PaperJJK.log("[SkillDescriptions] Saved " + info.skillId + ".json");
        } catch (IOException e) {
            PaperJJK.log("[SkillDescriptions] Failed to save " + info.skillId + ": " + e.getMessage());
        }
    }

    /**
     * Get skill info by ID
     */
    public static SkillInfo getSkillInfo(String skillId) {
        return skillDescriptions.get(skillId);
    }

    /**
     * Create default skill descriptions
     */
    private static void createDefaultDescriptions() {
        // Infinity skills
        addDefault(new SkillInfo("infinity_passive", "무한 (無限)",
            "무한급수를 이용한 완벽한 방어. 모든 공격을 완전히 차단합니다.", 0, 0, "passive"));
        addDefault(new SkillInfo("infinity_ao", "창 (蒼)",
            "무한의 인력을 발동하여 적을 끌어당깁니다.", 100, 5, "attraction"));
        addDefault(new SkillInfo("infinity_aka", "혁 (赫)",
            "무한의 척력을 발동하여 적을 밀어냅니다.", 100, 5, "repulsion"));
        addDefault(new SkillInfo("infinity_murasaki", "자 (紫)",
            "창과 혁을 융합한 허수 질량구. 모든 것을 소멸시킵니다.", 200, 10, "fusion"));

        // Mizushi skills
        addDefault(new SkillInfo("mizushi_kai", "해 (解)",
            "베는 저주력. 보이지 않는 베기 공격을 날립니다.", 50, 3, "slash"));
        addDefault(new SkillInfo("mizushi_hachi", "팔 (捌)",
            "베는 저주력을 8개 방향으로 확장합니다.", 80, 5, "slash"));
        addDefault(new SkillInfo("mizushi_fuga", "불화개방 (伏魔御廚子)",
            "영역전개 내에서 화살을 발사하는 최강의 술식.", 500, 30, "special"));

        PaperJJK.log("[SkillDescriptions] Created default skill descriptions");
    }

    private static void addDefault(SkillInfo info) {
        skillDescriptions.put(info.skillId, info);
        saveToFile(info);
    }

    /**
     * Skill information data class
     */
    public static class SkillInfo {
        public final String skillId;
        public final String displayName;
        public final String description;
        public final int requiredCE;
        public final int cooldown;
        public final String type;

        public SkillInfo(String skillId, String displayName, String description,
                        int requiredCE, int cooldown, String type) {
            this.skillId = skillId;
            this.displayName = displayName;
            this.description = description;
            this.requiredCE = requiredCE;
            this.cooldown = cooldown;
            this.type = type;
        }
    }
}
