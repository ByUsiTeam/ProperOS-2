package miao.byusi.properos.two.PopUpAssembly;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

public class NetworkUtils {
	
	public static String getLocalIpAddress(Context context) {
		try {
			WifiManager wifiManager = (WifiManager) context.getApplicationContext()
					.getSystemService(Context.WIFI_SERVICE);
			
			if (wifiManager != null) {
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				int ipAddress = wifiInfo.getIpAddress();
				if (ipAddress != 0) {
					return Formatter.formatIpAddress(ipAddress);
				}
			}
			return "127.0.0.1";
		} catch (Exception e) {
			return "127.0.0.1";
		}
	}
}