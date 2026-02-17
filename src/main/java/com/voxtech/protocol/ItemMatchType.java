package com.voxtech.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;

public enum ItemMatchType {
    All(0),
    Any(1);

    public static final ItemMatchType[] VALUES = values();
    private final int value;

    private ItemMatchType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static ItemMatchType fromValue(int value) {
        if (value >= 0 && value < VALUES.length) {
            return VALUES[value];
        } else {
            throw ProtocolException.invalidEnumValue("ItemMatchType", value);
        }
    }
}
