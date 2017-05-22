package com.redhat.iot.automobile.obd;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity
{
  private static final String TAG = "MainActivity";

  private static final boolean LOG_TO_SYSTEM_OUT = true;

  private OBD obd = new OBD();

  // I/O stuff
  private File file;
  PrintWriter printWriter;

  // Location Services (lat/long)
  LocationManager locationManager;
  LocationListener locationListener;
  private boolean isGPSEnabled = false;
  private boolean isNetworkEnabled = false;

  // List for output on screen
  private ListView outListView;


  private boolean isDone;


  private Timer logTimer;
  private LogFetcher logFetcher;
  private class LogFetcher extends TimerTask
  {
    @Override
    public void run()
    {
      try
      {
        synchronized (this)
        {
          runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              ArrayList al = obd.getNewProgramOutput();
              ArrayAdapter aa = (ArrayAdapter) outListView.getAdapter();
              aa.addAll(al);
            }
          });
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }





  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setupListView();
  }





  /**
   * Called when the user taps the Start button
   * @param view
   */
  public void startButtonHandler(View view)
  {
    ArrayAdapter aa = (ArrayAdapter) outListView.getAdapter();
    aa.clear();

    logTimer = new Timer(true);
    logTimer.scheduleAtFixedRate(new LogFetcher(), 0, 1000);

    if (!createOBDFile()) return;

    setupGeo();

    if (!startOBD())
    {
      printWriter.flush();
      printWriter.close();
      return;
    }

  }





  /**
   * Called when the user taps the Stop button
   * @param view
   */
  public void stopButtonHandler(View view)
  {
    stopOBD();
  }






  /**
   * Fire it up!
   * @return
   */
  private boolean startOBD()
  {
    log("Fire up the engine...");

    EditText et;

    // get IP Address UI field
    et = (EditText) findViewById(R.id.ipaddr);
    String ipAddress = et.getText().toString();

    // get Port UI field
    et = (EditText) findViewById(R.id.port);
    String s = et.getText().toString();
    int port = Integer.parseInt(s);

    try
    {
      printWriter = new PrintWriter(file);
    }
    catch (Exception e)
    {
      Log.e(TAG, "Error creating PrintWriter...", e);
      log("Error creating PrintWriter...");
      log(e.getMessage());
      userMsg("ERROR", "Error accessing file... (new PrintWriter() failed)");
      return false;
    }

    obd.setIpAddress(ipAddress);
    obd.setPort(port);
    obd.setPrintWriter(printWriter);

    new Thread(obd).start();

    return true;
  }




  /**
   * Stop the beast
   */
  private void stopOBD()
  {
    log("Stopping the Engine...");

    if (locationManager != null && locationListener != null) locationManager.removeUpdates(locationListener);
    if (obd != null) obd.stop();

    try {Thread.sleep(3000);} catch (Exception e) {}          // to allow for last output message to be captured by the timer

    if (logTimer != null) logTimer.cancel();
  }



  /**
   * Create the file to log OBD JSON records
   * @return true if successful, falso if not
   */
  private boolean createOBDFile()
  {
    log("Creating the file to store OBD output...");

    String state = Environment.getExternalStorageState();
    if (! Environment.MEDIA_MOUNTED.equals(state))
    {
      log("ERROR: External Storage not available...");
      userMsg("ERROR", "External Storage not available...");
      return false;
    }

    try
    {
      log("External Files Directory: " + getExternalFilesDir(null));

      String fileName = OBD.getNewFileName();

      log("File Name: " + fileName);

      file = new File( getExternalFilesDir(null), fileName);
      if (! file.createNewFile())
      {
        Log.v(TAG, "Error creating file: " + fileName);
        return false;
      }

      log("createOBDFile - FINISH...");

      return true;
    }
    catch (Exception e)
    {
      log("ERROR: Error creating file...");
      log(e.getMessage());
      Log.e(TAG, "Error creating file...", e);
      userMsg("ERROR", "Error creating file...");
      return false;
    }
  }





  /**
   * Display a dialog box with an OK button
   * @param message
   */
  private void userMsg(String title, String message)
  {
    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
    alertDialog.setTitle(title);
    alertDialog.setMessage(message);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
      new DialogInterface.OnClickListener()
      {
        public void onClick(DialogInterface dialog, int which)
        {
          dialog.dismiss();
        }
      });
    alertDialog.show();
  }








  /**
   * request permissions to use locaiton services
   */
  private void setupGeo()
  {
    log("Setting up GEO positioning...");

    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
      {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
      }
    }

    try{isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
    try{isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

    log("isGPSEnabled?: " + isGPSEnabled);
    log("isNetworkEnabled?: " + isNetworkEnabled);
    log("GPS enabled? " + isGPSEnabled);

    if(! isGPSEnabled)        // let's count on GPS - this could be modified to pull GPS and/or network and use the latest, but for now...
    {
      return;
    }

//    if(isNetworkEnabled)
//      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

    // Get Latitude / Longitude
    try
    {

      log("Lat/Long baseline...");
      Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

      if (location == null)
      {
        log("ERROR: Location services not working...");
        return;
      }


      log("Latitude: " + location.getLatitude());
      log("Longitude: " + location.getLongitude());

       locationListener = new LocationListener()
      {
        public void onLocationChanged(Location location)
        {
//          log("LocationListener - Latitude: " + location.getLatitude());
//          log("LocationListener - Longitude: " + location.getLongitude());
          obd.setGeo(location.getLatitude(), location.getLongitude());
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
      };

      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

    }
    catch (SecurityException se)
    {
      log("ERROR: SecurityException caught in OBD.configGeo()");
      log(se.getMessage());
      se.printStackTrace();
    }
  }


  /**
   * Log output to the TextView
   * @param s
   */
  private void log(String s)
  {
    if (LOG_TO_SYSTEM_OUT) Log.d(TAG, s);
    if (outListView != null)
    {
      ArrayAdapter aa = (ArrayAdapter) outListView.getAdapter();
      aa.add(s);
    }
  }


  /**
   * set up the list view component
   */
  private void setupListView()
  {
    outListView = (ListView) findViewById(R.id.outListView);

    ArrayList al = new ArrayList<String>();
    al.add("***** PROGRAM OUTPUT *****");

    // Create an ArrayAdapter from List
    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, al);

    // DataBind ListView with items from ArrayAdapter
    outListView.setAdapter(arrayAdapter);
  }




}
