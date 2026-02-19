package com.voxtech.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.helpers.ItemTargetHelper;
import com.voxtech.protocol.ItemMatchType;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;

import static com.hypixel.hytale.server.core.inventory.Inventory.*;

public class TargetFirstItem extends SimpleItemInteraction {

    @Nonnull
    public static final BuilderCodec<TargetFirstItem> CODEC = BuilderCodec
        .builder(TargetFirstItem.class, TargetFirstItem::new, SimpleItemInteraction.CODEC)
        .documentation("Scans the User entity's inventory until it finds an item slot that satisfies the provided matchers. If none is found, this interaction will fail.")
        .append(new KeyedCodec<>("ItemMatchers", new ArrayCodec<>(ItemCondition.ItemMatcher.CODEC, ItemCondition.ItemMatcher[]::new)),
            (object, itemMatchers) -> object.itemMatchers = itemMatchers,
            object -> object.itemMatchers)
            .documentation("The item matchers to compare against each slot")
            .addValidator(Validators.nonNull())
            .add()
        .append(new KeyedCodec<>("ItemMatchType", new EnumCodec<>(ItemMatchType.class)),
            (object, itemMatchType) -> object.itemMatchType = itemMatchType,
            object -> object.itemMatchType)
            .documentation("Whether all or any matchers need to match for a slot to be chosen")
            .add()
        .append(new KeyedCodec<>("InventorySections", new ArrayCodec<>(Codec.INTEGER, Integer[]::new)),
            (object, inventorySections) -> object.inventorySections = inventorySections,
            object -> object.inventorySections)
            .documentation("If provided, decides which sections to scan and what order to scan them in. A sensible default that scans all slots will be used otherwise.")
            .add()
        .build();

    private ItemCondition.ItemMatcher[] itemMatchers;
    private ItemMatchType itemMatchType = ItemMatchType.All;
    private Integer[] inventorySections = {HOTBAR_SECTION_ID, UTILITY_SECTION_ID, TOOLS_SECTION_ID, ARMOR_SECTION_ID, STORAGE_SECTION_ID, BACKPACK_SECTION_ID};

    @Override
    protected void interactWithItem(@NonNullDecl World world, @NonNullDecl CommandBuffer<EntityStore> buffer, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NullableDecl ItemStack itemInHand, @NullableDecl ItemContainer targetContainer, int targetSlot, @NullableDecl ItemStack targetItemStack, @NonNullDecl CooldownHandler cooldownHandler) {
        scanInventory(buffer, context);
    }

    @Override
    protected void simulateInteractWithItem(@NonNullDecl World world, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NullableDecl ItemStack itemInHand, @NullableDecl ItemContainer targetContainer, int targetSlot, @NullableDecl ItemStack targetItemStack) {
        scanInventory(context.getCommandBuffer(), context);
    }

    private void scanInventory(CommandBuffer<EntityStore> buffer, InteractionContext context) {
        Ref<EntityStore> user = context.getEntity();
        Entity entity = EntityUtils.getEntity(user, buffer);

        if (!(entity instanceof LivingEntity livingEntity)) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        Inventory inventory = livingEntity.getInventory();
        if (inventory == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        for (Integer inventorySectionId : inventorySections) {
            if (inventorySectionId == null) {
                continue;
            }

            ItemContainer container = inventory.getSectionById(inventorySectionId);
            if (container == null) {
                continue;
            }

            short activeSlot = switch (inventorySectionId) {
                case HOTBAR_SECTION_ID -> inventory.getActiveHotbarSlot();
                case UTILITY_SECTION_ID -> inventory.getActiveUtilitySlot();
                case TOOLS_SECTION_ID -> inventory.getActiveToolsSlot();
                default -> -1;
            };

            if (activeSlot >= 0 && checkItem(user, buffer, context, container, activeSlot)) {
                return;
            }

            for (short i = 0; i < container.getCapacity(); i++) {
                if (i == activeSlot) {
                    continue;
                }

                if (checkItem(user, buffer, context, container, i)) {
                    return;
                }
            }
        }

        context.getState().state = InteractionState.Failed;
    }

    private boolean checkItem(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, ItemContainer container, short slot) {
        ItemStack contents = container.getItemStack(slot);
        boolean matched = runMatchers(ref, buffer, context, container, slot, contents);

        if (matched) {
            ItemTargetHelper.putTargetItem(context, new ItemTargetHelper.TargetItemData(container, slot, contents));
        }

        return matched;
    }

    private boolean runMatchers(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, ItemContainer container, short slot, ItemStack contents) {
        for (ItemCondition.ItemMatcher matcher : itemMatchers) {
            boolean result = matcher.test(ref, buffer, context, container, slot, contents);
            if (!result && itemMatchType == ItemMatchType.All) {
                return false;
            }

            if (result && itemMatchType == ItemMatchType.Any) {
                return true;
            }
        }

        return itemMatchType != ItemMatchType.Any;
    }
}
