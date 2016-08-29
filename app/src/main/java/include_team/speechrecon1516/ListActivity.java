package include_team.speechrecon1516;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ListActivity extends ActivityStub  {

    private static final String TAG = "ListActivityDebug";
    private ArrayList<ArrayEntry> arr_list = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;

        /**
         * Creates DialogFragment for audio reproduction
         * @param fileName File to be played
         */
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

        /**
         * Creates DialogFragment for renaming record and corresponding text
         * @param position position of file in the array
         */
    protected void rename(int position) {

        MyAlertDialogFragment diaRename = MyAlertDialogFragment.newInstance();
        Bundle args = new Bundle();
        args.putString("filename", arr_list.get(position).getName());
        args.putInt("size", arr_list.size());
        args.putInt("position", position);
        args.putInt("type", MyAlertDialogFragment.RENAME);
        ArrayList<String> arr_strings = new ArrayList<>();
        for (int i=0; i<arr_list.size();i++)
            arr_strings.add(arr_list.get(i).getName());
        args.putStringArrayList("files", arr_strings);
        diaRename.setArguments(args);
        diaRename.show(getFragmentManager(), "tag");
    }

        /**
         * Actually renames files and update the list
         * @param pos Position of the file in the list
         * @param new_name Old name of file
         */
    protected void finalizeCaseRename(int pos, String new_name){
        String old_name = arr_list.get(pos).getName();
        arr_list.get(pos).setName(new_name);

        // rename audio file
        File newFile = new File(audio_path + "/" + new_name + ".amr");
        File oldFile = new File(audio_path + "/" + old_name + ".amr");
        if(!oldFile.renameTo(newFile))
            Log.e(TAG,"Failed to rename audio file" + old_name);

        // rename text file
        if(arr_list.get(pos).isTranscribed()){
            newFile = new File(txt_path + "/" + new_name + ".txt");
            oldFile = new File(txt_path + "/" + old_name + ".txt");
            if(!oldFile.renameTo(newFile))
                Log.e(TAG,"Failed to rename audio file " + old_name);
        }
        mAdapter.notifyItemChanged(pos);
    }

        /**
         * Create and shows text file from server response (see @link ActivityStub.processFinish)
         * @param file Chosen recording
         * @param text Server response
         */
    protected void processFinish(String file, String text){
        int pos = 0 ;
        for (int i=0; i<arr_list.size(); i++)
            if (arr_list.get(i).getName().compareTo(file)==0) {
                pos = i;
                break;
            }
        if(text.compareTo("***ERROR***")==0){
            Log.i(TAG, "Server send Error message");
            Toast.makeText(getApplicationContext(),getString(R.string.errorResponse), Toast.LENGTH_SHORT).show();
            return;
        }
        if(!cancel_call) {
            arr_list.get(pos).setTranscribed();
            setTxtFile(file, text);
            mAdapter.notifyItemChanged(pos);
            viewTranscription(txt_path, file);
        }
        else {
            Log.d(TAG, "Task Canceled");
            Toast.makeText(getApplicationContext(), R.string.trans_cancel, Toast.LENGTH_LONG).show();
        }

    }

        /**
         * Deletes audio and text files from the device
         * @param pos Position of the recording in the list
         */
    public void deleteFile(int pos){
        String name = arr_list.get(pos).getName();
        File toDelete = new File(audio_path + "/" + name + ".amr");
        if(!toDelete.delete())
            Toast.makeText(getApplicationContext(),getString(R.string.noDeleteAudio), Toast.LENGTH_SHORT).show();
        toDelete = new File(txt_path + "/" + name + ".txt");
        if(toDelete.exists())
            if(!toDelete.delete())
                Toast.makeText(getApplicationContext(),getString(R.string.noDeleteText),Toast.LENGTH_SHORT).show();
        arr_list.remove(pos);
        mAdapter.notifyItemRemoved(pos);
        Toast.makeText(getApplicationContext(), name + " " + getString(R.string.removed), Toast.LENGTH_LONG).show();

        if(arr_list.size()==0)
            this.getSupportActionBar().setSubtitle(R.string.listEmpty);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Method");
        setContentView(R.layout.activity_list);
        Context cx = this;
        audio_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                + this.getText(R.string.directory_main) + "/" + getText(R.string.directory_audio) + "/";

        txt_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"
                + getText(R.string.directory_main) + "/" + getText(R.string.directory_txt) + "/";

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarList);
        setSupportActionBar(toolbar);

        //////////////////////////////////////////////////
        //Read audio files in directory and populate ArrayList
        //////////////////////////////////////////////////

        File audio_dir = new File(audio_path);
        File audio_files[] = audio_dir.listFiles();

        File txt_dir = new File(txt_path);
        File txt_file[] = txt_dir.listFiles();

        if(audio_files!=null){
            Log.d(TAG, "Files in directory audio : "+ audio_files.length);
            Log.d(TAG, "Files in directory text : "+ txt_file.length);
            for (int i=0;i<audio_files.length;i++){
                String name = audio_files[i].getName();
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

        // Change subtitle on the toolbar if there isn't any audio file
        if(audio_files.length==0){
            toolbar.setSubtitle(R.string.listEmpty);
        }

        ////////////////////////////////////////////////
        //Create and adjust setting of the recyclerView
        ////////////////////////////////////////////////

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

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
                        ArrayList<String> arr_strings = new ArrayList<>();
                        for (int i=0; i<arr_list.size();i++)
                            arr_strings.add(arr_list.get(i).getName());
                        args.putStringArrayList("files", arr_strings);
                        args.putBoolean("isTranscribed", arr_list.get(position).isTranscribed());
                        diaMenu.setArguments(args);
                        diaMenu.show(getFragmentManager(), "tag");
                    }
                }));
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

}