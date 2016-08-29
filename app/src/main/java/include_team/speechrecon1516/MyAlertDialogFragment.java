package include_team.speechrecon1516;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MyAlertDialogFragment extends DialogFragment {
    static final int TEXT = 0;
    static final int RENAME = 1;
    static final int PLAY = 2;
    static final int START = 3;
    static final int PROGRESS = 4;

    private static final String TAG = "ListActivityDebug";

    // variables needed for playback
    private Handler handy = new Handler();
    private int startTime;
    private double endTime = 0;
    private SeekBar seek;
    private TextView timeText;
    private MediaPlayer player;
    private ImageButton play_pause;

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = player.getCurrentPosition();
            timeText.setText(String.format(Locale.ENGLISH, "%d′%d″/%d′%d″",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)),
                    TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) endTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
            );
            seek.setProgress(startTime);
            handy.postDelayed(this, 16);
        }
    };

    public static MyAlertDialogFragment newInstance() {
        MyAlertDialogFragment frag = new MyAlertDialogFragment();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (getArguments().getInt("type")) {

            case PROGRESS:
                Log.d(TAG, "Case Progress");
                return new ProgressDialog.Builder(getActivity())
                        .setMessage(getArguments().getString("message"))
                        .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int button) {
                                ((ActivityStub)getActivity()).setTranscribeCanceled(true);
                }
            })
                        .create();

            case START:
                String choices[] = {(String)getText(R.string.dialog1),(String)getText(R.string.dialog2),
                                    (String)getText(R.string.dialog3),(String)getText(R.string.dialog4)};
                if (getArguments().getBoolean("isTranscribed"))
                    choices[1] = (String)getText(R.string.dialogX);

                return new AlertDialog.Builder(getActivity())
                        .setTitle(getArguments().getString("filename"))
                        .setItems(choices, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0: // play record
                                        ((ListActivity)getActivity()).playRecord(getArguments().getString("filename"));
                                        break;
                                    case 1: // transcribe
                                        if (getArguments().getBoolean("isTranscribed"))
                                            ((ListActivity)getActivity()).viewTranscription(((ListActivity)getActivity()).txt_path, getArguments().getString("filename"));
                                        else
                                            ((ListActivity)getActivity()).executeCallToServer(getArguments().getString("filename"));
                                        break;
                                    case 2: // rename
                                        ((ListActivity)getActivity()).rename(getArguments().getInt("position"));
                                        break;
                                    case 3: // delete
                                        ((ListActivity)getActivity()).deleteFile(getArguments().getInt("position"));
                                        break;
                                }
                            }
                        })
                        .create();

            case TEXT:
                Log.d(TAG, "Case Text");
                return new AlertDialog.Builder(getActivity())
                        .setTitle(getArguments().getString("title"))
                        .setMessage(getArguments().getString("message"))
                        .create();

            case PLAY:

                player = MediaPlayer.create(getActivity(), Uri.parse(getArguments().getString("path") +
                                                                            getArguments().getString("filename") + ".amr"));

                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_play, null);

                AlertDialog dialog =  new AlertDialog.Builder(getActivity())
                        .setTitle(getArguments().getString("filename"))
                        .setView(view)
                        .create();

                player.seekTo(getArguments().getInt("new_start"));
                play_pause = (ImageButton) view.findViewById(R.id.button_play);

                if(getArguments().getBoolean("isPlaying")){
                    player.start();
                }
                else{
                    play_pause.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
                }

                handy.postDelayed(UpdateSongTime,50);

                timeText = (TextView) view.findViewById(R.id.time);
                endTime = player.getDuration();
                startTime = player.getCurrentPosition();

                seek = (SeekBar) view.findViewById(R.id.seekBar);
                seek.setMax((int) endTime);
                seek.setProgress(startTime);

                player.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        play_pause.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
                        handy.removeCallbacks(UpdateSongTime);
                        seek.setProgress(0);
                    }
                });
                play_pause.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (player.isPlaying()) {
                            play_pause.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
                            handy.removeCallbacks(UpdateSongTime);
                            player.pause();

                        } else {
                            play_pause.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                            player.start();
                            handy.postDelayed(UpdateSongTime,50);
                        }
                    }
                });
                return dialog;

            case RENAME:
                Log.d(TAG, "Case Rename");
                final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.rename, null);
                final EditText editText = (EditText) dialogView.findViewById(R.id.editText);
                editText.setText(getArguments().getString("filename"));
                editText.selectAll();

                final AlertDialog alDiag = new AlertDialog.Builder(getActivity())
                        .setTitle("Rename")
                        .setView(dialogView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String new_name = editText.getText().toString().replaceAll(" ", "");
                                new_name = new_name.replaceAll("\n", "");
                                if (new_name.compareTo("") != 0)
                                    new_name = new_name.substring(0, 1).toUpperCase() + new_name.substring(1);
                                if (new_name.compareTo("")==0){
                                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noRename), Toast.LENGTH_LONG).show();
                                    ((ListActivity)getActivity()).rename(getArguments().getInt("position"));
                                    return;
                                }
                                else {
                                    for(int i=0; i< getArguments().getInt("size"); i++)
                                        if(new_name.compareTo(getArguments().getStringArrayList("files").get(i))==0 &&
                                            new_name.compareTo(getArguments().getString("filename"))!=0) {
                                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.nameInUse), Toast.LENGTH_LONG).show();
                                            ((ListActivity)getActivity()).rename(getArguments().getInt("position"));
                                            return;
                                        }
                                }

                                ((ListActivity)getActivity()).finalizeCaseRename(getArguments().getInt("position"), new_name);

                            }
                        })
                        .create();

                editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            alDiag.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                return alDiag;
        }
        return null;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(getArguments().getInt("type")==PLAY){
            Log.d(TAG, "Fragment - onSaveInstanceState " + Boolean.toString(getArguments().getInt("type")==PLAY));
            getArguments().putInt("new_start", player.getCurrentPosition());

            if(player.isPlaying())
                getArguments().putBoolean("isPlaying", true);
            else
                getArguments().putBoolean("isPlaying", false);

            player.pause();
            play_pause.setBackground(getResources().getDrawable(R.drawable.ic_play_circle_filled_black_48dp, null));
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if(getArguments().getInt("type")==PLAY){
            handy.removeCallbacks(UpdateSongTime);
            player.release();
        }
        super.onDestroy();
    }
}