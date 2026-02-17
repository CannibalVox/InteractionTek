package com.voxtech.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;

import javax.annotation.Nonnull;

public class DurabilityMatcher extends ItemCondition.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<DurabilityMatcher> CODEC = BuilderCodec.builder(DurabilityMatcher.class, DurabilityMatcher::new, BASE_CODEC)
        .documentation("Used to match items that have a durability above or below a given threshold. Empty slots will fail.")
        .append(new KeyedCodec<>("LessThan", Codec.BOOLEAN),
            (object, lessThan) -> object.lessThan = lessThan,
            object -> object.lessThan)
            .documentation("When true, the matcher will pass if the target item's durability is less than or equal to the value. When false, if greater than or equal to the value.")
            .add()
        .append(new KeyedCodec<>("Value", Codec.DOUBLE),
            (object, value) -> object.value = value,
            object -> object.value)
            .documentation("The durability amount to compare the target item's durability against.")
            .addValidator(Validators.greaterThanOrEqual(0.0))
            .add()
        .append(new KeyedCodec<>("IgnoreNoDurability", Codec.BOOLEAN),
            (object, ignore) -> object.ignoreNoDurability = ignore,
            object -> object.ignoreNoDurability)
            .documentation("When true, the matcher will always pass if the target item's max durability is 0.")
            .add()
        .build();

    private boolean lessThan;
    private boolean ignoreNoDurability;
    private double value;

    @Override
    public boolean test0(Ref<EntityStore> user, ItemStack itemInHand, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        if (this.ignoreNoDurability && targetItem.getMaxDurability() == 0) {
            return true;
        }

        double durability = targetItem.getDurability();
        if (lessThan && durability <= value) {
            return true;
        }

        return !lessThan && durability >= value;
    }
}
