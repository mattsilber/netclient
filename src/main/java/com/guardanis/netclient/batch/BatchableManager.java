package com.guardanis.netclient.batch;

import android.content.Context;

import com.guardanis.netclient.NetInterface;

public interface BatchableManager<T> {

    public Batchable<T> buildBatchable(Context context, long cacheDuration);

    public NetInterface.SuccessListener<T> buildBatchableSuccessListener(Context context);

}