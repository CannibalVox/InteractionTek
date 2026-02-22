package com.voxtech.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.ItemTargetHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModifyItemInteraction extends SimpleItemInteraction {

    @Nonnull
    public static final BuilderCodec<ModifyItemInteraction> CODEC = BuilderCodec
        .builder(ModifyItemInteraction.class, ModifyItemInteraction::new, SimpleInteraction.CODEC)
        .documentation("This interaction executes a series of modifications against the target item. This interaction will fail if any of the modifications fail.")
        .appendInherited(new KeyedCodec<>("ItemModifications", new ArrayCodec<>(ItemModification.CODEC, ItemModification[]::new)),
            (object, itemModifications) -> object.itemModifications = itemModifications,
            object -> object.itemModifications,
            (object, parent) -> object.itemModifications = parent.itemModifications)
            .documentation("The modifications to execute. They will be executed in the order they are provided")
            .addValidator(Validators.nonNull())
            .add()
        .append(new KeyedCodec<>("ContinueOnFailure", Codec.BOOLEAN),
            (object, continueOnFailure) -> object.continueOnFailure = continueOnFailure,
            object -> object.continueOnFailure)
            .documentation("If true, all modifications will be attempted, even if one fails. If false, execution will immediately halt if a modification fails.")
            .add()
        .append(new KeyedCodec<>("RequiredGameMode", new EnumCodec<>(GameMode.class)),
            (object, requiredGameMode) -> object.requiredGameMode = requiredGameMode,
            object -> object.requiredGameMode)
            .documentation("If the User entity is a player and they are not in this game mode, this interaction will do nothing but succeed. This setting is ignored for non-players.")
            .add()
        .build();

    private ItemModification[] itemModifications;
    private boolean continueOnFailure;
    private GameMode requiredGameMode;

    @Override
    protected void interactWithItem(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> buffer, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nullable ItemContainer targetContainer, int targetSlot, @Nullable ItemStack targetItemStack, @Nonnull CooldownHandler cooldownHandler) {
        if (targetContainer == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        Ref<EntityStore> ref = context.getEntity();

        if (requiredGameMode != null) {
            Player player = buffer.getComponent(ref, Player.getComponentType());
            if (player != null && player.getGameMode() != requiredGameMode) {
                return;
            }
        }

        Entity entity = EntityUtils.getEntity(ref, buffer);

        if (!(entity instanceof LivingEntity livingEntity)) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        Inventory inventory = livingEntity.getInventory();

        if (inventory == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        for (ItemModification modification : itemModifications) {
            boolean success = modification.modifyItemStack(world, context.getEntity(), buffer, context, inventory, targetContainer, (short)targetSlot, targetItemStack);

            ItemTargetHelper.TargetItemData refreshed = ItemTargetHelper.refreshTargetItem(context);
            targetContainer = refreshed.getContainer();
            targetSlot = refreshed.getSlot();
            targetItemStack = refreshed.getItemStack();

            if (!success) {
                context.getState().state = InteractionState.Failed;

                if (!continueOnFailure) {
                    break;
                }
            }
        }

        ItemTargetHelper.replaceTargetItem(context, targetItemStack);
    }

    @Override
    protected void simulateInteractWithItem(@Nonnull World world, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nullable ItemContainer targetContainer, int targetSlot, @Nullable ItemStack targetItemStack) {

    }

    public static abstract class ItemModification {
        @Nonnull
        public static final CodecMapCodec<ItemModification> CODEC = new CodecMapCodec<>("Type");

        @Nonnull
        public static final BuilderCodec<ItemModification> BASE_CODEC = BuilderCodec
            .abstractBuilder(ItemModification.class)
            .appendInherited(new KeyedCodec<>("FailEmptyItem", Codec.BOOLEAN),
                (object, failEmptyItem) -> object.failEmptyItem = failEmptyItem,
                object -> object.failEmptyItem,
                (object, parent) -> object.failEmptyItem = parent.failEmptyItem)
                .documentation("If true, this modification will automatically fail if an empty slot is passed to it.  If false, empty slots will not fail, even if it means that the modification can take no action.")
                .add()
            .build();

        private boolean failEmptyItem;

        public boolean modifyItemStack(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
            if (targetItem == null && failEmptyItem) {
                return false;
            } else if (targetItem == null && !canOperateOnEmpty()) {
                return true;
            }

            return modify0(world, ref, buffer, context, inventory, targetContainer, targetSlot, targetItem);
        }

        public abstract boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem);

        public boolean canOperateOnEmpty() {
            return false;
        }
    }
}
