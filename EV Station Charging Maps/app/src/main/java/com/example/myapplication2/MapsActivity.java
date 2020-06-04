package com.example.myapplication2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationResultListener, PlaceSelectedListener {

    private GoogleMap googleMap;
    private EditText zipCodeEnter;
    private String keyword = "EV Charging Station";
    private int radius = 50000;
    private boolean zipCodeSearch = false;

    private String nextPageToken = "";
    private int requestCount = 0;
    private String ZIP_CODE_REQUEST = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    private String ZIP_CODE = "";
    private String PLACES_REQUEST = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=";
    private final String API_KEY = "AIzaSyBTQ3y2dE_ZT6riLUtJTU90MKQT8dv8Hrc";
    private final String DIALOG_PHOTO_REQUEST = "https://maps.googleapis.com/maps/api/place/photo?key=" + API_KEY;
    private final int REQUEST_LIMIT = 3;
    private final String REQUEST = "https://maps.googleapis.com/maps/api/place/details/json?key=" + API_KEY;

    //handle permission result
    private final int PERMISSION_REQUEST = 1000;

    private final int ACTIVITY_RQEUEST_CODE = 1000;
    //handle enable location result
    private final int LOCATION_REQUEST_CODE = 2000;


    private LocationHandler locationHandler;

    public Double latFromZip = 0.0;
    public Double lngFromZip = 0.0;

    private AsyncTask CurrentRunningTask = null;

    private List<GooglePlace> listPlaces = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    private List<String> placeIDList = new ArrayList<>();
    private List<String> photoURLList = new ArrayList<>();
    private List<String> addressList = new ArrayList<>();
    private List<String> operationalList = new ArrayList<>();
    private List<String> websiteList = new ArrayList<>();

    private String operational = null;
    private String website = null;
    private String photo = null;
    private String isOpenNow = "Open/Closed: CLOSED";

    private String radioButtonChosen;
    private double userLat;
    private double userlng;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationHandler = new LocationHandler(this, this, LOCATION_REQUEST_CODE, PERMISSION_REQUEST);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        radioButtonChosen = getIntent().getStringExtra("RadioButtonChosen");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //linearLayout.removeAllViews();
                //list = new ArrayList<>();
                if (markerList.contains(marker)) {
//                    linearLayout.setVisibility(View.VISIBLE);
//                    linearLayout.setBackgroundColor(Color.parseColor("#9B9A9A"));
//                    TextView textView = new TextView(linearLayout.getContext());
//                    textView.setText("Marker: " + String.valueOf(marker.getTitle()));
//                    linearLayout.addView(textView);

                    for (GooglePlace place: listPlaces) {
                        if (place.getLatLng().equals(marker.getPosition())) {
//                            placeId = place.getPlaceId();
//                            String url = REQUEST + "&placeid=" + placeId;
//
//                            new PlaceDetailsAsync().execute(url);
                            //new PlaceDetailsAsync().execute(url);
//                            new Handler().postDelayed(() -> {
//                                new PlaceDetailsAsync().execute(url);
//                            }, 0);
//                            StartPlaceSearch(place);
                            //MapsActivity.getPlace(place);

                            int index = listPlaces.indexOf(place);

                            if (index < addressList.size()) {
                                new PlaceInfoDialog(MapsActivity.this, place, addressList.get(index), operationalList.get(index), websiteList.get(index), photoURLList.get(index), isOpenNow, MapsActivity.this).showDialog();
                            }
                        }
                    }

                }
                return true;
            }
        });
        locationHandler.getUserLocation();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void getLocation(Location location) {
        googleMap.setMyLocationEnabled(true);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        userLat = location.getLatitude();
        userlng = location.getLongitude();
        MarkerOptions marker = new MarkerOptions().position(latLng).title("Current Location");
        googleMap.addMarker(marker);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));


        CurrentRunningTask = new PlaceRequest().execute(PLACES_REQUEST + API_KEY + "&radius=" + radius + "&location=" + latLng.latitude + "," + latLng.longitude + "&keyword=" + keyword);
    }

    @Override
    public void getPlace(GooglePlace googlePlace) {
        Intent intent = new Intent(this, PlaceDetailsActivity.class);
        intent.putExtra("placeid", googlePlace.getPlaceId());
        intent.putExtra("placeLatitude", googlePlace.getLatitudeString());
        intent.putExtra("placeLongitude", googlePlace.getLongitudeString());
        intent.putExtra("userLatitude", Double.toString(userLat));
        intent.putExtra("userLongitude", Double.toString(userlng));
        intent.putExtra("RadioButtonChosen", radioButtonChosen);
        startActivity(intent);
    }


    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            boolean isPermissionGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PermissionChecker.PERMISSION_GRANTED) {
                    isPermissionGranted = false;
                    break;
                }
            }
            if (isPermissionGranted){
                locationHandler.getUserLocation();
            }else{
                if (shouldShowRequestPermissionRationale(permissions[0]) && shouldShowRequestPermissionRationale(permissions[1])) {
                    locationHandler.getUserLocation();
                } else {
                    new AlertDialog.Builder(this).setTitle("Error").setMessage("Please go to settings page to enable location permission")
                            .setPositiveButton("Go to Settings", (dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .setCancelable(false)
                            .create()
                            .show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RQEUEST_CODE) {
            if (resultCode == RESULT_OK) {
                locationHandler.getUserLocation();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("Please enable location")
                        .setPositiveButton("Ok", (dialog, which) -> {
                            locationHandler.getUserLocation();
                            dialog.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false)
                        .create()
                        .show();
            }
        }
    }


        @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                zipCodeEnter = (EditText) findViewById(R.id.enterZipCode);
                ZIP_CODE = zipCodeEnter.getText().toString();
                //getLocationFromZipCode(zipCodeEnter.getText().toString());
                zipCodeSearch = true;
                googleMap.clear();
                //linearLayout.removeAllViews();
                markerList = new ArrayList<>();

                new Handler().postDelayed(() -> {
                    new PlaceRequest().execute(ZIP_CODE_REQUEST + ZIP_CODE + "&keyword=" + keyword + "&key=" + API_KEY);
                }, 0);

                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }


    private class PlaceRequest extends AsyncTask<String, Integer, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... params) {
            while (!this.isCancelled()) {
                try {
                    //System.out.println("0");
                    URL url = new URL(params[0]);

                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    //System.out.println("1");
                    httpURLConnection.setRequestMethod("GET");

                    //System.out.println("2");
                    httpURLConnection.connect();

                    //System.out.println("3");
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    //System.out.println("4");
                    while ((line = bufferedReader.readLine()) != null) {
                        //System.out.println("5");

                        //This prints the response from google
                        System.out.println(line);
                        stringBuilder.append(line);
                    }

                    //System.out.println("6");
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    //System.out.println("7");
                    if (jsonObject.has("next_page_token")) {
                        nextPageToken = jsonObject.getString("next_page_token");
                    } else {
                        nextPageToken = "";
                    }
                    //System.out.println(nextPageToken);
                    //System.out.println("8");
                    return jsonObject.getJSONArray("results");
                } catch (Exception e) {
                    System.out.println("Something bad Happened");
                    e.printStackTrace();
                }
                return new JSONArray();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            //progressBar.setVisibility(View.GONE);
            requestCount++;
            try {
                System.out.println("In OnPostExecute()");
                System.out.println(jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {

                    if(zipCodeSearch) {
                        System.out.println("Inside zip Search onPostExecute()");
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject location = jsonObject.getJSONObject("geometry").getJSONObject("location");
                        latFromZip = location.getDouble("lat");
                        lngFromZip = location.getDouble("lng");
                        //System.out.println("latFromZip = " + latFromZip + " " + "LngFromZip = " + lngFromZip);

                        LatLng latLng = new LatLng(latFromZip, lngFromZip);

                        //System.out.println("SHOULD BE PLACING THE MARKER");
//                        MarkerOptions marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//                        googleMap.addMarker(marker);
                        //System.out.println("UPDATING ZOOM");
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

                        new Handler().postDelayed(() -> {
                            new PlaceRequest().execute(PLACES_REQUEST + API_KEY + "&radius=" + 50000 + "&location=" + latFromZip + "," + lngFromZip + "&keyword=" + keyword);
                        }, 0);

                        zipCodeSearch = false;
                        //return;
                    } else {
                        System.out.println("Normal onPostExecute()");
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        JSONObject location = jsonObject.getJSONObject("geometry").getJSONObject("location");
                        String name = jsonObject.getString("name");
                        String placeId = jsonObject.getString("place_id");
                        LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                        System.out.println(latLng.toString());


                        GooglePlace googlePlace = new GooglePlace(name, placeId, latLng);
                        listPlaces.add(googlePlace);
                        placeIDList.add(placeId);

                        String url = REQUEST + "&placeid=" + placeId;

                        new PlaceDetailsAsync().execute(url);

//                        TextView textView = new TextView(linearLayout.getContext());
//                        textView.setText("TextView " + String.valueOf(i));
//                        linearLayout.addView(textView);

//                    MarkerClusterItem markerClusterItem = new MarkerClusterItem(latLng, name);
//                    clusterManager.addItem(markerClusterItem);

                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                        Marker marker = googleMap.addMarker(markerOptions);
                        marker.setTag(latLng);
                        marker.setTitle(name);
                        markerList.add(marker);
                        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }


                }
                //zipCodeSearch = false;
                //clusterManager.cluster();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (requestCount < REQUEST_LIMIT && !nextPageToken.equals("")) {
                //progressBar.setVisibility(View.VISIBLE);
                //System.out.println("Starting a new PLACE_REQUEST");
                String url = PLACES_REQUEST + API_KEY + "&pagetoken=" + nextPageToken;
                new Handler().postDelayed(() -> {
                    new PlaceRequest().execute(url);
                }, 2000);
            }
        }
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
                //setActionBarTitle(result.getString("name"));

                if (result.has("formatted_address") && !result.getString("formatted_address").equals("")) {
//                    placeAddress = "Address: " + result.getString("formatted_address");
                    addressList.add(result.getString("formatted_address"));
                }
//                System.out.println("Address: " + placeAddress);
                //openNow = result.getBoolean("open_now");
                if (result.has("permanently_closed")) {
                    operational = "Status: Not Operational";
                } else {
                    operational = "Status: Operational";
                }

                operationalList.add(operational);

                if (result.has("website") && !result.getString("website").equals("")) {
                    website = result.getString("website");
                } else {
                    website = "Not listed";
                }

                websiteList.add(website);


                ArrayList<String> listPhotos = getPhotos(result);
                if (listPhotos.isEmpty()) {
                    photo = null;
                } else {
                    photo = listPhotos.get(0);
                }

                photoURLList.add(photo);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private ArrayList<String> getPhotos(JSONObject jsonObject) {
            ArrayList<String> list = new ArrayList<>();
            try {
                if (jsonObject.has("photos")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("photos");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject json = jsonArray.getJSONObject(i);
                        int height = json.getInt("height");
                        int width = json.getInt("width");
                        String photoReference = json.getString("photo_reference");
                        String url = DIALOG_PHOTO_REQUEST + "&photoreference=" + photoReference + "&maxwidth=" + width + "&maxheight=" + height;
                        list.add(url);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }
    }
}
