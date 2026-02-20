package com.voxtech.item.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;

import javax.annotation.Nonnull;

public class InventoryMatcher extends ItemConditionInteraction.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<InventoryMatcher> CODEC = BuilderCodec
        .builder(InventoryMatcher.class, InventoryMatcher::new, BASE_CODEC)
        .documentation("This matcher will succeed if the target item is located in an inventory section specified by one of the provided section ids")
        .append(new KeyedCodec<>("SectionIds", new ArrayCodec<>(Codec.INTEGER, Integer[]::new)),
            (object, sectionIds) -> object.sectionIds = sectionIds,
            object -> object.sectionIds)
            .documentation("The list of inventory section ids to allow")
            .addValidator(Validators.nonNull())
            .add()
        .build();

    private Integer[] sectionIds;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        Entity entity = EntityUtils.getEntity(user, commandBuffer);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return false;
        }

        Inventory inventory = livingEntity.getInventory();
        if (inventory == null) {
            return false;
        }

        for (Integer sectionId : sectionIds) {
            if (sectionId != null && inventory.getSectionById(sectionId) == targetContainer) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean failEmptyItem() {
        return false;
    }
}
