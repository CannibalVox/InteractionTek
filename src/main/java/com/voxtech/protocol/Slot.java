package com.voxtech.protocol;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class Slot {
    public static final CodecMapCodec<Slot> CODEC = new CodecMapCodec<>("Type");
    public static final BuilderCodec<Slot> BASE_CODEC = BuilderCodec
            .abstractBuilder(Slot.class)
            .build();

    public abstract boolean test(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem);
}
