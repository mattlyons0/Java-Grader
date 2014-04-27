/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.GuiElements.Grader;

import DropboxGrader.Config;
import DropboxGrader.FileManagement.DbxFile;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookTable;
import DropboxGrader.GuiHelper;
import DropboxGrader.RunCompileJava.JavaFile;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.util.Configuration;

/**
 *
 * @author 141lyonsm
 */
public class JavaCodeBrowser extends JPanel implements MouseListener,ActionListener{
    private JScrollPane [] scrolls;
    private JEditorPane[] browserArea;
    private JPanel[] fileWindows;
    private JTabbedPane tabPane;
    private DbxFile file;
    private int numTextFiles;
    private JavaFile currentlyRunning;
    private int sortMode;
    private int sortOrder;
    private ArrayList<JavaFile> files;
    private CardLayout layout;
    public static final String[] sortModes = {"Default","Most Important","Most Code","Alphabetically"};
    
    
    public JavaCodeBrowser(DbxFile f){
        file=f;
        currentlyRunning=null;
        DefaultSyntaxKit.initKit();
        sort();
        init();
    }
    public void init(){
        if(files==null||file==null){
            return;
        }
        layout=new CardLayout(10,5);
        setLayout(layout);
        if(tabPane==null){
            tabPane=new JTabbedPane();
        }
        else{
            tabPane.removeAll();
            removeAll();
        }
        File[] files=this.files.toArray(new File[0]);
        int numFiles=1;
        boolean noJavaFiles=true;
        if(files!=null&&file.getJavaFiles()!=null){
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
                String structure=file.getFileStructure();
                if(structure==null){ //signals to us that it forgot some files and researched
                    sort();
                    init();
                    return;
                }
                t+="File Structure:\n"+structure;
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
                    browserArea[x].addMouseListener(this);
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
            layout.show(this, this.files.get(0).getName()); //select first tab
        }
    }
    private void sort(){ //no more than ~100 classes are going to be sorted at once, so I probably don't have to implement a fancy sorting algorithm
        if(file==null){
            return;
        }
        if(files==null)
            files=new ArrayList();
        if(files==null){
            if(file.getJavaFiles()!=null)
                files=new ArrayList(Arrays.asList(file.getJavaFiles()));
        }
        else{
            files.clear();
            if(file.getJavaFiles()!=null)
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
    }
    public String saveFile(){
        if(files==null){
            return "";
        }
        JavaFile[] files=this.files.toArray(new JavaFile[0]);
        String result="";
        for(int x=0;x<files.length;x++){
            String code=browserArea[x+numTextFiles].getText();
            result+=files[x].changeCode(code);
        }
        TextFile[] textFiles=file.getTextFiles();
        for(int i=0;i<textFiles.length;i++){
            textFiles[i].save(browserArea[i].getText());
        }
        return result;
    }
    public void setRunningFile(JavaFile f){
        String colorString1="<html><font color='00AB00'>";
        String colorString2="</font></html>";
        if(f!=null){
            for(int i=numTextFiles;i<files.size()+numTextFiles;i++){
                if(files.get(i-numTextFiles).equals(f)){
                    tabPane.setTitleAt(i,colorString1+tabPane.getTitleAt(i)+colorString2);
                    currentlyRunning=f;
                    break;
                }
            }
        }
        else if(currentlyRunning!=null){ //nothing is running anymore
            for(int i=numTextFiles;i<files.size()+numTextFiles;i++){
                if(files.get(i-numTextFiles).equals(currentlyRunning)){
                    tabPane.setTitleAt(i, tabPane.getTitleAt(i).replace(colorString1,"").replace(colorString2,""));
                    currentlyRunning=null;
                }
            }
        }
    }
    public void setSort(int mode,int order){
        sortMode=mode;
        sortOrder=order;
        Config.codeSortMode=sortMode;
        Config.codeSortOrder=sortOrder;
        
        sort();
        init();
        setRunningFile(currentlyRunning);
        revalidate();
        repaint();
    }
    public void setSortMode(int mode){
        if(sortMode==mode)
            return;
        sortMode=mode;
        Config.codeSortMode=sortMode;
        
        sort();
        init();
        setRunningFile(currentlyRunning);
        revalidate();
        repaint();
    }
    public void setSortOrder(int order){
        if(sortOrder==order)
            return;
        sortOrder=order;
        Config.codeSortOrder=sortOrder;
        
        sort();
        init();
        revalidate();
        repaint();
    }

    private JPopupMenu createRightClickMenu(int panelNum){
        JPopupMenu m=new JPopupMenu();
        JMenuItem m1=new JMenuItem("Cut");
        JMenuItem m2=new JMenuItem("Copy");
        JMenuItem m3=new JMenuItem("Paste");
        m1.setActionCommand("Cut "+panelNum);
        m2.setActionCommand("Copy "+panelNum);
        m3.setActionCommand("Paste "+panelNum);
        m1.addActionListener(this);
        m2.addActionListener(this);
        m3.addActionListener(this);
        m.add(m1);
        m.add(m2);
        m.add(m3);
        return m;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON3){
            int panelNum=-1;
            for(int i=0;i<browserArea.length;i++){
                if(browserArea[i]==e.getSource()){ //checking for instance equality (pointers same)
                    panelNum=i;
                    break;
                }
            }
            if(panelNum!=-1){
                JPopupMenu popup = createRightClickMenu(panelNum);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
        JEditorPane editor;
        if(e.getActionCommand().startsWith("Cut ")){
            editor=browserArea[GradebookTable.extractNumber("Cut ", e.getActionCommand())];
            try{
                int start=editor.getSelectionStart();
                int end=editor.getSelectionEnd();
                clip.setContents(new StringSelection(editor.getSelectedText()+editor.getText().substring(end,end)),null);
                String cut=editor.getText().substring(0,start)+editor.getText().substring(end+1);
                editor.setText(cut);
                editor.setCaretPosition(start);
            } catch(IllegalStateException ex){
                GuiHelper.alertDialog("Error Accessing Clipboard.");
                System.err.println("Error Cutting into TextTab.");
                ex.printStackTrace();
            }
        }
        else if(e.getActionCommand().startsWith("Copy ")){
            editor=browserArea[GradebookTable.extractNumber("Copy ", e.getActionCommand())];
            try{
                int end=editor.getSelectionEnd();
                clip.setContents(new StringSelection(editor.getSelectedText()+editor.getText().substring(end,end)),null);
            } catch(IllegalStateException ex){
                GuiHelper.alertDialog("Error Accessing Clipboard.");
                System.err.println("Error Copying into TextTab.");
                ex.printStackTrace();
            }
        }
        else if(e.getActionCommand().startsWith("Paste ")){
            editor=browserArea[GradebookTable.extractNumber("Paste ", e.getActionCommand())];
            try{
                Object clipData=clip.getData(DataFlavor.stringFlavor);
                if(clipData instanceof String){
                    int cursorLoc=editor.getCaretPosition();
                    String combined=editor.getText().substring(0,cursorLoc)+
                            (String)clipData+editor.getText().substring(cursorLoc,editor.getText().length());
                    editor.setText(combined);
                    editor.setCaretPosition(cursorLoc);
                }
            } catch(Exception ex){ //its a risky operation to assume the data ont he clipboard is a string, so we are ok if that fails
                System.err.println("Error Pasting into TextTab.");
                ex.printStackTrace();
            }
        }
    }
}
