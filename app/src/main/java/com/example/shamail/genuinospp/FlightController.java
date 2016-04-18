package com.example.shamail.genuinospp;

/**
 * Created by Shamail on 4/14/2016.
 */
public class FlightController {

    public int roll_offset = 0;
    public int pitch_offset = 0;
    public int yaw_offset = 0;

    public int roll_trim = 0;
    public int pitch_trim = 0;
    public int yaw_trim = 0;

    public int roll_Kp = 0;
    public float roll_Kd = 0;
    public float roll_Ki = 0;

    public int pitch_Kp = 0;
    public float pitch_Kd = 0;
    public float pitch_Ki = 0;

    public int yaw_Kp = 0;
    public float yaw_Kd = 0;
    public float yaw_Ki = 0;

    public int throttle = 0;

    private static FlightController _controller = null;
    public static FlightController getFlightController()
    {
        if(_controller == null)
            _controller = new FlightController();
        return _controller;
    }


}
