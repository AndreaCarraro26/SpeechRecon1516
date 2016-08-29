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


public abstract class ActivityStub extends AppCompatActivity {

    private String TAG = "CallToServerDebug";

    protected String audio_path;
    protected String txt_path;
    protected boolean cancel_call = false;

    public CallToServer call;

    /**
     * Sends recording to the server
     * @param filename Name of chosen recording
     */
    public void executeCallToServer(String filename){
        call = new CallToServer();
        call.execute(filename);
    }

    public CallToServer getCallToServer(){
        return call;
    }

    /**
     * creates text file
     * @param filename Name of chosen recording
     * @param text Text transcribed from audio
     */
    protected void setTxtFile(String filename, String text){
        txt_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                getText(R.string.directory_main) + "/" + getText(R.string.directory_txt) + "/";

        try{
            File text_file = new File(txt_path + filename + ".txt");
            if(text_file.createNewFile()) {
                FileOutputStream out = new FileOutputStream(text_file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(out);
                myOutWriter.append(text);
                myOutWriter.close();
                out.close();
                Toast.makeText(getBaseContext(), "Transcription saved in \n" + txt_path, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), "Text file already exists!", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Abstract method for managing server response
     * @param file Record passed to server
     * @param text Server response
     */
    protected abstract void processFinish(String file, String text );

    /**
     * Shows the transcription of the chosen recording
     * @param path Path of the texts folder
     * @param filename Name of the chosen recording
     */
    public void viewTranscription(String path, String filename){

        File file = new File(path,filename +".txt");

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

            Toast.makeText(getApplicationContext(), "Error reading text file!", Toast.LENGTH_LONG).show();
            return;
        }

        MyAlertDialogFragment diaText = MyAlertDialogFragment.newInstance();
        Bundle args = new Bundle();
        args.putInt("type", MyAlertDialogFragment.TEXT);
        args.putString("title", filename);
        args.putString("message", text.toString());
        diaText.setArguments(args);
        diaText.show(getFragmentManager(), "tag");

    }



    /**
     * Subclass interfacing with server
     */
    public class CallToServer extends AsyncTask<Void, Void, String> {

        public void setTranscribeCanceled(boolean canceled) {
            cancel_call = canceled;
            Log.d(TAG,"Cancel pressed");
        }


        boolean noConnectivity = false;

        String audio_path;
        String file_path;
        String filename;
        String audio_text;

        FileInputStream fileInputStream ;

        MyAlertDialogFragment prog = MyAlertDialogFragment.newInstance();

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        String rec_name ;

        public void execute(String record_name){
            rec_name = record_name;
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
                URL url = new URL(getString(R.string.serverLocation));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("charset","UTF-8");
                connection.setRequestProperty("Accept-Charset", "UTF-8");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary+";");
                connection.setRequestProperty("uploaded_file", file_path);
                connection.setConnectTimeout(5000); //set timeout to 5 seconds

                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\"" + filename +"\"" + lineEnd);
                dos.writeBytes(lineEnd);
                Log.d(TAG,"Connesso");

                Log.d(TAG, filename);
                // create a buffer of maximum size
                int bytesAvailable = fileInputStream.available();

                int maxBufferSize = 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byte[ ] buffer = new byte[bufferSize];

                // read file and write it into form...
                int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    if(cancel_call) {
                        Log.d(TAG, "Task Canceled");
                        return null;
                    }

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

                // retrieve response from server
                int ch;

                StringBuffer b =new StringBuffer();
                while( ( ch = is.read() ) != -1 ){ b.append( (char)ch ); }
                audio_text = b.toString();
                dos.close();
                Log.d(TAG, "doInBackground terminated - callToServer");

            }
            catch (MalformedURLException ex)
            {
                Log.e(TAG, "URL error: " + ex.getMessage());
                error = "Cannot connect to server: URL malformed.";
            }
            catch (SocketTimeoutException toe) {
                Log.e(TAG, "Timeout error: " + toe.getMessage());
                error = "Cannot connect to server: timeout expired";
            }
            catch (IOException ioe)
            {
                Log.e(TAG, "IO error: " + ioe.getMessage());
                error = "Cannot connect to server: timeout expired";
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
                Log.d(TAG, "ARRIvato");
                if(!cancel_call){
                    prog.dismiss();
                    Log.d(TAG, "Message from Server: " + audio_text);

                    processFinish(rec_name, audio_text);
                }

            }
        }
    }
}
