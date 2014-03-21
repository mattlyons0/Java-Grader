/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import DropboxGrader.GuiElements.Grader.JTerminal;
import DropboxGrader.GuiElements.Grader.JavaCodeBrowser;
import DropboxGrader.RunCompileJava.JavaRunner;
import DropboxGrader.GuiElements.FileBrowser.FileBrowserData;
import DropboxGrader.GuiElements.FileBrowser.FileBrowserTable;
import DropboxGrader.GuiElements.MiscViews.AuthView;
import DropboxGrader.GuiElements.FileBrowser.BrowserView;
import DropboxGrader.GuiElements.ContentViewManager;
import DropboxGrader.GuiElements.Grader.GraderView;
import DropboxGrader.GuiElements.MiscViews.ConfigView;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookView;
import DropboxGrader.GuiElements.MiscOverlays.NameOverlay;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookTable;
import DropboxGrader.Printing.Print;
import DropboxGrader.TextGrader.TextGrader;
import com.dropbox.core.DbxClient;
import com.sun.org.apache.xml.internal.serialize.Printer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableModel;

/**
 *
 * @author Matt
 */
public class Gui extends JFrame implements ActionListener{
    //Raw Data Instance Vars
    private FileManager fileManager;
    private DbxSession dbxSession;
    private DbxClient client;
    private TextGrader grader;
    private WorkerThread workerThread;
    private GuiListener listener;
    private ArrayList<Integer> selectedFiles;
    private DbxFile currentFile;
    private Print printer;
    
    //View Manager
    private ContentViewManager viewManager;
    
    private AuthView authView;
    private BrowserView browserView;
    private GraderView graderView;
    private GradebookView gradebookView;
    private ConfigView configView;
    
    public Gui(){
        super("Dropbox Grader");
        //UIManager.put("ProgressBar.foreground", new Color(120,200,55)); //color the progressbar green.
        viewManager=new ContentViewManager();
        selectedFiles=new ArrayList();        
        listener=new GuiListener(this);
        addWindowListener(listener);
        getContentPane().addComponentListener(listener);
        addWindowStateListener(listener);
        
        init();
        
        workerThread=new WorkerThread(this);
        Thread thread=new Thread(workerThread);
        thread.setName("WorkerThread");
        thread.start();
        
        initViewMan();
    }
    private void initViewMan(){
        authView=new AuthView(this);
        viewManager.addView(authView);
        viewManager.changeView("AuthView");
        configView=new ConfigView(this);
        viewManager.addView(configView);
    }
    private void init(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(screenSize.width*0.95),(int)(screenSize.height*0.9));
        setLocation(screenSize.width/2-this.getSize().width/2, screenSize.height/2-this.getSize().height/2);
        
        setContentPane(viewManager);
        
        setVisible(true);
        printer=new Print(this);
    }
    private void createSession(){
        fileManager=new FileManager(Config.dropboxFolder,Config.dropboxPeriod,client,this);
        workerThread.setFileManager(fileManager);
        grader=new TextGrader(fileManager,client);
        fileManager.setGrader(grader);
        
        browserView=new BrowserView(this,fileManager);
        viewManager.addView(browserView);
        graderView=new GraderView(this,fileManager);
        viewManager.addView(graderView);
        gradebookView=new GradebookView(this);
        viewManager.addView(gradebookView);
    }
    
    public void setupFileBrowserGui(){
        viewManager.changeView("BrowserView");
    }
    public void setupGraderGui(){
        viewManager.changeView("GraderView");
    }
    public void setupConfigGui(){
        viewManager.changeView("ConfigView");
    }
    public void setupGradebookGui(){
        viewManager.changeView("GradebookView");
    }
    public void goodKey(DbxClient client){
        this.client=client;
        createSession();

        setupFileBrowserGui();
    }
    public TextGrader getGrader(){
        return grader;
    }
    public JavaRunner getRunner(){
        if(graderView!=null)
            return graderView.getRunner();
        return null;
    }
    public void updateProgress(int val){
        browserView.updateProgress(val);
    }
    public void repaintTable(){
        FileBrowserTable fileBrowserTable=browserView.getTable();
        FileBrowserData fileBrowserData=browserView.getTableData();
        if(fileBrowserTable!=null){
            
            fileBrowserData.refresh();
            fileBrowserTable.revalidate();
            fileBrowserTable.repaint();
        }
    }
    public void setStatus(String status){
        browserView.setStatus(status);
    }
    public void gradeRows(){
        browserView.gradeRows();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO: use switch instead of if, because it uses jump tables.        
    }
    public void proccessEnded(){
        graderView.proccessEnded();
    }
    public JTerminal getTerminal(){
        return graderView!=null?graderView.getTerminal():null;
    }
    public FileManager getManager(){
        return fileManager;
    }
    public void isClosing(){
        if(graderView!=null){
            JSplitPane graderDivider=graderView.getDivider();
            if(graderDivider!=null)
                Config.dividerLocation=graderDivider.getDividerLocation();
        }
        if(viewManager!=null){
            if(viewManager.selectedViewNameEquals("ConfigView")){
                configView.saveData();
            }
        }
    }
    public WorkerThread getBackgroundThread(){
        return workerThread;
    }
    public DbxFile getCurrentFile(){
        return currentFile;
    }
    public void setCurrentFile(DbxFile file){
        currentFile=file;
    }
    public void setDbxSession(DbxSession s){
        dbxSession=s;
    }
    public DbxSession getDbxSession(){
        return dbxSession;
    }
    public ArrayList<Integer> getSelectedFiles(){
        return selectedFiles;
    }
    public GuiListener getGuiListener(){
        return listener;
    }
    public void refreshTable(){
        if(browserView!=null){
            browserView.refreshTable();
        }  
    }
    public void refreshFinished(){
        if(browserView!=null)
            browserView.refreshFinished();
    }
    public void fileBrowserDataChanged(){
        if(browserView!=null){
            browserView.dataChanged();
        }
    }
    public ContentViewManager getViewManager(){
        return viewManager;
    }
    public void gradebookDataChanged(){
        if(gradebookView!=null){
            gradebookView.dataChanged();
        }
    }
    public JavaCodeBrowser getCodeBrowser(){
        if(graderView!=null)
            return graderView.getCodeBrowser();
        return null;
    }
    public Print getPrinter(){
        return printer;
    }
}
