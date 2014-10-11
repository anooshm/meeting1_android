
package com.team.server;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ConnectionHelper {

    private static DefaultHttpClient createHttpClient() {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        ThreadSafeClientConnManager multiThreadedConnectionManager = new ThreadSafeClientConnManager(
                params, registry);
        DefaultHttpClient httpclient = new DefaultHttpClient(multiThreadedConnectionManager,
                params);
        return httpclient;
    }

    public static JSONObject postHttpRequest(String url, JSONObject jsonData)
            throws UnsupportedEncodingException, IOException, ClientProtocolException {
        DefaultHttpClient client = createHttpClient();
        HttpPost post = new HttpPost(url);
        StringEntity entity = new StringEntity(jsonData.toString());
       // entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        post.setEntity(entity);

        HttpResponse response = client.execute(post);
        return processResponse(response);
    }

    public static JSONObject getHttpRequest(String url, JSONObject jsonData)
            throws UnsupportedEncodingException, IOException, ClientProtocolException {
        // TODO uncomment after server integration
        DefaultHttpClient client = createHttpClient();
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        return processResponse(response);
    }

    private static JSONObject processResponse(HttpResponse response)
            throws IOException {
        HttpEntity httpEntity = response.getEntity();
        InputStream is = httpEntity.getContent();
        String jsonString = convertStreamToString(is);
        JSONObject json = null;
        try {
            json = new JSONObject(jsonString);
        } catch (JSONException je) {
            throw new RuntimeException(je);
        }
        return json;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return sb.toString();
    }

}
