
const int ledPin = 9;      // the pin that the LED is attached to
const int green = 10;
const int yellow = 11;
const int red = 12;
bool R = false;
bool G = false;
bool B = false;

void setup() {
  // initialize the serial communication:
  Serial.begin(9600);
  // initialize the ledPin as an output:
  pinMode(green, OUTPUT);
  pinMode(yellow, OUTPUT);
  pinMode(red, OUTPUT);
  
}

void loop() {
  byte brightness;

  // check if data has been sent from the computer:
  if (Serial.available()) {
    // read the most recent byte (which will be from 0 to 255):
    brightness = Serial.read();
    Serial.println(brightness, DEC);
    if (brightness >= 100) {
      brightness = pow(brightness-100,2)*255/10000;
      analogWrite(ledPin, brightness);
    }
    
    switch (brightness) {
      case 97:
        R = !R;
        break;
        case 98:
        G = !G;
        break;
        case 99:
        B = !B;
    }

    if (R) {
      digitalWrite(red,HIGH);
    } else {
      digitalWrite(red,LOW);
    }
    if (G) {
      digitalWrite(green,HIGH);
    } else {
      digitalWrite(green,LOW);
    }
    if (B) {
      digitalWrite(yellow,HIGH);
    } else {
      digitalWrite(yellow,LOW);
    }
    
  }
}
