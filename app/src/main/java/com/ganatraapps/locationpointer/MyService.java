    package com.ganatraapps.locationpointer;

    import android.app.Notification;
    import android.app.NotificationManager;
    import android.app.PendingIntent;
    import android.app.Service;
    import android.content.Context;
    import android.content.Intent;
    import android.hardware.Sensor;
    import android.hardware.SensorEvent;
    import android.hardware.SensorEventListener;
    import android.hardware.SensorManager;
    import android.os.Handler;
    import android.os.IBinder;
    import android.support.v4.app.NotificationCompat;

    public class MyService extends Service implements SensorEventListener{
//    private SensorManager mSensorManager;
//    private Sensor mAccelerometer;
    private float mAcceleration; // acceleration apart from gravity
    private float mAccelerationCurrent; // current acceleration including gravity
//    private float mAccelerationLast; // last acceleration including gravity

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI, new Handler());
        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float mAccelerationLast = mAccelerationCurrent;
        mAccelerationCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
        float delta = mAccelerationCurrent - mAccelerationLast;
        mAcceleration = mAcceleration * 0.9f + delta; // perform low-cut filter

        if (mAcceleration > 11) {
            showNotification();
        }
    }

    /**
     * show notification when Accel is more then the given int.
     */
    private void showNotification() {
        final NotificationManager mgr = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder note = new NotificationCompat.Builder(this);
        note.setContentTitle("Location Detected");
        note.setContentText("Click to see your current location");
        note.setTicker("Location Detected!");
        note.setAutoCancel(true);
        // to set default sound/light/vibrate or all
        note.setDefaults(Notification.DEFAULT_ALL);
        // Icon to be set on Notification
        note.setSmallIcon(R.mipmap.ic_launcher);
        // This pending intent will open after notification click
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this,
                MapsActivity.class), 0);
        // set pending intent to notification builder
        note.setContentIntent(pi);
        mgr.notify(101, note.build());
    }
}
