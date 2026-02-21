package com.voxtech;

import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.voxtech.interactions.ItemConditionInteraction;
import com.voxtech.interactions.ModifyItemInteraction;
import com.voxtech.interactions.TargetFirstItemInteraction;
import com.voxtech.item.matchers.*;
import com.voxtech.item.matchers.slot.*;
import com.voxtech.item.modifications.*;

import javax.annotation.Nonnull;

public class InteractionTekPlugin extends JavaPlugin {

    public InteractionTekPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Interaction.CODEC.register("TekItemCondition", ItemConditionInteraction.class, ItemConditionInteraction.CODEC);
        Interaction.CODEC.register("TekTargetFirstItem", TargetFirstItemInteraction.class, TargetFirstItemInteraction.CODEC);
        Interaction.CODEC.register("TekModifyItem", ModifyItemInteraction.class, ModifyItemInteraction.CODEC);

        ItemConditionInteraction.ItemMatcher.CODEC.register("Durability", DurabilityMatcher.class, DurabilityMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("EmptySlot", EmptySlotMatcher.class, EmptySlotMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Quantity", QuantityMatcher.class, QuantityMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("ItemState", ItemStateMatcher.class, ItemStateMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("ItemType", ItemTypeMatcher.class, ItemTypeMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Armor", ArmorMatcher.class, ArmorMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Weapon", WeaponMatcher.class, WeaponMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Usable", UsableMatcher.class, UsableMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Group", GroupMatcher.class, GroupMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("PortalKey", PortalKeyMatcher.class, PortalKeyMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("DropOnDeath", DropOnDeathMatcher.class, DropOnDeathMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("AssetTag", AssetTagMatcher.class, AssetTagMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Tool", ToolMatcher.class, ToolMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Glider", GliderMatcher.class, GliderMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Category", CategoryMatcher.class, CategoryMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Interaction", InteractionMatcher.class, InteractionMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Resource", ResourceMatcher.class, ResourceMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Inventory", InventoryMatcher.class, InventoryMatcher.CODEC);
        ItemConditionInteraction.ItemMatcher.CODEC.register("Slot", SlotMatcher.class, SlotMatcher.CODEC);

        SlotMatcher.Slot.CODEC.register("IndexedSlot", IndexedSlotMatcher.class, IndexedSlotMatcher.CODEC);
        SlotMatcher.Slot.CODEC.register("ActiveHotbar", ActiveHotbarMatcher.class, ActiveUtilityMatcher.CODEC);
        SlotMatcher.Slot.CODEC.register("ActiveUtility", ActiveUtilityMatcher.class, ActiveUtilityMatcher.CODEC);
        SlotMatcher.Slot.CODEC.register("InteractionHeldItem", InteractionHeldItemMatcher.class, InteractionHeldItemMatcher.CODEC);
        SlotMatcher.Slot.CODEC.register("TargetArmorSlot", TargetArmorSlotMatcher.class, TargetArmorSlotMatcher.CODEC);
        SlotMatcher.Slot.CODEC.register("AnyOtherSlot", AnyOtherSlotMatcher.class, AnyOtherSlotMatcher.CODEC);
        SlotMatcher.Slot.CODEC.register("TargetSlot", TargetSlotMatcher.class, TargetSlotMatcher.CODEC);

        ModifyItemInteraction.ItemModification.CODEC.register("AdjustQuantity", AdjustQuantityModification.class, AdjustQuantityModification.CODEC);
        ModifyItemInteraction.ItemModification.CODEC.register("Singulate", SingulateModification.class, SingulateModification.CODEC);
        ModifyItemInteraction.ItemModification.CODEC.register("AdjustDurability", AdjustDurabilityModification.class, AdjustDurabilityModification.CODEC);
        ModifyItemInteraction.ItemModification.CODEC.register("Conditional", ConditionalModification.class, ConditionalModification.CODEC);
        ModifyItemInteraction.ItemModification.CODEC.register("ChangeItem", ChangeItemModification.class, ChangeItemModification.CODEC);
        ModifyItemInteraction.ItemModification.CODEC.register("RelocateItem", RelocateItemModification.class, RelocateItemModification.CODEC);
    }

    @Override
    protected void shutdown() {
        ModifyItemInteraction.ItemModification.CODEC.remove(AdjustQuantityModification.class);
        ModifyItemInteraction.ItemModification.CODEC.remove(SingulateModification.class);
        ModifyItemInteraction.ItemModification.CODEC.remove(AdjustDurabilityModification.class);
        ModifyItemInteraction.ItemModification.CODEC.remove(ConditionalModification.class);
        ModifyItemInteraction.ItemModification.CODEC.remove(ChangeItemModification.class);
        ModifyItemInteraction.ItemModification.CODEC.remove(RelocateItemModification.class);

        SlotMatcher.Slot.CODEC.remove(IndexedSlotMatcher.class);
        SlotMatcher.Slot.CODEC.remove(ActiveHotbarMatcher.class);
        SlotMatcher.Slot.CODEC.remove(ActiveUtilityMatcher.class);
        SlotMatcher.Slot.CODEC.remove(InteractionHeldItemMatcher.class);
        SlotMatcher.Slot.CODEC.remove(TargetArmorSlotMatcher.class);
        SlotMatcher.Slot.CODEC.remove(AnyOtherSlotMatcher.class);
        SlotMatcher.Slot.CODEC.remove(TargetSlotMatcher.class);

        ItemConditionInteraction.ItemMatcher.CODEC.remove(DurabilityMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(EmptySlotMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(QuantityMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(ItemStateMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(ItemTypeMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(ArmorMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(WeaponMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(UsableMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(GroupMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(PortalKeyMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(DropOnDeathMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(AssetTagMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(ToolMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(GliderMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(CategoryMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(InteractionMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(ResourceMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(InventoryMatcher.class);
        ItemConditionInteraction.ItemMatcher.CODEC.remove(SlotMatcher.class);

        Interaction.CODEC.remove(ItemConditionInteraction.class);
        Interaction.CODEC.remove(TargetFirstItemInteraction.class);
        Interaction.CODEC.remove(ModifyItemInteraction.class);
    }
}