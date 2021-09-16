package com.stebars.waterordeath;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.MovingPistonBlock;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;

public class WoDFarmlandBlock extends FarmlandBlock {

	public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
	protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);
	public static final int REDUCE_MOISTURE_RATE_OUT_OF = 10000; // so rate = 10 means 10 out of 10000
	public static int reduceMoistureRate = OptionsHolder.COMMON.reduceMoistureRate.get();
	public static int startingMoisture = OptionsHolder.COMMON.startingMoisture.get();
	public static int moistureLoss = OptionsHolder.COMMON.moistureLoss.get();
	public static int waterRangeHorizontal = OptionsHolder.COMMON.waterRangeHorizontal.get();
	public static int waterRangeUp = OptionsHolder.COMMON.waterRangeUp.get();
	public static int waterRangeDown = OptionsHolder.COMMON.waterRangeDown.get();
	public static boolean dryingCropsStillDrop = OptionsHolder.COMMON.dryingCropsStillDrop.get();
	public static boolean dryCropsDie = OptionsHolder.COMMON.dryCropsDie.get();
	public static final int MAX_MOISTURE = 7;

	// TODO also need to provide IForgeBlock.isFertile?

	public WoDFarmlandBlock() {
		super(AbstractBlock.Properties.copy(Blocks.FARMLAND));
	}
	public WoDFarmlandBlock(Properties properties) {
		super(properties);

		// this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, Integer.valueOf(startingMoisture)));
		// This doesn't work, moisture still starts at 0, so I'm rather setting in hoe function
	}

	@Override
	public void tick(BlockState state, ServerWorld serverWorld, BlockPos pos, Random random) {
		if (!state.canSurvive(serverWorld, pos)) {
			turnToDirt(state, serverWorld, pos);
		}
	}

	@Override
	public boolean canSurvive(BlockState blockstate, IWorldReader p_196260_2_, BlockPos pos) {
		BlockState stateAbove = p_196260_2_.getBlockState(pos.above());
		return !(stateAbove.getMaterial().isSolid() || stateAbove.getBlock() instanceof MovingPistonBlock);
		// Removed `stateAbove.getBlock() instanceof FenceGateBlock`
	}

	@Override
	public void randomTick(BlockState blockState, ServerWorld serverWorld, BlockPos pos, Random random) {
		int moisture = blockState.getValue(MOISTURE);

		// Nearby water (source or flowing, or rain) increases hydration gradually
		if (serverWorld.isRainingAt(pos.above()) || isNearWater(serverWorld, pos)) {
			if (moisture < MAX_MOISTURE)
				serverWorld.setBlock(pos, blockState.setValue(MOISTURE, Integer.valueOf(moisture + 1)), 2);
			return;
		}

		// If not near water, we gradually reduce moisture, must be topped up by irrigation systems
		if (reduceMoistureRate > random.nextInt() % REDUCE_MOISTURE_RATE_OUT_OF) {
			if (moisture - moistureLoss >= 0)
				serverWorld.setBlock(pos, blockState.setValue(MOISTURE,
						Integer.valueOf(Integer.max(0, moisture - moistureLoss))), 2);
			else
				dryOut(blockState, serverWorld, pos);
		}
	}

	// Called when moisture is zero and would be reduced further
	public void dryOut(BlockState blockState, ServerWorld serverWorld, BlockPos pos) {
		final boolean underCrops = isUnderCrops(serverWorld, pos);
		if (!dryCropsDie && underCrops)
			return;

		// If drying crops shouldn't drop, first destroy plants, then turn to dirt
		if (underCrops && !dryingCropsStillDrop) {
			// add particles? serverWorld.getBlockState(pos.above()).addDestroyEffects(serverWorld, pos, particles);
			serverWorld.playSound(null, pos, SoundEvents.CROP_BREAK, SoundCategory.BLOCKS, 0.5F, 2.6F);
			// TODO play different sounds depending on crops, e.g. SoundEvents.NETHER_WART_BREAK
			serverWorld.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
		}

		turnToDirt(blockState, serverWorld, pos);
	}

	private static boolean isNearWater(IWorldReader p_176530_0_, BlockPos p_176530_1_) {
		for(BlockPos blockpos : BlockPos.betweenClosed(
				p_176530_1_.offset(-waterRangeHorizontal, -waterRangeUp, -waterRangeHorizontal), 
				p_176530_1_.offset( waterRangeHorizontal, waterRangeDown, waterRangeHorizontal))) {
			if (p_176530_0_.getFluidState(blockpos).is(FluidTags.WATER)) {
				return true;
			}
		}
		return net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(p_176530_0_, p_176530_1_);
	}

	// Vanilla farmland isn't pathfindable?? So override it to true
	@Override
	public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
		return true;
	}
	
	// Have to copy this from FarmlandBlock because it's not public there
	public boolean isUnderCrops(IBlockReader p_176529_0_, BlockPos p_176529_1_) {
		BlockState plant = p_176529_0_.getBlockState(p_176529_1_.above());
		BlockState state = p_176529_0_.getBlockState(p_176529_1_);
		return plant.getBlock() instanceof net.minecraftforge.common.IPlantable && state.canSustainPlant(p_176529_0_, p_176529_1_, Direction.UP, (net.minecraftforge.common.IPlantable)plant.getBlock());
	}

	// More functions that could be overridden
	/*@Override
	public void fallOn(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
		if (!p_180658_1_.isClientSide && net.minecraftforge.common.ForgeHooks.onFarmlandTrample(p_180658_1_, p_180658_2_, Blocks.DIRT.defaultBlockState(), p_180658_4_, p_180658_3_)) { // Forge: Move logic to Entity#canTrample
			turnToDirt(p_180658_1_.getBlockState(p_180658_2_), p_180658_1_, p_180658_2_);
		}

		super.fallOn(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_);
	}*/
	/*public static void turnToDirt(BlockState p_199610_0_, World p_199610_1_, BlockPos p_199610_2_) {
		p_199610_1_.setBlockAndUpdate(p_199610_2_, pushEntitiesUp(p_199610_0_, Blocks.DIRT.defaultBlockState(), p_199610_1_, p_199610_2_));
	}*/
}

