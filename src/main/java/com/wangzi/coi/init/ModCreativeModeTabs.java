package com.wangzi.coi.init;

import com.wangzi.coi.Coi;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    // 创建创造模式标签延迟注册器对象
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Coi.MODID);

    // 注册“工业巨头 机器”创造模式标签
    public static final RegistryObject<CreativeModeTab> COI_MACHINE =
            CREATIVE_MODE_TABS.register("coi_machine",
                    () -> CreativeModeTab.builder()
                            // 设置在创造模式标签显示的图标
                            .icon(() -> new ItemStack(ModBlocks.ORE_CRUSHER.get()))
                            // 设置标签的翻译键和显示名
                            .title(Component.translatable("tab.coi_machine"))
                            // 设置在该标签中显示的物品
                            .displayItems((itemDisplayParameters, output) -> {
                                output.accept(ModBlocks.ORE_CRUSHER.get());    // 矿石粉碎机
                            })
                            // 构建实例
                            .build());

    // 注册“工业巨头 能源”创造模式标签
    public static final RegistryObject<CreativeModeTab> COI_ENERGY =
            CREATIVE_MODE_TABS.register("coi_energy",
                    () -> CreativeModeTab.builder()
                            // 设置在创造模式标签显示的图标
                            .icon(() -> new ItemStack(ModBlocks.OAK_TELEGRAPH_POLE.get()))
                            // 设置标签的翻译键和显示名
                            .title(Component.translatable("tab.coi_energy"))
                            // 设置在该标签中显示的物品
                            .displayItems((itemDisplayParameters, output) -> {
                                output.accept(ModBlocks.OAK_TELEGRAPH_POLE.get());    // 橡木电线杆
                                output.accept(ModItems.ELECTRIC_WIRE.get());    // 电线
                            })
                            // 设置该创造模式标签必在“工业巨头 机器”创造模式标签之后
                            .withTabsBefore(COI_MACHINE.getKey())
                            // 构建实例
                            .build());

    // 注册“工业巨头 材料”创造模式标签
    public static final RegistryObject<CreativeModeTab> COI_MATERIAL =
            CREATIVE_MODE_TABS.register("coi_material",
                    () -> CreativeModeTab.builder()
                            // 设置在创造模式标签显示的图标
                            .icon(() -> new ItemStack(ModItems.COPPER_POWDER.get()))
                            // 设置标签的翻译键和显示名
                            .title(Component.translatable("tab.coi_material"))
                            // 设置在该标签中显示的物品
                            .displayItems((itemDisplayParameters, output) -> {
                                output.accept(ModItems.CHARCOAL_POWDER.get());    // 木炭粉
                                output.accept(ModItems.COAL_POWDER.get());        // 碳粉
                                output.accept(ModItems.COPPER_POWDER.get());      // 铜粉
                                output.accept(ModItems.IRON_POWDER.get());        // 铁粉
                                output.accept(ModItems.RAW_IRON_POWDER.get());    // 铁矿粉
                            })
                            // 设置该创造模式标签必在“工业巨头 能源”创造模式标签之后
                            .withTabsBefore(COI_ENERGY.getKey())
                            // 构建实例
                            .build());

    // 用于给主类挂载到事件总线
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
