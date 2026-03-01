package com.voxtech.transactions.rollback;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionCooldown;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Field;

public class CooldownRollback implements RollbackEntry {
    private final String id;
    private final CooldownHandler.Cooldown cooldown;

    public CooldownRollback(String id, CooldownHandler.Cooldown cooldown) {
        this.id = id;
        this.cooldown = cooldown;
    }

    @Override
    public void rollback(CommandBuffer<EntityStore> buffer, InteractionContext context, CooldownHandler cooldownHandler) {
        if (cooldown == null) {
            CooldownHandler.Cooldown removeCooldown = cooldownHandler.getCooldown(id);
            if (removeCooldown != null) {
                while (!removeCooldown.tick(1000));
            }
            return;
        }

        CooldownHandler.Cooldown newCooldown = cooldownHandler.getCooldown(id, cooldown.getCooldown(), cooldown.getCharges(), true, cooldown.interruptRecharge());
        assert newCooldown != null;

        newCooldown.setCooldownMax(cooldown.getCooldown());
        newCooldown.setCharges(cooldown.getCharges());

        copyField(cooldown, newCooldown, "chargeCount");
        copyField(cooldown, newCooldown, "remainingCooldown");
        copyField(cooldown, newCooldown, "chargeTimer");
    }

    private void copyField(CooldownHandler.Cooldown from, CooldownHandler.Cooldown to, String fieldName) {
        Class<?> cooldownClass = from.getClass();

        try {
            Field chargeCountField = cooldownClass.getField("chargeCount");
            chargeCountField.setAccessible(true);
            chargeCountField.set(to, chargeCountField.get(from));
        } catch(NoSuchFieldException | IllegalAccessException ex) {
            throw new RuntimeException("Internal error", ex);
        }
    }
}
