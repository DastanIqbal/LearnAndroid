package processing.async.exceptions;

public class FFmpegCommandAlreadyRunningException extends Exception {

    public FFmpegCommandAlreadyRunningException(String message) {
        super(message);
    }

}
