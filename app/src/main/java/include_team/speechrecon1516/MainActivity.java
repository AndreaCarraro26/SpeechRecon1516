package include_team.speechrecon1516;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityDebug";
    private static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;

    File file[] ;

    // Create a new instance of android.media.MediaRecorder
    private MediaRecorder audio_recorder = null;

    AlertDialog.Builder builder;

    // private static final String LOG_TAG = "Audio record";
    private static String audio_path = null;
    private static String main_path = null;
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
        audio_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audio_recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        audio_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        audio_recorder.setAudioSamplingRate(16000);

        audio_recorder.setOutputFile(audio_filename);
        audio_recorder.setAudioChannels(1);
        try {
            audio_recorder.prepare();
            audio_recorder.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed",e);
        }


    }

    private void stopRecording() {
        audio_recorder.stop();
        audio_recorder.release();
        audio_recorder = null;
    }

    public void renameFile() {

        //create the AlertDialog
        builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_title)
                .setMessage(R.string.save);
        final EditText et = new EditText(this);
        builder.setView(et);
        //add the buttons
        builder.setPositiveButton(R.string.save_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //operazioni per salvare il file
                from = new File(audio_filename);

                new_audio_filename = et.getText().toString().replaceAll(" ", "");
                new_audio_filename = new_audio_filename.replaceAll("\n", "");
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
                if (from.renameTo(to))
                    Toast.makeText(getApplicationContext(), new_audio_filename + " " + getString(R.string.saved), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //operazioni per eliminare il file
                from = new File(audio_filename);
                from.delete();
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();

        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        et.setText(audio_name);

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

        main_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.directory_main) + "/";
        File dir_main = new File(main_path);
        if(dir_main.mkdir())
            Log.d(TAG, "Created " + main_path);
        audio_path = main_path + getString(R.string.directory_audio) + "/";
        final File dir = new File(audio_path);
        if(dir.mkdir())
            Log.d(TAG, "Created " + audio_path);


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

                    file = dir.listFiles();

                    audio_name = "Record" + audioCounter;
                    audio_filename = audio_path + audio_name + ".amr";
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

        if (!audioStartRecording) {
            chronometer.stop();

            final Button btn_record = (Button) findViewById(R.id.button_record);
            final TextView text_record = (TextView) findViewById(R.id.text_record);
            final RelativeLayout main_layout = (RelativeLayout) findViewById(R.id.main_layout);
            final RelativeLayout timer = (RelativeLayout) findViewById(R.id.time_layout);

            renameFile();

            assert text_record != null;
            text_record.setText(R.string.record_button);
            assert btn_record != null;
            btn_record.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mic_48dp, 0, 0);
            assert main_layout != null;
            main_layout.removeView(timer);

            audioStartRecording = true;
        }

        if (audio_recorder != null) {
            audio_recorder.release();
            audio_recorder = null;
        }

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putInt("counter", audioCounter);
        Log.d(TAG, "Saved Persistent State");

        editor.commit();

        super.onPause();
        Log.d(TAG, "onPause Method");
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