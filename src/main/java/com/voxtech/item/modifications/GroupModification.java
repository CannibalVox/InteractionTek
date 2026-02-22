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
import com.voxtech.helpers.ItemTargetHelper;
import com.voxtech.interactions.ModifyItemInteraction;

import javax.annotation.Nonnull;

public class GroupModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<GroupModification> CODEC = BuilderCodec
        .builder(GroupModification.class, GroupModification::new, BASE_CODEC)
        .documentation("Execute a list of item modifications. Great to use with other modifications such as Singulate and Conditional.")
        .append(new KeyedCodec<>("ItemModifications", new ArrayCodec<>(ModifyItemInteraction.ItemModification.CODEC, ModifyItemInteraction.ItemModification[]::new)),
            (object, itemModifications) -> object.itemModifications = itemModifications,
            object -> object.itemModifications)
            .documentation("The modifications to execute. They will be executed in the order they are provided.")
            .addValidator(Validators.nonNull())
            .add()
        .append(new KeyedCodec<>("ContinueOnFailure", Codec.BOOLEAN),
            (object, continueOnFailure) -> object.continueOnFailure = continueOnFailure,
            object -> object.continueOnFailure)
            .documentation("If true, all modifications will be attempted, even if one fails. If false, execution will immediately halt if a modification fails.")
            .add()
        .build();

    private ModifyItemInteraction.ItemModification[] itemModifications;
    private boolean continueOnFailure;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        boolean retVal = true;
        for (ModifyItemInteraction.ItemModification modification : itemModifications) {
            if (!modification.modifyItemStack(world, ref, buffer, context, inventory, targetContainer, targetSlot, targetItem)) {
                if (!continueOnFailure) {
                    return false;
                }

                retVal = false;
            }

            ItemTargetHelper.TargetItemData refreshed = ItemTargetHelper.refreshTargetItem(context);
            targetContainer = refreshed.getContainer();
            targetSlot = refreshed.getSlot();
            targetItem = refreshed.getItemStack();
        }

        return retVal;
    }
}
