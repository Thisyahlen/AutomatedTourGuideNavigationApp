package com.example.automatedtourguidesystem;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.automatedtourguidesystem.ui.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HistoryAdapter  extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private ArrayList<History> dataSet;
    private int show_button = 1;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference ref;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Activity activity;



    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView state_name;
        TextView time;
        TextView day_ago;


//        ImageView imageViewIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.state_name = (TextView) itemView.findViewById(R.id.state_name);
            this.time = (TextView) itemView.findViewById(R.id.state_address);
            this.day_ago = (TextView) itemView.findViewById(R.id.distance);
//            this.user_contact = (TextView) itemView.findViewById(R.id.contact_number);

        }
    }

    public HistoryAdapter(ArrayList<History> data, int showbutton, Activity activity) {
        this.activity = activity;
        show_button = showbutton;
        this.dataSet = data;
    }

    @Override
    public HistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_recyle_layout, parent, false);

//        view.setOnClickListener(ScheduleActivity.myOnClickListener);

        HistoryAdapter.MyViewHolder myViewHolder = new HistoryAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final HistoryAdapter.MyViewHolder holder, final int listPosition) {

        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        TextView state_name = holder.state_name;
        TextView time = holder.time;
        TextView day_ago = holder.day_ago;


//        //set value into textview
        state_name.setText(dataSet.get(listPosition).getLocation());

        if(dataSet.get(listPosition).getStart_time()!=null&&dataSet.get(listPosition).getEnd_time()!=null){
            String startAndEnd=dataSet.get(listPosition).getStart_time()+" to "+dataSet.get(listPosition).getEnd_time();
            time.setText(startAndEnd);
        }

        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH);
            cal.setTime(sdf.parse(dataSet.get(listPosition).getEnd_time()));// all done

            long msDiff = Calendar.getInstance().getTimeInMillis() - cal.getTimeInMillis();
            long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
            day_ago.setText(daysDiff+" days ago");
        }catch(Exception e){
            e.printStackTrace();
            day_ago.setText("Few days ago");

        }








    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}

