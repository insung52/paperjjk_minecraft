package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

import java.io.*;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class JDomainData {
    private static File domainFolder;

    public static void init(File pluginFolder) {
        domainFolder = new File(pluginFolder, "domains");
        if (!domainFolder.exists()) {
            domainFolder.mkdirs();
        }
    }

    /**
     * Domain 블록 데이터를 압축된 파일로 저장
     */
    public static void saveDomainBlocks(UUID playerUuid, Jdomain_Builder builder) {
        if (builder == null || builder.block_count == 0) {
            PaperJJK.log("[JDomainData] No blocks to save for player " + playerUuid);
            return;
        }

        File domainFile = new File(domainFolder, playerUuid.toString() + ".dat");

        try (FileOutputStream fos = new FileOutputStream(domainFile);
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             DataOutputStream dos = new DataOutputStream(gzos)) {

            // 블록 개수 저장
            dos.writeInt(builder.block_count);
            PaperJJK.log("[JDomainData] Saving " + builder.block_count + " blocks for player " + playerUuid);

            // 각 블록 데이터 저장
            for (int i = 0; i < builder.block_count; i++) {
                Block block = builder.saved_blocks[i];
                Material material = builder.saved_material[i];
                BlockData blockData = builder.saved_blockdata[i];

                if (block == null) {
                    dos.writeBoolean(false); // null 마커
                    continue;
                }
                //PaperJJK.log(block.toString()+" "+material.toString());
                dos.writeBoolean(true); // 데이터 존재 마커

                // Location 저장
                Location loc = block.getLocation();
                dos.writeUTF(loc.getWorld().getName());
                dos.writeInt(loc.getBlockX());
                dos.writeInt(loc.getBlockY());
                dos.writeInt(loc.getBlockZ());

                // Material 저장
                dos.writeUTF(material.name());
                //PaperJJK.log(material.name());
                // BlockData 저장 (문자열로)
                dos.writeUTF(blockData.getAsString());
            }

            PaperJJK.log("[JDomainData] Domain blocks saved successfully: " + domainFile.length() + " bytes (compressed)");

        } catch (IOException e) {
            PaperJJK.log("[JDomainData] Failed to save domain blocks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 압축된 파일에서 Domain 블록 데이터 로드
     */
    public static boolean loadDomainBlocks(UUID playerUuid, Jdomain_Builder builder) {
        File domainFile = new File(domainFolder, playerUuid.toString() + ".dat");

        if (!domainFile.exists()) {
            PaperJJK.log("[JDomainData] No saved domain blocks found for player " + playerUuid);
            return false;
        }

        try (FileInputStream fis = new FileInputStream(domainFile);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             DataInputStream dis = new DataInputStream(gzis)) {

            // 블록 개수 로드
            int blockCount = dis.readInt();
            PaperJJK.log("[JDomainData] Loading " + blockCount + " blocks for player " + playerUuid);

            // 배열 초기화
            builder.saved_blocks = new Block[blockCount];
            builder.saved_material = new Material[blockCount];
            builder.saved_blockdata = new BlockData[blockCount];
            builder.saved_blockstate = new BlockState[blockCount];
            builder.block_count = blockCount;

            // 각 블록 데이터 로드
            int loadedCount = 0;
            for (int i = 0; i < blockCount; i++) {
                boolean hasData = dis.readBoolean();

                if (!hasData) {
                    builder.saved_blocks[i] = null;
                    builder.saved_material[i] = null;
                    builder.saved_blockdata[i] = null;
                    continue;
                }

                // Location 로드
                String worldName = dis.readUTF();
                int x = dis.readInt();
                int y = dis.readInt();
                int z = dis.readInt();

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    PaperJJK.log("[JDomainData] WARNING: World '" + worldName + "' not found, skipping block " + i);
                    dis.readUTF(); // Material skip
                    dis.readUTF(); // BlockData skip
                    continue;
                }

                Location loc = new Location(world, x, y, z);
                builder.saved_blocks[i] = loc.getBlock();

                // Material 로드
                String materialName = dis.readUTF();
                try {
                    builder.saved_material[i] = Material.valueOf(materialName);
                    //PaperJJK.log("Load : " + Material.valueOf(materialName));
                } catch (IllegalArgumentException e) {
                    PaperJJK.log("[JDomainData] WARNING: Invalid material '" + materialName + "', using AIR");
                    builder.saved_material[i] = Material.AIR;
                }

                // BlockData 로드
                String blockDataString = dis.readUTF();
                try {
                    builder.saved_blockdata[i] = Bukkit.createBlockData(blockDataString);
                } catch (IllegalArgumentException e) {
                    PaperJJK.log("[JDomainData] WARNING: Invalid block data, using default");
                    builder.saved_blockdata[i] = builder.saved_material[i].createBlockData();
                }

                // BlockState는 현재 상태로 (나중에 복원 시 업데이트됨)
                // builder.saved_blockstate[i] = builder.saved_blocks[i].getState();

                loadedCount++;
            }

            PaperJJK.log("[JDomainData] Domain blocks loaded successfully: " + loadedCount + "/" + blockCount + " blocks");
            return true;

        } catch (IOException e) {
            PaperJJK.log("[JDomainData] Failed to load domain blocks: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Domain 블록 데이터 파일 삭제
     */
    public static void deleteDomainBlocks(UUID playerUuid) {
        File domainFile = new File(domainFolder, playerUuid.toString() + ".dat");
        if (domainFile.exists()) {
            if (domainFile.delete()) {
                PaperJJK.log("[JDomainData] Deleted domain blocks file for player " + playerUuid);
            } else {
                PaperJJK.log("[JDomainData] Failed to delete domain blocks file for player " + playerUuid);
            }
        }
    }

    /**
     * Domain 데이터 파일 검증 및 미리보기 (처음 10개 블록만)
     */
    public static String verifyDomainFile(UUID playerUuid) {
        File domainFile = new File(domainFolder, playerUuid.toString() + ".dat");

        if (!domainFile.exists()) {
            return "§cDomain data file not found!";
        }

        StringBuilder result = new StringBuilder();
        result.append("§6=== Domain Data File Verification ===\n");
        result.append("§eFile: §f").append(domainFile.getName()).append("\n");
        result.append("§eSize: §f").append(domainFile.length()).append(" bytes (compressed)\n\n");

        try (FileInputStream fis = new FileInputStream(domainFile);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             DataInputStream dis = new DataInputStream(gzis)) {

            int blockCount = dis.readInt();
            result.append("§eTotal blocks: §f").append(blockCount).append("\n\n");

            result.append("§eFirst 20 blocks preview:\n");
            int previewCount = Math.min(20, blockCount);

            for (int i = 0; i < previewCount; i++) {
                boolean hasData = dis.readBoolean();

                if (!hasData) {
                    result.append("§7[").append(i).append("] §8null\n");
                    continue;
                }

                String worldName = dis.readUTF();
                int x = dis.readInt();
                int y = dis.readInt();
                int z = dis.readInt();
                String materialName = dis.readUTF();
                String blockDataString = dis.readUTF();

                result.append("§7[").append(i).append("] §f")
                      .append(worldName).append(" (")
                      .append(x).append(", ").append(y).append(", ").append(z).append(") ")
                      .append("§e").append(materialName).append("\n");
            }

            if (blockCount > 20) {
                result.append("§7... and ").append(blockCount - 20).append(" more blocks\n");
            }

            result.append("\n§aFile is valid and readable!");
            return result.toString();

        } catch (IOException e) {
            return "§cError reading file: " + e.getMessage();
        }
    }
}
