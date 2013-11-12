package com.org.test.testwifiscanner.comm;

import java.util.Random;
import java.util.concurrent.Callable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;import java.util.concurrent.FutureTask;

import org.osmdroid.util.GeoPoint;
import org.span.service.ManetHelper;


import com.org.test.testwifiscanner.manet.ManetManagerHelper;
import com.org.test.testwifiscanner.manet.ManetManagerObserver;
import com.org.test.testwifiscanner.manet.ManetStarter;
import com.org.test.testwifiscanner.support.ScannedWifiNode;
import com.org.test.testwifiscanner.support.ScannedWifiNodeHelper;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;

public class ExampleManetService extends Service implements LocationListener {
	public final static String TAG = "ExampleManetService";

	LocationManager locationManager = null;
	GeoPoint currentLocation = null;

	ManetHelper manetHelper = null;
	ManetManagerHelper manetManagerHelper = null;

	ManetStarter manetStarter = null;
	ManetManagerObserver manetManagerObserver = null;

	ScannedWifiNodeHelper wifiScanNodeHelper;

	WifiManager mainWifi;

	Service service = null;

	public final static int UDP_BROADCAST_PORT = 7777;
	public final static int TCP_LISTENING_PORT = 7778;

	public STAGES stage = STAGES.STOPPED;
	public enum STAGES {
		STOPPED,
		LISTENING,
		DISABLE_MANET,
		ENABLE_WIFI,
		START_SCAN,
		COMPLETED_SCAN,
		DISABLE_WIFI,
		ENABLE_MANET,
		SEND_SCANS
	}

	String UUID = "";

	public STAGES getStage() {
		return this.stage;
	}
	public WifiManager getWifiManager() {
		return this.mainWifi;
	}
	public ScannedWifiNodeHelper getScannedWifiNodeHelper() {
		return this.wifiScanNodeHelper;
	}

	public boolean isReceiverRegistered() {
		return autoReceiverRegistered;
	}

	public Boolean isManetRunning() {
		if(manetManagerHelper == null)
			manetManagerHelper = ManetManagerHelper.getInstance(manetHelper, activity);
		if(manetManagerHelper.connected) {
			return manetManagerHelper.running;
		} else {
			return false;
		}
	}

	private Activity activity = null;
	public void setActivity(Activity activity) {
		this.activity = activity;
		if(manetManagerHelper != null)
			manetManagerHelper.setActivity(activity);
		if(manetStarter != null)
			manetStarter.setActivity(activity);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.service = this;

		GeoPoint defaultGeoPoint = new GeoPoint(44335222, -74885210); // fake GPS coords
		int z = 500;
		Random rand = new Random();
		int xr = rand.nextInt(z) - (z/2);
		int yr = rand.nextInt(z) - (z/2);
		currentLocation = new GeoPoint(defaultGeoPoint.getLongitudeE6() + xr, defaultGeoPoint.getLatitudeE6() + yr);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		UUID = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

		wifiScanNodeHelper = new ScannedWifiNodeHelper();
		mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		manetHelper = new ManetHelper(this.getApplicationContext());
		manetManagerObserver = new ManetManagerObserver(manetHelper);
		Log.i(TAG, "Connecting to Manet Manager's Service");
		manetHelper.connectToService();
	}

	public void automate() {
		Thread thread = new Thread()
        {
            @Override
            public void run()
            {
            	// We should already be connected to ManetManager Service and it should be running, so...
        		// 1) Stop MM
        		// 2) Enable Wifi
        		// 3) Register Wifi Receiver and collect scans
        		// 4) Unregister receiver
        		// 5) Disable Wifi
        		// 6) Enable MM
            	synchronized(wifiScanNodeHelper) {
            		wifiScanNodeHelper.clear();
            	}

        		Log.i(TAG, "1) Stopping Manet");
        		//manetManagerObserver.stopManet();
        		manetManagerObserver.setExpectedState(false);
        		FutureTask<Boolean> adhocFutureTask = new FutureTask<Boolean>(manetManagerObserver);
                ExecutorService adhocExecutor = Executors.newFixedThreadPool(1);
                adhocExecutor.execute(adhocFutureTask);
                try {
        			if (adhocFutureTask.get()) {
        				Log.i(TAG, "Adhoc stopped successfully.");
        			} else {
        			    Log.i(TAG, "Failed to stop adhoc mode!");
        			}
        		} catch (InterruptedException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (ExecutionException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}

        		Log.i(TAG, "2) Enabling Wifi");
        		FutureTask<Boolean> wifiEnableFutureTask = new FutureTask<Boolean>(new EnableWifi());
        		ExecutorService wifiEnableExecutor = Executors.newFixedThreadPool(1);
        		wifiEnableExecutor.execute(wifiEnableFutureTask);
                try {
        			if (wifiEnableFutureTask.get()) {
        				Log.i(TAG, "Wifi enabled successfully.");
        			} else {
        			    Log.i(TAG, "Failed to enable wifi!");
        			}
        			Thread.sleep(2000);
        		} catch (InterruptedException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (ExecutionException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
                Log.d(TAG, "Test: Wifi is: " + (mainWifi.isWifiEnabled()?"enabled":"disabled"));

        		Log.i(TAG, "3) Register Wifi Receiver to collect scans");
        		if(autoReceiver == null)
        			autoReceiver = new AutoReceiver(1);
        		autoReceiver.startCollecting();
        		service.registerReceiver(autoReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        		autoReceiverRegistered = true;
       			mainWifi.startScan();
            }
        };
        thread.start();
	}
	private AutoReceiver autoReceiver = null;


	public class EnableWifi implements Callable<Boolean> {
		@Override
		public Boolean call() throws Exception {
			// Use the expectedState to determine if the manet should be stopped or started
			Log.d(TAG, "Enabling wifi...");
			return mainWifi.setWifiEnabled(true);
		}
	}

	public class DisableWifi implements Callable<Boolean> {
		@Override
		public Boolean call() throws Exception {
			// Use the expectedState to determine if the manet should be stopped or started
			Log.d(TAG, "Disabling wifi...");
			return mainWifi.setWifiEnabled(false);
		}
	}

	boolean autoReceiverRegistered = false;
	public class AutoReceiver extends BroadcastReceiver {
		long scanTimestamp1;
		long scanTimestamp2;
		long scanDuration;
		int scanLimit;
		int scanCount = 0;
		boolean collect = false;
		public AutoReceiver(int scanLimit) {
			super();
			this.scanLimit = scanLimit;
		}
		public void startCollecting() {
			collect = true;
		}
		public void stopCollecting() {
			collect = false;
		}
		public void onReceive(Context c, Intent intent) {
			Log.d(TAG, "onReceived: " + intent.getAction());
			if(collect) {
		    	Log.d(TAG, "Calling Receiver onReceive count: " + (scanCount + 1));
		    	scanTimestamp2 = System.currentTimeMillis()/1000;
		    	if(scanTimestamp1 == 0)
		    		scanTimestamp1 = scanTimestamp2;
		    	else
		    		scanDuration = scanTimestamp2 - scanTimestamp1;
		    	scanTimestamp1 = scanTimestamp2;
		    	synchronized(wifiScanNodeHelper) {
		    		// Collect the wifi scans as ScannedWifiNode's
		    		wifiScanNodeHelper.add(ScannedWifiNode.fromScanResults(mainWifi.getScanResults(), currentLocation, UUID));
		    		scanCount += 1;
		    	}
		    	if(scanCount >= scanLimit) {
		    		// The scan limit was reached
		    		try {
		    			collect = false;
		    			Log.d(TAG, "Receiver scan interval limit reached...");

		    			Log.i(TAG, "4) Unregistering receiver");
	    				try {
	    					service.unregisterReceiver(this);
	    				} catch(IllegalArgumentException e) {
	    					Log.i(TAG, "Wifi registration scan unnecessary.");
	    				}
	    				autoReceiverRegistered = false;

		    			Log.i(TAG, "5) Disable Wifi...");
		    			FutureTask<Boolean> wifiDisableFutureTask = new FutureTask<Boolean>(new EnableWifi());
		    			ExecutorService wifiDisableExecutor = Executors.newFixedThreadPool(1);
		    			wifiDisableExecutor.execute(wifiDisableFutureTask);
		    	        try {
		    				if (wifiDisableFutureTask.get()) {
		    					Log.i(TAG, "Wifi disabled successfully.");
		    				} else {
		    				    Log.i(TAG, "Failed to disable wifi!");
		    				}
		    			} catch (InterruptedException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			} catch (ExecutionException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			}

		    			Log.i(TAG, "6) Start Manet Manager...");
		    			manetManagerObserver.setExpectedState(true);
		    			FutureTask<Boolean> adhocFutureTask = new FutureTask<Boolean>(manetManagerObserver);
		    	        ExecutorService adhocExecutor = Executors.newFixedThreadPool(1);
		    	        adhocExecutor.execute(adhocFutureTask);
		    	        try {
		    				if (adhocFutureTask.get()) {
		    					Log.i(TAG, "Adhoc started successfully.");
		    				} else {
		    				    Log.i(TAG, "Failed to start adhoc mode!");
		    				}
		    			} catch (InterruptedException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			} catch (ExecutionException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			}
					} catch (Exception e) {
					// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			currentLocation = new GeoPoint(location);
		} else {
			Log.d(TAG, "Location changed is null?");
		}
	}
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	private final IBinder mBinder = new CommBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class CommBinder extends Binder {
		public ExampleManetService getService() {
			return ExampleManetService.this;
		}
	}
}
