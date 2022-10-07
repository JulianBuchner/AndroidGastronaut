package at.gasronaut.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.gasronaut.android.classes.Async.ServerRequest;
import at.gasronaut.android.classes.Configuration;
import at.gasronaut.android.classes.DeviceAdminReceiver;
import at.gasronaut.android.classes.Devices.BluetoothManager;
import at.gasronaut.android.classes.ForegroundServiceLauncher;
import at.gasronaut.android.classes.SettingsChangedListener;
import at.gasronaut.android.classes.StepCounter.StepDetector;
import at.gasronaut.android.classes.StepCounter.StepListener;
import at.gasronaut.android.classes.Voucher;

/**
 * Created by peter on 04.08.2016.
 */
public class AbstractActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private static final String TAG = "AbstractActivity";

    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));

    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    //private PackageManager mPackageManager;

    protected static Context context;
    protected static AbstractActivity activity;
    protected static boolean startedOnce;

    private static Rect rect;

    public final static int RESULT_CODE_ERROR = 0;
    public final static int RESULT_CODE_OK = 1;

    public final static int REQUEST_CODE_NONE = -1;
    public final static int REQUEST_CODE_OVERLAY = 1;
    public final static int REQUEST_CODE_CAMERA = 2;
    public final static int REQUEST_CODE_FINE_LOCATION = 3;
    public final static int REQUEST_CODE_WIFI = 4;
    public final static int REQUEST_CODE_BLUETOOTH = 5;
    public final static int REQUEST_CODE_ADDITIONAL_INFO = 6;
    public final static int ACCESS_COARSE_LOCATION = 7;
    public static final int REQUEST_CODE_APP_UPDATE = 11;
    public static final int REQUEST_CODE_ENTRY_CODE = 12;
    public static final int REQUEST_CODE_SCAN_TABLE = 13;


    private int displayedMessage = 0;
    private int displayedPrinterMessage = 0;
    private static boolean permissionsChecked = false;

    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private Sensor accel;
    public static int numSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get an instance of the SensorManager
        try {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            simpleStepDetector = new StepDetector();
            simpleStepDetector.registerListener(this);

            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        } catch (Exception e) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Set Default COSU policy
            mAdminComponentName = DeviceAdminReceiver.getComponentName(this);
            mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                    Context.DEVICE_POLICY_SERVICE);
            //mPackageManager = getPackageManager();
            if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                setDefaultCosuPolicies(true);
            }
        }

        context = getApplicationContext();
        activity = this;

        //BluetoothManager.getInstance(context).enableBluetoothListener();

        /*
        WifiManager.getInstance(context).enableWifiListener();
        WifiManager.getInstance(context).addSettingsChangedListener("connect", new SettingsChangedListener() {
            @Override
            public void changePerformed() {
                String wifiKey = WifiManager.getInstance(context).getConnectedWifiKey();
                String wifiName = WifiManager.getInstance(context).getConnectedWifiName();

                if (!Configuration.getInstance().wifiKey.equals(wifiKey) && !Configuration.getInstance().wifiName.equals(wifiName) && WifiManager.getInstance(context).isConnected()) {
                    Configuration.getInstance().wifiKey = wifiKey;
                    Configuration.getInstance().wifiName = wifiName;
                    Configuration.getInstance().saveSettings(true);
                }
            }
        });
        WifiManager.getInstance(context).scanForNetworks();
        */

        // workarround for kiosk - mode!
        //startService(new Intent(getBaseContext(), KioskService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Configuration.getInstance().closeProgress();
    }

    public void showMessageDialog(final int msgId, String subject, String message) {
        if (displayedMessage == 0) {
            displayedMessage = msgId;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            alertDialogBuilder.setTitle(subject);

            TextView text = new TextView(activity);
            text.setText(message);
            alertDialogBuilder.setView(text);

            //alertDialogBuilder.setView(linearLayout);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Gelesen",
                            (dialog, id) -> {
                                dialog.dismiss();
                                String data = "{\"hash\":\"" + Configuration.getInstance().customerHash + "\",\"device_id\":\"" + Settings.Secure.getString(AbstractActivity.getActivity().getContentResolver(),
                                        Settings.Secure.ANDROID_ID) + "\",\"message_id\":\"" + msgId + "\"}";

                                ServerRequest s = new ServerRequest("http://" + Configuration.getInstance().hostUrl, "/ajax/order/confirmmessage", data);
                                s.setRequestMethod(Request.Method.POST);

                                s.makeJsonRequest();

                                displayedMessage = 0;
                            })
                    .setNegativeButton("Abbrechen",
                            (dialog, id) -> {
                                dialog.dismiss();
                                displayedMessage = 0;
                            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(1500, 255));

            //v.vibrate(VibrationEffect.createWaveform(pattern, 3));
        } else {
            //deprecated in API 26
            v.vibrate(1500);
        }
    }

    public void showPrinterErrorDialog(String subject, String message) {
        if (displayedPrinterMessage == 0) {
            displayedPrinterMessage = 1;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            alertDialogBuilder.setTitle(subject);

            TextView text = new TextView(activity);
            text.setText(message);
            alertDialogBuilder.setView(text);

            //alertDialogBuilder.setView(linearLayout);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.dismiss();
                                    displayedPrinterMessage = 0;
                                }
                            })
                    .setNegativeButton("Abbrechen",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.dismiss();
                                    displayedPrinterMessage = 0;
                                }
                            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            vibrate();
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        }
    }

    public static int getAppVersionCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return 0;
        }
    }


    public static Context getContext() {
        return context;
    }

    public static AbstractActivity getActivity() {
        return activity;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save data into memory
        // Order.writeOpenOrders();

        if (Configuration.getInstance().overLay != null) {
            WindowManager manager = ((WindowManager) this.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE));

            try {
                manager.removeViewImmediate(Configuration.getInstance().overLay);
            } catch (Exception e) {
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Order.loadOpenOrders();

        displayedMessage = 0;

        try {
            preventStatusBarExpansion(this);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Configuration.getInstance().requestQueue == null) {
            Configuration.getInstance().requestQueue = Volley.newRequestQueue(this);
        }


        /*
        // Define Async task - Big in Gingerbread and before
        AsyncServerXmlCaller asc = new AsyncServerXmlCaller(new AsyncResponse() {
            @Override
            public void processFinish(Object output) {
                // nothing
            }
        });
        */

        startedOnce = true;

        if (isLockTaskAllowed() && (!isLockTaskRunning())) {
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Set Default COSU policy
                mAdminComponentName = DeviceAdminReceiver.getComponentName(this);
                mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                        Context.DEVICE_POLICY_SERVICE);
                mPackageManager = getPackageManager();
                if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                    setDefaultCosuPolicies(true);
                }
            }*/

            startLockTask();
        }

        Configuration.getInstance().activity = this;
        activity = this;

        /*if (!permissionsChecked) {
            permissionsChecked = true;

            checkDrawOverlayPermission();
        }*/

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                checkCameraPermission();
            }
        }, 5000);

        /*handler.postDelayed(new Runnable() {
            public void run() {
                checkCoarseLocationPermission();
            }
        }, 10000);*/
    }

    public void orderDone(int orderId, final int timestamp, ArrayList<Voucher> vouchers) {
    }

    public void orderDone(int orderId, final int timestamp) {
    }

    public void orderError() {
    }

    public final void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view != null) {
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    public final boolean isLockTaskRunning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // start lock task mode if its not already active
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            // if (mDevicePolicyManager.isLockTaskPermitted(this.getPackageName()) && mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            if ((am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_LOCKED) ||
                    (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_PINNED)) {
                return true;
            }
            // }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

            if (am.isInLockTaskMode()) {
                return true;
            }
        }

        return false;
    }

    public final boolean isLockTaskAllowed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                return true;
            }
            return false;
        } else {
            //return false;
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        }
    }

    public final void showKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();

        if (view != null) {
            inputManager.showSoftInput(view, 0);
        }

    }

    public final boolean isMenuBarAtTop() {
        /*View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        actionBar.hide();*/

        if (this.rect == null || this.rect.top <= 0) {
            this.rect = new Rect();
            this.getWindow().getDecorView().getWindowVisibleDisplayFrame(this.rect);
        }
        //}

        return this.rect.top > 0;
    }

    public final void preventStatusBarExpansion(Context context) {
        // Menu Bar is not at the Bottom
        if (isMenuBarAtTop()) {
            WindowManager manager = ((WindowManager) context.getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE));

            Activity activity = (Activity) context;
            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            localLayoutParams.gravity = Gravity.TOP;
            localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;

            //http://stackoverflow.com/questions/1016896/get-screen-dimensions-in-pixels
            int resId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            int result = 0;
            if (resId > 0) {
                result = activity.getResources().getDimensionPixelSize(resId);
            }

            localLayoutParams.height = result;

            localLayoutParams.format = PixelFormat.TRANSPARENT;

            if (Configuration.getInstance().overLay == null) {
                Configuration.getInstance().overLay = new CustomViewGroup(context);
            }

            manager.addView(Configuration.getInstance().overLay, localLayoutParams);
        }
    }

    public final void checkCoarseLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION);
        } else {
        }
    }


//    public final void checkFineLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Camera permission has not been granted yet. Request it directly.
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_CODE_FINE_LOCATION);
//        } else {
//        }
//    }

    public final void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_CAMERA);
        } else {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            // Log.i(TAG,
            //        "Displaying camera permission rationale to provide additional context.");
            /*
            Snackbar.make(this.getWindow().getDecorView().getRootView(), "Zugriff auf Kamera zulassen!",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(AbstractActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CODE_CAMERA);
                        }
                    })
                    .show();
             */
        }
    }

    private void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {

                /** if not construct intent to request permission */
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                /** request permission via start activity for result */
                startActivityForResult(intent, REQUEST_CODE_OVERLAY);
            }
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == REQUEST_CODE_OVERLAY) {
            /** if so check once again if we have permission */
            if (Settings.canDrawOverlays(AbstractActivity.getContext())) {
                // continue here - permission was granted
                preventStatusBarExpansion(this);
            }
        } else if (requestCode == REQUEST_CODE_APP_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "onActivityResult: app download failed");
            }
        } else if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                if (data.getStringExtra("SCAN_RESULT").compareToIgnoreCase("admin") == 0) {
                    openSettings("");
                } else if (data.getStringExtra("SCAN_RESULT").contains("importpreferences")) {
                    // reset on every new scan the sumUpAllPos - Feature!
                    Configuration.getInstance().sumUpAllPos = false;
                    String prefs = data.getStringExtra("SCAN_RESULT");
                    openSettings(prefs);
                } else if (data.getStringExtra("SCAN_RESULT").contains("connectbluetoothprinter")) {
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertMessageNoGps();
                        return;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            buildAlertMessageLocation();

                            return;
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            buildAlertMessageLocation();

                            return;
                        }
                    }

                    BluetoothManager.getInstance(this).enableBluetoothListener();
                    BluetoothManager.getInstance().addSettingsChangedListener("connect", (SettingsChangedListener) () -> {
                        String mac = BluetoothManager.getInstance().getConnectedPrinterMac();
                        if ((!Configuration.getInstance().blueToothPrint || !Configuration.getInstance().bluetoothMac.equalsIgnoreCase(mac)) && BluetoothManager.getInstance(context).isConnected()) {
                            Configuration.getInstance().bluetoothMac = mac;
                            Configuration.getInstance().blueToothPrint = BluetoothManager.getInstance().isConnected();
                            Configuration.getInstance().saveSettings(false);
                        }
                    });

                    String prefs = data.getStringExtra("SCAN_RESULT");
                    String[] pref = prefs.split(";");

                    // Hier die Verbindung zur gegebenen MAC - Adresse aufbauen
                    /**
                     * create a new bluetooth connection to a printer!
                     */
                    String btMacAddress = pref[1];
                    BluetoothManager bluetooth = BluetoothManager.getInstance();
                    //BluetoothThread.stopThread();

                    ForegroundServiceLauncher.getInstance().stopService(AbstractActivity.getContext());


                    bluetooth.tryToConnect(btMacAddress, true);
//                } else if (data.getStringExtra("SCAN_RESULT").contains("connectwifi")) {
//                    String prefs = data.getStringExtra("SCAN_RESULT");
//                    String[] pref = prefs.split(";");
//
//                    // Hier die Verbindung zum gegebenen Wifi herstellen
//                    /**
//                     * create a new wifi connection!
//                     */
//                    WifiManager wifiManager = WifiManager.getInstance(AbstractActivity.getContext());
//                    try {
//                        wifiManager.tryToConnect(pref[1], pref[2]);
//                    } catch (Exception e) {
//                        System.out.println(e);
//                    }
                } else {
                    System.out.println(data.getStringExtra("SCAN_RESULT"));

                    FragmentManager manager = getFragmentManager();

                    //TODO SettingsDialog dialog = new SettingsDialog();
                    //TODO dialog.show(manager, "Administrationsbereich");
                }
            }
        } else if (requestCode == REQUEST_CODE_ENTRY_CODE) {
            if (resultCode == RESULT_OK) {

                String url = "https://" + Configuration.getInstance().hostUrl + "/ajax/order/entrycode";

                JSONObject obj = new JSONObject();
                try {
                    //obj.put("hash", "asdf");
                    obj.put("hash", Configuration.getInstance().customerHash);
                    obj.put("entryCode", data.getStringExtra("SCAN_RESULT"));

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String color = response.getBoolean("success") ? "#00ff00" : "#ff0000";
                                        int iconId = response.getBoolean("success") ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert;
                                        String heading = response.getBoolean("success") ? "Ticket gültig!" : "Ticket ungültig!";
                                        String message = response.getString("data");
                                        AlertDialog alertDialog = new AlertDialog.Builder(AbstractActivity.this)
                                                .setIcon(iconId)
                                                .setCancelable(false)
                                                .setTitle(Html.fromHtml("<font color='" + color + "'>" + heading + "</font>"))
                                                .setMessage(message)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                    }
                                                })
                                                .show();

                                        ImageView imageView = alertDialog.findViewById(android.R.id.icon);
                                        if (imageView != null) {
                                            if (response.getBoolean("success")) {
                                                imageView.setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
                                            } else {
                                                imageView.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                                            }
                                        }
                                    } catch (JSONException e) {

                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //System.out.println(error);
                                }
                            }) {
                        @Override
                        public Priority getPriority() {
                            return Priority.HIGH;
                        }
                    };

                    Configuration.getInstance().requestQueue.add(jsonObjectRequest);
                } catch (JSONException e) {
                }
            }
        }

        /* else if (requestCode == REQUEST_CODE_FINE_LOCATION) {
            WifiThread.getInstance().run = true;
        }*/
    }

    public void openSettings(String preferences) {
        //TODO Intent intent = new Intent(this, com.android.gastronaut.activity.Settings.class);

        /*if (preferences != "") {
            String[] pref = preferences.split(";");
            if (pref.length > 1) {
                intent.putExtra("host_url", pref[1]);

                if (pref.length > 2) {
                    intent.putExtra("hash", pref[2]);

                    if (pref.length > 3) {
                        intent.putExtra("work_zone", pref[3]);

                        if (pref.length >= 6) {
                            intent.putExtra("wifi_name", pref[4]);
                            intent.putExtra("wifi_key", pref[5]);
                        }
                    }
                }
            }
        }

        startActivity(intent);*/
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the Camera - Work
                    Snackbar.make(this.getWindow().getDecorView().getRootView(), "Zugriff auf Kamera erlaubt!",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        public void run() {
                            checkCameraPermission();
                        }
                    }, 10000);
                }
                return;
            }

            case ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the Camera - Work
                    Snackbar.make(this.getWindow().getDecorView().getRootView(), "Zugriff auf Standort erlaubt!",
                            Snackbar.LENGTH_SHORT).show();
                }/* else {
                    Handler handler = new Handler();

                    handler.postDelayed(new Runnable() {
                        public void run() {
                            checkCoarseLocationPermission();
                        }
                    }, 10000);
                }*/
                return;
            }

            case REQUEST_CODE_BLUETOOTH: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the Camera - Work
                    Snackbar.make(this.getWindow().getDecorView().getRootView(), "Zugriff auf Bluetooth erlaubt!",
                            Snackbar.LENGTH_SHORT).show();
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus) {
            // Close every kind of system dialog
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                //sendBroadcast(closeDialog);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    public class CustomViewGroup extends ViewGroup {
        public CustomViewGroup(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void setDefaultCosuPolicies(boolean active) {
        // set user restrictions
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
        // setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        // setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);

        // disable keyguard and status bar
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);

        // enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);

        // set system update policy
        if (active) {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,
                    SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName,
                    null);
        }

        // set this Activity as a lock task package

        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,
                active ? new String[]{getPackageName()} : new String[]{});

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set Cosu activity as home intent receiver so that it is started
            // on reboot
            mDevicePolicyManager.addPersistentPreferredActivity(
                    mAdminComponentName, intentFilter, new ComponentName(
                            getPackageName(), MainActivity.class.getName()));
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities(
                    mAdminComponentName, getPackageName());
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName,
                    restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName,
                    restriction);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void enableStayOnWhilePluggedIn(boolean enabled) {
        if (enabled) {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
                            | BatteryManager.BATTERY_PLUGGED_USB
                            | BatteryManager.BATTERY_PLUGGED_WIRELESS));
        } else {
            mDevicePolicyManager.setGlobalSetting(
                    mAdminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    "0"
            );
        }
    }

    @Override
    public void startLockTask() {
        if (BuildConfig.DEBUG) {
            super.startLockTask();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isLockTaskAllowed() && (!isLockTaskRunning())) {
            startLockTask();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (isLockTaskAllowed() && (!isLockTaskRunning())) {
            startLockTask();
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
    }

    public static EditText findInput(ViewGroup np) {
        int count = np.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = np.getChildAt(i);
            if (child instanceof ViewGroup) {
                findInput((ViewGroup) child);
            } else if (child instanceof EditText) {
                return (EditText) child;
            }
        }
        return null;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Für das Suchen von umgliegenden Bluetoothgeräten muss GPS aktiviert sein. Willst du es aktivieren?")
                .setCancelable(false)
                .setPositiveButton("JA", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("NEIN", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void buildAlertMessageLocation() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Gastronaut benötigt Standortzugriff um nach umliegenden Bluetooth-Druckern zu suchen und im Fehlerfall die Verbindung neu aufzubauen, selbst wenn die Anwendung im Hintergrund läuft.")
                .setCancelable(false)
                .setPositiveButton("Erlauben", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(AbstractActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(
                                        AbstractActivity.this,
                                        new String[]
                                                {Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.BLUETOOTH_SCAN,
                                                        Manifest.permission.BLUETOOTH_CONNECT
                                                }, 0);
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (ActivityCompat.checkSelfPermission(AbstractActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(
                                        AbstractActivity.this,
                                        new String[]
                                                {
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                                }, 0);
                            }
                        } else {
                            if (ActivityCompat.checkSelfPermission(AbstractActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(
                                        AbstractActivity.this,
                                        new String[]
                                                {
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                }, 0);
                            }
                        }
                    }
                })
                .setNegativeButton("Verbieten", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
