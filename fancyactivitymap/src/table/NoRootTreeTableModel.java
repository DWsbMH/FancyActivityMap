/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package table;

import java.util.List;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import swingworker_proba.TrackData;
import swingworker_proba.TrackPointData;

/**
 *
 * @author Dávid
 */
public class NoRootTreeTableModel extends AbstractTreeTableModel{
    
    private final static String[] COLUMN_NAMES={"Latitude","Longitude","TimeStamp"};
    
    private List<TrackData> trackList;

    public NoRootTreeTableModel(List<TrackData> trackList) {
        super(new Object());
        this.trackList = trackList;
    }

    
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }
    
    public String getColumnName(int column)
    {
        return COLUMN_NAMES[column];
    }
    
    public boolean isCellEditsble(Object node,int column)
    {
        return false;
    }
    
    public boolean isLeaf(Object node)
    {
        return node instanceof TrackPointData; 
    }
    
    @Override
    public Object getValueAt(Object o, int i) {
        if(o instanceof TrackData)
        {
            TrackData track=(TrackData)o;
            
            if(i==0)
            {
                return track.getName();
            }
        }
        else if(o instanceof TrackPointData)
        {
            TrackPointData point=(TrackPointData)o;
            switch(i)
            {
                case 0:
                    return point.getPoint().getLatitude();
                case 1:
                    return point.getPoint().getLatitude();
                case 2:
                    return point.getTimeStamp();
            }
        }
        
        return null;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if(parent instanceof TrackData)
        {
            TrackData track=(TrackData)parent;
            return track.getTrack().get(index);
        }
        return trackList.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        if(parent instanceof TrackData)
        {
            TrackData track=(TrackData)parent;
            return track.getTrack().size();
        }
        
        return trackList.size();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TrackData track=(TrackData)parent;
        TrackPointData point=(TrackPointData)child;
        return track.getTrack().indexOf(point);
    }
    
}
