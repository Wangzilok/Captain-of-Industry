package com.wangzi.coi.compat.jei;

import com.wangzi.coi.Coi;
import com.wangzi.coi.init.ModBlocks;
import com.wangzi.coi.recipe.machine.OreCrusherRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OreCrusherCategory implements IRecipeCategory<OreCrusherRecipe> {
    // ResourceLocation第一个参数改成你自己的modid，第二个参数建议传你执行配方的机器名字
    public static final ResourceLocation UID = new ResourceLocation(Coi.MODID, "ore_crusher");

    // 这是绘制JEI显示背景的，如果你想用自定义的GUI贴图，就像这样写然后指定贴图的资源位置
    // 记得把modid换成你自己mod的，然后一定要以.png结尾！！
    // 本例里的路径对应的是 assets/klux/textures/jei/gui_compressor.png
//    public static final ResourceLocation TEXTURE = new ResourceLocation(Coi.MODID, "textures/jei/ore_crusher.png");

    // 定义你的RecipeType，改成你的自定义recipe类名
    public static final RecipeType<OreCrusherRecipe> ORE_CRUSHER_TYPE =
            new RecipeType<>(UID, OreCrusherRecipe.class);

    // 稍后用于绘制背景贴图和JEI的显示图标
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slotDrawable;
    private final IDrawable arrow;

    // 存储你的配方实例，把recipe的名字换成你自己的
    private OreCrusherRecipe oreCrusherRecipe;

    // 绘制背景贴图，把这里category的名字换成你的类名
    public OreCrusherCategory(IGuiHelper helper) {
        // TEXTURE是上边ResourceLocation指向的贴图。
        // 前两个参数填0就可以，最后两个分别是贴图的宽和高（如176x86），根据实际情况进行调整。
//        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 86);
        this.background = helper.createBlankDrawable(82, 38);

        // 绘制图标，在JEI的配方页显示的图标。
        // 一般就用你的机器图标就行了，确保你的机器已经注册。（当然用别的也可以）
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ORE_CRUSHER.get()));

        this.slotDrawable = helper.getSlotDrawable();
        this.arrow = helper.createAnimatedRecipeArrow(200);
    }

    @Override
    @NotNull
    public RecipeType<OreCrusherRecipe> getRecipeType() {
        // 这里的RecipeType应该是你刚刚写好的，照着弄下来就可以。
        return ORE_CRUSHER_TYPE;
    }

    @Override
    @NotNull
    public Component getTitle() {
        // JEI界面要显示的标题，你可以像本例里一样用一个语言键，方便做多语言兼容。
        return Component.translatable("jei.coi.ore_crusher");
    }

    // 绘制背景
    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    // 绘制图标
    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull OreCrusherRecipe recipe, @NotNull IFocusGroup focuses) {
        List<Ingredient> inputs = recipe.getInputs();
        List<Integer> inputCounts = recipe.getInputCounts();
        List<ItemStack> outputs = recipe.getOutputs();

        // 输入槽：遍历所有输入材料，依次排列
        for (int i = 0; i < inputs.size(); i++) {
            int x = 1;  // 所有输入槽的X坐标（垂直排列）
            int y = i * 18; // 每个槽垂直间距18像素

            // 将 Ingredient 展开为所有可能的 ItemStack，并设置数量
            ItemStack[] stacks = inputs.get(i).getItems();
            List<ItemStack> expanded = new ArrayList<>();
            for (ItemStack stack : stacks) {
                ItemStack copy = stack.copy();
                copy.setCount(inputCounts.get(i));
                expanded.add(copy);
            }

            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addItemStacks(expanded);
        }

        // 输出槽：遍历所有输出物品，依次排列
        for (int i = 0; i < outputs.size(); i++) {
            int x = 66;  // 所有输出槽的X坐标
            int y = i * 18; // 每个槽垂直间距18像素

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStack(outputs.get(i));
        }
    }

    @Override
    public void draw(@NotNull OreCrusherRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // 在每个槽位的位置绘制槽位边框
        // 输入槽位置
        for (int i = 0; i < recipe.getInputs().size(); i++) {
            int x = 0;  // 槽位边框比槽位大1像素，所以要偏移
            int y = i * 18 - 1;
            slotDrawable.draw(guiGraphics, x, y);
        }

        // 输出槽位置
        for (int i = 0; i < recipe.getOutputs().size(); i++) {
            int x = 66 - 1;
            int y = i * 18 - 1;
            slotDrawable.draw(guiGraphics, x, y);
        }

        arrow.draw(guiGraphics, 29, 0);

        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.coi.text.ten_seconds"), 31, 19, 0x7e7e7e, false);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.coi.energy_consumption_20"), 0, 30, 0x7efc20, true);
    }
}
