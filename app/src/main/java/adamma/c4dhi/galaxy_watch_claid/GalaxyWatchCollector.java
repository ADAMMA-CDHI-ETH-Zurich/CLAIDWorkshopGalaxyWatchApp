package org.c4dhi.adamma.claid_workshop_galaxy_watch;

import java.util.List;
import java.util.Map;

import adamma.c4dhi.claid.Logger.Logger;
import adamma.c4dhi.claid.Module.Channel;
import adamma.c4dhi.claid.Module.Module;
import adamma.c4dhi.claid.Module.PropertyHelper.PropertyHelper;
import adamma.c4dhi.claid_platform_impl.CLAID;
import adamma.c4dhi.claid_sensor_data.AccelerationData;
import adamma.c4dhi.claid_sensor_data.AccelerationSample;
import adamma.c4dhi.claid_sensor_data.HeartRateData;
import adamma.c4dhi.claid_sensor_data.HeartRateSample;

import com.samsung.android.service.health.tracking.ConnectionListener;
import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.HealthTrackerException;
import com.samsung.android.service.health.tracking.HealthTrackingService;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.HealthTrackerType;
import com.samsung.android.service.health.tracking.data.ValueKey;


public class GalaxyWatchCollector extends Module
{
    private final String TAG = GalaxyWatchCollector.class.getSimpleName();



    private boolean enableAccelerometer;
    private boolean enableHeartRate;
    private boolean isHandlerRunning;
    private boolean healthServicesRunning = false;
    Channel<AccelerationData> accelerometerSampleChannel;
    Channel<HeartRateData> heartRateSampleChannel;

    private HealthTracker accTracker = null;
    private HealthTracker heartRateTracker = null;
    private HealthTrackingService healthTrackingService = null;
    private String accelerometerChannel = "";
    private String heartRateChannel = "";




    public void initialize(Map<String, String> propertiesMap) {
        Logger.logInfo("Initializing GalaxyWatchCollector 1 " + propertiesMap);

        PropertyHelper properties = new PropertyHelper(propertiesMap);

        this.enableAccelerometer = properties.getProperty("enableAccelerometer", Boolean.class);
        this.enableHeartRate = properties.getProperty("enableHeartRate", Boolean.class);
        this.accelerometerChannel = properties.getProperty("accelerometerChannel", String.class);
        this.heartRateChannel = properties.getProperty("heartRateChannel", String.class);


        if(properties.wasAnyPropertyUnknown())
        {
            String unknownProperties = properties.unknownPropertiesToString();

            moduleError("Missing properties: [" + unknownProperties +"]." +
                    "Please sepcify the properties in the configuration file.");
            return;
        }


        this.accelerometerSampleChannel = this.publish(this.accelerometerChannel, AccelerationData.class);
        this.heartRateSampleChannel = this.publish(this.heartRateChannel, HeartRateData.class);

        setUpSamsungHealthTrackingServices();

    }

    private final ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void onConnectionSuccess()
        {
            Logger.logInfo("Connected to HSP");

            if(isHandlerRunning) {
                return;
            }

            if(enableAccelerometer)
            {
                System.out.println("GalaxyWatchCollector: Accelerometer enabled");
                try {
                    accTracker = healthTrackingService.getHealthTracker(HealthTrackerType.ACCELEROMETER);
                    Logger.logInfo("Successfully requested accelerometer health tracker.");
                } catch (final IllegalArgumentException e) {
                    Logger.logInfo("Failed to acquire accelerometer health tracker " + e.getMessage());
                }

                accTracker.setEventListener(trackerEventListener);

            }

            if(enableHeartRate)
            {
                System.out.println("GalaxyWatchCollector: Heartrate enabled");
                try {
                    heartRateTracker = healthTrackingService.getHealthTracker(HealthTrackerType.HEART_RATE);
                    Logger.logInfo("Successfully requested heartrate health tracker.");
                } catch (final IllegalArgumentException e) {
                    Logger.logInfo("Failed to acquire heartrate health tracker " + e.getMessage());
                }


                heartRateTracker.setEventListener(heartRateEventListener);

            }
            isHandlerRunning = true;

        }

        @Override
        public void onConnectionEnded() {

        }

        @Override
        public void onConnectionFailed(HealthTrackerException e) {
            Logger.logError("Unable to connect to HSP");

        }
    };



    private final HealthTracker.TrackerEventListener trackerEventListener = new HealthTracker.TrackerEventListener() {
        @Override
        public void onDataReceived(List<DataPoint> list) {
            if (list.size() != 0)
            {

                CLAID.enableKeepAppAwake();


                Logger.logInfo( "List Size : "+list.size());
                int sumX = 0;
                int sumY = 0;
                int sumZ = 0;
                Logger.logInfo( "Accelerometer start");
                /*ArrayList<Double> xs = new ArrayList<>();
                ArrayList<Double> ys = new ArrayList<>();
                ArrayList<Double> zs = new ArrayList<>();
                ArrayList<Long> timestamps = new ArrayList<>();*/

                AccelerationData.Builder accelerationData = AccelerationData.newBuilder();
                for(DataPoint dataPoint : list)
                {
                    double X = dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X);
                    double Y = dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y);
                    double Z = dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z);
                    /*Logger.logInfo( "Timestamp : "+dataPoint.getTimestamp());
                    Logger.logInfo( "AccX : " +dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X));
                    Logger.logInfo( "AccY : " +dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y));
                    Logger.logInfo( "AccZ : " +dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z));
                    sumX += dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X);
                    sumY += dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y);
                    sumZ += dataPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z);*/

                    /*xs.add(X);
                    ys.add(Y);
                    zs.add(Z);
                    timestamps.add(dataPoint.getTimestamp());*/
                    long lastTimeStamp = dataPoint.getTimestamp();

                    AccelerationSample.Builder sample = AccelerationSample.newBuilder();
                    sample.setAccelerationX(X);
                    sample.setAccelerationY(Y);
                    sample.setAccelerationZ(Z);
                    sample.setUnixTimestampInMs(lastTimeStamp);
                    accelerationData.addSamples(sample.build());
                    //  sample.set_x(X);
                    //  sample.set_y(Y);
                    //   sample.set_z(Z);
                }

                accelerometerSampleChannel.post(accelerationData.build());

                CLAID.disableKeepAppAwake(500);
            }
        }

        @Override
        public void onFlushCompleted() {
            Logger.logInfo( " onFlushCompleted called");
        }

        @Override
        public void onError(HealthTracker.TrackerError trackerError) {
            Logger.logInfo( " onError called");
            if (trackerError == HealthTracker.TrackerError.PERMISSION_ERROR) {
                Logger.logInfo("GalaxyWatchColelctor Permissions Check Failed");
            }
            if (trackerError == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
                Logger.logInfo("GalaxyWatchColelctor SDK policy denied");

            }
            isHandlerRunning = false;
        }
    };



    private final HealthTracker.TrackerEventListener heartRateEventListener = new HealthTracker.TrackerEventListener() {
        @Override
        public void onDataReceived(List<DataPoint> list) {
            System.out.println("Heartrate tracker on data received.");

            if (list.size() != 0)
            {
                Logger.logInfo( "List Size : "+list.size());

                HeartRateData.Builder heartRateData = HeartRateData.newBuilder();
                for(DataPoint dataPoint : list)
                {
                    int hr = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE);
                    int hrIbi = dataPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_IBI);
                    int status = dataPoint.getValue(ValueKey.HeartRateSet.STATUS);


                    HeartRateSample.Builder sample = HeartRateSample.newBuilder();

                    sample.setHr(hr);
                    sample.setHrInterBeatInterval(hrIbi);
                    sample.setStatus(status);
                    sample.setUnixTimestampInMs(dataPoint.getTimestamp());
                    heartRateData.addSamples(sample.build());
                }
                heartRateSampleChannel.post(heartRateData.build());

            }
        }

        @Override
        public void onFlushCompleted() {
            Logger.logInfo( " onFlushCompleted called");
        }

        @Override
        public void onError(HealthTracker.TrackerError trackerError) {
            Logger.logInfo( " onError called");
            if (trackerError == HealthTracker.TrackerError.PERMISSION_ERROR) {
                Logger.logInfo("GalaxyWatchCollector Permissions Check Failed");
            }
            if (trackerError == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
                Logger.logInfo("GalaxyWatchColelctor SDK policy denied");

            }
            isHandlerRunning = false;
        }
    };




    public final void setUpSamsungHealthTrackingServices() {
        Logger.logInfo("Setting up samsung health services");
        healthTrackingService = new HealthTrackingService(connectionListener, CLAID.getContext());
        healthTrackingService.connectService();
        healthServicesRunning = true;
    }

    protected void cleanup()
    {
        if(accTracker != null) {
            accTracker.unsetEventListener();
        }

        if(heartRateTracker != null) {
            heartRateTracker.unsetEventListener();
        }

        isHandlerRunning = false;
        if(healthTrackingService != null) {
            healthTrackingService.disconnectService();
        }

        healthServicesRunning = false;
    }


}
