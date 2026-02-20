package com.voxtech.item.matchers;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ItemResourceType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ItemConditionInteraction;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ResourceMatcher extends ItemConditionInteraction.ItemMatcher {

    @Nonnull
    public static final BuilderCodec<ResourceMatcher> CODEC = BuilderCodec
        .builder(ResourceMatcher.class, ResourceMatcher::new, BASE_CODEC)
        .documentation("This matcher will succeed if the item has resource types")
        .append(new KeyedCodec<>("ResourceConditions", new ArrayCodec<>(ResourceCondition.CODEC, ResourceCondition[]::new)),
            (object, resourceConditions) -> object.resourceConditions = resourceConditions,
            object -> object.resourceConditions)
            .documentation("If provided, the matcher will only succeed if one of the item's resource types matches one of the provided resource conditions")
            .add()
        .afterDecode(object -> {
            if (object.resourceConditions == null) {
                object.resourceConditionMap = new HashMap<>();
                return;
            }
            object.resourceConditionMap = new HashMap<>(object.resourceConditions.length);
            for (ResourceCondition condition : object.resourceConditions) {
                object.resourceConditionMap.put(condition.resourceId, condition);
            }
        })
        .build();

    private ResourceCondition[] resourceConditions;
    private Map<String, ResourceCondition> resourceConditionMap;

    @Override
    public boolean test0(Ref<EntityStore> user, CommandBuffer<EntityStore> commandBuffer, InteractionContext context, ItemContainer targetContainer, int targetSlot, ItemStack targetItem) {
        ItemResourceType[] resources = targetItem.getItem().getResourceTypes();

        if (resources == null || resources.length == 0) {
            return false;
        }

        if (resourceConditions == null || resourceConditions.length == 0) {
            return true;
        }

        for (ItemResourceType itemResource : resources) {
            ResourceCondition condition = resourceConditionMap.get(itemResource.id);
            if (condition != null && condition.test(itemResource)) {
                return true;
            }
        }

        return false;
    }

    public static class ResourceCondition {

        @Nonnull
        public static final BuilderCodec<ResourceCondition> CODEC = BuilderCodec
            .builder(ResourceCondition.class, ResourceCondition::new)
            .documentation("The conditions to match against an item's resource type")
            .append(new KeyedCodec<>("ResourceId", Codec.STRING),
                (object, resourceId) -> object.resourceId = resourceId,
                object -> object.resourceId)
                .documentation("The resource type to match")
                .addValidator(Validators.nonNull())
                .addValidator(Validators.nonEmptyString())
                .add()
            .append(new KeyedCodec<>("MinimumQuantity", Codec.INTEGER),
                (object, minimumQuantity) -> object.minimumQuantity = minimumQuantity,
                object -> object.minimumQuantity)
                .documentation("If provided, the matching item resource must also provided at least this much of the resource")
                .addValidator(Validators.greaterThanOrEqual(1))
                .add()
            .build();

        private String resourceId;
        private Integer minimumQuantity;

        public boolean test(ItemResourceType resourceType) {
            if (resourceId == null || !resourceId.equals(resourceType.id)) {
                return false;
            }

            return (minimumQuantity == null || resourceType.quantity >= minimumQuantity);
        }
    }
}
