
package com.team.meeting;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class EditActivity extends Activity {

    private String mOriginal;
    private String mProcessed;
    private String TAG = "EditActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mOriginal = getIntent().getStringExtra("original");
        mProcessed = getIntent().getStringExtra("processed");
        TextView tv = (TextView) findViewById(R.id.server_text);
        tv.setText(getIntent().getStringExtra("processed"));
        Log.d(TAG , mOriginal);
        Log.d(TAG, "processed :" + mProcessed );
    }

    private class PostTextCallback implements SpeechCallback {

        @Override
        public void processString(String string) {

        }

    }
    
    private class StartNlpCallback implements SpeechCallback {

        @Override
        public void processString(String string) {
            
        }
        
    }
}
