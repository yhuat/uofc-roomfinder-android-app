package com.uofc.roomfinder.android.util.threads;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.RoomFinderApplication;
import com.uofc.roomfinder.android.util.WiFiScanReceiver;

/**
 * this thread invokes every couple of seconds the WifiScanner which invokes the indoor location processing with RSSI and trilateration
 * 
 * @author benjaminlautenschlaeger
 * 
 */
public class WifiLocationProvider implements Runnable {

	boolean running = true;
	private WifiManager wifi;
	BroadcastReceiver receiver;

	@Override
	public void run() {
		// enable Wifi receiver
		receiver = new WiFiScanReceiver();
		RoomFinderApplication.getAppContext().registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		
		while (running) {
			try {
				System.out.println("still running");
				DataModel.getInstance().getWifiManager().startScan();
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
