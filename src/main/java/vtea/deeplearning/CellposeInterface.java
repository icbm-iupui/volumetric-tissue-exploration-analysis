/*
 * Copyright (C) 2020 Indiana University and 2023 University of Nebraska
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
package vtea.deeplearning;

import ij.ImageStack;
import ij.process.ImageProcessor;
import py4j.GatewayServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Interface for communicating with Cellpose Python server via Py4J.
 * Manages the lifecycle of the Python server and provides methods for segmentation.
 *
 * @author sethwinfree
 */
public class CellposeInterface implements AutoCloseable {

    private static final int DEFAULT_PORT = 25333;
    private static final long DEFAULT_STARTUP_TIMEOUT = 60000; // 60 seconds
    private static final long DEFAULT_REQUEST_TIMEOUT = 300000; // 5 minutes

    private Process pythonProcess;
    private GatewayServer gatewayServer;
    private Object pythonEntryPoint;
    private boolean serverRunning = false;

    private final int port;
    private final String pythonExecutable;
    private final String serverScriptPath;
    private final long startupTimeout;
    private final long requestTimeout;

    // Restart strategy
    private int restartAttempts = 0;
    private static final int MAX_RESTART_ATTEMPTS = 3;
    private static final long RESTART_BACKOFF_MS = 2000;

    /**
     * Constructor with default settings
     */
    public CellposeInterface() {
        this(DEFAULT_PORT, "python3", findServerScript(), DEFAULT_STARTUP_TIMEOUT, DEFAULT_REQUEST_TIMEOUT);
    }

    /**
     * Constructor with custom settings
     * @param port Py4J port
     * @param pythonExecutable Python executable path
     * @param serverScriptPath Path to cellpose_server.py
     * @param startupTimeout Timeout for server startup (ms)
     * @param requestTimeout Timeout for requests (ms)
     */
    public CellposeInterface(int port, String pythonExecutable, String serverScriptPath,
                            long startupTimeout, long requestTimeout) {
        this.port = port;
        this.pythonExecutable = pythonExecutable;
        this.serverScriptPath = serverScriptPath;
        this.startupTimeout = startupTimeout;
        this.requestTimeout = requestTimeout;
    }

    /**
     * Find the cellpose_server.py script
     * @return script path or null if not found
     */
    private static String findServerScript() {
        // Check common locations
        String[] searchPaths = {
            "python/cellpose_server.py",
            "../python/cellpose_server.py",
            System.getProperty("user.home") + "/.vtea/python/cellpose_server.py",
            "/usr/local/share/vtea/python/cellpose_server.py"
        };

        for (String path : searchPaths) {
            if (Files.exists(Paths.get(path))) {
                return path;
            }
        }

        return null; // Script not found
    }

    /**
     * Start the Python Cellpose server
     * @throws DeepLearningException if server fails to start
     */
    public void startServer() throws DeepLearningException {
        if (serverRunning) {
            return; // Already running
        }

        if (serverScriptPath == null || !Files.exists(Paths.get(serverScriptPath))) {
            throw new DeepLearningException(
                DeepLearningException.ErrorType.SERVER_STARTUP_FAILED,
                "Cellpose server script not found. Expected at: " + serverScriptPath
            );
        }

        try {
            // Build Python command
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(serverScriptPath);
            command.add("--port");
            command.add(String.valueOf(port));

            // Start Python process
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pythonProcess = pb.start();

            // Start output reader thread
            startOutputReader();

            // Wait for server to be ready
            if (!waitForServerReady(startupTimeout)) {
                stopServer();
                throw new DeepLearningException(
                    DeepLearningException.ErrorType.SERVER_STARTUP_FAILED,
                    "Python server did not start within " + startupTimeout + "ms"
                );
            }

            // Connect Py4J gateway
            connectGateway();

            serverRunning = true;
            restartAttempts = 0;

            System.out.println("Cellpose server started successfully on port " + port);

        } catch (IOException e) {
            throw new DeepLearningException(
                DeepLearningException.ErrorType.SERVER_STARTUP_FAILED,
                "Failed to start Python process: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Start a thread to read and log Python process output
     */
    private void startOutputReader() {
        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(pythonProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Cellpose] " + line);
                }
            } catch (IOException e) {
                System.err.println("Error reading Python output: " + e.getMessage());
            }
        });
        readerThread.setDaemon(true);
        readerThread.setName("CellposeOutputReader");
        readerThread.start();
    }

    /**
     * Wait for Python server to be ready
     * @param timeout timeout in milliseconds
     * @return true if server is ready
     */
    private boolean waitForServerReady(long timeout) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeout) {
            if (pythonProcess != null && pythonProcess.isAlive()) {
                // Check if port is listening (simple approach)
                try {
                    Thread.sleep(1000);
                    return true; // Assume ready after 1 second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * Connect to Python server via Py4J
     * @throws DeepLearningException if connection fails
     */
    private void connectGateway() throws DeepLearningException {
        try {
            gatewayServer = new GatewayServer(null, port);
            gatewayServer.start();

            // Get Python entry point
            pythonEntryPoint = gatewayServer.getPythonServerEntryPoint(new Class[0]);

            // Test connection
            if (!testConnection()) {
                throw new DeepLearningException(
                    DeepLearningException.ErrorType.CONNECTION_FAILED,
                    "Failed to establish connection to Python server"
                );
            }

        } catch (Exception e) {
            throw new DeepLearningException(
                DeepLearningException.ErrorType.CONNECTION_FAILED,
                "Py4J gateway connection failed: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Stop the Python server
     */
    public void stopServer() {
        serverRunning = false;

        if (gatewayServer != null) {
            try {
                gatewayServer.shutdown();
            } catch (Exception e) {
                System.err.println("Error shutting down gateway: " + e.getMessage());
            }
            gatewayServer = null;
        }

        if (pythonProcess != null && pythonProcess.isAlive()) {
            pythonProcess.destroy();
            try {
                pythonProcess.waitFor(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                pythonProcess.destroyForcibly();
            }
            pythonProcess = null;
        }

        System.out.println("Cellpose server stopped");
    }

    /**
     * Restart the server with exponential backoff
     * @throws DeepLearningException if restart fails
     */
    private void restartServer() throws DeepLearningException {
        if (restartAttempts >= MAX_RESTART_ATTEMPTS) {
            throw new DeepLearningException(
                DeepLearningException.ErrorType.SERVER_STARTUP_FAILED,
                "Server restart failed after " + MAX_RESTART_ATTEMPTS + " attempts"
            );
        }

        System.out.println("Restarting Cellpose server (attempt " + (restartAttempts + 1) + ")");

        stopServer();

        try {
            long backoff = RESTART_BACKOFF_MS * (long) Math.pow(2, restartAttempts);
            Thread.sleep(backoff);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        restartAttempts++;
        startServer();
    }

    /**
     * Check if server is running
     * @return true if running
     */
    public boolean isServerRunning() {
        return serverRunning && pythonProcess != null && pythonProcess.isAlive();
    }

    /**
     * Test connection to Python server
     * @return true if connection is working
     */
    public boolean testConnection() {
        if (!isServerRunning()) {
            return false;
        }

        try {
            // Call a simple Python method to test
            java.lang.reflect.Method method = pythonEntryPoint.getClass().getMethod("health_check");
            Object result = method.invoke(pythonEntryPoint);
            return "OK".equals(result);
        } catch (Exception e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get Python version from server
     * @return Python version string
     * @throws DeepLearningException if call fails
     */
    public String getPythonVersion() throws DeepLearningException {
        ensureServerRunning();
        try {
            java.lang.reflect.Method method = pythonEntryPoint.getClass().getMethod("get_python_version");
            return (String) method.invoke(pythonEntryPoint);
        } catch (Exception e) {
            throw new DeepLearningException("Failed to get Python version: " + e.getMessage(), e);
        }
    }

    /**
     * Get Cellpose version from server
     * @return Cellpose version string
     * @throws DeepLearningException if call fails
     */
    public String getCellposeVersion() throws DeepLearningException {
        ensureServerRunning();
        try {
            java.lang.reflect.Method method = pythonEntryPoint.getClass().getMethod("get_version");
            return (String) method.invoke(pythonEntryPoint);
        } catch (Exception e) {
            throw new DeepLearningException("Failed to get Cellpose version: " + e.getMessage(), e);
        }
    }

    /**
     * Segment 2D image
     * @param ip ImageProcessor
     * @param params Cellpose parameters
     * @return 2D label mask
     * @throws DeepLearningException if segmentation fails
     */
    public int[][] segment2D(ImageProcessor ip, CellposeParams params) throws DeepLearningException {
        ensureServerRunning();

        try {
            // Convert ImageProcessor to byte array
            int width = ip.getWidth();
            int height = ip.getHeight();
            float[] pixels = (float[]) ip.convertToFloatProcessor().getPixels();
            byte[] imageBytes = floatArrayToBytes(pixels);

            // Call Python segmentation
            java.lang.reflect.Method method = pythonEntryPoint.getClass().getMethod(
                "segment_2d", byte[].class, int.class, int.class, String.class);

            byte[] resultBytes = (byte[]) method.invoke(pythonEntryPoint,
                imageBytes, width, height, params.toJSON());

            // Convert result back to int array
            return bytesToIntArray2D(resultBytes, width, height);

        } catch (Exception e) {
            handleSegmentationError(e);
            throw new DeepLearningException(
                DeepLearningException.ErrorType.INFERENCE_FAILED,
                "2D segmentation failed: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Segment 3D stack
     * @param stack ImageStack
     * @param params Cellpose parameters
     * @return 3D label mask
     * @throws DeepLearningException if segmentation fails
     */
    public int[][][] segment3D(ImageStack stack, CellposeParams params) throws DeepLearningException {
        ensureServerRunning();

        try {
            // Convert ImageStack to byte array
            int width = stack.getWidth();
            int height = stack.getHeight();
            int depth = stack.getSize();

            float[] pixels = stackToFloatArray(stack);
            byte[] imageBytes = floatArrayToBytes(pixels);

            // Call Python segmentation
            java.lang.reflect.Method method = pythonEntryPoint.getClass().getMethod(
                "segment_3d", byte[].class, int.class, int.class, int.class, String.class);

            byte[] resultBytes = (byte[]) method.invoke(pythonEntryPoint,
                imageBytes, width, height, depth, params.toJSON());

            // Convert result back to int array
            return bytesToIntArray3D(resultBytes, width, height, depth);

        } catch (Exception e) {
            handleSegmentationError(e);
            throw new DeepLearningException(
                DeepLearningException.ErrorType.INFERENCE_FAILED,
                "3D segmentation failed: " + e.getMessage(),
                e
            );
        }
    }

    /**
     * Ensure server is running, restart if necessary
     * @throws DeepLearningException if server cannot be started
     */
    private void ensureServerRunning() throws DeepLearningException {
        if (!isServerRunning()) {
            if (restartAttempts < MAX_RESTART_ATTEMPTS) {
                restartServer();
            } else {
                throw new DeepLearningException(
                    DeepLearningException.ErrorType.CONNECTION_FAILED,
                    "Server is not running and restart limit reached"
                );
            }
        }
    }

    /**
     * Handle segmentation errors with recovery logic
     */
    private void handleSegmentationError(Exception e) {
        String message = e.getMessage();
        if (message != null && (message.contains("CUDA out of memory") || message.contains("GPU"))) {
            System.err.println("GPU error detected. Consider switching to CPU mode or reducing chunk size.");
        }
    }

    /**
     * Convert float array to byte array
     */
    private byte[] floatArrayToBytes(float[] floats) {
        byte[] bytes = new byte[floats.length * 4];
        for (int i = 0; i < floats.length; i++) {
            int bits = Float.floatToIntBits(floats[i]);
            bytes[i * 4] = (byte) (bits & 0xFF);
            bytes[i * 4 + 1] = (byte) ((bits >> 8) & 0xFF);
            bytes[i * 4 + 2] = (byte) ((bits >> 16) & 0xFF);
            bytes[i * 4 + 3] = (byte) ((bits >> 24) & 0xFF);
        }
        return bytes;
    }

    /**
     * Convert ImageStack to float array
     */
    private float[] stackToFloatArray(ImageStack stack) {
        int width = stack.getWidth();
        int height = stack.getHeight();
        int depth = stack.getSize();
        float[] result = new float[width * height * depth];

        for (int z = 0; z < depth; z++) {
            ImageProcessor ip = stack.getProcessor(z + 1);
            float[] slice = (float[]) ip.convertToFloatProcessor().getPixels();
            System.arraycopy(slice, 0, result, z * width * height, width * height);
        }

        return result;
    }

    /**
     * Convert byte array to 2D int array
     */
    private int[][] bytesToIntArray2D(byte[] bytes, int width, int height) {
        int[][] result = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = (y * width + x) * 4;
                result[y][x] = ((bytes[idx + 3] & 0xFF) << 24) |
                              ((bytes[idx + 2] & 0xFF) << 16) |
                              ((bytes[idx + 1] & 0xFF) << 8) |
                              (bytes[idx] & 0xFF);
            }
        }
        return result;
    }

    /**
     * Convert byte array to 3D int array
     */
    private int[][][] bytesToIntArray3D(byte[] bytes, int width, int height, int depth) {
        int[][][] result = new int[depth][height][width];
        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = ((z * height + y) * width + x) * 4;
                    result[z][y][x] = ((bytes[idx + 3] & 0xFF) << 24) |
                                     ((bytes[idx + 2] & 0xFF) << 16) |
                                     ((bytes[idx + 1] & 0xFF) << 8) |
                                     (bytes[idx] & 0xFF);
                }
            }
        }
        return result;
    }

    @Override
    public void close() {
        stopServer();
    }
}
