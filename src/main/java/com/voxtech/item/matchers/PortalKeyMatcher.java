package com.voxtech.item.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;

import javax.annotation.Nonnull;

public class PortalKeyMatcher extends ItemConditionInteraction.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<PortalKeyMatcher> CODEC = BuilderCodec
        .builder(PortalKeyMatcher.class, PortalKeyMatcher::new, BASE_CODEC)
        .documentation("This matcher will succeed if the target item is a portal key")
        .append(new KeyedCodec<>("RequiredTypeIds", new ArrayCodec<>(Codec.STRING, String[]::new)),
            (object, typeIds) -> object.requiredTypeIds = typeIds,
            object -> object.requiredTypeIds)
            .documentation("If included, the matcher will fail unless the portal key's typeId matches one of these values")
            .add()
        .build();

    private String[] requiredTypeIds;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        if (targetItem.getItem().getPortalKey() == null) {
            return false;
        }

        if (requiredTypeIds.length == 0) {
            return true;
        }

        String portalTypeId = targetItem.getItem().getPortalKey().getPortalTypeId();

        for (String typeId : requiredTypeIds) {
            if (typeId.equals(portalTypeId)) {
                return true;
            }
        }

        return false;
    }
}
