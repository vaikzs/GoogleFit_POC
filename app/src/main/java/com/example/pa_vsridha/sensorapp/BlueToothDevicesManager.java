package com.example.pa_vsridha.sensorapp;

import android.content.IntentSender.SendIntentException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.BleScanCallback;
import com.google.android.gms.fitness.request.StartBleScanRequest;

public class BlueToothDevicesManager {

    private static final String TAG = "SensorApp";
    private static final int REQUEST_BLUETOOTH = 1001;
    private Main2Activity mMonitor;
    private GoogleApiClient mClient;

    public BlueToothDevicesManager(Main2Activity monitor, GoogleApiClient client) {
        mMonitor = monitor;
        mClient = client;

    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case REQUEST_BLUETOOTH:
//                startBleScan();
//                break;
//
//        }
//    }


    public void startBleScan() {
        if (mClient.isConnected()) {
            Log.i(TAG, "Google account is connected");
        }
        BleScanCallback callback = new BleScanCallback() {
            @Override
            public void onDeviceFound(BleDevice device) {
                Log.i(TAG, "BLE Device Found: " + device.getName());
                claimDevice(device);
            }

            @Override
            public void onScanStopped() {

                Log.i(TAG, "BLE scan stopped");
            }
        };



        PendingResult result = Fitness.BleApi.startBleScan(mClient, new StartBleScanRequest.Builder()
                .setDataTypes(DataType.TYPE_ACTIVITY_SAMPLE, DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_WEIGHT, DataType.TYPE_HEIGHT, DataType.TYPE_CALORIES_EXPENDED, DataType.TYPE_LOCATION_SAMPLE).setBleScanCallback(callback)
                .build());

        result.setResultCallback(new ResultCallback() {
            @Override
            public void onResult(@NonNull Result result) {
                Status status = result.getStatus();
                if (!status.isSuccess()) {
                    String a = status.getStatusCode() + "";
                    Log.i(TAG, a);
                    switch (status.getStatusCode()) {

                        case FitnessStatusCodes.DISABLED_BLUETOOTH:
                            try {

                                status.startResolutionForResult(mMonitor, REQUEST_BLUETOOTH);

                            } catch (SendIntentException e) {
                                Log.i(TAG, "SendIntentException: " + e.getMessage());
                            }
                            break;
                    }
                    Log.i(TAG, "BLE scan unsuccessful");
                } else {
                    Log.i(TAG, "ble scan status message: " + status.getStatusMessage());
                    Log.i(TAG, "BLEHAHA scan successful: " + status.getResolution());


                }
            }
        });
    }

    public void claimDevice(BleDevice device) {
        //Stop ble scan

        //Claim device
        PendingResult<Status> pendingResult = Fitness.BleApi.claimBleDevice(mClient, device);
        pendingResult.setResultCallback(new ResultCallback<Status>() {

            @Override
            public void onResult(@NonNull Status st) {
                if (st.isSuccess()) {
                    Log.i(TAG, "Claimed device successfully");
                } else {
                    Log.e(TAG, "Did not successfully claim device");
                }
            }
        });
    }

}