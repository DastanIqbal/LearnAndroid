package com.flow.framework.callback;

public interface IRequestResponse {
    void onResponeFailure(int i, String str);

    void onResponeSuccess(int i, String str);
}
