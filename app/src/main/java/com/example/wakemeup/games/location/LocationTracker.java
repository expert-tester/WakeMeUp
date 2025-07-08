package com.example.wakemeup.games.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wakemeup.games.common.BaseGameFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.example.wakemeup.R;

public class LocationTracker extends BaseGameFragment {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final double TARGET_DISTANCE_METERS = 20.0;

    private TextView tvDistanceMoved, tvStatus, tvTargetDistance;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private android.location.Location startLocation = null;
    private android.location.Location previousLocation = null;
    private double totalDistanceMoved = 0.0;
    private boolean isTracking = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDistanceMoved = view.findViewById(R.id.tvDistanceMoved);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvTargetDistance = view.findViewById(R.id.tvTargetDistance);
        tvTargetDistance.setText(String.format("%.1f m", TARGET_DISTANCE_METERS));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(300)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                for (android.location.Location location : locationResult.getLocations()) {
                    if (location != null) {
                        handleLocation(location);
                    }
                }
            }
        };

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void startLocationUpdates() {
        if (isTracking) return;

        totalDistanceMoved = 0.0;
        startLocation = null;
        previousLocation = null;
        isTracking = true;
        updateDistanceUI(0.0);
        updateStatusUI("Status: Tracking started...");

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationUpdates() {
        if (!isTracking) return;

        isTracking = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);
        updateStatusUI("Status: Tracking stopped.");
    }

    private void handleLocation(android.location.Location currentLocation) {
        if (!isTracking) return;

        if (startLocation == null) {
            if (currentLocation.hasAccuracy() && currentLocation.getAccuracy() > 15.0) {
                updateStatusUI("Status: Waiting for better initial accuracy...");
                return;
            }
            startLocation = currentLocation;
            previousLocation = currentLocation;
            updateStatusUI("Status: First location acquired.");
            updateDistanceUI(0.0);
            return;
        }

        if (currentLocation.hasAccuracy() && currentLocation.getAccuracy() > 10.0) {
            updateStatusUI("Status: Low accuracy... Please wait");
            return;
        }

        float[] result = new float[1];
        android.location.Location.distanceBetween(
                previousLocation.getLatitude(), previousLocation.getLongitude(),
                currentLocation.getLatitude(), currentLocation.getLongitude(),
                result
        );
        double distanceSegment = result[0];

//        final double MAX_PLAUSIBLE_SPEED_MPS = 7.0;
//        if (distanceSegment > MAX_PLAUSIBLE_SPEED_MPS) {
//            updateStatusUI("Status: Extreme distance detected... Ignoring");
//            return;
//        }

        final double STATIONARY_THRESHOLD = 0.5;
        if (distanceSegment < STATIONARY_THRESHOLD) {
            updateStatusUI("Status: Tiny movement... Keep moving");
            return;
        }

        totalDistanceMoved += distanceSegment;
        previousLocation = currentLocation;
        updateStatusUI("Status: Moving...");
        updateDistanceUI(totalDistanceMoved);

        if (totalDistanceMoved >= TARGET_DISTANCE_METERS) {
            updateStatusUI("Status: Target reached!");
            Toast.makeText(requireContext(), "🎉 Target distance of " + TARGET_DISTANCE_METERS + "m reached! 🎉", Toast.LENGTH_LONG).show();
            stopLocationUpdates();
        }
    }

    private void updateDistanceUI(double distance) {
        tvDistanceMoved.setText(String.format("%.1f m", distance));
    }

    private void updateStatusUI(String status) {
        tvStatus.setText(status);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(requireContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
                updateStatusUI("Status: Permission denied.");
            }
        }
    }
}