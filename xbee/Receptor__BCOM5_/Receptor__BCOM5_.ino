#include <SoftwareSerial.h>
#include <EEPROM.h>

SoftwareSerial xbeeSerial(10, 11);

/* UNIQUE IDENTIFICATION */
String id = "";
int writeStringToEEPROM(int addrOffset, const String &strToWrite);
int readStringFromEEPROM(int addrOffset, String *strToRead);
void genString(int len, String *gen);
void checkIdArduino();


void setup()
{
  Serial.begin(57600);
  xbeeSerial.begin(57600);

  checkIdArduino();
}

void loop()
{
  
  xbeeSerial.println("data");
  if (xbeeSerial.available())
  {
    String data = xbeeSerial.readString();
    data = "|" + data;

    //Spliting data!
    for(int i = 0; i < data.length(); i++){
      Serial.print(data.charAt(i));
    }
  }
  

  delay(1000);
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
    }
  } while (id.length() <= 8);
}
