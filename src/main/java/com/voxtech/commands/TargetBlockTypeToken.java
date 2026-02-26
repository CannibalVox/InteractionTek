package com.voxtech.commands;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TargetBlockTypeToken implements Token {
    private PropertyValue<BlockType> property;

    public TargetBlockTypeToken(PropertyValue<BlockType> property) {
        this.property = property;
    }

    @Override
    public String value(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context) {
        BlockPosition pos = context.getTargetBlock();
        if (pos == null) {
            return null;
        }

        World world = ref.getStore().getExternalData().getWorld();
        BlockType block = world.getBlockType(pos.x, pos.y, pos.z);
        if (block == null) {
            return null;
        }

        return property.property(block);
    }
}
