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


public class AlertDialogRename extends DialogFragment {

    private static final String TAG = "ListActivityDebug";

    /**
     * Create new instance of AlertDialogMenu and set his bundle
     */
    public static AlertDialogRename newInstance() {
        AlertDialogRename frag = new AlertDialogRename();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                            String new_name = editText.getText().toString().replaceAll(" ", "");
                            new_name = new_name.replaceAll("\n", "");
                            if (new_name.compareTo("") != 0)
                                new_name = new_name.substring(0, 1).toUpperCase() + new_name.substring(1);
                            if (new_name.compareTo("")==0){
                                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.noRename), Toast.LENGTH_LONG).show();
                                ((ListActivity)getActivity()).rename(getArguments().getInt("position"));
                                return;
                            }
                            else {
                                for(int i=0; i< getArguments().getInt("size"); i++)
                                    if(new_name.compareTo(getArguments().getStringArrayList("files").get(i))==0 &&
                                            new_name.compareTo(getArguments().getString("filename"))!=0) {
                                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.nameInUse), Toast.LENGTH_LONG).show();
                                        ((ListActivity)getActivity()).rename(getArguments().getInt("position"));
                                        return;
                                    }
                            }
                            ((ListActivity)getActivity()).finalizeCaseRename(getArguments().getInt("position"), new_name);
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

}