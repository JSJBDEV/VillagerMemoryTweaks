package gd.rf.acro.vmt;

import gd.rf.acro.vmt.items.VillageInfoItem;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;

public class VMT implements ModInitializer {
	public static final VillageInfoItem VILLAGE_INFO_ITEM = new VillageInfoItem(new Item.Settings().group(ItemGroup.MISC));
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registry.ITEM,"vmt:village_info_item",VILLAGE_INFO_ITEM);
		System.out.println("Hello Fabric world!");
		ConfigUtils.checkConfigs();
	}
}
