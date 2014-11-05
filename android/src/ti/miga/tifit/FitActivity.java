package ti.miga.tifit;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import java.util.HashMap;
import org.appcelerator.titanium.TiApplication;

import org.appcelerator.kroll.common.Log;
import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.os.Bundle;
import android.content.IntentSender;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.data.Bucket;
import java.util.List;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.request.DataReadRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import com.google.android.gms.fitness.data.DataSet;
import android.os.AsyncTask;

public class FitActivity extends Activity {


    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mClient = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

    }


    public void buildFitnessClient(final Activity act) {
        mClient = new GoogleApiClient.Builder(act).addApi(Fitness.API).addScope(new Scope(Scopes.FITNESS_LOCATION_READ)).addConnectionCallbacks(
        new GoogleApiClient.ConnectionCallbacks() {

            @Override
            public void onConnected(Bundle bundle) {
                Log.i("FIT", "Connected!!!");
                new getSteps().execute();
                new getHeart().execute();
            }

            @Override
            public void onConnectionSuspended(int i) {
                if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                    Log.i("FIT", "Connection lost.  Cause: Network Lost.");
                } else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                    Log.i("FIT", "Connection lost.  Reason: Service Disconnected");
                }
            }
        }
        ).addOnConnectionFailedListener(
        new GoogleApiClient.OnConnectionFailedListener() {
            // Called whenever the API client fails to connect.
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.i("FIT", "Connection failed. Cause: " + result.toString());
                if (!result.hasResolution()) {
                    Log.i("FIT","error");
                    return;
                }
                if (!authInProgress) {
                    try {
                        Log.i("FIT", "Attempting to resolve failed connection");
                        authInProgress = true;
                        result.startResolutionForResult(act, REQUEST_OAUTH);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e("FIT", "Exception while starting resolution activity", e);
                    }
                }
            }
        }
        ).build();

    }

    @Override
    protected void onStart() {
        Log.i("FIT", "Connecting...");
        mClient.connect();
    }
    @Override
    protected void onStop() {
        if (mClient.isConnected()) {
            mClient.disconnect();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    private class getSteps extends AsyncTask<Void, Void, Void> {
        // get step data
        protected Void doInBackground(Void ... params) {
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            long startTime = cal.getTimeInMillis();

            DataReadRequest readRequest = new DataReadRequest.Builder().aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA).bucketByTime(1, TimeUnit.DAYS).setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS).build();
            DataReadResult dataReadResult =Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            printData(dataReadResult);
            return null;
        }
    }

    private class getHeart extends AsyncTask<Void, Void, Void> {
        // get heart data
        protected Void doInBackground(Void ... params) {

            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            long startTime = cal.getTimeInMillis();

            DataReadRequest readRequest = new DataReadRequest.Builder().aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY).bucketByTime(1, TimeUnit.DAYS).setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS).build();
            DataReadResult dataReadResult =Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            printData(dataReadResult);
            return null;
        }
    }

    private void printData(DataReadResult dataReadResult) {
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }


    private void dumpDataSet(DataSet dataSet) {
        Log.i("FIT", "Data returned for Data type: " + dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        KrollDict data = new KrollDict();

        for (DataPoint dp : dataSet.getDataPoints()) {
            for(Field field : dp.getDataType().getFields()) {
                KrollDict d = new KrollDict();
                data.put("type",dp.getDataType().getName());
                data.put("start",dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                data.put("end",dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                data.put("field",field.getName());
                data.put("value", dp.getValue(field)+"");
                TiApplication.getInstance().fireAppEvent("received", data);
            }
        }


    }
}
