package com.wangzi.coi.compat.jei;

import com.wangzi.coi.Coi;
import com.wangzi.coi.init.ModBlocks;
import com.wangzi.coi.init.ModRecipeTypes;
import com.wangzi.coi.recipe.machine.OreCrusherRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    @Override
    @NotNull
    public ResourceLocation getPluginUid() {
        // 把ResourceLocation的第一个参数改成你自己的 modid
        return new ResourceLocation(Coi.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        // 注册自定义配方类别
        registration.addRecipeCategories(new OreCrusherCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        // 注册具体的配方实例
        // 在自定义配方类里，你应该定义了一个内部静态类或静态字段来表示配方类型（RecipeType）
        // 这里就是在告诉RecipeManager，你要取的是这个类型的配方，需要把它之下的所有配方都拿出来，放在列表里方便后续显示和处理
        @SuppressWarnings("unchecked")
        List<OreCrusherRecipe> compressorRecipes = recipeManager.getAllRecipesFor((RecipeType<OreCrusherRecipe>) ModRecipeTypes.ORE_CRUSHER_TYPE.get());

        // 告诉JEI这里有一类自定义配方以及它的完整配方列表，让其能够在配方界面上显示它们
        registration.addRecipes(OreCrusherCategory.ORE_CRUSHER_TYPE, compressorRecipes);

    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        // 注册GUI交互器

    }

    // 可选项，但建议加上这一项，可以增强你mod的引导性
    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        // 注册配方催化剂
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ORE_CRUSHER.get()), OreCrusherCategory.ORE_CRUSHER_TYPE);
    }
}
