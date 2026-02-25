package com.voxtech.transactions.postcommit;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class BreakItemPostCommit implements PostCommitEntry {
    public BreakItemPostCommit(Item brokenItem, String message) {
        this.brokenItem = brokenItem;
        this.message = message;
    }

    private final Item brokenItem;
    private final String message;

    @Override
    public void postCommit(Ref<EntityStore> ref, CommandBuffer<EntityStore> commandBuffer) {
        Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
        if (playerComponent == null) {
            return;
        }

        Message itemNameMessage = Message.translation(brokenItem.getTranslationKey());
        playerComponent.sendMessage(Message.translation(message).param("itemName", itemNameMessage).color("#ff5555"));
        PlayerRef playerRefComponent = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if (playerRefComponent != null) {
            int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Item_Break");
            if (soundEventIndex > Integer.MIN_VALUE) {
                SoundUtil.playSoundEvent2dToPlayer(playerRefComponent, soundEventIndex, SoundCategory.SFX);
            }
        }
    }
}
