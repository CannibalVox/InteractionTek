package com.voxtech.item.modifications;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ModifyItemInteraction;
import joptsimple.internal.Strings;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeItemModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<ChangeItemModification> CODEC = BuilderCodec
        .builder(ChangeItemModification.class, ChangeItemModification::new, BASE_CODEC)
        .documentation("This modification will transform the target item between item states using a set of provided transformations.  Only the first matching transformation in the list will be executed. This modification will fail if no transformation can be executed.")
        .appendInherited(new KeyedCodec<>("Transformations", new ArrayCodec<>(ItemTransition.CODEC, ItemTransition[]::new)),
            (object, transformations) -> object.transformations = transformations,
    object -> object.transformations,
            (object, parent) -> object.transformations = parent.transformations)
            .documentation("List of transformations to attempt.  The first matching SourceItem/SourceItemState to match the target item will cause the item to be converted to the provided TargetItem/TargetItemState")
            .add()
        .append(new KeyedCodec<>("DropDurability", Codec.BOOLEAN),
            (object, keepDurability) -> object.keepDurability = keepDurability,
            object -> object.keepDurability)
            .documentation("If true, the modification will attempt to retain the item's durability during transitions between different item id's. Differing maximum durabilities may cause unusual results.")
            .add()
        .append(new KeyedCodec<>("DropDurabilitySameItem", Codec.BOOLEAN),
            (object, keepDurabilitySameItem) -> object.keepDurabilitySameItem = keepDurabilitySameItem,
            object -> object.keepDurabilitySameItem)
            .documentation("Defaults to true. When true, the modification will retain the item's durability during transitions between different states of the same item.")
            .add()
        .build();

    private ItemTransition[] transformations;
    private boolean keepDurability = true;
    private boolean keepDurabilitySameItem;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {

        String originalItemId = targetItem.getItemId();
        String sourceState = targetItem.getItem().getStateForItem(targetItem.getItemId());
        String sourceItem = originalItemId;

        if (sourceState != null) {
            sourceItem = targetItem.getItem().getData().getParentKey().toString();
        }

        ItemTransition foundTransition = null;
        for (ItemTransition transition : transformations) {
            if (transition.sourceItem != null && !transition.sourceItem.equals(sourceItem)) {
                continue;
            }

            // Match the state value: null matches null
            if ((transition.sourceItemState == null || transition.sourceItemState.equals("*")) && sourceState == null) {
                foundTransition = transition;
                break;
            }

            if (transition.sourceItemState == null || sourceState == null) {
                continue;
            }

            // Wildcard matching
            if (transition.sourceItemStateCompare != null) {
                Matcher matcher = transition.sourceItemStateCompare.matcher(sourceState);
                if (matcher.find()) {
                    foundTransition = transition;
                    break;
                }

                continue;
            }

            if (transition.sourceItemState.equals(sourceState)) {
                foundTransition = transition;
                break;
            }
        }

        if (foundTransition == null) {
            return false;
        }

        if (foundTransition.targetItem == null) {
            SlotTransaction transaction = targetContainer.removeItemStackFromSlot(targetSlot);
            return transaction.succeeded();
        }

        Item transformedItem = Item.getAssetMap().getAsset(foundTransition.targetItem);
        if (foundTransition.targetItemState != null && transformedItem != null) {
            transformedItem = transformedItem.getItemForState(foundTransition.targetItemState);
        }

        if (transformedItem == null) {
            return false;
        }

        // No-op
        if (transformedItem.getId().equals(originalItemId)) {
            return true;
        }

        ItemStack newItem = new ItemStack(transformedItem.getId(), targetItem.getQuantity(), targetItem.getMetadata());

        boolean sameItemId = foundTransition.targetItem.equals(sourceItem);
        if ((!sameItemId && keepDurability) || (sameItemId && keepDurabilitySameItem)) {
            // Carry over durability if we still have the same parent item or if user requested it
            newItem = newItem.withDurability(targetItem.getDurability());
        }

        ItemStackSlotTransaction slot = targetContainer.setItemStackForSlot(targetSlot, newItem);
        return slot.succeeded();
    }

    public static class ItemTransition {

        @Nonnull
        public static final BuilderCodec<ItemTransition> CODEC = BuilderCodec
            .builder(ItemTransition.class, ItemTransition::new)
            .documentation("A conversion from one item type to another, with item states optionally included")
            .append(new KeyedCodec<>("SourceItem", Codec.STRING),
                (object, itemId) -> object.sourceItem = itemId,
                object -> object.sourceItem)
                .documentation("The itemId to match with the original item. If left empty, all items will match.")
                .addValidator(Item.VALIDATOR_CACHE.getValidator().late())
                .add()
            .append(new KeyedCodec<>("SourceItemState", Codec.STRING),
                (object, state) -> object.sourceItemState = state,
                object -> object.sourceItemState)
                .documentation("The item state to match. If it is null, then only the base item with no state will be matched. Otherwise, the * character can be used as a wildcard to match many item states. If the text is only a wildcard, the base item will also be matched.")
                .add()
            .append(new KeyedCodec<>("TargetItem", Codec.STRING),
                (object, itemId) -> object.targetItem = itemId,
                object -> object.targetItem)
                .documentation("If included, the itemId to change the target item to. If left null, the target item will be removed.")
                .addValidator(Item.VALIDATOR_CACHE.getValidator().late())
                .add()
            .append(new KeyedCodec<>("TargetItemState", Codec.STRING),
                (object, state) -> object.targetItemState = state,
                object -> object.targetItemState)
                .documentation("If included, the itemId will be transitioned to this item state under the TargetItem")
                .add()
            .afterDecode(object -> {
                if (Strings.isNullOrEmpty(object.sourceItemState)) {
                    return;
                }

                String[] splits = object.sourceItemState.split("\\*", -1);
                if (splits.length == 1 || "*".equals(splits[0])) {
                    return;
                }

                String pattern = Strings.join(() -> Arrays.stream(splits).map(v -> v.replaceAll("\\W", "\\\\$0")).iterator(), ".+");
                object.sourceItemStateCompare = Pattern.compile(pattern);
            })
            .build();

        private String sourceItem;
        private String sourceItemState;
        private String targetItem;
        private String targetItemState;

        private Pattern sourceItemStateCompare;
    }
}
