package com.voxtech.transactions.steps;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.InventoryHelper;
import com.voxtech.interactions.ItemConditionInteraction;
import com.voxtech.interactions.ModifyItemInteraction;
import com.voxtech.interactions.TransactionInteraction;
import com.voxtech.protocol.ItemMatchType;
import com.voxtech.transactions.TransactionState;

import javax.annotation.Nonnull;
import javax.naming.directory.ModificationItem;

public class ModifyItemStep extends TransactionInteraction.TransactionStep {

    @Nonnull
    public static final BuilderCodec<ModifyItemStep> CODEC = BuilderCodec
        .builder(ModifyItemStep.class, ModifyItemStep::new, BASE_CODEC)
        .documentation("This will scan the specified entity's inventory for a slot that matches the item matchers and then apply the provided modifications to the slot.  This step will fail if there is no matching slot, or if any of the modifications fail to apply.")
        .appendInherited(new KeyedCodec<>("Modifications", new ArrayCodec<>(ModifyItemInteraction.ItemModification.CODEC, ModifyItemInteraction.ItemModification[]::new)),
            (object, modifications) -> object.modifications = modifications,
            object -> object.modifications,
            (object, parent) -> object.modifications = parent.modifications)
            .documentation("The modifications to apply to a slot")
            .add()
        .appendInherited(new KeyedCodec<>("Matchers", new ArrayCodec<>(ItemConditionInteraction.ItemMatcher.CODEC, ItemConditionInteraction.ItemMatcher[]::new)),
            (object, itemMatchers) -> object.itemMatchers = itemMatchers,
            object -> object.itemMatchers,
            (object, parent) -> object.itemMatchers = parent.itemMatchers)
            .documentation("If provided, only a slot that clears these matchers will have the modifications applied to it")
            .add()
        .append(new KeyedCodec<>("MatchType", new EnumCodec<>(ItemMatchType.class)),
            (object, itemMatchType) -> object.itemMatchType = itemMatchType,
            object -> object.itemMatchType)
            .documentation("Whether a slot must match any or all of the matchers to have the modifications applied to it")
            .add()
        .append(new KeyedCodec<>("InteractionTarget", new EnumCodec<>(InteractionTarget.class)),
            (object, interactionTarget) -> object.interactionTarget = interactionTarget,
            object -> object.interactionTarget)
            .documentation("Which interaction entity should have its inventory modified by this step")
            .add()
        .append(new KeyedCodec<>("InventorySections", new ArrayCodec<>(Codec.INTEGER, Integer[]::new)),
            (object, sectionIds) -> object.sectionIds = sectionIds,
            object -> object.sectionIds)
            .documentation("Which inventory sections to scan and in which order. By default, this will scan the entity's hotbar and storage.")
            .add()
        .build();


    private ItemConditionInteraction.ItemMatcher[] itemMatchers;
    private ItemMatchType itemMatchType;
    private InteractionTarget interactionTarget;
    private Integer[] sectionIds = {-1, -2};
    private ModifyItemInteraction.ItemModification[] modifications;

    @Override
    public boolean execute(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, TransactionState transaction, InteractionContext context, CooldownHandler cooldownHandler) {
        Ref<EntityStore> targetRef = interactionTarget.getEntity(context, ref);
        if (targetRef == null) {
            return false;
        }

        World world = targetRef.getStore().getExternalData().getWorld();

        for (Integer sectionId : sectionIds) {
            ComponentType<EntityStore, ? extends InventoryComponent> componentType = InventoryComponent.getComponentTypeById(sectionId);
            if (componentType == null) {
                continue;
            }

            InventoryComponent component = commandBuffer.getComponent(targetRef, componentType);
            if (component == null) {
                continue;
            }

            short activeSlot = InventoryHelper.getActiveSlot(component);
            ItemContainer container = component.getInventory();
            if (container == null) {
                continue;
            }

            if (activeSlot >= 0) {
                ItemStack item = container.getItemStack(activeSlot);
                if (itemMatchers == null || InventoryHelper.executeMatchers(itemMatchers, itemMatchType, targetRef, commandBuffer, context, container, activeSlot, item)) {
                    return InventoryHelper.executeModifications(modifications, world, targetRef, commandBuffer, transaction, context, container, activeSlot, item, false);
                }
            }

            for (short i = 0; i < container.getCapacity(); i++) {
                if (i == activeSlot) {
                    continue;
                }

                ItemStack item = container.getItemStack(i);
                if (InventoryHelper.executeMatchers(itemMatchers, itemMatchType, targetRef, commandBuffer, context, container, i, item)) {
                    return InventoryHelper.executeModifications(modifications, world, targetRef, commandBuffer, transaction, context, container, i, item, false);
                }
            }
        }

        return false;
    }
}
