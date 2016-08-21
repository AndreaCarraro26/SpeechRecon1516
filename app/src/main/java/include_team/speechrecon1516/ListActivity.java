package include_team.speechrecon1516;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ListActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private static final String TAG = "ListActivityDebug";
    private File audio_files[];
    private File txt_file[];


    private ArrayList<ArrayEntry> arr_list = new ArrayList<ArrayEntry>();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private String audio_path;
    private String txt_path;

    private Context cx;

    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);

    private class ArrayEntry {
        private String name;
        private boolean transcribed;
        Date date;

        public ArrayEntry(String cname, Date dd, boolean ctranscribed){
            name = cname;
            date = dd;
            transcribed = ctranscribed;
        }

        public String getName() {
            return name;
        }

        public Date getDate() {
            return date;
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

        public void setDate(Date date) {
            this.date = date;
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

        MyAlertDialogFragment prog = MyAlertDialogFragment.newInstance();

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String error;

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

            file_path = audio_path + arr_list.get(pos).getName() + ".amr";
            filename = arr_list.get(pos).getName() + ".amr";

            try {
                fileInputStream = new FileInputStream(file_path);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "onPreExecute - callToServer - " + e.getMessage());
            }

            Log.d(TAG, "onPreExecute - callToServer - File: " + file_path);


            Bundle args = new Bundle();
            args.putInt("type", MyAlertDialogFragment.PROGRESS);
            args.putString("message", getString(R.string.connecting));
            prog.setArguments(args);
            prog.show(getFragmentManager(), "tag");
            prog.setCancelable(false);

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
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary+"; charset=utf-8");
                connection.setRequestProperty("uploaded_file", file_path);
                connection.setConnectTimeout(5000); //set timeout to 5 seconds


                dos = new DataOutputStream(connection.getOutputStream());

                // Lancia eccezione se fatto partire da qua dentro
                //dialogP.setMessage(getString(R.string.uploading));

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + filename +"\"" + lineEnd);
                dos.writeBytes(lineEnd);

                Log.d(TAG, filename);
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
                error = "Cannot connect to server: URL malformed.";
            }
            catch (SocketTimeoutException toe) {
                Log.e(TAG, "Timeout error: " + toe.getMessage(), toe);
                error = "Cannot connect to server: timeout expired";
            }
            catch (IOException ioe)
            {
                Log.e(TAG, "IO error: " + ioe.getMessage(), ioe);
            }


            return null;
        }

        protected void onPostExecute(Void unused) {
            Log.d(TAG, "onPostExecute - callToServer");
            if(noConnectivity)
               return;

            if(error!=null){
                prog.dismiss();
                Log.d(TAG, error);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            }
            else {
                MyAdapter.ViewHolder holder = (MyAdapter.ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(pos);
                arr_list.get(pos).setTranscribed(true);
                mAdapter.notifyItemChanged(pos);

                prog.dismiss();

                Log.d(TAG, "Message from Server: " + serverResponse);
                setTxtFile(filename, serverResponse);
                viewTranscription(pos);
            }
        }


    }

    private void setToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbarList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setAudioFiles(){
        audio_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (String)getText(R.string.directory_main) + "/" + (String)getText(R.string.directory_audio) + "/";
        txt_path   = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (String)getText(R.string.directory_main) + "/" + (String)getText(R.string.directory_txt) + "/";
        Log.d(TAG, "Path for file: " + txt_path);

        File audio_dir = new File(audio_path);
        audio_dir.mkdir();
        audio_files = audio_dir.listFiles();

        File txt_dir = new File(txt_path);
        txt_dir.mkdir();
        txt_file = txt_dir.listFiles();

        if(audio_files!=null){
            Log.d(TAG, "Files in directory audio : "+ audio_files.length);
            Log.d(TAG, "Files in directory text : "+ txt_file.length);
            for (int i=0;i<audio_files.length;i++){
                String name = audio_files[i].getName();
                Log.d(TAG, name.substring(name.length() - 4, name.length()));
                if(name.substring(name.length() - 4, name.length()).compareTo(".amr")==0) {
                    name = name.substring(0, name.length() - 4);

                    boolean hasBeenTranscribed = false;
                    String txtname;
                    for (int j = 0; j < txt_file.length; j++) {
                        txtname = txt_file[j].getName();
                        if (txtname.substring(0, txtname.length() - 4).compareTo(name) == 0)
                            hasBeenTranscribed = true;
                    }

                    arr_list.add(new ArrayEntry(name, new Date(audio_files[i].lastModified()), hasBeenTranscribed));
                }
            }

            Collections.sort(arr_list, new Comparator<ArrayEntry>() {
                @Override
                public int compare(ArrayEntry a1, ArrayEntry a2) {
                    return a2.getDate().compareTo(a1.getDate());
                }
            });
            Log.d(TAG, "arr_list size: "+ arr_list.size());
        }
    }

    private void playRecord(String fileName){

        MyAlertDialogFragment diaPlay = MyAlertDialogFragment.newInstance();
        Bundle args = new Bundle();
        args.putInt("type", MyAlertDialogFragment.PLAY);
        args.putString("filename", fileName);
        args.putString("path", audio_path);
        args.putInt("new_start", 0);
        args.putBoolean("isPlaying", true);
        diaPlay.setArguments(args);
        diaPlay.show(getFragmentManager(), "tag");



    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        ArrayList<ArrayEntry> list ;

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView textView;
            public TextView dateView;
            public ImageView icon;
            public ImageView done;

            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);

                textView = (TextView) itemView.findViewById(R.id.name_file);
                dateView = (TextView) itemView.findViewById(R.id.data_string);
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

            holder.textView.setText(list.get(position).getName());
            holder.dateView.setText(df.format( list.get(position).getDate()));
            Log.d(TAG, df.format( list.get(position).getDate()));
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

        File file = new File(txt_path,arr_list.get(position).getName() +".txt");

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

        MyAlertDialogFragment diaText = MyAlertDialogFragment.newInstance();
        Bundle args = new Bundle();
        args.putInt("type", MyAlertDialogFragment.TEXT);
        args.putString("title", arr_list.get(position).getName());
        args.putString("message", text.toString());
        diaText.setArguments(args);
        diaText.show(getFragmentManager(), "tag");

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

                        MyAlertDialogFragment diaMenu = MyAlertDialogFragment.newInstance();
                        Bundle args = new Bundle();
                        args.putString("filename", fileName);

                        args.putInt("position", position);
                        args.putInt("type", MyAlertDialogFragment.START);
                        ArrayList<String> arr_strings = new ArrayList<String>();
                        for (int i=0; i<arr_list.size();i++)
                            arr_strings.add(arr_list.get(i).getName());
                        args.putStringArrayList("files", arr_strings);
                        args.putBoolean("isTranscribed", arr_list.get(position).isTranscribed());
                        diaMenu.setArguments(args);
                        diaMenu.show(getFragmentManager(), "tag");

                    }
                }));

    }

    private void rename(final int position, final String fileName) {

        MyAlertDialogFragment diaRename = MyAlertDialogFragment.newInstance();
        Bundle args = new Bundle();
        args.putString("filename", arr_list.get(position).getName());
        args.putInt("size", arr_list.size());
        args.putInt("position", position);
        args.putInt("type", MyAlertDialogFragment.RENAME);
        ArrayList<String> arr_strings = new ArrayList<String>();
        for (int i=0; i<arr_list.size();i++)
            arr_strings.add(arr_list.get(i).getName());
        args.putStringArrayList("files", arr_strings);
        diaRename.setArguments(args);
        diaRename.show(getFragmentManager(), "tag");


    }

    public void finalizeCaseRename(int pos, String text, String fileName){
        arr_list.get(pos).setName(text);
        File newFile = new File(audio_path + "/" + text + ".amr");
        File oldFile = new File(audio_path + "/" + fileName + ".amr");
        oldFile.renameTo(newFile);
        mAdapter.notifyItemChanged(pos);

    }

    private void setTxtFile(String filename, String text){
        txt_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (String)getText(R.string.directory_main) + "/" + (String)getText(R.string.directory_txt) + "/";

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

        setToolbar();

        setAudioFiles();

        setRecyclerList();

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

    public static class MyAlertDialogFragment extends DialogFragment {
        static final int TEXT = 0;
        static final int RENAME = 1;
        static final int PLAY = 2;
        static final int START = 3;
        static final int PROGRESS = 4;

        // variables needed for playback
        private Handler handy = new Handler();
        private int startTime;
        private double endTime= 0;
        private SeekBar seek;
        private TextView timeText;
        MediaPlayer player;
        Button play_pause;

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
                            .create();

                case START:
                    String choices[] = {(String)getText(R.string.dialog1),(String)getText(R.string.dialog2),(String)getText(R.string.dialog3),(String)getText(R.string.dialog4)};
                    if (getArguments().getBoolean("isTranscribed"))
                        choices[1] = (String)getText(R.string.dialogX);

                    final AlertDialog startDiag = new AlertDialog.Builder(getActivity())
                            .setTitle(getArguments().getString("filename"))
                            .setItems(choices, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case 0: // play record
                                            ((ListActivity)getActivity()).playRecord(getArguments().getString("filename"));
                                            break;
                                        case 1: // transcribe
                                            if (getArguments().getBoolean("isTranscribed"))
                                                ((ListActivity)getActivity()).viewTranscription(getArguments().getInt("position"));
                                            else
                                                ((ListActivity)getActivity()).executeCallToServer(getArguments().getInt("position"));
                                            break;
                                        case 2: // rename
                                            ((ListActivity)getActivity()).rename(getArguments().getInt("position"), getArguments().getString("filename"));
                                            break;
                                        case 3: // delete
                                            ((ListActivity)getActivity()).deleteFile(getArguments().getInt("position"));

                                            break;

                                    }

                                }
                            })
                            .create();
                    return startDiag;


                case TEXT:
                    Log.d(TAG, "Case Text");
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getArguments().getString("title"))
                            .setMessage(getArguments().getString("message"))
                            .create();

                case PLAY:
//                    Log.d(TAG, getArguments().getString("a"));
                    player = new MediaPlayer().create(((ListActivity)getActivity()), Uri.parse(getArguments().getString("path") + "/" + getArguments().getString("filename") + ".amr"));

                    View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_play, null);

                    AlertDialog dialog =  new AlertDialog.Builder(getActivity())
                        .setTitle(getArguments().getString("filename"))
                        .setView(view)
                        .create();

                    player.seekTo(getArguments().getInt("new_start"));
                    play_pause = (Button) view.findViewById(R.id.button_play);

                    if(getArguments().getBoolean("isPlaying")){
                        player.start();
                    }
                        else{
                        play_pause.setBackground(getResources().getDrawable(R.drawable.ic_play_circle_filled_black_48dp, null));
                    }

                    handy.postDelayed(UpdateSongTime,50);

                    timeText = (TextView) view.findViewById(R.id.time);
                    endTime = player.getDuration();
                    startTime = player.getCurrentPosition();

                    seek = (SeekBar) view.findViewById(R.id.seekBar);
                    seek.setMax((int) endTime);
                    seek.setProgress((int)startTime);



                    player.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            play_pause.setBackground(getResources().getDrawable(R.drawable.ic_play_circle_filled_black_48dp, null));
                            handy.removeCallbacks(UpdateSongTime);
                            seek.setProgress(0);
                        }
                    });

                    play_pause.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            if (player.isPlaying()) {
                                play_pause.setBackground(getResources().getDrawable(R.drawable.ic_play_circle_filled_black_48dp, null));
                                handy.removeCallbacks(UpdateSongTime);
                                player.pause();

                            } else {
                                play_pause.setBackground(getResources().getDrawable(R.drawable.ic_pause_circle_filled_black_48dp, null));
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
                                    String text = editText.getText().toString().replaceAll(" ", "");
                                    text = text.toString().replaceAll("\n", "");
                                    if (text.compareTo("") != 0)
                                        text = text.substring(0, 1).toUpperCase() + text.substring(1);
                                    if (text.compareTo("")==0){
                                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noRename), Toast.LENGTH_LONG).show();
                                        ((ListActivity)getActivity()).rename(getArguments().getInt("position"), getArguments().getString("filename"));
                                        return;
                                    }
                                    else {
                                        for(int i=0; i< getArguments().getInt("size"); i++)
                                            if(text.compareTo(getArguments().getStringArrayList("files").get(i))==0) {
                                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.nameInUse), Toast.LENGTH_LONG).show();
                                                ((ListActivity)getActivity()).rename(getArguments().getInt("position"), getArguments().getString("filename"));
                                                return;
                                            }
                                    }

                                    ((ListActivity)getActivity()).finalizeCaseRename(getArguments().getInt("position"), text, getArguments().getString("filename"));


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
        public void onPause(){
            super.onPause();

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

    public void executeCallToServer(int pos){
        new callToServer().execute(pos);
    }

    public void deleteFile(int pos){
        String name = arr_list.get(pos).getName();
        File toDelete = new File(audio_path + "/" + name + ".amr");
        toDelete.delete();
        toDelete = new File(txt_path + "/" + name + ".txt");
        toDelete.delete();
        arr_list.remove(pos);
        mAdapter.notifyItemRemoved(pos);
        Toast.makeText(getApplicationContext(), name + " " + getString(R.string.removed), Toast.LENGTH_LONG).show();
    }
}