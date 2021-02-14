package com.maps_sanjayprajapati_c0788252;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.maps_sanjayprajapati_c0788252.Util.generateCustomMarkerWithText;
import static com.maps_sanjayprajapati_c0788252.Util.roundDecimal;


public final class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerDragListener {
    private GoogleMap mGoogleMap;
    private final HashMap<String, LatLng> mMarkerMap = new HashMap<>();

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;

    private static final float INITIAL_ZOOM = 3.0F;
    private static final int PERMISSIONS_REQUEST_LOCATION = 100;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initComponents();
    }

    private void initComponents() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
                getLastKnownLocation();
            } else {
                checkLocationPermission();
            }
        } else {
            getLastKnownLocation();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint({"MissingPermission"})
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastKnownLocation = location;
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this).
                        setTitle(R.string.loc_permission_title).
                        setMessage(R.string.loc_permission_body).
                        setPositiveButton(R.string.ok, (dialogInterface, i) ->
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION))
                        .create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
            }
        }

    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length != 0 && grantResults[0] == 0) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
                    getLastKnownLocation();
                }
            } else {
                Toast.makeText(this, getString(R.string.loc_permssion_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint({"PotentialBehaviorOverride"})
    public void onMapReady(@NotNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        LatLng canada = new LatLng(52.856388, -104.610001);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(canada, INITIAL_ZOOM));
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnPolylineClickListener(this);
        mGoogleMap.setOnPolygonClickListener(this);
        mGoogleMap.setOnMarkerClickListener(this);
        mGoogleMap.setOnMapLongClickListener(this);
        mGoogleMap.setOnMarkerDragListener(this);
    }

    public void onMapClick(@Nullable LatLng point) {
        if (mMarkerMap.size() < 4 && point != null) {
            String nameString = "ABCD";
            String name = String.valueOf(nameString.charAt(mMarkerMap.size()));
            Marker marker = mGoogleMap.addMarker((new MarkerOptions()).position(point).draggable(true).icon(BitmapDescriptorFactory.fromBitmap(generateCustomMarkerWithText(this, name))));
            marker.setTag(name);
            if (lastKnownLocation != null) {
                double computeDistanceBetween = SphericalUtil.computeDistanceBetween(point, new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                marker.setTitle("Distance: " + roundDecimal(computeDistanceBetween / 1000) + "Km");
            }

            mMarkerMap.put(name, point);
            if (mMarkerMap.size() == 4) {
                drawOnMap();
            }
        }

    }

    private void drawOnMap() {

        mGoogleMap.addPolyline((new PolylineOptions()).clickable(true).color(Color.RED).
                add(mMarkerMap.get("A"), mMarkerMap.get("B")));
        mGoogleMap.addPolyline((new PolylineOptions()).clickable(true).color(Color.RED).
                add(mMarkerMap.get("B"), mMarkerMap.get("C")));
        mGoogleMap.addPolyline((new PolylineOptions()).clickable(true).color(Color.RED).
                add(mMarkerMap.get("C"), mMarkerMap.get("D")));
        mGoogleMap.addPolyline((new PolylineOptions()).clickable(true).color(Color.RED).
                add(mMarkerMap.get("D"), mMarkerMap.get("A")));


        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.add(mMarkerMap.get("A"),
                mMarkerMap.get("B"),
                mMarkerMap.get("C"),
                mMarkerMap.get("D"));
        polygonOptions.clickable(true);
        polygonOptions.fillColor(Color.parseColor("#5900FF00"));
        mGoogleMap.addPolygon(polygonOptions);
    }

    public void onPolylineClick(@NotNull Polyline polyline) {
        double distanceBetweenCitiesInKm = SphericalUtil.computeLength(polyline.getPoints()) / (double) 1000;
        new AlertDialog.Builder(this)
                .setMessage("Distance: " + roundDecimal(distanceBetweenCitiesInKm) + "Km")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public boolean onMarkerClick(@NotNull Marker marker) {
        Geocoder geocode = new Geocoder(this);
        try {
            List<Address> addressesList = geocode.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            if (!addressesList.isEmpty()) {
                Toast.makeText(this, addressesList.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        marker.showInfoWindow();
        return true;
    }

    public void onMapLongClick(@NotNull LatLng position) {
        if (mMarkerMap.size() >= 4) {
            List<LatLng> positions = new ArrayList<>();
            positions.add(mMarkerMap.get("A"));
            positions.add(mMarkerMap.get("B"));
            positions.add(mMarkerMap.get("C"));
            positions.add(mMarkerMap.get("D"));
            if (PolyUtil.containsLocation(position, positions, true)) {
                mGoogleMap.clear();
                mMarkerMap.clear();
            }
        }

    }

    public void onPolygonClick(@Nullable Polygon polygon) {
        double aToB = SphericalUtil.computeDistanceBetween(mMarkerMap.get("A"), mMarkerMap.get("B"));
        double bToC = SphericalUtil.computeDistanceBetween(mMarkerMap.get("B"), mMarkerMap.get("C"));
        double cToD = SphericalUtil.computeDistanceBetween(mMarkerMap.get("C"), mMarkerMap.get("D"));
        double totalInKm = (aToB + bToC + cToD) / (double) 1000L;

        new AlertDialog.Builder(this)
                .setMessage("Total distance: " + roundDecimal(totalInKm) + "Km")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public void onMarkerDragStart(@Nullable Marker marker) {
    }

    public void onMarkerDrag(@Nullable Marker marker) {
    }

    public void onMarkerDragEnd(@NotNull Marker marker) {
        String name = marker.getTag().toString();
        LatLng position = marker.getPosition();
        mMarkerMap.put(name, position);
        if (mMarkerMap.size() == 4) {
            mGoogleMap.clear();

            for (String key : mMarkerMap.keySet()) {
                mMarkerMap.get(key);
                Marker mkr = mGoogleMap.addMarker((new MarkerOptions()).position(mMarkerMap.get(key)).
                        draggable(true).icon(BitmapDescriptorFactory.fromBitmap(generateCustomMarkerWithText(this, key))));
                mkr.setTag(key);
                if (lastKnownLocation != null) {
                    double computeDistanceBetween = SphericalUtil.computeDistanceBetween(mMarkerMap.get(key), new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                    mkr.setTitle("Distance: " + roundDecimal(computeDistanceBetween / 1000) + "Km");
                }
            }
            drawOnMap();
        }

    }


}
