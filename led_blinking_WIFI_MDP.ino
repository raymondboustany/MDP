
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>

#ifdef ESP32
#pragma message(THIS EXAMPLE IS FOR ESP8266 ONLY!)
#error Select ESP8266 board.
#endif

//

ESP8266WebServer server(80); // 80 is the port number

const char* ssid = "Omar ";
const char* password = "atik03434084";


String ledon,ledoff,led1on,led1off;

void Redon()
{
  digitalWrite(4, HIGH);
  server.send(200, "text/html", ledon);
}

void Redoff()
{
  digitalWrite(4, LOW);
  server.send(200, "text/html", ledoff);
}

void violeton()
{
  digitalWrite(14, HIGH);
  server.send(200, "text/html", led1on);
}

void violetoff()
{
  digitalWrite(14, LOW);
  server.send(200, "text/html", led1off);
}

void setup() {

  Serial.begin(115200);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED)delay(500);

  Serial.print(WiFi.localIP());

  server.on("/led1on", Redon);
  server.on("/led1off", Redoff);
  server.on("/led2on", violeton);
  server.on("/led2off", violetoff);

  server.begin();

  pinMode(14, OUTPUT);//D5
  pinMode(4, OUTPUT);//D2

  digitalWrite(14, LOW);
  digitalWrite(4, LOW);

}

void loop()
{
  server.handleClient();
  delay(1);
}
