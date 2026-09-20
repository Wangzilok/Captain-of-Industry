package com.wangzi.coi.recipe.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class GeneralRecipe implements Recipe<Container> {
    protected final ResourceLocation id;

    // 【核心升级】：用列表存储所有的输入和输出
    protected final List<Ingredient> inputs;
    protected final List<Integer> inputCounts;
    protected final List<ItemStack> outputs; // 多输出列表

    public GeneralRecipe(ResourceLocation id, List<Ingredient> inputs, List<Integer> inputCounts, List<ItemStack> outputs) {
        this.id = id;
        this.inputs = inputs;
        this.inputCounts = inputCounts;
        this.outputs = outputs;
    }

    // 自适应匹配输入槽
    @Override
    public boolean matches(@NotNull Container pContainer, Level pLevel) {
        if (pLevel.isClientSide()) return false;
        for (int i = 0; i < inputs.size(); i++) {
            ItemStack slotItem = pContainer.getItem(i);
            if (!inputs.get(i).test(slotItem) || slotItem.getCount() < inputCounts.get(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @NotNull
    public ItemStack assemble(@NotNull Container pContainer, @NotNull RegistryAccess pRegistryAccess) {
        // 原版接口只要求返回一个，我们默认返回第一个输出物品（主要用于 JEI 展示）
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) { return true; }

    @Override
    @NotNull
    public ItemStack getResultItem(@NotNull RegistryAccess pRegistryAccess) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    @Override
    @NotNull
    public ResourceLocation getId() { return this.id; }

    @Override
    @NotNull
    public abstract RecipeSerializer<?> getSerializer();

    @Override
    @NotNull
    public abstract RecipeType<?> getType();

    // 获取所有的输入材料列表
    public List<Ingredient> getInputs() {
        return inputs;
    }

    // 获取所有的输入消耗数量列表
    public List<Integer> getInputCounts() {
        return inputCounts;
    }

    // 获取所有的输出物品（供 BlockEntity 插入输出槽使用）
    public List<ItemStack> getOutputs() {
        return outputs;
    }

    // ==================== 通用序列化器 ====================
    public static class Serializer<T extends GeneralRecipe> implements RecipeSerializer<T> {
        private final IRecipeFactory<T> factory;

        public Serializer(IRecipeFactory<T> factory) {
            this.factory = factory;
        }

        @Override
        @NotNull
        public T fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pJson) {
            // 1. 解析输入列表（强制要求 JSON 中必须为 ingredients 数组）
            List<Ingredient> inputs = new ArrayList<>();
            List<Integer> inputCounts = new ArrayList<>();
            JsonArray inputArray = GsonHelper.getAsJsonArray(pJson, "ingredients");
            for (int i = 0; i < inputArray.size(); i++) {
                JsonObject obj = inputArray.get(i).getAsJsonObject();
                inputs.add(Ingredient.fromJson(obj.get("ingredient")));
                inputCounts.add(GsonHelper.getAsInt(obj, "count", 1));
            }

            // 2. 解析输出列表（强制要求 JSON 中必须为 results 数组）
            List<ItemStack> outputs = new ArrayList<>();
            JsonArray outputArray = GsonHelper.getAsJsonArray(pJson, "results");
            for (int i = 0; i < outputArray.size(); i++) {
                outputs.add(ShapedRecipe.itemStackFromJson(outputArray.get(i).getAsJsonObject()));
            }

            return factory.create(pRecipeId, inputs, inputCounts, outputs);
        }

        @Override
        public T fromNetwork(@NotNull ResourceLocation pRecipeId, @NotNull FriendlyByteBuf pBuffer) {
            // 读取输入
            int inputSize = pBuffer.readVarInt();
            List<Ingredient> inputs = new ArrayList<>();
            List<Integer> inputCounts = new ArrayList<>();
            for (int i = 0; i < inputSize; i++) {
                inputs.add(Ingredient.fromNetwork(pBuffer));
                inputCounts.add(pBuffer.readVarInt());
            }

            // 读取输出
            int outputSize = pBuffer.readVarInt();
            List<ItemStack> outputs = new ArrayList<>();
            for (int i = 0; i < outputSize; i++) {
                outputs.add(pBuffer.readItem());
            }
            return factory.create(pRecipeId, inputs, inputCounts, outputs);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf pBuffer, T pRecipe) {
            // 写入输入
            pBuffer.writeVarInt(pRecipe.inputs.size());
            for (int i = 0; i < pRecipe.inputs.size(); i++) {
                pRecipe.inputs.get(i).toNetwork(pBuffer);
                pBuffer.writeVarInt(pRecipe.inputCounts.get(i));
            }
            // 写入输出
            pBuffer.writeVarInt(pRecipe.outputs.size());
            for (ItemStack output : pRecipe.outputs) {
                pBuffer.writeItemStack(output, false);
            }
        }

        @FunctionalInterface
        public interface IRecipeFactory<T extends GeneralRecipe> {
            T create(ResourceLocation id, List<Ingredient> inputs, List<Integer> inputCounts, List<ItemStack> outputs);
        }
    }
}