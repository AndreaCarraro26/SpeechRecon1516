package include_team.speechrecon1516;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
            Drawable iconFile = getDrawable(R.drawable.ic_record_voice_over_black_36dp);
            iconFile.setColorFilter(getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
            img.setImageDrawable(iconFile);

            return convertView;
        }
    }


    // Class with extends AsyncTask class
    private class callToServer  extends AsyncTask<String, Void, Void>  {

        URL url;
        private HttpURLConnection conn;
        FileInputStream fileInputStream;
        DataOutputStream dos;

        private ProgressDialog Dialog = new ProgressDialog(ListActivity.this);

        String fileName = "text.txt";

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        int serverResponseCode = 0;

        //File sourceFile = getResources().openRawResource(R.raw.text); // ???????
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.text);
        File source = new File(uri.toString());

        protected void onPreExecute() {
            // NOTE: You can call UI Element here.

            //UI Element
            //uiUpdate.setText("Output : ");
            Dialog.setMessage("Uploading...");
            Dialog.show();
        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {

            try{
                // open a URL connection to the Servlet
                url = new URL("http://192.168.115/save.php");
                // fileInputStream = new FileInputStream(sourceFile);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=uploaded_file;filename=" + fileName  + lineEnd);
                dos.writeBytes(lineEnd);


                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            }
            catch (Exception e){

            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            // NOTE: You can call UI Element here.

            // Close progress dialog
            Dialog.dismiss();

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
                            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.isConnected()) {
                               new callToServer().execute();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), getString(R.string.noNetwork), Toast.LENGTH_LONG).show();
                                break;
                            }



//                            final View dialogView1 =inflater.inflate(R.layout.dialog_server, null);
//                            final TextView textP = (TextView) dialogView1.findViewById(R.id.textProgress);
//                            builder2.setTitle("Connecting with server");
//                            builder2.setView(dialogView1);
//                            textP.setText(getString(R.string.textServer));
//                            dialog2 = builder2.create();
//                            dialog2.show();
//                            dialog2.setCancelable(false);
//
//

                            break;















                        case 2:
                            final View dialogView2 =inflater.inflate(R.layout.rename, null);
                            final EditText editText = (EditText) dialogView2.findViewById(R.id.editText);
                            builder2.setTitle("Rename");
                            builder2.setView(dialogView2);
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