package com.guardanis.netclient.batch;

import com.guardanis.netclient.errors.RequestError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BatchError extends RequestError {

    protected final Map<String, BatchItemResponse> itemResponses;

    public BatchError(Map<String, BatchItemResponse> itemResponses) {
        super("");

        this.itemResponses = itemResponses;
        this.errors = new ArrayList<String>();

        for(BatchItemResponse itemResponse : itemResponses.values()) {
            if(itemResponse.hasErrors()) {
                for(String message : itemResponse.getError()
                        .getErrors()) {
                    if(!errors.contains(message))
                        errors.add(message);
                }
            }
        }
    }

    public Map<String, BatchItemResponse> getItemResponses() {
        return itemResponses;
    }

    public Map<String, BatchItemResponse> getItemResponseFailures() {
        Map<String, BatchItemResponse> failures = new HashMap<String, BatchItemResponse>();

        for(BatchItemResponse item : itemResponses.values())
            if(item.hasErrors())
                failures.put(item.getBatchable().getKey(), item);

        return failures;
    }

}
