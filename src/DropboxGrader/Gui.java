/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import DropboxGrader.GuiElements.ContentViewManager;
import DropboxGrader.GuiElements.FileBrowser.BrowserView;
import DropboxGrader.GuiElements.FileBrowser.FileBrowserData;
import DropboxGrader.GuiElements.FileBrowser.FileBrowserTable;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookView;
import DropboxGrader.GuiElements.Grader.GraderView;
import DropboxGrader.GuiElements.Grader.JTerminal;
import DropboxGrader.GuiElements.Grader.JavaCodeBrowser;
import DropboxGrader.GuiElements.MiscViews.AuthView;
import DropboxGrader.GuiElements.MiscViews.ConfigView;
import DropboxGrader.RunCompileJava.JavaRunner;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.UnitTesting.UnitTester;
import com.dropbox.core.DbxClient;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

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
        setBackground(Color.lightGray);
        viewManager=new ContentViewManager(this);
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
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        if(!isOnScreen(new Point(Config.screenCoordX,Config.screenCoordY))){
            setLocation(0,0);
            Config.screenCoordX=0;
            Config.screenCoordY=0;
            setExtendedState(JFrame.NORMAL);
        }
        else{
            setLocation(Config.screenCoordX,Config.screenCoordY);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        if(!isLargerThanScreen(new Point(Config.screenWidth,Config.screenHeight))){
           Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
           setSize((int)(d.width*0.95),(int)(d.height*0.9));
           Config.screenWidth=(int)(d.width*0.95);
           Config.screenHeight=(int)(d.height*0.9);
           setExtendedState(JFrame.NORMAL);
        }
        else{
            setSize(Config.screenWidth,Config.screenHeight);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        setContentPane(viewManager);
        
        setVisible(true);
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
        
        //MOVE THIS TO ANOTHER THREAD
        TextAssignment[] assignments=grader.getSpreadsheet().getAllAssignments();
        for(int i=0;i<assignments.length;i++){
            if(assignments[i].unitTest!=null){
                UnitTester tester=new UnitTester(this,assignments[i]);
                tester.runTests();
            }
        }
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
    public GuiListener getListener(){
        return listener;
    }
    public boolean isOnScreen(Point windowLoc){
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();
        
        for(GraphicsDevice g:devices){
            Rectangle screenBounds=g.getDefaultConfiguration().getBounds();
            if(screenBounds.contains(windowLoc)){
                return true;
            }
        }
        return false;
    }
    public boolean isLargerThanScreen(Point windowSize){
        GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();
        
        for(GraphicsDevice g:devices){
            Rectangle screenBounds=g.getDefaultConfiguration().getBounds();
            if(windowSize.x>screenBounds.width||windowSize.y>screenBounds.height){
                return true;
            }
        }
        return false;
    }
}
