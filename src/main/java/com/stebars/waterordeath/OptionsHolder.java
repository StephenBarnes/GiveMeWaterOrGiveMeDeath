package com.stebars.waterordeath;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;


public class OptionsHolder {
	public static class Common {	

		public Map<Biome.Category, ConfigValue<Integer>> reduceMoistureRate = new HashMap<Biome.Category, ConfigValue<Integer>>();
		public Map<Biome.Category, ConfigValue<Integer>> moistureLoss = new HashMap<Biome.Category, ConfigValue<Integer>>();
		public ConfigValue<Integer> waterRangeHorizontal;
		public ConfigValue<Integer> waterRangeUp;
		public ConfigValue<Integer> waterRangeDown;
		public ConfigValue<Boolean> dryingCropsStillDrop;
		public ConfigValue<Boolean> dryCropsDie;

		public Common(ForgeConfigSpec.Builder builder) {
			builder.comment("Rate at which moisture reduces, by biome category. Each random tick, there's a "
					+ "[this value] / 10,000 chance that moisture rate will drop by the moistureLoss value (below) if there's no nearby water.")
			.push("reduceMoistureRate");
			for (Biome.Category category : Biome.Category.values()) {
				reduceMoistureRate.put(category, builder.define(category.toString(), defaultReduceMoistureRate(category)));
			}
			builder.pop();

			builder.comment("Moisture reduction per moisture loss tick without nearby water, by biome category.")
			.push("moistureLoss");
			for (Biome.Category category : Biome.Category.values()) {
				moistureLoss.put(category, builder.define(category.toString(), defaultMoistureLoss(category)));
			}
			builder.pop();

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

		private int defaultReduceMoistureRate(Biome.Category c) {
			switch(c) {
			case NONE:
			case TAIGA:
			case EXTREME_HILLS:
			case MESA:
			case PLAINS:
			case SAVANNA:
			case ICY:
			case THEEND:
			case DESERT:
			case NETHER:
				return 10000;
			case FOREST:
			case MUSHROOM:
				return 7000;
			case JUNGLE:
				return 3000;
			case BEACH:
			case SWAMP:
				return 2000;
			case OCEAN:
			case RIVER:
				return 1000;
			default:
				return 10000;
			}
		}

		private int defaultMoistureLoss(Biome.Category c) {
			switch(c) {
			case NETHER:
				return 4;
			case DESERT:
			case MESA:
				return 3;
			case TAIGA:
			case SAVANNA:
			case ICY:
			case THEEND:
				return 2;
			case PLAINS:
			case FOREST:
			case MUSHROOM:
			case EXTREME_HILLS:
			case NONE:
			case JUNGLE:
			case BEACH:
			case SWAMP:
			case OCEAN:
			case RIVER:
			default:
				return 1;
			}
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