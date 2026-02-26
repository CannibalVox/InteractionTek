package com.voxtech.commands;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class EntityComponentToken<T extends Component<EntityStore>> implements Token{
    private final PropertyValue<T> property;
    private final ComponentType<EntityStore, T> componentType;
    private final InteractionTarget target;

    public EntityComponentToken(ComponentType<EntityStore, T> componentType, InteractionTarget target, PropertyValue<T> property) {
        this.property = property;
        this.componentType = componentType;
        this.target = target;
    }

    @Override
    public String value(Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context) {
        Ref<EntityStore> entity = (target == null)?ref:target.getEntity(context, ref);
        if (entity == null) {
            return null;
        }

        T component = buffer.getComponent(entity, componentType);
        if (component == null) {
            return null;
        }

        return property.property(component);
    }
}
