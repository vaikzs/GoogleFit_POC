package com.example.pa_vsridha.sensorapp;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.pa_vsridha.sensorapp.logger.Log;
import com.example.pa_vsridha.sensorapp.logger.LogView;
import com.example.pa_vsridha.sensorapp.logger.LogWrapper;
import com.example.pa_vsridha.sensorapp.logger.MessageOnlyLogFilter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.SensorsApi;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_ENABLE_BT = 1001;
    private BluetoothAdapter mBluetoothAdapter;
    /*
        ProgressBar
     */
    ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss";
    private static final int REQUEST_BLUETOOTH = 1001;
    public static final String TAG = "SensorApp";
    private static final int REQUEST_OAUTH = 1;
    private boolean authInProgress = false;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    private OnDataPointListener mListener;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client = null;

    // [END mListener_variable_reference]
    private static final DataType[] myDataTypes = {
            DataType.TYPE_LOCATION_SAMPLE,
            DataType.TYPE_HEART_RATE_BPM,
            DataType.TYPE_STEP_COUNT_CUMULATIVE,
            DataType.TYPE_STEP_COUNT_DELTA,
            DataType.TYPE_DISTANCE_CUMULATIVE,
            DataType.TYPE_DISTANCE_DELTA,
            DataType.TYPE_ACTIVITY_SAMPLE
    };
    private static OnDataPointListener mLocationListener;
    private static OnDataPointListener mHeartRateBpmListener;
    private static OnDataPointListener mStepCountCumulativeListener;
    private static OnDataPointListener mStepCountDeltaListener;
    private static OnDataPointListener mDistanceCumulativeListener;
    private static OnDataPointListener mDistanceDeltaListener;
    private static OnDataPointListener mActivitySampleListener;
    private static final OnDataPointListener[] mDataSourceListeners = {
            mLocationListener,
            mHeartRateBpmListener,
            mStepCountCumulativeListener,
            mStepCountDeltaListener,
            mDistanceCumulativeListener,
            mDistanceDeltaListener,
            mActivitySampleListener
    };
    private BlueToothDevicesManager mBleDevicesManager;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LogView logView;
    private Context activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//
//
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
//        initializeLogging();

        // When permissions are revoked the app is restarted so onCreate is sufficient to check for
        // permissions core to the Activity's functionality.
        Log.i("Checking for Permission request", "Over here");
        if (!(checkPermissions(Manifest.permission.BODY_SENSORS) && checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermissions(Manifest.permission.BLUETOOTH) && checkPermissions(Manifest.permission.BLUETOOTH_ADMIN)))
            requestPermissions();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        buildFitnessClient();

    }


    private void populateUserProfile(Person.Name username, String email, Person.Image image, int gender) {

        TextView user = (TextView) findViewById(R.id.userTextView);
        TextView emailId = (TextView) findViewById(R.id.emailTextView);
        ImageView imgUrl = (ImageView) findViewById(R.id.imageView);

        emailId.setText(email);


    }


    /**
     * Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     * to connect to Fitness APIs. The scopes included should match the scopes your app needs
     * (see documentation for details). Authentication will occasionally fail intentionally,
     * and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     * can address. Examples of this include the user never having signed in before, or having
     * multiple accounts on the device and needing to specify which account to use, etc.
     */
    private void buildFitnessClient() {
        if (client == null && (checkPermissions(Manifest.permission.BODY_SENSORS) && checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermissions(Manifest.permission.BLUETOOTH) && checkPermissions(Manifest.permission.BLUETOOTH_ADMIN))) {


            client = new GoogleApiClient.Builder(this)
                    .addApi(Fitness.SENSORS_API)
                    .addApi(Fitness.BLE_API)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.SESSIONS_API)
                    .addApi(Fitness.CONFIG_API)
                    .addApi(Fitness.RECORDING_API)
                    .addApi(Plus.API)
                    .addScope(Fitness.SCOPE_LOCATION_READ_WRITE)
                    .addScope(Fitness.SCOPE_BODY_READ_WRITE)
                    .addScope(Fitness.SCOPE_ACTIVITY_READ_WRITE)
                    .addScope(Fitness.SCOPE_NUTRITION_READ_WRITE)


                    .addConnectionCallbacks(

                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.
                                    String accountName = Plus.AccountApi.getAccountName(client).toString();
                                    Log.i(TAG, accountName);

                                    populateUserProfile(null, accountName, null, 0);



                                    /*
                                                                        For Recording data
                                                                         */
//                                    subscribe();
                                    readHistoricData(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA);

                                    final Button steps = (Button) findViewById(R.id.step_btn);
                                    steps.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
//                                            logView.setText("");
                                            readHistoricData(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA);

                                        }
                                    });
                                    final Button activity = (Button) findViewById(R.id.act_btn);
                                    activity.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
//                                            logView.setText("");
                                            readHistoricData(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY);
                                        }
                                    });
                                    final Button distance = (Button) findViewById(R.id.dis_btn);
                                    distance.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
//                                            logView.setText("");
                                            readHistoricData(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA);
                                        }
                                    });
                                    final Button calories = (Button) findViewById(R.id.cal_btn);
                                    calories.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
//                                            logView.setText("");
                                            readHistoricData(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED);
                                        }
                                    });


                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i(TAG,
                                                "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.i(TAG, "Google Play services connection failed. Cause: " +
                                    result.toString());
                            Snackbar.make(
                                    Main2Activity.this.findViewById(R.id.main_content_view),
                                    result.getErrorCode() + ": Exception while connecting to Google Play services, might be because you didn't allow access",
                                    Snackbar.LENGTH_INDEFINITE).show();
                            Intent restartActivity = new Intent(Main2Activity.this, Main2Activity.class);
                            startActivity(restartActivity);
                        }
                    })
                    .build();
        }
        mBleDevicesManager = new BlueToothDevicesManager(this, client);


    }

    private void readHistoricData(DataType dataType, DataType aDataType) {
        // Begin by creating the query.
        DataReadRequest readRequest = queryFitnessData(dataType, aDataType);

        // [START read_dataset]
        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        Fitness.HistoryApi.readData(client, readRequest).setResultCallback(new com.google.android.gms.common.api.ResultCallback<DataReadResult>() {
            @Override
            public void onResult(DataReadResult dataReadResult) {
                Status status = dataReadResult.getStatus();
                if (status.isSuccess()) {

                    printData(dataReadResult);
                }
            }
        });


//        printData(dataReadResult);

    }

    /**
     * Return a {@link DataReadRequest} for all step count changes in the past week.
     */
    public static DataReadRequest queryFitnessData(DataType dt, DataType adt) {
        // [START build_read_data_request]
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DATE, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(dt, adt)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        // [END build_read_data_request]

        return readRequest;
    }

    /**
     * Log a record of the query result. It's possible to get more constrained data sets by
     * specifying a data source or data type, but for demonstrative purposes here's how one would
     * dump all the data. In this sample, logging also prints to the device screen, so we can see
     * what the query returns, but your app should not log fitness information as a privacy
     * consideration. A better option would be to dump the data you receive to a local data
     * directory to avoid exposing it to other applications.
     */
    public void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(TAG, "Number of returned buckets of DataSets are: "
                    + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);

//                    for (DataPoint eachDp : dataSet.getDataPoints())
//                        Log.i(TAG, eachDp.toString());


                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets are: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        } else {
            Log.i(TAG, "HAHA");
        }
        // [END parse_read_data_result]
    }


    // [START parse_dataset]
    private void dumpDataSet(DataSet dataSet) {
//        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();
        SimpleDateFormat smdf = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
        smdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        Log.i(TAG, dataSet.getDataType().getName().toString());
        if (dataSet.getDataType().getName().equals("com.google.step_count.delta")) {
            for (DataPoint dp : dataSet.getDataPoints()) {
                Log.i(TAG, "Data point:");
                Log.i(TAG, "\tType: " + dp.getDataType().getName());
                Log.i(TAG, "\tStart: " + smdf.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                Log.i(TAG, "\tEnd: " + smdf.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                for (Field field : dp.getDataType().getFields()) {
                    Log.i(TAG, "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));
                    TextView textViewTitle = (TextView) findViewById(R.id.textView);
                    textViewTitle.setText("Steps");
                    final TextView tvSteps = (TextView) findViewById(R.id.textSensorParameterValue);
                    tvSteps.setText(dp.getValue(field).toString() + " count");
                    ValueAnimator animator = new ValueAnimator();
                    animator.setObjectValues(0, Integer.parseInt(dp.getValue(field).toString()));
                    animator.setDuration(5000);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            tvSteps.setText(String.valueOf(animation.getAnimatedValue()));
                        }
                    });
                    animator.start();
                }
            }
        } else if (dataSet.getDataType().getName().equals("com.google.distance.delta")) {
            for (DataPoint dp : dataSet.getDataPoints()) {
                Log.i(TAG, "Data point:");
                Log.i(TAG, "\tType: " + dp.getDataType().getName());
                Log.i(TAG, "\tStart: " + smdf.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                Log.i(TAG, "\tEnd: " + smdf.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                for (Field field : dp.getDataType().getFields()) {
                    Log.i(TAG, "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));

                    TextView textViewTitle = (TextView) findViewById(R.id.textView);
                    textViewTitle.setText("Distance");
                    final TextView tvSteps = (TextView) findViewById(R.id.textSensorParameterValue);
                    tvSteps.setText(dp.getValue(field).toString() + " m");


                }
            }
        } else if (dataSet.getDataType().getName().equals("com.google.calories.expended")) {
            for (DataPoint dp : dataSet.getDataPoints()) {
                Log.i(TAG, "Data point:");
                Log.i(TAG, "\tType: " + dp.getDataType().getName());
                Log.i(TAG, "\tStart: " + smdf.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                Log.i(TAG, "\tEnd: " + smdf.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                for (Field field : dp.getDataType().getFields()) {
                    Log.i(TAG, "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));
                    TextView textViewTitle = (TextView) findViewById(R.id.textView);
                    textViewTitle.setText("Calories");
                    final TextView tvSteps = (TextView) findViewById(R.id.textSensorParameterValue);
                    tvSteps.setText(dp.getValue(field).toString() + " kcal");

                 
                }
            }
        } else {
            for (DataPoint dp : dataSet.getDataPoints()) {
                Log.i(TAG, "Data point:");
                Log.i(TAG, "\tType: " + dp.getDataType().getName());
                Log.i(TAG, "\tStart: " + smdf.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                Log.i(TAG, "\tEnd: " + smdf.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                for (Field field : dp.getDataType().getFields()) {
                    Log.i(TAG, "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));
                    TextView textViewTitle = (TextView) findViewById(R.id.textView);
                    textViewTitle.setText("Activity");
                    TextView tvSteps = (TextView) findViewById(R.id.textSensorParameterValue);
                    tvSteps.setText(dp.getValue(field).toString());


                }
            }
        }
    }

    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        // [START subscribe_to_datatype]
        Fitness.RecordingApi.subscribe(client, DataType.TYPE_ACTIVITY_SAMPLE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for activity detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed!");
                            }
                        } else {
                            Log.i(TAG, "There was a problem subscribing.");
                        }
                    }
                });
        // [END subscribe_to_datatype]
    }

    /**
     * Fetch a list of all active subscriptions and log it. Since the logger for this sample
     * also prints to the screen, we can see what is happening in this way.
     */
    private void dumpSubscriptionsList() {
        // [START list_current_subscriptions]
        Fitness.RecordingApi.listSubscriptions(client, DataType.TYPE_ACTIVITY_SAMPLE)
                // Create the callback to retrieve the list of subscriptions asynchronously.
                .setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
                    @Override
                    public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
                        for (Subscription sc : listSubscriptionsResult.getSubscriptions()) {
                            Log.i(TAG, sc.getDataSource().getName());
                            DataType dt = sc.getDataType();
                            Log.i(TAG, "Active subscription for data type: " + dt.getFields());

                            for (Field field : dt.getFields()) {
                                String val = field.getName();
                                Log.i(TAG, "Detected DataPoint field contents: " + field.describeContents());
                                Log.i(TAG, "Detected DataPoint field: " + field.getName());
                                Log.i(TAG, "Detected DataPoint value: " + val);


                            }
                        }

                    }
                });
        // [END list_current_subscriptions]
    }

    /**
     * Cancel the ACTIVITY_SAMPLE subscription by calling unsubscribe on that {@link DataType}.
     */
    private void cancelSubscription() {
        final String dataTypeStr = DataType.TYPE_ACTIVITY_SAMPLE.toString();
        Log.i(TAG, "Unsubscribing from data type: " + dataTypeStr);

        // Invoke the Recording API to unsubscribe from the data type and specify a callback that
        // will check the result.
        // [START unsubscribe_from_datatype]
        Fitness.RecordingApi.unsubscribe(client, DataType.TYPE_ACTIVITY_SAMPLE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Successfully unsubscribed for data type: " + dataTypeStr);
                        } else {
                            // Subscription not removed
                            Log.i(TAG, "Failed to unsubscribe for data type: " + dataTypeStr);
                        }
                    }
                });
        // [END unsubscribe_from_datatype]
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This ensures that if the user denies the permissions then uses Settings to re-enable
        // them, the app will start working.
        buildFitnessClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    /**
     * Find available data sources and attempt to register on a specific {@link DataType}.
     * If the application cares about a data type but doesn't care about the source of the data,
     * this can be skipped entirely, instead calling
     * {@link SensorsApi
     * #register(GoogleApiClient, SensorRequest, DataSourceListener)},
     * where the {@link SensorRequest} contains the desired data type.
     */
    private int findFitnessDataSources() {
        // [START find_data_sources]
        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.
        Fitness.SensorsApi.findDataSources(client, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                .setDataTypes(DataType.TYPE_ACTIVITY_SAMPLE, DataType.TYPE_STEP_COUNT_CADENCE, DataType.TYPE_WEIGHT, DataType.TYPE_HEIGHT, DataType.TYPE_CALORIES_EXPENDED, DataType.TYPE_LOCATION_SAMPLE)
                // Can specify whether data type is raw or derived.
                .setDataSourceTypes(DataSource.TYPE_RAW)

                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    public String[] myDataset;


                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        Log.i(TAG, "Result: " + dataSourcesResult.getStatus().toString());
                        Log.i(TAG, "Number of data sources: " + dataSourcesResult.getDataSources().size());
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {

                            Log.i(TAG, "Data source found: " + dataSource.getDevice().toString());
                            Log.i(TAG, "Data Source type: " + dataSource.getDataType().getName());

                            if (dataSource.getDataType().equals(DataType.TYPE_LOCATION_SAMPLE) && mLocationListener == null) {
                                Log.i(TAG, "TYPE_LOCATION_SAMPLE");
                                createDataListener(mLocationListener, dataSource, DataType.TYPE_LOCATION_SAMPLE);
                            }
                            if (dataSource.getDataType().equals(DataType.TYPE_HEART_RATE_BPM) && mHeartRateBpmListener == null) {
                                Log.i(TAG, "HEART RATE SAMPLE");
                                createDataListener(mHeartRateBpmListener, dataSource, DataType.TYPE_HEART_RATE_BPM);
                            }
                            if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_CUMULATIVE) && mStepCountCumulativeListener == null) {
                                Log.i(TAG, "TYPE_STEP_COUNT_CUMULATIVE");
                                createDataListener(mStepCountCumulativeListener, dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                            }
                            if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA) && mStepCountDeltaListener == null) {
                                Log.i(TAG, "TYPE_STEP_COUNT_DELTA");
                                createDataListener(mStepCountDeltaListener, dataSource, DataType.TYPE_STEP_COUNT_DELTA);
                            }
                            if (dataSource.getDataType().equals(DataType.TYPE_DISTANCE_CUMULATIVE) && mDistanceCumulativeListener == null) {
                                Log.i(TAG, "TYPE_DISTANCE_CUMULATIVE");
                                createDataListener(mDistanceCumulativeListener, dataSource, DataType.TYPE_DISTANCE_CUMULATIVE);
                            }
                            if (dataSource.getDataType().equals(DataType.TYPE_DISTANCE_DELTA) && mDistanceDeltaListener == null) {
                                Log.i(TAG, "TYPE_DISTANCE_DELTA");
                                createDataListener(mDistanceDeltaListener, dataSource, DataType.TYPE_DISTANCE_DELTA);
                            }
                            if (dataSource.getDataType().equals(DataType.TYPE_ACTIVITY_SAMPLE) && mActivitySampleListener == null) {
                                Log.i(TAG, "TYPE_ACTIVITY_SAMPLE");
                                createDataListener(mActivitySampleListener, dataSource, DataType.TYPE_ACTIVITY_SAMPLE);
                            }
                        }


                    }
                });
        // [END find_data_sources]
        return 100;
    }

    private void createDataListener(OnDataPointListener listener, DataSource dataSource, DataType dataType) {
        listener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {

                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.i(TAG, "Detected DataPoint field contents: " + field.describeContents());
                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
                    Log.i(TAG, "Detected DataPoint value: " + val);
                    Log.i(TAG, "Detected DataPoint source: " + dataPoint.getDataSource());
                    Log.i(TAG, "Detected DataPoint original source: " + dataPoint.getOriginalDataSource());
                    Log.i(TAG, "Timestamp" + dataPoint.getTimestamp(TimeUnit.DAYS));
                }
            }
        };
        registerFitnessDataListener(dataSource, dataType, listener);
    }

    private void unRegisterFitnessDataListeners() {
        for (OnDataPointListener listener : mDataSourceListeners) {
            if (listener != null) {
                unregisterFitnessDataListener();
            }
        }
    }

//    private void unRegisterListener(final OnDataPointListener listener) {
//        Fitness.SensorsApi.unregister(client, listener)
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(Status status) {
//                        if (status.isSuccess()) {
//                            Log.i(TAG, listener + " listener was removed!");
//                        } else {
//                            Log.i(TAG, listener + " listener was not removed.");
//                        }
//                    }
//                });
//    }

    /**
     * Initialize a custom log class that outputs both to in-app targets and logcat.
     */
    private void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);
        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);
        // On screen logging via a customized TextView.
//        logView = (LogView) findViewById(R.id.sample_logview_nav);

        // Fixing this lint errors adds logic without benefit.
        //noinspection AndroidLintDeprecation
        logView.setTextAppearance(this, R.style.Log);

        logView.setBackgroundColor(Color.WHITE);
        msgFilter.setNext(logView);
        Log.i(TAG, "Ready");
    }

    /**
     * Register a listener with the Sensors API for the provided {@link DataSource} and
     * {@link DataType} combo.
     */
    private void registerFitnessDataListener(DataSource dataSource, DataType dataType, final OnDataPointListener listener) {

        // [START register_data_listener]
        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
                    Log.i(TAG, "Detected DataPoint value: " + val);
                }

                Snackbar.make(
                        findViewById(R.id.main_content_view),
                        "Listening to data points...",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        })
                        .show();
            }
        };

        Fitness.SensorsApi.add(
                client,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .build(),
                listener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener registered!" + status.getStatusMessage());


                        } else {
                            Log.i(TAG, "Listener not registered.");
                        }
                    }
                });

        // [END register_data_listener]
        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.


    }

    /**
     * Unregister the listener with the Sensors API.
     */
    private void unregisterFitnessDataListener() {
        if (mListener == null) {
            // This code only activates one listener at a time.  If there's no listener, there's
            // nothing to unregister.
            return;
        }

        // [START unregister_data_listener]
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.SensorsApi.remove(
                client,
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener was removed!");
                        } else {
                            Log.i(TAG, "Listener was not removed.");
                        }
                    }
                });
        // [END unregister_data_listener]
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            dumpSubscriptionsList();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions(String permission) {

        int permissionState = ActivityCompat.checkSelfPermission(this,
                permission);

        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        boolean shouldProvideRationale1 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.BODY_SENSORS);
        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.BLUETOOTH);
        boolean shouldProvideRationale3 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.BLUETOOTH_ADMIN);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale && shouldProvideRationale1 && shouldProvideRationale2 && shouldProvideRationale3) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.main_content_view),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(Main2Activity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(Main2Activity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                if (!client.isConnecting() && !client.isConnected()) {
                    client.connect();
                }
            }
        } else if (requestCode == REQUEST_BLUETOOTH) {

            mBleDevicesManager.startBleScan();
        }
    }

    private void progressBar(View v) {
// prepare for a progress bar dialog

        progressBar = new ProgressDialog(v.getContext());
        progressBar.setTitle("Loading data sources please wait....");
        progressBar.setProgressStyle(ProgressBar.FOCUS_RIGHT);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();

        //reset progress bar status
        progressBarStatus = 0;

        new Thread(new Runnable() {
            public void run() {
                while (progressBarStatus < 100) {

                    // process some tasks
                    progressBarStatus = findFitnessDataSources();

                    // your computer is too fast, sleep 1 second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Update the progress bar
                    progressBarHandler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressBarStatus);
                        }
                    });
                }

                // ok, file is downloaded,
                if (progressBarStatus >= 100) {

                    // sleep 2 seconds, so that you can see the 100%
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // close the progress bar dialog
                    progressBar.dismiss();
                }
            }
        }).start();

    }


    public void revokeAuthorization(final GoogleApiClient client) {


        PendingResult<Status> pendingResult = Fitness.ConfigApi.disableFit(client);
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.i(TAG, "Google Fit disabled");
                    client.reconnect();
                } else {
                    Log.e(TAG, "Google Fit wasn't disabled " + status);

                }
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_scan_ble) {
            // Initializes Bluetooth adapter.

            mBleDevicesManager.startBleScan();


        } else if (id == R.id.nav_revoke) {

            revokeAuthorization(client);


        } else {
            if (id == R.id.nav_watch) {
                findFitnessDataSources();

            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Context getActivity() {
        return activity;
    }
}
