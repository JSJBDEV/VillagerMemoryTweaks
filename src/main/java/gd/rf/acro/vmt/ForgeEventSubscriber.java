package gd.rf.acro.vmt;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.system.CallbackI;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(bus= Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {
    private static int searchrange;
    private static boolean farmerunion;

    @SubscribeEvent
    public static void village(PlayerInteractEvent.EntityInteractSpecific event)
    {
        if(event.getTarget().getType()== EntityType.VILLAGER && !event.getTarget().getTags().contains("vmt_fixed") && event.getHand()== Hand.MAIN_HAND && !event.getPlayer().getEntityWorld().isRemote)
        {
            System.out.println("starting village fix at"+event.getPlayer().getPosition().toString());
            fixVillage((VillagerEntity) event.getTarget(),event.getPlayer());
        }

    }
    
    private static void fixVillage(VillagerEntity villagerIn, PlayerEntity playerIn)
    {
        farmerunion=ModConfig.GENERAL.farmerunion.get();
        searchrange=ModConfig.GENERAL.searchrange.get();
        BlockPos meetingpoint = getMeetingPoint(villagerIn);
        if(meetingpoint==null)
        {
            playerIn.sendMessage(new StringTextComponent("Cannot fix village, reason: no villager has found the meeting point"));
            return;
        }
        List<BlockPos> workstations = getJobSites(villagerIn.getPosition(),villagerIn.world);
        assignJobsAndMeetingPoint(playerIn,workstations,meetingpoint);
        System.out.println("Village fixed!");



    }
    //tries to find a villager who knows where the meeting point is
    private static BlockPos getMeetingPoint(VillagerEntity villager){
        if(villager.getBrain().hasMemory(MemoryModuleType.MEETING_POINT))
        {
            return villager.getBrain().getMemory(MemoryModuleType.MEETING_POINT).get().getPos();
        }
        List<VillagerEntity> villagers = villager.getEntityWorld().getEntitiesWithinAABB(
                VillagerEntity.class,
                new AxisAlignedBB(villager.getPosition().add(0-searchrange,0-searchrange,0-searchrange),villager.getPosition().add(searchrange,searchrange,searchrange)));
        for (VillagerEntity citizen : villagers) {
            if(citizen.getBrain().hasMemory(MemoryModuleType.MEETING_POINT))
            {
                return citizen.getBrain().getMemory(MemoryModuleType.MEETING_POINT).get().getPos();
            }
        }

        return searchForBell(villager);
    }
    //Fallback method if no villagers know where the meeting point is (usually because they are children)
    private static BlockPos searchForBell(VillagerEntity villagerEntity)
    {
        World world = villagerEntity.getWorld();
        BlockPos pos = villagerEntity.getPosition();
        for (int i = pos.getX()-searchrange; i < pos.getX()+searchrange; i++) {
            for (int j = pos.getY()-25; j < pos.getY()+25; j++) {
                for (int k = pos.getZ()-searchrange; k < pos.getZ()+searchrange; k++) {
                    if(world.getBlockState(new BlockPos(i,j,k)).getBlock()==Blocks.BELL)
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
        BlockPos pos = villagerEntity.getPosition();
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
        List<VillagerEntity> villagers = playerEntity.getEntityWorld().getEntitiesWithinAABB(
                VillagerEntity.class,
                new AxisAlignedBB(playerEntity.getPosition().add(0-searchrange,0-searchrange,0-searchrange),playerEntity.getPosition().add(searchrange,searchrange,searchrange)));
       villagers.forEach(citizen->
       {
           citizen.getBrain().setMemory(MemoryModuleType.MEETING_POINT,GlobalPos.of(DimensionType.OVERWORLD,point));
           citizen.addTag("vmt_fixed");

           if(!citizen.getBrain().hasMemory(MemoryModuleType.JOB_SITE))
           {
               if(worksites.size()>0)
               {
                   citizen.getBrain().setMemory(MemoryModuleType.JOB_SITE,GlobalPos.of(DimensionType.OVERWORLD,worksites.remove(0)));

               }
               else
               {
                   if(farmerunion && farms.size()>0)
                   {
                       BlockPos newfarm = farms.remove(0).up();
                       playerEntity.getEntityWorld().setBlockState(newfarm,Blocks.COMPOSTER.getDefaultState());
                       citizen.getBrain().setMemory(MemoryModuleType.JOB_SITE,GlobalPos.of(DimensionType.OVERWORLD,newfarm));
                       farms.add(newfarm);

                   }

               }
           }
           citizen.getBrain().setMemory(MemoryModuleType.HOME,GlobalPos.of(playerEntity.world.dimension.getType(),beds.remove(RandomUtils.nextInt(0,beds.size()))));

       });
    }
}
