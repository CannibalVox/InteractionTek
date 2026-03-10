package com.voxtech.transactions.steps;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.*;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.protocol.TransactionStep;
import com.voxtech.transactions.TransactionState;
import com.voxtech.transactions.rollback.ItemStackRollback;
import com.voxtech.transactions.rollback.SpawnEntityRollback;

import javax.annotation.Nonnull;

public class ProvideItemsStep extends TransactionStep {

    @Nonnull
    public static final BuilderCodec<ProvideItemsStep> CODEC = BuilderCodec
        .builder(ProvideItemsStep.class, ProvideItemsStep::new, BASE_CODEC)
        .documentation("Add items with the provided qualities to the inventory of the specified entity.  Drop excess items on the ground.")
        .appendInherited(new KeyedCodec<>("ItemId", Codec.STRING),
            (object, itemId) -> object.itemId = itemId,
            object -> object.itemId,
            (object, parent) -> object.itemId = parent.itemId)
            .documentation("The itemId to fill the entity's inventory with.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonEmptyString())
            .addValidator(Item.VALIDATOR_CACHE.getValidator().late())
            .add()
        .appendInherited(new KeyedCodec<>("Quantity", Codec.INTEGER),
            (object, quantity) -> object.quantity = quantity,
            object -> object.quantity,
            (object, parent) -> object.quantity = parent.quantity)
            .documentation("The quantity to add to the entity's inventory.")
            .addValidator(Validators.greaterThanOrEqual(1))
            .add()
        .appendInherited(new KeyedCodec<>("State", Codec.STRING),
            (object, state) -> object.state = state,
            object -> object.state,
            (object, parent) -> object.state = parent.state)
            .documentation("If provided, the new items will of the provided item state.")
            .add()
        .appendInherited(new KeyedCodec<>("Durability", Codec.DOUBLE),
            (object, durability) -> object.durability = durability,
            object -> object.durability,
            (object, parent) -> object.durability = parent.durability)
            .documentation("If provided, the new items will have the provided durability")
            .add()
        .appendInherited(new KeyedCodec<>("InteractionTarget", new EnumCodec<>(InteractionTarget.class)),
            (object, interactionTarget) -> object.interactionTarget = interactionTarget,
            object -> object.interactionTarget,
                (object, parent) -> object.interactionTarget = parent.interactionTarget)
            .documentation("The interaction entity to add items to")
            .add()
        .appendInherited(new KeyedCodec<>("FailOnSpillover", Codec.BOOLEAN),
            (object, failOnSpillover) -> object.failOnSpillover = failOnSpillover,
            object -> object.failOnSpillover,
            (object, parent) -> object.failOnSpillover = parent.failOnSpillover)
            .documentation("If true, this transaction step will fail if the items cannot fit within the entity's inventory without dropping on the ground.")
            .add()
        .build();


    private String itemId;
    private int quantity = 1;
    private boolean failOnSpillover;
    private String state;
    private Double durability;
    private InteractionTarget interactionTarget = InteractionTarget.USER;

    @Override
    public boolean execute(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, TransactionState transaction, InteractionContext context, CooldownHandler cooldownHandler) {
        Item item = Item.getAssetMap().getAsset(itemId);
        if (item == null) {
            return false;
        }

        if (state != null) {
            item = item.getItemForState(state);
            if (item == null) {
                return false;
            }
        }

        ItemStack itemStack = new ItemStack(item.getId(), quantity);
        if (durability != null) {
            itemStack = itemStack.withDurability(durability);
        }

        Ref<EntityStore> targetEntity = interactionTarget.getEntity(context, ref);
        if (targetEntity == null) {
            return false;
        }

        CombinedItemContainer container = InventoryComponent.getCombined(commandBuffer, targetEntity, InventoryComponent.HOTBAR_FIRST);
        if (container.getCapacity() == 0) {
            return false;
        }

        ItemStackTransaction itemTransaction = container.addItemStack(itemStack);
        transaction.queueRollback(new ItemStackRollback(container, itemTransaction));

        ItemStack remainder = itemTransaction.getRemainder();
        if (remainder == null || remainder.getQuantity() == 0) {
            return true;
        }

        if (failOnSpillover) {
            return false;
        }

        Ref<EntityStore> dropEntity = ItemUtils.dropItem(ref, remainder, commandBuffer);
        transaction.queueRollback(new SpawnEntityRollback(dropEntity));

        return true;
    }
}
