package com.voxtech.helpers;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

public class ExtraPermissionWrapper implements CommandSender {
    private final CommandSender wrapped;
    private final Set<String> addedPermissions;

    public ExtraPermissionWrapper(CommandSender wrapped, Set<String> addedPermissions) {
        this.wrapped = wrapped;
        this.addedPermissions = addedPermissions;
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
        if (addedPermissions.contains(s)) {
            return true;
        }
        return this.wrapped.hasPermission(s);
    }

    @Override
    public boolean hasPermission(@Nonnull String s, boolean b) {
        if (addedPermissions.contains(s)) {
            return true;
        }
        return this.wrapped.hasPermission(s, b);
    }

    @Override
    public void sendMessage(@Nonnull Message message) {
        this.wrapped.sendMessage(message);
    }
}
