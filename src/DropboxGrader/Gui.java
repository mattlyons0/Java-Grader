/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import DropboxGrader.FileManagement.FileManager;
import DropboxGrader.FileManagement.DbxFile;
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
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.UnitTesting.UnitTestManager;
import com.dropbox.core.DbxClient;
import java.awt.Color;
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
import javax.swing.SwingUtilities;

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
    private UnitTestManager unitMan;
    
    //View Manager
    private ContentViewManager viewManager;
    
    private AuthView authView;
    private BrowserView browserView;
    private GraderView graderView;
    private GradebookView gradebookView;
    private ConfigView configView;
    
    public Gui(){
        super("Dropbox Grader");
        setBackground(Color.lightGray);
        viewManager=new ContentViewManager(this);
        selectedFiles=new ArrayList();        
        listener=new GuiListener(this);
        addWindowListener(listener);
        addComponentListener(listener);
        addWindowStateListener(listener);
        workerThread=new WorkerThread(this);
        workerThread.invokeLater(new Runnable() {
            @Override
            public void run() {
               initViewMan(); 
            }
        });
        init();
    }
    private void initViewMan(){
        viewManager.postInit();
        authView=new AuthView(this);
        viewManager.addView(authView);
        viewManager.changeView("AuthView");
        configView=new ConfigView(this);
        viewManager.addView(configView);
    }
    private void init(){
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setContentPane(viewManager);
        Point location=new Point(Config.screenCoordX,Config.screenCoordY);
        if(!isOnScreen(location)){
            setLocation(defaultLocation());
            Config.screenCoordX=0;
            Config.screenCoordY=0;
        }
        else{
            setLocation(Config.screenCoordX,Config.screenCoordY);
        }
        boolean[] exceedsSize=isLargerThanScreen(new Point(Config.screenWidth,Config.screenHeight));
        if((exceedsSize[0]||exceedsSize[1])){
            Rectangle size=maxBounds();
            int sizeX=size.width;
            int sizeY=size.height;
            if(exceedsSize[0]&&getExtendedState()!=JFrame.MAXIMIZED_BOTH)
                sizeY=Config.screenHeight;
            if(exceedsSize[1])
                sizeX=Config.screenWidth;
            System.out.println(sizeX+","+sizeY);
            
            setSize(sizeX,sizeY);
        }
        else{
            setSize(Config.screenWidth,Config.screenHeight);
        }
        if(supportsState(Config.screenState))
            setExtendedState(Config.screenState);
        else
            setExtendedState(JFrame.NORMAL);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }
    private void createSession(){
        fileManager=new FileManager(Config.dropboxFolder,Config.dropboxPeriod,client,this);
        workerThread.setFileManager(fileManager);
        grader=new TextGrader(fileManager,client,Gui.this);
        fileManager.setGrader(grader);
        workerThread.invokeLater(new Runnable() {
            @Override
            public void run() {
                gradebookView=new GradebookView(Gui.this);
                viewManager.addView(gradebookView);

                
                unitMan=new UnitTestManager(Gui.this);
                unitMan.test();
            }
        });
        browserView=new BrowserView(Gui.this,fileManager);
        viewManager.addView(browserView);
        setupFileBrowserGui();
        
        graderView=new GraderView(Gui.this,fileManager);
        viewManager.addView(graderView);
        
        fileManager.postInit();
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
        if(viewManager.hasView("GradebookView")){
            viewManager.changeView("GradebookView");
        }
        else{ //the gradebook has not been initialized yet
            //since the workerthread isnt a thread pool (and I recently refactored startup) we know that it is getting started from the workerthread
            //and the worker thread will not get to this task until it has completed initilizing the grade system (and a few milliseconds on 
            //starting the unit test thread)
            browserView.getGradebookButton().setEnabled(false);
            browserView.getGradebookButton().setToolTipText("Gradebook will open once the grading system has initialized.");

            workerThread.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setupGradebookGui();
                    browserView.getGradebookButton().setEnabled(true);
                    browserView.getGradebookButton().setToolTipText("");
                }
            });
        }
    }
    public void goodKey(DbxClient client){
        this.client=client;
        createSession();
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
            JSplitPane bottomDivider=graderView.getBottomDivider();
            if(bottomDivider!=null)
                Config.bottomDividerLocation=bottomDivider.getDividerLocation();
        }
        if(viewManager!=null){
            if(viewManager.selectedViewNameEquals("ConfigView"))
                configView.saveData();
            viewManager.isClosing();
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
        unitMan.test();
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
    private boolean isOnScreen(Point windowLoc){
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
        
        for(GraphicsDevice g:devices){
            Rectangle screenBounds=g.getDefaultConfiguration().getBounds();
            if(screenBounds.contains(windowLoc)){
                return true;
            }
        }
        return false;
    }
    private boolean supportsState(int state){
        return Toolkit.getDefaultToolkit().isFrameStateSupported(state);
    }
    private Point defaultLocation(){
        Rectangle bounds=defaultSize();
        return new Point(bounds.x,bounds.y);
    }
    private Rectangle defaultSize(){
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    }
    private Rectangle maxBounds(){
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }
    private boolean[] isLargerThanScreen(Point windowSize){
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle max=env.getMaximumWindowBounds();
        boolean x=false;
        boolean y=false;
        if(windowSize.x>max.width)
            x=true;
        if(windowSize.y>max.height)
            y=true;
        return new boolean[]{x,y};
    }
    public UnitTestManager getTestManager(){
        return unitMan;
    }
}
