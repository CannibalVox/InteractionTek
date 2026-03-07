package com.voxtech;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.asseteditor.AssetEditorAssetType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.voxtech.composite.CompositeInteraction;
import com.voxtech.interactions.*;
import com.voxtech.item.matchers.*;
import com.voxtech.item.matchers.slot.*;
import com.voxtech.item.modifications.*;
import com.voxtech.protocol.ItemMatcher;
import com.voxtech.protocol.ItemModification;
import com.voxtech.protocol.Slot;
import com.voxtech.protocol.TransactionStep;
import com.voxtech.transactions.steps.*;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

public class InteractionTekPlugin extends JavaPlugin {

    public InteractionTekPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        AssetRegistry.register(HytaleAssetStore.builder(CompositeInteraction.class, new DefaultAssetMap<>())
                .setPath("Item/CompositeInteractions")
                .setCodec(CompositeInteraction.CODEC)
                .setKeyFunction(CompositeInteraction::getId)
                .build());
        getEventRegistry().register(LoadedAssetsEvent.class, Item.class, InteractionTekPlugin::onLoadItem);

        Interaction.CODEC.register("TekItemCondition", ItemConditionInteraction.class, ItemConditionInteraction.CODEC);
        Interaction.CODEC.register("TekTargetFirstItem", TargetFirstItemInteraction.class, TargetFirstItemInteraction.CODEC);
        Interaction.CODEC.register("TekModifyItem", ModifyItemInteraction.class, ModifyItemInteraction.CODEC);
        Interaction.CODEC.register("TekRandomBranch", RandomBranchInteraction.class, RandomBranchInteraction.CODEC);
        Interaction.CODEC.register("TekInterruptSelf", InterruptSelfInteraction.class, InterruptSelfInteraction.CODEC);
        Interaction.CODEC.register("TekRunCommand", RunCommandInteraction.class, RunCommandInteraction.CODEC);
        Interaction.CODEC.register("TekRunProxiedCommand", RunProxiedCommandInteraction.class, RunProxiedCommandInteraction.CODEC);
        Interaction.CODEC.register("TekTransaction", TransactionInteraction.class, TransactionInteraction.CODEC);
        Interaction.CODEC.register("TekCustom", CustomInteraction.class, CustomInteraction.CODEC);

        TransactionStep.CODEC.register("ConsumeItems", ConsumeItemsStep.class, ConsumeItemsStep.CODEC);
        TransactionStep.CODEC.register("ProvideItems", ProvideItemsStep.class, ProvideItemsStep.CODEC);
        TransactionStep.CODEC.register("ModifyItem", ModifyItemStep.class, ModifyItemStep.CODEC);
        TransactionStep.CODEC.register("ChangeStats", ChangeStatsStep.class, ChangeStatsStep.CODEC);
        TransactionStep.CODEC.register("TriggerCooldown", TriggerCooldownStep.class, TriggerCooldownStep.CODEC);
        //TransactionStep.CODEC.register("AlwaysFail", AlwaysFailStep.class, AlwaysFailStep.CODEC);

        ItemMatcher.CODEC.register("Durability", DurabilityMatcher.class, DurabilityMatcher.CODEC);
        ItemMatcher.CODEC.register("EmptySlot", EmptySlotMatcher.class, EmptySlotMatcher.CODEC);
        ItemMatcher.CODEC.register("Quantity", QuantityMatcher.class, QuantityMatcher.CODEC);
        ItemMatcher.CODEC.register("ItemState", ItemStateMatcher.class, ItemStateMatcher.CODEC);
        ItemMatcher.CODEC.register("ItemType", ItemTypeMatcher.class, ItemTypeMatcher.CODEC);
        ItemMatcher.CODEC.register("Armor", ArmorMatcher.class, ArmorMatcher.CODEC);
        ItemMatcher.CODEC.register("Weapon", WeaponMatcher.class, WeaponMatcher.CODEC);
        ItemMatcher.CODEC.register("Group", GroupMatcher.class, GroupMatcher.CODEC);
        ItemMatcher.CODEC.register("PortalKey", PortalKeyMatcher.class, PortalKeyMatcher.CODEC);
        ItemMatcher.CODEC.register("DropOnDeath", DropOnDeathMatcher.class, DropOnDeathMatcher.CODEC);
        ItemMatcher.CODEC.register("AssetTag", AssetTagMatcher.class, AssetTagMatcher.CODEC);
        ItemMatcher.CODEC.register("Tool", ToolMatcher.class, ToolMatcher.CODEC);
        ItemMatcher.CODEC.register("Glider", GliderMatcher.class, GliderMatcher.CODEC);
        ItemMatcher.CODEC.register("Category", CategoryMatcher.class, CategoryMatcher.CODEC);
        ItemMatcher.CODEC.register("Resource", ResourceMatcher.class, ResourceMatcher.CODEC);
        ItemMatcher.CODEC.register("Inventory", InventoryMatcher.class, InventoryMatcher.CODEC);
        ItemMatcher.CODEC.register("Slot", SlotMatcher.class, SlotMatcher.CODEC);

        Slot.CODEC.register("IndexedSlot", IndexedSlotMatcher.class, IndexedSlotMatcher.CODEC);
        Slot.CODEC.register("ActiveHotbar", ActiveHotbarMatcher.class, ActiveHotbarMatcher.CODEC);
        Slot.CODEC.register("ActiveUtility", ActiveUtilityMatcher.class, ActiveUtilityMatcher.CODEC);
        Slot.CODEC.register("InteractionHeldItem", InteractionHeldItemMatcher.class, InteractionHeldItemMatcher.CODEC);
        Slot.CODEC.register("TargetArmorSlot", TargetArmorSlotMatcher.class, TargetArmorSlotMatcher.CODEC);
        Slot.CODEC.register("AnyOtherSlot", AnyOtherSlotMatcher.class, AnyOtherSlotMatcher.CODEC);
        Slot.CODEC.register("TargetSlot", TargetSlotMatcher.class, TargetSlotMatcher.CODEC);

        ItemModification.CODEC.register("AdjustQuantity", AdjustQuantityModification.class, AdjustQuantityModification.CODEC);
        ItemModification.CODEC.register("Singulate", SingulateModification.class, SingulateModification.CODEC);
        ItemModification.CODEC.register("AdjustDurability", AdjustDurabilityModification.class, AdjustDurabilityModification.CODEC);
        ItemModification.CODEC.register("Conditional", ConditionalModification.class, ConditionalModification.CODEC);
        ItemModification.CODEC.register("ChangeItem", ChangeItemModification.class, ChangeItemModification.CODEC);
        ItemModification.CODEC.register("RelocateItem", RelocateItemModification.class, RelocateItemModification.CODEC);
        ItemModification.CODEC.register("Group", GroupModification.class, GroupModification.CODEC);
        //ItemModification.CODEC.register("AlwaysFail", AlwaysFailModification.class, AlwaysFailModification.CODEC);
    }

    @Override
    protected void shutdown() {
        ItemModification.CODEC.remove(AdjustQuantityModification.class);
        ItemModification.CODEC.remove(SingulateModification.class);
        ItemModification.CODEC.remove(AdjustDurabilityModification.class);
        ItemModification.CODEC.remove(ConditionalModification.class);
        ItemModification.CODEC.remove(ChangeItemModification.class);
        ItemModification.CODEC.remove(RelocateItemModification.class);
        ItemModification.CODEC.remove(GroupModification.class);
        //ItemModification.CODEC.remove(AlwaysFailModification.class);

        Slot.CODEC.remove(IndexedSlotMatcher.class);
        Slot.CODEC.remove(ActiveHotbarMatcher.class);
        Slot.CODEC.remove(ActiveUtilityMatcher.class);
        Slot.CODEC.remove(InteractionHeldItemMatcher.class);
        Slot.CODEC.remove(TargetArmorSlotMatcher.class);
        Slot.CODEC.remove(AnyOtherSlotMatcher.class);
        Slot.CODEC.remove(TargetSlotMatcher.class);

        ItemMatcher.CODEC.remove(DurabilityMatcher.class);
        ItemMatcher.CODEC.remove(EmptySlotMatcher.class);
        ItemMatcher.CODEC.remove(QuantityMatcher.class);
        ItemMatcher.CODEC.remove(ItemStateMatcher.class);
        ItemMatcher.CODEC.remove(ItemTypeMatcher.class);
        ItemMatcher.CODEC.remove(ArmorMatcher.class);
        ItemMatcher.CODEC.remove(WeaponMatcher.class);
        ItemMatcher.CODEC.remove(GroupMatcher.class);
        ItemMatcher.CODEC.remove(PortalKeyMatcher.class);
        ItemMatcher.CODEC.remove(DropOnDeathMatcher.class);
        ItemMatcher.CODEC.remove(AssetTagMatcher.class);
        ItemMatcher.CODEC.remove(ToolMatcher.class);
        ItemMatcher.CODEC.remove(GliderMatcher.class);
        ItemMatcher.CODEC.remove(CategoryMatcher.class);
        ItemMatcher.CODEC.remove(ResourceMatcher.class);
        ItemMatcher.CODEC.remove(InventoryMatcher.class);
        ItemMatcher.CODEC.remove(SlotMatcher.class);

        TransactionStep.CODEC.remove(ConsumeItemsStep.class);
        TransactionStep.CODEC.remove(ConsumeItemsStep.class);
        TransactionStep.CODEC.remove(ModifyItemStep.class);
        TransactionStep.CODEC.remove(ChangeStatsStep.class);
        TransactionStep.CODEC.remove(TriggerCooldownStep.class);
        //TransactionStep.CODEC.remove(AlwaysFailStep.class);

        Interaction.CODEC.remove(ItemConditionInteraction.class);
        Interaction.CODEC.remove(TargetFirstItemInteraction.class);
        Interaction.CODEC.remove(ModifyItemInteraction.class);
        Interaction.CODEC.remove(RandomBranchInteraction.class);
        Interaction.CODEC.remove(InterruptSelfInteraction.class);
        Interaction.CODEC.remove(RunCommandInteraction.class);
        Interaction.CODEC.remove(RunProxiedCommandInteraction.class);
        Interaction.CODEC.remove(TransactionInteraction.class);
    }

    private static void onLoadItem(@Nonnull LoadedAssetsEvent<String, Item, ?> event) {
        try {
            AssetEditorPlugin plugin = AssetEditorPlugin.get();
            Field initLock = AssetEditorPlugin.class.getDeclaredField("initLock");
            initLock.setAccessible(true);

            Field setupSchemasPacket = AssetEditorPlugin.class.getDeclaredField("setupSchemasPacket");
            setupSchemasPacket.setAccessible(true);

            StampedLock lock = (StampedLock)initLock.get(plugin);
            long stamp = lock.readLock();

            try {
                if (setupSchemasPacket.get(plugin) != null) {
                    Field scheduledReinit = AssetEditorPlugin.class.getDeclaredField("scheduledReinitFuture");
                    scheduledReinit.setAccessible(true);

                    Method reinitialize = AssetEditorPlugin.class.getDeclaredMethod("tryReinitializeAssetEditor");
                    reinitialize.setAccessible(true);

                    ScheduledFuture<?> future = (ScheduledFuture<?>)scheduledReinit.get(plugin);
                    if (future != null) {
                        future.cancel(false);
                    }

                    ScheduledFuture<?> newFuture = HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> reinitialize.invoke(plugin), 1L, TimeUnit.SECONDS);
                    scheduledReinit.set(plugin, newFuture);

                    HytaleLogger.getLogger().atInfo().log("Loading item " + event.getLoadedAssets());
                }
            } finally {
                lock.unlockRead(stamp);
            }
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
