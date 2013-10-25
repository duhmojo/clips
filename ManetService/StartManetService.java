package com.test.manet;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.span.service.ManetHelper;
import org.span.service.core.ManetService.AdhocStateEnum;

import android.app.Activity;
import android.util.Log;

public class StartManetService extends ManetService {
	final String TAG = "StartManetService";
    CountDownLatch adhocLatch = new CountDownLatch(1);

	public StartManetService(ManetHelper manetHelper) {
		super(manetHelper);
	}

	@Override
    public Boolean call() throws Exception
    {
        Log.i(TAG, "StartAdhocCallable.call()...");
        mManetHelper.sendStartAdhocCommand();
        boolean startedAdhocBeforeTimeout = adhocLatch.await(20, TimeUnit.SECONDS);
        return startedAdhocBeforeTimeout;
    }

    @Override
    public void onAdhocStateUpdated(AdhocStateEnum state, String info)
    {
        if (state == AdhocStateEnum.STARTED) {
            Log.i(TAG, "Adhoc is started!");
            adhocLatch.countDown();
        } else {
            Log.i(TAG, "Adhoc is not started!");
        }
    }
}
