package com.voxtech.transactions.steps;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.InventoryHelper;
import com.voxtech.protocol.ItemMatchType;
import com.voxtech.protocol.ItemMatcher;
import com.voxtech.protocol.TransactionStep;
import com.voxtech.transactions.TransactionState;
import com.voxtech.transactions.rollback.ItemSlotRollback;

import javax.annotation.Nonnull;

public class ConsumeItemsStep extends TransactionStep {

    @Nonnull
    public static final BuilderCodec<ConsumeItemsStep> CODEC = BuilderCodec
        .builder(ConsumeItemsStep.class, ConsumeItemsStep::new, BASE_CODEC)
        .documentation("Consume a specified quantity of items from the specified entity's inventory. Will fail if the specified entity does not exist or the full quantity cannot be consumed.")
        .appendInherited(new KeyedCodec<>("Matchers", new ArrayCodec<>(ItemMatcher.CODEC, ItemMatcher[]::new)),
            (object, matchers) -> object.matchers = matchers,
            object -> object.matchers,
            (object, parent) -> object.matchers = parent.matchers)
            .documentation("If provided, only consume items that fit these matchers.")
            .add()
        .append(new KeyedCodec<>("Quantity", Codec.INTEGER),
            (object, quantity) -> object.quantity = quantity,
            object -> object.quantity)
            .addValidator(Validators.greaterThanOrEqual(1))
            .add()
        .append(new KeyedCodec<>("MatchType", new EnumCodec<>(ItemMatchType.class)),
            (object, matchType) -> object.matchType = matchType,
            object -> object.matchType)
            .documentation("Whether a slot needs to match all or any of the matchers in the list to be consumed")
            .add()
        .append(new KeyedCodec<>("InteractionTarget", new EnumCodec<>(InteractionTarget.class)),
            (object, interactionTarget) -> object.interactionTarget = interactionTarget,
            object -> object.interactionTarget)
            .documentation("Which entity to consume items from")
            .add()
        .append(new KeyedCodec<>("InventorySections", new ArrayCodec<>(Codec.INTEGER, Integer[]::new)),
            (object, sectionIds) -> object.sectionIds = sectionIds,
            object -> object.sectionIds)
            .documentation("Which inventory sections to scan and in which order. By default, scans the hotbar and then the main storage.")
            .add()
        .build();

    private ItemMatcher[] matchers;
    private ItemMatchType matchType = ItemMatchType.All;
    private InteractionTarget interactionTarget = InteractionTarget.USER;
    private int quantity = 1;
    private Integer[] sectionIds = {-1, -2};

    @Override
    public boolean execute(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, TransactionState transaction, InteractionContext context, CooldownHandler cooldownHandler) {
        Ref<EntityStore> targetedEntity = interactionTarget.getEntity(context, ref);
        if (targetedEntity == null) {
            return false;
        }

        int quantityToConsume = quantity;
        for(int sectionId : sectionIds) {
            ComponentType<EntityStore, ? extends InventoryComponent> componentType = InventoryComponent.getComponentTypeById(sectionId);
            if (componentType == null) {
                continue;
            }

            InventoryComponent component = commandBuffer.getComponent(targetedEntity, componentType);
            if (component == null) {
                continue;
            }

            short activeSlot = InventoryHelper.getActiveSlot(component);
            ItemContainer container = component.getInventory();
            if (container == null) {
                continue;
            }

            if (activeSlot >= 0) {
                quantityToConsume = tryConsumeSlot(ref, commandBuffer, context, transaction, container, activeSlot, quantityToConsume);
                if (quantityToConsume == 0) {
                    break;
                }
            }

            for (short i = 0; i < container.getCapacity(); i++) {
                if (i == activeSlot) {
                    continue;
                }

                quantityToConsume = tryConsumeSlot(ref, commandBuffer, context, transaction, container, i, quantityToConsume);
                if (quantityToConsume == 0) {
                    break;
                }
            }
        }

        return quantityToConsume == 0;
    }

    private int tryConsumeSlot(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, TransactionState transaction, ItemContainer container, short slot, int quantityToConsume) {
        ItemStack stack = container.getItemStack(slot);

        if (matchers != null && !InventoryHelper.executeMatchers(matchers, matchType, ref, commandBuffer, context, container, slot, stack)) {
            return quantityToConsume;
        }

        ItemStackSlotTransaction itemTransaction = container.removeItemStackFromSlot(slot, stack, quantityToConsume, false, true);
        transaction.queueRollback(new ItemSlotRollback(container, itemTransaction));

        ItemStack remainder = itemTransaction.getRemainder();
        if (remainder == null) {
            return 0;
        }
        return remainder.getQuantity();
    }
}
