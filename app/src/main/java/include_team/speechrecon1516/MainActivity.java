package include_team.speechrecon1516;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.Manifest;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;


public class MainActivity extends ActivityStub {

    private static final String TAG = "MainActivityDebug";
    private static final String MyPREFERENCES = "MyPrefs";
    private SharedPreferences sharedpreferences;

    private File[] file ;
    private MediaRecorder audio_recorder = null;

    private static String audio_name = null;
    private static String audio_filename = null;

    private boolean isNotRecording = true;
    private Chronometer chronometer;
    private int audioCounter;

    private File from = null;

    /**
     * Saves newly recorded file
     * @param toastYES Set if you want to display a toast after saving file
     */
    private String saveFile(DialogInterface dialog,EditText et, boolean toastYES){
        //Link to audio file with temporary name
        from = new File(audio_filename);

        //Standardize file names
        String new_audio_filename = et.getText().toString().replaceAll(" ", "");
        new_audio_filename = new_audio_filename.replaceAll("\n", "");

        if (new_audio_filename.compareTo("") != 0)
            new_audio_filename = new_audio_filename.substring(0, 1).toUpperCase() + new_audio_filename.substring(1);
        if (new_audio_filename.compareTo("") == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.noRename), Toast.LENGTH_LONG).show();
            finalizeRecording();
            dialog.dismiss();
            return null;
        } else {
            //Check if file name is already in use
            for (int i = 0; i < file.length; i++)
                if (new_audio_filename.compareTo(file[i].getName().substring(0, file[i].getName().length() - 4)) == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.nameInUse), Toast.LENGTH_LONG).show();
                    finalizeRecording();
                    dialog.dismiss();
                    return null;
                }
        }
        File to = new File(audio_path + new_audio_filename + ".amr");
        if (from.renameTo(to) && toastYES)
            Toast.makeText(getApplicationContext(), new_audio_filename + " " + getString(R.string.saved), Toast.LENGTH_SHORT).show();

        audioCounter++;
        return new_audio_filename;
    }

    /**
     * Prepares audio recorder and starts it
     */
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

    /**
     * Creates an AlertDialog that lets decide what to do with the just recorded audio
     */
    private void finalizeRecording() {


        //Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_title)
                .setMessage(R.string.save);

        final View dialogView = getLayoutInflater().inflate(R.layout.rename, null);
        final EditText et = (EditText) dialogView.findViewById(R.id.editText);
        et.setText(audio_name);
        et.selectAll();

        builder.setView(dialogView);

        // Save file
        builder.setNegativeButton(R.string.save_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                saveFile(dialog, et, true);
                dialog.dismiss();
            }
        });

        // Discard file
        builder.setNeutralButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                from = new File(audio_filename);
                from.delete();
                dialog.dismiss();
            }
        });

        // Save and transcribe
        builder.setPositiveButton(R.string.save_trans, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filename = saveFile(dialog, et, false);
                Log.d(TAG, "file saved " +filename);
                if(filename!=null){
                    executeCallToServer(filename);
                    dialog.dismiss();
                }
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

        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
    }

    /**
     * Shows and saves last audio transcription (see @link ActivityStub.processFinish)
     * @param file Name chosen for the recording
     * @param text Text retrieved from server
     */
    protected void processFinish(String file, String text){
        if(text.compareTo("***ERROR***")==0) {
            Log.i(TAG, "Server send Error message");
            Toast.makeText(getApplicationContext(), getString(R.string.errorResponse), Toast.LENGTH_LONG).show();
            return;
        }
        if(!cancel_call) {
            setTxtFile(file, text);
            viewTranscription(txt_path, file);
        } else
            Toast.makeText(getApplicationContext(), R.string.trans_cancel, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Method");
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        audioCounter = sharedpreferences.getInt("counter", 0);

        if (Build.VERSION.SDK_INT >=23) {

            //check WRITE_EXTERNAL_STORAGE
            int perm = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.d(TAG, Integer.toString(perm));
            if (perm != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), getString(R.string.noPermission), Toast.LENGTH_LONG).show();
                this.finish();
            }

            //check RECORD_AUDIO
            perm = PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            Log.d(TAG, Integer.toString(perm));
            if (perm != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), getString(R.string.noPermission), Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
        String main_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                getString(R.string.directory_main) + "/";
        File dir_main = new File(main_path);
        if(dir_main.mkdir())
            Log.d(TAG, "Created " + main_path);

        audio_path = main_path + getString(R.string.directory_audio) + "/";
        final File dir = new File(audio_path);
        if(dir.mkdir())
            Log.d(TAG, "Created " + audio_path);

        String text_path = main_path + getString(R.string.directory_txt)+ "/";
        File dir_text = new File(text_path);
        if(dir_text.mkdir())
            Log.d(TAG, "Created " + text_path);

        //Set buttons
        final ImageButton btn_list = (ImageButton) findViewById(R.id.button_list);

        assert btn_list != null;
        btn_list.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ListActivity.class);
                startActivity(i);
            }
        });

        final ImageButton btn_info = (ImageButton) findViewById(R.id.info_button);

        assert btn_info != null;
        btn_info.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent k = new Intent(v.getContext(), InfoActivity.class);
                startActivity(k);
            }
        });

        final ImageButton btn_record = (ImageButton) findViewById(R.id.button_record);
        final TextView text_record = (TextView) findViewById(R.id.text_record);
        chronometer = (Chronometer) findViewById(R.id.chronometer);

        assert btn_record != null;
        btn_record.setImageResource(R.drawable.ic_mic_48dp);

        btn_record.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                assert text_record != null;
                if (isNotRecording) {
                    MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                    text_record.setText(R.string.stop_button);
                    btn_record.setImageResource(R.drawable.ic_stop_48dp);
                    chronometer.setVisibility(View.VISIBLE);
                    file = dir.listFiles();

                    audio_name = "Record" + audioCounter;
                    audio_filename = audio_path + audio_name + ".amr";

                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    btn_list.setClickable(false);
                    btn_info.setClickable(false);

                    startRecording();
                } else {

                    text_record.setText(R.string.record_button);
                    btn_record.setImageResource(R.drawable.ic_mic_48dp);
                    chronometer.stop();
                    chronometer.setVisibility(View.INVISIBLE);
                    btn_list.setClickable(true);
                    btn_info.setClickable(true);

                    //stop recording
                    audio_recorder.stop();
                    audio_recorder.release();
                    audio_recorder = null;

                    finalizeRecording();

                    MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
                isNotRecording = !isNotRecording;
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
        //Stop recording and discard audio
        if (!isNotRecording) {
            chronometer.stop();
            chronometer.setVisibility(View.INVISIBLE);
            final ImageButton btn_record = (ImageButton) findViewById(R.id.button_record);
            final TextView text_record = (TextView) findViewById(R.id.text_record);
            final LinearLayout main_layout = (LinearLayout) findViewById(R.id.main_layout);

            assert text_record != null;
            text_record.setText(R.string.record_button);
            assert btn_record != null;
            btn_record.setImageResource(R.drawable.ic_mic_48dp);
            assert main_layout != null;

            from = new File(audio_filename);
            if(!from.delete())
                Toast.makeText(getApplicationContext(),"File already doesn't exist",Toast.LENGTH_SHORT).show();

            isNotRecording = true;
        }

        if (audio_recorder != null) {
            audio_recorder.release();
            audio_recorder = null;
        }

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putInt("counter", audioCounter);
        Log.d(TAG, "Saved Persistent State");

        editor.apply();

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