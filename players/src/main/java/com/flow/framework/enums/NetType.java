package com.flow.framework.enums;

public enum NetType {
    WIFI(1),
    MOBILE(0);
    
    private int eventType;

    public static NetType currentMessageCode(int code) {
        return switchEventType(code);
    }

    private static NetType switchEventType(int code) {
        for (NetType eventType : values()) {
            if (eventType.getEventType() == code) {
                return eventType;
            }
        }
        return WIFI;
    }

    private NetType(int eventType) {
        this.eventType = eventType;
    }

    public int getEventType() {
        return this.eventType;
    }
}
