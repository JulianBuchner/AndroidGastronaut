package at.gasronaut.android.classes.Async.NotUsed;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import at.gasronaut.android.classes.Async.AsyncResponse;

public class AsyncServerCaller extends AsyncTask<String, Void, JSONObject> {
    public AsyncResponse delegate = null; // Call back interface

    public AsyncServerCaller(AsyncResponse asyncResponse) {
        delegate = asyncResponse; // Assigning call back interfacethrough constructor
    }

    @Override
    protected JSONObject doInBackground(String[] params) {
        Boolean done = false;
        int retryLimit = -1;

        if (params.length > 2) {
            retryLimit = Integer.parseInt(params[2]);
        }

        /*long offlineSystemIdx = -1;
        if (params.length > 3) {
            offlineSystemIdx = Long.parseLong(params[3]);
        }*/

        int retryCnt = 1;
        boolean success = false;
        boolean timeout = false;

        String result = "";
        while (!done) {
            try {
                result = "";

                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);

                try {
                    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                    wr.write("data=" + URLEncoder.encode(params[1], "UTF-8"));
                    wr.flush();
                    wr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                InputStreamReader is = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(is);
                StringBuilder sb = new StringBuilder();
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    result = sb.toString();

                    done = true;
                    success = true;
                } catch (Exception ex) {
                    done = true;
                    success = false;
                } finally {
                    is.close();
                }
            } catch (Exception e) {
                if (retryCnt >= retryLimit) {
                    done = true;
                    success = false;
                    timeout = true;
                } else {
                    ++retryCnt;
                }
            }
        }

        JSONObject obj = null;

        try {
            if (success) {
                obj = new JSONObject(result);
            } else {
                obj = new JSONObject();
                obj.put("success", false);
            }

            obj.put("timeout", timeout);

            /*
            if (offlineSystemIdx > 0) {
                obj.put("offlineSystemIdx", offlineSystemIdx);
            }
            */
        } catch (Exception e) {}

        return obj;
    }

    @Override
    protected void onPostExecute(JSONObject result){
        delegate.processFinish(result);
    }
}