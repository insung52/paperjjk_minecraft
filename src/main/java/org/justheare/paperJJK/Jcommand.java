package org.justheare.paperJJK;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static org.justheare.paperJJK.PaperJJK.*;

public class Jcommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            if(player.isOp()){
                if(args[0].equalsIgnoreCase("refill")){
                    PaperJJK.getjobject(player).curseenergy=PaperJJK.getjobject(player).max_curseenergy;
                    player.sendMessage("curse energy refilled!");
                }
                else if(args[0].equals("basic")){
                    if(args[1].equals("infinity")){
                        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("nct_infinity");
                            meta.setAuthor("ins");
                            meta.addPage("50000000/10000/1/1/end");
                            meta.setDisplayName("§5Infinity");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("unct_infinity");
                            meta.setAuthor("ins");
                            meta.addPage("passive/5/999999/a/end");
                            meta.setDisplayName("§fpassive");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("unct_infinity");
                            meta.setAuthor("ins");
                            meta.addPage("ao/10/999999/a/end");
                            meta.setDisplayName("§9ao_10");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("unct_infinity");
                            meta.setAuthor("ins");
                            meta.addPage("ao/100/999999/a/end");
                            meta.setDisplayName("§9§lao_100");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("domain_expand");
                            meta.setAuthor("ins");
                            meta.addPage("0/10/end");
                            meta.setDisplayName("§0domain_10");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("domain_expand");
                            meta.setAuthor("ins");
                            meta.addPage("0/30/end");
                            meta.setDisplayName("§0domain_30");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);
                    }
                    else if(args[1].equals("mizushi")){
                        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                        BookMeta meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("nct_mizushi");
                            meta.setAuthor("ins");
                            meta.addPage("400000000/20000/1/1/end");
                            meta.setDisplayName("§5Mizushi");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("unct_mizushi");
                            meta.setAuthor("ins");
                            meta.addPage("kai/10/10/a/end");
                            meta.setDisplayName("§ckai_10");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("unct_mizushi");
                            meta.setAuthor("ins");
                            meta.addPage("kai/100/10/a/end");
                            meta.setDisplayName("§c§lkai_100");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("unct_mizushi");
                            meta.setAuthor("ins");
                            meta.addPage("fuga/100/40/a/end");
                            meta.setDisplayName("§4fuga");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("domain_expand");
                            meta.setAuthor("ins");
                            meta.addPage("0/30/end");
                            meta.setDisplayName("§0domain_30");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("domain_expand");
                            meta.setAuthor("ins");
                            meta.addPage("1/50/end");
                            meta.setDisplayName("§8domain_50_nb");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("domain_expand");
                            meta.setAuthor("ins");
                            meta.addPage("1/100/end");
                            meta.setDisplayName("§8domain_100_nb");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);

                        book = new ItemStack(Material.WRITTEN_BOOK);
                        meta = (BookMeta) book.getItemMeta();
                        if (meta != null) {
                            meta.setTitle("domain_expand");
                            meta.setAuthor("ins");
                            meta.addPage("1/200/end");
                            meta.setDisplayName("§8domain_200_nb");
                            book.setItemMeta(meta);
                        }
                        player.getInventory().addItem(book);
                    }
                }
                else if(args[0].equals("id")){
                    if(args[1].equals("build")){
                        getjobject(player).innate_domain.build();
                    }
                    else if(args[1].equals("destroy")){
                        getjobject(player).innate_domain.destroy();
                    }
                }
                else if(args[0].equals("ed")){
                    if(args[1].equals("build")){
                        int range=Integer.parseInt(args[2]);
                        if(range>1&&range<=50){
                            getjobject(player).innate_domain.build_expand(range);
                        }
                        else{
                            getjobject(player).innate_domain.build_expand(30);
                        }

                    }
                    else if(args[1].equals("destroy")){
                        getjobject(player).innate_domain.destroy_expand();
                    }
                }
                else if(args[0].equals("nb")){
                    if(args[1].equals("build")){
                        int range=Integer.parseInt(args[2]);
                        if(range>1&&range<=200){
                            getjobject(player).innate_domain.drow_expand(range);
                        }
                        else{
                            getjobject(player).innate_domain.drow_expand(50);
                        }

                    }
                    else if(args[1].equals("destroy")){
                        getjobject(player).innate_domain.undrow_expand();
                    }
                }
                else if(args[0].equals("ce")){
                    try{
                        getjobject(player).curseenergy=Integer.parseInt(args[1]);
                    }
                    catch(Exception e){
                    }
                }
                else if(args[0].equals("reset")){
                    PaperJJK.getjobject(player).naturaltech="";
                    PaperJJK.getjobject(player).max_curseenergy=200;
                    PaperJJK.getjobject(player).curseenergy=0;
                    player.sendMessage("cursed technic reseted");
                }
                else if(args[0].equals("set")){
                    if(args[1].equals("air")){
                        if(args[2].equals("true")){
                            PaperJJK.getjobject(player).can_air_surface=true;
                        }
                        else if(args[2].equals("false")){
                            PaperJJK.getjobject(player).can_air_surface=false;
                        }
                    }
                }
                else if(args[0].equals("cw")){
                    if(args[1].equals("ish")){
                        ItemMeta itemMeta=player.getItemInHand().getItemMeta();
                        itemMeta.setDisplayName(ChatColor.GRAY+"Inverted Spear of Heaven");
                        NamespacedKey key = new NamespacedKey(jjkplugin, "custom_tag");
                        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING,"cw_ish");
                        player.getItemInHand().setItemMeta(itemMeta);
                    }
                    else if(args[1].equals("kamutoke")){
                        ItemMeta itemMeta=player.getItemInHand().getItemMeta();
                        itemMeta.setDisplayName(ChatColor.AQUA+"Kamutoke");
                        NamespacedKey key = new NamespacedKey(jjkplugin, "custom_tag");
                        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING,"cw_kamutoke");
                        player.getItemInHand().setItemMeta(itemMeta);
                    }
                }
                else if(args[0].equals("mahoraga")){
                    LivingEntity mahoraga = (LivingEntity) ((Player) sender).getWorld().spawnEntity(((Player) sender).getLocation(), EntityType.IRON_GOLEM);
                    mahoraga.getTargetEntity(50,true);
                    jobjects.add(new Jobject( mahoraga ));
                    mahoraga.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,99999999,2));
                    mahoraga.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,99999999,5));
                    mahoraga.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST,99999999,50));
                    mahoraga.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH,40,20));
                    for(LivingEntity living : mahoraga.getLocation().getNearbyLivingEntities(50)){
                        try{
                            living.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,1,5));
                            mahoraga.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,1,5));
                            living.attack(mahoraga);
                            mahoraga.attack(living);
                        }catch (Exception e){

                        }
                    }
                    Jobject m_jobject = getjobject(mahoraga);
                    m_jobject.setvalues("mahoraga",4000000,10000,1,1);
                    m_jobject.usejujut("mahoraga","mahoraga","mahoraga",false,100,999,'a',null);
                }
                else if(args[0].equals("save")){
                    player.sendMessage(ChatColor.YELLOW + "Saving player data...");
                    JData.saveAllData();
                    player.sendMessage(ChatColor.GREEN + "Player data saved successfully!");
                }
                else if(args[0].equals("domaininfo")){
                    Jobject jobject = getjobject(player);
                    if(jobject.innate_domain != null){
                        player.sendMessage(ChatColor.GOLD + "=== Domain Info ===");
                        player.sendMessage("IsBuilt: " + jobject.innate_domain.isbuilt);
                        player.sendMessage("Level: " + jobject.innate_domain.level);
                        player.sendMessage("Range: " + jobject.innate_domain.range);
                        if(jobject.innate_domain.originbuilder != null){
                            player.sendMessage("Builder exists: true");
                            player.sendMessage("Block count: " + jobject.innate_domain.originbuilder.block_count);
                        } else {
                            player.sendMessage("Builder exists: false");
                        }

                        // dat 파일 확인
                        java.io.File domainFile = new java.io.File(jjkplugin.getDataFolder(), "domains/" + player.getUniqueId().toString() + ".dat");
                        if(domainFile.exists()){
                            player.sendMessage(ChatColor.GREEN + "Domain data file exists: " + domainFile.length() + " bytes");
                        } else {
                            player.sendMessage(ChatColor.RED + "Domain data file not found!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have an innate domain!");
                    }
                }
                else if(args[0].equals("domainverify")){
                    String result = JDomainData.verifyDomainFile(player.getUniqueId());
                    for(String line : result.split("\n")){
                        player.sendMessage(line);
                    }
                }
                else if(args[0].equals("rule")){
                    if(args[1].equals("breakblock")){
                        if(args[2].equals("true")){
                            rule_breakblock=true;
                            player.sendMessage("break block set true");
                        }
                        else if(args[2].equals("false")){
                            rule_breakblock=false;
                            player.sendMessage("break block set false");
                        }
                    }
                    else if(args[1].equals("hud")){
                        if(args[2].equals("true")){
                            rule_hud=true;
                            player.sendMessage("hud set true");
                        }
                        else if(args[2].equals("false")){
                            rule_hud=false;
                            hud_off=true;
                            player.sendMessage("hud set false");
                        }
                    }
                }
            }
            else {
                sender.sendMessage(ChatColor.GRAY+"no permission");
            }
        }
        return false;
    }
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("jjk")){
            if(args.length==1){
                return Arrays.asList("refill","basic","reset","set","cw","ce","mahoraga","id","ed","nb","save","domaininfo","domainverify","rule");
            }
            else if(args.length==2){
                if(args[0].equals("basic")){
                    return Arrays.asList("infinity","mizushi");
                }
                else if(args[0].equals("id")||args[0].equals("ed")||args[0].equals("nb")){
                    return Arrays.asList("build","destroy");
                }
                else if(args[0].equals("cw")){
                    return Arrays.asList("ish","kamutoke");
                }
                else if(args[0].equals("rule")){
                    return Arrays.asList("breakblock","hud");
                }
            }
            else if(args.length==3){
                if(args[0].equals("rule")){
                    if(args[1].equals("breakblock")){
                        return Arrays.asList("true","false");
                    }
                    else if(args[1].equals("hud")){
                        return Arrays.asList("true","false");
                    }
                }

            }
        }
        return List.of("");
    }
}
