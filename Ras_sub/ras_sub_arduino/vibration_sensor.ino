void setup() 
 {
    Serial.begin(9600);
    Serial.println("");
 }
 double val[8] = {0};
 // port num net
 void printVal(double val,int port)
 {
     Serial.print(port+1);
     Serial.print("\t");
     Serial.println(val);
 }
 // port num + sensor data output
 void loop() 
 {
   int i;
   for(i = 0 ; i < 8; i++)
   {
     val[i]=analogRead(i);
   }
   for(i = 0 ; i < 8; i++)
   {
     if(val[i]> 100)
       printVal(val[i],i);
   }
 }