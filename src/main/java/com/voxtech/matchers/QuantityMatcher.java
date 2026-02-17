package com.voxtech.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;

public class QuantityMatcher extends ItemCondition.ItemMatcher {
    public static final BuilderCodec<QuantityMatcher> CODEC = BuilderCodec.builder(QuantityMatcher.class, QuantityMatcher::new, BASE_CODEC)
            .documentation("Used to match item stacks that have a quantity above or below a given threshold. Always fails empty slots.")
            .append(new KeyedCodec<>("LessThan", Codec.BOOLEAN),
                    (object, lessThan) -> object.lessThan = lessThan,
                    object -> object.lessThan)
            .documentation("When true, the matcher will pass if the target item stack's quantity is less than or equal to the value. When false, if greater than or equal to the value.")
            .add()
            .append(new KeyedCodec<>("Value", Codec.INTEGER),
                    (object, value) -> object.value = value,
                    object -> object.value)
            .documentation("The quantity to compare the target item stack's quantity against.")
            .add()
            .build();

    private boolean lessThan;
    private int value;

    @Override
    public boolean test0(Ref<EntityStore> user, ItemStack itemInHand, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        if (targetItem == null) {
            return false;
        }

        int quantity = targetItem.getQuantity();
        if (lessThan && quantity <= value) {
            return true;
        }

        return (!lessThan && quantity >= value);
    }
}
