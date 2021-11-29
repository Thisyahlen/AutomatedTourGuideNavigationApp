package com.example.automatedtourguidesystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class AddAdminActivity extends AppCompatActivity {

    private Intent intent;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);
        mStorage= FirebaseStorage.getInstance().getReference("uploads");

        Button audio = findViewById(R.id.Audio);
        Button location = findViewById(R.id.Location);

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OpenDialog();
            }
        });



        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(AddAdminActivity.this, AddLocationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void openFileChooser() {
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,100);
    }

    private void OpenDialog() {
        LinearLayout layout = new LinearLayout(AddAdminActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));

        AlertDialog.Builder builder = new AlertDialog.Builder(AddAdminActivity.this);
        final  AlertDialog a = builder.setItems(new String[] { "Record Audio", "Select Audio" }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    intent = new Intent(AddAdminActivity.this, AddAudioActivity.class);
                    startActivity(intent);
                }
                else{
                    openFileChooser();
                }

            }
        } ).setTitle("Add Tour Guide Audio").setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        Log.e("1", "onHandleIntent "+ data);

        if(requestCode==100 && resultCode == RESULT_OK ){
            final Uri uri = data.getData();

            String filename = String.valueOf(uri);



            StorageReference filepath = mStorage.child("Audio").child("external_storage").child(filename);



            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    intent = new Intent(AddAdminActivity.this, AddLocationActivity.class);
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful()) ;
                    Uri downloadUrl = urlTask.getResult();
                    if (downloadUrl != null) {
                        String urlimage = downloadUrl.toString();
                        intent.putExtra("Audio_url", String.valueOf(urlimage));

                    }

                    Toast.makeText(getApplicationContext(), "Audio Upload Successfully.", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }
            });
            //the selected audio.Do some thing with uri


        }

        super.onActivityResult(requestCode, resultCode, data);
    }


}
