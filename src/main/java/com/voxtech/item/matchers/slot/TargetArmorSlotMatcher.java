package com.voxtech.item.matchers.slot;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.item.matchers.SlotMatcher;
import com.voxtech.protocol.Slot;

import javax.annotation.Nonnull;

public class TargetArmorSlotMatcher extends Slot {

    @Nonnull
    public static final BuilderCodec<TargetArmorSlotMatcher> CODEC = BuilderCodec
            .builder(TargetArmorSlotMatcher.class, TargetArmorSlotMatcher::new, BASE_CODEC)
            .documentation("This matcher will pass if the target item is an armor piece and the target slot is the appropriate slot to place it in.")
            .build();

    @Override
    public boolean test(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        if (targetItem == null || targetItem.getItem().getArmor() == null) {
            return false;
        }

        InventoryComponent.Armor armor = commandBuffer.getComponent(user, InventoryComponent.Armor.getComponentType());
        if (armor == null) {
            return false;
        }

        return (targetContainer == armor.getInventory() && targetSlot == (short)targetItem.getItem().getArmor().getArmorSlot().ordinal());
    }
}
