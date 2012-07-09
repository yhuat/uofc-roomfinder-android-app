package com.uofc.roomfinder.android.util;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import com.uofc.roomfinder.android.DataModel;
import com.uofc.roomfinder.android.util.tasks.WiFiLocationQuery;

public class WiFiScanReceiver extends BroadcastReceiver {
	private static final String TAG = "WiFiScanReceiver";

	public WiFiScanReceiver() {
		super();
	}

	@Override
	public void onReceive(Context c, Intent intent) {

		List<ScanResult> results = DataModel.getInstance().getWifiManager().getScanResults();
		String macAdresses = "";
		String powerLevels = "";
		String frequencies = "";

		for (ScanResult result : results) {
			// if (result.SSID.contains("airuc") || result.SSID.contains("eduroam") || result.SSID.contains("voiceuc")) {

			// each mac address of the listed access points ends with a "0", every other mac address is just a fake address for other a other SSID
			if (result.BSSID.endsWith("0")) {
				// System.out.println("bssid: " + result.BSSID + ", SSID: " + result.SSID + ", power: " + result.level + ", frequ: " + result.frequency);
				macAdresses += result.BSSID + ",";
				powerLevels += result.level + ",";
				frequencies += result.frequency + ",";
			}
			// }
		}

		// cut off last comma
		if (macAdresses.length() > 0) {
			macAdresses = macAdresses.substring(0, macAdresses.length() - 1);
			powerLevels = powerLevels.substring(0, powerLevels.length() - 1);
			frequencies = frequencies.substring(0, frequencies.length() - 1);
		}

		// start location query
		if (macAdresses.length() > 2) {
			new WiFiLocationQuery().execute(macAdresses, powerLevels, frequencies);
		}
	}

}
