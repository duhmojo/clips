package com.test.manet;

import java.util.HashSet;
import java.util.concurrent.Callable;

import org.span.service.ManetHelper;
import org.span.service.ManetObserver;
import org.span.service.core.ManetService.AdhocStateEnum;
import org.span.service.routing.Node;
import org.span.service.system.ManetConfig;

import android.app.Activity;

public class ManetService implements Callable<Boolean>, ManetObserver {
	public ManetHelper mManetHelper = null;
	public ManetService(ManetHelper manetHelper) {
		this.mManetHelper = manetHelper;
	}

	@Override
	public Boolean call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onAdhocStateUpdated(AdhocStateEnum arg0, String arg1) {
		// TODO Auto-generated method stub

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
