package com.stebars.waterordeath;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;


@Mod(WaterOrDeath.MODID)
public class WaterOrDeath {
	final static String MODID = "waterordeath";

	public WaterOrDeath() {
		MinecraftForge.EVENT_BUS.register(this);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, OptionsHolder.COMMON_SPEC);
	}

	// TODO currently dispensers with hoes just launch the hoe (same as vanilla), should change so they actually hoe the land
}
