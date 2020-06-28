package gd.rf.acro.vmt.items;

import gd.rf.acro.vmt.ConfigUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.dynamic.GlobalPos;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class VillageInfoItem extends Item {
    private static int searchrange;
    private static boolean farmerunion;

    public VillageInfoItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        searchrange=Integer.parseInt(ConfigUtils.config.get("searchrange"));
        VillagerEntity entity = user.getEntityWorld().getEntities(
                VillagerEntity.class,
                new Box(user.getBlockPos().add(0-searchrange,0-searchrange,0-searchrange),user.getBlockPos().add(searchrange,searchrange,searchrange)),VillagerEntity::isAlive).get(0);

        if(entity!=null)
        {
            fixVillage(entity,user);
        }
        return super.use(world, user, hand);
    }

    private static void fixVillage(VillagerEntity villagerIn, PlayerEntity playerIn)
    {
        if(ConfigUtils.config.get("farmerunion").equals("true"))
        {
            farmerunion=true;
        }
        else
        {
            farmerunion=false;
        }
        searchrange=Integer.parseInt(ConfigUtils.config.get("searchrange"));
        BlockPos meetingpoint = getMeetingPoint(villagerIn);
        if(meetingpoint==null)
        {
            playerIn.sendMessage(new LiteralText("Cannot fix village, reason: no villager has found the meeting point"),true);
            return;
        }
        List<BlockPos> workstations = getJobSites(villagerIn.getBlockPos(),villagerIn.world);
        assignJobsAndMeetingPoint(playerIn,workstations,meetingpoint);
        System.out.println("Village fixed!");



    }
    //tries to find a villager who knows where the meeting point is
    private static BlockPos getMeetingPoint(VillagerEntity villager){
        if(villager.getBrain().hasMemoryModule(MemoryModuleType.MEETING_POINT))
        {
            return villager.getBrain().getOptionalMemory(MemoryModuleType.MEETING_POINT).get().getPos();
        }
        List<VillagerEntity> villagers = villager.getEntityWorld().getEntities(
                VillagerEntity.class,
                new Box(villager.getBlockPos().add(0-searchrange,0-searchrange,0-searchrange),villager.getBlockPos().add(searchrange,searchrange,searchrange)),VillagerEntity::isAlive);

        for (VillagerEntity citizen : villagers) {
            if(citizen.getBrain().hasMemoryModule(MemoryModuleType.MEETING_POINT))
            {
                
                return citizen.getBrain().getOptionalMemory(MemoryModuleType.MEETING_POINT).get().getPos();
            }
        }

        return searchForBell(villager);
    }
    //Fallback method if no villagers know where the meeting point is (usually because they are children)
    private static BlockPos searchForBell(VillagerEntity villagerEntity)
    {
        World world = villagerEntity.getEntityWorld();
        BlockPos pos = villagerEntity.getBlockPos();
        for (int i = pos.getX()-searchrange; i < pos.getX()+searchrange; i++) {
            for (int j = pos.getY()-25; j < pos.getY()+25; j++) {
                for (int k = pos.getZ()-searchrange; k < pos.getZ()+searchrange; k++) {
                    if(world.getBlockState(new BlockPos(i,j,k)).getBlock()== Blocks.BELL)
                    {
                        return new BlockPos(i,j,k);
                    }
                }
            }
        }
        return null;
    }
    private static List<BlockPos> searchForBeds(PlayerEntity villagerEntity)
    {
        List<BlockPos> beds = new ArrayList<>();
        World world = villagerEntity.getEntityWorld();
        BlockPos pos = villagerEntity.getBlockPos();
        for (int i = pos.getX()-searchrange; i < pos.getX()+searchrange; i++) {
            for (int j = pos.getY()-25; j < pos.getY()+25; j++) {
                for (int k = pos.getZ()-searchrange; k < pos.getZ()+searchrange; k++) {
                    if(BlockTags.BEDS.contains(world.getBlockState(new BlockPos(i,j,k)).getBlock()))
                    {
                       beds.add(new BlockPos(i,j,k));
                    }
                }
            }
        }
        return beds;
    }


    private static final List<String> JOB_SITES = Arrays.asList(
            Blocks.COMPOSTER.getTranslationKey(),
            Blocks.BLAST_FURNACE.getTranslationKey(),
            Blocks.BARREL.getTranslationKey(),
            Blocks.LECTERN.getTranslationKey(),
            Blocks.CARTOGRAPHY_TABLE.getTranslationKey(),
            Blocks.SMOKER.getTranslationKey(),
            Blocks.BREWING_STAND.getTranslationKey(),
            Blocks.GRINDSTONE.getTranslationKey(),
            Blocks.FLETCHING_TABLE.getTranslationKey(),
            Blocks.STONECUTTER.getTranslationKey(),
            Blocks.CAULDRON.getTranslationKey(),
            Blocks.LOOM.getTranslationKey(),
            Blocks.SMITHING_TABLE.getTranslationKey());
    private static List<BlockPos> farms;
    private static List<BlockPos> getJobSites(BlockPos pos, World world)
    {
        List<BlockPos> list = new ArrayList<>();
        farms=new ArrayList<>();
        for (int i = pos.getX()-searchrange; i < pos.getX()+searchrange; i++) {
            for (int j = pos.getY()-25; j < pos.getY()+25; j++) {
                for (int k = pos.getZ()-searchrange; k < pos.getZ()+searchrange; k++) {
                    String block = world.getBlockState(new BlockPos(i,j,k)).getBlock().getTranslationKey();

                    if(JOB_SITES.contains(block))
                    {
                        list.add(new BlockPos(i,j,k));
                        if(farmerunion && block.equals(Blocks.COMPOSTER.getTranslationKey()))
                        {
                            farms.add(new BlockPos(i,j,k));
                        }
                    }
                }
            }
        }
        return list;
    }
    //actually does most of the fixing
    private static void assignJobsAndMeetingPoint(PlayerEntity playerEntity,List<BlockPos> worksites, BlockPos point)
    {
        List<BlockPos> beds = searchForBeds(playerEntity);
        List<VillagerEntity> villagers = playerEntity.getEntityWorld().getEntities(
                VillagerEntity.class,
                new Box(playerEntity.getBlockPos().add(0-searchrange,0-searchrange,0-searchrange),playerEntity.getBlockPos().add(searchrange,searchrange,searchrange)),VillagerEntity::isAlive);
        villagers.forEach(citizen->
        {
            citizen.getBrain().method_29519(MemoryModuleType.MEETING_POINT, GlobalPos.create(playerEntity.world.getRegistryKey(),point));
            citizen.addScoreboardTag("vmt_fixed");

            if(!citizen.getBrain().hasMemoryModule(MemoryModuleType.JOB_SITE))
            {
                if(worksites.size()>0)
                {
                    citizen.getBrain().method_29519(MemoryModuleType.JOB_SITE,GlobalPos.create(playerEntity.world.getRegistryKey(),worksites.remove(RandomUtils.nextInt(0,worksites.size()))));

                }
                else
                {
                    if(farmerunion && farms.size()>0)
                    {
                        BlockPos newfarm = farms.remove(0).up();
                        playerEntity.getEntityWorld().setBlockState(newfarm,Blocks.COMPOSTER.getDefaultState());
                        citizen.getBrain().method_29519(MemoryModuleType.JOB_SITE,GlobalPos.create(playerEntity.world.getRegistryKey(),newfarm));
                        farms.add(newfarm);

                    }

                }
            }
            citizen.getBrain().method_29519(MemoryModuleType.HOME,GlobalPos.create(playerEntity.world.getRegistryKey(),beds.remove(RandomUtils.nextInt(0,beds.size()))));
        });
    }
}
