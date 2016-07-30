package include_team.speechrecon1516;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = "ListActivityDebug";
    boolean toggle[];
    private ListView mylist;
    ToggleButton playPause;
    MediaPlayer player;
    Activity ac = this;
    ArrayList<String> arr_list;

    // variables needed for playback
    private Handler handy = new Handler();
    private double startTime = 0;
    private double endTime= 0;
    SeekBar seek;
    TextView timeText;

    // Dialog must be closed in onPause
    AlertDialog dialog;
    AlertDialog dialog2;
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
            Drawable iconFile = getDrawable(R.drawable.ic_equalizer_black_48dp);
//            int rand = (int) Math.random()*3;
//            switch(rand){
//                case 0:
//                    iconFile.setColorFilter(getColor(R.color.primary), PorterDuff.Mode.SRC_ATOP);
//                    break;
//                case 1:
//                    iconFile.setColorFilter(getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
//                    break;
//                case 2:
//                    iconFile.setColorFilter(getColor(R.color.accent), PorterDuff.Mode.SRC_ATOP);
//                    break;
//            }
//            ;
            iconFile.setColorFilter(getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
            img.setImageDrawable(iconFile);

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Method");
        setContentView(R.layout.activity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);

        try {
            arr_list = (ArrayList<String>) ObjectSerializer.deserialize(prefs.getString("arr_list", ObjectSerializer.serialize(new ArrayList<List>())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (arr_list==null){
            arr_list = new ArrayList<String>();
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
            arr_list.add("aaa");
            arr_list.add("zzz");
            arr_list.add("cwewreulo");
        }

        mylist = (ListView) findViewById(R.id.listView);
        mylist.setItemsCanFocus(true);
        final ListAdapter arr_adapter = new ListAdapter(this, R.layout.list_entry, arr_list);
        mylist.setAdapter(arr_adapter);
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
                String choises[] = {(String)getText(R.string.dialog1),(String)getText(R.string.dialog2),(String)getText(R.string.dialog3),(String)getText(R.string.dialog4)};
                builder.setItems(choises, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                        case 0:
                            builder2.setTitle(file);
                            builder2.setView(inflater.inflate(R.layout.dialog_play, null));
                            player = MediaPlayer.create(ac, R.raw.prova);
                            player.setLooping(true);
                            player.start();
                            dialog2 = builder2.create();
                            dialog2.show();

                            timeText = (TextView) dialog2.findViewById(R.id.time);
                            endTime = player.getDuration();
                            startTime = player.getCurrentPosition();
                            seek = (SeekBar) dialog2.findViewById(R.id.seekBar);
                            seek.setMax((int) endTime);
                            seek.setProgress((int)startTime);

                            handy.postDelayed(UpdateSongTime,50);
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
                                    handy.removeCallbacks(UpdateSongTime);
                                    player.release();
                                }
                            });
                            dialog2.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    handy.removeCallbacks(UpdateSongTime);
                                    player.release();
                                }
                            });
                            break;
                        case 1:
                            Toast.makeText(getApplicationContext(), "Caso2", Toast.LENGTH_LONG).show();
                            break;
                        case 2:
                            final View dialogView =inflater.inflate(R.layout.rename, null);
                            final EditText editText = (EditText) dialogView.findViewById(R.id.editText);
                            builder2.setTitle("Rename");
                            builder2.setView(dialogView);
                            builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String text = editText.getText().toString().replaceAll(" ", "");
                                    text = text.toString().replaceAll("\n", "");
                                    boolean rename = true;
                                    if (text.compareTo("")==0){
                                        Toast.makeText(getApplicationContext(), getString(R.string.noRename), Toast.LENGTH_LONG).show();
                                        rename = !rename;
                                    }
                                    else {
                                        for(int i=0; i<arr_list.size(); i++)
                                            if(text.compareTo(arr_list.get(i))==0) {
                                                rename = !rename;
                                                Toast.makeText(getApplicationContext(), getString(R.string.nameInUse), Toast.LENGTH_LONG).show();
                                                break;
                                            }
                                    }
                                    if (rename)
                                        arr_list.set(position,text);
                                }
                            });
                            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                @Override
                                public void onFocusChange(View v, boolean hasFocus) {
                                    if (hasFocus) {
                                        dialog2.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                    }
                                }
                            });
                            dialog2 = builder2.create();
                            dialog2.show();
                            break;
                        case 3:
                            String nome = arr_list.get(position);
                            arr_list.remove(position);
                            arr_adapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), nome + getString(R.string.removed), Toast.LENGTH_LONG).show();
                            break;

                        }

                    }
                });

                dialog = builder.create();
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

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("arr_list", arr_list);
        Log.d(TAG,"State Saved");
        super.onSaveInstanceState(savedInstanceState);
    }

    /*@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(TAG, "Inside of onRestoreInstanceState");
        arr_list = (ArrayList<String>) savedInstanceState.getSerializable("array_list");
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart Method");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume Method");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause Method");
        handy.removeCallbacks(UpdateSongTime);
        if (player != null)
            player.release();
        if (dialog != null)
            dialog.dismiss();
        if (dialog2 != null)
            dialog2.dismiss();


        SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString("arr_list", ObjectSerializer.serialize(arr_list));
            Log.d(TAG, "Saved Persistent State");
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStopMethod");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy Method");
    }

}