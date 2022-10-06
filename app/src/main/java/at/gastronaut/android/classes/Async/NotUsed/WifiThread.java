package at.gastronaut.android.classes.Async.NotUsed;

//import android.os.Build;
//import android.os.Looper;
//
//import com.android.gastronaut.activity.AbstractActivity;
//import com.android.gastronaut.classes.Devices.NotUsed.GpsManager;
//import com.android.gastronaut.classes.Devices.NotUsed.WifiManager;
//
///**
// * Created by peter on 01.07.2016.
// */
//public class WifiThread extends Thread {
//    public boolean run = false;
//
//    private static WifiThread _instance;
//
//    public final static synchronized WifiThread getInstance() {
//        if (_instance == null) {
//            _instance = new WifiThread();
//        }
//
//        return _instance;
//    }
//
//    @Override
//    public void run() {
//        Looper.prepare();
//
//        while (run) {
//            String wifiName = WifiManager.getInstance(AbstractActivity.getContext()).getConnectedWifiName();
//            String wifiKey  = WifiManager.getInstance(AbstractActivity.getContext()).getConnectedWifiKey();
//
//            if (!wifiKey.equals("")  && !wifiName.equals("")) {
//                if (!WifiManager.getInstance(AbstractActivity.getContext()).isEnabled()) {
//                    WifiManager.getInstance(AbstractActivity.getContext()).enable();
//                }
//
//                if (!WifiManager.getInstance(AbstractActivity.getContext()).isConnected(wifiName)) {
////                    WifiManager.getInstance(AbstractActivity.getContext()).tryToConnect(wifiName, wifiKey);
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        // We need also to enable GPS to get the networks into a list!
//                        if (!GpsManager.getInstance(AbstractActivity.getContext()).isEnabled()) {
//                            run = false;
//                            GpsManager.getInstance(AbstractActivity.getContext()).displayLocationSettingsRequest();
//                            //GpsManager.getInstance(AbstractActivity.getContext()).enable();
//                        } else {
//                            if (WifiManager.getInstance(AbstractActivity.getContext()).isNetworkAvailable(wifiName)) {
//                                if (!WifiManager.getInstance(AbstractActivity.getContext()).isConnected(wifiName)) {
//                                    WifiManager.getInstance(AbstractActivity.getContext()).connectToWifi(wifiName, wifiKey);
//                                } else {
//                                    WifiManager.getInstance(AbstractActivity.getContext()).setData(wifiName, wifiKey);
//                                }
//                            } else {
//                                WifiManager.getInstance(AbstractActivity.getContext()).scanForNetworks();
//                            }
//                        }
//                    } else {
//                        if (WifiManager.getInstance(AbstractActivity.getContext()).isNetworkAvailable(wifiName)) {
//                            if (!WifiManager.getInstance(AbstractActivity.getContext()).isConnected(wifiName)) {
//                                WifiManager.getInstance(AbstractActivity.getContext()).connectToWifi(wifiName, wifiKey);
//                            } else {
//                                WifiManager.getInstance(AbstractActivity.getContext()).setData(wifiName, wifiKey);
//                            }
//                        } else {
//                            WifiManager.getInstance(AbstractActivity.getContext()).scanForNetworks();
//                        }
//                    }
//                }
//            }
//
//            try {
//                Thread.sleep(15000);
//            } catch (InterruptedException e) {}
//            //Looper.loop();
//        }
//
//        Looper.myLooper().quit();
//    }
//}