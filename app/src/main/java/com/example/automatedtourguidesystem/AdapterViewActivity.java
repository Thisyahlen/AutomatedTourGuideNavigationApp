package com.example.automatedtourguidesystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.automatedtourguidesystem.ui.Report;
import com.example.automatedtourguidesystem.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class AdapterViewActivity extends RecyclerView.Adapter<AdapterViewActivity.MyViewHolder> {

    private Activity activity;
    private ArrayList<Report> dataSet;
    private Location Location_cur;
    private int show_button = 1;
    private FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
    private DatabaseReference ref= firebaseDatabase.getReference();
    private FirebaseAuth  auth = FirebaseAuth.getInstance();
    private FirebaseUser  user = auth.getCurrentUser();
    private String currentUserID;
    private ValueEventListener a;



    public static class MyViewHolder extends RecyclerView.ViewHolder {



        TextView state_name;
        TextView state_address;
        TextView distance;
        RelativeLayout relative;
        ImageButton audio_play;
        Button delete;






        public MyViewHolder(View itemView) {
            super(itemView);

            this.state_name = (TextView) itemView.findViewById(R.id.state_name);
            this.state_address = (TextView) itemView.findViewById(R.id.state_address);
            this.distance = (TextView) itemView.findViewById(R.id.distance);
            this.relative = (RelativeLayout) itemView.findViewById(R.id.relative);
            this.audio_play = (ImageButton) itemView.findViewById(R.id.audio_play);
            this.delete = (Button)itemView.findViewById(R.id.delete);



        }
    }

    public AdapterViewActivity(Activity activity,ArrayList<Report> data,int showbutton,Location Location_cur) {
        this.activity = activity;
        show_button = showbutton;
        this.dataSet = data;
        this.Location_cur = Location_cur;
    }

    @Override
    public AdapterViewActivity.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_adapter_view, parent, false);

        AdapterViewActivity.MyViewHolder myViewHolder = new AdapterViewActivity.MyViewHolder(view);
        return myViewHolder;
    }



    @Override
    public void onBindViewHolder(AdapterViewActivity.MyViewHolder holder, final int listPosition) {

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (auth.getCurrentUser() != null) {
            currentUserID = auth.getCurrentUser().getUid();
        } else {
            currentUserID = null;
        }
        ref = FirebaseDatabase.getInstance().getReference();


        TextView state_name = holder.state_name;
        TextView state_address = holder.state_address;
        TextView distance = holder.distance;
        RelativeLayout relative = holder.relative;
        final ImageButton audio_play = holder.audio_play;
        Button delete =holder.delete;


        //set value into textview
        state_name.setText(dataSet.get(listPosition).getPlace());
        state_address.setText(String.valueOf(dataSet.get(listPosition).getAddress()));
        if (Location_cur != null) {
            distance.setText(String.valueOf(dataSet.get(listPosition).getDistanceDifferent(Location_cur.getLatitude(), Location_cur.getLongitude())) + "KM");
        } else {
            distance.setText("");
        }

        final MediaPlayer mediaPlayer=new MediaPlayer();
        try {
            mediaPlayer.setDataSource(dataSet.get(listPosition).getAudio());
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                }

            });
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }



        audio_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mediaPlayer.isPlaying()) {
                    audio_play.setImageResource(R.drawable.play);
                    mediaPlayer.pause();
                }else {
                    audio_play.setImageResource(R.drawable.stop);
                    mediaPlayer.start();
                }



            }

        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Are you sure you want to cancel this request?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                a=new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {
                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                    snapshot.getRef().removeValue();
                                                }


                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                };
                                ref.child("Full_Location_Information").orderByChild("address").equalTo(dataSet.get(listPosition).getAddress()).addValueEventListener(a);
                            }

                        }) .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
        };
        });







        final Intent intent=new Intent(activity, LocationGeofence.class);;

        relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new Intent(activity, BookingActivity.class);
                intent.putExtra("state_name", dataSet.get(listPosition).getPlace());
                intent.putExtra("state_address", dataSet.get(listPosition).getAddress());
                intent.putExtra("lat", dataSet.get(listPosition).getLatitude());
                intent.putExtra("long", dataSet.get(listPosition).getLongitude());
                activity.startActivity(intent);


            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }



}


