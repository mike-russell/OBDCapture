# OBDCapture

OBDCapture is an Android app, developed in Android Studio 2.3.1

It uses the [OBD-JAVA-API library](https://github.com/pires/obd-java-api) library to execute the actual OBD-II PIDs.

OBDCapture was developed as a data generation utility for Red Hat’s [RADAnalytics platform](http://radanalytics.io/). The larger picture is to show how to do real time analytics on driving data (i.e. real time underwriting, etc.).

It was developed to generate real driving data. There are no cool gauges, or other fancy UI elements.


## Output

OBDCapture executes OBD-II PIDs (via obd-java-api) every 1/2 second. It generates about 1MB of data per driving hour.

It saves each capture as a JSON record in a file in the following folder on the Android device: 

My Files > Device Storage > Android > data > com.redhat.iot.automobile.obd > files

For each driving session, a file will be created in this folder with the following format:

“OBD-” + YYYY-MM-DD + “ “ + HH-MM-SS.mmm.txt

Sample file name:

“OBD-2017-05-01 22-44-49.455.txt”

Each record is a JSON object with the following format:

{

"vin":"5N1AR3DD2AC69123”,

“timestamp":"2017-04-20 2:54:29.600",

"dtcCount":"0",

"distanceWithMIL":"0.0",

“speed":"0.0",

“rpm":"637",

"engineRunTime":"55",

"throttlePos":"2.3529413",

"barometricPressure":"14.79385",

"latitude”:”99.9999”,

"longitude”:”-99.9999”

}


** Dictionary: **

“vin": Vehicle Identification Number

“timestamp": Timestamp of each OBD-II capture

"dtcCount": Number of Display Trouble Codes (i.e. Check Engine Light, etc)

"distanceWithMIL": Amount of miles driven since Malfunction Indicator Lamp on

“speed": Current MPH

“rpm": Current RPM

"engineRunTime": Amount of time engine has been running (seconds) 

"throttlePos": Throttle position (percentage)

"barometricPressure": Barometric Pressure (psi)

"latitude": Latitude

"longitude": Longitude


## Prerequisites

To run this app, you must have an OBD-II WiFi scanner.

All vehicles after 1996 have an OBD-II port, so the car will support it if it’s after 1996 model year.

OBDCapture communicates with an OBD-II WiFi scanner. It will probably work with a Bluetooth scanner, but it has only been tested on the [Foseal WiFi scanner](http://www.foseal.com). 


## Dependencies

OBDCapture uses the [OBD-JAVA-API library](https://github.com/pires/obd-java-api). I would recommend reading the readme.md for that project for a deeper understanding of the OBD-II protocol.


## How to run

After the app is installed on your Android phone:

1) Plug in the OBD-II WiFi scanner into the car’s port.
2) Go into your WiFi settings on your Android phone and connect to the scanner (WIFI_OBD)
3) Start the OBDCapture app
4) Click the START button

You’ll see messages logged to the app (in a ListView).

Click the STOP button to stop logging.

Note: There is no logic for restarting. The app assumes that it is a new driving session every time. So, if you are restarting the session, you need to do #1-4 above (i.e. unplug the scanner and start over).



