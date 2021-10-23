package com.stebars.waterordeath.mixin;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.stebars.waterordeath.OptionsHolder;

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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin extends Block {

	@Shadow public static final IntegerProperty MOISTURE = BlockStateProperties.MOISTURE;
	
	private static final int REDUCE_MOISTURE_RATE_OUT_OF = 10000; // so rate = 10 means 10 out of 10000
	private static int reduceMoistureRate = OptionsHolder.COMMON.reduceMoistureRate.get();
	private static int moistureLoss = OptionsHolder.COMMON.moistureLoss.get();
	private static int waterRangeHorizontal = OptionsHolder.COMMON.waterRangeHorizontal.get();
	private static int waterRangeUp = OptionsHolder.COMMON.waterRangeUp.get();
	private static int waterRangeDown = OptionsHolder.COMMON.waterRangeDown.get();
	private static boolean dryingCropsStillDrop = OptionsHolder.COMMON.dryingCropsStillDrop.get();
	private static boolean dryCropsDie = OptionsHolder.COMMON.dryCropsDie.get();
	private static final int MAX_MOISTURE = 7;

	public FarmlandBlockMixin(AbstractBlock.Properties p_i48400_1_) {
		super(p_i48400_1_);
		this.registerDefaultState(this.stateDefinition.any().setValue(MOISTURE, Integer.valueOf(0)));
	}

	@Overwrite
	public void tick(BlockState state, ServerWorld serverWorld, BlockPos pos, Random random) {
		if (!state.canSurvive(serverWorld, pos))
			turnToDirt(state, serverWorld, pos);
	}

	@Shadow
	public static void turnToDirt(BlockState p_199610_0_, World p_199610_1_, BlockPos p_199610_2_) {
		p_199610_1_.setBlockAndUpdate(p_199610_2_, pushEntitiesUp(p_199610_0_, Blocks.DIRT.defaultBlockState(), p_199610_1_, p_199610_2_));
	}

	@Overwrite
	public boolean canSurvive(BlockState blockstate, IWorldReader p_196260_2_, BlockPos pos) {
		BlockState stateAbove = p_196260_2_.getBlockState(pos.above());
		return !(stateAbove.getMaterial().isSolid() || stateAbove.getBlock() instanceof MovingPistonBlock);
		// Removed `stateAbove.getBlock() instanceof FenceGateBlock`
	}

	@Overwrite
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
	private void dryOut(BlockState blockState, ServerWorld serverWorld, BlockPos pos) {
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

	@Overwrite
	private static boolean isNearWater(IWorldReader p_176530_0_, BlockPos p_176530_1_) {
		for(BlockPos blockpos : BlockPos.betweenClosed(
				p_176530_1_.offset(-waterRangeHorizontal, -waterRangeUp, -waterRangeHorizontal), 
				p_176530_1_.offset( waterRangeHorizontal, waterRangeDown, waterRangeHorizontal)))
			if (p_176530_0_.getFluidState(blockpos).is(FluidTags.WATER))
				return true;
		return net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(p_176530_0_, p_176530_1_);
	}

	// Vanilla farmland isn't pathfindable?? So overwrite it to true
	@Overwrite
	public boolean isPathfindable(BlockState p_196266_1_, IBlockReader p_196266_2_, BlockPos p_196266_3_, PathType p_196266_4_) {
		return true;
	}

	@Shadow private boolean isUnderCrops(IBlockReader p_176529_0_, BlockPos p_176529_1_) {
		BlockState plant = p_176529_0_.getBlockState(p_176529_1_.above());
		BlockState state = p_176529_0_.getBlockState(p_176529_1_);
		return plant.getBlock() instanceof net.minecraftforge.common.IPlantable && state.canSustainPlant(p_176529_0_, p_176529_1_, Direction.UP, (net.minecraftforge.common.IPlantable)plant.getBlock());
	}

}