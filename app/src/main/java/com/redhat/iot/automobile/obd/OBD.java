package com.redhat.iot.automobile.obd;

import android.os.Looper;

import com.redhat.iot.automobile.obd.OBDDataElement;

import java.net.Socket;

import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.*;


import com.github.pires.obd.commands.*;
import com.github.pires.obd.commands.control.*;
import com.github.pires.obd.commands.engine.*;
import com.github.pires.obd.commands.fuel.*;
import com.github.pires.obd.commands.pressure.*;
import com.github.pires.obd.commands.protocol.*;
import com.github.pires.obd.commands.temperature.*;



/**
 * Created by mirussell on 4/26/17.
 */

public class OBD implements Runnable
{

  private Socket socket;
  private String ipAddress;
  private int port;

  private boolean isDone;

  public static boolean LOG = true;

  private PrintWriter fileOut;
  double latitude, longitude;

  private ArrayList<Output> allOutput = new ArrayList<Output>();        // hold a list of all Output objects


  // simple class to hold program output and a boolean whether it was reported back already (a simple filter for new items that werent previously reported)
  private class Output
  {
    private boolean reported = false;       // every time the parent program calls getOutput(), this will only return items that have not been reported yet
    private String message;

    public Output(String message)
    {
      this.message = message;
      reported = false;
    }

    public boolean isReported()
    {
      return reported;
    }

    public String getMessage()
    {
      reported = true;
      return message;
    }
  }


  /**
   * Constructor
   */
  public OBD()
  {
  }


  /**
   * run()
   *
   * Called when client calls start(), but you know that already right?
   *
   */
  @Override
  public void run()
  {
    Looper.prepare();

    allOutput.clear();

    log("OBD.run() - START...");

    try
    {
      connect();
      mainLoop();
    }
    catch (Exception e)
    {
      log("!!!!!!!!!! Caught Exception in OBD.run() !!!!!!!!!!");
      log(e.getMessage());
      e.printStackTrace();
    }

    log("OBD.run() - FINISH...");

    Looper.loop();
  }


  /**
   * Party On!
   */
  private void mainLoop()
  {
    OBDDataElement obd;

    isDone = false;

    // main loop
    while (! isDone)
    {
      log("*************************");

      obd = new OBDDataElement();

      // get a fresh timestamp
      obd.setTimestamp(getCurrentTime());

      obd.setLatitude(String.valueOf(latitude));
      obd.setLongitude(String.valueOf(longitude));
      log("Latitude: " + latitude);
      log("Longitude: " + longitude);

      String s = "";        // temp string obj

      try { s = getVIN(); } catch (Exception e) {log(e.getMessage());s="";}
      obd.setVin(s);
      log("VIN: "	+  obd.getVin());

      try { s = getDTCCount(); } catch (Exception e) {log(e.getMessage());s="";}
      obd.setDtcCount(s);
      log("# of Diagnostic Codes on: "	+  s);

      try { s = getDistanceWithMIL(); } catch (Exception e) {log(e.getMessage());s="";}
      obd.setDistanceWithMIL(s);
      log("Distance with MIL on: "	+  s);

      try { s = getCurrentSpeed(); } catch (Exception e) {log(e.getMessage());s="";}
      obd.setSpeed(s);
      log("Speed: "	+  s);

      try { s = getCurrentRPM(); } catch (Exception e) {log(e.getMessage());s="";}
      obd.setRpm(s);
      log("RPM: "	+  s);

      try { s = getRuntimeSinceEngineStart(); } catch (Exception e) {log(e.getMessage());s="";}
      obd.setEngineRunTime(s);
      log("Run Time since engine start: "	+  s);

      try { s = getThrottlePosition(); } catch (Exception e) {log(e.getMessage());s="";}
      obd.setThrottlePos(s);
      log("Throttle Position: "	+  s);

      try { s = getBarometricPressure(); } catch (Exception e) {log(e.getMessage());s="";}
      obd.setBarometricPressure(s);
      log("Barometric Pressure: "	+  s);

      fileOut.println(obd.toJSON());
      fileOut.flush();


      try {Thread.sleep(500);} catch (Exception e) {}
    }

    log("OBD.mainLoop() - done, closing output file...");

    fileOut.close();

  }


  /**
   * Stop the engine!
   */
  public void stop()
  {
    isDone = true;
  }


  /**
   * public method to allow the setting of latitude and longitude
   * @param latitude
   * @param longitude
   */
  public void setGeo(double latitude, double longitude)
  {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  /**
   * Allow client to set the ipAddress of the OBD-II scanner
   * @param ipAddress
   */
  public void setIpAddress(String ipAddress)
  {
    this.ipAddress = ipAddress;

  }


  /**
   * Allow client to set the port of the OBD-II scanner
   * @param port
   */
  public void setPort(int port)
  {
    this.port = port;

  }


  /**
   * This is where to write output JSON to
   * @param printWriter
   */
  public void setPrintWriter(PrintWriter printWriter)
  {
    this.fileOut = printWriter;
  }



  /**
   * Return the arraylist that all output is logged to
   */
  public ArrayList<String> getAllProgramOutput()
  {
    ArrayList al = new ArrayList();
    if (allOutput != null)
    {
      for (int i=0; i<allOutput.size(); i++)
      {
        al.add(allOutput.get(i).getMessage());
      }
    }
    return al;
  }




  /**
   * Return an ArrayList of strings that have not been reported already
   */
  public ArrayList<String> getNewProgramOutput()
  {
    ArrayList<Output> temp = new ArrayList(allOutput);
    ArrayList al = new ArrayList();
    if (temp != null)
    {
      for (int i=0; i<temp.size(); i++)
      {
        Output o = temp.get(i);
        if (! o.isReported())
        {
          al.add(allOutput.get(i).getMessage());          // get it from allOuutput so it sets
        }
      }
    }
    return al;
  }





  /**
   * Connect
   * Opens the Socket connection and initialize the OBD2 scanner
   * @throws Exception
   */
  private void connect() throws Exception
  {
    log("Connecting to OBDII...");
    //log("OBD.connect() - ipAddress: " + ipAddress);
    //log("OBD.connect() - port: " + port);
    socket = new Socket(ipAddress, port);
    sendCommand("ATD");
    Thread.sleep(1000);
    sendCommand("ATZ");
    Thread.sleep(1000);
    sendCommand("AT E0");
    Thread.sleep(1000);
    sendCommand("AT L0");
    Thread.sleep(1000);
    sendCommand("AT S0");
    Thread.sleep(1000);
    sendCommand("AT H0");
    Thread.sleep(1000);
    sendCommand("AT SP 0");
    log("Connected Successfully!");
    log("Pausing for three seconds...");
    Thread.sleep(3000);
  }




  /**
   * getVIN()
   * @return the VIN of the automobile
   * @throws Exception
   */
  public String getVIN() throws Exception
  {
    //log("OBDCommandLineApp.getVIN() - START...");
    VinCommand cmd = new VinCommand();
    cmd.run(socket.getInputStream(), socket.getOutputStream());
    //log("OBDCommandLineApp.getVIN() - FINISH...");
    return (cmd.getCalculatedResult());
  }

  /**
   * getCurrentSpeed()
   * @return the current speed of the vehicle, in MPH
   * @throws Exception
   */
  public String getCurrentSpeed() throws Exception
  {
    //log("OBDCommandLineApp.getCurrentSpeed() - START...");
    SpeedCommand cmd = new SpeedCommand();
    cmd.run(socket.getInputStream(), socket.getOutputStream());
    //log("OBDCommandLineApp.getCurrentSpeed() - FINISH...");
    return (String.valueOf(cmd.getImperialUnit()));
  }

  /**
   * getCurrentRPM()
   * @return the current RPM of the vehicle
   * @throws Exception
   */
  public String getCurrentRPM() throws Exception
  {
    //log("OBDCommandLineApp.getCurrentRPM() - START...");
    RPMCommand cmd = new RPMCommand();
    cmd.run(socket.getInputStream(), socket.getOutputStream());
    //log("OBDCommandLineApp.getCurrentRPM() - FINISH...");
    return (cmd.getCalculatedResult());
  }

  /**
   * getThrottlePosition()
   * @return the current Throttle Position
   * @throws Exception
   */
  public String getThrottlePosition() throws Exception
  {
    //log("OBDCommandLineApp.getThrottlePosition() - START...");
    ThrottlePositionCommand cmd = new ThrottlePositionCommand();
    cmd.run(socket.getInputStream(), socket.getOutputStream());
    //log("OBDCommandLineApp.getThrottlePosition() - FINISH...");
    return (String.valueOf(cmd.getPercentage()));
  }

  /**
   * getBarometricPressure()
   * @return the current Barometric Pressure
   * @throws Exception
   */
  public String getBarometricPressure() throws Exception
  {
    //log("OBDCommandLineApp.getBarometricPressure() - START...");
    BarometricPressureCommand cmd = new BarometricPressureCommand();
    cmd.run(socket.getInputStream(), socket.getOutputStream());
    //log("OBDCommandLineApp.getBarometricPressure() - FINISH...");
    return (String.valueOf(cmd.getImperialUnit()));
  }

  /**
   * getDTCCount()
   * @return Number of DTCs currently flagged on
   * @throws Exception
   */
  public String getDTCCount() throws Exception
  {
    //log("OBDCommandLineApp.getDTCCount() - START...");
    DtcNumberCommand cmd = new DtcNumberCommand();
    cmd.run(socket.getInputStream(), socket.getOutputStream());
    //log("OBDCommandLineApp.getDTCCount() - FINISH...");
    return (String.valueOf(cmd.getTotalAvailableCodes()));
  }

  /**
   * getRuntimeSinceEngineStart()
   * @return How long the car has been running since starting
   * @throws Exception
   */
  public String getRuntimeSinceEngineStart() throws Exception
  {
    //log("OBDCommandLineApp.getRuntimeSinceEngineStart() - START...");
    RuntimeCommand cmd = new RuntimeCommand();
    cmd.run(socket.getInputStream(), socket.getOutputStream());
    //log("OBDCommandLineApp.getRuntimeSinceEngineStart() - FINISH...");
    return (String.valueOf(cmd.getCalculatedResult()));
  }

  /**
   * getDistanceWithMIL()
   * @return DIstance traveled with Malfunction Indicator Lamp on
   * @throws Exception
   */
  public String getDistanceWithMIL() throws Exception
  {
    //log("OBDCommandLineApp.getDistanceWithMIL() - START...");
    DistanceMILOnCommand cmd = new DistanceMILOnCommand();
    cmd.useImperialUnits(true);
    cmd.run(socket.getInputStream(), socket.getOutputStream());
    //log("OBDCommandLineApp.getDistanceWithMIL() - FINISH...");
    return (cmd.getCalculatedResult());
  }






  /**
   * sendCommand
   * Send a raw command to the OBD-II scanner
   * This method does not check the return value from
   * the command and doesn't return anything
   * @param command
   * @throws Exception
   */
  private void sendCommand(String command) throws Exception
  {
    log("Sending command: " + command);
    ObdRawCommand rc = new ObdRawCommand(command);
    rc.run(socket.getInputStream(), socket.getOutputStream());
  }


  /**
   * return a new file name (to the current millisecond)
   * @return
   */
  public static String getNewFileName()
  {
    return "OBD-" + getCurrentTime().replaceAll(":", "-") + ".txt";
  }


  private static String getCurrentTime()
  {
    Calendar cal = Calendar.getInstance();

    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("MM");				// for some reason month was coming out wrong - one month earlier using the cal.get() ?????????
    String month = df.format(cal.getTime());

    String t = String.valueOf(cal.get(Calendar.YEAR)) + "-" +
      month + "-" +
      String.format("%02d", cal.get(Calendar.DATE)) + " " +
      String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) + ":" +
      String.format("%02d", cal.get(Calendar.MINUTE)) + ":" +
      String.format("%02d", cal.get(Calendar.SECOND)) + "." +
      String.valueOf(cal.get(Calendar.MILLISECOND)) ;
    return (t);
  }






  /**
   * log to the screen and to the System.out
   * @param s
   */
  private void log(String s)
  {
    if (LOG) System.out.println(s);
    if (allOutput != null)
    {
      allOutput.add(new Output(s));
    }
  }




}       // CLASS DISMISSED!
