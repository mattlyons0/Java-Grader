/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import DropboxGrader.GuiElements.AuthView;
import DropboxGrader.GuiElements.BrowserView;
import DropboxGrader.GuiElements.ContentViewManager;
import DropboxGrader.GuiElements.GraderView;
import DropboxGrader.GuiElements.ConfigView;
import DropboxGrader.GuiElements.GradebookView;
import DropboxGrader.GuiElements.NameOverlay;
import DropboxGrader.GuiElements.SpreadsheetBrowser.SpreadsheetTable;
import DropboxGrader.TextGrader.TextGrader;
import com.dropbox.core.DbxClient;
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
        
        initViewMan();
        init();
    }
    private void initViewMan(){
        authView=new AuthView(this);
        viewManager.addView(authView);
        viewManager.changeView("AuthView");
        configView=new ConfigView(this);
        viewManager.addView(configView);
        viewManager.addOverlay(new NameOverlay());
    }
    private void init(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screenSize.width-100,screenSize.height-100);
        setLocation(screenSize.width/2-this.getSize().width/2, screenSize.height/2-this.getSize().height/2);
        
        setContentPane(viewManager);
        
        setVisible(true);
        
        dbxSession=new DbxSession(this);
    }
    private void createSession(){
        fileManager=new FileManager(Config.dropboxFolder,Config.dropboxPeriod,client,this);
        workerThread=new WorkerThread(fileManager,this);
        new Thread(workerThread).start();
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
    public void promptKey(){
        authView.promptKey();
    }
    public void badKey(){
        authView.badKey();
    }
    public void goodKey(String loginName,DbxClient client){
        authView.goodKey(loginName);
        this.client=client;
        createSession();

        setupFileBrowserGui();
    }
    public TextGrader getGrader(){
        return grader;
    }
    public JavaRunner getRunner(){
        return graderView.getRunner();
    }
    public void updateProgress(int val){
        browserView.updateProgress(val);
    }
    public void repaintTable(){
        FileBrowser fileBrowserTable=browserView.getTable();
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
        browserView.refreshTable();
    }
    public void refreshFinished(){
        if(browserView!=null)
            browserView.refreshFinished();
    }
}
