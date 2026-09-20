package com.wangzi.coi.init;

import com.wangzi.coi.Coi;
import com.wangzi.coi.container.menu.OreCrusherMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    // 创建模组菜单延迟注册器对象
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Coi.MODID);

    // region

    // 矿石粉碎机菜单
    public static final RegistryObject<MenuType<OreCrusherMenu>> ORE_CRUSHER_MENU =
            MENUS.register("ore_crusher_menu",
                    () -> IForgeMenuType.create(OreCrusherMenu::new));

    // end region

    // 用于给主类挂载到事件总线
    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
