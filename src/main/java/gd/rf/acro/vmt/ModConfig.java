package gd.rf.acro.vmt;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=VMT.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModConfig {
    //template config from https://github.com/VsnGamer/ElevatorMod/blob/1.15.2/src/main/java/xyz/vsngamer/elevatorid/init/ModConfig.java
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final CommonGeneral GENERAL = new CommonGeneral(BUILDER);
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    public static class CommonGeneral
    {
        public final ForgeConfigSpec.BooleanValue farmerunion;
        public final ForgeConfigSpec.IntValue searchrange;
        CommonGeneral(ForgeConfigSpec.Builder builder)
        {
            builder.push("General");
            searchrange=builder.comment("Maximum search range, for detecting something is part of a village").defineInRange("searchrange",50,20,Integer.MAX_VALUE);
            farmerunion=builder.comment("Should villagers with no initial jobsite become farmers?").define("farmerunion",true);
            builder.pop();
        }
    }

    @SubscribeEvent
    public static void onLoad(final net.minecraftforge.fml.config.ModConfig.Loading event)
    {
       VMT.LOGGER.debug("loaded VMT config");
    }
}
