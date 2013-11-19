
/**
 * Assignment 13
 * Matt Lyons
 * Builds objects to be drawn
 */

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.util.Random;
public class Builder
{
   private boolean debug=false;
   private Graphics2D g2;
   private int xOffset;
   private int[][] housesValues;
   /*I put in the offset so I could make it scroll down the street
   although with all the random perameters that would be hard to 
   replicate since theres no way to change the position of 2d
   geometry without re-creating it. Originally I planned maybe
   1 or 2 random parameters but it turned out to be a huge 
   ammount. I thought it would be maybe 10 lines max to draw
   each house but if I knew it would have been this big I 
   would have made a house object for each house, and seperate
   it into steps such as floors at a time (and even seperating
   making rectangles so I could fill them ahead of time) Also I 
   have to fill the rectangles in order so the windows dont get
   covered, so that made the code messy fast. Had a blast writing
   it though. I may animate it in the future and refactor the code.
   */
   public Builder(Graphics2D geometry){
       g2=geometry;
       xOffset=0;
    }
    public void setOffset(int offset){
        xOffset=offset;
    }
   public void house(int x,int y){
       //Draws a house based on positions required
       //Floor1 (can be 25-175px tall)
       int floorHeight=random(150)+25;
       Rectangle floor1=
        new Rectangle(x-xOffset,y-floorHeight,50,floorHeight);
       g2.setColor(randColor());
       g2.fillRect(x-xOffset,y-floorHeight,50,floorHeight);
       int floor1Bottom=(int)floor1.getMaxY()-20;
       int doorX=random(40)+x;
       //Door
       //(must be at ground level, can be anywhere on x within house)
       Rectangle door=
        new Rectangle(doorX-xOffset,floor1Bottom,10,20);
       g2.setColor(randColor());
       g2.fillRect(doorX-xOffset,floor1Bottom,10,20);
       int floor1Top=(int)floor1.getMinY();
       int windowSize=random(5)+5;
       int window1X=random(40-windowSize)+5+x;
       int window1Y=random(floor1Bottom-(floor1Top+windowSize))
       +floor1Top;
       if(random(2)==1){
           //1/2 change has window on 1st floor
           //Window can be anywhere above door on y, anywhere within
           //building, x
           Rectangle window1=
            new Rectangle(window1X,window1Y,windowSize,windowSize);
           g2.setColor(randColor());
           g2.fillRect(window1X,window1Y,windowSize,windowSize);
           g2.setColor(randColor());
           g2.draw(window1);
        }
       int floors=random(2);
       int floor2Height=random(150)+25;
       double bottomRoof=(int)floor1.getMinY();
       if(debug)System.out.println("Floors: "+floors+" Floor1Height: "
       +floorHeight+" Floor2Height: "+floor2Height+" Floor1Top: "
       +floor1Top+" Floor1Bottom: "+floor1Bottom+"\n");
       if(floors==2){
           //1/2 chance there are 2 floors
           //2nd floor can be 25-175px tall
           Rectangle floor2=
            new Rectangle(x-xOffset,floor1Top-floor2Height,50,
            floor2Height);
           g2.setColor(randColor());
           g2.fillRect(x-xOffset,floor1Top-floor2Height,50,
            floor2Height);
           int floor2Bottom=(int)floor2.getMaxY();
           int floor2Top=(int)floor2.getMinY();
           if(debug)System.out.println("Floor2Top: "+floor2Top+
            " Floor2Bottom: " +floor2Bottom);
           int window2X=random(40-windowSize)+5+x;
           int window2Y=random(floor2Bottom-(floor2Top+windowSize))+
            floor2Top;
           if(random(2)==2){
               //1/2 chance for window on floor 2
               //window can be anywhere on y axis of floor
               //can be anywhere within building on x
               Rectangle window2=
                new Rectangle(window2X,window2Y,windowSize,windowSize);
               g2.setColor(randColor());
               g2.fillRect(window2X,window2Y,windowSize,windowSize);
               g2.setColor(randColor());
               g2.draw(window2);
            }
           g2.setColor(randColor());
           g2.draw(floor2);
           bottomRoof=(int)floor2.getMinY();
       }
       //Make roof (between 5 and 45 pixels high)
       int roofHeight=random(20)+5;
       Point2D.Double roofP1=
        new Point2D.Double(x-xOffset,bottomRoof);
       Point2D.Double roofP2=
        new Point2D.Double(x-xOffset+25,bottomRoof-roofHeight);
       Point2D.Double roofP3=
        new Point2D.Double(x-xOffset+50,bottomRoof);
       Line2D.Double roofL1=
        new Line2D.Double(roofP1,roofP2);
       Line2D.Double roofL2=
        new Line2D.Double(roofP2,roofP3);
       g2.setColor(randColor());
       //Fill roof
       int[] xPoints=new int[3];
       xPoints[0]=x-xOffset;
       xPoints[1]=x-xOffset+25;
       xPoints[2]=x-xOffset+50;
       int[] yPoints=new int[3];
       yPoints[0]=(int)bottomRoof;
       yPoints[1]=(int)bottomRoof-roofHeight;
       yPoints[2]=(int)bottomRoof;
       g2.fillPolygon(xPoints,yPoints,3);
       //Draw floor1 and roof outline
       g2.setColor(randColor());
       g2.draw(floor1);
       g2.setColor(randColor());
       g2.draw(door);
       g2.setColor(randColor());
       g2.draw(roofL1);
       g2.draw(roofL2);
    }
   public void street(int x,int y, int width){
       //Top Street Line
       Point2D.Double topPoint1=
        new Point2D.Double(x,y+20);
       Point2D.Double topPoint2=
        new Point2D.Double(x+width,y+20);
       Line2D.Double topLine=
        new Line2D.Double(topPoint1,topPoint2);
       //Bottom Street Line
       Point2D.Double bottomPoint1=
        new Point2D.Double(x,y-20);
       Point2D.Double bottomPoint2=
        new Point2D.Double(x+width,y-20);
       Line2D.Double bottomLine=
        new Line2D.Double(bottomPoint1,bottomPoint2);
       //Dashed Lines in middle of street
       for(x=width/30;x>0;x--){
           Rectangle sideMarker=
            new Rectangle(x*30-15,y,10,4);
           g2.setColor(Color.orange);
           g2.fillRect(x*30-15,y,10,4);
           g2.draw(sideMarker);
        }
       g2.setColor(Color.black);
       g2.draw(topLine);
       g2.draw(bottomLine);
    }
   public int random(int max){
       //Returns saved value of random output 1-max
       Random gen=new Random();
       int numb=0;
       if(max>0){
           numb=gen.nextInt(max)+1;
       }
    
       return numb;
    }
   public Color randColor(){
       /*Generates random color to paint lines with
       It creates a cool effect with the outline differant
       from the inside and the idea was to make each house
       unique, so this does that.
       */
       int r=random(256)-1;
       int g=random(256)-1;
       int b=random(256)-1;
       Color color=new Color(r,g,b);
       return color;
    }
   public void makeBlock(int height, int width, int houses){
           for(int x=houses;x>0;x--){
               int houseY=height/2-30;
               int streetY=height/2;
               house(x*50-50,houseY);
               street(0,streetY,width);
            }
    }
   public String toString(){
       String str="X Offset: "+xOffset;
       return str;
       //not too useful since offset is unimplemented
    }
}
