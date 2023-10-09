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

package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.util.formatting.LegacyTextHelper;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.util.adventure.text.Component;

import javax.annotation.Nullable;

public interface ItemRegistry {

    /**
     * Gets the name for the given item.
     *
     * @param itemType the item
     * @return The name
     * @deprecated use {@link ItemRegistry#getDisplayName(ItemType)} instead
     */
    @Deprecated
    default com.sk89q.worldedit.util.formatting.text.Component getRichName(ItemType itemType) {
        return LegacyTextHelper.adapt(getDisplayName(itemType));
    }

    /**
     * Gets the name for the given item.
     *
     * @param itemType the item
     * @return The name
     */
    Component getDisplayName(ItemType itemType);

    /**
     * Gets the name for the given item stack.
     *
     * @param itemStack the item stack
     * @return The name
     * @deprecated use {@link ItemRegistry#getDisplayName(BaseItemStack)} instead
     */
    @Deprecated
    default com.sk89q.worldedit.util.formatting.text.Component getRichName(BaseItemStack itemStack) {
        return LegacyTextHelper.adapt(getDisplayName(itemStack.getType()));
    }

    /**
     * Gets the name for the given item stack.
     *
     * @param itemStack the item stack
     * @return The name
     */
    default Component getDisplayName(BaseItemStack itemStack) {
        return getDisplayName(itemStack.getType());
    }

    /**
     * Gets the name for the given item.
     *
     * @param itemType the item
     * @return The name, or null if it's unknown
     * @deprecated Names are now translatable, use {@link #getDisplayName(ItemType)}.
     */
    @Deprecated
    @Nullable
    default String getName(ItemType itemType) {
        return getDisplayName(itemType).toString();
    }

    /**
     * Get the material for the given item.
     *
     * @param itemType the item
     * @return the material, or null if the material information is not known
     */
    @Nullable
    ItemMaterial getMaterial(ItemType itemType);
}
