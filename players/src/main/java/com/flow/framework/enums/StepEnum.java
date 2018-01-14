package com.flow.framework.enums;

public enum StepEnum {
    TRANK_URL(1),
    INJECT_JS_FUN(2),
    EXECUTE_FUN_LOCK(3),
    EXECUTE_FUN_NOT_LOCK(4),
    SEND_MESSAGE(5),
    OBTAIN_HTML_CODE(6),
    DESTORY_RESET(7),
    SET_PHONE_DATA(8),
    GET_IMAGE_BASE64(9),
    UPLOAD_USER_SMS_FOR_LOG(10),
    SEND_MESSAGE_FOR_PIN(11),
    DO_JS_FUN_MODEL_TO_SMS(12),
    SEND_MESSAGE_FOR_ZA(13);
    
    private int step_code;

    public static StepEnum currentStep(int code) {
        return switchStep(code);
    }

    public static int getCurrentStep(StepEnum stepEnum) {
        return stepEnum.getStep();
    }

    private static StepEnum switchStep(int code) {
        for (StepEnum stepEnum : values()) {
            if (stepEnum.getStep() == code) {
                return stepEnum;
            }
        }
        return TRANK_URL;
    }

    private StepEnum(int step_code) {
        this.step_code = step_code;
    }

    public int getStep() {
        return this.step_code;
    }
}
