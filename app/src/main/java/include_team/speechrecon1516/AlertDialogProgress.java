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


public class AlertDialogProgress extends DialogFragment {

    private static final String TAG = "ListActivityDebug";

    /**
     * Create new instance of AlertDialogMenu and set his bundle
     */
    public static AlertDialogProgress newInstance() {
        AlertDialogProgress frag = new AlertDialogProgress();
        Bundle progArg = new Bundle();
        frag.setArguments(progArg);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                Log.d(TAG, "Case Progress");
                View progressView = getActivity().getLayoutInflater().inflate(R.layout.progress, null);
                return new ProgressDialog.Builder(getActivity())
                        .setView(progressView)
                        .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int button) {
                                ((ActivityStub) getActivity()).getCallToServer().cancel(true);
                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.trans_cancel),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}