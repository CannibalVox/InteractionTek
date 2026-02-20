package com.voxtech.item.matchers.modifications;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.voxtech.interactions.ModifyItemInteraction;
import com.voxtech.validators.ValueOr;

import javax.annotation.Nonnull;

public class AdjustDurabilityModification extends ModifyItemInteraction.ItemModification {

    @Nonnull
    public static final BuilderCodec<AdjustDurabilityModification> CODEC = BuilderCodec
        .builder(AdjustDurabilityModification.class, AdjustDurabilityModification::new, BASE_CODEC)
        .documentation("This modification will increase or reduce an item's current durability. It will fail if the item does not have durability, or when attempting to reduce the durability of an item that is already broken.")
        .append(new KeyedCodec<>("Delta", Codec.DOUBLE),
            (object, delta) -> object.delta = delta,
            object -> object.delta)
            .documentation("The amount to increase or reduce the item's current durability by")
            .add()
        .append(new KeyedCodec<>("IgnoreBroken", Codec.BOOLEAN),
            (object, ignoreBroken) -> object.ignoreBroken = ignoreBroken,
            object -> object.ignoreBroken)
            .documentation("If true, attempting to reduce the durability of an object that is already broken will not fail, it will simply have no effect.")
            .add()
        .append(new KeyedCodec<>("IgnoreNoDurability", Codec.BOOLEAN),
            (object, ignoreNoDurability) -> object.ignoreNoDurability = ignoreNoDurability,
            object -> object.ignoreNoDurability)
            .documentation("If true, attempting to operate on an item that does not have durability (i.e. max durability of 0) will not fail, it will simply have no effect.")
            .add()
        .append(new KeyedCodec<>("BrokenItem", Codec.STRING),
            (object, brokenItem) -> object.brokenItem = brokenItem,
    object -> object.brokenItem)
            .documentation("If provided, the item will have its item id changed to this in the event that this modification breaks the item. This will have no effect if the item is already broken.  Setting this value to 'Empty' will destroy the item when it breaks.")
            .addValidator((new ValueOr<>("Empty", Item.VALIDATOR_CACHE.getValidator())).late())
            .add()
        .append(new KeyedCodec<>("UnbrokenItem", Codec.STRING),
            (object, unbrokenItem) -> object.unbrokenItem = unbrokenItem,
            object -> object.unbrokenItem)
            .documentation("If provided, the item will have its item id changed to this in the event that this modification caused the item to go from broken to not broken")
            .addValidator(Item.VALIDATOR_CACHE.getValidator().late())
            .add()
        .append(new KeyedCodec<>("NotifyOnBreak", Codec.BOOLEAN),
            (object, notifyOnBreak) -> object.notifyOnBreak = notifyOnBreak,
            object -> object.notifyOnBreak)
            .documentation("If true, and the User entity is a player, the player will receive a chat message indicating that their item broke even when it changes types through BrokenItem. Normally, a message will only display if the item broke but did not change types.")
            .add()
        .append(new KeyedCodec<>("NotifyOnBreakMessage", Codec.STRING),
            (object, notifyOnBreakMessage) -> object.notifyOnBreakMessage = notifyOnBreakMessage,
            object -> object.notifyOnBreakMessage)
            .documentation("Custom translation key for the break notification message. Supports {itemName} parameter. Defaults to 'server.general.repair.itemBroken' if not specified")
            .add()
        .build();


    private double delta;
    private boolean ignoreBroken;
    private boolean ignoreNoDurability;
    private String unbrokenItem;
    private String brokenItem;
    private boolean notifyOnBreak;
    private String notifyOnBreakMessage;

    @Override
    public boolean modify0(World world, Ref<EntityStore> ref, CommandBuffer<EntityStore> buffer, InteractionContext context, Inventory inventory, ItemContainer targetContainer, short targetSlot, ItemStack targetItem) {
        Player playerComponent = buffer.getComponent(ref, Player.getComponentType());

        if (targetItem.getMaxDurability() < 0.001) {
            return ignoreNoDurability;
        }

        boolean wasBroken = targetItem.isBroken();
        if (delta < 0 && wasBroken) {
            return ignoreBroken;
        }

        ItemStack newItem = targetItem.withIncreasedDurability(delta);

        boolean isBroken = targetItem.isBroken();

        // Convert the item if it broke or if it unbroke
        if (isBroken && !wasBroken && brokenItem != null) {
            if ("Empty".equals(brokenItem)) {
                newItem = null;
            } else {
                newItem = new ItemStack(brokenItem, targetItem.getQuantity(), targetItem.getMetadata());
            }
        } else if (!isBroken && wasBroken && unbrokenItem != null) {
            newItem = new ItemStack(unbrokenItem, targetItem.getQuantity(), targetItem.getMetadata());
        }

        // if it broke, then notify if there was no transformation or if we explicitly say to
        // we'll always notify if there's no transformation just because the player has to have SOME indication that something
        // changed
        if (playerComponent != null && isBroken && !wasBroken && ((newItem != null && targetItem.getItemId().equals(newItem.getItemId())) || notifyOnBreak)) {
            Message itemNameMessage = Message.translation(targetItem.getItem().getTranslationKey());
            String messageKey = this.notifyOnBreakMessage != null ? this.notifyOnBreakMessage : "server.general.repair.itemBroken";
            playerComponent.sendMessage(Message.translation(messageKey).param("itemName", itemNameMessage).color("#ff5555"));
            PlayerRef playerRefComponent = buffer.getComponent(ref, PlayerRef.getComponentType());
            if (playerRefComponent != null) {
                int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Item_Break");
                if (soundEventIndex > Integer.MIN_VALUE) {
                    SoundUtil.playSoundEvent2dToPlayer(playerRefComponent, soundEventIndex, SoundCategory.SFX);
                }
            }
        }

        ItemStackSlotTransaction slotTransaction = targetContainer.setItemStackForSlot(targetSlot, newItem);
        return slotTransaction.succeeded();
    }
}
