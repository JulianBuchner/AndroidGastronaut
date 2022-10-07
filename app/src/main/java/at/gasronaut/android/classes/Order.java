package at.gasronaut.android.classes;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import at.gasronaut.android.classes.Async.AsyncResponse;
import at.gasronaut.android.classes.Async.ServerRequest;

public class Order implements Serializable {
    public int orderId;
    public int parentOrderId;
    public HashMap<Integer, Integer> items = new HashMap<>();
    public String additionalInfo = "";
    public String phoneId;
    public int tableId;
    public boolean storno = false;
    public boolean payLater = false;
    public boolean reprint = false;
    public int timestamp = 0;
    public String base64;

    @Override
    public Order clone() {
        try {
            super.clone();
        } catch (Exception e) {
        }

        Order result = new Order();
        result.orderId = this.orderId;
        result.parentOrderId = this.parentOrderId;
        result.storno = this.storno;
        result.phoneId = this.phoneId;
        result.tableId = this.tableId;
        result.additionalInfo = this.additionalInfo;

        result.items = new HashMap<>(this.items);

        return result;
    }

    public static void sendOrder(Order order) {
        TableConfig.lastTableId = order.tableId;

        String json = "";
        if (order.items.size() > 0) {
            for (Integer key : order.items.keySet()) {
                Integer value = order.items.get(key);

                if (value <= 0) {
                    continue;
                }

                if (json.compareTo("") > 0) {
                    json += ",";
                }

                MenuItem i = Configuration.getItem(key);
                if (i instanceof RenamedPosition) {
                    json += "{\"id\":\"" + i.id + "\",\"amount\":\"" + value + "\",\"name_addon\":\"" + ((RenamedPosition) i).addon + "\",\"version\":\"" + ((RenamedPosition) i).version + "\"}";
                } else {
                    json += "{\"id\":\"" + key + "\",\"amount\":\"" + value + "\",\"version\":\"0\"}";
                }
            }

            String addon = "";

            if (order.orderId > 0) {
                addon = "\"order_id\":\"" + order.orderId + "\",";
            }

            if (order.parentOrderId > 0) {
                addon = "\"parent_order_id\":\"" + order.parentOrderId + "\",";
            }

            if (order.base64 != null) {
                addon += "\"base64_info\":\"" + order.base64 + "\",";

            }

            json = "{" + addon + "\"table_id\":\"" + order.tableId + "\"," +
                    "\"item_timestamp\":\"" + Configuration.getInstance().itemTimestamp + "\"," +
                    "\"workzone\":\"" + Configuration.getInstance().workZone + "\"," +
                    "\"additional_info\":\"" + order.additionalInfo.replace("\"", "&quot;") + "\"," +
                    "\"device_id\":\"" + order.phoneId + "\"," +
                    "\"username\":\"" + Configuration.getInstance().userName + "\"," +
                    "\"hash\":\"" + Configuration.getInstance().customerHash + "\"," +
                    "\"data\":[" + json + "]}";

            //if (!Configuration.getInstance().offlineSystem) {
            try {
                    /*AsyncServerCaller asc = new AsyncServerCaller(new AsyncResponse() {
                        @Override
                        public void processFinish(Object output) {
                            JSONObject obj = (JSONObject) output;
                            try {
                                if (obj != null && obj.getBoolean("success")) {
                                    int orderId = obj.getInt("order_id");
                                    int timestamp = obj.getInt("timestamp");
                                    //progress.dismiss();
                                    //closeProgress();
                                    //activity.orderDone(orderId);
                                    Configuration.getInstance().activity.orderDone(orderId, timestamp);
                                } else {
                                    Configuration.getInstance().closeProgress();

                                    if (obj != null && obj.getBoolean("timeout")) {
                                        Configuration.getInstance().activity.orderError();
                                    } else {
                                        // fehlermeldung anzeigen -> irgendetwas ist schief gelaufen...
                                        Configuration.getInstance().activity.orderError();
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    });
                    */

                AsyncResponse respListener = new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {
                        JSONObject obj = (JSONObject) output;
                        try {
                            if (obj != null && obj.getBoolean("success")) {
                                int orderId = obj.getInt("order_id");
                                int timestamp = obj.getInt("timestamp");

                                ArrayList<Voucher> list = null;
                                if (!obj.isNull("voucher")) {
                                    list = new ArrayList<>();

                                    JSONArray vouchers = obj.getJSONArray("voucher");

                                    for(int i = 0; i < vouchers.length(); ++i) {
                                        if (vouchers.getJSONObject(i).getDouble("usedAmount") < vouchers.getJSONObject(i).getDouble("value")) {
                                            Voucher v = new Voucher(vouchers.getJSONObject(i));
                                            list.add(v);
                                        }
                                    }
                                }
                                //progress.dismiss();
                                //closeProgress();
                                //activity.orderDone(orderId);
                                Configuration.getInstance().activity.orderDone(orderId, timestamp, list);
                            } else {
                                Configuration.getInstance().closeProgress();

                                if (obj != null && obj.getBoolean("timeout")) {
                                    Configuration.getInstance().activity.orderError();
                                } else {
                                    // fehlermeldung anzeigen -> irgendetwas ist schief gelaufen...
                                    Configuration.getInstance().activity.orderError();
                                }
                            }
                        } catch (Exception e) {
                            Configuration.getInstance().closeProgress();
                            Configuration.getInstance().activity.orderError();
                        }
                    }
                };

                // show message dlg
                Configuration.getInstance().showProgress("Bitte warten...", "Bitte warten Sie während die Bestellung übertragen wird!");

                if (order.storno) {
                    new ServerRequest("http://" + Configuration.getInstance().hostUrl, "/ajax/order/storno", json, respListener, 1, Request.Priority.IMMEDIATE, Configuration.fallbackServer).makeJsonRequest();
                } else if (order.payLater) {
                    new ServerRequest("http://" + Configuration.getInstance().hostUrl, "/ajax/order/payLater", json, respListener, 1, Request.Priority.IMMEDIATE, Configuration.fallbackServer).makeJsonRequest();
                } else {
                    new ServerRequest("http://" + Configuration.getInstance().hostUrl, "/ajax/order/makeOrder", json, respListener, 1, Request.Priority.IMMEDIATE, Configuration.fallbackServer).makeJsonRequest();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            //} else {
            // Add Json String to list and send in thread!
            //    OfflineSystemOrder obj = new OfflineSystemOrder();
            //    obj.timestamp = System.currentTimeMillis();
            //    obj.json = json;

            //    Configuration.ordersToSend.put(obj.timestamp, obj);

            //    Configuration.getInstance().activity.orderDone();
            //}
        }
    }

    /*
        public static void loadOpenOrders() {
            if (Configuration.getInstance().offlineSystem && (Configuration.ordersToSend.size() <= 0)) {
                // load data from storage
                try {
                    FileInputStream fis = Configuration.getInstance().activity.openFileInput("pending_orders");
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Object o = ois.readObject();
                    if (o != null) {
                        Map<Long, OfflineSystemOrder> tmpList = Collections.synchronizedMap((Map<Long, OfflineSystemOrder>) o);

                        FileOutputStream fos = Configuration.getInstance().activity.openFileOutput("pending_orders", Context.MODE_PRIVATE);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);

                        oos.writeObject(null);
                        oos.close();

                        if (tmpList != null) {
                            for (Map.Entry<Long, OfflineSystemOrder> entry : tmpList.entrySet()) {
                                entry.getValue().setSending(false);
                            }

                            Configuration.ordersToSend = tmpList;
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        }

        public static void writeOpenOrders() {
            if (Configuration.getInstance().offlineSystem) {
                // Save data into memory
                try {
                    FileOutputStream fos = Configuration.getInstance().activity.openFileOutput("pending_orders", Context.MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);

                    oos.writeObject(Configuration.ordersToSend);
                    oos.close();
                } catch (Exception e) {
                }
            }
        }
    */
    public static ArrayList<Integer> sortEntries(HashMap<Integer, Integer> orders) {
        DictionaryComparator bvc = new DictionaryComparator();
        ArrayList sorted_map = new ArrayList<>();
        sorted_map.addAll(orders.keySet());

        Collections.sort(sorted_map, bvc);

        return sorted_map;
    }

}

class DictionaryComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer key1, Integer key2) {
        MenuItem item1 = Configuration.getItem(key1);
        MenuItem item2 = Configuration.getItem(key2);


        if (item1 != null && item2 != null) {
            int result = item1.type.compareToIgnoreCase(item2.type);
            if (result == 0) {
                return item1.sort > item2.sort ? 1 : -1;
            } else if (result > 0) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return 0;
        }
    }
}

