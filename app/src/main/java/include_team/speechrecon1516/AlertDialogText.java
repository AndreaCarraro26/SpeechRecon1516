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
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class AlertDialogText extends DialogFragment {


    private static final String TAG = "ListActivityDebug";

    /**
     * Create new instance of AlertDialogMenu and set his bundle
     */
    public static AlertDialogText newInstance() {
        AlertDialogText frag = new AlertDialogText();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Case Text");
        //Shows transcription result
        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString("title"))
                .setMessage(getArguments().getString("message"))
                .create();
    }
}