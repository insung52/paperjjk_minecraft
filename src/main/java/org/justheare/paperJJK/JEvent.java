package org.justheare.paperJJK;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.justheare.paperJJK.PaperJJK.*;

//평타 :
//주술
//환경 :
public class JEvent implements Listener {
    // 상호작용 가능한 블록 목록
    private static final Set<Material> INTERACTABLE_BLOCKS = EnumSet.of(
            Material.CHEST, Material.TRAPPED_CHEST,   // 상자
            Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, // 화로류
            Material.BARREL, Material.CRAFTING_TABLE, Material.ENCHANTING_TABLE, // 작업대, 마법부여대
            Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL, // 모루
            Material.LECTERN, Material.STONECUTTER, Material.LOOM, Material.CARTOGRAPHY_TABLE, Material.GRINDSTONE, // 기능 블록
            Material.BELL, Material.BEEHIVE, Material.BEE_NEST, // 종, 벌집
            Material.LEVER, Material.STONE_BUTTON, Material.OAK_BUTTON, Material.SPRUCE_BUTTON, // 버튼, 레버
            Material.BIRCH_BUTTON, Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON,
            Material.WARPED_BUTTON, Material.CRIMSON_BUTTON,
            //Material.TRAPDOOR, Material.IRON_TRAPDOOR, // 다락문
            //Material.FENCE_GATE, Material.IRON_DOOR, Material.OAK_DOOR, // 문과 울타리 문
            Material.JUKEBOX // 주크박스
    );
    private final PaperJJK jjkplugin;
    public JEvent(PaperJJK instance){
        jjkplugin = instance;
    }
    @EventHandler
    public void onTarget(EntityTargetEvent event){
        Jobject jobject = getjobject(event.getEntity());
        //PaperJJK.log("target"+event.getEntity()+" "+event.getTarget());
        if(jobject!=null&&event.getTarget()!=null){
            if(jobject.naturaltech.equals("mahoraga")){
                jobject.jujuts.get(0).m_target=event.getTarget();
            }
        }
    }
    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event){
        Entity victim= event.getEntity();
        Jobject v_jobject= getjobject(victim);
        double damage=event.getDamage();
        if(v_jobject!=null) {
            //log("ede ccc");
            if(!victim.getScoreboardTags().contains("cursed")){
                if(v_jobject.naturaltech.equals("mahoraga")){
                    if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)||event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)||event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)){
                        //PaperJJK.log("entity to ede");
                        //log("ede not fire");
                    }
                    else if(event.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING) || event.getCause().equals(EntityDamageEvent.DamageCause.LAVA) ||event.getCause().equals(EntityDamageEvent.DamageCause.FIRE)||event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)||event.getCause().equals(EntityDamageEvent.DamageCause.CONTACT)||event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)||event.getCause().equals(EntityDamageEvent.DamageCause.HOT_FLOOR)    ){
                        Mahoraga jujut = (Mahoraga) v_jobject.jujuts.get(0);
                        //log("ede fire");
                        //PaperJJK.log(event.getCause().toString()+" entitydamage "+jujut.adapt_list.size());
                        double power = jujut.pre_adapt(event.getCause().toString(),"damage",1);
                        //PaperJJK.log(String.valueOf(power));
                        if(power<Math.random()){
                            event.setCancelled(true);
                            return;
                        }
                    }
                    else {
                        Mahoraga jujut = (Mahoraga) v_jobject.jujuts.get(0);
                        //log("ede fire");
                        //PaperJJK.log(event.getCause().toString()+" entitydamage "+jujut.adapt_list.size());
                        double power = jujut.pre_adapt(event.getCause().toString(),"damage",1);
                    }
                }
            }

            if(!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)){
                //PaperJJK.log("curse");
                if (v_jobject.naturaltech.equals("infinity")) {
                    for (Jujut jujut : v_jobject.jujuts) {
                        if (jujut instanceof Infinity_passive ip) {

                            event.setCancelled(true);
                            break;
                        }
                    }
                }
                if (!event.isCancelled()) {
                    if(v_jobject.max_cursecurrent>0){
                        damage -= damage * ((Math.pow(v_jobject.curseenergy, 0.15) * 5) / 100) * ((double) v_jobject.cursecurrent / v_jobject.max_cursecurrent);
                        event.setDamage(damage);
                        v_jobject.curseenergy -= (v_jobject.max_cursecurrent - v_jobject.cursecurrent) * event.getDamage() / 3;
                        if (v_jobject.curseenergy < 0) {
                            v_jobject.curseenergy = 0;
                        }
                    }

                }
            }
        }
        if(victim instanceof LivingEntity living){
            if(    ( event.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING) || event.getCause().equals(EntityDamageEvent.DamageCause.LAVA) ||event.getCause().equals(EntityDamageEvent.DamageCause.FIRE)||event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)||event.getCause().equals(EntityDamageEvent.DamageCause.CONTACT)||event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)||event.getCause().equals(EntityDamageEvent.DamageCause.HOT_FLOOR) )   ){
                living.setMaximumNoDamageTicks(5);
                living.setNoDamageTicks(5);
                if(event.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING)){
                    event.setDamage(damage/2);
                }
            }
        }
    }
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event){
        Entity attacker=event.getDamager();
        Jobject a_jobject = getjobject(attacker);
        if(a_jobject!=null&&a_jobject.infinity_stun_tick>0){
            event.setCancelled(true);
            return;
        }
        Entity victim=event.getEntity();
        double damage=event.getDamage();
        Jobject v_jobject= getjobject(victim);
        if(victim instanceof LivingEntity living){
            if(!    (event.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING) ||event.getCause().equals(EntityDamageEvent.DamageCause.LAVA) ||event.getCause().equals(EntityDamageEvent.DamageCause.FIRE)||event.getCause().equals(EntityDamageEvent.DamageCause.CONTACT)||event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION) )                ){
                living.setMaximumNoDamageTicks(2);
                living.setNoDamageTicks(2);
            }
        }
        if(!victim.getScoreboardTags().contains("cursed")){
            if(attacker instanceof  Player player&&!player.getEquipment().getItemInMainHand().isEmpty()){
                if(getItemTag(player.getEquipment().getItemInMainHand()).equals("cw_ish")&&player.getCooldown(player.getEquipment().getItemInMainHand().getType())==0){
                    player.setCooldown(player.getEquipment().getItemInMainHand().getType(),getjobject(player).usecw("ish",true));
                }
                if(getItemTag(player.getEquipment().getItemInMainHand()).equals("cw_kamutoke")&&player.getCooldown(player.getEquipment().getItemInMainHand().getType())==0){
                    player.setCooldown(player.getEquipment().getItemInMainHand().getType(),getjobject(player).usecw("kamutoke",true));
                }

            }
            else if(attacker instanceof LivingEntity living&&getItemTag(living.getEquipment().getItemInMainHand()).equals("cw_ish")){
                if(getjobject(attacker)!=null){
                    getjobject(attacker).usecw("ish",true);
                }
                else {
                    jobjects.add(new Jliving(attacker));
                    getjobject(attacker).usecw("ish",true);
                }
            }
            else if(attacker instanceof LivingEntity living&&getItemTag(living.getEquipment().getItemInMainHand()).equals("cw_kamutoke")){
                if(getjobject(attacker)!=null){
                    getjobject(attacker).usecw("kamutoke",true);
                }
                else {
                    jobjects.add(new Jliving(attacker));
                    getjobject(attacker).usecw("kamutoke",true);
                }
            }
        }

        if(v_jobject!=null){
            boolean c1 = victim.getScoreboardTags().contains("cursed");

            damage-=damage*((Math.pow(v_jobject.curseenergy,0.15)*4.5)/100)*((double) (v_jobject.max_cursecurrent-v_jobject.cursecurrent) /v_jobject.max_cursecurrent);
            //log("edee ccc");
            if(!c1){
                if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)||event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)||event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)){
                    if(v_jobject.naturaltech.equals("mahoraga")){
                        Mahoraga jujut = (Mahoraga) v_jobject.jujuts.get(0);
                        damage = jujut.pre_adapt(event.getCause().toString(),"damage",damage);
                        //log("edee fire");
                        event.setDamage(damage);
                    }
                }
                else{
                }
            }

            if(v_jobject.naturaltech.equals("infinity")){   //infinity defence
                for (Jujut jujut : v_jobject.jujuts) {
                    if (jujut instanceof Infinity_passive ip) {
                        Jobject atj = getjobject(attacker);
                        if(atj!=null&&atj.naturaltech.equals("mahoraga")){
                            Mahoraga mjujut = (Mahoraga) atj.jujuts.get(0);
                            if(mjujut.pre_adapt("infinity_passive","curse",1)>0){
                                event.setCancelled(true);
                                return;
                            }
                        }


                        else if(atj==null){
                            event.setCancelled(true);
                            return;
                        }
                        else if(!c1){
                            event.setDamage(0);
                            event.setCancelled(true);
                            return;
                        }
                        //event.setCancelled(ip.defence(attacker));
                    }
                }
            }
            if(victim.getScoreboardTags().contains("cursed")){     //cursed attack
                v_jobject.curseenergy-=(v_jobject.max_cursecurrent-v_jobject.cursecurrent)*event.getDamage();
                if(v_jobject.curseenergy<0){
                    v_jobject.curseenergy=0;
                }
                else {
                    event.setDamage(damage);
                }
                victim.removeScoreboardTag("cursed");

            }
            else {     //physical
                if(v_jobject.cursespirit) {
                    event.setCancelled(true);
                }
                if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)){
                    boolean bf = is_black_flash(attacker,victim);
                    if(bf){
                        damage=Math.pow(damage,1.5);
                        event.setDamage(damage);
                    }
                }
                else {
                    event.setDamage(Math.round(damage));
                    v_jobject.curseenergy-=(v_jobject.max_cursecurrent-v_jobject.cursecurrent)*event.getDamage()/100;
                }
                if(attacker instanceof Player player&&!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                    event.setCancelled(useing_jujut(player, true,victim));
                }
            }
        }
        else{
            if(victim.getScoreboardTags().contains("cursed")){
                victim.removeScoreboardTag("cursed");
            }
            else {
                if(event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)){
                    boolean bf = is_black_flash(attacker,victim);
                    if(bf){
                        damage=damage*damage;
                        event.setDamage(damage);
                    }
                }
                if(attacker instanceof Player player&&!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)){
                    event.setCancelled(useing_jujut(player,true,victim));
                }
            }
        }
        if(victim instanceof LivingEntity living){
            if(    (event.getCause().equals(EntityDamageEvent.DamageCause.LAVA) ||event.getCause().equals(EntityDamageEvent.DamageCause.FIRE)||event.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK)||event.getCause().equals(EntityDamageEvent.DamageCause.CONTACT)||event.getCause().equals(EntityDamageEvent.DamageCause.SUFFOCATION)||event.getCause().equals(EntityDamageEvent.DamageCause.HOT_FLOOR) )   ){
                living.setMaximumNoDamageTicks(5);
                living.setNoDamageTicks(5);
            }
        }
    }
    @EventHandler
    public void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event){
        if(!event.getNewGameMode().equals(GameMode.CREATIVE)&&!event.getNewGameMode().equals(GameMode.SPECTATOR)) {
            if (getjobject(event.getPlayer()).max_curseenergy > 1000) {
                event.getPlayer().setAllowFlight(true);
            } else {
                event.getPlayer().setAllowFlight(false);
            }
        }

    }
    @EventHandler
    public void onLightningStrikeEvent(LightningStrikeEvent event){
        if(!event.getLightning().isEffect()) {
            for (Jobject jobject : jobjects) {
                if (jobject.naturaltech.equals("infinity")) {
                    for (Jujut jujut : jobject.jujuts) {
                        if (jujut instanceof Infinity_passive ip) {
                            double dx = event.getLightning().getLocation().getX() - jobject.user.getLocation().getX();
                            double dz = event.getLightning().getLocation().getZ() - jobject.user.getLocation().getZ();
                            double dist = Math.pow(Math.pow(dx, 2) + Math.pow(dz, 2), 0.5);
                            if (dist < ip.use_power+3) {
                                event.getLightning().teleport(jobject.user.getLocation().add(dx, ip.use_power - dist + 2, dz));
                                event.getWorld().strikeLightningEffect(event.getLightning().getLocation());
                                event.getLightning().setLifeTicks(0);
                                ip.defending(event.getLightning(),'l');
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event){
        Player player=event.getPlayer();
        Jobject a_jobject = getjobject(player);
        if(a_jobject!=null&&a_jobject.infinity_stun_tick>0){
            event.setCancelled(true);
            return;
        }
        Action action=event.getAction();
        ItemStack item=player.getItemInHand();
        if(action.isRightClick()&&event.getClickedBlock()!=null){
            Block block = event.getClickedBlock();
            Material material = block.getType();

            // 1. GUI가 있는 블록 감지 (ex. 상자, 화로, 작업대 등)
            BlockState state = block.getState();
            if (state instanceof InventoryHolder) {
                return;
            }
            // 2. isInteractable()을 이용해 버튼, 레버, 문 등 감지
            if (material.isInteractable()) {
                return;
            }
        }
        if (item.hasItemMeta()&&(action.isRightClick()||action.isLeftClick())) {
            //int jo_num=PaperJJK.getjobject_num(player);
            Jobject eventjobject = getjobject(player);
            if(item.getItemMeta() instanceof BookMeta bookMeta && !bookMeta.getAuthor().isEmpty()){
                String[] name = bookMeta.getTitle().split("_");
                String item_name = PlainTextComponentSerializer.plainText().serialize(bookMeta.displayName());
                String[] values = PlainTextComponentSerializer.plainText().serialize(bookMeta.page(1)).split("/");
                if (name[0].equalsIgnoreCase("nct")) {
                    String nct_name = name[1];
                    PaperJJK.log("[JEvent] NCT book used - naturaltech: " + eventjobject.naturaltech + ", blocked: " + eventjobject.blocked);
                    eventjobject.setvalues(nct_name, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));
                    event.setCancelled(true);
                }
                else if(name[0].equalsIgnoreCase("unct")&& !eventjobject.blocked&&(action.isLeftClick()||action.isRightClick())){
                    PaperJJK.log("[JEvent] UNCT book used - naturaltech: " + eventjobject.naturaltech + ", blocked: " + eventjobject.blocked);
                    if(player.getCooldown(item.getType())==0) {
                        int cooldown = eventjobject.usejujut(name[1],item_name,values[0],(action.isLeftClick()),Integer.parseInt(values[1]), Integer.parseInt(values[2]), values[3].charAt(0),null);
                        PaperJJK.log("[JEvent] Technique cooldown: " + cooldown);
                        player.setCooldown(item.getType(), cooldown);
                    }
                } else if(name[0].equalsIgnoreCase("unct")) {
                    PaperJJK.log("[JEvent] UNCT blocked - naturaltech: " + eventjobject.naturaltech + ", blocked: " + eventjobject.blocked);
                }
            }
            else if(getItemTag(item).equals("cw_ish")&&player.getCooldown(item.getType())==0){
                player.setCooldown(item.getType(),eventjobject.usecw("ish",action.isLeftClick()));
            }
            else if(getItemTag(item).equals("cw_kamutoke")&&player.getCooldown(item.getType())==0){
                player.setCooldown(item.getType(),eventjobject.usecw("kamutoke",action.isLeftClick()));
            }
        }
    }
    //{"minecraft:custom_name":'{"extra":[{"bold":false,"color":"dark_purple","italic":false,"obfuscated":false,"strikethrough":false,"text":"Infinity","underlined":false}],"text":""}',"minecraft:written_book_content":{author:"ins",pages:[{raw:'{"extra":["50000000/10000/1/1/end"],"text":""}'}],title:{raw:"nct_infinity"}}}
    @EventHandler
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        Jobject a_jobject = getjobject(player);
        if(a_jobject!=null&&a_jobject.infinity_stun_tick>0){
            event.setCancelled(true);
            return;
        }
        ItemStack mainHandItem = event.getMainHandItem();
        ItemStack offHandItem = event.getOffHandItem();
        if (offHandItem.hasItemMeta()&&offHandItem.getItemMeta() instanceof BookMeta bookMeta && !bookMeta.getAuthor().isEmpty()) {
            Jobject eventjobject = getjobject(player);
            String item_name = PlainTextComponentSerializer.plainText().serialize(bookMeta.displayName());
            String[] values = PlainTextComponentSerializer.plainText().serialize(bookMeta.page(1)).split("/");
            if(player.getCooldown(offHandItem.getType())>0){
                event.setCancelled(true);
                return;
            }
            String[] name = bookMeta.getTitle().split("_");
            if(name[0].equalsIgnoreCase("unct")){
                if(eventjobject.fixjujut(item_name,player.isSneaking())){
                }
                event.setCancelled(true);
            }
            else if(name[0].equalsIgnoreCase("domain")){
                if(eventjobject.use_domain(values,player.isSneaking())){

                }
                event.setCancelled(true);
            }

        }
    }
    @EventHandler
    public void onPlayerItemHeldEvent(PlayerItemHeldEvent event){
        Player player=event.getPlayer();
        int newSlot=event.getNewSlot();
        int PreSlot=event.getPreviousSlot();
        ItemStack itemStack=player.getItemInHand();
        if (player.isSneaking()&&itemStack.hasItemMeta()&&itemStack.getItemMeta() instanceof BookMeta bookMeta && !bookMeta.getAuthor().isEmpty()) {
            String[] name = bookMeta.getTitle().split("_");
            String item_name = PlainTextComponentSerializer.plainText().serialize(bookMeta.displayName());
            String[] values = PlainTextComponentSerializer.plainText().serialize(bookMeta.page(1)).split("/");
            Jobject eventjobject = getjobject(player);
            //PaperJJK.log(content);
            if(name[0].equalsIgnoreCase("unct")){
                if((newSlot-PreSlot+9)%9>(PreSlot-newSlot+9)%9){
                    if(eventjobject.scroll(item_name,(-newSlot+PreSlot+9)%9)) event.setCancelled(true);
                }
                else {
                    if(eventjobject.scroll(item_name,-(-PreSlot+newSlot+9)%9)) event.setCancelled(true);
                }
            }

        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        event.setJoinMessage("sorcerer joined");
        Player joiner=event.getPlayer();
        joiner.setAllowFlight(true);
        UUID joineruuid=joiner.getUniqueId();
        PaperJJK.log("[JEvent] Player joining: " + joiner.getName() + " (UUID: " + joineruuid + ")");
        PaperJJK.log("[JEvent] Total jobjects in memory: " + jobjects.size());

        boolean dex=false;
        for(int r = 0; r< jobjects.size(); r++){
            if(jobjects.get(r).uuid.equals(joineruuid)){
                dex=true;
                PaperJJK.log("[JEvent] Found matching jobject at index " + r);
                PaperJJK.log("[JEvent] Jobject data - naturaltech: " + jobjects.get(r).naturaltech +
                             ", max_curseenergy: " + jobjects.get(r).max_curseenergy +
                             ", blocked: " + jobjects.get(r).blocked);

                jobjects.get(r).user=joiner;
                jobjects.get(r).player=joiner;

                // Jplayer로 타입 변환 (player가 null이었던 경우)
                if(!(jobjects.get(r) instanceof Jplayer)){
                    PaperJJK.log("[JEvent] Converting Jobject to Jplayer...");
                    Jobject oldData = jobjects.get(r);
                    Jplayer newJplayer = new Jplayer(joiner);
                    // 기존 데이터 복사
                    newJplayer.uuid = oldData.uuid;
                    newJplayer.naturaltech = oldData.naturaltech;
                    newJplayer.max_curseenergy = oldData.max_curseenergy;
                    newJplayer.curseenergy = oldData.curseenergy;
                    newJplayer.max_cursecurrent = oldData.max_cursecurrent;
                    newJplayer.cursecurrent = oldData.cursecurrent;
                    newJplayer.reversecurse = oldData.reversecurse;
                    newJplayer.reversecurse_out = oldData.reversecurse_out;
                    newJplayer.can_air_surface = oldData.can_air_surface;
                    newJplayer.black_flash_num = oldData.black_flash_num;
                    newJplayer.blocked = oldData.blocked;
                    newJplayer.innate_domain = oldData.innate_domain;
                    if(newJplayer.innate_domain != null){
                        newJplayer.innate_domain.owner = newJplayer;
                        PaperJJK.log("[JEvent] Transferred innate_domain - isbuilt: " + newJplayer.innate_domain.isbuilt +
                                     ", originbuilder: " + (newJplayer.innate_domain.originbuilder != null ? "exists" : "null") +
                                     ", block_count: " + (newJplayer.innate_domain.originbuilder != null ? newJplayer.innate_domain.originbuilder.block_count : 0));
                    }
                    newJplayer.domain = oldData.domain;
                    if(newJplayer.domain != null){
                        newJplayer.domain.jobject = newJplayer;
                    }
                    jobjects.set(r, newJplayer);
                    PaperJJK.log("[JEvent] Converted to Jplayer - naturaltech: " + newJplayer.naturaltech + ", blocked: " + newJplayer.blocked);
                } else {
                    PaperJJK.log("[JEvent] Already Jplayer instance");
                }

                if(getjobject(event.getPlayer()).max_curseenergy>1000) {
                    event.getPlayer().setAllowFlight(true);
                }
                else{
                    event.getPlayer().setAllowFlight(false);
                }
                PaperJJK.log("[JEvent] Player " + joiner.getName() + " reconnected with saved data");
                PaperJJK.log("[JEvent] Final check - naturaltech: " + getjobject(joiner).naturaltech +
                             ", innate_domain: " + (getjobject(joiner).innate_domain != null ? "exists" : "null"));
                break;
            }
        }
        if(!dex){
            PaperJJK.log("[JEvent] No existing data found, creating new Jplayer");
            jobjects.add(new Jplayer(joiner));
            event.setJoinMessage("new sorcerer joined");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PaperJJK.log("[JEvent] Player " + player.getName() + " quit");
    }

    public boolean useing_jujut(Player player, boolean leftclick,Entity entity){
        ItemStack item=player.getItemInHand();
        if (item.hasItemMeta()&&item.getItemMeta() instanceof BookMeta bookMeta && !bookMeta.getAuthor().isEmpty()) {
            int jo_num=PaperJJK.getjobject_num(player);
            Jobject eventjobject = getjobject(player);
            String[] name = bookMeta.getTitle().split("_");
            String item_name = PlainTextComponentSerializer.plainText().serialize(bookMeta.displayName());
            String[] values = PlainTextComponentSerializer.plainText().serialize(bookMeta.page(1)).split("/");
            if(name[0].equalsIgnoreCase("unct")&& !eventjobject.blocked){
                if(player.getCooldown(item.getType())==0) {
                    player.setCooldown(item.getType(), eventjobject.usejujut(name[1],item_name,values[0],(leftclick),Integer.parseInt(values[1]), Integer.parseInt(values[2]), values[3].charAt(0),entity));
                }
            }
            else if(name[0].equals("nct")&&!eventjobject.blocked&&!eventjobject.reversecurse_out&&eventjobject.user.isSneaking()&&entity instanceof LivingEntity living){
                eventjobject.reversecursing_out=true;
                eventjobject.reversecursing_out_entity=living;
                return true;
            }
        }
        else if(getItemTag(item).equals("cw_ish")&&player.getCooldown(item.getType())==0){
            player.setCooldown(item.getType(),getjobject(player).usecw("ish",true));
        }
        else if(getItemTag(item).equals("cw_kamutoke")&&player.getCooldown(item.getType())==0){
            player.setCooldown(item.getType(),getjobject(player).usecw("kamutoke",true));
        }
        return false;
    }
    public String getItemTag(ItemStack item) {
        if (item == null ||item.getItemMeta()==null) {
            //log("nnnull");
            return "";
        }

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(jjkplugin, "custom_tag");
        String rrr = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if(rrr==null){
            return "";
        }

        return rrr;
    }

}
