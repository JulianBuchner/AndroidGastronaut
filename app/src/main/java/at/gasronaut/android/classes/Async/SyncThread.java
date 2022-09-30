package at.gasronaut.android.classes.Async;

import android.graphics.Bitmap;
import android.provider.Settings;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import at.gasronaut.android.AbstractActivity;
import at.gasronaut.android.classes.Configuration;
import at.gasronaut.android.classes.Sponsor;
import at.gasronaut.android.classes.TableConfig;


public class SyncThread extends Thread {
    private boolean locked = false;
    private boolean firstRun = true;

    private String stepAddon = "";

    @Override
    public void run() {
        while (true) {
            if (!locked) {
                synchronized (this) {
                    locked = true;
                }

                stepAddon = "";

                if ((Configuration.getInstance().stepTimestamp + 60) <= ((int) (System.currentTimeMillis() / 1000L))) {
                    Configuration.getInstance().stepTimestamp = ((int) (System.currentTimeMillis() / 1000L));
                    stepAddon = ",\"steps\":\"" + AbstractActivity.numSteps + "\"";
                }

                final AsyncResponse asyncResponse = new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {
                        try {
                            JSONObject obj = (JSONObject) output;
                            Configuration configuration = Configuration.getInstance();
                            try {
                                if (obj == null || !obj.getBoolean("success")) {
                                    if (Configuration.settingsJson != null) {
                                        obj = Configuration.settingsJson;
                                    }
                                }

                                if (obj != null && obj.getBoolean("success")) {
                                    int tmp = Integer.parseInt(obj.getString("timestamp"));
                                    boolean offlineSystem = (obj.getInt("offline_system") > 0);
                                    Configuration.getInstance().offlineSystem = offlineSystem;

                                    TableConfig.getInstance().setNumTables(Integer.parseInt(obj.getString("num_tables")));
                                    configuration.showOrderHistory = Integer.parseInt(obj.getString("show_order_history")) > 0;
                                    configuration.showOrderReprint = Integer.parseInt(obj.getString("show_order_reprint")) > 0;
                                    configuration.hideStornoButton = Integer.parseInt(obj.getString("hide_storno_button")) > 0;
                                    configuration.hidePayLaterButton = Integer.parseInt(obj.getString("hide_pay_later_button")) > 0;
                                    configuration.showOrderStorno = Integer.parseInt(obj.getString("show_order_storno")) > 0;
                                    configuration.showTableStorno = Integer.parseInt(obj.getString("show_table_storno")) > 0;
                                    try {
                                        configuration.sendBill = Integer.parseInt(obj.getString("send_bill")) > 0;
                                    } catch (Exception e) {
                                        configuration.sendBill = false;
                                    }

                                    try {
                                        configuration.allowNegativeAmounts = Integer.parseInt(obj.getString("allow_negative_amounts")) > 0;
                                    } catch (Exception e) {
                                        configuration.allowNegativeAmounts = true;
                                    }

                                    try {
                                        configuration.showPaidItems = Integer.parseInt(obj.getString("show_paid_items")) > 0;
                                    } catch (Exception e) {
                                        configuration.showPaidItems = false;
                                    }

                                    /*
                                    try {
                                        configuration.selectTableType = obj.getString("select_table_type");
                                    } catch (Exception e) {
                                        configuration.selectTableType = Configuration.SELECT_TABLE_TYPE_SELECT;
                                    }
                                     */

                                    if (!obj.isNull("show_table_dialog")) {
                                        TableConfig.showTableDialog = Integer.parseInt(obj.getString("show_table_dialog"));
                                    }

                                    if (!obj.isNull("fallback_server") && obj.getString("fallback_server").length() > 0) {
                                        Configuration.fallbackServer = obj.getString("fallback_server");
                                        if (!Configuration.fallbackServer.contains("http://")) {
                                            Configuration.fallbackServer = "http://" + Configuration.fallbackServer;
                                        }
                                    }

                                    if (configuration.offline != (Integer.parseInt(obj.getString("offline")) > 0)) {
                                        configuration.offline = Integer.parseInt(obj.getString("offline")) > 0;
                                        if (configuration.orderList.size() <= 0) {
                                            configuration.loadEntries();
                                        } else {
                                            configuration.reloadConfig = true;
                                        }
                                    }

                                    try {
                                        if (!obj.isNull("offline_printers")) {
                                            JSONArray offlinePrinters = obj.getJSONArray("offline_printers");

                                            String msg = "";

                                            if (offlinePrinters.length() > 0) {
                                                for (int i = 0; i < offlinePrinters.length(); i++) {
                                                    JSONObject printer = offlinePrinters.getJSONObject(i);

                                                    if (Configuration.offlinePrinters.containsKey(printer.getString("name"))) {
                                                        if (System.currentTimeMillis() < (Configuration.offlinePrinters.get(printer.getString("name")) + 1000 * 60 * 2)) {
                                                            continue;
                                                        } else {
                                                            Configuration.offlinePrinters.remove(printer.getString("name"));
                                                        }
                                                    }

                                                    Configuration.offlinePrinters.put(printer.getString("name"), System.currentTimeMillis());

                                                    msg += " - " + printer.getString("name") + ", Typ: " + printer.getString("type") + "\n";
                                                }

                                                if (msg.length() > 0) {
                                                    msg = "Bitte kontaktieren Sie Ihren Systembeauftragten. Folgende(r) Drucker sind derzeit offline: \n\n" + msg;

                                                    msg += "\n\nKontrollieren Sie bitte ob die blaue Status-LED am Mini-PC leuchtet und starten Sie den Mini-PC gegebenenfalls neu.";
                                                    AbstractActivity.getActivity().showPrinterErrorDialog("ACHTUNG: Drucker offline", msg);
                                                }
                                            } else {
                                                Configuration.offlinePrinters.clear();
                                            }
                                        }
                                    } catch (Exception e) {
                                    }

                                    if (!obj.isNull("message_id") && Integer.parseInt(obj.getString("message_id")) > 0) {
                                        AbstractActivity.getActivity().showMessageDialog(Integer.parseInt(obj.getString("message_id")), obj.getString("subject"), obj.getString("message"));
                                    }

                                    if (!obj.isNull("steps_updated") && (Integer.parseInt(obj.getString("steps_updated")) > 0)) {
                                        AbstractActivity.numSteps = AbstractActivity.numSteps - Integer.parseInt(obj.getString("steps_updated"));
                                        if (AbstractActivity.numSteps < 0) {
                                            AbstractActivity.numSteps = 0;
                                        }
                                    }

                                    if (Configuration.timestamp == 0 || Configuration.timestamp < tmp) {
                                        if (!obj.isNull("sponsors")) {
                                            JSONObject sponsors = obj.getJSONObject("sponsors");
                                            JSONArray arr = sponsors.getJSONArray("sponsors");

                                            AsyncResponse ar = new AsyncResponse() {
                                                @Override
                                                public void processFinish(Object output) {
                                                    Sponsor s = (Sponsor) output;
                                                    if (s.getBitmap() != null) {
                                                        if (Configuration.sponsorHashMap.containsKey(s.getId())) {
                                                            Bitmap tmp = s.getBitmap();

                                                            if (tmp != null) {
                                                                Configuration.sponsorHashMap.get(s.getId()).setBitmap(tmp);
                                                            }
                                                        } else {
                                                            Configuration.sponsorHashMap.put(s.getId(), s);
                                                        }

                                                        /*
                                                        ImageView image = new ImageView(AbstractActivity.getActivity());
                                                        image.setImageBitmap(Configuration.sponsorHashMap.get(s.getFilename()).getBitmap());

                                                        AlertDialog.Builder builder =
                                                                new AlertDialog.Builder(AbstractActivity.getActivity()).
                                                                        setMessage(Configuration.sponsorHashMap.get(s.getFilename()).getText()).
                                                                        setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                dialog.dismiss();
                                                                            }
                                                                        }).
                                                                        setView(image);
                                                        builder.create().show();*/
                                                    }
                                                }
                                            };

                                            Set<Integer> remove = new HashSet<>();
                                            remove.addAll(Configuration.sponsorHashMap.keySet());


                                            for (int i = 0; i < arr.length(); ++i) {
                                                JSONObject img = arr.getJSONObject(i);

                                                if (!Configuration.sponsorHashMap.containsKey(img.getInt("id"))) {
                                                    remove.remove(img.getInt("id"));

                                                    Sponsor s = new Sponsor(img.getInt("id"), img.getString("filename"), null, img.getString("text"));
                                                    Configuration.sponsorHashMap.put(s.getId(), s);


                                                    if (!img.isNull("filename")) {
                                                        ServerRequest.downloadImage(s.getId(), "http://" + Configuration.getInstance().hostUrl, sponsors.getString("path"), img.getString("filename"), ar);
                                                    }
                                                }
                                            }

                                            for (Integer i : remove) {
                                                Configuration.sponsorHashMap.remove(i);
                                            }
                                        }
                                    }

                                    /**
                                     * Save Settings
                                     */
                                    if (Configuration.timestamp == 0 || Configuration.timestamp < tmp) {
                                        Configuration.saveSettingsJson(obj);

                                        Configuration.timestamp = tmp;

                                        if (configuration.orderList.size() <= 0) {
                                            configuration.loadEntries();
                                        } else {
                                            configuration.reloadConfig = true;
                                        }
                                    }
                                }

                            } catch (Exception e) {
                            }
                        } finally {
                            synchronized (this) {
                                locked = false;
                            }
                        }
                    }
                };

                String data = "{\"hash\":\"" + Configuration.getInstance().customerHash + "\",\"device_id\":\"" + Settings.Secure.getString(AbstractActivity.getActivity().getContext().getContentResolver(), Settings.Secure.ANDROID_ID) + "\"" + stepAddon + ",\"username\":\"" + Configuration.getInstance().userName + "\"}";

                ServerRequest request = new ServerRequest("http://" + Configuration.getInstance().hostUrl, "/ajax/order/timestamp", data, asyncResponse);

                if (firstRun) {
                    firstRun = false;
                    request.setPriority(Request.Priority.IMMEDIATE);
                }

                request.makeJsonRequest();

            }

            try {
                Thread.sleep(10000);
            } catch (Exception e) {

            }
        }

/*
        try {
            AsyncServerCaller asc = new AsyncServerCaller(new AsyncResponse() {
                @Override
                public void processFinish(Object output) {
                    JSONObject obj = (JSONObject) output;
                    Configuration configuration = Configuration.getInstance();
                    try {
                        if (obj != null && obj.getBoolean("success")) {
                            int tmp = Integer.parseInt(obj.getString("timestamp"));
                            boolean offlineSystem = (obj.getInt("offline_system") > 0);
                            Configuration.getInstance().offlineSystem = offlineSystem;

                            if ((offlineSystem && (configuration.sot == null)) || (offlineSystem && !configuration.sot.run)) {
                                System.out.println("offlinesystem: " + offlineSystem);
                                if (configuration.sot == null || !configuration.sot.run) {
                                    Order.loadOpenOrders();

                                    configuration.sot = new SendOrderThread();
                                    configuration.sot.start();
                                }
                            } else if (!configuration.offlineSystem && configuration.sot != null) {
                                configuration.sot.run = false;
                            }

                            configuration.tables.setNumTables(Integer.parseInt(obj.getString("num_tables")));
                            configuration.showOrderHistory = Integer.parseInt(obj.getString("show_order_history")) > 0;
                            configuration.showOrderReprint = Integer.parseInt(obj.getString("show_order_reprint")) > 0;
                            configuration.showOrderStorno = Integer.parseInt(obj.getString("show_order_storno")) > 0;

                            if (configuration.offline != (Integer.parseInt(obj.getString("offline")) > 0)) {
                                configuration.offline = Integer.parseInt(obj.getString("offline")) > 0;
                                if (configuration.orderList.size() <= 0) {
                                    configuration.loadEntries();
                                } else {
                                    configuration.reloadConfig = true;
                                }
                            }

                            if (Configuration.timestamp == 0) {
                                Configuration.timestamp = tmp;
                            } else {
                                if (Configuration.timestamp < tmp) {
                                    Configuration.timestamp = tmp;

                                    if (configuration.orderList.size() <= 0) {
                                        configuration.loadEntries();
                                    } else {
                                        configuration.reloadConfig = true;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                    } finally {
                        run = false;
                    }
                }
            });

            asc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "http://" + Configuration.getInstance().hostUrl + "/ajax/order/timestamp", json, "1");
        } catch (Exception e) {
        }*/
    }
}
