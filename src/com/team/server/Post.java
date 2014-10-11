
package com.team.server;

import android.os.AsyncTask;

import org.json.JSONObject;

public class Post extends AsyncTask<String, Void, JSONObject> {

    private String TAG = "Post";
    private ServerCallBack mCallback;

    public Post(ServerCallBack callback) {
        mCallback = callback;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject json = null;

        String url = params[0];
        String jsonDataString = params[1];

        try {
            JSONObject jsonData = new JSONObject(jsonDataString);
            json = ConnectionHelper.postHttpRequest(url, jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    protected void onPostExecute(JSONObject json) {

        if (isCancelled()) {
            return;
        }

        mCallback.onResult(json);
        return;
    }
}
