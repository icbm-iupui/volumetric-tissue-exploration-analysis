/*
 * Copyright (C) 2020 Indiana University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package vtea.util;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/**
 * Helper class for running background tasks using SwingWorker pattern.
 * This ensures proper EDT threading and error handling.
 *
 * @author Claude Code
 */
public class BackgroundTaskHelper {

    /**
     * Execute a background task with no return value.
     *
     * @param task The task to execute in background
     * @param onSuccess Optional callback when task completes successfully (runs on EDT)
     * @param onError Optional callback when task fails (runs on EDT)
     */
    public static void execute(
            RunnableWithException task,
            Runnable onSuccess,
            Consumer<Exception> onError) {

        new SwingWorker<Void, Void>() {
            private Exception exception;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    task.run();
                } catch (Exception e) {
                    exception = e;
                    throw e;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    if (onError != null) {
                        onError.accept(exception != null ? exception : e);
                    } else {
                        // Default error handling
                        System.err.println("ERROR in background task: " + e.getMessage());
                        if (e.getCause() != null) {
                            e.getCause().printStackTrace();
                        }
                    }
                }
            }
        }.execute();
    }

    /**
     * Execute a background task with no callbacks.
     *
     * @param task The task to execute in background
     */
    public static void execute(RunnableWithException task) {
        execute(task, null, null);
    }

    /**
     * Execute a background task that returns a result.
     *
     * @param <T> The result type
     * @param task The task to execute in background
     * @param onSuccess Callback with result when task completes (runs on EDT)
     * @param onError Optional callback when task fails (runs on EDT)
     */
    public static <T> void executeWithResult(
            SupplierWithException<T> task,
            Consumer<T> onSuccess,
            Consumer<Exception> onError) {

        new SwingWorker<T, Void>() {
            private Exception exception;

            @Override
            protected T doInBackground() throws Exception {
                try {
                    return task.get();
                } catch (Exception e) {
                    exception = e;
                    throw e;
                }
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    if (onError != null) {
                        onError.accept(exception != null ? exception : e);
                    } else {
                        // Default error handling
                        System.err.println("ERROR in background task: " + e.getMessage());
                        if (e.getCause() != null) {
                            e.getCause().printStackTrace();
                        }
                    }
                }
            }
        }.execute();
    }

    /**
     * Functional interface for tasks that can throw exceptions.
     */
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    /**
     * Functional interface for suppliers that can throw exceptions.
     */
    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
