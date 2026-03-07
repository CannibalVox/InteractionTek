package com.voxtech.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;

import javax.annotation.Nonnull;

public class CustomInteraction extends SimpleInteraction {

    @Nonnull
    public static final BuilderCodec<CustomInteraction> CODEC = BuilderCodec
            .builder(CustomInteraction.class, CustomInteraction::new, SimpleInteraction.CODEC)
            .documentation("SendMessage in a weird way.")
            .append(new KeyedCodec<>("Message", Codec.STRING),
                    (object, message) -> object.message = message,
                    object -> object.message)
            .add()
            .build();

    private String message;

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        super.tick0(firstRun, time, type, context, cooldownHandler);
    }
}
