package com.test.manet;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.span.service.ManetHelper;
import org.span.service.core.ManetService.AdhocStateEnum;

import android.app.Activity;
import android.util.Log;

public class StopManetService extends ManetService {
	final String TAG = "StopManetService";
    CountDownLatch adhocLatch = new CountDownLatch(1);

	public StopManetService(ManetHelper manetHelper) {
		super(manetHelper);
	}

	@Override
    public Boolean call() throws Exception
    {
        Log.i(TAG, "StopAdhocCallable.call()...");
        mManetHelper.sendStopAdhocCommand();
        boolean stoppedAdhocBeforeTimeout = adhocLatch.await(20, TimeUnit.SECONDS);
        return stoppedAdhocBeforeTimeout;
    }

    @Override
    public void onAdhocStateUpdated(AdhocStateEnum state, String info)
    {
        if (state == AdhocStateEnum.STOPPED) {
            Log.i(TAG, "Adhoc is stopped!");
            adhocLatch.countDown();
        } else {
            Log.i(TAG, "Adhoc is not stopped!");
        }
    }
}
