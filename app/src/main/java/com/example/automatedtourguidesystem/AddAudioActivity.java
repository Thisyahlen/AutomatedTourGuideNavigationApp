package com.example.automatedtourguidesystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AddAudioActivity extends AppCompatActivity {


    private Button startbtn, stopbtn ;
    private FloatingActionButton mRecordButton = null;
    private FloatingActionButton mPlayButton= null;
    private FloatingActionButton mStopButton= null;
    private Button mPauseButton = null;
    private TextView mRecordLabel;
    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;
    private Intent intent;
    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;
    private String mFileName = null;
    private String mFilePath = null;



    private MediaRecorder mRecorder = null;


    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;
    private RecordingService.OnTimerChangedListener onTimerChangedListener = null;
    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    private Timer mTimer = null;
    private TimerTask mIncrementTimerTask = null;

    private Chronometer mChronometer = null;

    private MediaPlayer mPlayer;
    long timeWhenPaused = 0; //stores time when user clicks pause button
    private DatabaseReference databaseReference;
    private StorageReference mStorage;
    private static final String LOG_TAG = "AudioRecording";

    private ProgressDialog mProgress;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_audio);
//        startbtn = (Button)findViewById(R.id.btnRecord);
//        stopbtn = (Button)findViewById(R.id.btnStop);
        mPlayButton = (FloatingActionButton)findViewById(R.id.btnPlay);
        mStopButton = (FloatingActionButton)findViewById(R.id.btnStopPlay);


        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/AudioRecording.3gp";

        databaseReference = FirebaseDatabase.getInstance().getReference();

        mProgress = new ProgressDialog(this);
        mStorage= FirebaseStorage.getInstance().getReference("uploads");

        mChronometer = (Chronometer)findViewById(R.id.chronometer);
        //update recording prompt text
        mRecordingPrompt = (TextView)findViewById(R.id.recording_status_text);

        mRecordButton = (FloatingActionButton)findViewById(R.id.btnRecord);
        mRecordButton.setColorNormal(getResources().getColor(R.color.primary));
        mRecordButton.setColorPressed(getResources().getColor(R.color.primary_dark));
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            }
        });

        mPauseButton = (Button)findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(LOG_TAG, "prepare() stop record");

                onPauseRecord(mPauseRecording);
                mPauseRecording = !mPauseRecording;
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPlayer = new MediaPlayer();
                if(mFilePath !=null) {
                    try {
                        mPlayer.setDataSource(mFilePath);
                        mPlayer.prepare();
                        mPlayer.start();

                        Toast.makeText(getApplicationContext(), "Recording Started Playing", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                } else {

                    Toast.makeText(getApplicationContext(), "No audio recorded.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mPlayer !=null) {
                    mPlayer.release();
                    mPlayer = null;

                    Toast.makeText(getApplicationContext(), "Playing Audio Stopped", Toast.LENGTH_SHORT).show();
                } else {


                    Toast.makeText(getApplicationContext(), "No Recorded audio played.", Toast.LENGTH_SHORT).show();
                }


            }
        });

        Button upload = findViewById(R.id.audio_upload_button);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mFilePath!=null) {
                    uploadAudio();
                } else {
                    Toast.makeText(getApplicationContext(),"No audio file recorded.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onRecord(boolean start){

        Intent intent = new Intent(this, RecordingService.class);

        if (start) {
            // start recording
            mRecordButton.setImageResource(R.drawable.ic_media_stop);
            //mPauseButton.setVisibility(View.VISIBLE);
            startRecording();
            Toast.makeText(this,R.string.toast_recording_start,Toast.LENGTH_SHORT).show();
            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                //folder /SoundRecorder doesn't exist, create the folder
                folder.mkdir();
            }

            //start Chronometer
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    if (mRecordPromptCount == 0) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                    } else if (mRecordPromptCount == 1) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                    } else if (mRecordPromptCount == 2) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                        mRecordPromptCount = -1;
                    }

                    mRecordPromptCount++;
                }
            });

            //start RecordingService
            startService(intent);
            //keep screen on while recording
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
            mRecordPromptCount++;

        } else {
            //stop recording
            stopRecording();
            mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            //mPauseButton.setVisibility(View.GONE);
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            mRecordingPrompt.setText(getString(R.string.record_prompt));

            stopService(intent);
            //allow the screen to turn off again once recording is finished
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    //TODO: implement pause recording
    private void onPauseRecord(boolean pause) {


        if (pause) {


            //pause recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_media_play ,0 ,0 ,0);
            mRecordingPrompt.setText((String)getString(R.string.resume_recording_button).toUpperCase());
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();

        } else {
            //resume recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_media_pause ,0 ,0 ,0);
            mRecordingPrompt.setText((String)getString(R.string.pause_recording_button).toUpperCase());
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            mChronometer.start();
        }
    }



    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }

        super.onDestroy();
    }

    public void startRecording() {
        setFileNameAndPath();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);


        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

            //startTimer();
            //startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void setFileNameAndPath(){
        int count = 0;
        File f;

        do{
            count++;

            mFileName = getString(R.string.default_file_name)
                    + "_" + count + ".mp4";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;

            f = new File(mFilePath);
        }while (f.exists() && !f.isDirectory());
    }

    public void stopRecording() {
        mRecorder.stop();

        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();
        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;

        try {
//            Toast.makeText(getApplicationContext(),"The Recording has stopped.", Toast.LENGTH_SHORT).show();
//            mDatabase.addRecording(mFileName, mFilePath, mElapsedMillis);

        } catch (Exception e){
            Log.e(LOG_TAG, "exception", e);
        }
    }




    private void uploadAudio() {

        if(mFilePath !=null) {

            mProgress.setMessage("Uploading Audio...");
            mProgress.show();

            StorageReference filepath = mStorage.child("Audio").child(mFilePath);

            final Uri uri = Uri.fromFile(new File(mFilePath));

            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    intent = new Intent(AddAudioActivity.this, AddLocationActivity.class);

                    intent.putExtra("Audio_url", String.valueOf(uri));

                    mProgress.dismiss();
                    Toast.makeText(getApplicationContext(), "Audio Upload Successfully.", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }
            });

        } else {

            Toast.makeText(getApplicationContext(), "Please record an audio first.", Toast.LENGTH_SHORT).show();
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
    private void RequestPermissions() {
        ActivityCompat.requestPermissions(AddAudioActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }
}
