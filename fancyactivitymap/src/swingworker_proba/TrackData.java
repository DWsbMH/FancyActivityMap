/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swingworker_proba;

import java.util.ArrayList;
import java.util.Date;
import org.jxmapviewer.viewer.GeoPosition;

/**
 *
 * @author Dávid
 */
public class TrackData {
    private String name;
    private ArrayList<TrackPointData> track;
    
    public TrackData(String name)
    {
        this.name=name;
        track=new ArrayList<TrackPointData>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<TrackPointData> getTrack() {
        return track;
    }
    
    public void addPoint(TrackPointData point)
    {
        track.add(point);
    }

    public ArrayList<GeoPosition> getGeoPositions()
    {
        ArrayList<GeoPosition> geoPositions=new ArrayList<GeoPosition>();
        for(TrackPointData point:track)
        {
            geoPositions.add(point.getPoint());
        }
        return geoPositions;
    }
    
    public GeoPosition getGeoPosition(Date time)
    {
        /*for(TrackPointData point:track)
        {
            if(point.getTimeStamp().toString().compareTo(time.toString())==0)
            {
                return point.getPoint();
            }
        }
        
        return null;*/
        if(time.getTime()>getEndTime().getTime() || time.toString().compareTo(getEndTime().toString())==0)
        {
            return track.get(track.size()-1).getPoint();
        }
        for(TrackPointData point:track)
        {
            GeoPosition gp=null;
            if(point.getTimeStamp().toString().compareTo(time.toString())==0)
            {
                gp=point.getPoint();
                return gp;
            }
            else if(point.getTimeStamp().getTime()>time.getTime())
            {
                return point.getPoint();
            }            
        }
        return null;
    }
    
    public Date getStartTime()
    {
        return track.get(0).getTimeStamp();
    }
    
    public Date getEndTime()
    {
        return track.get(track.size()-1).getTimeStamp();
    }
    
    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder("Track name: "+name+"\n");
        for(TrackPointData point:track)
        {
            sb.append(point.toString()+"\n");
        }
        return sb.toString();
    }
    
    
    
}
