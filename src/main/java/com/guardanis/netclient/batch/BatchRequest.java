package com.guardanis.netclient.batch;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import com.guardanis.netclient.ApiRequest;
import com.guardanis.netclient.NetInterface.*;
import com.guardanis.netclient.R;
import com.guardanis.netclient.WebRequest;
import com.guardanis.netclient.WebResult;
import com.guardanis.netclient.errors.RequestError;
import com.guardanis.netclient.tools.NetUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A simple way to manage multiple, asynchronous GET WebRequests and provide a simple interface for retrieving the data
 */
public class BatchRequest {

    protected Context context;

    protected List<Batchable> batchableItems = new ArrayList<Batchable>();

    protected SuccessListener<BatchResponse> batchSuccessListener;
    protected List<Respondable> itemSuccessListeners = new ArrayList<Respondable>();

    protected FailListener failListener;

    protected List<WebRequest> requests;
    protected boolean canceled = false;
    protected boolean failOnCancel = true;
    protected boolean postedFailures = false;

    protected List<BatchItemResponse> responses = new ArrayList<BatchItemResponse>();

    protected boolean autoDelegateApiRequests = true;

    public BatchRequest(Context context){
        this.context = context.getApplicationContext();
    }

    public BatchRequest add(Batchable batchable){
        batchableItems.add(batchable);

        return this;
    }

    /**
     * A helper method for adding a Batchable and a callback for its successful result.
     * See: add(Batchable) and onItemSuccess(String, SuccessListener)
     */
    public BatchRequest applyFrom(BatchableManager batchableManager, long cacheDuration){
        Batchable batchable = batchableManager.buildBatchable(context, cacheDuration);

        this.add(batchable);

        this.onItemSuccess(batchable.getKey(),
                batchableManager.buildBatchableSuccessListener(context));

        return this;
    }

    /**
     * Set a SuccessListener for the result of a BatchableItemResponse based on the Batchable's key. This response
     * will be triggered only after all requests have successfully completed, but immediately before the BatchSuccess
     * callback is triggered. NOTE: There is absolutely no compile-time type-safety using these callbacks, and it is
     * entirely up to you to ensure your key maps to the correct response value, else you may get ClassCastExceptions
     */
    public <T> BatchRequest onItemSuccess(String key, SuccessListener<T> successListener) {
        return onItemSuccess(new Respondable(key, successListener));
    }

    /**
     * Set a SuccessListener for the result of a BatchableItemResponse based on the Batchable's key. This response
     * will be triggered only after all requests have successfully completed, but immediately before the BatchSuccess
     * callback is triggered. NOTE: There is absolutely no compile-time type-safety using these callbacks, and it is
     * entirely up to you to ensure your key maps to the correct response value, else you may get ClassCastExceptions
     */
    public <T> BatchRequest onItemSuccess(Respondable<T> respondable) {
        itemSuccessListeners.add(respondable);

        return this;
    }

    /**
     * Set a SuccessListener for the overall BatchResponse. This response will be triggered only after all
     * requests have successfully completed and each individual BatchItemResponse SuccessListener has been triggered
     */
    public BatchRequest onBatchSuccess(SuccessListener<BatchResponse> successListener) {
        this.batchSuccessListener = successListener;
        return this;
    }

    public BatchRequest onBatchFail(FailListener failListener) {
        this.failListener = failListener;
        return this;
    }

    public BatchRequest setFailOnCancel(boolean failOnCancel){
        this.failOnCancel = failOnCancel;
        return this;
    }

    public BatchRequest execute(){
        if(requests != null || postedFailures)
            throw new RuntimeException("You cannot restart a BatchRequest!");
        else if(batchableItems.size() < 1)
            throw new RuntimeException("You never added any Batchables!");

        this.requests = new ArrayList<WebRequest>();

        for(Batchable batchable : batchableItems)
            requests.add(buildBatchRequest(batchable)
                    .execute());

        return this;
    }

    protected WebRequest<BatchItemResponse> buildBatchRequest(Batchable batchable){
        if(!autoDelegateApiRequests || Patterns.WEB_URL
                .matcher(batchable.getUrl())
                .matches())
            return buildWebRequest(batchable);
        else
            return buildApiRequest(batchable);
    }

    protected WebRequest<BatchItemResponse> buildWebRequest(Batchable batchable){
        WebRequest<BatchItemResponse> request = new WebRequest<BatchItemResponse>(context,
                WebRequest.ConnectionType.GET,
                batchable.getUrl());

        return configureRequest(request, batchable);
    }
    protected WebRequest<BatchItemResponse> buildApiRequest(Batchable batchable){
        WebRequest<BatchItemResponse> request = new ApiRequest<BatchItemResponse>(context,
                WebRequest.ConnectionType.GET,
                batchable.getUrl());

        return configureRequest(request, batchable);
    }

    protected WebRequest<BatchItemResponse> configureRequest(WebRequest<BatchItemResponse> request, Batchable batchable){
        request.setCacheDurationMs(batchable.getMaxCacheDuration())
                .setResponseParser(new BatchItemParser(batchable))
                .onSuccess(new BatchItemSuccessListener(batchable))
                .onFail(new BatchItemFailListener(batchable));

        Map<String, String> properties = batchable.getRequestProperties();

        for(Entry<String, String> item : properties.entrySet())
            request.addRequestProperty(item.getKey(), item.getValue());

        return request;
    }

    protected void onDataReceived(){
        if(!canceled && responses.size() == batchableItems.size())
            processResponses();
    }

    protected void processResponses(){
        final List<String> errors = new ArrayList<String>();

        for(BatchItemResponse response : responses)
            if(response.hasErrors())
                errors.addAll(response.getError().getErrors());

        if(0 < errors.size()){
            if(!canceled || failOnCancel)
                safelyPostFailure(new RequestError(errors));

            return;
        }

        BatchResponse response = new BatchResponse();

        for(BatchItemResponse item : responses)
            response.put(item);

            postSuccessfulData(response);
    }

    protected void postSuccessfulData(final BatchResponse response){
        if(canceled || postedFailures)
            return;

        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    public void run() {
                        try{
                            for(Respondable respondable : itemSuccessListeners)
                                respondable.getSuccessListener()
                                        .onSuccess(response.get(respondable.getKey())
                                                .getData());

                            if(batchSuccessListener != null)
                                batchSuccessListener.onSuccess(response);
                        }
                        catch(Throwable e){
                            if(NetUtils.getInstance(context)
                                    .isLoggingEnabled())
                                e.printStackTrace();

                            if(!canceled)
                                safelyPostFailure(new RequestError(e.getMessage()));
                        }
                    }
                });
    }

    public void cancel(){
        this.canceled = true;

        if(failOnCancel)
            safelyPostFailure(new RequestError(context.getString(R.string.nc__error_canceled)));

        if(requests != null)
            for(WebRequest request : requests)
                request.cancel();
    }

    protected void safelyPostFailure(final RequestError error){
        if(postedFailures)
            return;

        postedFailures = true;

        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    public void run() {
                        if(failListener != null)
                            failListener.onFail(error);
                    }
                });
    }

    protected static class BatchItemParser implements ResponseParser<BatchItemResponse> {
        protected final Batchable batchable;

        public BatchItemParser(Batchable batchable){
            this.batchable = batchable;
        }

        @Override
        public BatchItemResponse parse(WebResult result) throws Exception {
            return new BatchItemResponse(batchable)
                    .setResult(result, batchable.getResponseParser()
                            .parse(result));
        }
    }

    protected class BatchItemSuccessListener implements SuccessListener<BatchItemResponse> {
        protected final Batchable batchable;

        public BatchItemSuccessListener(Batchable batchable){
            this.batchable = batchable;
        }

        @Override
        public void onSuccess(BatchItemResponse result) {
            responses.add(result);

            onDataReceived();
        }
    }

    protected class BatchItemFailListener implements FailListener {
        protected final Batchable batchable;

        public BatchItemFailListener(Batchable batchable){
            this.batchable = batchable;
        }

        @Override
        public void onFail(RequestError error) {
            responses.add(new BatchItemResponse(batchable)
                    .setError(error));

            onDataReceived();
        }
    }

    /**
     * Set whether or not to automatically delegate between WebRequest/ApiRequest base
     * on the Batchable's URL
     */
    public BatchRequest setAutoDelegateApiRequests(boolean autoDelegateApiRequests) {
        this.autoDelegateApiRequests = autoDelegateApiRequests;
        return this;
    }

    public static class Respondable<T> {
        private String key;
        private SuccessListener<T> successListener;

        public Respondable(String key, SuccessListener<T> successListener){
            this.key = key;
            this.successListener = successListener;
        }

        public String getKey() {
            return key;
        }

        public SuccessListener<T> getSuccessListener() {
            return successListener;
        }
    }

}
