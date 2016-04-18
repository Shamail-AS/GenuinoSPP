package com.example.shamail.genuinospp;

/**
 * Created by Shamail on 4/14/2016.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothConnection {


    public OutputStream outStream = null;
    public InputStream inStream = null;
    public InputStreamReader inReader = null;

    public BluetoothSocket btSocket = null;
    public BluetoothDevice btDevice = null;

    public final static int TRIM_MSG_CODE = 1;
    public final static int PID_MSG_CODE = 2;
    public final static int PID_YAW_MSG_CODE = 3;
    public final static int ORIENT_MSG_CODE = 1;



    private static BluetoothConnection _connection;
    public static BluetoothConnection getConnection()
    {
        if(_connection == null)
            _connection = new BluetoothConnection();
        return _connection;
    }

    public void setStreams(OutputStream out_, InputStream in_, InputStreamReader reader_)
    {
        outStream = out_;
        inStream = in_;
        inReader = reader_;
    }
    public String sendMessage(String message_) throws IOException
    {
        outStream.write(message_.getBytes());
        return "SENT";
    }
    public String readMessage() throws IOException
    {
        CharBuffer input = CharBuffer.allocate(18);
        if(inReader.ready())
        {
            inReader.read(input);
            return input.toString();
        }
        return ""; //reader wasn't ready
    }



}
