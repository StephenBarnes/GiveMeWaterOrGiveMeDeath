package com.stebars.waterordeath;

import com.stebars.waterordeath.OptionsHolder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


@Mod(WaterOrDeath.MODID)
public class WaterOrDeath {
	final static String MODID = "waterordeath";
	
	private static final DeferredRegister<Block> OVERWRITE_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "minecraft");
	public static final RegistryObject<Block> FARMLAND_IOD_BLOCK = OVERWRITE_BLOCKS.register("farmland",
			() -> new WoDFarmlandBlock());
	
	public static int startingMoisture = OptionsHolder.COMMON.startingMoisture.get();
	
    public WaterOrDeath() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, OptionsHolder.COMMON_SPEC);
    	OVERWRITE_BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    @SubscribeEvent
    public void hoeToNewFarmland(UseHoeEvent e) {
    	// Change hoe events to create the new farmland block, instead of the original FarmlandBlock.
    	// This is necessary because else hoeing a block turns it into the old version of farmland, which no longer exists,
    	// so you get a block with ID minecraft:air and the purple-black lack-of-texture
    	    	
    	ItemUseContext context = e.getContext();
    	World world = context.getLevel();
    	BlockPos blockpos = context.getClickedPos();

    	// If you try to hoe from the bottom, or hoe a block with another block on top, deny
    	if (context.getClickedFace() == Direction.DOWN || !world.isEmptyBlock(blockpos.above())) {
    		e.setResult(Result.DENY);
    		return;
    	}
    	
    	BlockState modifiedBlockState = world.getBlockState(blockpos).getToolModifiedState(world, blockpos, context.getPlayer(), context.getItemInHand(), net.minecraftforge.common.ToolType.HOE);

    	// removing this check causes game crash when you try to hoe non-hoeable blocks
    	if (modifiedBlockState == null || modifiedBlockState.getBlock() == null) {
    		e.setResult(Result.DENY);
    		return;
    	}

    	if (!(modifiedBlockState.getBlock() instanceof FarmlandBlock)) {
    		e.setResult(Result.DENY);
    		return;
    	}

    	e.setResult(Result.ALLOW);
    	
    	PlayerEntity playerentity = context.getPlayer();
    	world.playSound(playerentity, blockpos, SoundEvents.HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

    	if (!world.isClientSide) {
    		world.setBlock(blockpos, FARMLAND_IOD_BLOCK.get().defaultBlockState().setValue(
    				((WoDFarmlandBlock) FARMLAND_IOD_BLOCK.get()).MOISTURE, startingMoisture), 11);
    			// No, can't get this starting block state in constructor, have to re-make every time
    		if (playerentity != null) {
    			context.getItemInHand().hurtAndBreak(1, playerentity, (p_220043_1_) -> {
    				p_220043_1_.broadcastBreakEvent(context.getHand());
    			});
    		}
    	}
    }
    
    // TODO currently dispensers with hoes just launch the hoe (same as vanilla), should change so they actually hoe the land
    // TODO add textures that have a smooth gradient from zero to 7 moisture, instead of just 2
}
