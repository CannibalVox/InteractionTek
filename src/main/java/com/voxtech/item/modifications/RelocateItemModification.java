package com.voxtech.item.modifications;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.ItemTargetHelper;
import com.voxtech.interactions.ModifyItemInteraction;
import com.voxtech.item.matchers.SlotMatcher;

import javax.annotation.Nonnull;

import static com.hypixel.hytale.server.core.inventory.Inventory.*;

public class RelocateItemModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<RelocateItemModification> CODEC = BuilderCodec
        .builder(RelocateItemModification.class, RelocateItemModification::new, BASE_CODEC)
        .documentation("This modification will move the current target item stack to any one slot where it can be placed in the provided inventory section. Then, the interaction chain's target item will change to the item's new slot. If the item cannot be placed successfully, the modification will fail.")
        .append(new KeyedCodec<>("InventorySectionId", Codec.INTEGER),
            (object, inventorySectionId) -> object.inventorySectionId = inventorySectionId,
            object -> object.inventorySectionId)
            .documentation("The inventory section id of the inventory section the target item should be relocated to")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.lessThan(0))
            .add()
        .append(new KeyedCodec<>("Slot", SlotMatcher.Slot.CODEC),
            (object, slotMatcher) -> object.slotMatcher = slotMatcher,
            object -> object.slotMatcher)
            .documentation("If provided, a slot must pass this matcher for the target item to be placed there.")
            .add()
        .append(new KeyedCodec<>("SkipRetarget", Codec.BOOLEAN),
            (object, skipRetarget) -> object.skipRetarget = skipRetarget,
    object -> object.skipRetarget)
            .documentation("If true, the target item slot will not be changed even if the relocation is successful.")
            .add()
        .append(new KeyedCodec<>("FlexibleQuantity", Codec.BOOLEAN),
            (object, flexibleQuantity) -> object.flexibleQuantity = flexibleQuantity,
            object -> object.flexibleQuantity)
            .documentation("If true, the first slot that will accept any of the current target item's quantity will be used, even if all of the quantity cannot fit. The remaining quantity will remain in place.")
            .add()
        .build();

    private int inventorySectionId;
    private SlotMatcher.Slot slotMatcher;
    private boolean skipRetarget;
    private boolean flexibleQuantity;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        ItemContainer toContainer = inventory.getSectionById(inventorySectionId);
        if (toContainer == null) {
            return false;
        }

        short activeSlot = switch (inventorySectionId) {
            case HOTBAR_SECTION_ID -> inventory.getActiveHotbarSlot();
            case UTILITY_SECTION_ID -> inventory.getActiveUtilitySlot();
            case TOOLS_SECTION_ID -> inventory.getActiveToolsSlot();
            default -> -1;
        };

        if (activeSlot >= 0 && attemptSlot(ref, buffer, context, targetContainer, toContainer, targetSlot, activeSlot, targetItem)) {
            return true;
        }

        for (short i = 0; i < toContainer.getCapacity(); i++) {
            if (i == activeSlot) {
                continue;
            }

            if (attemptSlot(ref, buffer, context, targetContainer, toContainer, targetSlot, activeSlot, targetItem)) {
                return true;
            }
        }

        return false;
    }

    private boolean attemptSlot(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, ItemContainer fromContainer, ItemContainer toContainer, short fromSlot, short toSlot, ItemStack targetItem) {
        // Is this a valid place to send the item?
        if (slotMatcher != null && !slotMatcher.test(ref, buffer, context, toContainer, toSlot, targetItem)) {
            return false;
        }

        if (!toContainer.canAddItemStackToSlot(toSlot, targetItem, !flexibleQuantity, true)) {
            return false;
        }

        // We can add so let's give it a shot
        MoveTransaction<SlotTransaction> transaction = fromContainer.moveItemStackFromSlotToSlot(fromSlot, targetItem.getQuantity(),toContainer, toSlot);

        if (transaction.succeeded() && !skipRetarget) {
            // Change the current target
            ItemTargetHelper.putTargetItem(context, new ItemTargetHelper.TargetItemData(toContainer, toSlot, transaction.getAddTransaction().getSlotAfter()));
        }

        return transaction.succeeded();
    }
}
