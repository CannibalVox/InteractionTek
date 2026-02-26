package com.voxtech.commands;

import com.hypixel.hytale.server.core.entity.entities.Player;

@FunctionalInterface
public interface PropertyValue<T> {
    String property(T obj);
}
