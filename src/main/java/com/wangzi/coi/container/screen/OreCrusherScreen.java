package com.wangzi.coi.container.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wangzi.coi.Coi;
import com.wangzi.coi.container.menu.OreCrusherMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

// 继承 AbstractContainerScreen<OreCrusherMenu> 类，并指定它绑定的后端菜单是 OreCrusherMenu
public class OreCrusherScreen extends GeneralScreen<OreCrusherMenu> {
    // 定义 GUI 的背景图路径
    private static final ResourceLocation TEXTURE = new ResourceLocation(Coi.MODID, "textures/gui/ore_crusher.png");

    // 构造函数
    public OreCrusherScreen(OreCrusherMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    // 核心绘制方法，将背景图和槽位画到屏幕上
    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        super.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);

        // 【底层渲染设置】将我们要画的背景图绑定到渲染系统中
        RenderSystem.setShaderTexture(0, TEXTURE);

        // 计算 GUI 在屏幕上的居中位置
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 将背景图绘制到屏幕上
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight + 6);

        // 进度条
        int l = this.menu.getProgress();
        pGuiGraphics.blit(TEXTURE, this.leftPos + 77, this.topPos + 49, 176, 0, l , 5);

        // 机器状态
        if(this.menu.getState() == 0) pGuiGraphics.blit(TEXTURE, this.leftPos + 65, this.topPos + 12, 176, 5, 46, 35);
    }
}