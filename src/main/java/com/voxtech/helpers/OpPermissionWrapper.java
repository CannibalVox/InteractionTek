package com.voxtech.helpers;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OpPermissionWrapper implements CommandSender {
    private final CommandSender wrapped;

    public OpPermissionWrapper(CommandSender wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String getDisplayName() {
        return this.wrapped.getDisplayName();
    }

    @Override
    public UUID getUuid() {
        return this.wrapped.getUuid();
    }

    @Override
    public boolean hasPermission(@Nonnull String s) {
        return true;
    }

    @Override
    public boolean hasPermission(@Nonnull String s, boolean b) {
        return true;
    }

    @Override
    public void sendMessage(@Nonnull Message message) {
        this.wrapped.sendMessage(message);
    }
}
