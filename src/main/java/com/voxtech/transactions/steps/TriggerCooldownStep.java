package com.voxtech.transactions.steps;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionCooldown;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.TransactionInteraction;
import com.voxtech.transactions.TransactionState;
import com.voxtech.transactions.rollback.CooldownRollback;

import javax.annotation.Nonnull;

public class TriggerCooldownStep extends TransactionInteraction.TransactionStep {

    @Nonnull
    public static final BuilderCodec<TriggerCooldownStep> CODEC = BuilderCodec
            .builder(TriggerCooldownStep.class, TriggerCooldownStep::new, BASE_CODEC)
            .documentation("Trigger a cooldown for the current interaction chain with the specified qualities- if the cooldown is already ticking, this transaction step will fail.")
            .appendInherited(new KeyedCodec<>("Cooldown", RootInteraction.COOLDOWN_CODEC),
                (object, cooldown) -> object.cooldown = cooldown,
                object -> object.cooldown,
                (object, parent) -> object.cooldown = parent.cooldown)
                .documentation("The specification for the cooldown to trigger.")
                .add()
            .build();

    private InteractionCooldown cooldown;

    @Override
    public boolean execute(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, TransactionState transaction, InteractionContext context, CooldownHandler cooldownHandler) {
        String cooldownId = cooldown.cooldownId;
        float time = 0.35f;
        float[] charges = InteractionManager.DEFAULT_CHARGE_TIMES;
        boolean interruptRecharge = false;

        if (cooldownId == null) {
            InteractionChain chain = context.getChain();
            assert chain != null;

            RootInteraction rootInteraction = chain.getInitialRootInteraction();
            InteractionCooldown rootCooldown = rootInteraction.getCooldown();
            if (rootCooldown != null) {
                cooldownId = rootCooldown.cooldownId;
                if (rootCooldown.cooldown > 0.0f) {
                    time = rootCooldown.cooldown;
                }

                if (rootCooldown.interruptRecharge) {
                    interruptRecharge = true;
                }

                if (rootCooldown.chargeTimes != null && rootCooldown.chargeTimes.length > 0) {
                    charges = rootCooldown.chargeTimes;
                }
            }

            if (cooldownId == null) {
                cooldownId = rootInteraction.getId();
            }
        }

        CooldownHandler.Cooldown possibleCooldown = cooldownHandler.getCooldown(cooldownId);
        if (possibleCooldown != null) {
            if (possibleCooldown.hasCooldown(false)) {
                return false;
            }

            time = possibleCooldown.getCooldown();
            charges = possibleCooldown.getCharges();
            interruptRecharge = possibleCooldown.interruptRecharge();
        }

        if (cooldown.cooldown > 0.0f) {
            time = cooldown.cooldown;
        }

        if (cooldown.chargeTimes != null && cooldown.chargeTimes.length > 0) {
            charges = cooldown.chargeTimes;
        }

        if (cooldown.interruptRecharge) {
            interruptRecharge = true;
        }

        CooldownHandler.Cooldown newCooldown = cooldownHandler.getCooldown(cooldownId, time, charges, true, interruptRecharge);
        assert newCooldown != null;

        newCooldown.setCooldownMax(time);
        newCooldown.setCharges(charges);
        newCooldown.deductCharge();

        transaction.queueRollback(new CooldownRollback(cooldownId, possibleCooldown));

        return true;
    }
}
