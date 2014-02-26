/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author 141lyonsm
 */
public class ContentViewManager extends JPanel{
    private ArrayList<ContentView> views;
    private int selectedView;
    private GridBagConstraints constraints;
    
    public ContentViewManager(){
        super();
        
        views=new ArrayList();
        selectedView=-1;
        setLayout(new GridBagLayout());
        constraints=new GridBagConstraints();
        constraints.weightx=1;
        constraints.weighty=1;
        constraints.fill=GridBagConstraints.BOTH;
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
    private void changeView(int viewNum){
        if(selectedView!=-1){
            remove(views.get(selectedView));
        }
        views.get(viewNum).switchedTo();
        add(views.get(viewNum),constraints);
        selectedView=viewNum;
    }
}
