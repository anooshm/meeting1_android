
package com.team.meeting;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private Button mStartListeningButton;
    private Button mStopListeningButton;
    private LinearLayout mResulsLayout;
    private SharedPreferences mPrefs;
    private boolean mIsListening;
    protected String TAG = "SpeechService";
    private int mBindFlag;
    private Messenger mServiceMessenger;
    private Context activityContext;
    private String mString = "";
    private WakeLock mWakeLock;
    private String mMeetingId;
    private boolean mIsOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        actionBar.setCustomView(R.layout.actionbar);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        setContentView(R.layout.activity_main);
        mPrefs = (SharedPreferences) getSharedPreferences(Constants.PREFS, 0);
        mIsListening = mPrefs.getBoolean(Constants.LISTENING, false);
        mIsOwner = mPrefs.getBoolean(Constants.IS_OWNER, false);
        mMeetingId = mPrefs.getString("mid", null);
        mStartListeningButton = (Button) findViewById(R.id.start_processing);
        mStopListeningButton = (Button) findViewById(R.id.stop_processing);
        if (mIsOwner) {
            mStopListeningButton.setText("End");
        }

        mResulsLayout = (LinearLayout) findViewById(R.id.results_layout);
        checkLayout();

        // start Service
        Log.d(TAG, "Service started from main activity");

        activityContext = this.getApplicationContext();
        Intent service = new Intent(activityContext, SpeechService.class);
        activityContext.startService(service);

        mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0
                : Context.BIND_ABOVE_CLIENT;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mStartListeningButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                startListening();
            }
        });

        mStopListeningButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPrefs.edit().putBoolean(Constants.LISTENING, false).commit();
                stopListening();
            }
        });

    }

    protected void stopListening() {

        mIsListening = false;
        checkLayout();
        Log.d(TAG, "On Stop called");
        if (mServiceMessenger != null)
        {
            Message msg = new Message();
            msg.what = SpeechService.MSG_RECOGNIZER_CANCEL_OVERRIDE;
            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Stoping listen");
            // unbindService(mServiceConnection);
            // mServiceMessenger = null;
        }

    }

    protected void startListening() {
        Log.d(TAG, "On start called");
        mPrefs.edit().putBoolean(Constants.LISTENING, true).commit();
        mIsListening = true;
        checkLayout();
        bindService(new Intent(this, SpeechService.class), mServiceConnection, mBindFlag);
    }

    private void checkLayout() {
        if (mIsListening) {
            mStartListeningButton.setEnabled(false);
            mStartListeningButton.setBackgroundColor(getResources().getColor(
                    android.R.color.darker_gray));
            mStopListeningButton.setBackgroundColor(getResources().getColor(android.R.color.black));
            mStopListeningButton.setEnabled(true);
        } else {
            mStopListeningButton.setBackgroundColor(getResources().getColor(
                    android.R.color.darker_gray));
            mStartListeningButton
                    .setBackgroundColor(getResources().getColor(android.R.color.black));
            mStartListeningButton.setEnabled(true);
            mStopListeningButton.setEnabled(false);
        }

    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mServiceMessenger != null)
        {
            unbindService(mServiceConnection);
            mServiceMessenger = null;
        }
        this.finish();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d(TAG, "onServiceConnected");

            mServiceMessenger = new Messenger(service);
            mServiceMessenger.getBinder();
            Message msg = new Message();

            msg.what = SpeechService.MSG_RECOGNIZER_START_LISTENING_OVERRIDE;

            try
            {
                mServiceMessenger.send(msg);
            }
            catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            Log.d(TAG, "onServiceDisconnected");
            mServiceMessenger = null;
        }

    }; // mServiceConnection

    private class SpeechProcessCallback implements SpeechCallback {
        @Override
        public void processString(String string) {
            mString = mString + ":" + string;
            Log.d(TAG, "Callback called - " + string);
        }
    }

}
