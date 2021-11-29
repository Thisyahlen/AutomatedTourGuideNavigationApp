package com.example.automatedtourguidesystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.automatedtourguidesystem.ui.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

public class VisitingPlacesAdapter extends RecyclerView.Adapter<VisitingPlacesAdapter.MyViewHolder> {

    private ArrayList<Report> dataSet;
    private int show_button = 1;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference ref;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Activity activity;
    private Location Location_cur;


    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView state_name;
        TextView state_address;
        TextView distance;
        LinearLayout relative;
        ImageButton audio_play;

//        ImageView imageViewIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.state_name = (TextView) itemView.findViewById(R.id.state_name);
            this.state_address = (TextView) itemView.findViewById(R.id.state_address);
            this.distance = (TextView) itemView.findViewById(R.id.distance);
            this.relative = (LinearLayout) itemView.findViewById(R.id.relative);
            this.audio_play = (ImageButton) itemView.findViewById(R.id.audio_play);
//            this.user_contact = (TextView) itemView.findViewById(R.id.contact_number);

        }
    }

    public VisitingPlacesAdapter(ArrayList<Report> data, int showbutton, Activity activity, Location Location_cur) {
        this.activity = activity;
        show_button = showbutton;
        this.dataSet = data;
        this.Location_cur = Location_cur;
    }

    @Override
    public VisitingPlacesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.visiting_places_recyle_layout, parent, false);

//        view.setOnClickListener(ScheduleActivity.myOnClickListener);

        VisitingPlacesAdapter.MyViewHolder myViewHolder = new VisitingPlacesAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final VisitingPlacesAdapter.MyViewHolder holder, final int listPosition) {

        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        TextView state_name = holder.state_name;
        TextView state_address = holder.state_address;
        TextView distance = holder.distance;
        LinearLayout relative = holder.relative;
        final ImageButton audio_play = holder.audio_play;

        //set value into textview
        state_name.setText(dataSet.get(listPosition).getPlace());
        state_address.setText(String.valueOf(dataSet.get(listPosition).getAddress()));
        if (Location_cur != null) {
            distance.setText(String.valueOf(dataSet.get(listPosition).getDistanceDifferent(Location_cur.getLatitude(), Location_cur.getLongitude())) + "KM");
        } else {
            distance.setText("");
        }

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(dataSet.get(listPosition).getAudio());
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                }

            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audio_play.setImageResource(R.drawable.play);
                    mp.pause();
                }
            });

            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            mediaPlayer = null;
        }

        final MediaPlayer mediaPlayer1 = mediaPlayer;
        audio_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer1 != null) {
                    if (mediaPlayer1.isPlaying()) {
                        audio_play.setImageResource(R.drawable.play);
                        mediaPlayer1.pause();
                    } else {
                        audio_play.setImageResource(R.drawable.stop);
                        mediaPlayer1.start();
                    }

                }


            }

        });


        final Intent intent = new Intent(activity, LocationGeofence.class);
        ;

        relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new Intent(activity, BookingActivity.class);
                intent.putExtra("state_name", dataSet.get(listPosition).getPlace());
                intent.putExtra("state_address", dataSet.get(listPosition).getAddress());
                intent.putExtra("lat", dataSet.get(listPosition).getLatitude());
                intent.putExtra("long", dataSet.get(listPosition).getLongitude());
                intent.putExtra("audio_url", dataSet.get(listPosition).getAudio());
//                intent.putExtra("distance", dataSet.get(listPosition).getLongitude());
                activity.startActivity(intent);


            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
