package com.qrcodescanner.skripsiapplicationpresensi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class layout2 extends AppCompatActivity implements LocationListener{

    Button btnScan;
    public static TextView result;
    LocationManager locationManager;

    private FirebaseAuth mAuth;

    TextInputLayout textInputLayout;
    AutoCompleteTextView autoCompleteTextView;

    String locationText = "";
    String locationLatitude = "";
    String locationLongitude = "";

    // sebagai contoh maximal jarak atau radius adalah 20Meter
    int max_radius = 500;
    public double user_distance = 0;


    double pc_lat = 1.418755;
    double pc_long = 124.983861;

    // lat & long lokasi beanr
    double uic_lat = 1.419170;
    double uic_long = 124.984490;

    double fw_lat = 1.416299;
    double fw_long = 124.984046;

    double audit_lat = 1.418111;
    double audit_long = 124.984594;

    String location_sabbath = "";
    String worship_activity = "";

    // combobox untuk memilih keterangan tempat kita ibadah dan worship apa saja
    public String[] arr_tempat_ibadah = new String[]{"Pioneer Chapel", "Fern Wallace", "Auditorium", "UIC"};
    public String[] arr_worship_activity = new String[]{"Midweek", "Vesper", "Sabbath", "Adventist Youth Program"};


    private int mInterval = 3000; // 3 seconds by default, can be changed later
    private Handler mHandler;
    AlertDialog.Builder builder;

    // get current nama hari (days name)
    SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
    Date d = new Date();
    String dayOfTheWeek = sdf.format(d);

    // get current time (format: HH:mm:ss)
    String waktu_hour = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout2);

        // combobox for worship activity (Sabbath, Vesper, Midweek, Adventist Youth Program)
        ArrayAdapter<String> adapter_worship = new ArrayAdapter<>(this, R.layout.drop_down_item, arr_worship_activity);
        AutoCompleteTextView autoCompleteTextView_worship = findViewById(R.id.activity_worship);
        autoCompleteTextView_worship.setAdapter(adapter_worship);

        // combobox untuk tempat ibadah (UIC, Auditorium, Pioneer Chapel, Fern Wallaca)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.drop_down_item, arr_tempat_ibadah);
        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.filled_exposed);
        autoCompleteTextView.setAdapter(adapter);




        //code combobox for tempat ibadah (UIC, FW, PC, Auditorium)
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(layout2.this, autoCompleteTextView.getText().toString(), Toast.LENGTH_SHORT).show();
                location_sabbath = autoCompleteTextView.getText().toString();
            }
        });

        //code combobox for worship activity (Sabbath, Vesper, Midweek, Adventist Youth Program)
        autoCompleteTextView_worship.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(layout2.this, autoCompleteTextView_worship.getText().toString(), Toast.LENGTH_SHORT).show();
                worship_activity = autoCompleteTextView_worship.getText().toString();
            }
        });

        btnScan = findViewById(R.id.btn_scan);
        result = findViewById(R.id.result);

        FirebaseAuth.getInstance().signOut();


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get information from mainActivity
                Intent intent = getIntent();
                String str_user_id = intent.getStringExtra("id-key");
                String str_user_name = intent.getStringExtra("name-key");
                String str_user_residence = intent.getStringExtra("residence-key");

                //Toast.makeText(layout2.this, "user id: "+str_user_id, Toast.LENGTH_SHORT).show();

                getLocation();
                Toast.makeText(layout2.this, "Location Begin: "+locationText, Toast.LENGTH_SHORT).show();

                // Cek GPS location
                if(locationLatitude == "" && locationLongitude == ""){
                    onBackPressed();
                }else{
                    //user_distance = calculate_distance(Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude), pc_lat, pc_long);
                    Toast.makeText(layout2.this, "Distance: "+user_distance, Toast.LENGTH_SHORT).show();

                        // pop-up jika tidak memilih tempat ibadah atau worship activity
                    if(location_sabbath == ""){
                        displayDialog("Place of Worship", "Please, select your place of worship.");
                    } else if ( worship_activity == "") {
                        displayDialog("Worship Activity", "Please, select your current worship activity.");


                        // Pilihan tentang hari ibadah yang di tentukan dan waktu yang di tentukan
                    } else if ( worship_activity.equalsIgnoreCase("Midweek") && !dayOfTheWeek.equalsIgnoreCase("Wednesday")) {
                        displayDialog("This Day", "maaf, hari ini bukan hari rabu, jadi pilihan anda bukan Midweek, silahkan pilih Worship Activity yang lain.");

                    } else if (worship_activity.equalsIgnoreCase("Midweek") && (Integer.parseInt(waktu_hour) <= 18 || Integer.parseInt(waktu_hour) >= 20)) {
                        displayDialog("Waktu Absen", "Maaf ini bukan waktu pengambilan absen Midweek.");

                    } else if ( worship_activity.equalsIgnoreCase("Vesper") && !dayOfTheWeek.equalsIgnoreCase("Friday")) {
                        displayDialog("This Day", "maaf, hari ini bukan hari Jumat, jadi pilihan anda bukan Vesper, silahkan pilih Worship Activity yang lain.");

                    } else if (worship_activity.equalsIgnoreCase("Vesper") && (Integer.parseInt(waktu_hour) <= 18 || Integer.parseInt(waktu_hour) >= 20)) {
                        displayDialog("Waktu Absen", "Maaf ini bukan waktu pengambilan absen Vesper.");

                    } else if ( worship_activity.equalsIgnoreCase("Sabbath") && !dayOfTheWeek.equalsIgnoreCase("Saturday")) {
                        displayDialog("This Day", "maaf, hari ini bukan hari Sabtu, jadi pilihan anda bukan Sabbath pagi, silahkan pilih Worship Activity yang lain.");

                    } else if (worship_activity.equalsIgnoreCase("Sabbath") && (Integer.parseInt(waktu_hour) <= 8 || Integer.parseInt(waktu_hour) >= 10)) {
                        displayDialog("Waktu Absen", "Maaf ini bukan waktu pengambilan absen Sabbath.");

                    //} else if ( worship_activity.equalsIgnoreCase("Adventist Youth Program") && !dayOfTheWeek.equalsIgnoreCase("Saturday")) {
                    //    displayDialog("This Day", "maaf, hari ini bukan hari Sabtu, jadi pilihan anda bukan Sabbath sore, silahkan pilih Worship Activity yang lain.");

                    //} else if (worship_activity.equalsIgnoreCase("Adventist Youth Program") && (Integer.parseInt(waktu_hour) <= 5 || Integer.parseInt(waktu_hour) >= 6)) {
                    //    displayDialog("Waktu Absen", "Maaf ini bukan waktu pengambilan absen Sabbath.");


                    } else {
                        // Perbandingan Radius dan jarak kita berada
                        if (user_distance > max_radius) {
                            displayDialog("notification", "Sorry you are too far away to take this attendance.");

                        } else { // Import to scannerActivity untuk tampilkan ke realtime database firebase
                            Intent scanner_act = new Intent(getApplicationContext(), ScannerActivity.class);
                            scanner_act.putExtra("id-key-student", str_user_id);
                            scanner_act.putExtra("name-key-student", str_user_name);
                            scanner_act.putExtra("residence-key-student", str_user_residence);
                            scanner_act.putExtra("lat-key-student", locationLatitude);
                            scanner_act.putExtra("long-key-student", locationLongitude);
                            scanner_act.putExtra("distance-key-student", "" + user_distance);
                            scanner_act.putExtra("location-sabbath-key-student", "" + location_sabbath);
                            scanner_act.putExtra("worship-activity-key-student", "" + worship_activity);
                            startActivity(scanner_act);
                        }
                    }
                }

            }
        });

        // GPS permission
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, (LocationListener) this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        locationText = location.getLatitude() + ", " + location.getLongitude();
        locationLatitude = location.getLatitude() + "";
        locationLongitude = location.getLongitude() + "";

        // Calculate empat tempat ibadah
        if(location_sabbath.equalsIgnoreCase("Pioneer Chapel")){
            this.user_distance = calculate_distance(Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude), pc_lat, pc_long);

        } else if(location_sabbath.equalsIgnoreCase("Fern Wallace")){
            this.user_distance = calculate_distance(Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude), fw_lat, fw_long);

        } else if(location_sabbath.equalsIgnoreCase("Auditorium")) {
            this.user_distance = calculate_distance(Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude), audit_lat, audit_long);

        } else if(location_sabbath.equalsIgnoreCase("UIC")) {
            this.user_distance = calculate_distance(Double.parseDouble(locationLatitude), Double.parseDouble(locationLongitude), uic_lat, uic_long);

        }

        Toast.makeText(layout2.this, "Location After: "+locationText, Toast.LENGTH_SHORT).show();
        Toast.makeText(layout2.this, "Distance: "+user_distance, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(layout2.this, "Please Enable your GPS/Location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onBackPressed() {
        displayDialog("GPS/Location", "Please Activate your GPS/Location and wait for detect your location");

    }

    public void displayDialog(String title, String message){
        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(layout2.this);

        // Set the message show for the Alert time
        builder.setMessage(message);

        // Set Alert Title
        builder.setTitle(title);

        // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
        builder.setPositiveButton("Okay", (DialogInterface.OnClickListener) (dialog, which) -> {
            // When the user click yes button then app will close
            dialog.cancel();

        });

        // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
        //builder.setNegativeButton("Exit", (DialogInterface.OnClickListener) (dialog, which) -> {
            // If user click no then dialog box is canceled.
          //  finish();
        //});

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
    }

    // Code kalkulasi 2 jarak yang berbeda
    public static double distance(LatLng start, LatLng end){
        try {
            Location location1 = new Location("locationA");
            location1.setLatitude(start.latitude);
            location1.setLongitude(start.longitude);
            Location location2 = new Location("locationB");
            location2.setLatitude(end.latitude);
            location2.setLongitude(end.longitude);
            double distance = location1.distanceTo(location2);
            return distance;
        } catch (Exception e) {

            e.printStackTrace();

        }
        return 0;
    }

    private double calculate_distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        //convert dari km to m (Kilometer to Meter)
        int m = 1000;
        dist = dist*m;

        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
