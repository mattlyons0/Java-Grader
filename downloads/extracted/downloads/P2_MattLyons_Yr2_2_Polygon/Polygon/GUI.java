package Polygon;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Manages gui stuffs
 * @author Matt
 */
public class GUI extends JFrame implements ChangeListener{
    private Polygon poly;
    private RegularPolygon polyData;
    private JSlider sideSlider,lengthSlider;
    private JLabel sideLabel,lengthLabel;
    private int sides;
    private double length;
    /**
     * Creates new gui with default constructor
     */
    public GUI(){
        super("Polygons");
        polyData=new RegularPolygon();
        poly=new Polygon(polyData.getXVerts(),polyData.getYVerts(),polyData.getNumSide()); 
        
        init();
    }

    /**
     * Inits widgets
    */
    public void init() {
        sideLabel=new JLabel();
        lengthLabel=new JLabel();
        
        sideSlider=new JSlider(JSlider.HORIZONTAL,3,100,3);
        sideSlider.addChangeListener(this);
        lengthSlider=new JSlider(JSlider.HORIZONTAL,2,100,50);
        lengthSlider.addChangeListener(this);
        
        add(sideLabel);
        add(sideSlider);
        add(lengthLabel);
        add(lengthSlider);
        
        setLayout(new FlowLayout(FlowLayout.LEADING));
        Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
        setSize(d.width, d.height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        stateChanged(new ChangeEvent(sideSlider));
        stateChanged(new ChangeEvent(lengthSlider)); //set label values
        
        
        postInit();
    }
    /**
     * Stuff to do after init
    */
    public void postInit(){
        setVisible(true);
    }
    public void updatePoly(){
        poly=new Polygon(polyData.getXVerts(),polyData.getYVerts(),polyData.getNumSide());
    }
    @Override
    public void paint(Graphics g){
        super.paint(g);
        g.drawPolygon(poly);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource().equals(sideSlider)){
            polyData.setNumSides(sideSlider.getValue());
            sideLabel.setText("Sides: "+sideSlider.getValue());
        }
        else if(e.getSource().equals(lengthSlider)){
            polyData.setSideLength(lengthSlider.getValue()/10.0);
            lengthLabel.setText("Length: "+lengthSlider.getValue()/10.0);
        }
        updatePoly();
        repaint();
    }
}
