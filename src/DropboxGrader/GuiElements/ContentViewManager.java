/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import DropboxGrader.Gui;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DebugGraphics;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 *
 * @author 141lyonsm
 */
public class ContentViewManager extends JDesktopPane{    
    private JInternalFrame backgroundPanel;
    
    private ArrayList<ContentView> views;
    private ArrayList<ContentOverlay> overlays;
    private int selectedView;
    private GridBagConstraints constraints;
    
    public ContentViewManager(){
        super();
        
        views=new ArrayList();
        overlays=new ArrayList();
        selectedView=-1;
        backgroundPanel=new JInternalFrame("",false,false,false,false);
        backgroundPanel.setLayout(new GridBagLayout());
        backgroundPanel.setSize(100,100);
        backgroundPanel.setBorder(null);
        BasicInternalFrameUI ui=(BasicInternalFrameUI) backgroundPanel.getUI();
        ui.setNorthPane(null);
        backgroundPanel.setVisible(true);
        
        add(backgroundPanel,JLayeredPane.DEFAULT_LAYER);
        constraints=new GridBagConstraints();
        constraints.weightx=1;
        constraints.weighty=1;
        constraints.fill=GridBagConstraints.BOTH;
        
        setVisible(true);
    }
    public void addView(ContentView v){
        v.setup();
        views.add(v);
    }
    public void changeView(String viewName){
        for(int i=0;i<views.size();i++){
            if(views.get(i).hasName(viewName)){
                changeView(i);
                revalidate();
                repaint();
                return;
            }
        }
    }
    public ContentView getContentView(String viewName){
        for(ContentView v:views){
            if(v.hasName(viewName)){
                return v;
            }
        }
        return null;
    }
    public boolean selectedViewNameEquals(String otherViewName){
        if(views.get(selectedView).getViewName().equals(otherViewName)){
            return true;
        }
        return false;
    }
    public void addOverlay(ContentOverlay o){
        o.setup();
        add(o,JLayeredPane.POPUP_LAYER);
    }
    private void changeView(int viewNum){
        if(selectedView!=-1){
            backgroundPanel.remove(views.get(selectedView));
        }
        views.get(viewNum).switchedTo();
        backgroundPanel.add(views.get(viewNum),constraints);
        try {
            backgroundPanel.setMaximum(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ContentViewManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        selectedView=viewNum;
    }
}
