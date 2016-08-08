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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ListActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private static final String TAG = "ListActivityDebug";
    private File audio_file[];
    private File txt_file[];
    private ToggleButton playPause;
    private MediaPlayer player;
    private Activity ac = this;

    private ListView mylist;
    private ArrayList<ArrayEntry> arr_list = new ArrayList<ArrayEntry>();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String audio_path;
    private String txt_path;

    // variables needed for playback
    private Handler handy = new Handler();
    private double startTime = 0;
    private double endTime= 0;
    private SeekBar seek;
    private TextView timeText;

    private Context cx;
    private AlertDialog.Builder builderMenu;
    private AlertDialog.Builder builder2;
    private AlertDialog.Builder builderPlay;
    private AlertDialog.Builder builderText;
    private LayoutInflater inflater;

    // Dialog must be closed in onPause
    private AlertDialog dialogMenu;
    private AlertDialog dialogReplace;
    private AlertDialog dialogPlay;
    private AlertDialog dialogText;

    private class ArrayEntry {
        private String name;
        private String info;
        private boolean transcribed;

        public ArrayEntry(String cname, String cinfo, boolean ctranscribed){
            name = cname;
            info = cinfo;
            transcribed = ctranscribed;
        }

        public String getName() {
            return name;
        }

        public String getInfo() {
            return info;
        }

        public boolean isTranscribed() {
            return transcribed;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setTranscribed(boolean transcribed) {
            this.transcribed = transcribed;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }

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

    private class callToServer extends AsyncTask<String, Void, Void>  {

        boolean noConnectivity = false;
        URL url;
        HttpURLConnection connection;

        String file_path;
        String filename;
        String serverResponse ;

        InputStream inputStream;
        FileInputStream fileInputStream ;
        DataOutputStream dos;

        ProgressDialog Dialog = new ProgressDialog(ListActivity.this);

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int pos ; // position on the list

        public void execute(int position){
            pos = position;
            execute();
        }

        protected void onPreExecute() {

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            if (!(networkInfo != null && networkInfo.isConnected())) {
                Toast.makeText(getApplicationContext(), getString(R.string.noNetwork), Toast.LENGTH_LONG).show();
                noConnectivity = true;
                return;
            }

            file_path = audio_path + arr_list.get(pos).getName() + ".mp3";
            filename = arr_list.get(pos).getName() + ".mp3";

            try {
                fileInputStream = new FileInputStream(file_path);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "onPreExecute - callToServer - " + e.getMessage());
            }

            Log.d(TAG, "onPreExecute - callToServer - File: " + file_path);

            Dialog.setMessage("Uploading...");
            Dialog.show();
        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {

            if (noConnectivity)
                return null;

            try{
                Log.d(TAG, "doInBackground - Connecting..");
                url = new URL(getString(R.string.serverLocation));
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
                connection.setRequestProperty("uploaded_file", file_path);


                dos = new DataOutputStream(connection.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + filename +"\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of maximum size
                int bytesAvailable = fileInputStream.available();

                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[ ] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0,bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // close streams
                fileInputStream.close();

                dos.flush();

                InputStream is = connection.getInputStream();

                // retrieve the response from server
                int ch;

                StringBuffer b =new StringBuffer();
                while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
                serverResponse =b.toString();
                dos.close();
                Log.d(TAG, "doInBackground terminated - callToServer");
            }
            catch (MalformedURLException ex)
            {
                Log.e(TAG, "URL error: " + ex.getMessage(), ex);
            }

            catch (IOException ioe)
            {
                Log.e(TAG, "IO error: " + ioe.getMessage(), ioe);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
           if(noConnectivity)
               return;

            Log.d(TAG, "onPostExecute - callToServer");

            MyAdapter.ViewHolder holder = (MyAdapter.ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(pos);
            //holder.done.setVisibility(View.VISIBLE);
            arr_list.get(pos).setTranscribed(true);
            mAdapter.notifyItemChanged(pos);

            Dialog.dismiss();

            Log.d(TAG, "Message from Server: "+ serverResponse);
            setTxtFile(filename, serverResponse);
            viewTranscription(pos);


        }


    }

    private void setToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbarList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setBuilders(){
        builderMenu = new AlertDialog.Builder(cx);
        builder2 = new AlertDialog.Builder(cx);
        builderPlay = new AlertDialog.Builder(cx);
        builderText = new AlertDialog.Builder(cx);
        inflater = getLayoutInflater();
    }

    private void setAudioFiles(){
        audio_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (String)getText(R.string.directory_audio) + "/";
        txt_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (String)getText(R.string.directory_txt) + "/";
        Log.d(TAG, "Path for file: " + audio_path);

        File audio_dir = new File(audio_path);
        audio_dir.mkdir();
        audio_file = audio_dir.listFiles();

        File txt_dir = new File(txt_path);
        txt_dir.mkdir();
        txt_file = txt_dir.listFiles();

        if(audio_file!=null){
            Log.d(TAG, "Files in directory: "+ audio_file.length);
            for (int i=0;i<audio_file.length;i++){
                String name = audio_file[i].getName();
                name = name.substring(0, name.length() - 4);

                boolean hasBeenTranscribed = false;
                String txtname;
                for(int j=0; j<txt_file.length; j++){
                    txtname = txt_file[j].getName();
                    Log.d(TAG+"moi", txtname.substring(0, txtname.length() - 4) +" " + name);
                    if (txtname.substring(0, txtname.length() - 4).compareTo(name)==0)
                        hasBeenTranscribed = true;
                }

                arr_list.add(new ArrayEntry(name, null, hasBeenTranscribed));
            }
            Log.d(TAG, "arr_list size: "+ arr_list.size());
        }
    }

    private void playRecord(String fileName){
        builderPlay.setTitle(fileName);
        builderPlay.setView(inflater.inflate(R.layout.dialog_play, null));
        player = MediaPlayer.create(ac, Uri.parse(audio_path + "/" + fileName + ".mp3"));
        player.setLooping(true);
        player.start();
        dialogPlay = builder2.create();
        dialogPlay.show();

        timeText = (TextView) dialogReplace.findViewById(R.id.time);
        endTime = player.getDuration();
        startTime = player.getCurrentPosition();
        seek = (SeekBar) dialogReplace.findViewById(R.id.seekBar);
        seek.setMax((int) endTime);
        seek.setProgress((int)startTime);

        handy.postDelayed(UpdateSongTime,50);
        playPause = (ToggleButton) dialogReplace.findViewById(R.id.togglePlay);
        playPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
            }
        });

        dialogPlay.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handy.removeCallbacks(UpdateSongTime);
                player.release();
            }
        });
        dialogPlay.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                handy.removeCallbacks(UpdateSongTime);
                player.release();
            }
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        ArrayList<ArrayEntry> list ;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView textView;
            public ImageView icon;
            public ImageView done;

            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                textView = (TextView) itemView.findViewById(R.id.name_file);
                icon = (ImageView) itemView.findViewById(R.id.equalizer);
                done = (ImageView) itemView.findViewById(R.id.done);

            }

        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public MyAdapter(ArrayList<ArrayEntry> ls) {
            list = ls;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_entry, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.textView.setText(list.get(position).getName());
            Drawable iconFile = getDrawable(R.drawable.ic_record_voice_over_black_36dp);
            iconFile.setColorFilter(getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
            holder.icon.setImageDrawable(iconFile);

            Drawable doneFile = getDrawable(R.drawable.ic_done_black_36dp);
            doneFile.setColorFilter(getColor(R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
            holder.done.setImageDrawable(doneFile);

            if (!(list.get(position).isTranscribed()))
                holder.done.setVisibility(View.INVISIBLE);
            else
                holder.done.setVisibility(View.VISIBLE);



        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    public void viewTranscription(int position){
        builderText.setTitle(arr_list.get(position).getName());

        //Get the text file
        File file = new File(txt_path,arr_list.get(position).getName() +".txt");

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        builderText.setMessage(text.toString());
        dialogText = builderText.create();
        dialogText.show();
    }


    private void setRecyclerList(){
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(arr_list);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(cx, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, final int position) {

                        final String fileName =  (String) arr_list.get(position).getName();

                        builderMenu.setTitle(fileName);
                        String choices[] = {(String)getText(R.string.dialog1),(String)getText(R.string.dialog2),(String)getText(R.string.dialog3),(String)getText(R.string.dialog4)};

                        if (arr_list.get(position).isTranscribed())
                            choices[1] = (String)getText(R.string.dialogX);

                        builderMenu.setItems(choices, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case 0: // play record
                                        playRecord(fileName);
                                        break;
                                    case 1: // transcribe
                                        if (arr_list.get(position).isTranscribed())
                                            viewTranscription(position);
                                        else
                                            new callToServer().execute(position);
                                        break;

                                    case 2: // rename
                                        rename(position, fileName);
                                        break;
                                    case 3: // delete
                                        String nome = arr_list.get(position).getName();
                                        File toDelete = new File(audio_path + "/" + fileName + ".mp3");
                                        toDelete.delete();
                                        arr_list.remove(position);
                                        mAdapter.notifyItemRemoved(position);
                                        Toast.makeText(getApplicationContext(), nome + " " + getString(R.string.removed), Toast.LENGTH_LONG).show();
                                        break;

                                }

                            }
                        });

                        dialogMenu = builderMenu.create();
                        dialogMenu.show();
                    }
                }));

    }

    private void setViewList(){
       // mylist = (ListView) findViewById(R.id.listView);
        mylist.setItemsCanFocus(true);
        final ListAdapter arr_adapter = new ListAdapter(this, R.layout.list_entry, arr_list);
        mylist.setAdapter(arr_adapter);

        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                final String fileName =  (String) mylist.getItemAtPosition(position);

                builderMenu.setTitle(fileName);
                String choises[] = {(String)getText(R.string.dialog1),(String)getText(R.string.dialog2),(String)getText(R.string.dialog3),(String)getText(R.string.dialog4)};
                builderMenu.setItems(choises, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0: // play record
                                playRecord(fileName);
                                break;
                            case 1: // transcribe
                                new callToServer().execute(position);
                                break;

                            case 2: // rename
                                rename(position, fileName);
                                break;
                            case 3: // delete
                                String nome = arr_list.get(position).getName();
                                File toDelete = new File(audio_path + "/" + fileName + ".mp3");
                                toDelete.delete();
                                arr_list.remove(position);
                                arr_adapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), nome + " " + getString(R.string.removed), Toast.LENGTH_LONG).show();
                                break;

                        }

                    }
                });

                dialogMenu = builderMenu.create();
                dialogMenu.show();
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
                        if(text.compareTo(arr_list.get(i).getName())==0) {
                            Toast.makeText(getApplicationContext(), getString(R.string.nameInUse), Toast.LENGTH_LONG).show();
                            rename(position, fileName);
                            return;
                        }
                }

                arr_list.get(position).setName(text);
                File newFile = new File(audio_path + "/" + text + ".mp3");
                File oldFile = new File(audio_path + "/" + fileName + ".mp3");
                oldFile.renameTo(newFile);
                mAdapter.notifyItemChanged(position);

            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialogReplace.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        editText.setText(fileName);
        editText.selectAll();
        dialogReplace = builder2.create();
        dialogReplace.show();

    }

    private void setTxtFile(String filename, String text){
        txt_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (String)getText(R.string.directory_txt) + "/";
        Log.d(TAG, "Path for txt file: " + txt_path);
        File dir = new File(txt_path);
        dir.mkdir();

        try{
            File newfile = new File(txt_path + filename.substring(0, filename.length()-4) + ".txt");
            newfile.createNewFile();
            FileOutputStream out = new FileOutputStream(newfile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(out);
            myOutWriter.append(text);
            myOutWriter.close();
            out.close();
            Toast.makeText(getBaseContext(), "Transcription saved in \n" + txt_path,  Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
               Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Method");
        setContentView(R.layout.activity_list);
        cx = this;
       // SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);

        setToolbar();

        setBuilders();

        setAudioFiles();

        setRecyclerList();

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

 /*   @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable("arr_list", arr_list);
        Log.d(TAG,"State Saved");
        super.onSaveInstanceState(savedInstanceState);
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
        if (dialogMenu != null)
            dialogMenu.dismiss();
        if (dialogReplace != null)
            dialogReplace.dismiss();
        if (dialogText != null)
            dialogText.dismiss();
        if (dialogPlay != null)
            dialogPlay.dismiss();


 /*       SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString("arr_list", ObjectSerializer.serialize(arr_list));
            Log.d(TAG, "Saved Persistent State");
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();*/
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