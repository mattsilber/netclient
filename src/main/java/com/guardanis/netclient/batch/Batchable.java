package com.guardanis.netclient.batch;

import com.guardanis.netclient.NetInterface;
import com.guardanis.netclient.tools.RequestProperties;
import com.guardanis.netclient.tools.UrlParams;

import java.util.HashMap;
import java.util.Map;

public class Batchable<T> {

    private String key;
    private String url;

    private final Map<String, String> requestProperties = new HashMap<String, String>();

    private NetInterface.ResponseParser<T> responseParser;

    private long maxCacheDuration = 0;

    public Batchable(String key) {
        this.key = key;
    }

    public String getKey(){
        return key;
    }

    public Batchable<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Batchable<T> setRequestProperty(String key, String value){
        this.requestProperties.put(key, value);
        return this;
    }

    public Map<String, String> getRequestProperties() {
        return requestProperties;
    }

    public NetInterface.ResponseParser<T> getResponseParser() {
        return responseParser;
    }

    public Batchable<T> setResponseParser(NetInterface.ResponseParser<T> responseParser) {
        this.responseParser = responseParser;
        return this;
    }

    public long getMaxCacheDuration() {
        return maxCacheDuration;
    }

    public Batchable<T> setMaxCacheDuration(long maxCacheDuration) {
        this.maxCacheDuration = maxCacheDuration;
        return this;
    }
}
