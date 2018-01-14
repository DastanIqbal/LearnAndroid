package com.flow.framework.dao;

import android.content.Context;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import com.flow.framework.callback.IRequestResponse;
import com.flow.framework.net.NetRequest;
import com.flow.framework.net.ResultCallback;
import okhttp3.Request;

public class PullServerDao extends IDao {
    private static PullServerDao instance;

    private class RequestServiceHandler extends ResultCallback {
        private IRequestResponse iRequestResponse;

        public RequestServiceHandler(IRequestResponse iRequestResponse) {
            this.iRequestResponse = iRequestResponse;
        }

        public void onError(Request request, Exception e) {
            super.onError(request, e);
            this.iRequestResponse.onResponeFailure(300, e.getLocalizedMessage());
        }

        public void onResponse(String responseString) {
            super.onResponse(responseString);
            this.iRequestResponse.onResponeSuccess(Callback.DEFAULT_DRAG_ANIMATION_DURATION, responseString);
        }
    }

    private PullServerDao() {
    }

    public static PullServerDao getInstance() {
        if (instance == null) {
            instance = new PullServerDao();
        }
        return instance;
    }

    public void requestService(Context context, IRequestResponse iRequestResponse) {
        NetRequest.instance().enqueueMainServer(context, new RequestServiceHandler(iRequestResponse));
    }
}
