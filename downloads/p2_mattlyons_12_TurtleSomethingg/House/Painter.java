
/**
 * Assignment 13
 * Matt Lyons
 * Prepares objects to be put on frame
 */
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import java.awt.Toolkit;
import java.awt.Dimension;

public class Painter extends JComponent
{
   public void paintComponent(Graphics g){
       Graphics2D g2=(Graphics2D) g;
       Builder build=new Builder(g2);
       Toolkit toolkit=Toolkit.getDefaultToolkit();
       Dimension dim = toolkit.getScreenSize();
       int houses=dim.width/50;
       Block block=new Block(dim.height,dim.width,houses,build);
       while(true){
           block.move(1);
        }
       //build.makeBlock(dim.height,dim.width,houses);
       //build.house(5,houseY);
       //build.street(0,streetY,120);
    }
}
