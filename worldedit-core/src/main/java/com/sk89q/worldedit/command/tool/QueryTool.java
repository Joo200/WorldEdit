/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.NamedTextColor;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import javax.annotation.Nullable;

/**
 * Looks up information about a block.
 */
public class QueryTool implements BlockTool {

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission("worldedit.tool.info");
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {

        World world = BlockTool.requireWorld(clicked);
        BlockVector3 blockPoint = clicked.toVector().toBlockPoint();
        BaseBlock block = world.getFullBlock(blockPoint);

        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("@" + clicked.toVector().toBlockPoint() + ": ", TextColor.BLUE));
        builder.append(block.getBlockType().getRichName().color(TextColor.YELLOW));

        String blockStateString = block.toString();
        Component blockStateComponent = Component.text(" (" + blockStateString + ") ", TextColor.GRAY)
            .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TranslatableComponent.of("worldedit.tool.info.blockstate.hover")))
            .clickEvent(ClickEvent.of(ClickEvent.Action.COPY_TO_CLIPBOARD, blockStateString));
        builder.append(blockStateComponent);

        final int internalId = BlockStateIdAccess.getBlockStateId(block.toImmutableState());
        if (BlockStateIdAccess.isValidInternalId(internalId)) {
            builder.append(Component.text(" (" + internalId + ") ", NamedTextColor.DARK_GRAY)
                .hoverEvent(HoverEvent.showText(Component.translatable("worldedit.tool.info.internalid.hover"))));
        }
        final int[] legacy = LegacyMapper.getInstance().getLegacyFromBlock(block.toImmutableState());
        if (legacy != null) {
            builder.append(Component.text(" (" + legacy[0] + ":" + legacy[1] + ") ", NamedTextColor.DARK_GRAY)
                .hoverEvent(HoverEvent.showText(Component.translatable("worldedit.tool.info.legacy.hover"))));
        }

        builder.append(Component.text(" (" + world.getBlockLightLevel(blockPoint) + "/"
            + world.getBlockLightLevel(blockPoint.add(0, 1, 0)) + ")", NamedTextColor.WHITE)
            .hoverEvent(HoverEvent.showText(Component.translatable("worldedit.tool.info.light.hover"))));

        player.print(builder.build());

        return true;
    }

}
