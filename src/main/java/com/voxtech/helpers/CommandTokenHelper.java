package com.voxtech.helpers;

import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import com.voxtech.commands.*;
import joptsimple.internal.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandTokenHelper {
    private static Map<String, Token> VAR_TOKENS = Map.ofEntries(
            Map.entry("player", new EntityComponentToken<Player>(Player.getComponentType(),
                    null,
                    Player::getDisplayName)),
            Map.entry("self", new EntityComponentToken<UUIDComponent>(UUIDComponent.getComponentType(),
                    InteractionTarget.USER,
                    u -> u.getUuid().toString())),
            Map.entry("selfX", new EntityComponentToken<TransformComponent>(TransformComponent.getComponentType(),
                    InteractionTarget.USER,
                    t -> Integer.toString((int) t.getTransform().getPosition().x))),
            Map.entry("selfY", new EntityComponentToken<TransformComponent>(TransformComponent.getComponentType(),
                    InteractionTarget.USER,
                    t -> Integer.toString((int) t.getTransform().getPosition().y))),
            Map.entry("selfZ", new EntityComponentToken<TransformComponent>(TransformComponent.getComponentType(),
                    InteractionTarget.USER,
                    t -> Integer.toString((int) t.getTransform().getPosition().z))),
            Map.entry("blockX", new TargetBlockPositionToken(p -> Integer.toString(p.x))),
            Map.entry("blockY", new TargetBlockPositionToken(p -> Integer.toString(p.y))),
            Map.entry("blockZ", new TargetBlockPositionToken(p -> Integer.toString(p.z))),
            Map.entry("targetX", new EntityComponentToken<TransformComponent>(TransformComponent.getComponentType(),
                    InteractionTarget.TARGET,
                    t -> Integer.toString((int) t.getTransform().getPosition().x))),
            Map.entry("targetY", new EntityComponentToken<TransformComponent>(TransformComponent.getComponentType(),
                    InteractionTarget.TARGET,
                    t -> Integer.toString((int) t.getTransform().getPosition().y))),
            Map.entry("targetZ", new EntityComponentToken<TransformComponent>(TransformComponent.getComponentType(),
                    InteractionTarget.TARGET,
                    t -> Integer.toString((int) t.getTransform().getPosition().z)))
    );

    public static List<Token> tokenizeCommand(String command) {
        List<Token> tokens = new ArrayList<>();
        if (Strings.isNullOrEmpty(command)) {
            return tokens;
        }

        int cursor = 0;

        while (cursor < command.length()) {
            int nextCursor = command.indexOf('@', cursor);
            if (nextCursor < 0) {
                tokens.add(new StringToken(command.substring(cursor)));
                break;
            }

            if (nextCursor > cursor) {
                tokens.add(new StringToken(command.substring(cursor, nextCursor)));
            }

            if (nextCursor == command.length() - 1) {
                tokens.add(new StringToken(command.substring(nextCursor)));
                break;
            }

            if (command.charAt(nextCursor + 1) == '@') {
                tokens.add(new StringToken("@"));
                cursor = nextCursor + 2;
                continue;
            }

            cursor = nextCursor + 1;
            nextCursor = command.indexOf(' ', cursor);
            String varName = nextCursor >= 0 ? command.substring(cursor, nextCursor) : command.substring(cursor);
            if (!VAR_TOKENS.containsKey(varName)) {
                tokens.add(new StringToken("@" + varName));
            } else {
                tokens.add(VAR_TOKENS.get(varName));
            }

            if (nextCursor < 0) {
                break;
            }

            cursor = nextCursor;
        }


        return tokens;
    }
}
