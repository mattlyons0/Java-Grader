/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements;

import java.awt.GridBagConstraints;
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
        
        constraints=new GridBagConstraints();
        constraints.fill=GridBagConstraints.BOTH;
        constraints.anchor=GridBagConstraints.CENTER;
        constraints.weightx=1;
        constraints.weighty=1;
    }
    public void addView(ContentView v){
        views.add(v);
    }
    public void changeView(String viewName){
        for(int i=0;i<views.size();i++){
            if(views.get(i).hasName(viewName)){
                changeView(i);
            }
        }
    }
    private void changeView(int viewNum){
        if(selectedView!=-1){
            remove(views.get(selectedView));
        }
        add(views.get(viewNum),constraints);
        selectedView=viewNum;
    }
}
