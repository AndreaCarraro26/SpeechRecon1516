package include_team.speechrecon1516;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class PlayerSupport {

    Handler handy = new Handler();
    double startTime = 0;
    double endTime= 0;
    SeekBar seek;
    TextView timeText;

    String audio_path;
    String fileName;

    MediaPlayer player;

    public PlayerSupport(MediaPlayer play){
        player = play;
    }



    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            startTime = player.getCurrentPosition();
            timeText.setText(String.format("%d′%d″/%d′%d″",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) startTime)),
                    TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) endTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
            );
            seek.setProgress((int)startTime);
            handy.postDelayed(this, 16);
        }
    };

    public void exec(){
        player.start();
        // timeText = (TextView) diaPlay.getDialog().findViewById(R.id.time);
        endTime = player.getDuration();
        startTime = player.getCurrentPosition();
        seek.setMax((int) endTime);
        seek.setProgress((int)startTime);

        handy.postDelayed(UpdateSongTime,50);
    }
}
