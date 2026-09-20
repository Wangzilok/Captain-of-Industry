package com.wangzi.coi.container.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.wangzi.coi.container.menu.GeneralMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class GeneralScreen<T extends GeneralMenu> extends AbstractContainerScreen<T> {
    // 构造函数
    public GeneralScreen(T pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    // 界面初始化方法，在构造函数执行完毕、所有变量都准备就绪后调用
    public void init() {
        super.init();
    }

    // 核心总渲染方法每一帧都会被调用，负责统筹整个界面的绘制顺序
    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // 绘制半透明的背景遮罩
        this.renderBackground(pGuiGraphics);
        // 调用父类的 render 方法
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        // 绘制鼠标悬停时的提示框
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    // 核心绘制方法，将背景图和槽位画到屏幕上
    @Override
    protected void renderBg(@NotNull GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        // 【底层渲染设置】设置当前使用的着色器程序
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        // 【底层渲染设置】设置颜色为纯白（1.0F, 1.0F, 1.0F, 1.0F）
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // 重写 renderLabels 方法只画物品栏，不画标题
    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        // 只画"物品栏"，不画标题
        pGuiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY + 8, 0x404040, false);
    }
}