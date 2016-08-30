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


public class AlertDialogPlay extends DialogFragment {

    private static final String TAG = "ListActivityDebug";

    // variables needed for playback
    private Handler handy = new Handler();
    private int startTime;
    private double endTime = 0;
    private SeekBar seek;
    private TextView timeText;
    private MediaPlayer player;
    private ImageButton play_pause;

    /**
     * Update time during playback
     */
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

    /**
     * Create new instance of AlertDialogMenu and set his bundle
     */
    public static AlertDialogPlay newInstance() {
        AlertDialogPlay frag = new AlertDialogPlay();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Allow to reproduce the record and navigate into it with a seekbar
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
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                if(player != null && fromUser) {
                    player.seekTo(progress);
                }
            }
        });

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

    }

    public void onSaveInstanceState(Bundle savedInstanceState) {

        getArguments().putInt("new_start", player.getCurrentPosition());

        if(player.isPlaying())
            getArguments().putBoolean("isPlaying", true);
        else
            getArguments().putBoolean("isPlaying", false);

        player.pause();
        play_pause.setBackground(getResources().getDrawable(R.drawable.ic_play_circle_filled_black_48dp, null));

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        handy.removeCallbacks(UpdateSongTime);
        player.release();

        super.onDestroy();
    }
}