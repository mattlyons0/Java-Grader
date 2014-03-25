/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements.Grader;

import DropboxGrader.DbxFile;
import DropboxGrader.RunCompileJava.JavaFile;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import jsyntaxpane.DefaultSyntaxKit;

/**
 *
 * @author 141lyonsm
 */
public class JavaCodeBrowser extends JPanel{
    private JScrollPane [] scrolls;
    private JEditorPane[] browserArea;
    private JPanel[] fileWindows;
    private JTabbedPane tabPane;
    private DbxFile file;
    private int numTextFiles;
    private int currentlyRunning;
    private Color defaultBackground;
    private int sortMode;
    private int sortOrder;
    private ArrayList<JavaFile> files;
    private CardLayout layout;
    public static final String[] sortModes = {"Default","Most Important","Most Code","Alphabetically"};
    
    
    public JavaCodeBrowser(DbxFile f){
        file=f;
        DefaultSyntaxKit.initKit();
        sort();
        init();
    }
    public void init(){
        currentlyRunning=-1;
        if(files==null){
            return;
        }
        layout=new CardLayout(10,5);
        setLayout(layout);
        if(tabPane==null)
            tabPane=new JTabbedPane();
        else
            tabPane.removeAll();
        File[] files=this.files.toArray(new File[0]);
        int numFiles=1;
        boolean noJavaFiles=true;
        if(files!=null){
            numFiles=file.getJavaFiles().length;
            noJavaFiles=false;
        }
        if(numFiles==0||file.isInvalidZip()){
            noJavaFiles=true;
        }
        File[] text=file.getTextFiles();
        if(text==null)
            text=new File[0];
        numTextFiles=text.length;
        File[] temp=files;
        if(temp==null)
            temp=new File[0];
        files=new File[text.length+temp.length];
        for(int x=0;x<files.length;x++){
            if(x<text.length){
                files[x]=text[x];
            }
            else{
                files[x]=temp[x-text.length];
            }
        }
        numFiles=files.length;
        if(noJavaFiles)
            numFiles++;
        browserArea=new JEditorPane[numFiles];
        fileWindows=new JPanel[numFiles];
        scrolls=new JScrollPane [numFiles];
        GridBagConstraints constraints=new GridBagConstraints();
        constraints.fill=GridBagConstraints.BOTH;
        constraints.weightx=1;
        constraints.weighty=1;
        for(int x=0;x<numFiles;x++){
            fileWindows[x]=new JPanel();
            fileWindows[x].setLayout(new GridBagLayout());
            
            browserArea[x]=new JEditorPane();
            browserArea[x].setEditable(true);
            scrolls[x]=new JScrollPane (browserArea[x]);
            if(noJavaFiles&&numFiles-x==1){ //theres no files
                String t="No .java files found in the zip file.\n";
                t+="File Structure:\n"+file.getFileStructure();
                if(file.isInvalidZip()){
                    t="Zip file is invalid.";
                }
                browserArea[x].setText(t);
            }
            else{
                if(files[x] instanceof JavaFile){
                    JavaFile jf=(JavaFile)files[x];
                    browserArea[x].setContentType("text/java");
                    browserArea[x].setText(jf.getCode());
                }
                else if(files[x] instanceof TextFile){
                    TextFile tf=(TextFile)files[x];
                    browserArea[x].setText(tf.getText());
                }
            }
            browserArea[x].setCaretPosition(0);
            fileWindows[x].add(scrolls[x],constraints);
            String tabName;
            if(!noJavaFiles||numFiles-x!=1){
                tabName=files[x].getName();
            }
            else{
                tabName="Zipped File Structure";
            }
            add(fileWindows[x],tabName);
            tabPane.addTab(tabName, fileWindows[x]);
            if(!noJavaFiles&&files[x] instanceof JavaFile)
                tabPane.setToolTipTextAt(x,getTooltip((JavaFile)files[x]));
        }
        add(tabPane,BorderLayout.CENTER);
        if(numFiles!=0&&files.length!=0){
            defaultBackground=tabPane.getBackgroundAt(0);
            layout.show(this, this.files.get(0).getName());
        }
    }
    private void sort(){ //no more than 100 classes are going to be sorted at once, so I probably don't have to implement a fancy sorting algorithm
        if(file==null){
            return;
        }
        if(files==null){
            files=new ArrayList(Arrays.asList(file.getJavaFiles()));
        }
        else{
            files.clear();
            files.addAll(Arrays.asList(file.getJavaFiles()));
        }
        if(sortMode==0){ //default order
            //we already did it in the else statement above
        }
        else if(sortMode==1){ //most important (most dependencies)
            ArrayList<JavaFile> files=new ArrayList();
            for(JavaFile f:this.files){ //we don't want to mutate files as this method might get threaded in the future
                files.add(f);
            }
            ArrayList<JavaFile> sorted=new ArrayList();
            int size=files.size();
            JavaFile largest=null;
            for(int i=0;i<size;i++){
                for(int x=0;x<files.size();x++){
                    if(largest==null||files.get(x).getDependencies().length>largest.getDependencies().length){
                        largest=files.get(x);
                    }
                }
                sorted.add(largest);
                files.remove(largest);
                largest=null;
            }
            this.files.clear();
            for(JavaFile f:sorted){
                this.files.add(f);
            }
        }
        else if(sortMode==2){ //Most Code (characters)
            ArrayList<JavaFile> files=new ArrayList();
            for(JavaFile f:this.files){ //we don't want to mutate files as this method might get threaded in the future
                files.add(f);
            }
            ArrayList<JavaFile> sorted=new ArrayList();
            int size=files.size();
            JavaFile largest=null;
            for(int i=0;i<size;i++){
                for(int x=0;x<files.size();x++){
                    if(largest==null||files.get(x).getCode().length()>largest.getCode().length()){
                        largest=files.get(x);
                    }
                }
                sorted.add(largest);
                files.remove(largest);
                largest=null;
            }
            this.files.clear();
            for(JavaFile f:sorted){
                this.files.add(f);
            }
        }
        else if(sortMode==3){ //Alphabetically
            ArrayList<String> fileNames=new ArrayList();
            for(JavaFile f:files){
                fileNames.add(f.getName());
            }
            Collections.sort(fileNames, Collator.getInstance());
            
            ArrayList<JavaFile> filesCopy=new ArrayList();
            for(JavaFile f:files){
                filesCopy.add(f);
            }
            int size=filesCopy.size();
            files.clear();
            for(int i=0;i<size;i++){
                for(int x=0;x<fileNames.size();x++){
                    if(fileNames.get(i).equals(filesCopy.get(x).getName())){
                        files.add(filesCopy.get(x));
                        break;
                    }
                }
            }
        }
        
        if(sortOrder==1){ //sort descending
            Collections.reverse(files);
        }
        
    }
    private String getTooltip(JavaFile f){
        if(sortMode==0){
            return null;
        }
        else if(sortMode==1){
            return f.getDependencies().length+" Dependencies";
        }
        else if(sortMode==2){
            return f.getCode().length()+" Characters";
        }
        else if(sortMode==3){
            return null;
        }
        else{
            return "There is no tooltip defined for this sorting mode. \nSomeone should get on that.";
        }
    }
    public void setFile(DbxFile f){
        file=f;
        
        sort();
        init();
    }
    public String saveFile(){
        if(files==null){
            return "";
        }
        JavaFile[] files=this.files.toArray(new JavaFile[0]);
        String result="";
        for(int x=0;x<files.length;x++){
            String code=browserArea[x+numTextFiles].getText();
            result=files[x].changeCode(code)+"\n";
        }
        return result;
    }
    public void setRunningFile(JavaFile f){
        if(f!=null){
            JavaFile[] files=file.getJavaFiles();
            for(int i=numTextFiles;i<files.length+numTextFiles;i++){
                if(files[i-numTextFiles].equals(f)){
                    tabPane.setBackgroundAt(i, new Color(163,255,163));
                    tabPane.setTitleAt(i,"*"+tabPane.getTitleAt(i)+"*");
                    currentlyRunning=i;
                    break;
                }
            }
        }
        else if(currentlyRunning!=-1){ //nothing is running anymore
            tabPane.setBackgroundAt(currentlyRunning, defaultBackground);
            tabPane.setTitleAt(currentlyRunning, tabPane.getTitleAt(currentlyRunning).replaceAll("\\*", ""));
            currentlyRunning=-1;
        }
    }
    public void setSortMode(int mode){
        sortMode=mode;
        
        sort();
        init();
        revalidate();
        repaint();
    }
    public void setSortOrder(int order){
        sortOrder=order;
        
        sort();
        init();
        revalidate();
        repaint();
    }
}
