#include <Wire.h>
#include <ZX_Sensor.h>

// Constants
const int ZX_ADDR = 0x10;    // ZX Sensor I2C address
const int INT_PIN = 2;     // Pin to look for interrupt

// Global Variables
ZX_Sensor zx_sensor = ZX_Sensor(ZX_ADDR);
GestureType gesture;
uint8_t x_pos;
uint8_t z_pos;
uint8_t gesture_speed;

void setup() {
  
  uint8_t ver;

  // Initialize Serial port
  Serial.begin(9600);
  
  // Initialize ZX Sensor (configure I2C and read model ID)
  if ( zx_sensor.init() ) {
  
  //  Serial.println("ZX Sensor initialization complete");
  } else {
    Serial.println("Error -  ZX Sensor init!");
  }
  
  // Read the model version number and ensure the library will work
  ver = zx_sensor.getModelVersion();
  if ( ver == ZX_ERROR ) {
    Serial.println("Error version number");
  } else {
  }
  if ( ver != ZX_MODEL_VER ) {
    while(1);
  }
  
  ver = zx_sensor.getRegMapVersion();
  if ( ver == ZX_ERROR ) {
    Serial.println("Error reading register map version number");
  } else {
  }
  if ( ver != ZX_REG_MAP_VER ) {
    while(1);
  }
  
}

void loop() {
  
  if ( zx_sensor.gestureAvailable() ) {
    x_pos = zx_sensor.readX();
    z_pos = zx_sensor.readZ();
    gesture = zx_sensor.readGesture();
    gesture_speed = zx_sensor.readGestureSpeed();

    //gesture set left = 1 / right = 2 / up = 3 / down = 4
    switch ( gesture ) {

      case LEFT_SWIPE:
        Serial.println("1");
        break;
        
      case RIGHT_SWIPE:
        Serial.println("2");
        break;

      case UP_SWIPE:
        Serial.println("3");
        break;

      default:
            
          if( x_pos <= 150 && z_pos <= 50 )
            {
                Serial.println("4");
            }
        
        break;
    }
  }
}
