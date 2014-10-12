
package com.team.meeting;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.team.server.Get;
import com.team.server.Post;
import com.team.server.ServerCallBack;

import org.json.JSONException;
import org.json.JSONObject;

public class EditActivity extends Activity {

    private String mOriginal;
    private String mProcessed;
    private String TAG = "EditActivity";
    private SharedPreferences mPrefs;
    private boolean mIsListening;
    private String mMeetingId;
    private EditText mMessageContent;
    private boolean mIsOwner;
    private LinearLayout mParticipantLayout;
    private LinearLayout mResultLayout;
    private String mUser;
    private TextView mKeywordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        setContentView(R.layout.activity_edit);
        mPrefs = (SharedPreferences) getSharedPreferences(Constants.PREFS, 0);
        mUser = mPrefs.getString("user", "Anoosh");
        mMeetingId = mPrefs.getString("mid", null);
        mIsOwner = mPrefs.getBoolean(Constants.IS_OWNER, false);
        Log.d(TAG, "EditActivity" + mMeetingId);
        mOriginal = getIntent().getStringExtra("original");
        mProcessed = getIntent().getStringExtra("processed");
        mMessageContent = (EditText) findViewById(R.id.meeting_content_edittext);
        Button button = (Button) findViewById(R.id.share_button);
        Button emailShareButton = (Button) findViewById(R.id.edit_button);
        mParticipantLayout = (LinearLayout) findViewById(R.id.participant_layout);
        mResultLayout = (LinearLayout) findViewById(R.id.results_layout);
        mKeywordText = (TextView) findViewById(R.id.keyword_text);
        if (mIsOwner) {
            mResultLayout.setVisibility(View.VISIBLE);
            mParticipantLayout.setVisibility(View.GONE);
        } else {
            mResultLayout.setVisibility(View.GONE);
            mParticipantLayout.setVisibility(View.VISIBLE);
        }
        emailShareButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, mMessageContent.getText().toString());

                startActivity(Intent.createChooser(sharingIntent, "Share Using"));

            }
        });
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    String content = mMessageContent.getText().toString();
                    String correctedNotes = "";
                    String split[] = content.split("\\.");
                    for (String string : split) {
                        Log.d(TAG, string);
                        if (!isNumeric(string)) {
                            Log.d(TAG, string);
                            correctedNotes = addToSentence(string, correctedNotes, ":::");
                        }

                    }
                    correctedNotes = correctedNotes.replace("\n", "").replace(",", "");
                    json.put(Constants.MID, mMeetingId);
                    json.put(Constants.CORRECT_NOTES, correctedNotes);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Log.d(TAG, "json" + json.toString());
                String params[] = new String[2];
                params[0] = Constants.CORRECT_MEETING_MINUTES_URL;
                params[1] = json.toString();
                new Post(new CorrectedMmCallback()).execute(params);
                // Intent
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setClassName("com.salesforce.chatter",
                        "com.salesforce.chatter.SendIntentHandler");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Meeting Notes: " + "\n\n"
                        + mMessageContent.getText().toString());
                
                startActivity(sharingIntent);
            }
        });
        mMessageContent.setText("");
        mMessageContent.setText(postProcess(getIntent().getStringExtra("processed")));
        Log.d(TAG, "processed :" + mProcessed);
        sendText();
    }

    public boolean isNumeric(String str)
    {
        try
        {
            Integer d = Integer.parseInt(str);
        } catch (NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private String postProcess(String stringExtra) {
        String split[] = stringExtra.split(":::");
        int i = 1;
        String sentence = "";
        for (String string : split) {
            if (string != null && string.length() > 0 && !string.equals(" ")) {
                sentence = addToSentence(i + ". " + string, sentence, "\n");
                i = i + 1;
            }
        }
        sentence = sentence + "\n" + "\n";
        return sentence;
    }

    private String addToSentence(String string, String sentence1, String operator) {
        if (!(string != null && string.length() > 0)) {
            return sentence1;
        }
        if (sentence1 != null && sentence1.length() > 0) {
            sentence1 = sentence1 + operator + string;
        } else {
            sentence1 = string;
        }
        return sentence1;
    }

    private void sendText() {
        JSONObject json = new JSONObject();
        try {
            json.put(Constants.MID, mMeetingId);
            json.put(Constants.USER, mUser);
            json.put(Constants.NOTES, mProcessed);
            String params[] = new String[2];
            params[0] = Constants.SAVE_URL;
            params[1] = json.toString();
            Log.d(TAG, json.toString());
            new Post(new PostTextCallback()).execute(params);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class PostTextCallback implements ServerCallBack {

        @Override
        public void onResult(JSONObject json) {
            if (json != null && !json.toString().contains("error")) {
                if (!mIsOwner) {
                    return;
                }
                Log.d(TAG, "PostTextCallback - " + json);
                JSONObject finishJson = new JSONObject();
                try {
                    finishJson.put(Constants.MID, mMeetingId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String params[] = new String[2];
                params[0] = Constants.FINISH_URL;
                params[1] = finishJson.toString();
                new Post(new StartNlpCallback()).execute(params);
            }
        }

    }

    private class StartNlpCallback implements ServerCallBack {

        @Override
        public void onResult(JSONObject json) {
            if (json != null && !json.toString().contains("error")) {
                Log.d(TAG, "PostTextCallback -startNlp " + json);
                JSONObject getMmJson = new JSONObject();
                try {
                    getMmJson.put(Constants.MID, mMeetingId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String params[] = new String[2];
                params[0] = Constants.GET_MEETING_MINUTES_URL + "?mid=" + mMeetingId;
                params[1] = getMmJson.toString();
                new Get(new GetMmCallback()).execute(params);
            }
        }
    }

    private class GetMmCallback implements ServerCallBack {

        @Override
        public void onResult(JSONObject json) {
            if (json != null && json.toString().contains("success")) {

                try {
                    String learned = json.getString(Constants.LEARNED_MINUTES);
                    String token = json.getString("token");
                    String output = postProcess(learned + " ,");
                    mKeywordText.setText("Keywords : " + "\n" + token);
                    mMessageContent.setText(output);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject getMmJson = new JSONObject();
                        try {
                            getMmJson.put(Constants.MID, mMeetingId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String params[] = new String[2];
                        params[0] = Constants.GET_MEETING_MINUTES_URL + "?mid=" + mMeetingId;
                        params[1] = getMmJson.toString();

                        new Get(new GetMmCallback()).execute(params);
                    }
                }, 5000);
            }
        }
    }

    private class CorrectedMmCallback implements ServerCallBack {

        @Override
        public void onResult(JSONObject json) {
            if (json != null) {
                Log.d(TAG, json.toString());
            }
        }
    }
}
