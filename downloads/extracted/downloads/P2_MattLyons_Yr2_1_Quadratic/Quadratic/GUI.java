package Quadratic;

import java.awt.FlowLayout;
import java.awt.Rectangle;
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
    private JSlider aSlider,bSlider,cSlider;
    private JLabel aVal,bVal,cVal,resultVal;
    private JFrame frame;
    
    private Quadratic quad;
    private double a,b,c,result;
    /**
     * Creates new gui with 0.0 values.
     */
    public GUI(){
        quad=new Quadratic(a,b,c);//doubles default to 0.0
        init();
    }
        
        /**
         * Inits widgets
         */
	public void init() {
            frame=new JFrame();
           
            aVal=new JLabel();
            aVal.setText("A: 0");
            bVal=new JLabel();
            bVal.setText("B: 0");
            cVal=new JLabel();
            cVal.setText("C: 0");
            resultVal=new JLabel();
            
            aSlider=new JSlider(JSlider.HORIZONTAL,-1000,1000,0);
            aSlider.addChangeListener(this);
            bSlider=new JSlider(JSlider.HORIZONTAL,-1000,1000,0);
            bSlider.addChangeListener(this);
            cSlider=new JSlider(JSlider.HORIZONTAL,-1000,1000,0);
            cSlider.addChangeListener(this);
            
            frame.setLayout(new FlowLayout(FlowLayout.LEADING));
            frame.setSize(600, 520);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            
            frame.add(aVal);
            frame.add(bVal);
            frame.add(cVal);
            frame.add(aSlider);
            frame.add(bSlider);
            frame.add(cSlider);
            frame.add(resultVal);
            
            postInit();
	}
        /**
         * Stuff to do after init
         */
	public void postInit(){
            frame.setVisible(true);
            stateChanged(new ChangeEvent(aSlider)); //defaults result
        }
    
        /**
         * Slider changed
         * @param e event that changed.
         */
    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource().equals(aSlider)){
            a=aSlider.getValue();
        }
        else if(e.getSource().equals(bSlider)){
            b=bSlider.getValue();
        }
        else if(e.getSource().equals(cSlider)){
            c=cSlider.getValue();
        }
        aVal.setText("A: "+a);
        bVal.setText("B: "+b);
        cVal.setText("C: "+c);
        quad.changeValues(a, b, c);
        resultVal.setText("Solution: "+quad.solve()[0]+", "+quad.solve()[1]);
    }
}
