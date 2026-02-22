package com.voxtech.item.modifications;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;
import com.voxtech.interactions.ModifyItemInteraction;
import com.voxtech.protocol.ItemMatchType;

import javax.annotation.Nonnull;

public class ConditionalModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<ConditionalModification> CODEC = BuilderCodec
        .builder(ConditionalModification.class, ConditionalModification::new, BASE_CODEC)
        .documentation("Execute a list of item modifications only if the target item matches a set of item matchers. If the target item does not match, this modification doesn't fail, it simply does not take any action.")
        .append(new KeyedCodec<>("ItemMatchers", new ArrayCodec<>(ItemConditionInteraction.ItemMatcher.CODEC, ItemConditionInteraction.ItemMatcher[]::new)),
            (object, itemMatchers) -> object.itemMatchers = itemMatchers,
            object -> object.itemMatchers)
            .documentation("A set of conditions that must match the target item for the modifications to execute")
            .addValidator(Validators.nonNull())
            .add()
        .append(new KeyedCodec<>("ItemMatchType", new EnumCodec<>(ItemMatchType.class)),
            (object, itemMatchType) -> object.itemMatchType = itemMatchType,
            object -> object.itemMatchType)
            .documentation("Whether all of the conditions or just any need to match in order for the modifications to execute")
            .add()
        .append(new KeyedCodec<>("Modification",ModifyItemInteraction.ItemModification.CODEC),
            (object, modification) -> object.modification = modification,
            object -> object.modification)
            .documentation("The modification to execute")
            .addValidator(Validators.nonNull())
            .add()
        .build();

    private ItemMatchType itemMatchType = ItemMatchType.All;
    private ItemConditionInteraction.ItemMatcher[] itemMatchers;
    private ModifyItemInteraction.ItemModification modification;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        if (!targetItemMatches(ref, buffer, context, targetContainer, targetSlot, targetItem)) {
            return true;
        }

        return modification.modifyItemStack(world, ref, buffer, context, inventory, targetContainer, targetSlot, targetItem);
    }

    private boolean targetItemMatches(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        for (ItemConditionInteraction.ItemMatcher matcher : itemMatchers) {
            boolean success = matcher.test(ref, buffer, context, targetContainer, targetSlot, targetItem);
            if (success && itemMatchType == ItemMatchType.Any) {
                return true;
            }

            if (!success && itemMatchType == ItemMatchType.All) {
                return false;
            }
        }

        return itemMatchType != ItemMatchType.Any;
    }
}
