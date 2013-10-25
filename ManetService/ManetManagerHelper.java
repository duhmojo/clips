package com.test.manet;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.span.service.ManetHelper;
import android.content.Context;
import android.util.Log;

public class ManetManagerHelper {
	final String TAG = "ManetManagerHelper";

	private static ManetManagerHelper instance = null;
	private ManetHelper manetHelper = null;
	protected ManetManagerHelper(ManetHelper manetHelper) {
		this.manetHelper = manetHelper;
	}
	public static ManetManagerHelper getInstance(Context context) {
		if(instance == null) {
			instance = new ManetManagerHelper(new ManetHelper(context));
		}
		return instance;
	}
	public void Connect() {
		try {
			ConnectToManetService connectToService = new ConnectToManetService(manetHelper);

			manetHelper.registerObserver(connectToService);
		    FutureTask<Boolean> serviceFutureTask = new FutureTask<Boolean>(connectToService);
		    ExecutorService serviceExecutor = Executors.newFixedThreadPool(1);
		    serviceExecutor.execute(serviceFutureTask);

		    Log.i(TAG, "Blocking waiting for serviceFutureTask...");
		    boolean serviceStarted = serviceFutureTask.get();
		    if (serviceStarted) {
		    	Log.i(TAG, "Service started.");
		    } else {
		    	Log.i(TAG, "Service failed to start!");
		    	return;
		    }
		    manetHelper.unregisterObserver(connectToService);
		} catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	}
	public void Start() {
		try {
			StartManetService startAdhocCallable = new StartManetService(manetHelper);

			manetHelper.registerObserver(startAdhocCallable);
	        FutureTask<Boolean> adhocFutureTask = new FutureTask<Boolean>(startAdhocCallable);
	        ExecutorService adhocExecutor = Executors.newFixedThreadPool(1);
	        adhocExecutor.execute(adhocFutureTask);
	        boolean adhocStarted = adhocFutureTask.get();
	        if (adhocStarted) {
	        	Log.i(TAG, "Adhoc started successfully.");
	        } else {
	            Log.i(TAG, "Failed to start adhoc mode!");
	            return;
	        }
	        manetHelper.unregisterObserver(startAdhocCallable);
		} catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	}
	public void Stop() {
		try {
			StopManetService stopAdhocCallable = new StopManetService(manetHelper);

			manetHelper.registerObserver(stopAdhocCallable);
	        FutureTask<Boolean> adhocFutureTask = new FutureTask<Boolean>(stopAdhocCallable);
	        ExecutorService adhocExecutor = Executors.newFixedThreadPool(1);
	        adhocExecutor.execute(adhocFutureTask);
	        boolean adhocStopped = adhocFutureTask.get();
	        if (adhocStopped) {
	        	Log.i(TAG, "Adhoc stopped successfully.");
	        } else {
	            Log.i(TAG, "Failed to stop adhoc mode!");
	            return;
	        }
	        manetHelper.unregisterObserver(stopAdhocCallable);
		} catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	}
}
