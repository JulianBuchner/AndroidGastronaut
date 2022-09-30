package at.gasronaut.android.classes.Async.NotUsed;

import android.os.AsyncTask;
import android.os.StrictMode;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import at.gasronaut.android.classes.Async.AsyncResponse;

public class AsyncServerXmlCaller extends AsyncTask<String, Void, Document> {

    public AsyncResponse delegate = null; // Call back interface

    public AsyncServerXmlCaller(AsyncResponse asyncResponse) {
        delegate = asyncResponse; // Assigning call back interfacethrough constructor
    }


    @Override
    protected Document doInBackground(String[] params) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Document document = null;
        //InputStream stream = null;
        try {
            URL url = new URL(params[0]);
            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(5 * 1000);
            urlc.setReadTimeout(5000);

            urlc.connect();

            if (urlc.getResponseCode() == 200) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                //stream = url.openStream();

                InputStreamReader i = new InputStreamReader(urlc.getInputStream());
                BufferedReader reader = new BufferedReader(i);
                //StringBuilder sb = new StringBuilder();
                String line;
                try {
                    String doc = "";
                    while ((line = reader.readLine()) != null) {
                        doc += line;
                    }

                    document = builder.parse(new InputSource(new StringReader(doc)));

                } catch (Exception e) {
                    System.out.println(e);
                } finally {
                    i.close();
                }

                //InputSource is = new InputSource(stream);
                //Document document = builder.parse(is);

            }

        } catch (Exception e) {
            System.out.println(e);
        } finally {
        }

        return document;
    }

    @Override
    protected void onPostExecute(Document d) {
        delegate.processFinish(d);
    }
}
