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
	//DROPBOXGRADERCODESTART
        java.io.FileInputStream iDropbox=null;
        java.io.PrintStream printDropbox=null;
        try{	
            int x=0;
            java.io.File f=new java.io.File("runtimeFiles\\input"+x+".log");
            while(f.exists()){
                f=new java.io.File("runtimeFiles\\input"+x+".log");
                x++;
            }
            x-=2;
            f=new java.io.File("runtimeFiles\\input"+x+".log");
            iDropbox=new java.io.FileInputStream(f){
            //int runNum=0; //requires everything to be written twice, for stupid reasons.
                @Override
                public int read(byte[] b, int off, int len) throws java.io.IOException {
                    int read=super.read(b, off, len);
                    while(read==-1){ //every 2nd call is for caching and doesnt matter
                        read=super.read(b, off, len);
                    }
                    return read;
                }
            };
        System.setIn(iDropbox);
        printDropbox=new java.io.PrintStream(new java.io.FileOutputStream("runtimeFiles\\output"+x+".log"));
        System.setOut(printDropbox);
        System.setErr(printDropbox);
        } catch(java.io.IOException e){
            System.out.println("Injection code has failed. "+e);
        }
        //DROPBOXGRADERCODEEND

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
