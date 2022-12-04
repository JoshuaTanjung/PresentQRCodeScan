package com.qrcodescanner.skripsiapplicationpresensi;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    ZXingScannerView ScannerView;
    DatabaseReference dbref;

    String id_student = "";
    String name_student = "";
    String lat_student = "";
    String long_student = "";
    String distance_student = "";
    String residence_student = "";
    String location_sabbath_student = "";
    String worship_activity_student = "";
    Map<String,Object> taskMap;

    // get current date
    Date c = Calendar.getInstance().getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    String getCurrentDate = df.format(c);

    // get current time
    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get information from mainActivity
        Intent intent = getIntent();
         id_student = intent.getStringExtra("id-key-student");
         name_student = intent.getStringExtra("name-key-student");
         lat_student = intent.getStringExtra("lat-key-student");
         long_student = intent.getStringExtra("long-key-student");
         distance_student = intent.getStringExtra("distance-key-student");
         residence_student = intent.getStringExtra("residence-key-student");
         location_sabbath_student = intent.getStringExtra("location-sabbath-key-student");
         worship_activity_student = intent.getStringExtra("worship-activity-key-student");

        // create new parent and child of firebase
        dbref = FirebaseDatabase.getInstance().getReference().child("Absensi").child(""+getCurrentDate).child(""+id_student);
        taskMap = new HashMap<>();

        // scan
        ScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(ScannerView);                // Set the scanner view as the content view


        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        ScannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        ScannerView.startCamera();          // Start camera on resume
    }

    @Override
    protected void onPause() {
        super.onPause();
        ScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        String data_scanned = rawResult.getText().toString();

        // status scanner about residence
        Map<String,Object> taskMap = new HashMap<>();
        taskMap.put("status", ""+data_scanned);
        dbref.updateChildren(taskMap);

        // information after scan
        layout2.result.setText("Thank You Students");
        onBackPressed();

        taskMap.put("name", ""+name_student);
        taskMap.put("id", ""+id_student);
        taskMap.put("lat", ""+lat_student);
        taskMap.put("long", ""+long_student);
        taskMap.put("time", ""+currentTime);
        taskMap.put("date", ""+getCurrentDate);
        taskMap.put("distance", ""+distance_student);
        taskMap.put("residence", ""+residence_student);
        taskMap.put("place", ""+location_sabbath_student);
        taskMap.put("worship", ""+worship_activity_student);
        dbref.updateChildren(taskMap);

    }

}