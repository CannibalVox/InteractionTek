package com.voxtech.transactions.steps;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.Object2FloatMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ValueType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.protocol.TransactionStep;
import com.voxtech.transactions.TransactionState;
import com.voxtech.transactions.rollback.StatRollback;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

import javax.annotation.Nonnull;

public class ChangeStatsStep extends TransactionStep {

    @Nonnull
    public static final BuilderCodec<ChangeStatsStep> CODEC = BuilderCodec
        .builder(ChangeStatsStep.class, ChangeStatsStep::new, BASE_CODEC)
        .documentation("Apply delta values to the target entity's stats.  If any of the provided failure conditions are met, fail this transaction step.")
        .appendInherited(new KeyedCodec<>("StatDeltas", new Object2FloatMapCodec<>(Codec.STRING, Object2FloatOpenHashMap::new), true),
            (object, statAssets) -> object.statAssets = statAssets,
            object -> object.statAssets,
            (object, parent) -> object.statAssets = parent.statAssets)
            .documentation("The delta values to apply to a set of provided stats. Deltas can be either positive or negative. The final stat values will be clamped between the entity's minimum and maximum values.")
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonEmptyMap())
            .addValidator(EntityStatType.VALIDATOR_CACHE.getMapKeyValidator())
            .add()
        .appendInherited(new KeyedCodec<>("ValueType", new EnumCodec<>(ValueType.class)),
            (object, valueType) -> object.valueType = valueType,
            object -> object.valueType,
            (object, parent) -> object.valueType = parent.valueType)
            .documentation("Whether the delta values are in absolute stat values or percentages")
            .add()
        .appendInherited(new KeyedCodec<>("InteractionTarget", new EnumCodec<>(InteractionTarget.class)),
            (object, interactionTarget) -> object.interactionTarget = interactionTarget,
            object -> object.interactionTarget,
            (object, parent) -> object.interactionTarget = parent.interactionTarget)
            .documentation("Which entity to modify the stats of.")
            .add()
        .appendInherited(new KeyedCodec<>("FailOnExhaust", Codec.BOOLEAN),
            (object, failOnExhaust) -> object.failOnExhaust = failOnExhaust,
            object -> object.failOnExhaust,
            (object, parent) -> object.failOnExhaust = parent.failOnExhaust)
            .documentation("Defaults to true. If true, this transaction step will fail if the change would cause a stat to become exhausted (reach minimum)")
            .add()
        .appendInherited(new KeyedCodec<>("FailOnAlreadyExhausted", Codec.BOOLEAN),
            (object, failOnAlreadyExhausted) -> object.failOnAlreadyExhausted = failOnAlreadyExhausted,
            object -> object.failOnAlreadyExhausted,
            (object, parent) -> object.failOnAlreadyExhausted = parent.failOnAlreadyExhausted)
            .documentation("If true, this transaction step will fail if it attempts to subtract from stats that were already exhausted (at minimum) before the transaction began")
            .add()
        .appendInherited(new KeyedCodec<>("ExhaustAtZero", Codec.BOOLEAN),
            (object, exhaustAtZero) -> object.exhaustAtZero = exhaustAtZero,
            object -> object.exhaustAtZero,
            (object, parent) -> object.exhaustAtZero = parent.exhaustAtZero)
            .documentation("Defaults to true. If true, stats will count as exhausted if they are at are below zero, even if they are not at minimum")
            .add()
        .appendInherited(new KeyedCodec<>("FailOnOvercap", Codec.BOOLEAN),
            (object, failOnOvercap) -> object.failOnOvercap = failOnOvercap,
            object -> object.failOnOvercap,
            (object, parent) -> object.failOnOvercap = parent.failOnOvercap)
            .documentation("If true, this transaction step will fail if the change would cause a stat to become overcapped (surpass maximum). Even if false, stats will be clamped to their maximum value.")
            .add()
        .appendInherited(new KeyedCodec<>("FailOnAlreadyCapped", Codec.BOOLEAN),
            (object, failOnAlreadyCapped) -> object.failOnAlreadyCapped = failOnAlreadyCapped,
            object -> object.failOnAlreadyCapped,
            (object, parent) -> object.failOnAlreadyCapped = parent.failOnAlreadyCapped)
            .documentation("If true, this transaction step will fail if it attempts to add to stats that were already at their maximum.")
            .add()
        .afterDecode(object -> {
            object.entityStats = EntityStatsModule.resolveEntityStats(object.statAssets);
        })
        .build();

    private Object2FloatMap<String> statAssets;
    private Int2FloatMap entityStats;
    private ValueType valueType = ValueType.Absolute;
    private InteractionTarget interactionTarget = InteractionTarget.USER;
    private boolean failOnOvercap;
    private boolean failOnExhaust = true;
    private boolean failOnAlreadyExhausted;
    private boolean failOnAlreadyCapped;
    private boolean exhaustAtZero = true;

    @Override
    public boolean execute(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer, TransactionState transaction, InteractionContext context, CooldownHandler cooldownHandler) {
        Ref<EntityStore> targetRef = interactionTarget.getEntity(context, ref);
        if (targetRef == null) {
            return false;
        }

        EntityStatMap stats = commandBuffer.getComponent(targetRef, EntityStatMap.getComponentType());
        if (stats == null) {
            return false;
        }

        for (Int2FloatMap.Entry entry : entityStats.int2FloatEntrySet()) {
                int statIndex = entry.getIntKey();
            float delta = entry.getFloatValue();

            EntityStatValue stat = stats.get(statIndex);
            if (stat == null) {
                return false;
            }

            if (valueType == ValueType.Percent) {
                delta = delta * (stat.getMax() - stat.getMin()) / 100.0f;
            }

            float oldAmount = stat.get();
            float amount = oldAmount + delta;

            boolean wasExhausted = (oldAmount <= 0 && exhaustAtZero) || oldAmount <= stat.getMin();
            boolean isExhausted = (amount <= 0 && exhaustAtZero) || amount < stat.getMin();

            if (failOnAlreadyExhausted && delta < 0 && wasExhausted) {
                return false;
            }

            if (failOnExhaust && delta < 0 && isExhausted) {
                return false;
            }

            if (failOnOvercap && delta > 0 && (amount > stat.getMax())) {
                return false;
            }

            if (failOnAlreadyCapped && delta >0 && oldAmount >= stat.getMax()) {
                return false;
            }

            stats.addStatValue(EntityStatMap.Predictable.SELF, statIndex, delta);
            transaction.queueRollback(new StatRollback(targetRef, statIndex, oldAmount));
        }

        return true;
    }
}
