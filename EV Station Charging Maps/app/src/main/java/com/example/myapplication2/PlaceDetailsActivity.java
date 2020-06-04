package com.example.myapplication2;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import static java.lang.Math.sqrt;

public class PlaceDetailsActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {


    private String placeId = "";
    private String placeAddress;
    private String operational;
    private String website;
    private ImageView imageView2;
    private double placeLatitude;
    private double placeLongitude;
    private double userLatitude;
    private double userLongitude;
    private double distance;
    private String PlaceName;


    private TextView addressTextView;
    private TextView operationalTextView;
    private TextView websiteTextView;
    private TextView price;
    private TextView TimeOfCharge;
    private TextView distanceTextView;
    private String radioButtonChosen;


    private final String API_KEY = "AIzaSyBTQ3y2dE_ZT6riLUtJTU90MKQT8dv8Hrc";
    private final String REQUEST = "https://maps.googleapis.com/maps/api/place/details/json?key=" + API_KEY;
    private String isOpenNow = "Open/Closed: CLOSED";
    private TextView isOpenTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_place);

        if (!getIntent().hasExtra("placeid")) {
            finish();
        }

        addressTextView = findViewById(R.id.Address);
        operationalTextView = findViewById(R.id.Operational);
        websiteTextView = findViewById(R.id.Website);
        TimeOfCharge = findViewById(R.id.TimeOfCharge);
        isOpenTextView = findViewById(R.id.IsOpen);
        distanceTextView = findViewById(R.id.Distance);


        placeId = getIntent().getStringExtra("placeid");
        radioButtonChosen = getIntent().getStringExtra("RadioButtonChosen");
        placeLatitude = Double.parseDouble(getIntent().getStringExtra("placeLatitude"));
        placeLongitude = Double.parseDouble(getIntent().getStringExtra("placeLongitude"));
        userLatitude = Double.parseDouble(getIntent().getStringExtra("userLatitude"));
        userLongitude = Double.parseDouble(getIntent().getStringExtra("userLongitude"));

        String url = REQUEST + "&placeid=" + placeId;

        new PlaceDetailsAsync().execute(url);

        StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager().findFragmentById(R.id.streetviewpanorama);

        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.specific_place_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }


    private void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        streetViewPanorama.setPosition(new LatLng(placeLatitude, placeLongitude));
        streetViewPanorama.setPanningGesturesEnabled(true);
        streetViewPanorama.setStreetNamesEnabled(true);
        distance();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        distanceTextView.setText("Distance to station: " + decimalFormat.format(distance).toString() + " " + "mi.");


    }


    public void distance() {
        double deglen = 110.25;
        double x = Math.abs(userLatitude - placeLatitude);
        double y = Math.abs((userLongitude - placeLongitude) * Math.cos(placeLatitude));
        this.distance = deglen*sqrt(x*x + y*y) * 0.621371;
    }

    private class PlaceDetailsAsync extends AsyncTask<String, Integer, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                return new JSONObject(stringBuilder.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new JSONObject();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            try {
                JSONObject result = jsonObject.getJSONObject("result");
                setActionBarTitle(result.getString("name"));

                PlaceName = result.getString("name");

                //PlaceName = "Tesla Charging Station";

                String[] letterSections = PlaceName.split(" ");

                if (letterSections[0].equals("Tesla") || letterSections[0].equals("tesla")) {
                    if (radioButtonChosen.equals("0")) {
                        TimeOfCharge.setText("1 hr");
                    } else if (radioButtonChosen.equals("1")) {
                        TimeOfCharge.setText("40 min");
                    } else if (radioButtonChosen.equals("2")) {
                        TimeOfCharge.setText("Charger not Supported");
                    }
                } else {
                    if (radioButtonChosen.equals("0")) {
                        TimeOfCharge.setText("5 hrs");
                    }else if (radioButtonChosen.equals("1")) {
                        TimeOfCharge.setText("4 hrs");
                    } else if (radioButtonChosen.equals("2")) {
                        TimeOfCharge.setText("3 hrs");
                    }
                }

                if (result.has("formatted_address") && !result.getString("formatted_address").equals("")) {
                    placeAddress = result.getString("formatted_address");
                }
//                System.out.println("Address: " + placeAddress);
                //openNow = result.getBoolean("open_now");
                if (result.has("permanently_closed")) {
                    operational = "Status: Not Operational";
                } else {
                    operational = "Status: Operational";
                }
                if (result.has("website") && !result.getString("website").equals("")) {
                    website = result.getString("website");
                } else {
                    website = "Not listed";
                }
                if (result.has("opening_hours")) {
                    //System.out.println("Open now result " + result.getJSONArray("opening_hours"));
                    if (result.getJSONObject("opening_hours").getBoolean("open_now") == false) {
                        isOpenNow = "Open/Closed: CLOSED";
                    } else {
                        isOpenNow = "Open/Closed: OPEN";
                    }
                }

                addressTextView.setText(placeAddress);
                operationalTextView.setText(operational);
                websiteTextView.setText(website);
                isOpenTextView.setText(isOpenNow);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
