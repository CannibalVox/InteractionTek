package com.voxtech.protocol;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public abstract class ItemMatcher {
    public static final CodecMapCodec<ItemMatcher> CODEC = new CodecMapCodec<>("Type");
    public static final BuilderCodec<ItemMatcher> BASE_CODEC = BuilderCodec.abstractBuilder(ItemMatcher.class)
            .appendInherited(new KeyedCodec<>("Invert", Codec.BOOLEAN),
                    (object, invert) -> object.invert = invert,
                    object -> object.invert,
                    (object, parent) -> object.invert = parent.invert)
            .documentation("Inverts the results of the matcher")
            .add()
            .appendInherited(new KeyedCodec<>("AllowEmpty", Codec.BOOLEAN),
                    (object, allowEmpty) -> object.allowEmpty = allowEmpty,
                    object -> object.allowEmpty,
                    (object, parent) -> object.allowEmpty = parent.allowEmpty)
            .documentation("If true, the matcher will succeed when the target slot is empty.")
            .add()
            .build();

    protected boolean invert;
    protected boolean allowEmpty;

    public final boolean test(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        if (targetItem == null && failEmptyItem()) {
            return allowEmpty;
        }

        return this.test0(user, commandBuffer, context, targetContainer, targetSlot, targetItem) ^ this.invert;
    }

    public boolean failEmptyItem() {
        return true;
    }

    public abstract boolean test0(Ref<EntityStore> user,  CommandBuffer<EntityStore> commandBuffer, InteractionContext context,ItemContainer targetContainer, int targetSlot, ItemStack targetItem);
}
