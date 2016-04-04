package com.example.shamail.genuinospp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

/**
 * Created by Shamail on 3/13/2016.
 */
public class Orientation implements SensorEventListener {

    private int initZrotation = 0;
    private boolean initSet = false;
    private MadgwickAHRS filter;
    private int factor = 800;

    private float ax, ay,az;
    private float gx,gy,gz;

    public interface Listener {
        void OnOrientationChanged(float yaw, float pitch, float roll);
    }

    private static final int SENSOR_DELAY_MICROS = 50 * 1000; // 50ms

    private final SensorManager mSensorManager;
    private final Sensor mRotationSensor;
    private final Sensor mGyro;
    private final Sensor mAccel;
    private final WindowManager mWindowManager;

    private int mLastAccuracy;
    private Listener mListener;

    public Orientation(SensorManager sensorManager, WindowManager windowManager) {
        mSensorManager = sensorManager;
        mWindowManager = windowManager;

        // Can be null if the sensor hardware is not available
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        filter = new MadgwickAHRS(SENSOR_DELAY_MICROS,(float)Math.sqrt(3.0f / 4.0f));

    }


    public boolean startListening(Listener listener) {
        if (mListener == listener) {
            return true;
        }
        mListener = listener;
        if (mRotationSensor == null) {
            Log.w("Orientation","Rotation vector sensor not available; will not provide orientation data.");
            return false;
        }
        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY_MICROS);
        //mSensorManager.registerListener(this,mGyro,SENSOR_DELAY_MICROS);
        //mSensorManager.registerListener(this,mAccel,SENSOR_DELAY_MICROS);

        return true;
    }

    public void stopListening() {
        mSensorManager.unregisterListener(this);
        mListener = null;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener == null) {
            return;
        }
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            //return;
        }
        if (event.sensor == mRotationSensor) {
            updateOrientation(event.values);
        }
        if(event.sensor == mGyro)
        {
            Log.i("GYRO",String.valueOf(event.values[0]));
            gx = event.values[0];
            gy = event.values[1];
            gz = event.values[2];
        }
        if(event.sensor == mAccel)
        {
            Log.i("ACCEL",String.valueOf(event.values[0]));
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
        }
        //filter.update(gx/factor,gy/factor,gz/factor,ax/factor,ay/factor,az/factor);
        //updateOrientationAHRS();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }
    private void updateOrientationAHRS()
    {
        mListener.OnOrientationChanged(filter.getYaw(),filter.getPitch(),filter.getRoll());
    }

    private void updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        //float[] quaternions = new float[4];

        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
       // SensorManager.getQuaternionFromVector(quaternions,rotationVector);
        //Log.i("YAW",String.valueOf(rotationVector[2]));
        // Transform rotation matrix into azimuth/pitch/roll

        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientation);

        int mAzimuthAngleNotFlat = (int)Math.toDegrees(Math.atan((rotationMatrix[1] - rotationMatrix[3])/(rotationMatrix[0] + rotationMatrix[4])));
        //if (mAzimuthAngleNotFlat < 0) { mAzimuthAngleNotFlat += 360; }

        //float[] angles = toAngles(quaternions);

        if(!initSet)
        {
            initZrotation = mAzimuthAngleNotFlat;
            initSet = true;
        }
        // Convert radians to degrees..NOT. We're fine with radians
        //float yaw = (float)Math.toDegrees(orientation[0]);
        float yaw = (float)(mAzimuthAngleNotFlat - initZrotation);
        float pitch = (float)Math.toDegrees(orientation[1]);
        float roll = (float)Math.toDegrees(orientation[2]);
        //float roll = orientation[2] * -57;

       // Log.i("Quat",angles[0]+","+angles[2]+","+angles[1]);
        mListener.OnOrientationChanged(yaw,pitch,roll);
       // mListener.OnOrientationChanged(angles[0], angles[2], angles[1]);
    }

    public float[] toAngles(float[] quats) {

        float[] angles = new float[3];

         if (quats.length != 4) {
            throw new IllegalArgumentException("Angles array must have three elements");
        }
        float w = quats[0];
        float x = quats[1];
        float y = quats[2];
        float z = quats[3];

        float sqw = w * w;
        float sqx = x * x;
        float sqy = y * y;
        float sqz = z * z;
        float unit = sqx + sqy + sqz + sqw; // if normalized is one, otherwise
        // is correction factor
        float test = x * y + z * w;
        if (test > 0.499 * unit) { // singularity at north pole
            angles[1] = 2 * (float)Math.atan2(x, w);
            angles[2] = (float)(Math.PI/2);
            angles[0] = 0;
        } else if (test < -0.499 * unit) { // singularity at south pole
            angles[1] = -2 * (float)Math.atan2(x, w);
            angles[2] = -(float)(Math.PI/2);
            angles[0] = 0;
        } else {
            angles[1] = (float)Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw); // roll or heading
            angles[2] = (float)Math.asin(2 * test / unit); // pitch or attitude
            angles[0] = (float)Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw); // yaw or bank
        }
        return angles;
    }


}
