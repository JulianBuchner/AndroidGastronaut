package at.gastronaut.android;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;

import at.gastronaut.android.classes.Configuration;
import at.gastronaut.android.databinding.ActivityMainBinding;

public class MainActivity extends AbstractActivity {
    private static final String TAG = "GastronautMainActivity";

    // boolean flag for showing / hiding the back button!
    public boolean rootMenu = true;
    private int selCategoryId = 0;

    private static boolean firstRun = true;

    private float yStart = 0;
    private float yEnd = 0;
    private float xStart = 0;
    private float xEnd = 0;


    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private AppUpdateManager mAppUpdateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);


        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_login, R.id.nav_cashTable, R.id.nav_orderHistory, R.id.nav_printout, R.id.nav_storno, R.id.nav_qrcodescan)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);



        /*
        Button button = (Button)findViewById(R.id.nav_header_shoppingbasket);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main);
                navController.navigateUp();
                navController.navigate(R.id.nav_orderHistory);


            }
        });
        */
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    public void onStop() {
        super.onStop();

        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Configuration.getInstance().mainActivity = this;

        if (Configuration.getInstance().reloadConfig) {
            Configuration.getInstance().closeProgress();
            Configuration.getInstance().showProgress("Bitte warten...", "Bitte warten Sie während die Einstellungen übernommen werden!");
            Configuration.getInstance().loadSettings();
            Configuration.getInstance().loadEntries();
        } else {
            rootMenu = true;
            if (!Configuration.getInstance().offline) {
                parseMenuItem(Configuration.getInstance().menu);
            } else {
                parseMenuItem(null);
                Configuration.getInstance().menu = null;
            }
        }

        mAppUpdateManager = AppUpdateManagerFactory.create(this);
        mAppUpdateManager.registerListener(installStateUpdatedListener);
        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(/*AppUpdateType.FLEXIBLE*/ AppUpdateType.IMMEDIATE)) {

                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, /*AppUpdateType.FLEXIBLE*/ AppUpdateType.IMMEDIATE, MainActivity.this, REQUEST_CODE_APP_UPDATE);

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }

            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                // CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                // popupSnackbarForCompleteUpdate();
            } else {
                Log.e(TAG, "checkForAppUpdateAvailability: something else");
            }
        });
    }

    // muss noch überall geändert werden, um zu löschen
    public void click(View v) {
        FragmentManager manager = getFragmentManager();

        SettingsDialog dialog = new SettingsDialog();
        dialog.show(manager, "Administrationsbereich");
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_APP_UPDATE) {
//            if (resultCode != RESULT_OK) {
//                Log.e(TAG, "onActivityResult: app download failed");
//            }
//        } else if (requestCode == 0) {
//            if (resultCode == RESULT_OK) {
//                if (data.getStringExtra("SCAN_RESULT").compareToIgnoreCase("admin") == 0) {
//                    openSettings("");
//                } else if (data.getStringExtra("SCAN_RESULT").contains("importpreferences")) {
//                    // reset on every new scan the sumUpAllPos - Feature!
//                    Configuration.getInstance().sumUpAllPos = false;
//                    String prefs = data.getStringExtra("SCAN_RESULT");
//                    openSettings(prefs);
//                } else if (data.getStringExtra("SCAN_RESULT").contains("connectbluetoothprinter")) {
//                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                        buildAlertMessageNoGps();
//                        return;
//                    }
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                            buildAlertMessageLocation();
//
//                            return;
//                        }
//                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                            buildAlertMessageLocation();
//
//                            return;
//                        }
//                    }
//
//                    BluetoothManager.getInstance(this).enableBluetoothListener();
//                    BluetoothManager.getInstance().addSettingsChangedListener("connect", new SettingsChangedListener() {
//                        @Override
//                        public void changePerformed() {
//                            String mac = BluetoothManager.getInstance().getConnectedPrinterMac();
//                            if ((!Configuration.getInstance().blueToothPrint || !Configuration.getInstance().bluetoothMac.equalsIgnoreCase(mac)) && BluetoothManager.getInstance(context).isConnected()) {
//                                Configuration.getInstance().bluetoothMac = mac;
//                                Configuration.getInstance().blueToothPrint = BluetoothManager.getInstance().isConnected();
//                                Configuration.getInstance().saveSettings(false);
//                            }
//                        }
//                    });
//
//                    String prefs = data.getStringExtra("SCAN_RESULT");
//                    String[] pref = prefs.split(";");
//
//                    // Hier die Verbindung zur gegebenen MAC - Adresse aufbauen
//                    /**
//                     * create a new bluetooth connection to a printer!
//                     */
//                    String btMacAddress = pref[1];
//                    BluetoothManager bluetooth = BluetoothManager.getInstance();
//                    //BluetoothThread.stopThread();
//
//                    ForegroundServiceLauncher.getInstance().stopService(AbstractActivity.getContext());
//
//
//                    bluetooth.tryToConnect(btMacAddress, true);
////                } else if (data.getStringExtra("SCAN_RESULT").contains("connectwifi")) {
////                    String prefs = data.getStringExtra("SCAN_RESULT");
////                    String[] pref = prefs.split(";");
////
////                    // Hier die Verbindung zum gegebenen Wifi herstellen
////                    /**
////                     * create a new wifi connection!
////                     */
////                    WifiManager wifiManager = WifiManager.getInstance(AbstractActivity.getContext());
////                    try {
////                        wifiManager.tryToConnect(pref[1], pref[2]);
////                    } catch (Exception e) {
////                        System.out.println(e);
////                    }
//                } else {
//                    System.out.println(data.getStringExtra("SCAN_RESULT"));
//
//                    FragmentManager manager = getFragmentManager();
//
//                    SettingsDialog dialog = new SettingsDialog();
//                    dialog.show(manager, "Administrationsbereich");
//                }
//            }
//        } else if (requestCode == RC_ENTRY_CODE) {
//            if (resultCode == RESULT_OK) {
//
//                String url = "https://" + Configuration.getInstance().hostUrl + "/ajax/order/entrycode";
//
//                JSONObject obj = new JSONObject();
//                try {
//                    //obj.put("hash", "asdf");
//                    obj.put("hash", Configuration.getInstance().customerHash);
//                    obj.put("entryCode", data.getStringExtra("SCAN_RESULT"));
//
//                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                            (Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
//                                @Override
//                                public void onResponse(JSONObject response) {
//                                    try {
//                                        String color = response.getBoolean("success") ? "#00ff00" : "#ff0000";
//                                        int iconId = response.getBoolean("success") ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert;
//                                        String heading = response.getBoolean("success") ? "Ticket gültig!" : "Ticket ungültig!";
//                                        String message = response.getString("data");
//                                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
//                                                .setIcon(iconId)
//                                                .setCancelable(false)
//                                                .setTitle(Html.fromHtml("<font color='" + color + "'>" + heading + "</font>"))
//                                                .setMessage(message)
//                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialogInterface, int i) {
//                                                    }
//                                                })
//                                                .show();
//
//                                        ImageView imageView = alertDialog.findViewById(android.R.id.icon);
//                                        if (imageView != null) {
//                                            if (response.getBoolean("success")) {
//                                                imageView.setColorFilter(Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
//                                            } else {
//                                                imageView.setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
//                                            }
//                                        }
//                                    } catch (JSONException e) {
//
//                                    }
//                                }
//                            }, new Response.ErrorListener() {
//
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    //System.out.println(error);
//                                }
//                            }) {
//                        @Override
//                        public Priority getPriority() {
//                            return Priority.HIGH;
//                        }
//                    };
//
//                    Configuration.getInstance().requestQueue.add(jsonObjectRequest);
//                } catch (JSONException e) {
//                }
//            }
//        } else
        if (requestCode == AbstractActivity.REQUEST_CODE_SCAN_TABLE) {
            if (resultCode == RESULT_OK) {
                TableConfig.selectedTableId = Integer.parseInt(data.getStringExtra("SCAN_RESULT").split(";")[1]);
                TableConfig.tableSelected = true;
                TableConfig.lastTableId = TableConfig.selectedTableId;

                TableConfig.dialogOpen = false;
            }
        }
    }

    public void orderButtonClick(View v) {

        Intent intent = new Intent(this, Orders.class);
        startActivity(intent);
    }


    public void parseMenuItem(MenuItem item) {
        TableLayout layout = findViewById(R.id.TableLayout);
        parseMenuItem(item, layout, 3, false);


        TableLayout shortlink = this.findViewById(R.id.shortLinkTable);
        if (rootMenu || Configuration.favoritesList.size() <= 0) {
            shortlink.setVisibility(View.GONE);
        } else {
            shortlink.setVisibility(View.VISIBLE);

            layout = findViewById(R.id.shortLinkTable);

            item = Configuration.favoritesList.get(0);

            parseMenuItem(item, layout, Configuration.favoritesList.size(), true);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void parseMenuItem(MenuItem item, TableLayout layout, int elems, Boolean isShortlink) {
        ImageButton table = (ImageButton) findViewById(R.id.tableButton);
        if (rootMenu && TableConfig.showTableDialog == TableConfig.SELECT_TABLE_FIRST) {
            table.setVisibility(View.VISIBLE);
        } else {
            table.setVisibility(View.INVISIBLE);
        }

        int placeHolderInLine = 0;
        int others = 0;

        try {
            Button btn;
            //TableLayout layout = (TableLayout) findViewById(R.id.TableLayout);
            TableRow row = null;
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            FrameLayout top = (FrameLayout) findViewById(R.id.topMenue);

            /*int x = top.getWidth();
            x = x / 3;*/

            int x = 0;
            if (!isShortlink) {
                x = (size.x - 80) / elems;
            } else {
                x = (size.x) / elems;
            }

            final int width = x;

            layout.removeAllViews();

            ImageButton backBtn = (ImageButton) findViewById(R.id.backButton);

            try {
                if (this.rootMenu) {
                    backBtn.setVisibility(View.INVISIBLE);
                } else {
                    backBtn.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                System.out.println(e);
            }

            int i = 0;
            while ((item != null) || ((item == null) && (i % elems != 0))) {
                if (item == null) {
                    TextView t = generatePlaceholder(width);
                    row.addView(t);
                    ++i;

                    continue;
                }

                if (!item.hidden) {
                    if (i % elems == 0) {
                        row = new TableRow(this);
                        layout.addView(row);
                        others = 0;
                        placeHolderInLine = 0;
                    }

                    if (item.id > 0 || item.categoryId > 0) {
                        ++others;

                        btn = new Button(this);
                        TableRow.LayoutParams tr = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
                        //tr.setMargins(0, 0, 0, 0);
                        //btn.setPadding(7, 0, 7, 0);
                        tr.weight = 1;
                        btn.setLayoutParams(tr);

                        if (item.useColor > 0) {
                            if (item.categoryId > 0 || (item.useColor == 2 && item.categoryId == 0)) {
                                btn.getBackground().setColorFilter(0xFF000000 + Integer.parseInt(item.color, 16), PorterDuff.Mode.SRC_OVER);
                            } else {
                                if (item.useColor == 1) {
                                    if (Configuration.getInstance().categoryList.get(selCategoryId).color.length() > 0) {
                                        btn.getBackground().setColorFilter(0xFF000000 + Integer.parseInt(Configuration.getInstance().categoryList.get(selCategoryId).color, 16), PorterDuff.Mode.SRC_OVER);
                                    }
                                }
                            }
                        }

                        if (!isShortlink) {
                            btn.setMinHeight(width);
                            btn.setHeight(width);
                        }

                        btn.setWidth(width);

                        if (Configuration.getInstance().orderList.get(item.id) != null) {
                            btn.setText(item.name + " (" + Configuration.getInstance().orderList.get(item.id) + ")");
                        } else {
                            if (isShortlink) {
                                btn.setTextSize(12);
                            }
                            btn.setText(item.name);
                        }

                        if (item.categoryId > 0) {
                            btn.setTag("c" + item.categoryId);
                            btn.setId(item.categoryId);
                        } else {
                            btn.setTag("i" + item.id);
                            btn.setId(item.id);
                        }

                        if (item != null && item.categoryId <= 0) {

                            final MenuItem reference = item;
                            btn.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    switch (event.getAction()) {
                                        case MotionEvent.ACTION_DOWN:
                                            yStart = event.getY();
                                            xStart = event.getX();
                                            break;

                                        case MotionEvent.ACTION_UP:
                                            yEnd = event.getY();
                                            xEnd = event.getX();
                                            float deltaX = xStart - xEnd;
                                            float deltaY = yStart - yEnd;
                                            if ((Math.abs(deltaY) >= (width - (0.4 * width))) || (Math.abs(deltaX) >= (width - (0.4 * width)))) {

                                                yStart = 0;
                                                yEnd = 0;
                                                xStart = 0;
                                                xEnd = 0;

                                                if (reference.locked) {
                                                    Toast.makeText(AbstractActivity.getContext(), "Eintrag derzeit gesperrt! \nSperrgrund: " + Configuration.itemsList.get(v.getId()).lockedReason + "\nPreis: " + Configuration.itemsList.get(v.getId()).price + " €", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(AbstractActivity.getContext(), "Preis: " + Configuration.itemsList.get(v.getId()).price + " €", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                            break;
                                    }

                                    return false;
                                }
                            });
                        }

                        if (!item.locked) {
                            btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    showTableDialog(false);

                                    MenuItem item = null;
                                    if (view.getTag().toString().startsWith("c")) {
                                        item = Configuration.getInstance().categoryList.get(view.getId());
                                        if (item != null) {
                                            if (item.categoryId > 0) {
                                                rootMenu = false;
                                                selCategoryId = item.categoryId;
                                                parseMenuItem(item.subItem);

                                                return;
                                            }
                                        }
                                    }

                                    item = Configuration.getInstance().itemsList.get(view.getId());

                                    // save current timestamp
                                    Configuration.getInstance().itemTimestamp = Configuration.getInstance().orderList.isEmpty() ? (int) (System.currentTimeMillis() / 1000L) : Configuration.getInstance().itemTimestamp;

                                    Configuration.getInstance().orderList.put(view.getId(), Configuration.getInstance().orderList.get(view.getId()) == null ? 1 : (Configuration.getInstance().orderList.get(view.getId()) < 99 ? Configuration.getInstance().orderList.get(view.getId()) + 1 : 99));

                                    String caption = ((Button) view).getText().toString();
                                    if (caption.indexOf("(") > 0) {
                                        caption = caption.substring(0, caption.indexOf("(") - 1);
                                    }
                                    ((Button) view).setText(caption + " (" + Configuration.getInstance().orderList.get(view.getId()) + ")");

                                    //((Button) view).setTypeface(Typeface.DEFAULT_BOLD);
                                    //((Button) view).setBackgroundColor(Color.GREEN);
                                }
                            });

                            btn.setOnLongClickListener(new View.OnLongClickListener() {
                                public boolean onLongClick(View v) {
                                    showTableDialog(false);

                                    MenuItem item = null;

                                    if (v.getTag().toString().startsWith("c")) {
                                        item = Configuration.getInstance().categoryList.get(v.getId());
                                        if (item != null) {
                                            item = item.subItem;
                                            if (item != null) {
                                                return false;
                                            }
                                        }
                                    }

                                    item = Configuration.getInstance().itemsList.get(v.getId());

                                    if (Configuration.getInstance().orderList.get(v.getId()) != null && Configuration.getInstance().orderList.get(v.getId()) > 1) {
                                        Configuration.getInstance().orderList.put(v.getId(), Configuration.getInstance().orderList.get(v.getId()) - 1);

                                        String caption = ((Button) v).getText().toString();
                                        if (caption.indexOf("(") > 0) {
                                            caption = caption.substring(0, caption.indexOf("(") - 1);
                                        }
                                        ((Button) v).setText(caption + " (" + Configuration.getInstance().orderList.get(v.getId()) + ")");
                                    } else if (Configuration.getInstance().orderList.get(v.getId()) != null && Configuration.getInstance().orderList.get(v.getId()) == 1) {
                                        Configuration.getInstance().orderList.remove(v.getId());

                                        String caption = ((Button) v).getText().toString();
                                        if (caption.indexOf("(") > 0) {
                                            caption = caption.substring(0, caption.indexOf("(") - 1);
                                        }

                                        ((Button) v).setText(caption);
                                    } else {
                                        final Button selectedButton = (Button) v;

                                        RelativeLayout linearLayout = new RelativeLayout(MainActivity.this);
                                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                        linearLayout.setLayoutParams(params);
                                        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
                                        final EditText et = new EditText(context);
                                        et.setInputType(InputType.TYPE_CLASS_NUMBER);
                                        et.setTextSize(50);
                                        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                                        et.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                                        et.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                        et.setTextColor(Color.WHITE);
                                        et.requestFocus();
                                        linearLayout.addView(et);
                                        alertDialogBuilder.setView(linearLayout);
                                        alertDialogBuilder
                                                .setCancelable(true)
                                                .setPositiveButton("Ok",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                int amount = 0;
                                                                try {
                                                                    amount = Integer.parseInt(et.getText().toString());
                                                                } catch (Exception e) {
                                                                }
                                                                dialog.dismiss();

                                                                if (amount > 0) {
                                                                    Configuration.getInstance().itemTimestamp = Configuration.getInstance().orderList.isEmpty() ? 0 : Configuration.getInstance().itemTimestamp;

                                                                    final int fAmout = amount;
                                                                    Configuration.getInstance().orderList.put(selectedButton.getId(), amount);
                                                                    runOnUiThread(new Runnable() {

                                                                        @Override
                                                                        public void run() {
                                                                            selectedButton.getText().toString();

                                                                            selectedButton.setText(selectedButton.getText().toString() + " (" + Integer.toString(fAmout) + ")");
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        })
                                                .setNegativeButton("Abbrechen",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int i) {
                                                                dialog.dismiss();
                                                            }
                                                        });

                                        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                                        alertDialog.show();
                                        showKeyboard();
                                    }

                                    Configuration.getInstance().itemTimestamp = Configuration.getInstance().orderList.isEmpty() ? 0 : Configuration.getInstance().itemTimestamp;

                                    return true;
                                }
                            });
                        } else {
                            btn.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_OVER);

                            btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    Toast.makeText(AbstractActivity.getContext(), "Eintrag derzeit gesperrt! \nSperrgrund: " + Configuration.itemsList.get(view.getId()).lockedReason + "\nPreis: " + Configuration.itemsList.get(view.getId()).price + " €", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        if (!isShortlink || (isShortlink) && item.categoryId > 0) {
                            row.addView(btn);
                        }
                    } else {
                        if (!isShortlink) {
                            TextView t = generatePlaceholder(width);
                            ++placeHolderInLine;
                            if (placeHolderInLine % elems == 0) {
                                TableRow.LayoutParams tr = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
                                tr.weight = 1;
                                t.setLayoutParams(tr);
                                t.setMinHeight(width);
                                t.setHeight(width);
                            }

                            row.addView(t);
                        } else {
                            --i;
                        }
                    }

                    ++i;
                }
                item = item.nextItem;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private TextView generatePlaceholder(int width) {
        TextView t = new TextView(this);
        TableRow.LayoutParams tr = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
        //tr.setMargins(7, 0, 7, 0);
        tr.weight = 1;
        t.setWidth(width);
        t.setLayoutParams(tr);

        return t;
    }
}