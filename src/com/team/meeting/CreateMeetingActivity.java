
package com.team.meeting;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.team.server.Post;
import com.team.server.ServerCallBack;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateMeetingActivity extends Activity {

    private Button mCreateMeetingButton;
    private EditText mCreateMeetingText;
    private Button mNextButton;
    private SharedPreferences mPrefs;
    private boolean mIsOwner = false;
    private Button mViewProfileButton;
    private String mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        setContentView(R.layout.activity_create_meeting);
        mPrefs = (SharedPreferences) getSharedPreferences(Constants.PREFS, 0);
        mPrefs.edit().putBoolean(Constants.IS_OWNER, false).commit();
        mUser = mPrefs.getString("user", "Anoosh");
        mCreateMeetingButton = (Button) findViewById(R.id.create_meeting_button);
        mCreateMeetingText = (EditText) findViewById(R.id.create_meeting_text);
        mNextButton = (Button) findViewById(R.id.next_button);
        mViewProfileButton = (Button) findViewById(R.id.view_profile);
        mViewProfileButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName("com.salesforce.chatter", "com.salesforce.chatter.Chatter");
                startActivity(intent);
            }
        });

        mCreateMeetingButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constants.NAME, "Sample");
                    json.put(Constants.DESC, "first MEeting");
                    json.put(Constants.OWNER, mUser);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String params[] = new String[2];
                params[0] = Constants.BASE_URL + Constants.CM;
                params[1] = json.toString();
                new Post(new CreateMeetingCallback()).execute(params);

            }
        });

        mNextButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String meetingId = mCreateMeetingText.getText().toString();
                if (meetingId != null && meetingId.length() > 0) {
                    mPrefs.edit().putString(Constants.MID, meetingId).commit();
                }
                if (!mIsOwner) {
                    mPrefs.edit().putBoolean(Constants.IS_OWNER, false).commit();
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private class CreateMeetingCallback implements ServerCallBack {

        @Override
        public void onResult(JSONObject json) {
            if (json != null) {
                String meetingId = "";
                try {
                    meetingId = json.getString(Constants.MID);
                    mIsOwner = true;
                    if (meetingId != null && meetingId.length() > 0) {
                        mPrefs.edit().putString(Constants.MID, meetingId).commit();
                        mPrefs.edit().putBoolean(Constants.IS_OWNER, true).commit();
                        mCreateMeetingText.setVisibility(View.VISIBLE);
                        mCreateMeetingText.setText(meetingId);
                        mNextButton.setVisibility(View.VISIBLE);
                        mCreateMeetingButton.setEnabled(false);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        }

    }
}
