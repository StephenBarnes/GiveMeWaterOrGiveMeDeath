package com.stebars.waterordeath;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;


public class OptionsHolder {
	public static class Common {	

	    public ConfigValue<Integer> startingMoisture;
	    public ConfigValue<Integer> reduceMoistureRate;
	    public ConfigValue<Integer> moistureLoss;
	    public ConfigValue<Integer> waterRangeHorizontal;
	    public ConfigValue<Integer> waterRangeUp;
	    public ConfigValue<Integer> waterRangeDown;
	    public ConfigValue<Boolean> dryingCropsStillDrop;
	    public ConfigValue<Boolean> dryCropsDie;
	    
		public Common(ForgeConfigSpec.Builder builder) {
	        startingMoisture = builder.comment("Moisture of farmland blocks when created (e.g. with a hoe). Max moisture is 7.")
	        		.define("startingMoisture", 2);
	        reduceMoistureRate = builder.comment("Rate at which moisture reduces. Each random tick, there's a "
	        		+ "[this value] / 10,000 chance that moisture rate will reduce by 1 if there's no nearby water.")
	        		.define("reduceMoistureRate", 10000);
	        moistureLoss = builder.comment("Moisture reduction per random tick without nearby water.")
	        		.define("moistureLoss", 1);
	        waterRangeHorizontal = builder.comment("How far horizontally a farmland block can be from water and still be "
	        		+ "hydrated. In vanilla, this is 4. Note that if this is 4, it means if we can start at the farmland "
	        		+ "and move up to 4 blocks west/east and THEN ALSO up to 4 blocks north/south, it will be hydrated.")
	        		.define("waterRangeHorizontal", 4);
	        waterRangeUp = builder.comment("How far up a farmland block can be from water and still be hydrated. In "
	        		+ "vanilla, this is 0.")
	        		.define("waterRangeUp", 0);
	        waterRangeDown = builder.comment("How far down a farmland block can be from water and still be hydrated. In "
	        		+ "vanilla, this is 1.")
	        		.define("waterRangeDown", 1);
	        dryingCropsStillDrop = builder.comment("Whether broken crops drop seeds. If false, crops just break, nothing "
	        		+ "gets dropped.")
	        		.define("dryingCropsStillDrop", false);
	        dryCropsDie = builder.comment("Whether soil that drops to zero moisture and dries further will kill crops and "
	        		+ "turn into dirt. In vanilla, this is false.")
	        		.define("dryCropsDie", true);
		}
	}

	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;

	static { //constructor
		Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON = commonSpecPair.getLeft();
		COMMON_SPEC = commonSpecPair.getRight();
	}
}