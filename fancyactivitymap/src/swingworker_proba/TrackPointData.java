/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swingworker_proba;

import java.util.Date;
import org.jxmapviewer.viewer.GeoPosition;

/**
 *
 * @author Dávid
 */
public class TrackPointData {
    
    private GeoPosition point;
    private Date time;

    public TrackPointData(GeoPosition point, Date time) {
        this.point = point;
        this.time = time;
    }

    public GeoPosition getPoint() {
        return point;
    }

    public void setPoint(GeoPosition point) {
        this.point = point;
    }

    public Date getTimeStamp() {
        return time;
    }

    public void setTimeStamp(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "lat: "+point.getLatitude() + "long: " +point.getLongitude()+ ", time=" + time;
    }
    
}
