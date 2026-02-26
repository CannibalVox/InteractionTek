package com.voxtech.commands;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public interface Token {
    public String value(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context);
}
