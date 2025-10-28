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

/**
 * Exception thrown when deep learning operations fail.
 * Wraps errors from Python communication, model loading, inference, etc.
 *
 * @author sethwinfree
 */
public class DeepLearningException extends Exception {

    private final ErrorType errorType;

    /**
     * Types of deep learning errors
     */
    public enum ErrorType {
        SERVER_STARTUP_FAILED("Failed to start Python server"),
        CONNECTION_FAILED("Failed to connect to Python server"),
        MODEL_LOAD_FAILED("Failed to load model"),
        INFERENCE_FAILED("Inference failed"),
        TIMEOUT("Operation timed out"),
        GPU_ERROR("GPU error occurred"),
        DATA_TRANSFER_ERROR("Data transfer error"),
        INVALID_PARAMETERS("Invalid parameters"),
        UNKNOWN("Unknown error");

        private final String description;

        ErrorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Constructor with error type and message
     * @param errorType type of error
     * @param message error message
     */
    public DeepLearningException(ErrorType errorType, String message) {
        super(errorType.getDescription() + ": " + message);
        this.errorType = errorType;
    }

    /**
     * Constructor with error type, message, and cause
     * @param errorType type of error
     * @param message error message
     * @param cause underlying cause
     */
    public DeepLearningException(ErrorType errorType, String message, Throwable cause) {
        super(errorType.getDescription() + ": " + message, cause);
        this.errorType = errorType;
    }

    /**
     * Constructor with message only (unknown error type)
     * @param message error message
     */
    public DeepLearningException(String message) {
        super(message);
        this.errorType = ErrorType.UNKNOWN;
    }

    /**
     * Constructor with message and cause (unknown error type)
     * @param message error message
     * @param cause underlying cause
     */
    public DeepLearningException(String message, Throwable cause) {
        super(message, cause);
        this.errorType = ErrorType.UNKNOWN;
    }

    /**
     * Get the error type
     * @return error type
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Check if this is a recoverable error
     * @return true if error might be recoverable
     */
    public boolean isRecoverable() {
        switch (errorType) {
            case CONNECTION_FAILED:
            case TIMEOUT:
            case DATA_TRANSFER_ERROR:
                return true;
            case SERVER_STARTUP_FAILED:
            case MODEL_LOAD_FAILED:
            case GPU_ERROR:
            case INVALID_PARAMETERS:
            case INFERENCE_FAILED:
            case UNKNOWN:
            default:
                return false;
        }
    }

    /**
     * Get user-friendly error message with suggestions
     * @return user-friendly message
     */
    public String getUserMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage()).append("\n\n");

        switch (errorType) {
            case SERVER_STARTUP_FAILED:
                sb.append("Suggestions:\n");
                sb.append("- Check that Python 3 is installed\n");
                sb.append("- Run: python -m pip install cellpose py4j\n");
                sb.append("- Check Python path in settings\n");
                break;

            case CONNECTION_FAILED:
                sb.append("Suggestions:\n");
                sb.append("- Restart the Python server\n");
                sb.append("- Check that port 25333 is not blocked\n");
                sb.append("- Check firewall settings\n");
                break;

            case MODEL_LOAD_FAILED:
                sb.append("Suggestions:\n");
                sb.append("- Check internet connection (models download on first use)\n");
                sb.append("- Verify model name is correct\n");
                sb.append("- Check disk space in model cache directory\n");
                break;

            case GPU_ERROR:
                sb.append("Suggestions:\n");
                sb.append("- Try switching to CPU mode\n");
                sb.append("- Reduce chunk size to use less GPU memory\n");
                sb.append("- Update GPU drivers\n");
                break;

            case TIMEOUT:
                sb.append("Suggestions:\n");
                sb.append("- Increase timeout in settings\n");
                sb.append("- Reduce chunk size\n");
                sb.append("- Check if Python server is responsive\n");
                break;

            case INVALID_PARAMETERS:
                sb.append("Suggestions:\n");
                sb.append("- Check parameter ranges\n");
                sb.append("- Verify channel indices are valid\n");
                sb.append("- Ensure diameter > 0\n");
                break;

            default:
                sb.append("Please check the log file for more details.");
                break;
        }

        return sb.toString();
    }
}
