package com.example.shamail.genuinospp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;

public class DisplayTrimActivity extends AppCompatActivity {

    private SeekBar seek_throttle;
    private SeekBar seek_roll_Kp;
    private SeekBar seek_roll_Kd;
    private SeekBar seek_roll_Ki;
    //private SeekBar seek_pitch_Kp;
    //private SeekBar seek_pitch_Kd;
    //private SeekBar seek_pitch_Ki;
    private SeekBar seek_yaw_Kp;
    private SeekBar seek_yaw_Kd;
    private SeekBar seek_yaw_Ki;
    private SeekBar seek_trim_roll;
    private SeekBar seek_trim_pitch;
    private SeekBar seek_trim_yaw;

    
    private boolean channelEnabled = true;

    private Button btn_trim_reset;

    private FlightController flightController;
    private BluetoothConnection blueConn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_trim);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        flightController = FlightController.getFlightController();
        blueConn = BluetoothConnection.getConnection();

        seek_throttle = (SeekBar) findViewById(R.id.seek_throttle);
        seek_roll_Kp = (SeekBar) findViewById(R.id.seek_roll_Kp);
        seek_roll_Kd = (SeekBar) findViewById(R.id.seek_roll_Kd);
        seek_roll_Ki = (SeekBar) findViewById(R.id.seek_roll_Ki);

        seek_yaw_Kp = (SeekBar) findViewById(R.id.seek_yaw_Kp);
        seek_yaw_Kd = (SeekBar) findViewById(R.id.seek_yaw_Kd);
        seek_yaw_Ki = (SeekBar) findViewById(R.id.seek_yaw_Ki);

        seek_trim_pitch = (SeekBar) findViewById(R.id.seek_trim_pitch);
        seek_trim_roll = (SeekBar) findViewById(R.id.seek_trim_roll);
        seek_trim_yaw = (SeekBar) findViewById(R.id.seek_trim_yaw);

        btn_trim_reset = (Button) findViewById(R.id.btn_rst_trims);


        seek_throttle.setProgress(flightController.throttle);
        seek_throttle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.throttle = progress;
                sendTrimsAndThrottle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_roll_Kd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.roll_Kd = (float)progress;
                flightController.pitch_Kd = flightController.roll_Kd;
                sendRollPitchPID();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_roll_Kp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.roll_Kp = progress;
                flightController.pitch_Kp = flightController.roll_Kp;
                sendRollPitchPID();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_roll_Ki.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.roll_Ki = (float)progress;
                flightController.pitch_Ki = flightController.roll_Ki;
                sendRollPitchPID();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seek_yaw_Kd.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.yaw_Kd = (float)progress;
                sendYawPID();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_yaw_Kp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.yaw_Kp = progress;
                sendYawPID();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_yaw_Ki.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.yaw_Ki = (float)progress;
                sendYawPID();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seek_trim_pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.pitch_trim = progress - 30;
                sendTrimsAndThrottle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_trim_roll.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.roll_trim = progress - 30;
                sendTrimsAndThrottle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seek_trim_yaw.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.yaw_trim = progress - 30;
                sendTrimsAndThrottle();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btn_trim_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTrims();
            }
        });
    }

    private void resetTrims()
    {
        flightController.yaw_trim = 0;
        flightController.roll_trim = 0;
        flightController.pitch_trim = 0;

        seek_trim_pitch.setProgress(30);
        seek_trim_roll.setProgress(30);
        seek_trim_yaw.setProgress(30);
    }

    private void sendTrimsAndThrottle()
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(BluetoothConnection.TRIM_MSG_CODE); //code for parsing trims
        strBuilder.append(':');
        strBuilder.append(flightController.yaw_trim);
        strBuilder.append(':');
        strBuilder.append(flightController.pitch_trim);
        strBuilder.append(':');
        strBuilder.append(flightController.roll_trim);
        strBuilder.append(':');
        strBuilder.append(flightController.throttle);
        strBuilder.append(";");

        sendMessage(strBuilder.toString());
    }

    private void sendRollPitchPID()
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(BluetoothConnection.PID_MSG_CODE); //code for parsing trims
        strBuilder.append(':');
        strBuilder.append(flightController.roll_Kp);
        strBuilder.append(':');
        strBuilder.append(flightController.roll_Kd);
        strBuilder.append(':');
        strBuilder.append(flightController.roll_Ki);
        strBuilder.append(':');
        strBuilder.append(flightController.throttle);
        strBuilder.append(";");

        sendMessage(strBuilder.toString());
    }

    private void sendYawPID()
    {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(BluetoothConnection.PID_YAW_MSG_CODE); //code for parsing trims
        strBuilder.append(':');
        strBuilder.append(flightController.yaw_Kp);
        strBuilder.append(':');
        strBuilder.append(flightController.yaw_Kd);
        strBuilder.append(':');
        strBuilder.append(flightController.yaw_Ki);
        strBuilder.append(':');
        strBuilder.append(flightController.throttle);
        strBuilder.append(";");

        sendMessage(strBuilder.toString());
    }

    public boolean sendMessage(String msg)
    {
        if(!channelEnabled){
            return readMessage();
        }

        try{

            //outStream.write(msg.getBytes());
            blueConn.sendMessage(msg);
            channelEnabled = false;

            return true;
        } catch (IOException e) {
            //e.printStackTrace();

            return false;
        }
    }
    public boolean readMessage()
    {
        try{
            String input = blueConn.readMessage();
            if( input.equals("O")){
                channelEnabled = true;

            }

            return true;

        } catch (IOException e) {

            return false;
        }
    }


}
