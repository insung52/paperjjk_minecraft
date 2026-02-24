package org.justheare.paperJJK;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Jdomain_innate extends Jdomain{
    public boolean isexpanded=false;
    boolean isexpanding=false;
    Jdomain_expand expanded_domain;
    Jdomain_expand attack_target=null;
    Jobject attacker;
    ArrayList<LivingEntity> domain_targets;
    public boolean no_border_on=false;
    Location nb_location;
    boolean onground=false;
    int nb_range;
    Jdomain_effector effector;
    Material innate_border=Material.GLASS;
    public Jdomain_innate(Jobject owner) {
        super(owner);
    }
    void expand_effect(boolean nb){

    }
    void tp_effect(){

    }
    public boolean build_expand(int range){//영역 전개. 근처 결있영 체크
        if(level<3){
            owner.user.sendMessage("you can't expand domain!");
            return false;
        }
        if(isexpanded||isexpanding){
            owner.user.sendMessage("already expanded domain!");
            return false;
        }
        if(!isbuilt){
            owner.user.sendMessage("you don't have innate domain!");
            return false;
        }
        if(owner.cursed_tech_block_tick>0){
            owner.user.sendMessage("your cursed technic burnt");
            return false;
        }
        if(owner.infinity_stun_tick>0){
            owner.user.sendMessage("you can't do anything");
            return false;
        }
        owner.curseenergy-=30000;
        onground=false;
        owner.user.sendMessage("domain expansion...");
        expand_effect(false);
        boolean close=false;

        for(Jdomain_expand expand : PaperJJK.expanded_domains){
            if(expand.owner.innate_domain.domain_targets.contains(owner.user)){
                owner.user.sendMessage("you're now attacking other domain.");
                attack_target=expand;

                expand.owner.innate_domain.attacker=owner;
                close=true;
                expand.manager.level_diff=expand.owner.innate_domain.level-owner.innate_domain.level;
                break;
            }
            if(expand.location.getWorld() == owner.user.getLocation().getWorld()){
                if(expand.range+range>expand.location.distance(owner.user.getLocation())){
                    //attack
                    owner.user.sendMessage("you're now attacking other domain.");
                    attack_target=expand;

                    expand.owner.innate_domain.attacker=owner;
                    close=true;
                    expand.manager.level_diff=expand.owner.innate_domain.level-owner.innate_domain.level;

                    ArrayList<LivingEntity> aa= new ArrayList<>();
                    aa.add(owner.player);
                    expand.translate(aa,expand.location, expand.range, expand.owner.innate_domain.location,expand.owner.innate_domain.range);
                    break;
                }
            }
        }
        if(!close){
            //expand
            Jdomain_expand expand=new Jdomain_expand(owner,range);
            expanded_domain=expand;
            expand.expand();
            PaperJJK.expanded_domains.add(expand);
        }
        start_effect();
        return true;
    }
    public boolean destroy_expand(){  //영역 해제,
        if(isexpanded&&!isexpanding){
            Bukkit.getScheduler().cancelTask(effector.tasknum);
            if(attacker==null){
                ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) location.getNearbyLivingEntities(range);
                ArrayList<LivingEntity> tee=new ArrayList<>();
                for(LivingEntity living : tentities){
                    if(living.getLocation().distance(location)>=range){
                        continue;
                    }
                    tee.add(living);
                }
                PaperJJK.expanded_domains.remove(expanded_domain);
                expanded_domain.destroy();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        expanded_domain.translate(tee,location,range+3,expanded_domain.location, expanded_domain.range);
                    }
                }.runTaskLater(PaperJJK.jjkplugin, 2L);

            }
            else if(!attacker.innate_domain.no_border_on){  //결있영에 공격받는중 이였다면 공격자로 이동
                if(attacker.innate_domain.attack_target!=null){
                    isexpanded=false;
                    expanded_domain.owner=attacker;
                    attacker.innate_domain.isexpanded=true;
                    ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) location.getNearbyLivingEntities(range+3);
                    ArrayList<LivingEntity> tee=new ArrayList<>();
                    for(LivingEntity living : tentities){
                        if(living.getLocation().distance(location)>=range+3){
                            continue;
                        }
                        tee.add(living);
                    }
                    attacker.innate_domain.domain_targets=tee;
                    expanded_domain.translate(tee,location,range+3,attacker.innate_domain.location,attacker.innate_domain.range);
                    attacker.innate_domain.attack_target=null;
                    attacker.innate_domain.expanded_domain=expanded_domain;
                    attacker.innate_domain.start_effect();
                    attacker=null;

                }
                else {
                    ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) location.getNearbyLivingEntities(range+3);
                    ArrayList<LivingEntity> tee=new ArrayList<>();
                    for(LivingEntity living : tentities){
                        if(living.getLocation().distance(location)>=range+3){
                            continue;
                        }
                        tee.add(living);
                    }
                    PaperJJK.expanded_domains.remove(expanded_domain);
                    expanded_domain.translate(tee,location,range+3,expanded_domain.location, expanded_domain.range);
                    expanded_domain.destroy();
                }

            }
            else{   //걸없영에 공격받던중
                ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) location.getNearbyLivingEntities(range+3);
                ArrayList<LivingEntity> tee=new ArrayList<>();
                for(LivingEntity living : tentities){
                    if(living.getLocation().distance(location)>=range+3){
                        continue;
                    }
                    tee.add(living);
                }
                expanded_domain.destroy();
                PaperJJK.expanded_domains.remove(expanded_domain);
                attacker.innate_domain.attack_target=null;
                //PaperJJK.log("destroy translate : "+location.getX() + " " + location.getY() + " "  + location.getZ() + " -> "+expanded_domain.location.getX() + " " + expanded_domain.location.getY() + " "  + expanded_domain.location.getZ());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        expanded_domain.translate(tee,location,range+3,expanded_domain.location, expanded_domain.range);
                    }
                }.runTaskLater(PaperJJK.jjkplugin, 2L);
                attacker = null;
            }
        }
        else if(attack_target!=null){   //공격중이였다면 공격 해제
            attack_target.owner.innate_domain.attacker=null;
            attack_target=null;
        }
        else {
            owner.user.sendMessage("no domain to destroy!");
            return false;
        }
        owner.disablejujut();
        owner.player.setCooldown(Material.WRITTEN_BOOK,20*30);
        owner.cursed_tech_block_tick = 20*30;
        return true;
    }
    public boolean drow_expand(int range){//결없영 전개
        if(level<10){
            owner.user.sendMessage("you can't open domain border!");
            return false;
        }
        onground=true;
        if(isexpanded||isexpanding){
            owner.user.sendMessage("already expanded domain!");
            return false;
        }
        if(owner.cursed_tech_block_tick>0){
            owner.user.sendMessage("your cursed technic burnt");
            return false;
        }
        if(owner.infinity_stun_tick>0){
            owner.user.sendMessage("you can't do anything");
            return false;
        }
        current_radius=0;
        no_border_on=true;
        isexpanded=true;
        nb_range=range;
        owner.curseenergy-=30000;
        owner.user.sendMessage("domain expansion...");
        expand_effect(true);
        boolean close=false;
        nb_location=owner.user.getLocation();
        for(Jdomain_expand expand : PaperJJK.expanded_domains){
            if(expand.location.getWorld() == owner.user.getWorld()){
                if(expand.range+nb_range>expand.location.distance(owner.user.getLocation())){
                    //attack
                    owner.user.sendMessage("you're now attacking other domain.");
                    attack_target=expand;
                    expand.owner.innate_domain.attacker=owner;
                    nb_location=expand.location;
                    ArrayList<LivingEntity> aa= new ArrayList<>();
                    aa.add(owner.player);
                    //expand.translate(aa,expand.location, expand.range, expand.owner.innate_domain.location,expand.owner.innate_domain.range);
                    close=true;
                    break;
                }
            }
            if(expand.owner.innate_domain.domain_targets.contains(owner.user)){
                owner.user.sendMessage("you're now attacking other domain.");
                attack_target=expand;
                expand.owner.innate_domain.attacker=owner;
                nb_location=expand.location;
                close=true;
                break;
            }
        }
        start_effect();
        return true;
    }
    public boolean undrow_expand(){
        if(no_border_on&&isexpanded){
            if(attack_target!=null){
                attack_target.owner.innate_domain.attacker=null;
                attack_target=null;
            }
            no_border_on=false;
            isexpanded=false;
            Bukkit.getScheduler().cancelTask(effector.tasknum);
            owner.disablejujut();
            owner.player.setCooldown(Material.WRITTEN_BOOK,20*30);
            owner.cursed_tech_block_tick = 20*30;
            return true;
        }
        owner.user.sendMessage("no domain to destroy!");
        return false;
    }
    void build(){
        if(isbuilding){
            owner.user.sendMessage("already building!");
        }
        else if(isbuilt){
            owner.user.sendMessage("already built!");
        }
        else{
            owner.user.sendMessage("innate domain build started...");
            isbuilding=true;
            location = owner.user.getLocation();
            originbuilder = new Jdomain_Builder(this,range,innate_border,owner.user.getLocation());
            originbuilder.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, originbuilder, 1, 1);
        }
    }
    void destroy(){
        if(isbuilding){
            owner.user.sendMessage("already building!");
        }
        else if(!isbuilt){
            owner.user.sendMessage("not built!");
        }
        else{
            owner.user.sendMessage("innate domain destroy started...");
            isbuilding=true;
            isbuilt=false;

            // block_count가 0이면 (데이터 로드로 복원된 경우) 블록 데이터가 없음
            if(originbuilder.block_count == 0){
                //owner.user.sendMessage(ChatColor.YELLOW + "Warning: No block data saved. Domain will be marked as destroyed but blocks may remain.");
                //owner.user.sendMessage(ChatColor.YELLOW + "You can manually break the blocks or rebuild the domain.");
                isbuilding=false;
                destroy_finished();
            } else {
                originbuilder.build_mode=false;
                originbuilder.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, originbuilder, 1, 1);
            }
        }
    }
    void start_effect(){
        Jdomain_effector effector = new Jdomain_effector(this);
        this.effector=effector;
        effector.tick=0;
        effector.tasknum = Bukkit.getScheduler().scheduleSyncRepeatingTask(PaperJJK.jjkplugin, effector, 1, 1);
    }
    void set_special(){
    }

    @Override
    void build_finished(){
        super.build_finished();
        // 블록 데이터 저장
        if(originbuilder != null && originbuilder.block_count > 0){
            JDomainData.saveDomainBlocks(owner.uuid, originbuilder);
        }
    }

    @Override
    void destroy_finished(){
        super.destroy_finished();
        // 블록 데이터 삭제 (선택사항 - 필요하면 나중에 재사용 가능)
        // JDomainData.deleteDomainBlocks(owner.uuid);
    }
}
class Jdomain_Builder implements Runnable{
    int tasknum;
    int delay=20;
    boolean wait=false;
    boolean build_mode=true;
    Location location;
    int speed=2000;
    double tick1=0;
    double tick2=0;
    Jdomain domain;
    int radius;
    Material border_material;
    int rx,ry,rz;
    int block_count=0;
    Block[] saved_blocks;
    Material[] saved_material;
    BlockData[] saved_blockdata;
    BlockState[] saved_blockstate;
    Set<BlockPos> processedBlocks = new HashSet<>();
    //Vector[]
    public Jdomain_Builder(Jdomain domain, int radius, Material border_material,Location location){
        this.domain=domain;
        this.radius=radius;
        this.border_material=border_material;
        this.location=location;
        saved_blocks=new Block[radius*radius*70];
        saved_material=new Material[radius*radius*70];
        saved_blockdata=new BlockData[radius*radius*70];
        saved_blockstate=new BlockState[radius*radius*70];
    }
    @Override
    public void run() {
        if(delay>0){
            delay--;
        }
        if(delay==0&&wait){
            Bukkit.getScheduler().cancelTask(tasknum);
            domain.build_finished();
            wait=false;
        }
        if(!wait || !build_mode){
            //50 * 40
            for(int r=0; r<40*radius; r++){
                if(build_mode){
                    rx=(int) Math.round(Math.sin(tick1 /radius/4*Math.PI)*Math.sin(tick2 /radius/4*Math.PI)*radius);
                    ry=(int) Math.round(Math.cos(tick2 /radius/4*Math.PI)*radius);
                    rz=(int) Math.round(Math.cos(tick1 /radius/4*Math.PI)*Math.sin(tick2 /radius/4*Math.PI)*radius);

                    BlockPos pos = new BlockPos(rx, ry, rz);

                    if(processedBlocks.contains(pos)){
                        tick1++;
                        if(tick1==radius*8){

                            tick1=0;
                            tick2++;
                            if(tick2==radius*4){
                                block_count--;
                                wait=true;
                                break;
                            }
                        }
                        continue;
                    }
                    Location tlocation=location.clone().add(rx,ry,rz);
                    saved_blocks[block_count]=tlocation.getBlock();
                    saved_material[block_count]=tlocation.getBlock().getType();
                    //PaperJJK.log(tlocation.getBlock().getType()+"");
                    saved_blockdata[block_count]=tlocation.getBlock().getBlockData();
                    saved_blockstate[block_count]=tlocation.getBlock().getState();
                    tlocation.getBlock().setType(border_material);
                    block_count++;
                    // 처리 완료 좌표 저장
                    processedBlocks.add(pos);
                    tick1++;
                    if(tick1==radius*8){

                        tick1=0;
                        tick2++;
                        if(tick2==radius*8){
                            block_count--;
                            wait=true;
                            break;
                        }
                    }
                }
                else{
                    if(block_count>0){
                        block_count--;
                        //PaperJJK.log("Replace : "+saved_material[block_count].name());
                        saved_blocks[block_count].setType(saved_material[block_count]);
                        saved_blocks[block_count].setBlockData(saved_blockdata[block_count]);
                        if(Math.random()<0.01){
                            saved_blocks[block_count].getWorld().playSound(saved_blocks[block_count].getLocation(), Sound.BLOCK_GLASS_BREAK, 5F, 0.7F);
                        }
                        if (saved_blockstate[block_count] instanceof Chest) {
                            Chest chest = (Chest) saved_blockstate[block_count];
                            Inventory chestInventory = chest.getInventory();
                            chestInventory.setContents(((Chest) saved_blockstate[block_count]).getInventory().getContents());  // 상자 내부 아이템 복원
                        }
                        BlockState state = saved_blockstate[block_count];
                        if(state != null){
                            state.update(true);
                        }
                    }
                    else{
                        domain.destroy_finished();
                        Bukkit.getScheduler().cancelTask(tasknum);
                        break;
                    }
                }
            }
        }
    }
}
// ========================
// 블록 좌표 클래스
// ========================
class BlockPos {
    final int x, y, z;

    BlockPos(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof BlockPos)) return false;
        BlockPos other = (BlockPos)o;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode(){
        return Objects.hash(x, y, z);
    }
}

class Jdomain_effector implements Runnable{
    int tasknum;
    int tick;
    Jdomain_innate domain;
    Jdomain_effector (Jdomain_innate domain){
        this.domain=domain;
        tick=0;
    }
    /*
    public void difDomainTargets(ArrayList<Entity> newTargets){
        for(Entity newTarget : newTargets){
            if(newTarget instanceof LivingEntity living){
                Jobject jobject = PaperJJK.getjobject(living);
                if(jobject!=null){

                }
            }
        }
    }
    public void getTargetInRange(){
        ArrayList<Entity> tentities = (ArrayList<Entity>) domain.nb_location.getNearbyEntities(domain.nb_range,domain.nb_range,domain.nb_range);
        ArrayList<Entity> tee=new ArrayList<>();
        for(Entity living : tentities) {
            if (living.equals(domain.owner.user)) {
                continue;
            }
            if (living instanceof BlockDisplay) {
                continue;
            }
            if (living.getLocation().distance(domain.nb_location) < domain.current_radius) {
                Jobject jobject = PaperJJK.getjobject(living);
                if (jobject != null && jobject.user instanceof Player player && SimpleDomainManager.isActive(player)) {
                    // Simple domain is active - ignore sure-hit effect
                    SimpleDomainManager.decreasePower(player, domain.level * 4);
                    tee.add(living);
                    continue;
                }
                tee.add(living);
            }
            difDomainTargets(tee);
        }
    }
    public void getLivingTargetInRange(){
        ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) domain.location.getNearbyLivingEntities(domain.range+1);
        ArrayList<Entity> tee=new ArrayList<>();
        for(LivingEntity living : tentities) {
            if (living.equals(domain.owner.user)) {
                continue;
            }
            if (living instanceof BlockDisplay) {
                continue;
            }
            if (living.getLocation().distance(domain.location) <= domain.range + 1) {

                Jobject jobject = PaperJJK.getjobject(living);
                if(jobject!=null && jobject.user instanceof Player player && SimpleDomainManager.isActive(player)){
                    // Simple domain is active - ignore sure-hit effect
                    SimpleDomainManager.decreasePower(player, domain.level*4);
                    tee.add(living);
                    continue;
                }
                if(jobject!=null && jobject.naturaltech.equals("physical_gifted")){
                    continue;
                }
                tee.add(living);
            }
            difDomainTargets(tee);
        }
    }*/
    public void breakSimpleDomain(Player target,  int mul){
        // Todo : decreasePower
        SimpleDomainManager.decreasePower(target, domain.level*mul);
    }
    public void effect_tick(){
        if(domain.no_border_on){
            if(domain.current_radius< domain.nb_range){
                domain.current_radius++;
            }
            ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) domain.nb_location.getNearbyLivingEntities(domain.current_radius);
            for(LivingEntity living : tentities){
                if(living.getLocation().distance(domain.nb_location)>= domain.nb_range){
                    continue;
                }
                if(living.equals(domain.owner.user)){
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,5,2));
                }
                else {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,5,0));
                }
            }
        }
        else if(domain.attacker==null){
            for(LivingEntity living : domain.domain_targets){
                if(living.equals(domain.owner.user)){
                    living.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,5,2));
                }
                else {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,5,0));
                }
            }
        }
    }
    @Override
    public void run() {
        if(domain.isexpanded){
            tick++;
            effect_tick();
            if(!domain.no_border_on&&tick%3==0){
                ArrayList<LivingEntity> tentities = (ArrayList<LivingEntity>) domain.location.getNearbyLivingEntities(domain.range+3);

                for(LivingEntity living : tentities){
                    if(living.getLocation().distance(domain.location)>=domain.range+3){
                        continue;
                    }
                    if(domain.attacker!=null){
                        living.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,30,0));
                    }
                }
            }
            if(domain.no_border_on){
                if(domain.owner.user instanceof LivingEntity living){
                    if(living.getHealth()<living.getMaxHealth()*0.5){
                        living.sendMessage("too low health!");
                        domain.owner.innate_domain.undrow_expand();
                    }
                }
            }
        }
        if(domain.tp_delay>0){
            domain.tp_delay--;
            if(domain.tp_delay==1) {
                domain.tp_effect();
            }
        }
    }
}