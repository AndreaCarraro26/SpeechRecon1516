package include_team.speechrecon1516;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;


/**
 * Created by camillom on 23/08/16.
 */


public class ActivityStub extends AppCompatActivity {

    String TAG = "CallToServerDebug";

    protected String  audio_path;
    protected String txt_path;


    protected void setTxtFile(String filename, String text){
        txt_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + (String)getText(R.string.directory_main) + "/" + (String)getText(R.string.directory_txt) + "/";

        try{
            File newfile = new File(txt_path + filename + ".txt");
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

    public class CallToServer extends AsyncTask<Void, Void, String> {

        public ServerResponse delegate = null;

        boolean noConnectivity = false;
        URL url;
        HttpURLConnection connection;

        String audio_path;
        String file_path;
        String filename;
        String audio_text;

        FileInputStream fileInputStream ;
        DataOutputStream dos;

        MyAlertDialogFragment prog = MyAlertDialogFragment.newInstance();

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        String rec_name ; // position on the list
        int pos;

        public void execute(String record_name, int position){
            rec_name = record_name;
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
            audio_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                    + getText(R.string.directory_main) + "/" + getText(R.string.directory_audio) + "/";
            Log.d(TAG, "onPreExecute - callToServer - audio_path = " + audio_path);
            file_path = audio_path + rec_name + ".amr";
            filename = rec_name + ".amr";

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
        protected String doInBackground(Void... unused) {

            if (noConnectivity)
                return null;
            String error = null;

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
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
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
                audio_text = b.toString();
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

            return error;
        }

        protected void onPostExecute(String error) {
            Log.d(TAG, "onPostExecute - callToServer");
            if(noConnectivity)
                return;

            if(error!=null){
                prog.dismiss();
                Log.d(TAG, error);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            }
            else {
                // MyAdapter.ViewHolder holder = (MyAdapter.ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(pos);
                prog.dismiss();
                Log.d(TAG, "Message from Server: " + audio_text);

                delegate.processFinish(audio_text, pos);
                /*
                mAdapter.notifyItemChanged(pos);


                setTxtFile(filename, audio_text);
                viewTranscription(pos);*/

            }
        }
    }
}
