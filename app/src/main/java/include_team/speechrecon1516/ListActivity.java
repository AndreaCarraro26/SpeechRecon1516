package include_team.speechrecon1516;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    private Toolbar toolbar;

    private static final String TAG = "ListActivityDebug";
    private ListView mylist;
    private File file[];
    ToggleButton playPause;
    MediaPlayer player;
    Activity ac = this;
    ArrayList<String> arr_list = new ArrayList<String>();

    String audio_path;

    // variables needed for playback
    private Handler handy = new Handler();
    private double startTime = 0;
    private double endTime= 0;
    SeekBar seek;
    TextView timeText;

    Context cx;
    AlertDialog.Builder builder;
    AlertDialog.Builder builder2;
    LayoutInflater inflater;

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

    private void setToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbarList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setBuilders(){
        builder = new AlertDialog.Builder(cx);
        builder2 = new AlertDialog.Builder(cx);
        inflater = getLayoutInflater();
    }

    private void setAudioFiles(){
        audio_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (String)getText(R.string.directory) + "/";
        Log.d(TAG, "Path for file: " + audio_path);
        File dir = new File(audio_path);
        dir.mkdir();
        file = dir.listFiles();
        if(file!=null){
            Log.d(TAG, "Files in directory: "+ file.length);
            for (int i=0;i<file.length;i++){
                String name = file[i].getName();
                arr_list.add(name.substring(0, name.length() - 4));
            }
        }
    }

    private void setViewList(){
        mylist = (ListView) findViewById(R.id.listView);
        mylist.setItemsCanFocus(true);
        final ListAdapter arr_adapter = new ListAdapter(this, R.layout.list_entry, arr_list);
        mylist.setAdapter(arr_adapter);

        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final String fileName =  (String) mylist.getItemAtPosition(position);

                builder.setTitle(fileName);
                String choises[] = {(String)getText(R.string.dialog1),(String)getText(R.string.dialog2),(String)getText(R.string.dialog3),(String)getText(R.string.dialog4)};
                builder.setItems(choises, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0: // play record
                                builder2.setTitle(fileName);
                                builder2.setView(inflater.inflate(R.layout.dialog_play, null));
                                player = MediaPlayer.create(ac, Uri.parse(audio_path + "/" + fileName + ".mp3"));
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
                            case 1: // transcribe
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

                            case 2: // rename
                                rename(position, fileName);
                                break;
                            case 3: // delete
                                String nome = arr_list.get(position);
                                File toDelete = new File(audio_path + "/" + fileName + ".mp3");
                                toDelete.delete();
                                arr_list.remove(position);
                                arr_adapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), nome + " " + getString(R.string.removed), Toast.LENGTH_LONG).show();
                                break;

                        }

                    }
                });

                dialog = builder.create();
                dialog.show();
            }
        });

    }

    private void rename(final int position, final String fileName) {
        final View dialogView2 =inflater.inflate(R.layout.rename, null);
        final EditText editText = (EditText) dialogView2.findViewById(R.id.editText);

        builder2.setTitle("Rename");
        builder2.setView(dialogView2);
        builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String text = editText.getText().toString().replaceAll(" ", "");
                text = text.toString().replaceAll("\n", "");
                if (text.compareTo("") != 0)
                    text = text.substring(0, 1).toUpperCase() + text.substring(1);
                if (text.compareTo("")==0){
                    Toast.makeText(getApplicationContext(), getString(R.string.noRename), Toast.LENGTH_LONG).show();
                    rename(position, fileName);
                    return;
                }
                else {
                    for(int i=0; i<arr_list.size(); i++)
                        if(text.compareTo(arr_list.get(i))==0) {
                            Toast.makeText(getApplicationContext(), getString(R.string.nameInUse), Toast.LENGTH_LONG).show();
                            rename(position, fileName);
                            return;
                        }
                }

                arr_list.set(position,text);
                File newFile = new File(audio_path + "/" + text + ".mp3");
                File oldFile = new File(audio_path + "/" + fileName + ".mp3");
                oldFile.renameTo(newFile);


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

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Method");
        setContentView(R.layout.activity_list);
        cx = this;
        SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);

        setToolbar();

        setBuilders();

        setAudioFiles();

        setViewList();

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