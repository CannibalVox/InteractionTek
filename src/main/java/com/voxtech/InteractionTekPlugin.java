package com.voxtech;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.voxtech.interactions.ItemCondition;
import com.voxtech.interactions.TargetFirstItem;
import com.voxtech.matchers.*;
import com.voxtech.matchers.slot.ActiveHotbarMatcher;
import com.voxtech.matchers.slot.ActiveUtilityMatcher;
import com.voxtech.matchers.slot.IndexedSlotMatcher;
import com.voxtech.matchers.slot.InteractionHeldItemMatcher;

import javax.annotation.Nonnull;

public class InteractionTekPlugin extends JavaPlugin {

    public InteractionTekPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Interaction.CODEC.register("TekItemCondition", ItemCondition.class, ItemCondition.CODEC);
        Interaction.CODEC.register("TekTargetFirstItem", TargetFirstItem.class, TargetFirstItem.CODEC);

        ItemCondition.ItemMatcher.CODEC.register("Durability", DurabilityMatcher.class, DurabilityMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("EmptySlot", EmptySlotMatcher.class, EmptySlotMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Quantity", QuantityMatcher.class, QuantityMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("ItemState", ItemStateMatcher.class, ItemStateMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("ItemType", ItemTypeMatcher.class, ItemTypeMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Armor", ArmorMatcher.class, ArmorMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Weapon", WeaponMatcher.class, WeaponMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Usable", UsableMatcher.class, UsableMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Group", GroupMatcher.class, GroupMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("PortalKey", PortalKeyMatcher.class, PortalKeyMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("DropOnDeath", DropOnDeathMatcher.class, DropOnDeathMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("AssetTag", AssetTagMatcher.class, AssetTagMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Tool", ToolMatcher.class, ToolMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Glider", GliderMatcher.class, GliderMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Category", CategoryMatcher.class, CategoryMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Interaction", InteractionMatcher.class, InteractionMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Resource", ResourceMatcher.class, ResourceMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Inventory", InventoryMatcher.class, InventoryMatcher.CODEC);
        ItemCondition.ItemMatcher.CODEC.register("Slot", SlotMatcher.class, SlotMatcher.CODEC);

        SlotMatcher.Slot.CODEC.register("IndexedSlot", IndexedSlotMatcher.class, IndexedSlotMatcher.CODEC);
        SlotMatcher.Slot.CODEC.register("ActiveHotbar", ActiveHotbarMatcher.class, ActiveUtilityMatcher.CODEC);
        SlotMatcher.Slot.CODEC.register("ActiveUtility", ActiveUtilityMatcher.class, ActiveUtilityMatcher.CODEC);
        SlotMatcher.Slot.CODEC.register("InteractionHeldItem", InteractionHeldItemMatcher.class, InteractionHeldItemMatcher.CODEC);
    }

    @Override
    protected void shutdown() {
        SlotMatcher.Slot.CODEC.remove(IndexedSlotMatcher.class);
        SlotMatcher.Slot.CODEC.remove(ActiveHotbarMatcher.class);
        SlotMatcher.Slot.CODEC.remove(ActiveUtilityMatcher.class);
        SlotMatcher.Slot.CODEC.remove(InteractionHeldItemMatcher.class);

        ItemCondition.ItemMatcher.CODEC.remove(DurabilityMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(EmptySlotMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(QuantityMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(ItemStateMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(ItemTypeMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(ArmorMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(WeaponMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(UsableMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(GroupMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(PortalKeyMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(DropOnDeathMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(AssetTagMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(ToolMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(GliderMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(CategoryMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(InteractionMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(ResourceMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(InventoryMatcher.class);
        ItemCondition.ItemMatcher.CODEC.remove(SlotMatcher.class);

        Interaction.CODEC.remove(ItemCondition.class);
        Interaction.CODEC.remove(TargetFirstItem.class);
    }
}