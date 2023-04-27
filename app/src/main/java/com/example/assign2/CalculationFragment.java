package com.example.assign2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalculationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalculationFragment extends Fragment implements LocationListener, SensorEventListener {
    protected LocationManager locationManager;
    private double lat, longt;
    private final int LOCATION_REFRESH_TIME = 60000, LOCATION_REFRESH_DISTANCE = 0;
    private double acclx = 0, accly = 0, xdelta = 0, ydelta = 0;
    private SensorManager sensorManager_;
    private Sensor accelerometer_;
    private long gTimestamp = 0;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CalculationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalculationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalculationFragment newInstance(String param1, String param2) {
        CalculationFragment fragment = new CalculationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View temp = inflater.inflate(R.layout.fragment_calculation, container, false);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        sensorManager_ = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer_ = (Sensor) sensorManager_.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        return temp;
    }

    @Override
    public void onResume() {
        super.onResume();

        sensorManager_.registerListener(this, accelerometer_, SensorManager.SENSOR_DELAY_NORMAL);

        if ((ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        sensorManager_.unregisterListener(this,accelerometer_);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //  Every 60 seconds the location and, essentially, calibration is reset
        //  By updating the marker using the GPS
        lat = location.getLatitude();
        longt = location.getLongitude();

        // Create Bundle
        Bundle loc = new Bundle();
        loc.putDouble("lat", lat);
        loc.putDouble("longt", longt);
        getParentFragmentManager().setFragmentResult("coordinates", loc);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //  Marker is constantly updated by adding to the previous GPS location
        //  using calculations acquired from accelerometer data.
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // NOTE:    Device is always laying flat, faced up for this application, it never rotates either
            acclx = (double) event.values[0];
            accly = (double) event.values[1];

            //  Change in time acquire to determine distance travelled given accelerometer data
            if (gTimestamp <= 0) {
                gTimestamp = event.timestamp;
                return;
            }
            long dT = event.timestamp - gTimestamp;
            gTimestamp = event.timestamp;
            dT *= 0.000000001f; // Convert NanoS to S

            // Get distance moved in North/South and East/West directions in meters
            xdelta = acclx * (dT * dT);
            ydelta = accly * (dT * dT);

            // Send data to MapFragment
            Bundle XYdelta = new Bundle();
            XYdelta.putDouble("xdelta", xdelta);
            XYdelta.putDouble("ydelta", ydelta);
            getParentFragmentManager().setFragmentResult("distance", XYdelta);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}