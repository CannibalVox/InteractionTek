package com.voxtech.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.protocol.ItemMatchType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemConditionInteraction extends SimpleItemInteraction {
    @Nonnull
    public static final BuilderCodec<ItemConditionInteraction> CODEC = BuilderCodec.builder(
        ItemConditionInteraction.class, ItemConditionInteraction::new, SimpleInteraction.CODEC
    )
    .documentation("This interaction will fail if the target item does not match the ItemMatchers")
    .append(new KeyedCodec<>("ItemMatchType", new EnumCodec<>(ItemMatchType.class)),
        (interaction, matchType) -> interaction.itemMatchType = matchType,
        interaction -> interaction.itemMatchType)
        .documentation("Whether all or any matchers need to match for this interaction to succeed")
        .add()
    .append(new KeyedCodec<>("Matchers", new ArrayCodec<>(ItemMatcher.CODEC, ItemMatcher[]::new)),
        (object, matchers) -> object.itemMatchers = matchers,
        object -> object.itemMatchers)
        .documentation("These matchers test the target item to decide if the interaction fails")
        .add()
    .build();

    private ItemMatchType itemMatchType = ItemMatchType.All;
    private ItemMatcher[] itemMatchers;

    @Override
    protected void interactWithItem(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> buffer, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nullable ItemContainer targetContainer, int targetSlot, @Nullable ItemStack targetItemStack, @Nonnull CooldownHandler cooldownHandler) {
        this.matchItem(context, itemInHand, targetContainer, targetSlot, targetItemStack);
    }

    @Override
    protected void simulateInteractWithItem(@Nonnull World world, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nullable ItemContainer targetContainer, int targetSlot, @Nullable ItemStack targetItemStack) {
        this.matchItem(context, itemInHand, targetContainer, targetSlot, targetItemStack);
    }

    private void matchItem(@Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nullable ItemContainer targetContainer, int targetSlot, @Nullable ItemStack targetItemStack) {
        Ref<EntityStore> ref = context.getEntity();

        if (this.itemMatchers.length == 0) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        for (ItemMatcher matcher : this.itemMatchers) {
            boolean success = matcher.test(ref, context.getCommandBuffer(), context, targetContainer, targetSlot, targetItemStack);
            if (success && this.itemMatchType == ItemMatchType.Any) {
                return;
            }

            if (!success && this.itemMatchType == ItemMatchType.All) {
                context.getState().state = InteractionState.Failed;
                return;
            }
        }

        if (this.itemMatchType == ItemMatchType.Any) {
            // Did not find any successful matches
            context.getState().state = InteractionState.Failed;
        }
    }

    public abstract static class ItemMatcher {
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

        public final boolean test(Ref<EntityStore> user,  CommandBuffer<EntityStore> commandBuffer, InteractionContext context,ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
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
}
