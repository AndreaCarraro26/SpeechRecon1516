package include_team.speechrecon1516;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class ListActivity extends AppCompatActivity {

    private static final String TAG = "ListActivityDebug";

    private ArrayList<ArrayEntry> arr_list = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private String audio_path;
    private String txt_path;

    private Context cx;


    public class CallToServer extends AsyncTask<String, Void, Void> {

        boolean noConnectivity = false;
        URL url;
        HttpURLConnection connection;

        ArrayList<ArrayEntry> arr_list = new ArrayList<>();
        String audio_path;
        String file_path;
        String filename;
        String serverResponse ;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setAudioFiles(){
        audio_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getText(R.string.directory_main) + "/" + getText(R.string.directory_audio) + "/";
        txt_path   = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getText(R.string.directory_main) + "/" + getText(R.string.directory_txt) + "/";
        Log.d(TAG, "Path for file: " + txt_path);

        File audio_dir = new File(audio_path);
        audio_dir.mkdir();
        File audio_files[] = audio_dir.listFiles();

        File txt_dir = new File(txt_path);
        txt_dir.mkdir();
        File txt_file[] = txt_dir.listFiles();

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

    protected void playRecord(String fileName){

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


    protected void viewTranscription(int position){

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
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(arr_list, this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(cx, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, final int position) {

                        final String fileName = arr_list.get(position).getName();

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

    protected void rename(final int position, final String fileName) {

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

    protected void finalizeCaseRename(int pos, String text, String fileName){
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

    public void executeCallToServer(int pos){
        new CallToServer().execute(pos);
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