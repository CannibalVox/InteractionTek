package com.voxtech.commands;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class EntityPropertyToken implements Token{
    private final InteractionTarget target;
    private final PropertyValue<Entity> property;

    public EntityPropertyToken( InteractionTarget target, PropertyValue<Entity> property) {
        this.property = property;
        this.target = target;
    }

    @Override
    public String value(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context) {
        Ref<EntityStore> entityRef = (target == null)?ref:target.getEntity(context, ref);
        if (entityRef == null) {
            return null;
        }

        Entity entity = EntityUtils.getEntity(entityRef, buffer);
        return property.property(entity);
    }
}
