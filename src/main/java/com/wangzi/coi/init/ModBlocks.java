package com.wangzi.coi.init;

import com.wangzi.coi.Coi;
import com.wangzi.coi.block.InfiniteEnergy;
import com.wangzi.coi.block.energy.OakTelegraphPoleBlock;
import com.wangzi.coi.block.machine.OreCrusherBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    // 创建方块延迟注册器对象
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Coi.MODID);

    // region

    // 矿石粉碎机
    public static final RegistryObject<Block> ORE_CRUSHER =
            registerBlock("ore_crusher",
                    () -> new OreCrusherBlock(BlockBehaviour.Properties
                            .copy(Blocks.IRON_BLOCK)
                            .noOcclusion()));

    // 橡木电线杆
    public static final RegistryObject<Block> OAK_TELEGRAPH_POLE =
            registerBlock("oak_telegraph_pole",
                    () -> new OakTelegraphPoleBlock(BlockBehaviour.Properties
                            .copy(Blocks.OAK_FENCE)
                            .noOcclusion()));

    // ==================== 测试方块 ====================
    // 无尽能源
    public static final RegistryObject<Block> INFINITE_ENERGY =
            registerBlock("infinite_energy",
                    () -> new InfiniteEnergy(BlockBehaviour.Properties
                            .copy(Blocks.IRON_BLOCK)
                            .noOcclusion()));

    // end region

    // 方块类通用注册方法
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block){
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // 方块物品类通用注册方法
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block){
        return ModItems.ITEMS.register(name,
                () -> new BlockItem(block.get(), new Item.Properties())
        );
    }

    // 用于给主类挂载到事件总线
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
