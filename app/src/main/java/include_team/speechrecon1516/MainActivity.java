package include_team.speechrecon1516;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityDebug";
    private static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;

    File file[] ;

    // Create a new instance of android.media.MediaRecorder
    private MediaRecorder audio_recorder = null;

    private static final String LOG_TAG = "Audio record";
    private static String audio_path = null;
    private static String audio_name = null;
    private static String audio_filename = null;
    private static String new_audio_filename = null;

    private boolean audioStartRecording = true;

    Context ctx;
    Chronometer chronometer;
    private int audioCounter;
    EditText et = null;

    File from = null;
    File to = null;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }


    private void startRecording() {

        audio_recorder = new MediaRecorder();
        // Set the audio source using MediaRecorder.setAudioSource().
        audio_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // Set output file format using MediaRecorder.setOutputFormat()
        audio_recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        // Set output file name using MediaRecorder.setOutputFile()
        audio_recorder.setOutputFile(audio_filename);
        // Set the audio encoder using MediaRecorder.setAudioEncoder()
        audio_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        try {
            audio_recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        audio_recorder.start();
    }


    private void stopRecording() {
        audio_recorder.stop();
        audio_recorder.release();
        audio_recorder = null;
    }

    public void renameFile() {

        final Dialog dialog = new Dialog(ctx);
        dialog.setContentView(R.layout.dialog);
        TextView tv = (TextView) dialog.findViewById(R.id.textView1);
        et = (EditText) dialog.findViewById(R.id.editText1);
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        et.setText(audio_name);
        Button btn = (Button) dialog.findViewById(R.id.button1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from = new File(audio_filename);

                new_audio_filename = et.getText().toString().replaceAll(" ", "");
                new_audio_filename = new_audio_filename.toString().replaceAll("\n", "");
                if (new_audio_filename.compareTo("") != 0)
                    new_audio_filename = new_audio_filename.substring(0, 1).toUpperCase() + new_audio_filename.substring(1);
                if (new_audio_filename.compareTo("") == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.noRename), Toast.LENGTH_LONG).show();
                    renameFile();
                    dialog.dismiss();
                    return;
                } else {
                    for (int i = 0; i < file.length; i++)
                        if (new_audio_filename.compareTo(file[i].getName().substring(0, file[i].getName().length() - 4)) == 0) {
                            Toast.makeText(getApplicationContext(), getString(R.string.nameInUse), Toast.LENGTH_LONG).show();
                            renameFile();
                            dialog.dismiss();
                            return;
                        }
                }

                to = new File(audio_path + new_audio_filename + ".mp3");
                from.renameTo(to);
                Toast.makeText(getApplicationContext(), new_audio_filename + " " + getString(R.string.saved), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Method");
        setContentView(R.layout.activity_main);

        ctx = this;
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        audioCounter = sharedpreferences.getInt("counter", 0);

        Button btn_list = (Button) findViewById(R.id.button_list);

        assert btn_list != null;
        btn_list.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ListActivity.class);
                startActivity(i);
            }
        });


        final Button btn_record = (Button) findViewById(R.id.button_record);
        final TextView text_record = (TextView) findViewById(R.id.text_record);

        //Inflate the Hidden Layout Information View
        final RelativeLayout main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        final View timer = getLayoutInflater().inflate(R.layout.chrono, main_layout, false);

        assert btn_record != null;
        btn_record.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (audioStartRecording) {
                    assert text_record != null;
                    text_record.setText(R.string.stop_button);
                    btn_record.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_stop_48dp, 0, 0);
                    assert main_layout != null;
                    main_layout.addView(timer);

                    audio_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (String)getText(R.string.directory) + "/";
                    final File dir = new File(audio_path);
                    dir.mkdir();
                    file = dir.listFiles();

                    audio_name = "Record" + audioCounter;
                    audio_filename = audio_path + audio_name + ".mp3";
                    Log.d("nome", audio_filename);
                    audioCounter++;
                    chronometer = (Chronometer) findViewById(R.id.chronometer);
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                } else {
                    assert text_record != null;
                    text_record.setText(R.string.record_button);
                    btn_record.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mic_48dp, 0, 0);
                    chronometer.stop();

                    renameFile();

                    assert main_layout != null;
                    main_layout.removeView(timer);
                }

                onRecord(audioStartRecording);

                audioStartRecording = !audioStartRecording;
            }
        });

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "State resumed");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "State saved");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart Method");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume Method");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause Method");

        if (audio_recorder != null) {
            audio_recorder.release();
            audio_recorder = null;
        }

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putInt("counter", audioCounter);
        Log.d(TAG, "Saved Persistent State");

        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStopMethod");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy Method");
    }
}