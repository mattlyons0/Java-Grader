/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import DropboxGrader.Gui;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;

/**
 *
 * @author 141lyonsm
 */
public class ContentViewManager extends JDesktopPane implements ComponentListener,InternalFrameListener{    
    private Gui gui;
    private JInternalFrame backgroundPanel;
    
    private ArrayList<ContentView> views;
    private ArrayList<ContentOverlay> overlays;
    private int selectedView;
    private long overlayID;
    private GridBagConstraints constraints;
    
    public ContentViewManager(Gui gui){
        super();
        this.gui=gui;
        gui.addComponentListener(this);
        
        views=new ArrayList();
        overlays=new ArrayList();
        selectedView=-1;
        overlayID=0;
        
        removeAll(); //removes taskbar added on linux (gtk) l&f
    }
    /**
     * Lengthy init operations required for view manager.
     * Takes around 100ms on a good computer.
     * Should only be called once and must be called before using manager.
     * 
     * Used to set the contentPane of the JFrame and make it popup without displaying anything
     * until postInit is called.
     */
    public void postInit(){
        backgroundPanel=new JInternalFrame("",false,false,false,false);
        componentResized(null);
        backgroundPanel.setLayout(new GridBagLayout());
        backgroundPanel.setBorder(null);
        BasicInternalFrameUI ui=(BasicInternalFrameUI) backgroundPanel.getUI();
        ui.setNorthPane(null);
        backgroundPanel.setVisible(true);
        
        add(backgroundPanel,JLayeredPane.DEFAULT_LAYER);
        constraints=new GridBagConstraints();
        constraints.weightx=1;
        constraints.weighty=1;
        constraints.fill=GridBagConstraints.BOTH;
    }
    public void isClosing(){
        for(JInternalFrame frame:getAllFramesInLayer(JLayeredPane.POPUP_LAYER)){
            if(frame instanceof ContentOverlay){
                ContentOverlay overlay=(ContentOverlay) frame;
                overlay.isClosing();
                overlay.dispose();
            }
        }
    }
    public void addView(ContentView v){
        v.setup();
        views.add(v);
    }
    public boolean hasView(String viewName){
        for(int i=0;i<views.size();i++){
            if(views.get(i).getViewName().equals(viewName))
                return true;
        }
        return false;
    }
    /**
     * Changes current background view to another which has already been added.
     * Thread Safe
     * @param viewName name of view to be changed to
     */
    public void changeView(final String viewName){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<views.size();i++){
                    if(views.get(i).hasName(viewName)){
                        changeView(i);
                        revalidate();
                        repaint();
                        return;
                    }
                }
            }
        });
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
        if(selectedView==-1){
            return false;
        }
        if(views.get(selectedView).getViewName().equals(otherViewName)){
            return true;
        }
        return false;
    }
    public void addOverlay(final ContentOverlay o){
        if(o.shouldBeCached()){
            overlays.add(o);
        }
        o.addInternalFrameListener(ContentViewManager.this);
        o.setID(overlayID);
        overlayID++;
        o.setup();
        setLocation(o);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                add(o,JLayeredPane.POPUP_LAYER);
                o.switchedTo();
                try {
                    o.setMaximum(false);
                    o.setSelected(true);
                } catch (Exception ex) {
                    //oh no it cant be selected! whatever will we do!
                }
            }
        });
    }
    public boolean removeOverlay(String name){
        boolean removed=false;
        for(int i=0;i<overlays.size();i++){
            ContentOverlay o=overlays.get(i);
            if(o.getViewName().equals(name)){
                o.dispose();
                removed=true;
            }
        }
        return removed;
    }
    public void setLocation(ContentOverlay frame){
        int range=30;
        Point loc1=frame.getLocation();
        JInternalFrame[] frames=getAllFrames();
        for(JInternalFrame otherFrame:frames){
            if(otherFrame instanceof ContentOverlay && ((ContentOverlay)otherFrame).getID()!=frame.getID()){
                Point loc2=otherFrame.getLocation();
                int xDist=distWithinRange(loc1.x,range,loc2.x);
                int yDist=distWithinRange(loc1.y,range,loc2.y);
                boolean changed=false;
                if(xDist!=-1){
                    frame.setLocation(loc1.x+xDist, loc1.y);
                    loc1=frame.getLocation();
                    changed=true;
                }
                if(yDist!=-1){
                    frame.setLocation(loc1.x,loc1.y+yDist);
                    loc1=frame.getLocation();
                    changed=true;
                }
                if(changed){
                    setLocation(frame);
                    break;
                }
            }
        }
    }
    public static int distWithinRange(int number,int range,int desired){
        if(number==desired||number-range==desired||number+range==desired)
            return range;
        if(number>desired){
            if(number-range<desired)
                return range-(number-range);
        }
        else if(number<desired){
            if(number+range>desired)
                return (number+range)-desired;
        }
        return -1;            
    }
    private void changeView(int viewNum){
        if(selectedView!=-1){
            backgroundPanel.remove(views.get(selectedView));
        }
        views.get(viewNum).switchedTo();
        backgroundPanel.add(views.get(viewNum),constraints);
        componentResized(null);
        selectedView=viewNum;
    }
    @Override
    public void componentResized(ComponentEvent e) {
        if(backgroundPanel!=null)
            backgroundPanel.setSize(gui.getContentPane().getSize());
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {}

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {}

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        ContentOverlay overlay=(ContentOverlay)e.getInternalFrame();
        if(overlay.shouldBeCached()){
            for(int i=0;i<overlays.size();i++){
                ContentOverlay o=overlays.get(i);
                if(o.getViewName().equals(overlay.getViewName())){
                    overlays.remove(o);
                    o.removeInternalFrameListener(this); //I think this should get removed when invalidating or removing frame, 
                    //but in case it doesnt I don't want this reference stopping it from GC.
                    break;
                }
            }
        }
        remove(e.getInternalFrame());
        e.getInternalFrame().invalidate();
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {}

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {}

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {}

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {}
}
