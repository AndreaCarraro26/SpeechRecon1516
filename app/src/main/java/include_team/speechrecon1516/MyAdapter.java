package include_team.speechrecon1516;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private static final String TAG = "ListActivityDebug";
    private ArrayList<ArrayEntry> list ;
    private Context mContext;
    private DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public TextView dateView;
        public ImageView icon;
        public ImageView done;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.name_file);
            dateView = (TextView) itemView.findViewById(R.id.data_string);
            icon = (ImageView) itemView.findViewById(R.id.equalizer);
            done = (ImageView) itemView.findViewById(R.id.done);
        }
    }

    public MyAdapter(ArrayList<ArrayEntry> ls, Context c) {
        this.list = ls;
        this.mContext = c;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_entry, parent, false);
      return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.textView.setText(list.get(position).getName());
        holder.dateView.setText(df.format( list.get(position).getDate()));
        Log.d(TAG, df.format( list.get(position).getDate()));
        Drawable iconFile = mContext.getDrawable(R.drawable.ic_record_voice_over_black_36dp);
        iconFile.setColorFilter(ContextCompat.getColor(mContext, R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        holder.icon.setImageDrawable(iconFile);

        Drawable doneFile = mContext.getDrawable(R.drawable.ic_done_black_36dp);
        doneFile.setColorFilter(ContextCompat.getColor(mContext, R.color.primary_dark), PorterDuff.Mode.SRC_ATOP);
        holder.done.setImageDrawable(doneFile);

        if (!(list.get(position).isTranscribed()))
            holder.done.setVisibility(View.INVISIBLE);
        else
            holder.done.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
