/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package swingworker_proba;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 *
 * @author Dávid
 */
public class PixelHeatMap {

    private int map[][];
    private final String lvlMap;
    private final String outputFile;
    private final int imgWidth;
    private final int imgHeight;
    
    public PixelHeatMap(final ArrayList<Point> points,final String outputFile, final String lvlMap)
    {
        this.lvlMap=lvlMap;
        this.outputFile=outputFile;
        
        BufferedImage img=loadImage(lvlMap);
        imgWidth=img.getWidth();
        imgHeight=img.getHeight();
        
        map=new int[imgWidth][imgHeight];
        
        for(int i=0;i<imgWidth;i++)
            for(int j=0;j<imgHeight;j++)
                map[i][j]=0;
        
        addPoints(points);
        
    }
   
    private void addPoints(final List<Point> points)
    {
        for(Point point:points)
        {
            map[point.x][point.y]++;
        }
    }
    
    private int getIntensity(int w,int h,final int range)
    {
	int sum=0;
	int mini=(w-range>=0)?w-range:0;
	int maxi=(w+range+1<=imgWidth)?w+range+1:imgWidth;
	int minj=(h-range>=0)?h-range:0;
	int maxj=(h+range+1<=imgHeight)?h+range+1:imgHeight;
	for(int i=mini;i<maxi;i++)
            for(int j=minj;j<maxj;j++)
                sum+=map[i][j];
	return sum;
    }

    public void createPixelMap(final int range)
    {
        BufferedImage img=new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics=img.createGraphics();
        graphics.setPaint(Color.BLACK);
        graphics.fillRect(0, 0, imgWidth, imgHeight);
	int max=0;
	for(int h=0;h<imgHeight;h++)
	{
            for(int w=0;w<imgWidth;w++)
            {
                if(getIntensity(w,h,range)>max)
                    max=getIntensity(w,h,range);
            }
	}
        
	for(int h=0;h<imgHeight;h++)
	{
            for(int w=0;w<imgWidth;w++)
            {
                if(getIntensity(w,h,range)!=0)
		{
                    //img.setRGB(w, h, new Color(0+250*getIntensity(w,h,range)/max,0,255-250*getIntensity(w,h,range)/max).getRGB());
                    Graphics g=img.getGraphics();
                    g.setColor(new Color(0+250*getIntensity(w,h,range)/max,0,255-250*getIntensity(w,h,range)/max));
                    //g.fillRect(w-5, h-5, 10, 10);
                    g.fillRect(w, h, 1, 1);
		}
            }
	}        
        
        final BufferedImage output=loadImage(lvlMap);
        addImage(output,img,0.80f);
        saveImage(output,outputFile);
        
        //BufferedImage negatedImage=negateImage(loadImage(lvlMap));
        //final BufferedImage output = loadImage(lvlMap);
        //addImage(output, negatedImage,0.90f);
        //saveImage(negatedImage, outputFile);
        
        //saveImage(output, outputFile);
        System.out.println("done creating heatmap.");
    }

    private void addImage(final BufferedImage buff1, final BufferedImage buff2,
            final float opaque) {
        addImage(buff1, buff2, opaque, 0, 0);
    }    
    
    private void addImage(final BufferedImage buff1, final BufferedImage buff2,
            final float opaque, final int x, final int y) {
        final Graphics2D g2d = buff1.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                opaque));
        g2d.drawImage(buff2, x, y, null);
        g2d.dispose();
    }    
    
    private BufferedImage negateImage(final BufferedImage img) {
        final int width = img.getWidth();
        final int height = img.getHeight();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                final int rGB = img.getRGB(x, y);

                // Swaps values
                // i.e. 255, 255, 255 (white)
                // becomes 0, 0, 0 (black)
                final int r = Math.abs(((rGB >>> 16) & 0xff) - 255); // red
                                                                     // inverted
                final int g = Math.abs(((rGB >>> 8) & 0xff) - 255); // green
                                                                    // inverted
                final int b = Math.abs((rGB & 0xff) - 255); // blue inverted

                // transform back to pixel value and set it
                img.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }    
    
    private BufferedImage loadImage(final String ref) {
        BufferedImage b1 = null;
        try {
            b1 = ImageIO.read(new File(ref));
        } catch (final IOException e) {
            System.out.println("error loading the image: " + ref + " : " + e);
        }
        return b1;
    }
    
    private void saveImage(final BufferedImage buff, final String dest) {
        try {
            final File outputfile = new File(dest);
            ImageIO.write(buff, "png", outputfile);
        } catch (final IOException e) {
            System.out.println("error saving the image: " + dest + ": " + e);
        }
    }    
    
}
