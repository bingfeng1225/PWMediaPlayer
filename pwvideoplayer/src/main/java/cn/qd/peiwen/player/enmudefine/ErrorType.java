package cn.qd.peiwen.player.enmudefine;

/**
 * Created by nick on 2017/12/13.
 */

/**
 * 播放错误类型信息
 */
public enum ErrorType {
    MEDIA_ERROR_NONE(""),
    MEDIA_ERROR_UNKNOWN("An unknown error occurred"),
    MEDIA_ERROR_INVALID_REQUEST("The server recognized the request as being malformed (bad request, unauthorized, forbidden, not found, etc)"),
    MEDIA_ERROR_SERVICE_UNAVAILABLE("The device was unavailable to reach the service"),
    MEDIA_ERROR_INTERNAL_SERVER_ERROR("The server accepted the request, but was unable to process it as expected"),
    MEDIA_ERROR_INTERNAL_DEVICE_ERROR("There was an internal error on the device");

    private final String message;

    ErrorType(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
