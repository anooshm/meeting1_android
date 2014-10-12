
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

public class FirstActivity extends Activity {

    private Button mNextButton;
    private EditText mEditText;
    private SharedPreferences mPrefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        
        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        
        mPrefs = (SharedPreferences)getSharedPreferences(Constants.PREFS , 0);
        mEditText = (EditText) findViewById(R.id.username);
        mNextButton = (Button) findViewById(R.id.homePageNextButton);
        mNextButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String username = mEditText.getText().toString();
                if (username != null && username.length() > 0) {
                    mPrefs.edit().putString("user", username).commit();
                    Intent intent = new Intent(getApplicationContext(), CreateMeetingActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

}
