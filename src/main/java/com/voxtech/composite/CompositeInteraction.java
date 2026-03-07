package com.voxtech.composite;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;

import javax.annotation.Nonnull;

public class CompositeInteraction implements JsonAssetWithMap<String, DefaultAssetMap<String, CompositeInteraction>> {

    private static AssetStore<String, CompositeInteraction, DefaultAssetMap<String, CompositeInteraction>> ASSET_STORE;

    public static AssetStore<String, CompositeInteraction, DefaultAssetMap<String, CompositeInteraction>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(CompositeInteraction.class);
        }

        return ASSET_STORE;
    }

    @Nonnull
    public static final AssetBuilderCodec<String, CompositeInteraction> CODEC = AssetBuilderCodec
            .builder(CompositeInteraction.class, CompositeInteraction::new, Codec.STRING,
                    (object, id) -> object.id = id,
                    object -> object.id,
                    (object, data) -> object.data = data,
                    object -> object.data)
            .appendInherited(new KeyedCodec<>("Name", Codec.STRING),
                    (object, name) -> object.name = name,
                    object -> object.name,
                    (object, parent) -> object.name = parent.name)
            .add()
            .appendInherited(new KeyedCodec<>("Description", Codec.STRING),
                    (object, description) -> object.description = description,
                    object -> object.description,
                    (object, parent) -> object.description = parent.description)
            .add()
            .build();

    private String id;
    private AssetExtraInfo.Data data;

    private String name;
    private String description;

    @Override
    public String getId() {
        return id;
    }
}
