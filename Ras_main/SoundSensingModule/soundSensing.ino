/*
This is sensing code for sound in arduino due.
*/

const int sampleWindow = 50; // Sample window width in mS (50 mS = 20Hz)
unsigned int sample[4];
int count = 0;
double dataTable[10][4] = {0}; // datas that system measures.
bool TABLE_FULL = false; // state Table
int loopCount = 0; // For index that modified when updating
double average[4]; // Averages of each datas
//----------------------------------------------------------------------
//                          setting Function
//----------------------------------------------------------------------
void setup() 
{
   Serial.begin(9600);
   Serial.println("");
}
//----------------------------------------------------------------------
//                          print Function
//----------------------------------------------------------------------

// fuction to print data list
void printLine(int *voltsList,int size) 
{
  int i;
  for(i=0;i<size;i++)
  {
    Serial.print(voltsList[i]);
    Serial.print("\t");
  }
  Serial.print("\n");
}
// fuction to print data table
void printTable()
{
  int i, j;
  Serial.println("-------------------------------------");
  for(i = 0 ; i < 10 ; i ++ )
  {
    for(j = 0 ; j<4 ;j++)
    {
      Serial.print(dataTable[i][j]);
      Serial.print("\t");
    }
    Serial.print("\n");
  }
  Serial.println("-------------------------------------");  
}
// fuction to print average data
void printAverage()
{
  int i;
  for(i = 0; i < 4 ; i ++)
  {
    Serial.print(average[i]);
    Serial.print("\t");
  }
  Serial.print("\n");
}
//----------------------------------------------------------------------
//                          Function about data
//----------------------------------------------------------------------
// function to check occuring around system
bool occurSound(int *voltsList,int size)
{
  int i;
  for(i=0;i<size;i++)
  {
    if(voltsList[i] - average[i] >500) // This is 
    {
        return true;
    }
  }
  return false;
}
// fuction to get max value from geting data list
int getMax(int *voltsList,int size)
{
  int i;
  double temp = 0;
  int index = -1;
    for(i=0;i<size;i++)
    {
      if(voltsList[i] > temp)
      {
        temp = voltsList[i];
        index = i;
      }
    }
    return index;
}
// function to calculate average
double getAverage()
{
  int i, j;
  for(i = 0 ; i < 4; i ++)
  {
    double sum = 0;
    for(j = 0 ; j < 10 ; j ++)
    {
      sum =+ dataTable[j][i];
    }
    average[i] = sum / 10;
  }
}
// function to save data at table
void storeTotable(int *voltsList, int size){
  int i;
  for(i = 0 ; i < size; i ++)
  {
    dataTable[count][i] = voltsList[i];
  }
  count++;
  count = count%10;  
}
// function to get circle points.
void makingCirclePoint(int maxIndex, int *data)
{
  
  int left, right;
  int result[3] = {0};
  int width = 260 ;
  int height = 390;
  double maxValue = 100 + (data[maxIndex-1] - 500)/14;
  
  result[2] = maxValue;
  if(maxIndex == 1)
  {
    left = data[3];
    right = data[1];
  }

  else if(maxIndex == 4)
  {
    left = data[2];
    right = data[0];
  }
  else
  {
    left = data[maxIndex-2];
    right = data[maxIndex];
  }

  right = 100 + (right - 500)/14;
  left = 100 + (left - 500)/14;
  double mov = left -right;
  double sum = left+right;
  double x, y;
  if(maxIndex == 1)
  {
    x = 70;
    y = 45;
    if(maxValue < sum)
    {
      if(mov > 0)
      {
        x = x + width*(mov/sum);
      }
      else
      {
        y = y - height*(mov/sum);
      }
    }
  }
  else if(maxIndex == 2)
  {
    x = 70;
    y = 435;
    if(maxValue < sum)
    {
      if(mov > 0)
      {
        y = y - height*(mov/sum);
      }
      else
      {
        x = x - width*(mov/sum);
      }
    }
  }
  else if(maxIndex == 3)
  {
    x = 330;
    y = 435;
    if(maxValue < sum)
    {
      if(mov > 0)
      {
        x = x - width*(mov/sum);
      }
      else
      {
        y = y + height*(mov/sum);
      }
    }
  }
  else
  {
    x = 330;
    y = 45;
    if(maxValue < sum)
    {
      if(mov > 0)
      {
        y = y + height*(mov/sum);
      }
      else
      {
        x = x - width*(mov/sum);
      }
    }
  }

  
   result[0] = x;
   result[1] = y;

    Serial.print(result[0]);
    Serial.print("/");
    Serial.print(result[1]);
    Serial.print("/");
    Serial.println(result[2]);
}


//----------------------------------------------------------------------
//                          loop Function
//----------------------------------------------------------------------
void loop() 
{
   unsigned long startMillis= millis();  // Start of sample window
   unsigned int peakToPeak[4] = {0};   // peak-to-peak level
 
   unsigned int signalMax[4] = {0,0,0,0};
   unsigned int signalMin[4] = {1024,1024,1024,1024};

  int i;
 
   // collect data for 50 mS
   while (millis() - startMillis < sampleWindow)
   {
    
    for(i=0;i<4;i++)
    {
      sample[i] = analogRead(i); // setting each sensor's port
      if (sample[i] < 1024)  // toss out spurious readings
      {
         if (sample[i] > signalMax[i])
         {
            signalMax[i] = sample[i];  // save just the max levels
         }
         else if (sample[i] < signalMin[i])
         {
            signalMin[i] = sample[i];  // save just the min levels
         }
      }
    }
   }
   int volts[4];
   for(i=0;i<4;i++)
   {
     peakToPeak[i] = signalMax[i] - signalMin[i];  // max - min = peak-peak amplitude
     volts[i] = (peakToPeak[i] * 3.3) / 1.024; // getting data
   }
   if(occurSound(volts,4)==true)  // Sending data through usb serial
   {
   // Serial.print(getMax(volts,4)+1);
    //Serial.print("\t");
    //printLine(volts,4);
    makingCirclePoint(getMax(volts,4)+1, volts);
    delay(1000);
   } 
   storeTotable(volts,4); // saving data to table
   loopCount ++; 
   if(loopCount >= 10 && TABLE_FULL == false) // when first 10 loop
      TABLE_FULL = true;
   if(TABLE_FULL == true) // after getting 10 data  
   {
      getAverage();
   }
}
