package com.voxtech.item.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolMatcher extends ItemConditionInteraction.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<ToolMatcher> CODEC = BuilderCodec
        .builder(ToolMatcher.class, ToolMatcher::new, BASE_CODEC)
        .documentation("This matcher will succeed if the target item is a tool")
        .appendInherited(new KeyedCodec<>("ToolConditions", new ArrayCodec<>(ToolCondition.CODEC, ToolCondition[]::new)),
            (object, toolConditions) -> object.toolConditions = toolConditions,
            object -> object.toolConditions,
            (object, parent) -> object.toolConditions = parent.toolConditions)
            .documentation("If included, this matcher will only succeed if one of the tool's specs matches one of the provided conditions.")
            .add()
        .afterDecode(object -> {
            object.toolConditionsMap = new HashMap<>();

            if (object.toolConditions == null) {
                return;
            }

            for (ToolCondition condition : object.toolConditions) {
                if (!object.toolConditionsMap.containsKey(condition.gatherType)) {
                    object.toolConditionsMap.put(condition.gatherType, new ArrayList<>());
                }
                List<ToolCondition> conditionList = object.toolConditionsMap.get(condition.gatherType);
                conditionList.add(condition);
            }
        })
        .build();

    private ToolCondition[] toolConditions;
    private Map<String, List<ToolCondition>> toolConditionsMap;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        ItemTool tool = targetItem.getItem().getTool();
        if (tool == null) {
            return false;
        }

        if (toolConditions == null || toolConditions.length == 0) {
            return true;
        }

        for (ItemToolSpec spec : tool.getSpecs()) {
            if (spec.getGatherType() == null || spec.getGatherType().isEmpty() || !toolConditionsMap.containsKey(spec.getGatherType())) {
                continue;
            }

            List<ToolCondition> matchingConditions = toolConditionsMap.get(spec.getGatherType());
            for (ToolCondition condition : matchingConditions) {
                if (condition.matches(spec)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static class ToolCondition {

        @Nonnull
        public static final BuilderCodec<ToolCondition> CODEC = BuilderCodec.
            builder(ToolCondition.class, ToolCondition::new)
            .documentation("Specifies a set of requirements to compare a tool's specs against")
            .appendInherited(new KeyedCodec<>("GatherType", Codec.STRING),
                (object, gatherType) -> object.gatherType = gatherType,
                object -> object.gatherType,
                (object, parent) -> object.gatherType = parent.gatherType)
                .documentation("This tool condition will only match a tool spec if the spec's gather type matches this value")
                .addValidator(Validators.nonNull())
                .addValidator(Validators.nonEmptyString())
                .add()
            .append(new KeyedCodec<>("MinimumQuality", Codec.INTEGER),
                (object, minimumQuality) -> object.minimumQuality = minimumQuality,
                object -> object.minimumQuality)
                .documentation("If provided, a tool spec's quality must be greater than or equal to this value in order to match")
                .addValidator(Validators.greaterThanOrEqual(0))
                .add()
            .build();

        private String gatherType;
        private Integer minimumQuality;

        public boolean matches(ItemToolSpec spec) {
            if (!gatherType.equals(spec.getGatherType())) {
                return false;
            }

            return (minimumQuality == null || spec.getQuality() >= minimumQuality);
        }
    }
}
