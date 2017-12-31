/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swingworker_proba;

import com.hs.gpxparser.GPXParser;
import com.hs.gpxparser.modal.GPX;
import com.hs.gpxparser.modal.Track;
import com.hs.gpxparser.modal.TrackSegment;
import com.hs.gpxparser.modal.Waypoint;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.prism.shader.AlphaTextureDifference_Color_AlphaTest_Loader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdesktop.swingx.JXTreeTable;
import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;
import table.NoRootTreeTableModel;

/**
 *
 * @author Dávid
 */
public class MainFrame extends JFrame {

    private ArrayList<TrackData> trackList = new ArrayList<TrackData>();
    private List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();

    public MainFrame(String title) {
        super(title);
        setSize(1280, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JXMapKit mapKit = new JXMapKit();

        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(/*8*/4);

        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);

        mapKit.setTileFactory(tileFactory);
        mapKit.setAddressLocationShown(false);
        mapKit.setCenterPosition(new GeoPosition(34.5133, -94.1629));

        JPanel panelMapKit = new JPanel(new BorderLayout());
        panelMapKit.add(mapKit, BorderLayout.CENTER);
        add(panelMapKit, BorderLayout.CENTER);

        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadMenuItem = new JMenuItem("Load Tracks");
        JMenuItem excelMenuItem = new JMenuItem("Excel");
        fileMenu.add(loadMenuItem);
        fileMenu.add(excelMenuItem);
        menu.add(fileMenu);
        add(menu, BorderLayout.NORTH);

        JButton buttonTest = new JButton("Test");
        //add(buttonTest,BorderLayout.SOUTH);

        JXTreeTable table = new JXTreeTable() {

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }

        };
        JButton buttonShowTracks = new JButton("Show Tracks");
        JButton buttonShowAll = new JButton("Show All Tracks");
        JButton buttonHideAll = new JButton("Hide All Tracks");
        JButton buttonHeatMap = new JButton("Heatmap from Tracks");
        JButton buttonHeatMapPixel = new JButton("Pixel based Heatmap");

        //List<Painter<JXMapViewer>> painters=new ArrayList<Painter<JXMapViewer>>();
        JLabel lineLabel = new JLabel("Line width");
        JSlider lineWidth = new JSlider(1, 10, 2);

        lineWidth.setMajorTickSpacing(1);
        lineWidth.setPaintTicks(true);
        lineWidth.setPaintLabels(true);

        JButton buttonColor = new JButton("Pick a Color");

        JLabel labelTransparency = new JLabel("Line transparency");

        JSlider alphaT = new JSlider(0, 255, 255);
        alphaT.setMajorTickSpacing(51);
        alphaT.setPaintTicks(true);
        alphaT.setPaintLabels(true);

        loadMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File("."));
                chooser.setDialogTitle("Select a folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);

                if (chooser.showOpenDialog(loadMenuItem) == JFileChooser.APPROVE_OPTION) {
                    File[] dir = chooser.getSelectedFile().listFiles(new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".gpx");
                        }
                    });

                    GPXParser gpxParser = new GPXParser();
                    for (File file : dir) {
                        try {
                            FileInputStream gpxInput = new FileInputStream(file);
                            GPX gpx = gpxParser.parseGPX(gpxInput);
                            HashSet<Track> trackSet = gpx.getTracks();

                            for (Track trackTemp : trackSet) {
                                TrackData track = new TrackData(trackTemp.getName());
                                ArrayList<TrackSegment> trackSegment = trackTemp.getTrackSegments();
                                for (TrackSegment trackSegmentTemp : trackSegment) {
                                    ArrayList<Waypoint> waypoints = trackSegmentTemp.getWaypoints();
                                    for (Waypoint waypoint : waypoints) {
                                        TrackPointData point = new TrackPointData(new GeoPosition(waypoint.getLatitude(), waypoint.getLongitude()), waypoint.getTime());
                                        track.addPoint(point);
                                    }
                                }
                                trackList.add(track);
                            }
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (dir.length != 0) {
                        NoRootTreeTableModel noRootTreeTableModel = new NoRootTreeTableModel(trackList);
                        table.setTreeTableModel(noRootTreeTableModel);
                        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                        JPanel panelFrameW = new JPanel(new BorderLayout());
                        panelFrameW.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
                        JPanel panelTrackButtons1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        JPanel panelTrackButtons2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        JPanel panelTrack = new JPanel(new BorderLayout());
                        panelTrackButtons1.add(buttonShowTracks);
                        panelTrackButtons1.add(buttonShowAll);
                        panelTrackButtons1.add(buttonHeatMapPixel);
                        panelTrackButtons2.add(buttonHideAll);
                        panelTrackButtons2.add(buttonHeatMap);
                        panelTrack.add(panelTrackButtons1, BorderLayout.NORTH);
                        panelTrack.add(panelTrackButtons2, BorderLayout.SOUTH);
                        panelFrameW.add(panelTrack, BorderLayout.SOUTH);
                        add(panelFrameW, BorderLayout.WEST);

                        JPanel panelCustomizeTrack = new JPanel();
                        panelCustomizeTrack.add(lineLabel);
                        panelCustomizeTrack.add(lineWidth);
                        panelCustomizeTrack.add(buttonColor);
                        panelCustomizeTrack.add(labelTransparency);
                        panelCustomizeTrack.add(alphaT);
                        add(panelCustomizeTrack, BorderLayout.SOUTH);

                        setVisible(true);
                    }
                }
            }
        });

        excelMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser fileChooser = new JFileChooser(new File("."));
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    FileNameExtensionFilter excelFilter = new FileNameExtensionFilter("Excel file", "xlsx");
                    fileChooser.addChoosableFileFilter(excelFilter);
                    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                        String excelFilePath = fileChooser.getSelectedFile().getPath();
                        InputStream excelFile = new FileInputStream(excelFilePath);
                        XSSFWorkbook wb = new XSSFWorkbook(excelFile);
                        XSSFSheet sheet = wb.getSheetAt(0);
                        Iterator rows = sheet.rowIterator();
                        boolean headerRow = true;
                        List<long[]> rowList = new ArrayList<long[]>();
                        byte fps = 20;
                        float videoLengthInMinutes = 2;
                        long startTime = Long.MAX_VALUE;
                        long endTime = 0;
                        byte startRaceTimeIndex=-1;
                        while (rows.hasNext()) {
                            XSSFRow row = (XSSFRow) rows.next();
                            Iterator cells = row.cellIterator();
                            byte cellCount = 0;
                            if (headerRow) {
                                while(cells.hasNext())
                                {
                                    XSSFCell cell = (XSSFCell) cells.next();
                                    if(cell.toString().compareToIgnoreCase("Start Race Time")==0)
                                    {
                                        startRaceTimeIndex=cellCount;
                                    }
                                    cellCount+=1;
                                }
                                headerRow = false;
                            } else {
                                long[] startAndEndTime = new long[2];
                                while (cells.hasNext()) {
                                    if (cellCount == startRaceTimeIndex) {
                                        XSSFCell cell = (XSSFCell) cells.next();
                                        if (!cell.toString().isEmpty()) {
                                            startAndEndTime[0] = LocalTime.parse(cell.toString()).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                        }
                                    }
                                    if ((cellCount == row.getLastCellNum()-2) && (startAndEndTime[0] > 0L)) {
                                        XSSFCell cell = (XSSFCell) cells.next();
                                        if (!cell.toString().isEmpty()) {
                                            startAndEndTime[1] = LocalTime.parse(cell.toString()).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                                            rowList.add(startAndEndTime);
                                            if (startTime > startAndEndTime[0]) {
                                                startTime = startAndEndTime[0];
                                            }
                                            if (endTime < startAndEndTime[1]) {
                                                endTime = startAndEndTime[1];
                                            }
                                        }
                                    } else {
                                        cells.next();
                                    }
                                    cellCount += 1;
                                }
                            }
                        }
                        wb.close();
                        excelFile.close();
                        long videoLengthInMiliSeconds = (long) (videoLengthInMinutes * 60 * 1000);
                        long timeInMillisecond = (endTime - startTime) / (fps * (videoLengthInMiliSeconds / 1000));

                        FileNameExtensionFilter gpxFilter = new FileNameExtensionFilter("Gpx files", "gpx");
                        fileChooser.removeChoosableFileFilter(excelFilter);
                        fileChooser.setSelectedFile(new File(""));
                        fileChooser.addChoosableFileFilter(gpxFilter);
                        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                            String gpxFile = fileChooser.getSelectedFile().getPath();
                            GPXParser gpxParser = new GPXParser();
                            FileInputStream gpxInput = new FileInputStream(gpxFile);
                            GPX gpx = gpxParser.parseGPX(gpxInput);
                            ArrayList<double[]> geoPoints = new ArrayList<>();
                            for (Track track : gpx.getTracks()) {
                                for (TrackSegment tSegment : track.getTrackSegments()) {
                                    for (Waypoint wp : tSegment.getWaypoints()) {
                                        double[] latLong = new double[2];
                                        latLong[0] = wp.getLatitude();
                                        latLong[1] = wp.getLongitude();
                                        geoPoints.add(latLong);
                                    }
                                }
                            }
                            for (double[] geoPoint : geoPoints) {
                                System.out.println("lat: " + geoPoint[0] + " long: " + geoPoint[1]);
                            }
                            System.out.println(geoPoints.size());
                            System.out.println(startTime + " , " + endTime);
                            float timeDiff = (endTime - startTime) / geoPoints.size();
                            long total = startTime;
                            for (int i = 0; i < geoPoints.size(); i++) {
                                System.out.println("lat: " + geoPoints.get(i)[0] + " long: " + geoPoints.get(i)[1] + " timestamp: " + new Date(total));
                                total += (long) timeDiff;
                            }
                            System.out.println("start: " + new Date(startTime) + " end: " + new Date(endTime));
                        }
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        lineWidth.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (!lineWidth.getValueIsAdjusting()) {
                    boolean l = false;
                    for (Painter<JXMapViewer> painter : painters) {
                        if (painter.getClass().getTypeName().compareTo("swingworker_proba.RoutePainter") == 0) {
                            RoutePainter rpTemp = (RoutePainter) painter;
                            rpTemp.setLineWidth(lineWidth.getValue());
                            painters.set(painters.indexOf(painter), rpTemp);
                            l = true;
                        }
                    }
                    if (l) {
                        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                        mapKit.getMainMap().setOverlayPainter(painter);
                    }
                }
            }
        });

        buttonColor.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (painters.size() > 0) {
                    RoutePainter rpTemp = (RoutePainter) painters.get(0);
                    Color newColor = JColorChooser.showDialog(null, "Choose a color", rpTemp.getColor());
                    boolean l = false;
                    if (newColor == null) {
                        newColor = rpTemp.getColor();
                    }
                    for (Painter<JXMapViewer> painter : painters) {
                        if (painter.getClass().getTypeName().compareTo("swingworker_proba.RoutePainter") == 0) {
                            rpTemp = (RoutePainter) painter;
                            rpTemp.setColor(newColor);
                            painters.set(painters.indexOf(painter), rpTemp);
                            l = true;
                        }
                    }
                    if (l) {
                        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                        mapKit.getMainMap().setOverlayPainter(painter);
                        alphaT.setValue(newColor.getAlpha());
                    }
                }
            }
        });

        alphaT.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (!alphaT.getValueIsAdjusting()) {
                    if (painters.size() > 0) {
                        RoutePainter rpTemp = (RoutePainter) painters.get(0);
                        int red = rpTemp.getColor().getRed();
                        int green = rpTemp.getColor().getGreen();
                        int blue = rpTemp.getColor().getBlue();
                        int alpha = alphaT.getValue();
                        Color transparency = new Color(red, green, blue, alpha);
                        for (Painter<JXMapViewer> painter : painters) {
                            rpTemp = (RoutePainter) painter;
                            rpTemp.setColor(transparency);
                            painters.set(painters.indexOf(painter), rpTemp);
                        }

                        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                        mapKit.getMainMap().setOverlayPainter(painter);
                    }
                }
            }
        });

        buttonShowTracks.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] indexes = table.getSelectedRows();
                if (indexes.length > 0) {
                    ArrayList<GeoPosition> geoPositions = new ArrayList<>();
                    Color color = null;
                    if (painters.size() > 0) {
                        RoutePainter rp = (RoutePainter) painters.get(0);
                        color = rp.getColor();
                    }
                    painters = new ArrayList<>();
                    for (int i = 0; i < indexes.length; i++) {
                        int j = 0;
                        boolean ok = false;

                        while (j < trackList.size() && !ok) {
                            if (table.getValueAt(indexes[i], 0).toString().compareTo(trackList.get(j).getName()) == 0) {
                                RoutePainter rp = new RoutePainter(trackList.get(j).getTrack());
                                rp.setLineWidth(lineWidth.getValue());
                                if (color != null) {
                                    rp.setColor(color);
                                }
                                painters.add(rp);
                                geoPositions.addAll(trackList.get(j).getGeoPositions());
                                ok = true;
                            } else {
                                j++;
                            }
                        }
                    }
                    CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                    mapKit.getMainMap().setOverlayPainter(painter);
                    mapKit.getMainMap().zoomToBestFit(new HashSet<GeoPosition>(geoPositions), 1);
                }
            }
        });

        buttonShowAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (trackList.size() > 0) {
                    ArrayList<GeoPosition> geoPositions = new ArrayList<>();
                    Color color = null;
                    if (painters.size() > 0) {
                        RoutePainter rp = (RoutePainter) painters.get(0);
                        color = rp.getColor();
                    }
                    painters = new ArrayList<>();
                    for (TrackData tracks : trackList) {
                        RoutePainter rp = new RoutePainter(tracks.getTrack());
                        rp.setLineWidth(lineWidth.getValue());
                        if (color != null) {
                            rp.setColor(color);
                        }
                        painters.add(rp);
                        geoPositions.addAll(tracks.getGeoPositions());
                    }
                    CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                    mapKit.getMainMap().setOverlayPainter(painter);
                    mapKit.getMainMap().zoomToBestFit(new HashSet<GeoPosition>(geoPositions), 1);
                }
            }
        });

        buttonHideAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (painters.size() > 0) {
                    painters = new ArrayList<>();
                    CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                    mapKit.getMainMap().setOverlayPainter(painter);
                }
            }
        });

        buttonHeatMap.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (trackList.size() > 0) {
                    TrackData marathon = null;
                    for (TrackData trackDataTemp : trackList) {
                        if (trackDataTemp.getName().compareTo("Running 10/9/16 9:24 am") == 0) {
                            marathon = trackDataTemp;
                            break;
                        }
                    }
                    if (marathon != null) {
                        int numberOfAhletes = 100;
                        TrackData[] athletes = new TrackData[numberOfAhletes];

                        for (int i = 0; i < athletes.length; i++) {
                            athletes[i] = new TrackData(null);
                            athletes[i].addPoint(marathon.getTrack().get(0));
                        }

                        for (int i = 0; i < athletes.length; i++) {
                            TrackPointData prevPoint = athletes[i].getTrack().get(0);

                            double kmph = kmPerHourRandom(7.5, 20);
                            int change = 0 + new Random().nextInt(marathon.getTrack().size() - 0 + 1);

                            for (int j = 1; j < marathon.getTrack().size(); j++) {
                                double dist = distance(prevPoint.getPoint(), marathon.getGeoPositions().get(j));

                                double timeStamp1 = prevPoint.getTimeStamp().getTime();

                                double mps = (kmph * 1000) / 3600;

                                double timeStamp2 = ((dist * 1000) / mps) + timeStamp1;

                                Date currentTime = new Date((long) timeStamp2);

                                TrackPointData currentPoint = new TrackPointData(marathon.getGeoPositions().get(j), currentTime);

                                athletes[i].addPoint(currentPoint);

                                prevPoint = currentPoint;

                                if (j == change) {
                                    kmph = kmPerHourRandom(7.5, 20);
                                    change = (j + 1) + new Random().nextInt(marathon.getTrack().size() - (j + 1) + 1);
                                }

                            }
                        }

                        double videoLengthInMinutes = 1;
                        int fps = 20;
                        long endTime = 0;
                        long startTime = marathon.getTrack().get(0).getTimeStamp().getTime();

                        for (TrackData athlete : athletes) {
                            if (athlete.getTrack().get(athlete.getTrack().size() - 1).getTimeStamp().getTime() > endTime) {
                                endTime = athlete.getTrack().get(athlete.getTrack().size() - 1).getTimeStamp().getTime();
                            }
                        }

                        long videoLengthInMiliSeconds = (long) (videoLengthInMinutes * 60 * 1000);
                        long snapshotTimeInMiliSeconds = (endTime - startTime) / (fps * (videoLengthInMiliSeconds / 1000));

                        /*mapKit.getMainMap().setOverlayPainter(null);
                         mapKit.setMiniMapVisible(false);
                         mapKit.setZoomButtonsVisible(false);
                         mapKit.setZoomSliderVisible(false);*/
                         //int j=1;
                        snapShotMaker(mapKit, marathon, athletes, startTime, endTime, snapshotTimeInMiliSeconds,/*athletes.length*/ 0.1f);
                        /*Rectangle bounds=mapKit.getMainMap().getViewportBounds();
                         
                         for(long time=startTime;time<=endTime;time+=snapshotTimeInMiliSeconds)
                         {
                         ArrayList<Point> points=new ArrayList<>();
                             
                         for(TrackData athlete:athletes)
                         {
                         Date newDate=new Date(time);
                         GeoPosition geoPoint=null;
                         geoPoint=athlete.getGeoPosition(newDate);
                         Point2D p=mapKit.getMainMap().getTileFactory().geoToPixel(geoPoint, mapKit.getMainMap().getZoom());
                         int x=(int)p.getX()-bounds.x;
                         int y=(int)p.getY()-bounds.y;
                         Point point=new Point(x, y);
                         points.add(point);
                         }
                             
                         try {
                         BufferedImage img=getScreenShot(mapKit.getMainMap());
                         File dir=new File("videos");
                         dir.mkdir();
                         String fileName="videos/image"+String.format("%05d", j)+".jpg";
                         File file=new File(fileName);                                 
                         ImageIO.write(img, "jpeg", file);
                                 
                         HeatMap heatMap=new HeatMap(points,fileName , fileName);
                         heatMap.createHeatMap(10f);
                         j++;                                 
                         } catch (IOException ex) {
                         Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                         }
                         }
                         
                         mapKit.setMiniMapVisible(true);
                         mapKit.setZoomButtonsVisible(true);
                         mapKit.setZoomSliderVisible(true);*/
                    }
                }
            }
        });

        buttonHeatMapPixel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (trackList.size() > 0) {
                    TrackData marathon = null;

                    for (TrackData trackDataTemp : trackList) {
                        if (trackDataTemp.getName().compareTo("Running 10/9/16 9:24 am") == 0) {
                            marathon = trackDataTemp;
                            break;
                        }
                    }
                    if (marathon != null) {
                        int numberOfAhletes = 100;
                        TrackData[] athletes = new TrackData[numberOfAhletes];

                        for (int i = 0; i < athletes.length; i++) {
                            athletes[i] = new TrackData(null);
                            athletes[i].addPoint(marathon.getTrack().get(0));
                        }

                        System.out.println(new Date());

                        for (int i = 0; i < athletes.length; i++) {
                            TrackPointData prevPoint = athletes[i].getTrack().get(0);

                            double kmph = kmPerHourRandom(7.5, 20);
                            int change = 0 + new Random().nextInt(marathon.getTrack().size() - 0 + 1);

                            for (int j = 1; j < marathon.getTrack().size(); j++) {
                                double dist = distance(prevPoint.getPoint(), marathon.getGeoPositions().get(j));

                                double timeStamp1 = prevPoint.getTimeStamp().getTime();

                                double mps = (kmph * 1000) / 3600;

                                double timeStamp2 = ((dist * 1000) / mps) + timeStamp1;

                                Date currentTime = new Date((long) timeStamp2);

                                TrackPointData currentPoint = new TrackPointData(marathon.getGeoPositions().get(j), currentTime);

                                athletes[i].addPoint(currentPoint);

                                prevPoint = currentPoint;

                                if (j == change) {
                                    kmph = kmPerHourRandom(7.5, 20);
                                    change = (j + 1) + new Random().nextInt(marathon.getTrack().size() - (j + 1) + 1);
                                }

                            }
                            System.out.println((i + 1) + ". athlete");
                        }

                        System.out.println(new Date());

                        double videoLengthInMinutes = 1;
                        int fps = 20;
                        long endTime = 0;
                        long startTime = marathon.getTrack().get(0).getTimeStamp().getTime();

                        for (TrackData athlete : athletes) {
                            if (athlete.getTrack().get(athlete.getTrack().size() - 1).getTimeStamp().getTime() > endTime) {
                                endTime = athlete.getTrack().get(athlete.getTrack().size() - 1).getTimeStamp().getTime();
                            }
                        }

                        long videoLengthInMiliSeconds = (long) (videoLengthInMinutes * 60 * 1000);
                        long snapshotTimeInMiliSeconds = (endTime - startTime) / (fps * (videoLengthInMiliSeconds / 1000));

                        pixelMapMaker(mapKit, marathon, athletes, startTime, endTime, snapshotTimeInMiliSeconds);
                    }
                }
            }
        });

        buttonTest.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<GeoPosition> setList = new ArrayList<>();
                setList.add(new GeoPosition(47.748269, 17.631598));
                setList.add(new GeoPosition(47.764484, 17.662067));

                start(mapKit, setList);
            }
        });

        setVisible(true);
    }

    private void start(JXMapKit mapKit, ArrayList<GeoPosition> list) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                mapKit.getMainMap().zoomToBestFit(new HashSet<GeoPosition>(list), 1);
                mapKit.setMiniMapVisible(false);
                mapKit.setZoomButtonsVisible(false);
                mapKit.setZoomSliderVisible(false);
                setEnabled(false);
                mapKit.setEnabled(false);
                Thread.sleep(500);
                return null;
            }

            @Override
            protected void done() {
                for (int i = 0; i < 1000000; i++) {
                    System.out.println(i + 1);
                }
                setEnabled(true);
                mapKit.setEnabled(true);
                mapKit.setMiniMapVisible(true);
                mapKit.setZoomButtonsVisible(true);
                mapKit.setZoomSliderVisible(true);
            }

        };

        worker.execute();
    }

    private void snapShotMaker(JXMapKit mapKit, TrackData athlete, TrackData[] athletes, long startTime, long endTime, long snapshotTimeInMiliSeconds, float heatPoints) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                mapKit.getMainMap().zoomToBestFit(new HashSet<GeoPosition>(athlete.getGeoPositions()), 1);
                mapKit.getMainMap().setOverlayPainter(null);
                mapKit.setMiniMapVisible(false);
                mapKit.setZoomButtonsVisible(false);
                mapKit.setZoomSliderVisible(false);
                Thread.sleep(2000);
                return null;
            }

            @Override
            protected void done() {
                Rectangle bounds = mapKit.getMainMap().getViewportBounds();

                int j = 1;

                for (long time = startTime; time <= endTime; time += snapshotTimeInMiliSeconds) {
                    ArrayList<Point> points = new ArrayList<>();

                    for (TrackData athlete : athletes) {
                        Date newDate = new Date(time);
                        GeoPosition geoPoint = null;
                        geoPoint = athlete.getGeoPosition(newDate);
                        Point2D p = mapKit.getMainMap().getTileFactory().geoToPixel(geoPoint, mapKit.getMainMap().getZoom());
                        int x = (int) p.getX() - bounds.x;
                        int y = (int) p.getY() - bounds.y;
                        Point point = new Point(x, y);
                        points.add(point);
                    }

                    try {
                        BufferedImage img = getScreenShot(mapKit.getMainMap());
                        File dir = new File("videos");
                        dir.mkdir();
                        String fileName = "videos/image" + String.format("%05d", j) + ".jpg";
                        File file = new File(fileName);
                        ImageIO.write(img, "jpeg", file);

                        HeatMap heatMap = new HeatMap(points, fileName, fileName);
                        heatMap.createHeatMap(heatPoints);
                        j++;
                    } catch (IOException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                mapKit.setMiniMapVisible(true);
                mapKit.setZoomButtonsVisible(true);
                mapKit.setZoomSliderVisible(true);
                System.out.println("Finished");
            }

        };
        worker.execute();
    }

    private void pixelMapMaker(JXMapKit mapKit, TrackData athlete, TrackData[] athletes, long startTime, long endTime, long snapshotTimeInMiliSeconds) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                mapKit.getMainMap().zoomToBestFit(new HashSet<GeoPosition>(athlete.getGeoPositions()), 1);
                mapKit.getMainMap().setOverlayPainter(null);
                mapKit.setMiniMapVisible(false);
                mapKit.setZoomButtonsVisible(false);
                mapKit.setZoomSliderVisible(false);
                Thread.sleep(2000);
                return null;
            }

            @Override
            protected void done() {
                Rectangle bounds = mapKit.getMainMap().getViewportBounds();

                int j = 1;

                System.out.println(new Date());

                for (long time = startTime; time <= endTime; time += snapshotTimeInMiliSeconds) {
                    ArrayList<Point> points = new ArrayList<>();

                    for (TrackData athlete : athletes) {
                        Date newDate = new Date(time);
                        GeoPosition geoPoint = null;
                        geoPoint = athlete.getGeoPosition(newDate);
                        Point2D p = mapKit.getMainMap().getTileFactory().geoToPixel(geoPoint, mapKit.getMainMap().getZoom());
                        int x = (int) p.getX() - bounds.x;
                        int y = (int) p.getY() - bounds.y;
                        Point point = new Point(x, y);
                        points.add(point);
                    }

                    if (time == (startTime + snapshotTimeInMiliSeconds)) {
                        try {
                            PrintWriter writer = new PrintWriter("points.txt");
                            for (Point point : points) {
                                writer.println(point.x + "\t" + point.y);
                            }
                            writer.close();
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    try {
                        BufferedImage img = getScreenShot(mapKit.getMainMap());
                        File dir = new File("videos2");
                        dir.mkdir();
                        String fileName = "videos2/image" + String.format("%05d", j) + ".jpg";
                        File file = new File(fileName);
                        ImageIO.write(img, "jpeg", file);

                        PixelHeatMap pixelMap = new PixelHeatMap(points, fileName, fileName);
                        pixelMap.createPixelMap(5);
                        j++;
                    } catch (IOException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

                mapKit.setMiniMapVisible(true);
                mapKit.setZoomButtonsVisible(true);
                mapKit.setZoomSliderVisible(true);
                System.out.println(new Date());
                System.out.println("Finished");
            }

        };

        worker.execute();
    }

    protected static BufferedImage getScreenShot(Component component) {
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        component.paint(image.getGraphics());

        return image;
    }

    private static double distance(GeoPosition gp1, GeoPosition gp2) {
        double lat1 = gp1.getLatitude() * Math.PI / 180;
        double lon1 = gp1.getLongitude() * Math.PI / 180;
        double lat2 = gp2.getLatitude() * Math.PI / 180;
        double lon2 = gp2.getLongitude() * Math.PI / 180;

        double r = 6378100;

        double rho1 = r * Math.cos(lat1);
        double z1 = r * Math.sin(lat1);
        double x1 = rho1 * Math.cos(lon1);
        double y1 = rho1 * Math.sin(lon1);

        double rho2 = r * Math.cos(lat2);
        double z2 = r * Math.sin(lat2);
        double x2 = rho2 * Math.cos(lon2);
        double y2 = rho2 * Math.sin(lon2);

        double dot = x1 * x2 + y1 * y2 + z1 * z2;
        double cosTheta = dot / Math.pow(r, 2);

        double theta = Math.acos(cosTheta);

        return r * theta;
    }

    private static double kmPerHourRandom(double min, double max) {
        Random r = new Random();
        double range = max - min;
        double scaled = r.nextDouble() * range;
        return scaled + min;
    }

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame("MapTool");
    }

}
