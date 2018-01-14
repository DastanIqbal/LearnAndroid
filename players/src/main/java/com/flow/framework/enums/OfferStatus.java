package com.flow.framework.enums;

public enum OfferStatus {
    OFFER_FRIST(0),
    UPDATE_USER_STATUS(1),
    LP_FRIST_PAGE(2),
    LP_SECOND_PAGE(3),
    LP_POSTSUBMIT(4),
    LP_ADVERTISERDATA(5),
    LP_SENDSMS(6),
    UPDATE_USER_FAIL(7),
    PAYMENT_BACK(8),
    RELEASE_DATA(10),
    UPLOAD_DURING(11),
    UPLOAD_DURING_1(12),
    UPLOAD_DURING_3(13),
    UPLOAD_DURING_4(14),
    UPLOAD_HTML_CODE(15),
    UPLOAD_SMS_LOG(16),
    UPLOAD_SMS_REPLACE_CODE(17),
    SETTING_END_URL(18);
    
    private int offerStatus;

    public static int getIntFromOfferStatus(OfferStatus offerStatus) {
        return offerStatus.getOfferStatus();
    }

    private OfferStatus(int offerStatus) {
        this.offerStatus = offerStatus;
    }

    public int getOfferStatus() {
        return this.offerStatus;
    }
}
