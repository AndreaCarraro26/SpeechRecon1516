package include_team.speechrecon1516;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = "ListActivity";
    boolean toggle[];
    private ListView mylist;
    ToggleButton playPause;
    MediaPlayer player;
    Activity ac = this;

    // variables needed for playback
    private Handler handy = new Handler();
    private double startTime = 0;
    private double endTime= 0;
    SeekBar seek;
    TextView timeText;

    public class ListViewCache  {

        private View baseView;
        private TextView textName;
        private ImageView imagePlay;

        public ListViewCache(View baseView) {
            this.baseView = baseView;
        }

        public View getViewBase() {
            return baseView;
        }

        public TextView getTextName(int resource) {
            if (textName == null) {
                textName = (TextView) baseView.findViewById(R.id.name_file);
            }
            return textName;
        }

        public ImageView getImageView(int resource) {
            if (imagePlay == null) {
                imagePlay = (ImageView) baseView.findViewById(R.id.equalizer);
            }
            return imagePlay;
        }
    }


    public class ListAdapter extends ArrayAdapter{

            private int resource;
            private LayoutInflater inflater;
            private Context context;


            public ListAdapter ( Context ctx, int resourceId, List objects) {

                super( ctx, resourceId, objects );
                resource = resourceId;
                inflater = LayoutInflater.from( ctx );
                context=ctx;
            }

        @Override
        public View getView (int position, View convertView, ViewGroup parent ) {

            String name_file = (String) getItem( position );
            ListViewCache viewCache;


            if ( convertView == null ) {
                convertView = ( RelativeLayout ) inflater.inflate( resource, null );
                viewCache = new ListViewCache( convertView );
                //((ImageView) convertView.findViewById(R.id.play_button)).setOnClickListener(PlayButtonListener);
                convertView.setTag( viewCache );
            }
            else {
                convertView = ( RelativeLayout ) convertView;
                viewCache = ( ListViewCache ) convertView.getTag();
            }

            // metti il nome della lista nella view
            TextView txtName = (TextView) viewCache.getTextName(resource);
            txtName.setText(name_file);

            // metti il simbolo di play
            ImageView img=(ImageView)viewCache.getImageView(resource);
            Drawable buttonPlay = getDrawable(R.drawable.ic_equalizer_black_48dp);
            //Drawable buttonPause = getDrawable(R.drawable.ic_play_circle_outline_black_48dp);
            img.setImageDrawable(buttonPlay);

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ArrayList<String> arr_list = new ArrayList<String>();
        arr_list.add("ciao");
        arr_list.add("culo");
        arr_list.add("aaa");
        arr_list.add("zzz");
        arr_list.add("cwewreulo");
        arr_list.add("aaggghaa");
        arr_list.add("zzssad");
        arr_list.add("ciao");
        arr_list.add("culo");
        arr_list.add("aaa");
        arr_list.add("zzz");
        arr_list.add("cwewreulo");


        toggle = new boolean[arr_list.size()];
        Arrays.fill(toggle, false);

        mylist = (ListView) findViewById(R.id.listView);
        mylist.setItemsCanFocus(true);
        mylist.setAdapter(new ListAdapter(this, R.layout.list_entry, arr_list));
        final Context cx = this;
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();


        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

                final String file =  (String) mylist.getItemAtPosition(position);

                builder.setTitle(file);
                String choises[] = {(String)getText(R.string.dialog1),(String)getText(R.string.dialog2),(String)getText(R.string.dialog3)};
                builder.setItems(choises, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                        case 0:
                            builder2.setTitle(file);
                            builder2.setView(inflater.inflate(R.layout.dialog_play, null));
                            player = MediaPlayer.create(ac, R.raw.prova);
                            player.setLooping(true);
                            player.start();
                            AlertDialog dialog2 = builder2.create();
                            dialog2.show();

                            timeText = (TextView) dialog2.findViewById(R.id.time);
                            endTime = player.getDuration();
                            startTime = player.getCurrentPosition();
                            seek = (SeekBar) dialog2.findViewById(R.id.seekBar);
                            seek.setMax((int) endTime);
                            seek.setProgress((int)startTime);


                            handy.postDelayed(UpdateSongTime,100);
                            playPause = (ToggleButton) dialog2.findViewById(R.id.togglePlay);
                            playPause.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    if (player.isPlaying()) {
                                        player.pause();
                                    } else {
                                        player.start();
                                    }
                                }

                            });

                            dialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    player.release();
                                    handy.removeCallbacks(UpdateSongTime);
                                }
                            });
                            dialog2.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    player.release();
                                    handy.removeCallbacks(UpdateSongTime);
                                }
                            });
                            break;
                        case 1:
                            Toast.makeText(getApplicationContext(), "Caso2", Toast.LENGTH_LONG).show();
                            break;
                        case 2:
                            Toast.makeText(getApplicationContext(), "Caso3", Toast.LENGTH_LONG).show();
                            break;
                        }

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



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
            handy.postDelayed(this, 100);
        }
    };

   /*
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }
*/
}