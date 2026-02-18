package com.voxtech.matchers;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemCondition;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class AssetTagMatcher extends ItemCondition.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<AssetTagMatcher> CODEC = BuilderCodec
        .builder(AssetTagMatcher.class, AssetTagMatcher::new, BASE_CODEC)
        .documentation("This item matcher will succeed if the target item has any of the provided asset tags.")
        .append(new KeyedCodec<>("AssetTags", new ArrayCodec<>(Codec.STRING, String[]::new)),
            (object, assetTags) -> object.assetTags = assetTags,
            object -> object.assetTags)
            .documentation("The asset tags to match against the item")
            .addValidator(Validators.nonNull())
            .add()
            .afterDecode(object -> {
                object.assetTagIndices = new ArrayList<>(object.assetTags.length);
                for(String tag : object.assetTags) {
                    object.assetTagIndices.add(AssetRegistry.getOrCreateTagIndex(tag));
                }
            })
        .build();

    private String[] assetTags;
    private ArrayList<Integer> assetTagIndices;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        AssetExtraInfo.Data data = targetItem.getItem().getData();
        if (data == null || assetTagIndices.isEmpty()) {
            return false;
        }

        for (Integer assetTagIndex : assetTagIndices) {
            if (assetTagIndex != null && data.getTags().containsKey(assetTagIndex.intValue())) {
                return true;
            }
        }

        return false;
    }
}
