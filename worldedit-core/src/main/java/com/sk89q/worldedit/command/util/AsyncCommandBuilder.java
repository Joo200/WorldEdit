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

package com.sk89q.worldedit.command.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.command.exception.ExceptionConverter;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.formatting.component.ErrorFormat;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.format.NamedTextColor;
import com.sk89q.worldedit.util.task.FutureForwardingTask;
import com.sk89q.worldedit.util.task.Supervisor;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.exception.CommandException;
import org.enginehub.piston.exception.CommandExecutionException;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class AsyncCommandBuilder<T> {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final Callable<T> callable;
    private final Actor sender;

    @Nullable
    private Supervisor supervisor;
    @Nullable
    private String description;
    @Nullable
    private Component delayMessage;
    @Nullable
    private Component workingMessage;

    @Nullable
    private Component successMessage;
    @Nullable
    private Consumer<T> consumer;

    @Nullable
    private Component failureMessage;
    @Nullable
    private ExceptionConverter exceptionConverter;

    private AsyncCommandBuilder(Callable<T> callable, Actor sender) {
        checkNotNull(callable);
        checkNotNull(sender);
        this.callable = callable;
        this.sender = sender;
    }

    public static <T> AsyncCommandBuilder<T> wrap(Callable<T> callable, Actor sender) {
        return new AsyncCommandBuilder<>(callable, sender);
    }

    public AsyncCommandBuilder<T> registerWithSupervisor(Supervisor supervisor, String description) {
        this.supervisor = checkNotNull(supervisor);
        this.description = checkNotNull(description);
        return this;
    }

    @Deprecated
    public AsyncCommandBuilder<T> sendMessageAfterDelay(String message) {
        return sendMessageAfterDelay(Component.text(checkNotNull(message)));
    }

    @Deprecated
    public AsyncCommandBuilder<T> sendMessageAfterDelay(Component message) {
        return setDelayMessage(message);
    }

    public AsyncCommandBuilder<T> setDelayMessage(Component message) {
        this.delayMessage = checkNotNull(message);
        return this;
    }

    public AsyncCommandBuilder<T> setWorkingMessage(Component message) {
        checkNotNull(this.delayMessage, "Must have a delay message if using a working message");
        this.workingMessage = checkNotNull(message);
        return this;
    }

    public AsyncCommandBuilder<T> onSuccess(@Nullable Component message, @Nullable Consumer<T> consumer) {
        checkArgument(message != null || consumer != null, "Can't have null message AND consumer");
        this.successMessage = message;
        this.consumer = consumer;
        return this;
    }

    public AsyncCommandBuilder<T> onSuccess(@Nullable String message, @Nullable Consumer<T> consumer) {
        checkArgument(message != null || consumer != null, "Can't have null message AND consumer");
        this.successMessage = message == null ? null : Component.text(message, NamedTextColor.LIGHT_PURPLE);
        this.consumer = consumer;
        return this;
    }

    public AsyncCommandBuilder<T> onFailure(@Nullable Component message, @Nullable ExceptionConverter exceptionConverter) {
        checkArgument(message != null || exceptionConverter != null, "Can't have null message AND exceptionConverter");
        this.failureMessage = message;
        this.exceptionConverter = exceptionConverter;
        return this;
    }

    public AsyncCommandBuilder<T> onFailure(@Nullable String message, @Nullable ExceptionConverter exceptionConverter) {
        checkArgument(message != null || exceptionConverter != null, "Can't have null message AND exceptionConverter");
        this.failureMessage = message == null ? null : ErrorFormat.wrap(message);
        this.exceptionConverter = exceptionConverter;
        return this;
    }

    public ListenableFuture<T> buildAndExec(ListeningExecutorService executor) {
        final ListenableFuture<T> future = checkNotNull(executor).submit(this::runTask);
        if (delayMessage != null) {
            FutureProgressListener.addProgressListener(
                future,
                sender,
                delayMessage,
                workingMessage
            );
        }
        if (supervisor != null && description != null) {
            supervisor.monitor(FutureForwardingTask.create(future, description, sender));
        }
        return future;
    }

    @SuppressWarnings("deprecation")
    private T runTask() {
        T result = null;
        try {
            result = callable.call();
            if (consumer != null) {
                consumer.accept(result);
            }
            if (successMessage != null) {
                sender.print(successMessage);
            }
        } catch (Throwable orig) {
            Component failure = failureMessage != null ? failureMessage : Component.text("An error occurred");
            try {
                if (exceptionConverter != null) {
                    try {
                        if (orig instanceof com.sk89q.minecraft.util.commands.CommandException) {
                            throw new CommandExecutionException(orig, ImmutableList.of());
                        }
                        exceptionConverter.convert(orig);
                        throw orig;
                    } catch (CommandException converted) {
                        Component message;

                        // TODO remove this once WG migrates to piston and can use piston exceptions everywhere
                        message = tryExtractOldCommandException(converted);

                        if (message == null) {
                            if (Strings.isNullOrEmpty(converted.getMessage())) {
                                message = Component.text("Unknown error.");
                            } else {
                                message = converted.getRichMessage();
                            }
                        }
                        sender.printError(failure.append(Component.text(": ")).append(message));
                    }
                } else {
                    throw orig;
                }
            } catch (Throwable unknown) {
                sender.printError(failure.append(Component.text(": Unknown error. Please see console.")));
                LOGGER.error("Uncaught exception occurred in task: " + description, orig);
            }
        }
        return result;
    }

    // this is needed right now since worldguard is still on the 2011 command framework which throws and converts
    // com.sk89q.minecraft.util.commands.CommandException. the ExceptionConverter currently expects converted
    // exceptions to be org.enginehub.piston.CommandException, throw it wraps the resulting InvocationTargetException in
    // a CommandExecutionException. here, we unwrap those layers to retrieve the original WG error message
    @SuppressWarnings("deprecation")
    private Component tryExtractOldCommandException(CommandException converted) {
        Component message = null;
        if (converted instanceof CommandExecutionException) {
            Throwable parentCause = converted;
            while ((parentCause = parentCause.getCause()) != null) {
                if (parentCause instanceof com.sk89q.minecraft.util.commands.CommandException) {
                    final String msg = parentCause.getMessage();
                    if (!Strings.isNullOrEmpty(msg)) {
                        message = Component.text(msg);
                    }
                    break;
                }
            }
        }
        return message;
    }
}
