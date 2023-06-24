#include <SoftwareSerial.h>
#include <EEPROM.h>

SoftwareSerial xbeeSerial(10, 11);

/* UNIQUE IDENTIFICATION */
String id = "";
int writeStringToEEPROM(int addrOffset, const String &strToWrite);
int readStringFromEEPROM(int addrOffset, String *strToRead);
void genString(int len, String *gen);
void checkIdArduino();


char vectorDirection(int vector);

void setup()
{
  Serial.begin(57600);
  xbeeSerial.begin(57600);

  
  checkIdArduino();
}

void loop()
{

  if (xbeeSerial.available())
  {
    String cmd = xbeeSerial.readString();
    if(cmd.startsWith("data")){
        int windSpeed = random(0, 100);
        char direction = vectorDirection(random(0, 4));
      
        int temp = random(0, 50);
        int humidity = random(0, 100);
      
        String msg = id + "*" + String(windSpeed) + "*" +direction + "*" + temp + "*"+humidity+"*"+String(random(0, 100))+"*"+String(random(0, 100))+"*"+String(random(0, 100))+"*"+String(random(0, 100))+"*"+String(random(0, 100))+"|";
        xbeeSerial.println(msg);
    }
  }
   
}

char vectorDirection(int vector){
  if(vector == 0){
    return 'N';
  }else if(vector == 1){
    return 'S';
  }else if(vector == 2){
    return 'O';
  }else{
    return 'E';
  }
}


/* Sistema de Identificação Unica */
int writeStringToEEPROM(int addrOffset, const String &strToWrite)
{
  byte len = strToWrite.length();
  EEPROM.write(addrOffset, len);
  for (int i = 0; i < len; i++)
  {
    EEPROM.write(addrOffset + 1 + i, strToWrite[i]);
  }
  return addrOffset + 1 + len;
}

int readStringFromEEPROM(int addrOffset, String *strToRead)
{
  int newStrLen = EEPROM.read(addrOffset);
  char data[newStrLen + 1];
  for (int i = 0; i < newStrLen; i++)
  {
    if ( ((char)EEPROM.read(addrOffset + 1 + i)) == '\n') {
      break;
    }
    data[i] = EEPROM.read(addrOffset + 1 + i);
  }
  data[newStrLen] = '\0'; // !!! NOTE !!! Remove the space between the slash "/" and "0" (I've added a space because otherwise there is a display bug)
  *strToRead = String(data);
  return addrOffset + 1 + newStrLen;
}


void genString(int len, String *gen) {
  String hashString = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";
  char data[(len + 1)];
  for (int i = 0; i < len; i++) {
    data[i] = hashString[random(0, (hashString.length() - 1))];
  }
  data[len] = '\0';

  *gen = String(data);
}

void checkIdArduino() {
  do {
    delay(100);
    readStringFromEEPROM(0, &id);
    delay(100);
    if (id.length() <= 8) {
      String idToWrite = "";
      genString(8, &idToWrite);
      writeStringToEEPROM(0, idToWrite + "\n");
      Serial.println("Novo id: " + idToWrite);
    } else {
      Serial.println("ID Atual: " + id);
    }
  } while (id.length() <= 8);

  id = id.substring(0, id.length() - 1);
}
