
/**
 * Assignment 13
 * Matt Lyons
 * Makes frame to display swing elements
 */
import javax.swing.JFrame;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Color;

public class Viewer
{
   public static void main(String[] args){
       Painter geometry = new Painter();
       //Size to screensize
       Toolkit toolkit=Toolkit.getDefaultToolkit();
       final Dimension dim = toolkit.getScreenSize();
       final JFrame frame=new JFrame();
       frame.setSize(dim.width,dim.height-45);
       //Compensate for 45px taskbar
       frame.setTitle("Cityscape");
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       JButton refresh = new JButton("New City");
       MouseListener listener=new MouseAdapter() {
           public void mousePressed(MouseEvent mouseEvent){
               frame.setSize(dim.width,dim.height-44);
               frame.setSize(dim.width,dim.height-45);
               //A hacky way to repaint()
            }
        };
       refresh.addMouseListener(listener);
       Container settings=frame.getContentPane();
       settings.add(refresh,BorderLayout.SOUTH);
       //Put in refresh button
       frame.add(geometry);
       //frame.getContentPane().setBackground(Color.black);
       frame.setVisible(true);
       // note to self: ADD FILTERS AND 'GRADs. and a penis...HAH GAYY
    }
}
//See attached sheet for output.