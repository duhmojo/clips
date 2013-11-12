package com.drdc.test.testwifiscanner.manet;

import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.span.service.ManetHelper;
import org.span.service.ManetObserver;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.routing.Node;
import org.span.service.system.ManetConfig;

import android.util.Log;

public class ManetManagerObserver implements Callable<Boolean>, ManetObserver {
	final String TAG = "ManetManagerObserver";

	public ManetHelper mManetHelper = null;
	public AdhocStateEnum mStatus = null;

    CountDownLatch adhocLatch = new CountDownLatch(1);
    public Completed mComplete = null;

    boolean expectedState = false;

	public ManetManagerObserver(ManetHelper manetHelper) {
		this.mManetHelper = manetHelper;
		this.mManetHelper.registerObserver(this);
	}

	public void setExpectedState(boolean expected) {
		this.expectedState = expected;
	}

	public boolean startManet() {
		expectedState = true;
		FutureTask<Boolean> adhocFutureTask = new FutureTask<Boolean>(this);
        ExecutorService adhocExecutor = Executors.newFixedThreadPool(1);
        adhocExecutor.execute(adhocFutureTask);
        try {
			if (adhocFutureTask.get()) {
				Log.i(TAG, "Adhoc started successfully.");
				return true;
			} else {
			    Log.i(TAG, "Failed to start adhoc mode!");
			    return false;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return false;
	}

	public boolean stopManet() {
		expectedState = false;
		FutureTask<Boolean> adhocFutureTask = new FutureTask<Boolean>(this);
        ExecutorService adhocExecutor = Executors.newFixedThreadPool(1);
        adhocExecutor.execute(adhocFutureTask);
        try {
			if (adhocFutureTask.get()) {
				Log.i(TAG, "Adhoc started successfully.");
				return true;
			} else {
			    Log.i(TAG, "Failed to start adhoc mode!");
			    return false;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return false;
	}

	@Override
	public Boolean call() throws Exception {
		// Use the expectedState to determine if the manet should be stopped or started
		if(expectedState) {
			Log.i(TAG, "StartAdhocCallable.call()...");
	        mManetHelper.sendStartAdhocCommand();
	        boolean startedAdhocBeforeTimeout = adhocLatch.await(20, TimeUnit.SECONDS);
	        return startedAdhocBeforeTimeout;
		} else {
			Log.i(TAG, "StopAdhocCallable.call()...");
	        mManetHelper.sendStopAdhocCommand();
	        boolean stoppedAdhocBeforeTimeout = adhocLatch.await(20, TimeUnit.SECONDS);
	        return stoppedAdhocBeforeTimeout;
		}
	}

    @Override
    public void onAdhocStateUpdated(AdhocStateEnum state, String info)
    {
    	mStatus = state;
        if (state == AdhocStateEnum.STARTED) {
            Log.i(TAG, "Adhoc State Update: STARTED");
        } else {
        	if (state == AdhocStateEnum.STOPPED) {
        		Log.i(TAG, "Adhoc State Update: STOPPED");
        	} else {
        		mStatus = null;
        	}
        }
        adhocLatch.countDown();
        if(mComplete != null) mComplete.Started();
    }

	@Override
	public void onConfigUpdated(ManetConfig arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPeersUpdated(HashSet<Node> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRoutingInfoUpdated(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceConnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceDisconnected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onServiceStopped() {
		// TODO Auto-generated method stub

	}


}

