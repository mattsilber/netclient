package com.guardanis.netclient;

import com.guardanis.netclient.errors.RequestError;

public class NetInterface {

    public interface ResponseParser<T> {
        public T parse(WebResult result) throws Exception;
    }

    public interface SuccessListener<T> {
        public void onSuccess(T result);
    }

    public interface FailListener {
        public void onFail(RequestError error);
    }

}
