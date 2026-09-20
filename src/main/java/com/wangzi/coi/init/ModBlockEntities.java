package com.wangzi.coi.init;

import com.wangzi.coi.Coi;
import com.wangzi.coi.blockentity.InfiniteEnergyEntity;
import com.wangzi.coi.blockentity.energy.OakTelegraphPoleBlockEntity;
import com.wangzi.coi.blockentity.machine.OreCrusherBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    // 创建方块对象延迟注册器对象
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Coi.MODID);

    // region

    // 矿石粉碎机
    public static final RegistryObject<BlockEntityType<OreCrusherBlockEntity>> ORE_CRUSHER_ENTITY =
            BLOCK_ENTITIES.register("ore_crusher_entity",
                    () -> BlockEntityType
                            .Builder
                            .of(OreCrusherBlockEntity::new, ModBlocks.ORE_CRUSHER.get())
                            .build(null));

    // 矿石粉碎机
    public static final RegistryObject<BlockEntityType<OakTelegraphPoleBlockEntity>> OAK_TELEGRAPH_POLE_ENTITY =
            BLOCK_ENTITIES.register("oak_telegraph_pole_entity",
                    () -> BlockEntityType
                            .Builder
                            .of(OakTelegraphPoleBlockEntity::new, ModBlocks.OAK_TELEGRAPH_POLE.get())
                            .build(null));

    // ==================== 测试方块实体 ====================
    // 无尽能源
    public static final RegistryObject<BlockEntityType<InfiniteEnergyEntity>> INFINITE_ENERGY =
            BLOCK_ENTITIES.register("infinite_energy_block_entity",
                    () -> BlockEntityType
                            .Builder
                            .of(InfiniteEnergyEntity::new, ModBlocks.INFINITE_ENERGY.get())
                            .build(null));

    // end region

    // 用于给主类挂载到事件总线
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
