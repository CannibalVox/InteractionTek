package com.voxtech.interactions;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.ItemTargetHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SimpleItemInteraction extends SimpleInteraction {
    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        if (firstRun) {
            ItemTargetHelper.TargetItemData targetItem = ItemTargetHelper.getTargetItem(context);
            Ref<EntityStore> ref = context.getEntity();
            CommandBuffer<EntityStore> buffer = context.getCommandBuffer();
            ItemStack itemInHand = null;

            assert buffer != null;

            World world = buffer.getExternalData().getWorld();

            if (EntityUtils.getEntity(ref, buffer) instanceof LivingEntity livingEntity) {
                Inventory inventory = livingEntity.getInventory();
                itemInHand = inventory.getItemInHand();
            }

            this.interactWithItem(world, buffer, type, context, itemInHand, targetItem.getContainer(), targetItem.getSlot(), targetItem.getItemStack(), cooldownHandler);
            super.tick0(firstRun, time, type, context, cooldownHandler);
        }
    }

    @Override
    protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        if (firstRun) {
            ItemTargetHelper.TargetItemData targetItem = ItemTargetHelper.getTargetItem(context);
            Ref<EntityStore> ref = context.getEntity();
            CommandBuffer<EntityStore> buffer = context.getCommandBuffer();
            ItemStack itemInHand = null;

            assert buffer != null;

            World world = buffer.getExternalData().getWorld();

            if (EntityUtils.getEntity(ref, buffer) instanceof LivingEntity livingEntity) {
                Inventory inventory = livingEntity.getInventory();
                itemInHand = inventory.getItemInHand();
            }

            this.simulateInteractWithItem(world, type, context, itemInHand, targetItem.getContainer(), targetItem.getSlot(), targetItem.getItemStack());
            super.tick0(firstRun, time, type, context, cooldownHandler);
        }
    }

    protected abstract void interactWithItem(
        @Nonnull World world,
        @Nonnull CommandBuffer<EntityStore> buffer,
        @Nonnull InteractionType type,
        @Nonnull InteractionContext context,
        @Nullable ItemStack itemInHand,
        @Nullable ItemContainer targetContainer,
        int targetSlot,
        @Nullable ItemStack targetItemStack,
        @Nonnull CooldownHandler cooldownHandler
    );

    protected abstract void simulateInteractWithItem(
        @Nonnull World world,
        @Nonnull InteractionType type,
        @Nonnull InteractionContext context,
        @Nullable ItemStack itemInHand,
        @Nullable ItemContainer targetContainer,
        int targetSlot,
        @Nullable ItemStack targetItemStack
    );
}
