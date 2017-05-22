package com.redhat.iot.automobile.obd;

/**
 * Created by mirussell on 4/26/17.
 */

public class OBDDataElement
{

  private String vin, timestamp, dtcCount, distanceWithMIL, speed, rpm, engineRunTime, throttlePos, barometricPressure, latitude, longitude = "";

  public String toJSON()
  {
    String json = "{";
    json += getJSONElement("vin", vin);
    json += "," + getJSONElement("timestamp", timestamp);
    json += "," + getJSONElement("dtcCount", dtcCount);
    json += "," + getJSONElement("distanceWithMIL", distanceWithMIL);
    json += "," + getJSONElement("speed", speed);
    json += "," + getJSONElement("rpm", rpm);
    json += "," + getJSONElement("engineRunTime", engineRunTime);
    json += "," + getJSONElement("throttlePos", throttlePos);
    json += "," + getJSONElement("barometricPressure", barometricPressure);
    json += "," + getJSONElement("latitude", latitude);
    json += "," + getJSONElement("longitude", longitude);
    json += "}";
    return json;
  }

  /**
   * format a data element for JSON
   * @param key
   * @param value
   * @return
   */
  private String getJSONElement(String key, String value)
  {
    return "\"" + key + "\":\"" + value + "\"";
  }

  /**
   * @return the vin
   */
  public String getVin()
  {
    return vin;
  }

  /**
   * @param vin the vin to set
   */
  public void setVin(String vin)
  {
    this.vin = vin;
  }

  /**
   * @return the timestamp
   */
  public String getTimestamp()
  {
    return timestamp;
  }

  /**
   * @param timestamp the timestamp to set
   */
  public void setTimestamp(String timestamp)
  {
    this.timestamp = timestamp;
  }

  /**
   * @return the dtcCount
   */
  public String getdtcCount()
  {
    return dtcCount;
  }

  /**
   * @param dtcCount the dtcCount to set
   */
  public void setDtcCount(String dtcCount)
  {
    this.dtcCount = dtcCount;
  }

  /**
   * @return the distanceWithMIL
   */
  public String getDistanceWithMIL()
  {
    return distanceWithMIL;
  }

  /**
   * @param distanceWithMIL the distanceWithMIL to set
   */
  public void setDistanceWithMIL(String distanceWithMIL)
  {
    this.distanceWithMIL = distanceWithMIL;
  }

  /**
   * @return the speed
   */
  public String getSpeed()
  {
    return speed;
  }

  /**
   * @param speed the speed to set
   */
  public void setSpeed(String speed)
  {
    this.speed = speed;
  }

  /**
   * @return the rpm
   */
  public String getRpm()
  {
    return rpm;
  }

  /**
   * @param rpm the rpm to set
   */
  public void setRpm(String rpm)
  {
    this.rpm = rpm;
  }

  /**
   * @return the engineRunTime
   */
  public String getEngineRunTime()
  {
    return engineRunTime;
  }

  /**
   * @param engineRunTime the engineRunTime to set
   */
  public void setEngineRunTime(String engineRunTime)
  {
    this.engineRunTime = engineRunTime;
  }

  /**
   * @return the throttlePos
   */
  public String getThrottlePos()
  {
    return throttlePos;
  }

  /**
   * @param throttlePos the throttlePos to set
   */
  public void setThrottlePos(String throttlePos)
  {
    this.throttlePos = throttlePos;
  }

  /**
   * @return the barometricPressure
   */
  public String getBarometricPressure()
  {
    return barometricPressure;
  }

  /**
   * @param barometricPressure the barometricPressure to set
   */
  public void setBarometricPressure(String barometricPressure)
  {
    this.barometricPressure = barometricPressure;
  }

  /**
   * @return the latitude
   */
  public String getLatitude()
  {
    return latitude;
  }

  /**
   * @param latitude the latitude to set
   */
  public void setLatitude(String latitude)
  {
    this.latitude = latitude;
  }

  /**
   * @return the longitude
   */
  public String getLongitude()
  {
    return longitude;
  }

  /**
   * @param longitude the longitude to set
   */
  public void setLongitude(String longitude)
  {
    this.longitude = longitude;
  }

}
