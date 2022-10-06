package at.gastronaut.android.classes.Async;
/*
import android.os.AsyncTask;

import com.android.gastronaut.classes.Configuration;
import com.android.gastronaut.classes.Order;

import org.json.JSONObject;

public class SendOrderThread extends Thread {
    public boolean run = true;

    @Override
    public void run() {
         while(run) {
            // run through json - order list and send. if it was successfully sent --> remove it!
            String json = "{}";

            Configuration configuration = Configuration.getInstance();

            try {
                for (Long key : configuration.ordersToSend.keySet()) {
                    if (configuration.ordersToSend.get(key) != null && !configuration.ordersToSend.get(key).isSending()) {
                        configuration.ordersToSend.get(key).setSending(true);

                        AsyncServerCaller asc = new AsyncServerCaller(new AsyncResponse() {
                            @Override
                            public void processFinish(Object output) {
                                JSONObject obj = (JSONObject) output;
                                try {
                                    if (obj != null) {
                                        long key = Long.parseLong(obj.getString("offlineSystemIdx"));

                                        if (obj.getBoolean("success")) {
                                            Configuration.getInstance().ordersToSend.remove(key);
                                            Order.writeOpenOrders();
                                        } else {
                                            Configuration.getInstance().ordersToSend.get(key).setSending(false);
                                        }
                                    }
                                } catch (Exception e) {
                                }
                            }
                        });

                        asc.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "http://" + configuration.hostUrl + "/ajax/order/makeorder", configuration.ordersToSend.get(key).json, "1", key.toString());
                    }
                }

                Thread.sleep(5000);
            } catch (Exception e) {
            }
        }
    }
}

*/