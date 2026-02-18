package com.voxtech.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class CategoryMatcher extends ItemCondition.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<CategoryMatcher> CODEC = BuilderCodec
        .builder(CategoryMatcher.class, CategoryMatcher::new, BASE_CODEC)
        .documentation("This matcher will succeed if the target item belongs to one of the provided categories.")
        .append(new KeyedCodec<>("Categories", new SetCodec<>(Codec.STRING, HashSet<String>::new, true)),
            (object, categories) -> object.categories = categories,
            object -> object.categories)
            .documentation("The list of categories to consider.")
            .addValidator(Validators.nonNull())
            .add()
        .build();

    private Set<String> categories;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        String[] itemCategories = targetItem.getItem().getCategories();

        for(String category : itemCategories) {
            if (categories.contains(category)) {
                return true;
            }
        }

        return false;
    }
}
