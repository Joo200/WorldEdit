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

package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.util.Identifiable;
import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.format.NamedTextColor;
import com.sk89q.worldedit.util.auth.Subject;
import com.sk89q.worldedit.util.formatting.LegacyTextHelper;

import java.io.File;
import java.util.Locale;

/**
 * An object that can perform actions in WorldEdit.
 */
public interface Actor extends Identifiable, SessionOwner, Subject {

    /**
     * Get the name of the actor.
     *
     * @return String
     */
    String getName();

    /**
     * Gets the display name of the actor. This can be a nickname, and is not guaranteed to be unique.
     *
     * @return The display name
     */
    default String getDisplayName() {
        return getName();
    }

    /**
     * Print a message.
     *
     * @param msg The message text
     * @deprecated Use component-based functions (print)
     */
    @Deprecated
    void printRaw(String msg);

    /**
     * Print a WorldEdit message.
     *
     * @param msg The message text
     * @deprecated Use component-based functions (printDebug)
     */
    @Deprecated
    default void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            print(Component.text(part, NamedTextColor.GRAY));
        }
    }

    /**
     * Print a WorldEdit message.
     *
     * @param msg The message text
     * @deprecated Use component-based functions (printInfo)
     */
    @Deprecated
    default void print(String msg) {
        for (String part : msg.split("\n")) {
            print(Component.text(part, NamedTextColor.LIGHT_PURPLE));
        }
    }

    /**
     * Print a WorldEdit error.
     *
     * @param msg The error message text
     * @deprecated Use component-based functions (printError)
     */
    @Deprecated
    default void printError(String msg) {
        for (String part : msg.split("\n")) {
            print(Component.text(part, NamedTextColor.RED));
        }
    }

    /**
     * Print a WorldEdit error.
     *
     * @param component The component to print
     */
    default void printError(Component component) {
        print(component.color(NamedTextColor.RED));
    }

    /**
     * Print a WorldEdit message.
     *
     * @param component The component to print
     */
    default void printInfo(Component component) {
        print(component.color(NamedTextColor.LIGHT_PURPLE));
    }

    /**
     * Print a WorldEdit message.
     *
     * @param component The component to print
     */
    default void printDebug(Component component) {
        print(component.color(NamedTextColor.GRAY));
    }

    /**
     * Print a {@link Component}.
     *
     * @param component The component to print
     */
    void print(Component component);

    /**
     * Print a WorldEdit error.
     *
     * @param component The component to print
     * @deprecated Use {@link #printError(Component)} instead.
     */
    @Deprecated
    default void printError(com.sk89q.worldedit.util.formatting.text.Component component) {
        print(LegacyTextHelper.adapt(component).color(NamedTextColor.RED));
    }

    /**
     * Print a WorldEdit message.
     *
     * @param component The component to print
     * @deprecated Use {@link #printInfo(Component)} instead.
     */
    @Deprecated
    default void printInfo(com.sk89q.worldedit.util.formatting.text.Component component) {
        print(LegacyTextHelper.adapt(component).color(NamedTextColor.LIGHT_PURPLE));
    }

    /**
     * Print a WorldEdit message.
     *
     * @param component The component to print
     * @deprecated Use {@link #printDebug(Component)} instead.
     */
    @Deprecated
    default void printDebug(com.sk89q.worldedit.util.formatting.text.Component component) {
        print(LegacyTextHelper.adapt(component).color(NamedTextColor.GRAY));
    }

    /**
     * Print a {@link Component}.
     *
     * @param component The component to print
     * @deprecated Use {@link #print(Component)} instead.
     */
    @Deprecated
    default void print(com.sk89q.worldedit.util.formatting.text.Component component) {
        print(LegacyTextHelper.adapt(component));
    }

    /**
     * Returns true if the actor can destroy bedrock.
     *
     * @return true if bedrock can be broken by the actor
     */
    boolean canDestroyBedrock();

    /**
     * Return whether this actor is a player.
     *
     * @return true if a player
     */
    boolean isPlayer();

    /**
     * Open a file open dialog.
     *
     * @param extensions null to allow all
     * @return the selected file or null if something went wrong
     */
    File openFileOpenDialog(String[] extensions);

    /**
     * Open a file save dialog.
     *
     * @param extensions null to allow all
     * @return the selected file or null if something went wrong
     */
    File openFileSaveDialog(String[] extensions);

    /**
     * Send a CUI event.
     *
     * @param event the event
     */
    void dispatchCUIEvent(CUIEvent event);

    /**
     * Get the locale of this actor.
     *
     * @return The locale
     */
    Locale getLocale();

    /**
     * Sends any relevant notices to the user when they first use WorldEdit in a session.
     */
    default void sendAnnouncements() {
    }
}
