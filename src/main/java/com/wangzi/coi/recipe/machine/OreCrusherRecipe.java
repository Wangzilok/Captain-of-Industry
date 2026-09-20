package com.wangzi.coi.recipe.machine;

import com.wangzi.coi.init.ModRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// 实现 Recipe 接口，作为核心逻辑类，负责定义输入、输出以及匹配规则
public class OreCrusherRecipe extends GeneralRecipe {
    // 构造函数：由序列化器调用，将解析好的数据注入到配方对象中
    public OreCrusherRecipe(ResourceLocation id, List<Ingredient> inputs, List<Integer> inputCounts, List<ItemStack> outputs) {
        super(id, inputs, inputCounts, outputs);
    }

    // 绑定序列化器
    @NotNull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ORE_CRUSHING_SERIALIZER.get();
    }

    // 绑定配方类型
    @NotNull
    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ORE_CRUSHER_TYPE.get();
    }
}