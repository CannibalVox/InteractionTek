package com.voxtech.item.modifications;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ModifyItemInteraction;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import joptsimple.internal.Strings;

import javax.annotation.Nonnull;
import javax.naming.StringRefAddr;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeItemModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<ChangeItemModification> CODEC = BuilderCodec
        .builder(ChangeItemModification.class, ChangeItemModification::new, BASE_CODEC)
        .documentation("This modification will transform the target item between item states using a set of provided transformations.  Only the first matching transformation in the list will be executed. This modification will fail if no transformation can be executed.")
        .append(new KeyedCodec<>("Transformations", new ArrayCodec<>(ItemTransition.CODEC, ItemTransition[]::new)),
            (object, transformations) -> object.transformations = transformations,
    object -> object.transformations)
            .documentation("List of transformations to attempt.  The first matching SourceItem/SourceItemState to match the target item will cause the item to be converted to the provided TargetItem/TargetItemState")
            .add()
        .build();

    private ItemTransition[] transformations;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {

        String sourceItem = targetItem.getItemId();
        String sourceState = targetItem.getItem().getStateForItem(targetItem.getItemId());

        if (sourceState != null) {
            sourceItem = targetItem.getItem().getData().getParentKey().toString();
        }

        ItemTransition foundTransition = null;
        for (ItemTransition transition : transformations) {
            if (!sourceItem.equals(transition.sourceItem)) {
                continue;
            }

            // Match the state value: null matches null, "" matches everything
            if ((transition.sourceItemState == null && sourceState == null) ||
                "".equals(transition.sourceItemState)) {
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

        Item transformedItem = Item.getAssetMap().getAsset(foundTransition.targetItem);
        if (foundTransition.targetItemState != null && transformedItem != null) {
            transformedItem = transformedItem.getItemForState(foundTransition.targetItemState);
        }

        if (transformedItem == null) {
            return false;
        }

        ItemStack newItem = new ItemStack(transformedItem.getId(), targetItem.getQuantity(), targetItem.getMetadata());
        newItem = newItem.withDurability(targetItem.getDurability());

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
                .documentation("The itemid to match with the original item. If no State is specified, only the base item id with no item state will match.")
                .addValidator(Item.VALIDATOR_CACHE.getValidator().late())
                .addValidator(Validators.nonNull())
                .add()
            .append(new KeyedCodec<>("SourceItemState", Codec.STRING),
                (object, state) -> object.sourceItemState = state,
                object -> object.sourceItemState)
                .documentation("The item state to match. If it is null, then only the base item with no state will be matched. Otherwise, the * character can be used as a wildcard to match many item states. If this value is blank text, then all states in addition to the base item will be matched.")
                .add()
            .append(new KeyedCodec<>("TargetItem", Codec.STRING),
                (object, itemId) -> object.targetItem = itemId,
                object -> object.targetItem)
                .documentation("The itemid to transition matching items to.")
                .addValidator(Item.VALIDATOR_CACHE.getValidator().late())
                .addValidator(Validators.nonNull())
                .add()
            .append(new KeyedCodec<>("TargetItemState", Codec.STRING),
                (object, state) -> object.targetItemState = state,
                object -> object.targetItemState)
                .documentation("If included, the itemid will be transitioned to this item state under the TargetItem")
                .add()
            .afterDecode(object -> {
                if (Strings.isNullOrEmpty(object.sourceItemState)) {
                    return;
                }

                String[] splits = object.sourceItemState.split("\\*", -1);
                if (splits.length == 1 || "*".equals(splits[0])) {
                    return;
                }

                String pattern = Strings.join(() -> Arrays.stream(splits).map(v -> v.replaceAll("[\\W]", "\\\\$0")).iterator(), ".+");
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
