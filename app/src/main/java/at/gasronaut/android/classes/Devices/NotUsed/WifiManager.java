package at.gasronaut.android.classes.Devices.NotUsed;

//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.net.wifi.ScanResult;
//import android.net.wifi.WifiConfiguration;
//import android.os.Build;
//
//import com.android.gastronaut.activity.AbstractActivity;
//import com.android.gastronaut.classes.Async.NotUsed.WifiThread;
//import com.android.gastronaut.classes.Devices.Device;
//
//import java.util.List;
//
//public class WifiManager extends Device {
//    private static Device _instance;
//
//    private String connectedWifiName = "";
//    private String connectedWifiKey = "";
//
//    private List<ScanResult> availableNetworks;
//
//    private WifiManager(Context c) {
//        super(c);
//
//
//        WifiThread wifiThread = WifiThread.getInstance();
//        if (!wifiThread.run) {
//            wifiThread.run = true;
//            wifiThread.start();
//        }
//
//        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
//            // We need also to enable GPS to get the networks into a list!
//            if (!GpsManager.getInstance(AbstractActivity.getContext()).isEnabled()) {
//                GpsManager.getInstance(AbstractActivity.getContext()).enable();
//            }
//        }
//    }
//
//    /**
//     * Get Instance
//     *
//     * @param c
//     * @return Instance
//     */
//    public static WifiManager getInstance(Context c) {
//        if (_instance == null) {
//            _instance = new WifiManager(c);
//        }
//
//        return (WifiManager) _instance;
//    }
//
//    /**
//     * Connect to Wifi Network
//     *
//     * @param wifiName the name of the wifi network
//     * @param wifiKey  the key of the wifi network
//     */
//    public void connectToWifi(String wifiName, String wifiKey) {
//        android.net.wifi.WifiManager wifiManager = getWifiManager();
//        boolean success = false;
//
//        //if (isNetworkAvailable(wifiName)) {
//            if (!wifiName.equals(getActuallyConnectedWifiName().replace("\"", ""))) {
//                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
//                if (list == null) {
//                    success = false;
//                    return;
//                }
//
//                for (WifiConfiguration i : list) {
//                    if (i.SSID != null && i.SSID.equals("\"" + wifiName + "\"")) {
//                        wifiManager.disconnect();
//                        wifiManager.enableNetwork(i.networkId, true);
//                        success = wifiManager.reconnect();
//
//                        break;
//                    }
//                }
//
//                if (!success) {
//                    WifiConfiguration wifiConfig = new WifiConfiguration();
//                    wifiConfig.SSID = String.format("\"%s\"", wifiName);
//                    wifiConfig.preSharedKey = String.format("\"%s\"", wifiKey);
//
//                    int netId = wifiManager.addNetwork(wifiConfig);
//                    wifiManager.disconnect();
//                    wifiManager.enableNetwork(netId, true);
//
//                    success = wifiManager.reconnect();
//                }
//            } else {
//                success = true;
//            }
//        //}
//
//        boolean changed = false;
//
//        //if (success) {
//            if (!connectedWifiName.equals(wifiName) || !connectedWifiKey.equals(wifiKey)) {
//                changed = true;
//            }
//        //}
//        connectedWifiName = wifiName;
//        connectedWifiKey = wifiKey;
//
//        if (changed) {
//            notifySettingsChanged();
//        }
//
//    }
//
//    public void disconnect() {
//        connectedWifiKey = "";
//        connectedWifiName = "";
//
//        android.net.wifi.WifiManager wifiManager = getWifiManager();
//
//        wifiManager.disconnect();
//    }
//
//    /**
//     * Reconnect if connection is lost (or the WiFi Name differs)
//     */
//    public void reconnect() {
//        if (!isConnected() && !connectedWifiKey.equals("") && !connectedWifiName.equals("")) {
//            connectToWifi(connectedWifiName, connectedWifiKey);
//        }
//    }
//
//    public boolean isConnected() {
//        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mWifi = connManager.getActiveNetworkInfo();
//
//        if (mWifi != null) { // connected to the internet
//            if ((mWifi.getType() == ConnectivityManager.TYPE_WIFI) && !connectedWifiKey.equals("") && !connectedWifiName.equals("")) {
//                try {
//                    if (!mWifi.isConnected() || !connectedWifiName.equals(mWifi.getExtraInfo().replace("\"", ""))) {
//                        return false;
//                    } else {
//                        return true;
//                    }
//                } catch(Exception e) {
//
//                }
//            }
//
//            return false;
//        }
//
//        return false;
//    }
//
//    private String getActuallyConnectedWifiName() {
//        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mWifi = connManager.getActiveNetworkInfo();
//
//        if (mWifi != null) { // connected to the internet
//            if ((mWifi.getType() == ConnectivityManager.TYPE_WIFI)) {
//                try {
//                    if (mWifi.isConnected()) {
//                        return mWifi.getExtraInfo().replace("\"", "");
//                    }
//                } catch (Exception e) {
//                }
//            }
//        }
//
//        return "";
//    }
//
//    public boolean isConnected(String wifiName) {
//        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mWifi = connManager.getActiveNetworkInfo();
//
//        if (mWifi != null) { // connected to the internet
//            if ((mWifi.getType() == ConnectivityManager.TYPE_WIFI)) {
//                try {
//                    if (!mWifi.isConnected() || !wifiName.equals(mWifi.getExtraInfo().replace("\"", ""))) {
//                        return false;
//                    } else {
//                        return true;
//                    }
//                } catch(Exception e) {
//                    return true;
//                }
//            }
//
//            return false;
//        }
//
//        return false;
//    }
//
//    public boolean isNetworkAvailable(String name) {
//        if (availableNetworks == null) {
//            return false;
//        }
//
//        for (ScanResult sr : availableNetworks) {
//            if (sr.SSID.equals(name)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    public void scanForNetworks() {
//        android.net.wifi.WifiManager wifiManager = getWifiManager();
//
//        context.registerReceiver(this,
//                new IntentFilter(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//
//        wifiManager.startScan();
//    }
//
//    public void enableWifiListener() {
//        context.registerReceiver(this,
//                new IntentFilter(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION));
//    }
//
//    public android.net.wifi.WifiManager getWifiManager() {
//        return (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//    }
//
//    public void setData(String wifiName, String wifiKey) {
//        connectedWifiName = wifiName;
//        connectedWifiKey = wifiKey;
//    }
//
//    public String getConnectedWifiName() {
//        return connectedWifiName;
//    }
//
//    public String getConnectedWifiKey() {
//        return connectedWifiKey;
//    }
//
//    @Override
//    public void enable() {
//        if (!isEnabled()) {
//            getWifiManager().setWifiEnabled(true);
//        }
//    }
//
//    @Override
//    public void disable() {
//        if (isEnabled()) {
//            WifiThread wifiThread = WifiThread.getInstance();
//            if (!wifiThread.run) {
//                wifiThread.run = true;
//                wifiThread.start();
//            }
//
//            getWifiManager().setWifiEnabled(false);
//        }
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return getWifiManager().isWifiEnabled();
//    }
//
//    public void tryToConnect(String wifiName, String wifiKey) {
//        if (!isEnabled()) {
//            enable();
//        }
//
//        connectToWifi(wifiName, wifiKey);
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals(android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//            availableNetworks = getWifiManager().getScanResults();
//        } else if (intent.getAction().equals(android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION)) {
//
//            int extraWifiState = intent.getIntExtra(android.net.wifi.WifiManager.EXTRA_WIFI_STATE, android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN);
//
//            if (extraWifiState == android.net.wifi.WifiManager.WIFI_STATE_ENABLED) {
//                scanForNetworks();
//            }
//        }
//    }
//}
