package com.guardanis.netclient.batch;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchResponse {

    private Map<String, BatchItemResponse> itemsMap = new HashMap<String, BatchItemResponse>();

    public BatchResponse() { }

    public BatchResponse(Map<String, BatchItemResponse> responses) {
        this.itemsMap = responses;
    }

    public BatchResponse put(BatchItemResponse response){
        itemsMap.put(response.getBatchable().getKey(), response);
        return this;
    }

    public <T extends BatchItemResponse> T get(String key){
        try{
            return (T) itemsMap.get(key);
        }
        catch(ClassCastException e){ e.printStackTrace(); }
        catch(Exception e){ e.printStackTrace(); }

        return null;
    }

    public Collection<BatchItemResponse> getItems(){
        return itemsMap.values();
    }

}