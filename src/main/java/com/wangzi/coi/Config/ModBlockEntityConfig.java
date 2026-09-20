package com.wangzi.coi.Config;

import net.minecraft.world.item.crafting.RecipeType;

public record ModBlockEntityConfig(String displayName,
                                   ModBlockType blockType,
                                   int itemStackHandlerSize,
                                   int inputSlotIdx,
                                   EnergyStorageConfig energyStorageConfig,
                                   RecipeType<?> recipeType) {}
