package com.test.manet;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.span.service.ManetHelper;

import android.util.Log;

public class ConnectToManetService extends ManetService {
	final String TAG = "ConnectToManetService";
    CountDownLatch serviceLatch = new CountDownLatch(1);

    public ConnectToManetService(ManetHelper manetHelper) {
    	super(manetHelper);
    }

    @Override
    public Boolean call() throws Exception
    {
        Log.i(TAG, "ConnectToManetService.call()...");
        mManetHelper.connectToService();
        boolean finishedBeforeTimeout = serviceLatch.await(3, TimeUnit.SECONDS);
        return finishedBeforeTimeout;
    }

    @Override
    public void onServiceConnected()
    {
        Log.i(TAG, "ConnectToServiceCallable.onServiceConnected");
        serviceLatch.countDown();
    }
}
