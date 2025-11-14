package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JData {
    private static File dataFile;
    private static FileConfiguration dataConfig;

    public static void init(File pluginFolder) {
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }
        dataFile = new File(pluginFolder, "playerdata.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                PaperJJK.log("Failed to create playerdata.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public static void saveAllData() {
        if (dataConfig == null) return;

        PaperJJK.log("Saving player data...");

        // 기존 데이터 초기화
        dataConfig.set("players", null);

        for (Jobject jobject : PaperJJK.jobjects) {
            if (jobject instanceof Jplayer) {
                saveJobject(jobject);
            }
        }

        try {
            dataConfig.save(dataFile);
            PaperJJK.log("Player data saved successfully!");
        } catch (IOException e) {
            PaperJJK.log("Failed to save player data: " + e.getMessage());
        }
    }

    /**
     * Load player slot configuration from playerdata
     */
    public static void loadPlayerSlots(Jplayer jplayer) {
        String uuid = jplayer.uuid.toString();
        String path = "players." + uuid;

        jplayer.slot1Skill = dataConfig.getString(path + ".slot1Skill", "");
        jplayer.slot2Skill = dataConfig.getString(path + ".slot2Skill", "");
        jplayer.slot3Skill = dataConfig.getString(path + ".slot3Skill", "");
        jplayer.slot4Skill = dataConfig.getString(path + ".slot4Skill", "");

        // Initialize slots based on naturaltech if empty
        jplayer.initializeSlots();

        PaperJJK.log("[JData] Loaded player slots - 1:" + jplayer.slot1Skill +
                     ", 2:" + jplayer.slot2Skill +
                     ", 3:" + jplayer.slot3Skill +
                     ", 4:" + jplayer.slot4Skill);
    }

    public static void saveJobject(Jobject jobject) {
        String uuid = jobject.uuid.toString();
        String path = "players." + uuid;

        // 기본 데이터
        dataConfig.set(path + ".naturaltech", jobject.naturaltech);
        dataConfig.set(path + ".max_curseenergy", jobject.max_curseenergy);
        dataConfig.set(path + ".curseenergy", jobject.curseenergy);
        dataConfig.set(path + ".max_cursecurrent", jobject.max_cursecurrent);
        dataConfig.set(path + ".cursecurrent", jobject.cursecurrent);
        dataConfig.set(path + ".reversecurse", jobject.reversecurse);
        dataConfig.set(path + ".reversecurse_out", jobject.reversecurse_out);
        dataConfig.set(path + ".can_air_surface", jobject.can_air_surface);
        dataConfig.set(path + ".black_flash_num", jobject.black_flash_num);
        dataConfig.set(path + ".blocked", jobject.blocked);

        // Innate Domain 데이터
        if (jobject.innate_domain != null) {
            String domainPath = path + ".innate_domain";
            dataConfig.set(domainPath + ".isbuilt", jobject.innate_domain.isbuilt);
            dataConfig.set(domainPath + ".level", jobject.innate_domain.level);
            dataConfig.set(domainPath + ".range", jobject.innate_domain.range);
            dataConfig.set(domainPath + ".innate_border", jobject.innate_domain.innate_border.name());

            if (jobject.innate_domain.location != null && jobject.innate_domain.isbuilt) {
                Location loc = jobject.innate_domain.location;
                dataConfig.set(domainPath + ".location.world", loc.getWorld().getName());
                dataConfig.set(domainPath + ".location.x", loc.getX());
                dataConfig.set(domainPath + ".location.y", loc.getY());
                dataConfig.set(domainPath + ".location.z", loc.getZ());
            }
        }

        // Skill slot configuration (for Jplayer only)
        if (jobject instanceof Jplayer jplayer) {
            dataConfig.set(path + ".slot1Skill", jplayer.slot1Skill);
            dataConfig.set(path + ".slot2Skill", jplayer.slot2Skill);
            dataConfig.set(path + ".slot3Skill", jplayer.slot3Skill);
            dataConfig.set(path + ".slot4Skill", jplayer.slot4Skill);

            // Domain expansion configuration
            dataConfig.set(path + ".normalDomainRange", jplayer.normalDomainRange);
            dataConfig.set(path + ".noBarrierDomainRange", jplayer.noBarrierDomainRange);
        }

        // Mahoraga 적응 데이터
        if (jobject.naturaltech.equals("mahoraga") && !jobject.jujuts.isEmpty() && jobject.jujuts.get(0) instanceof Mahoraga) {
            Mahoraga mahoraga = (Mahoraga) jobject.jujuts.get(0);
            String mahoPath = path + ".mahoraga";
            dataConfig.set(mahoPath + ".adapt_list", mahoraga.adapt_list);
            dataConfig.set(mahoPath + ".adapt_list_power", mahoraga.adapt_list_power);
            dataConfig.set(mahoPath + ".adapt_list_num", mahoraga.adapt_list_num);
        }
    }

    public static void loadAllData() {
        if (dataConfig == null) {
            PaperJJK.log("[JData] ERROR: dataConfig is null!");
            return;
        }

        PaperJJK.log("[JData] Loading player data...");

        if (!dataConfig.contains("players")) {
            PaperJJK.log("[JData] No player data found in config.");
            return;
        }

        int loadedCount = 0;
        for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                PaperJJK.log("[JData] Loading data for UUID: " + uuidString);
                loadJobject(uuid);
                loadedCount++;
            } catch (Exception e) {
                PaperJJK.log("[JData] ERROR: Failed to load data for UUID " + uuidString + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        PaperJJK.log("[JData] Player data loaded successfully! Total: " + loadedCount + " players");
    }

    private static void loadJobject(UUID uuid) {
        String path = "players." + uuid.toString();

        PaperJJK.log("[JData] Loading jobject from path: " + path);

        // 플레이어가 이미 접속해 있는지 확인
        Jobject existingJobject = null;
        for (Jobject jo : PaperJJK.jobjects) {
            if (jo.uuid.equals(uuid)) {
                existingJobject = jo;
                PaperJJK.log("[JData] Found existing jobject for UUID: " + uuid);
                break;
            }
        }

        // 접속 중이 아니면 임시 Jobject 생성 (실제 플레이어 접속 시 재연결됨)
        if (existingJobject == null) {
            existingJobject = new Jobject(null);
            existingJobject.uuid = uuid;
            PaperJJK.jobjects.add(existingJobject);
            PaperJJK.log("[JData] Created new jobject for UUID: " + uuid);
        }

        // 기본 데이터 로드
        existingJobject.naturaltech = dataConfig.getString(path + ".naturaltech", "");
        existingJobject.max_curseenergy = dataConfig.getInt(path + ".max_curseenergy", 200);
        existingJobject.curseenergy = dataConfig.getInt(path + ".curseenergy", 1);
        existingJobject.max_cursecurrent = dataConfig.getInt(path + ".max_cursecurrent", 1);
        existingJobject.cursecurrent = dataConfig.getInt(path + ".cursecurrent", 0);
        existingJobject.reversecurse = dataConfig.getBoolean(path + ".reversecurse", false);
        existingJobject.reversecurse_out = dataConfig.getBoolean(path + ".reversecurse_out", false);
        existingJobject.can_air_surface = dataConfig.getBoolean(path + ".can_air_surface", false);
        existingJobject.black_flash_num = dataConfig.getDouble(path + ".black_flash_num", 0.01);
        existingJobject.blocked = dataConfig.getBoolean(path + ".blocked", true);

        PaperJJK.log("[JData] Loaded basic data - naturaltech: " + existingJobject.naturaltech +
                     ", max_curseenergy: " + existingJobject.max_curseenergy +
                     ", blocked: " + existingJobject.blocked +
                     ", can_air_surface: " + existingJobject.can_air_surface);

        // Skill slot configuration (for Jplayer only)
        if (existingJobject instanceof Jplayer jplayer) {
            jplayer.slot1Skill = dataConfig.getString(path + ".slot1Skill", "");
            jplayer.slot2Skill = dataConfig.getString(path + ".slot2Skill", "");
            jplayer.slot3Skill = dataConfig.getString(path + ".slot3Skill", "");
            jplayer.slot4Skill = dataConfig.getString(path + ".slot4Skill", "");

            // Initialize slots based on naturaltech if empty
            jplayer.initializeSlots();

            // Domain expansion configuration
            jplayer.normalDomainRange = dataConfig.getInt(path + ".normalDomainRange", 30);
            jplayer.noBarrierDomainRange = dataConfig.getInt(path + ".noBarrierDomainRange", 50);

            PaperJJK.log("[JData] Loaded slot config - 1:" + jplayer.slot1Skill +
                         ", 2:" + jplayer.slot2Skill +
                         ", 3:" + jplayer.slot3Skill +
                         ", 4:" + jplayer.slot4Skill);
            PaperJJK.log("[JData] Loaded domain config - normal:" + jplayer.normalDomainRange +
                         ", noBarrier:" + jplayer.noBarrierDomainRange);
        }

        // Innate Domain 생성 및 로드
        if (dataConfig.contains(path + ".innate_domain")) {
            String domainPath = path + ".innate_domain";
            PaperJJK.log("[JData] Found innate_domain data in config");
            PaperJJK.log("[JData] Loading innate domain for naturaltech: " + existingJobject.naturaltech);

            // naturaltech에 따라 적절한 innate_domain 생성
            if (existingJobject.naturaltech.equals("infinity")) {
                existingJobject.innate_domain = new Infinity_domain(existingJobject);
                PaperJJK.log("[JData] Created Infinity_domain");
            } else if (existingJobject.naturaltech.equals("mizushi")) {
                existingJobject.innate_domain = new Mizushi_domain(existingJobject);
                PaperJJK.log("[JData] Created Mizushi_domain");
            } else if (!existingJobject.naturaltech.isEmpty()) {
                existingJobject.innate_domain = new Jdomain_innate(existingJobject);
                PaperJJK.log("[JData] Created Jdomain_innate");
            }

            if (existingJobject.innate_domain != null) {
                existingJobject.innate_domain.isbuilt = dataConfig.getBoolean(domainPath + ".isbuilt", false);
                existingJobject.innate_domain.level = dataConfig.getInt(domainPath + ".level", 0);
                existingJobject.innate_domain.range = dataConfig.getInt(domainPath + ".range", 30);

                PaperJJK.log("[JData] Domain data - isbuilt: " + existingJobject.innate_domain.isbuilt +
                             ", level: " + existingJobject.innate_domain.level +
                             ", range: " + existingJobject.innate_domain.range);

                String borderName = dataConfig.getString(domainPath + ".innate_border", "GLASS");
                try {
                    existingJobject.innate_domain.innate_border = Material.valueOf(borderName);
                } catch (IllegalArgumentException e) {
                    existingJobject.innate_domain.innate_border = Material.GLASS;
                    PaperJJK.log("[JData] WARNING: Invalid border material: " + borderName);
                }

                // Location 로드
                if (dataConfig.contains(domainPath + ".location.world")) {
                    String worldName = dataConfig.getString(domainPath + ".location.world");
                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        double x = dataConfig.getDouble(domainPath + ".location.x");
                        double y = dataConfig.getDouble(domainPath + ".location.y");
                        double z = dataConfig.getDouble(domainPath + ".location.z");
                        existingJobject.innate_domain.location = new Location(world, x, y, z);
                        PaperJJK.log("[JData] Loaded domain location: " + worldName + " " + x + "," + y + "," + z);

                        // isbuilt가 true면 builder 재생성 및 블록 데이터 로드
                        if (existingJobject.innate_domain.isbuilt) {
                            existingJobject.innate_domain.originbuilder = new Jdomain_Builder(
                                existingJobject.innate_domain,
                                existingJobject.innate_domain.range,
                                existingJobject.innate_domain.innate_border,
                                existingJobject.innate_domain.location
                            );
                            PaperJJK.log("[JData] Restored domain builder");

                            // 저장된 블록 데이터 로드
                            boolean blocksLoaded = JDomainData.loadDomainBlocks(uuid, existingJobject.innate_domain.originbuilder);
                            if (blocksLoaded) {
                                PaperJJK.log("[JData] Loaded saved domain blocks for player " + uuid);
                            } else {
                                PaperJJK.log("[JData] No saved domain blocks found for player " + uuid);
                            }
                        }
                    } else {
                        PaperJJK.log("[JData] WARNING: World '" + worldName + "' not found for player " + uuid);
                    }
                }
            }
        } else {
            PaperJJK.log("[JData] No innate domain data found for this player");
        }

        // Mahoraga 적응 데이터는 실제 Mahoraga 소환 시 적용되도록 저장만 해둠
        if (dataConfig.contains(path + ".mahoraga")) {
            // 이 데이터는 나중에 Mahoraga 생성 시 적용
        }
    }

    // Mahoraga 소환 시 저장된 적응 데이터 로드
    public static void loadMahoragaAdaptData(Mahoraga mahoraga, UUID playerUuid) {
        if (dataConfig == null) return;

        String path = "players." + playerUuid.toString() + ".mahoraga";
        if (!dataConfig.contains(path)) return;

        List<String> adaptList = dataConfig.getStringList(path + ".adapt_list");
        List<Double> adaptPower = (List<Double>) dataConfig.getList(path + ".adapt_list_power", new ArrayList<>());
        List<Integer> adaptNum = dataConfig.getIntegerList(path + ".adapt_list_num");

        mahoraga.adapt_list.clear();
        mahoraga.adapt_list_power.clear();
        mahoraga.adapt_list_num.clear();

        mahoraga.adapt_list.addAll(adaptList);
        mahoraga.adapt_list_power.addAll(adaptPower);
        mahoraga.adapt_list_num.addAll(adaptNum);

        PaperJJK.log("Loaded Mahoraga adaptation data for player " + playerUuid);
    }
}
