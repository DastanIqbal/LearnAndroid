package com.flow.framework.enums;

public enum EventType {
    NOTHING(0),
    DO_OFFERS(1),
    CHECK_SEND_MSG(3),
    DO_UPDATE_REPEATING(5);
    
    private int eventType;

    public static EventType currentMessageCode(int code) {
        return switchEventType(code);
    }

    private static EventType switchEventType(int code) {
        for (EventType eventType : values()) {
            if (eventType.getEventType() == code) {
                return eventType;
            }
        }
        return NOTHING;
    }

    private EventType(int eventType) {
        this.eventType = eventType;
    }

    public int getEventType() {
        return this.eventType;
    }
}
