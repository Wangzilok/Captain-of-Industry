package com.wangzi.coi.init;

import com.wangzi.coi.Coi;
import com.wangzi.coi.block.machine.OreCrusherBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    // 创建物品延迟注册器对象
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Coi.MODID);

    // region

    // 木炭粉
    public static final RegistryObject<Item> CHARCOAL_POWDER =
            ITEMS.register("charcoal_powder",
                    () -> new Item(new Item.Properties()));

    // 碳粉
    public static final RegistryObject<Item> COAL_POWDER =
            ITEMS.register("coal_powder",
                    () -> new Item(new Item.Properties()));

    // 铜粉
    public static final RegistryObject<Item> COPPER_POWDER =
            ITEMS.register("copper_powder",
                    () -> new Item(new Item.Properties()));

    // 电线
    public static final RegistryObject<Item> ELECTRIC_WIRE =
            ITEMS.register("electric_wire",
                    () -> new Item(new Item.Properties()));

    // 铁粉
    public static final RegistryObject<Item> IRON_POWDER =
            ITEMS.register("iron_powder",
                    () -> new Item(new Item.Properties()));

    // 铁矿粉
    public static final RegistryObject<Item> RAW_IRON_POWDER =
            ITEMS.register("raw_iron_powder",
                    () -> new Item(new Item.Properties()));

    // end region

    // 用于给主类挂载到事件总线
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
