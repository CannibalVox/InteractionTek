package com.voxtech.commands;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TargetBlockPositionToken implements Token {
    private PropertyValue<BlockPosition> property;

    public TargetBlockPositionToken(PropertyValue<BlockPosition> property) {
        this.property = property;
    }

    @Override
    public String value(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context) {
        BlockPosition targetBlock = context.getTargetBlock();
        if (targetBlock == null) {
            return null;
        }

        return property.property(targetBlock);
    }
}
