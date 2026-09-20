package com.wangzi.coi.init;

import com.wangzi.coi.Coi;
import com.wangzi.coi.entity.ElectricWireEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    // 创建实体延迟注册器对象
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Coi.MODID);

    // region

    // 电线
    public static final RegistryObject<EntityType<ElectricWireEntity>> ELECTRIC_WIRE =
            ENTITIES.register("electric_wire", () ->
                    EntityType.Builder.<ElectricWireEntity>of(ElectricWireEntity::new, MobCategory.MISC)
                            .noSummon()
                            .clientTrackingRange(64)
                            .updateInterval(20)
                            .sized(0.2f, 0.2f)
                            .build("electric_wire")
            );

    // end region

    // 用于给主类挂载到事件总线
    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
