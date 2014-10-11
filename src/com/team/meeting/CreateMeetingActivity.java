
package com.team.meeting;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.team.server.Post;
import com.team.server.ServerCallBack;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateMeetingActivity extends Activity {

    private Button mCreateMeetingButton;
    private TextView mCreateMeetingText;
    private Button mNextButton;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        mPrefs = (SharedPreferences)getSharedPreferences(Constants.PREFS , 0);
        mCreateMeetingButton = (Button) findViewById(R.id.create_meeting_button);
        mCreateMeetingText = (TextView) findViewById(R.id.create_meeting_text);
        mNextButton = (Button) findViewById(R.id.next_button);

        mCreateMeetingButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    json.put(Constants.NAME, "Sample");
                    json.put(Constants.DESC, "first MEeting");
                    json.put(Constants.OWNER, "Anoosh");
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
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
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
                    if (meetingId != null && meetingId.length() > 0) {
                        mPrefs.edit().putString(Constants.MID, meetingId);
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
