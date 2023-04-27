package com.example.assign2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment {
    private LatLng marker = new LatLng(0, 0);
    private GoogleMap myMap;
    private final double latConv = 110574, longtConv = 111320;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            myMap = googleMap;

            /*
            LatLng sydney = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
             */
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);

        //return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("coordinates", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                double lat = result.getDouble("lat");
                double longt = result.getDouble("longt");
                marker = new LatLng(lat, longt);
                updateMap(marker);
            }
        });

        getParentFragmentManager().setFragmentResultListener("distance", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                //  Convert meter distance change, and update longt,lat
                //  Possibly mixed up axis IDK ! Difficult to test, these are the equations anyway...
                Log.d("YDELTA", String.valueOf(result.getDouble("ydelta")));
                Log.d("XDELTA", String.valueOf(result.getDouble("xdelta")));
                double lat =  (double) (result.getDouble("ydelta") / latConv) + marker.latitude;
                double longt = (double) (result.getDouble("xdelta") / (longtConv * Math.cos(Math.toRadians(lat)))) + marker.longitude;
                marker = new LatLng(lat, longt);

                // App does not like when both this onFragmentResult calls same updateMarker func.
                // updateMap(marker);
                // BRUTE FORCE SOLUTION:    rewrite code from function here and only call
                //                          the function in the other onFragmentResult
                if(myMap != null) {
                    myMap.clear();
                }
                Log.d("acclLATLONG", marker.toString());
                if(marker != null) {
                    myMap.addMarker(new MarkerOptions().position(marker).title("Current Location"));
                    myMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    public void updateMap(LatLng marker){
        if(myMap != null) {
            myMap.clear();
        }
        Log.d("LATLONG", marker.toString());
        if(marker != null) {
            myMap.addMarker(new MarkerOptions().position(marker).title("Current Location"));
            myMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        }
    }
}