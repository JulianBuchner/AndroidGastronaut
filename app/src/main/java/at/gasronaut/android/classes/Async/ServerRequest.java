package at.gasronaut.android.classes.Async;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import at.gasronaut.android.classes.Configuration;
import at.gasronaut.android.classes.Sponsor;

public class ServerRequest {
    public static final int REQUEST_TIMEOUT = 5000;

    private AsyncResponse listener = null;
    private String host;
    private String url;
    private String fallbackServer = "";
    private String data;
    private int maxRetries;
    private Request.Priority priority;
    private int method = Request.Method.POST;

    boolean done = false;

    public ServerRequest(String host, String url, String data) {
        this(host, url, data, null);
    }

    public ServerRequest(String host, String url, String data, AsyncResponse response) {
        this(host, url, data, response, 1);
    }

    public ServerRequest(String host, String url, String data, AsyncResponse response, int retries) {
        this(host, url, data, response, retries, Request.Priority.NORMAL);
    }

    public ServerRequest(String host, String url, String data, AsyncResponse response, int retries, Request.Priority priority) {
        this(host, url, data, response, retries, priority, "");
    }

    public ServerRequest(String host, String url, String data, AsyncResponse response, int retries, Request.Priority priority, String fallbackServer) {
        for (int i = 0; i < Configuration.SSL_HOSTS.length; ++i) {
            if (host.contains(Configuration.SSL_HOSTS[i])) {
                host = host.replace("http", "https");

                HttpsTrustManager.allowAllSSL();

                break;
            }
        }

        for (int i = 0; i < Configuration.SSL_HOSTS.length; ++i) {
            if (fallbackServer.contains(Configuration.SSL_HOSTS[i])) {
                fallbackServer = fallbackServer.replace("http", "https");

                HttpsTrustManager.allowAllSSL();

                break;
            }
        }

        this.host = host;
        this.url = url;
        this.data = data;
        this.listener = response;
        this.maxRetries = retries;
        this.priority = priority;
        this.fallbackServer = fallbackServer;
    }

    public void setRequestMethod(int method) {
        this.method = method;
    }

    public void setPriority(Request.Priority p) {
        this.priority = p;
    }

    public void makeJsonRequest() {
        StringRequest postRequest = new StringRequest(method, host + url,
                response -> {
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(response);
                        obj.put("timeout", false);

                        done = true;
                    } catch (Exception e) {
                        done = false;
                    }

                    try {
                        if (!done) {
                            obj = new JSONObject();
                            obj.put("success", false);
                            obj.put("timeout", false);
                        }

                        if (listener != null) {
                            listener.processFinish(obj);
                        }
                    } catch (Exception e) {
                    }
                },
                error -> {
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("success", false);

                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            if (fallbackServer.length() > 0) {
                                ServerRequest fallback = new ServerRequest(fallbackServer, url, data, listener, maxRetries, priority);
                                fallback.makeJsonRequest();
                                return;
                            }

                            obj.put("timeout", true);
                        } else {
                            obj.put("timeout", false);
                        }

                        if (listener != null) {
                            listener.processFinish(obj);
                        }
                    } catch (Exception e) {
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("data", data);
                return params;
            }

            @Override
            public Priority getPriority() {
                return priority;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT,
                this.maxRetries,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        Configuration.getInstance().requestQueue.add(postRequest);
    }

    public void makeStringRequest() {
        StringRequest request = new StringRequest(method, host + url,
                response -> {
                    /*try {
                        response = new String(response.getBytes("ISO-8859-1"), "UTF-8");
                    } catch (Exception e) {
                    }*/

                    Document document = null;
                    try {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        document = builder.parse(new InputSource(new StringReader(response)));
                    } catch (Exception e) {
                    }

                    if (listener != null) {
                        listener.processFinish(document);
                    }
                },
                error -> {
                    if (listener != null) {
                        listener.processFinish(null);
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("data", data);
                return params;
            }

            @Override
            public Priority getPriority() {
                return priority;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT,
                this.maxRetries,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        Configuration.getInstance().requestQueue.add(request);
    }

    public void ping() {
        StringRequest request = new StringRequest(Request.Method.GET, host + url, response -> {
        }, error -> {
        }) {
            @Override
            public Priority getPriority() {
                return priority;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                JSONObject obj = new JSONObject();

                try {
                    if (response.statusCode == HttpURLConnection.HTTP_OK) {
                        obj.put("success", true);
                    } else {
                        obj.put("success", false);
                    }
                } catch (Exception e) {
                }

                if (listener != null) {
                    listener.processFinish(obj);
                }

                return super.parseNetworkResponse(response);
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT,
                this.maxRetries,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        Configuration.getInstance().requestQueue.add(request);
    }


    public static void downloadImage(final int id, String host, String imageurl, final String filename, final AsyncResponse response) {
        for (int i = 0; i < Configuration.SSL_HOSTS.length; ++i) {
            if (host.contains(Configuration.SSL_HOSTS[i])) {
                host = host.replace("http", "https");

                HttpsTrustManager.allowAllSSL();

                break;
            }
        }

        String url = host + imageurl + filename;

        ImageRequest request = new ImageRequest(url,
                bitmap -> {
                    Sponsor s = new Sponsor(id, filename, bitmap);
                    response.processFinish(s);
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                error -> {
                });

        request.setRetryPolicy(new DefaultRetryPolicy(REQUEST_TIMEOUT,
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        Configuration.getInstance().requestQueue.add(request);
    }
}
