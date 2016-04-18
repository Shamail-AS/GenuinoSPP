package com.example.shamail.genuinospp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements Orientation.Listener {

    private Orientation orientation;
    private BluetoothConnection blueConnman;
    private FlightController flightController;

    public final static String THROTTLE_MESSAGE = "com.example.shamail.genuinospp.THROTTLE";


    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private boolean channelEnabled = true;

    public OutputStream outStream = null;
    public InputStream inStream = null;
    public InputStreamReader inReader = null;

    private TextView connStatus;
    private TextView sentStatus;
    private TextView receivedStatus;
    private TextView txtThrottle;
    private TextView txtRoll;
    private TextView txtPitch;
    private TextView txtYaw;
    private TextView txtMode;

    private TextView trim_txtRoll;
    private TextView trim_txtPitch;
    private TextView trim_txtYaw;

    public int roll_offset = 0;
    public int pitch_offset = 0;
    public int yaw_offset = 0;


    private boolean zeroOut = true;

    private static String genuinoAddress = "98:D3:31:80:81:82";

    private int _yaw = 0;
    private int _roll = 0;
    private int _pitch = 0;

    private SeekBar seek_trim_roll;
    private SeekBar seek_trim_pitch;
    private SeekBar seek_trim_yaw;
    private SeekBar throttle_slider;


    // Well known SPP UUID
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zeroOut = !zeroOut;
                txtMode.setEnabled(zeroOut);
            }
        });

        orientation = new Orientation((SensorManager) getSystemService(Activity.SENSOR_SERVICE),
                getWindow().getWindowManager());
        flightController = FlightController.getFlightController();
        //blueConnman = BluetoothConnection.getConnection();

        throttle_slider = (SeekBar) findViewById(R.id.seek_throttle);
        seek_trim_pitch = (SeekBar) findViewById(R.id.seek_trim_pitch);
        seek_trim_roll = (SeekBar) findViewById(R.id.seek_trim_roll);
        seek_trim_yaw = (SeekBar) findViewById(R.id.seek_trim_yaw);
        throttle_slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.throttle = 800+progress;
                sendOrientations();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seek_trim_pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                flightController.pitch_trim = progress - 30;
                sendOrientations();
                setTrimSummary();
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
                sendOrientations();
                setTrimSummary();
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
                sendOrientations();
                setTrimSummary();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Button btnRight = (Button)findViewById(R.id.btn_right);
        //Button btnLeft = (Button)findViewById(R.id.btn_left);
        Button btnTune = (Button)findViewById(R.id.btn_tune);
        Button btnCallibrate = (Button)findViewById(R.id.btn_call);


       /* btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    _yaw = 20;
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    _yaw = 0;
                    return true;
                }
                return false;
            }
        });
        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() ==MotionEvent.ACTION_DOWN) {
                    _yaw = -20;
                    return true;

                }
                if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    _yaw = 0;
                    return true;
                }
                return false;
            }
        });
        */
        btnTune.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DisplayTrimActivity.class);
                startActivity(intent);
            }
        });
        btnCallibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roll_offset = _roll;
                pitch_offset = _pitch;
                yaw_offset = _yaw;
            }
        });





        connStatus = (TextView)findViewById(R.id.txt_conn_status);
        sentStatus = (TextView)findViewById(R.id.txt_sent);
        receivedStatus = (TextView)findViewById(R.id.txt_recieved);

        txtMode = (TextView)findViewById(R.id.txt_mode);
        txtThrottle = (TextView)findViewById(R.id.txt_throttle);
        txtRoll = (TextView)findViewById(R.id.txt_roll);
        txtPitch = (TextView)findViewById(R.id.txt_pitch);
        txtYaw = (TextView)findViewById(R.id.txt_yaw);

        trim_txtRoll = (TextView)findViewById(R.id.txt_trim_roll);
        trim_txtPitch = (TextView)findViewById(R.id.txt_trim_pitch);
        trim_txtYaw = (TextView)findViewById(R.id.txt_trim_yaw);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        CheckBTState();


    }

    private void setTrimSummary()
    {
        //trim_txtPitch.setText(flightController.pitch_trim);
        //trim_txtRoll.setText(flightController.roll_trim);
        //trim_txtYaw.setText(flightController.yaw_trim);
    }

    private boolean CheckBTState() {
        if (btAdapter == null){
            AlertBox("Fatal Error","The bt Adapter not there. Bluetooth unsupported or something");
            return false;
        }
        else
        {
            if(btAdapter.isEnabled())
            {
                connStatus.setText("Blutooth enabled");
                return true;
            }
            else
            {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
            }
            return true;
        }
    }

    public void AlertBox( String title, String message ){
        new AlertDialog.Builder(this)
                .setTitle( title )
                .setMessage( message + " Press OK to exit." )
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                }).show();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        orientation.stopListening();
        sendMessage(BluetoothConnection.ORIENT_MSG_CODE + ":0:0:0:0;");
    }

    @Override
    public void onResume()
    {

        super.onResume();
        //setTrimSummary();



        if(!orientation.startListening(this))
        {
            AlertBox("FATAL","Sensors aren't available");
            connStatus.setText("NO SENSORS!");
        }
        if(!CheckBTState())
        {
            AlertBox("FATAL","BT isn't available");
            return;
        }

        connStatus.setText("Getting genuino from address");
        BluetoothDevice genuino = btAdapter.getRemoteDevice(genuinoAddress);
        if(genuino == null)
            return;

        try{
            connStatus.setText("trying to connect");
            btSocket = genuino.createRfcommSocketToServiceRecord(MY_UUID);

        } catch (IOException e) {
            AlertBox("Fatal","Could not create bt socket");
            connStatus.setText("Failed to create socket to the genuino :(");
            e.printStackTrace();
        }

        btAdapter.cancelDiscovery();

        try{
            connStatus.setText("Trying to bt COM port to Genuino");
            btSocket.connect();
            connStatus.setText("Connection established");
        } catch (IOException e) {
            //e.printStackTrace();
            try{
                btSocket.close();
            } catch (IOException e1) {
                AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e1.getMessage() + ".");
            }
        }

        try{
            outStream = btSocket.getOutputStream();
            inStream = btSocket.getInputStream();
            inReader = new InputStreamReader(inStream);
            connStatus.setText("Channel is Open!!");
            //blueConnman.setStreams(outStream,inStream,inReader);


        } catch (IOException e) {
            //e.printStackTrace();
            AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }


        sendOrientations();

    }

    public boolean sendMessage(String msg)
    {
        if(!channelEnabled){
            return readMessage();
        }

        try{
            sentStatus.setText(msg);
           outStream.write(msg.getBytes());
            channelEnabled = false;
            connStatus.setText("Conn is disabled. Waiting for 'O'");
            return true;
        } catch (IOException e) {
            //e.printStackTrace();
            sentStatus.setText("SEND ERROR:" + e.getMessage());
            return false;
        }
    }
    public boolean readMessage()
    {
        try{
            if(inReader.ready())
            {
                char input = (char)inReader.read();
                if( input == 'O'){
                    channelEnabled = true;
                    connStatus.setText("Conn is open. 'O' Received");
                }
                receivedStatus.setText("INPUT :: "+input);
            }
            return true;

        } catch (IOException e) {
            sentStatus.setText("READ ERROR:" + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            ConnectToDevice(genuinoAddress);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnOrientationChanged(float yaw, float pitch, float roll) {
        //_yaw = Math.round(yaw*100)/100;

        _roll = Math.round(roll*75)/100 - roll_offset ;
        _pitch = Math.round(pitch*75)/100 - pitch_offset ;
        if(_roll > 30 ) _roll = 30;
        if(_roll < -30) _roll = -30;

        if(_pitch > 30) _pitch = 30;
        if(_pitch < -30) _pitch = -30;

        txtThrottle.setText(String.valueOf(flightController.throttle));
        txtRoll.setText(String.valueOf(_roll + flightController.roll_trim));
        txtPitch.setText(String.valueOf(_pitch + flightController.pitch_trim));
        txtYaw.setText(String.valueOf(_yaw + flightController.yaw_trim));

        sendOrientations();
    }

    private void sendOrientations(){

        int final_yaw = flightController.yaw_trim;
        int final_roll = flightController.roll_trim;
        int final_pitch = flightController.pitch_trim;

        if(!zeroOut) {
            final_pitch += _pitch;
            final_roll += _roll;
            final_yaw += _yaw;
        }

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(BluetoothConnection.ORIENT_MSG_CODE);
        strBuilder.append(':');
        strBuilder.append(final_yaw);
        strBuilder.append(':');
        strBuilder.append(final_pitch);
        strBuilder.append(':');
        strBuilder.append(final_roll);
        strBuilder.append(':');
        strBuilder.append(flightController.throttle);
        strBuilder.append(";");
        if(!zeroOut)
            sendMessage(strBuilder.toString());
        else
            sendMessage(BluetoothConnection.ORIENT_MSG_CODE+":0:0:0:"+flightController.throttle+";");
    }

    private boolean ConnectToDevice(String deviceAddress)
    {
        connStatus.setText("Getting genuino from address");
        BluetoothDevice genuino = btAdapter.getRemoteDevice(deviceAddress);
        if(genuino == null)
            return false;

        try{
            connStatus.setText("trying to connect");
            btSocket = genuino.createRfcommSocketToServiceRecord(MY_UUID);

        } catch (IOException e) {
            AlertBox("Fatal","Could not create bt socket");
            connStatus.setText("Failed to create socket to the genuino :(");
            e.printStackTrace();
        }

        btAdapter.cancelDiscovery();

        try{
            connStatus.setText("Trying to bt COM port to Genuino");
            btSocket.connect();
            connStatus.setText("Connection established");
        } catch (IOException e) {
            //e.printStackTrace();
            try{
                btSocket.close();
            } catch (IOException e1) {
                AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e1.getMessage() + ".");
            }
        }

        try{
            outStream = btSocket.getOutputStream();
            inStream = btSocket.getInputStream();
            inReader = new InputStreamReader(inStream);
            connStatus.setText("Channel is Open!!");
            return true;

        } catch (IOException e) {
            //e.printStackTrace();
            AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }

        return false;
    }
}
