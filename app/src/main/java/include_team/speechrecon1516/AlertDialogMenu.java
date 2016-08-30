package include_team.speechrecon1516;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;


public class AlertDialogMenu extends DialogFragment {

    private static final String TAG = "ListActivityDebug";

    /**
     * Create new instance of AlertDialogMenu and set his bundle
     */
    public static AlertDialogMenu newInstance() {
        AlertDialogMenu frag = new AlertDialogMenu();
        Bundle args = new Bundle();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            String choices[] = {(String)getText(R.string.dialog1),(String)getText(R.string.dialog2),
                    (String)getText(R.string.dialog3),(String)getText(R.string.dialog4)};
            if (getArguments().getBoolean("isTranscribed"))
                choices[1] = (String)getText(R.string.dialogX);

            return new AlertDialog.Builder(getActivity())
                    .setTitle(getArguments().getString("filename"))
                    .setItems(choices, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0: // play record
                                    ((ListActivity)getActivity()).playRecord(getArguments().getString("filename"));
                                    break;
                                case 1: // transcribe
                                    if (getArguments().getBoolean("isTranscribed"))
                                        ((ListActivity)getActivity()).viewTranscription(((ListActivity)getActivity()).txt_path, getArguments().getString("filename"));
                                    else{
                                        ((ListActivity)getActivity()).executeCallToServer(getArguments().getString("filename"));

                                    }
                                    break;
                                case 2: // rename
                                    ((ListActivity)getActivity()).rename(getArguments().getInt("position"));
                                    break;
                                case 3: // delete
                                    ((ListActivity)getActivity()).deleteFile(getArguments().getInt("position"));
                                    break;
                            }
                        }
                    })
                    .create();
    }
}