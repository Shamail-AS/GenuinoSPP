#include <CurieIMU.h>
#include <Servo.h>


int ax, ay, az;
int gx, gy, gz;

float yaw, pitch, roll, _yaw, _pitch, _roll;
float total_yaw, total_pitch, total_roll;
float yawTrim,pitchTrim,rollTrim;

int motorSpeed = 0;
Servo ESC9;
Servo ESC6;
Servo ESC5;
Servo ESC3;
const int ledPin = 13;


float pid_p_gain_roll=50;
float pid_i_gain_roll=0;float pid_i_mem_roll=0;
float pid_d_gain_roll=0;
int pid_max_roll=200;

float pid_p_gain_pitch=50;
float pid_i_gain_pitch=0;float pid_i_mem_pitch=0;
float pid_d_gain_pitch=0;
int pid_max_pitch=200;

float pid_p_gain_yaw=50;
float pid_i_gain_yaw=0.0;float pid_i_mem_yaw=0;
float pid_d_gain_yaw=0;
int pid_max_yaw=200;

float boost = 1;

float prev_error_roll,prev_error_pitch,prev_error_yaw = 0;
float prev_roll, prev_pitch, prev_yaw = 0;

//Compimentary filter//
float filter_error_roll=0;
float filter_error_pitch=0;
float filter_error_yaw=0;

float filter_gyro_roll = 0;
float filter_gyro_pitch = 0;
float filter_gyro_yaw = 0;
//======================//

// Moving average error smoother//
float mv_avg_roll[3];
float mv_avg_pitch[3];
float mv_avg_yaw[3];
//=============================

float pid_out_roll,pid_out_yaw,pid_out_pitch = 0;
int esc1,esc2,esc3,esc4 = 0;
int throttle=0;
//========================================================================//

long timestamp;


void setup() {
  Serial1.begin(57600);
  //Serial.begin(115200);
  pinMode(13,OUTPUT);
  digitalWrite(13,HIGH);
  ESC9.attach(9);
  long timestamp = micros() + 2000000; //5 secs
  while(micros() < timestamp)
  {
    ESC9.writeMicroseconds(0);
  }
  ESC6.attach(6); 
  timestamp = micros() + 2000000; //5 secs
  while(micros() < timestamp)
  {
    ESC6.writeMicroseconds(0);
  }
  ESC5.attach(5);
  timestamp = micros() + 2000000; //5 secs
  while(micros() < timestamp)
  {
    ESC5.writeMicroseconds(0);
  }
  ESC3.attach(3);
  timestamp = micros() + 2000000; //5 secs
  while(micros() < timestamp)
  {
    ESC3.writeMicroseconds(0);
  }
  digitalWrite(13,LOW);
  delayMicroseconds(200000);
  digitalWrite(13,HIGH);
  
  CurieIMU.begin();
  CurieIMU.autoCalibrateGyroOffset();
  delay(3000);
  CurieIMU.autoCalibrateAccelerometerOffset(X_AXIS, 0);
  CurieIMU.autoCalibrateAccelerometerOffset(Y_AXIS, 0);
  CurieIMU.autoCalibrateAccelerometerOffset(Z_AXIS, 1);
  delay(3000);
  CurieIMU.setGyroRate(400); // herts
  CurieIMU.setGyroRange(125); //degrees per sec
  CurieIMU.setAccelerometerRate(400);
  CurieIMU.setAccelerometerRange(2); //2 G we can use G values for our calcs
  digitalWrite(13,HIGH);
  Serial1.print("O");
  
  while(Serial1.available())
  {
    Serial1.read();
  }
  
  yawTrim=rollTrim=pitchTrim=0;
  _yaw =_pitch =_roll=0;

  getPosition();

  prev_roll = roll;
  prev_pitch = pitch;
  prev_yaw = yaw;

  digitalWrite(13,LOW);
  timestamp = micros();
}

// Loop function //

void loop() {
  
  if(Serial1.available())
  {
    //Serial1.print("OK");
    getData();
    Serial1.print("O");
  }
  
  //esc1 = esc2 = esc3 = esc4 = throttle;
  //printMotorOutputs();
  //printTabletOrientation();
  if(micros() >= timestamp)
  {
    PID(_roll*0.0174533,_pitch*0.0174533,_yaw*0.0174533); //convert to radians before sending   
    RunMotors();
    timestamp = micros() + 2000; //run program at 200 hertz

    getPosition();
    //printOrientation();
  }

}

void getPosition()
{
  CurieIMU.readMotionSensor(ax, ay, az, gx, gy, gz); 
  smoothGX();
  smoothGY();
  smoothGZ();

  float rate_yaw = convertRawGyro(gz)  ; //degree per sec;
  float rate_pitch = convertRawGyro(gx) ;
  float rate_roll = convertRawGyro(gy) ;
  //integrate
  roll += (rate_roll/400) ; // rate * time period OR (rate / freq)
  pitch += (rate_pitch/400) ;
  yaw += (rate_yaw/400);

  float forceMagApprox = abs(ax)+abs(ay)+abs(az);
  if(forceMagApprox > 8192 && forceMagApprox < 32768)
  {
    float acc_pitch = atan2((float)ax,(float)az); // radians around x axis
    pitch = pitch * 0.90 + acc_pitch * 0.10;
    float acc_roll = atan2((float)ay,(float)az); // around y axis
    roll = roll * 0.90 + acc_roll * 0.10;
  }
  /*
    float temp=0;
    temp=pitch;
    pitch=roll;
    roll=temp;
    */
}

float convertRawGyro(int gRaw) {
  // since we are using 250 degrees/seconds range
  // -250 maps to a raw value of -32768
  // +250 maps to a raw value of 32767

  float g = ((gRaw) * CurieIMU.getGyroRange()) / 32768.0;
  return g*0.0174533; //rad per sec
}

void RunMotors()
{
  if(esc1 > 1500) esc1 = 1500;
  if(esc2 > 1500) esc2 = 1500;
  if(esc3 > 1500) esc3 = 1500;
  if(esc4 > 1500) esc4 = 1500;

  ESC3.writeMicroseconds(esc1);
  ESC9.writeMicroseconds(esc2);
  ESC5.writeMicroseconds(esc3);
  ESC6.writeMicroseconds(esc4);
 
}

// PID Controller //

void PID(float set_point_roll, float set_point_pitch, float set_point_yaw)
{
  float error_roll=set_point_roll-roll;
  float error_yaw=set_point_yaw-yaw;
  float error_pitch=set_point_pitch-pitch;

  //printSmoothedErrors(error_roll, error_pitch, error_yaw);
  /*filter_error_roll = (filter_error_roll * 0.9)+(error_roll*0.1);
  filter_error_pitch =  (filter_error_pitch*0.9)+(error_pitch*0.1);
  filter_error_yaw = (filter_error_yaw*0.9)+(error_yaw*0.1);

  error_roll = filter_error_roll;
  error_pitch = filter_error_pitch;
  error_yaw = filter_error_yaw;
  */
  pid_i_mem_roll+=pid_i_gain_roll*error_roll;
  pid_i_mem_yaw+=pid_i_gain_yaw*error_yaw;
  pid_i_mem_pitch+=pid_i_gain_pitch*error_pitch;

  if(pid_i_mem_roll > pid_max_roll) pid_i_mem_roll = pid_max_roll;
  else if (pid_i_mem_roll < -pid_max_roll) pid_i_mem_roll = -pid_max_roll;

  if(pid_i_mem_yaw > pid_max_yaw) pid_i_mem_yaw = pid_max_yaw;
  else if (pid_i_mem_yaw < -pid_max_yaw) pid_i_mem_yaw = -pid_max_yaw;

  if(pid_i_mem_pitch > pid_max_pitch) pid_i_mem_pitch = pid_max_pitch;
  else if (pid_i_mem_pitch < -pid_max_pitch) pid_i_mem_pitch = -pid_max_pitch;

  float output_roll=(pid_p_gain_roll*error_roll + (pid_i_mem_roll) - (roll-prev_roll)*pid_d_gain_roll);
  float output_yaw=(pid_p_gain_yaw*error_yaw + (pid_i_mem_yaw) - (yaw-prev_yaw)*pid_d_gain_yaw);
  float output_pitch=(pid_p_gain_pitch*error_pitch + (pid_i_mem_pitch) - (pitch-prev_pitch)*pid_d_gain_pitch);

  output_roll *= boost;
  output_pitch *= boost;
  output_yaw *= boost;


  prev_error_roll= error_roll;
  prev_error_yaw= error_yaw;
  prev_error_pitch= error_pitch;

  prev_roll = roll;
  prev_pitch = pitch;
  prev_yaw = yaw;

  if(output_roll>pid_max_roll)
    output_roll=pid_max_roll;
  else if(output_roll<(-pid_max_roll*0.5))
    output_roll=-pid_max_roll;
  else 
    output_roll=output_roll;    

  if(output_yaw>pid_max_yaw)
    output_yaw=pid_max_yaw;
  else if(output_yaw<(-pid_max_yaw*0.5))
    output_yaw=-pid_max_yaw;
  else 
    output_yaw=output_yaw;    

  if(output_pitch>pid_max_pitch)
    output_pitch=pid_max_pitch;
  else if(output_pitch<(-pid_max_pitch*0.5))
    output_pitch=-pid_max_pitch;
  else 
    output_pitch=output_pitch;    


  esc1=throttle+output_pitch+output_roll-output_yaw+118;//-pitchTrim-rollTrim+outputYaw; // ESC3 White wire - Front left - pin3
  esc2=throttle+output_pitch-output_roll+output_yaw+4;//+pitchTrim+rollTrim+outputYaw; // ESC9 Red wire - Front right - pin9

  esc3=throttle-output_pitch-output_roll-output_yaw+0;//+pitchTrim-rollTrim-outputYaw; // ESC5 Orange wire - Rear left - pin5
  esc4=throttle-output_pitch+output_roll+output_yaw+1;//-pitchTrim+rollTrim-outputYaw; // ESC6 Grey wire - Rear right - pin6




  if(throttle > 1100) //if in flight, don't let any motor turn off
  {
    if(esc1 < 1050) esc1 = 1050;
    if(esc2 < 1050) esc2 = 1050;
    if(esc3 < 1050) esc3 = 1050;
    if(esc4 < 1050) esc4 = 1050;
  }
  if(throttle < 1050)
  {
    esc1 = esc2 = esc3 = esc4 = throttle;
  }  
}

void getData()
{
  // read in the data from the bluetooth 
  // Read in as the expected pattern YAW : ROLL : PITCH : THROTTLE
  // if YAW is > 60, treat it as special commands


  char input[28+1]; //max msg format = 0:-0000:-0000:-0000:0000 + 4 extra
  byte size = readSerial(input);

  input[size] = 0;
  //Serial1.print("inside get DAta");
  
  char* data = strtok(input,":");
  //data=strtok(0,":");
  _yaw = atoi(data);       
  data = strtok(0,":");
  _pitch = atoi(data);
  data = strtok(0,":");
  _roll = atoi(data);
  data = strtok(0,":");
  throttle = atoi(data);
  //Serial1.print(throttle);
}


/*
void getData()
{
// read in the data from the bluetooth 
// Read in as the expected pattern YAW : ROLL : PITCH : THROTTLE
// if YAW is > 60, treat it as special commands


char input[28+1]; //max msg format = 0:-0000:-0000:-0000:0000 + 4 extra
byte size = readSerial(input);

input[size] = 0;

char* data = strtok(input,":");

switch(atoi(data)) //the first number indicates type of data - command code
{
case 1:
//set yaw, roll and pitch pid
data = strtok(0,":");
pid_p_gain_roll = pid_p_gain_pitch = pid_p_gain_yaw = atoi(data);
data = strtok(0,":");
pid_i_gain_roll = pid_i_gain_pitch = pid_i_gain_yaw = atoi(data)/1000;
data = strtok(0,":");
pid_d_gain_roll = pid_d_gain_pitch = pid_d_gain_yaw = atoi(data)/10;
break;

case 2:
//set yaw pid
data = strtok(0,":");
pid_p_gain_yaw=atoi(data);
data = strtok(0,":");
pid_i_gain_yaw=atoi(data)/1000;
data = strtok(0,":");
pid_d_gain_yaw=atoi(data)/10;
break;

case 3:
//set setpoint values
data = strtok(0,":");
_yaw = atoi(data);       
data = strtok(0,":");
_pitch = atoi(data);
data = strtok(0,":");
_roll = atoi(data);

break;

default:
data = strtok(0,":");
data = strtok(0,":");
data = strtok(0,":");
break;
}
data = strtok(0,":");
throttle = atoi(data);


}
*/
byte readSerial(char input[])
{
  //Serial1.print("inside read serial");
  char b = (char)Serial1.read();
  int i = 0;
  while(b != ';')
  {
    //Serial1.print(b);
    if(Serial1.available())
    {
      input[i++] = b;
      b = (char)Serial1.read();
      //Serial1.print('c'); 
    }    
  }
  //Serial1.print(input);
  //Serial1.print("input");
  while(Serial1.available())
  {
    Serial1.read();
  }
  return i;
}

float smoothGY()
{
  int i = 0;
  int acc_i = 0;
  for(i = 0; i < 2; i++)
  {
  mv_avg_roll[i] =  mv_avg_roll[i+1];
  }
  mv_avg_roll[2] = gy;

  float smooth_error = 0;
  for(i = 0; i < 3; i++)
  {
  acc_i += sq(i+1);
  smooth_error += sq(i+1)*mv_avg_roll[i];
  }
  gy = smooth_error/acc_i; 
  }
  float smoothGX()
  {
  int i = 0;
  int acc_i = 0;
  for(i = 0; i < 2; i++)
  {
  mv_avg_pitch[i] =  mv_avg_pitch[i+1];
  }
  mv_avg_pitch[2] = gx;

  float smooth_error = 0;
  for(i = 0; i < 3; i++)
  {
  acc_i += sq(i+1);
  smooth_error += sq(i+1)*mv_avg_pitch[i];
  }
  gx = smooth_error/acc_i;

  }
float smoothGZ()
{
int i = 0;
int acc_i = 0;
for(i = 0; i < 2; i++)
{
mv_avg_yaw[i] =  mv_avg_yaw[i+1];
}
mv_avg_yaw[2] = gz;

float smooth_error = 0;
for(i = 0; i < 2; i++)
{
acc_i += sq(i+1);
smooth_error += sq(i+1)*mv_avg_yaw[i];
}
gz = smooth_error/acc_i;

}

void printFilteredErrors()
{
Serial1.print(filter_error_roll); Serial1.print("\t");
Serial1.print(filter_error_yaw); Serial1.print("\t");
Serial1.println(filter_error_pitch);
}
void printSmoothedErrors(float error_roll, float error_pitch, float error_yaw)
{
Serial1.print(error_roll); Serial1.print("\t");
Serial1.print(error_yaw); Serial1.print("\t");
Serial1.println(error_pitch); 
}
void printSetPoints(float set_point_roll, float set_point_pitch, float set_point_yaw)
{
Serial1.print(set_point_roll); Serial1.print("\t");
Serial1.print(set_point_yaw); Serial1.print("\t");
Serial1.println(set_point_pitch);
}
void printOrientation()
{
//Serial.print(roll); Serial.print("\t");
//Serial.print(yaw); Serial.print("\t");
//Serial.println(pitch);

if (Serial.available() > 0) {
int val = Serial.read();
if(val == 's')
{
Serial.print(yaw);
Serial.print(",");
Serial.print(pitch);
Serial.print(",");
Serial.println(roll);
}
}

}
void printTabletOrientation()
{
Serial1.print(throttle); Serial1.print("\t");
Serial1.print(_roll); Serial1.print("\t");
Serial1.print(_yaw); Serial1.print("\t");
Serial1.println(_pitch);
}
void printPidOutputs(float output_roll, float output_yaw, float output_pitch)
{
Serial1.print(output_roll); Serial1.print("\t");
Serial1.print(output_yaw); Serial1.print("\t");
Serial1.println(output_pitch); 
}
void printMotorOutputs()
{
Serial1.print(esc1); Serial1.print("\t");
Serial1.print(esc2); Serial1.print("\t");
Serial1.print(esc3); Serial1.print("\t");
Serial1.println(esc4);
}

void noConnectionMode()
{
ESC9.writeMicroseconds(0);
ESC6.writeMicroseconds(0);
ESC5.writeMicroseconds(0);
ESC3.writeMicroseconds(0);
}


