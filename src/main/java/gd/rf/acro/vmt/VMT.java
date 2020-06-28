package gd.rf.acro.vmt;


import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(VMT.MODID)
public class VMT {
    public static final String MODID = "vmt";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public VMT()
    {
        LOGGER.debug("*villager noises intensify*");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, gd.rf.acro.vmt.ModConfig.SPEC);
    }
}
