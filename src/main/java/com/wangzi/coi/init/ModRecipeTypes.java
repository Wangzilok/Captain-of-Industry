package com.wangzi.coi.init;

import com.wangzi.coi.Coi;
import com.wangzi.coi.recipe.machine.OreCrusherRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeTypes {
    // 创建配方类型延迟注册器对象
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Coi.MODID);

    // 创建序列化器延迟注册器对象
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Coi.MODID);

    // region

    // 矿石粉碎机配方类型
    public static final RegistryObject<RecipeType<?>> ORE_CRUSHER_TYPE =
            RECIPE_TYPES.register("ore_crusher", () -> RecipeType.simple(new ResourceLocation(Coi.MODID, "ore_crusher")));
    // 矿石粉碎机序列化器
    public static final RegistryObject<RecipeSerializer<?>> ORE_CRUSHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("ore_crusher", () -> new OreCrusherRecipe.Serializer<>(OreCrusherRecipe::new));

    // end region

    // 用于给主类挂载到事件总线
    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
