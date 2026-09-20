package com.wangzi.coi.client;

import com.wangzi.coi.Coi;
import com.wangzi.coi.container.screen.OreCrusherScreen;
import com.wangzi.coi.init.ModBlockEntities;
import com.wangzi.coi.init.ModEntities;
import com.wangzi.coi.init.ModMenuTypes;
import com.wangzi.coi.renderer.ElectricWireRenderer;
import com.wangzi.coi.renderer.ModBlockEntityRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// 这是一个专门处理客户端（Client）初始化事件的配置类
// modid = Coi.MODID : 指定该事件订阅器属于 Coi 模组
// bus = Bus.MOD : 指定监听 MOD 事件总线（用于处理模组生命周期事件，如客户端初始化）
// value = Dist.CLIENT : 【核心安全机制】指定该类仅在物理客户端加载。
//                       如果玩家把模组放进服务器，Forge 会直接忽略这个类，
//                       从而完美避免了因服务器没有渲染引擎而导致的崩溃。
@Mod.EventBusSubscriber(modid = Coi.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    // 监听客户端初始化事件。
    // 当游戏客户端完成基础加载，准备进入主菜单时，Forge 会自动触发此方法。
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        registerScreens();
        registerBlockEntityRenderers();
    }

    // 注册实体和其绑定的渲染器
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ELECTRIC_WIRE.get(), ElectricWireRenderer::new);
    }

    // 集中注册所有的 GUI 界面（Screen）与后端菜单（Menu）的绑定关系
    private static void registerScreens() {
        MenuScreens.register(
                ModMenuTypes.ORE_CRUSHER_MENU.get(),
                OreCrusherScreen::new
        );
    }

    // 集中注册所有的 BlockEntity 与自定义渲染器的绑定关系
    private static void registerBlockEntityRenderers() {
        BlockEntityRenderers.register(
                ModBlockEntities.OAK_TELEGRAPH_POLE_ENTITY.get(),
                ModBlockEntityRenderer::new
        );
    }
}
