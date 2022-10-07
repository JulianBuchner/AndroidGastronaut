package at.gasronaut.android.classes.Async.NotUsed;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import at.gasronaut.android.classes.Async.AsyncResponse;
import at.gasronaut.android.classes.Configuration;

public class AsyncServerPinger extends AsyncTask<String, Void, JSONObject> {
    public AsyncResponse delegate = null; // Call back interface

    public AsyncServerPinger(AsyncResponse asyncResponse) {
        delegate = asyncResponse; // Assigning call back interfacethrough constructor
    }

    @Override
    protected JSONObject doInBackground(String[] params) {
        ConnectivityManager cm = (ConnectivityManager) Configuration.getInstance().activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        JSONObject obj = new JSONObject();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL(params[0]);   // Change to "http://google.com" for www  test.
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(1 * 1000);          // 1 s.
                urlc.connect();
                if (urlc.getResponseCode() == 200) {        // 200 = "OK" code (http connection is fine).
                    obj.put("success", true);
                } else {
                    obj.put("success", false);
                }
            } catch (MalformedURLException e1) {
                try {
                    obj.put("success", false);
                } catch(Exception e) {}
            } catch (IOException e2) {
                try {
                    obj.put("success", false);
                } catch(Exception e) {}
            } catch(Exception e3) {
                try {
                    obj.put("success", false);
                } catch(Exception e) {}
            }
        }

        return obj;
    }

    @Override
    protected void onPostExecute (JSONObject result){
        delegate.processFinish(result);
    }
}