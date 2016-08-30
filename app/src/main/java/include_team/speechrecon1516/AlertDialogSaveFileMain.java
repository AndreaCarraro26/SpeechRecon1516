package include_team.speechrecon1516;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class AlertDialogSaveFileMain extends DialogFragment {

    private static final String TAG = "ListActivityDebug";

    /**
     * Create new instance of AlertDialogMenu and set his bundle
     */
    public static AlertDialogSaveFileMain newInstance() {
        AlertDialogSaveFileMain frag = new AlertDialogSaveFileMain();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Create a menù that allow to choose what to do with a record
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.save_title)
               .setMessage(R.string.save);

        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.rename, null);
        final EditText et = (EditText) dialogView.findViewById(R.id.editText);
        et.setText(getArguments().getString("audio_name"));
        et.selectAll();

        builder.setView(dialogView);

        // Save file
        builder.setNegativeButton(R.string.save_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                ((MainActivity)getActivity()).saveFile(dialog, et.getText().toString(), true);
                dialog.dismiss();
            }
        });

        // Discard file
        builder.setNeutralButton(R.string.delete_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File from = new File(getArguments().getString("audio_filename"));
                from.delete();
                dialog.dismiss();
            }
        });

        // Save and transcribe
        builder.setPositiveButton(R.string.save_trans, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filename =  ((MainActivity)getActivity()).saveFile(dialog, et.getText().toString(), false);
                Log.d(TAG, "file saved " +filename);
                if(filename!=null){
                    ((MainActivity)getActivity()).executeCallToServer(filename);
                    dialog.dismiss();
                }
            }
        });

        final AlertDialog dialog = builder.create();

        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        return dialog;
    }

}
