package include_team.speechrecon1516;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private static final String TAG = "ListActivity";
    boolean toggle[];
    private ListView mylist;



    public class ListViewCache  {

        private View baseView;
        private TextView textName;
        private ImageView imagePlay;

        public ListViewCache(View baseView) {
            this.baseView = baseView;
        }

        public View getViewBase() {
            return baseView;
        }

        public TextView getTextName(int resource) {
            if (textName == null) {
                textName = (TextView) baseView.findViewById(R.id.name_file);
            }
            return textName;
        }

        public ImageView getImageView(int resource) {
            if (imagePlay == null) {
                imagePlay = (ImageView) baseView.findViewById(R.id.play_button);
            }
            return imagePlay;
        }
    }


    public class ListAdapter extends ArrayAdapter{

            private int resource;
            private LayoutInflater inflater;
            private Context context;


            public ListAdapter ( Context ctx, int resourceId, List objects) {

                super( ctx, resourceId, objects );
                resource = resourceId;
                inflater = LayoutInflater.from( ctx );
                context=ctx;
            }

           /* private View.OnClickListener PlayButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int position = getListView().getPositionForView(v);
                    if (position != ListView.INVALID_POSITION) {
                        showMessage(getString(R.string.you_want_to_buy_format, CHEESES[position]));
                    }
                }
           }
    */

        @Override
        public View getView (int position, View convertView, ViewGroup parent ) {

            String name_file = (String) getItem( position );
            ListViewCache viewCache;


            if ( convertView == null ) {
                convertView = ( RelativeLayout ) inflater.inflate( resource, null );
                viewCache = new ListViewCache( convertView );
                //((ImageView) convertView.findViewById(R.id.play_button)).setOnClickListener(PlayButtonListener);
                convertView.setTag( viewCache );
            }
            else {
                convertView = ( RelativeLayout ) convertView;
                viewCache = ( ListViewCache ) convertView.getTag();
            }

            // metti il nome della lista nella view
            TextView txtName = (TextView) viewCache.getTextName(resource);
            txtName.setText(name_file);

            // metti il simbolo di play
            ImageView img=(ImageView)viewCache.getImageView(resource);
            Drawable buttonPlay = getDrawable(R.drawable.ic_equalizer_black_48dp);
            //Drawable buttonPause = getDrawable(R.drawable.ic_play_circle_outline_black_48dp);
            img.setImageDrawable(buttonPlay);

            return convertView;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ArrayList<String> arr_list = new ArrayList<String>();
        arr_list.add("ciao");
        arr_list.add("culo");
        arr_list.add("aaa");
        arr_list.add("zzz");
        arr_list.add("cwewreulo");
        arr_list.add("aaggghaa");
        arr_list.add("zzssad");
        arr_list.add("ciao");
        arr_list.add("culo");
        arr_list.add("aaa");
        arr_list.add("zzz");
        arr_list.add("cwewreulo");
        arr_list.add("aaggghaa");
        arr_list.add("zzssad");
        arr_list.add("ciao");
        arr_list.add("culo");
        arr_list.add("aaa");
        arr_list.add("zzz");
        arr_list.add("cwewreulo");
        arr_list.add("aaggghaa");
        arr_list.add("zzssad");

        toggle = new boolean[arr_list.size()];
        Arrays.fill(toggle, false);

        mylist = (ListView) findViewById(R.id.listView);
        mylist.setItemsCanFocus(true);
        mylist.setAdapter(new ListAdapter(this, R.layout.list_entry, arr_list));
        final Context cx = this;
        // 1. Instantiate an AlertDialog.Builder with its constructor
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);


        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

                Object o = mylist.getItemAtPosition(position);
                String file = o.toString();
                // Toast.makeText(getApplicationContext(), "You have chosen the pen: " + " " + pen, Toast.LENGTH_LONG).show();
                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setTitle(file);
                String choises[] = {(String)getText(R.string.dialog1),(String)getText(R.string.dialog1),(String)getText(R.string.dialog1)};
                builder.setItems(choises, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                        case 0:
                            Toast.makeText(getApplicationContext(), "Caso1", Toast.LENGTH_LONG).show();
                            break;
                        case 1:
                            Toast.makeText(getApplicationContext(), "Caso3", Toast.LENGTH_LONG).show();
                            break;
                        case 2:
                            Toast.makeText(getApplicationContext(), "Caso2", Toast.LENGTH_LONG).show();
                            break;
                        }

                    }
                });
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });




       /* mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
                toggle[position] = !toggle[position];

                ImageView imageView = (ImageView) itemView.findViewById(R.id.play_button);
                imageView.setImageResource(toggle[position] ? R.drawable.ic_play_circle_outline_black_48dp : R.drawable.ic_pause_circle_filled_black_48dp);
            }

        });*/
    }


   /*
    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }
    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }
    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
    }
*/
}