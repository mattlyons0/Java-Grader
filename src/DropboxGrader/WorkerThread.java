/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt
 */
public class WorkerThread implements Runnable{
    private Gui gui;
    private FileManager manager;
    private ArrayList<DbxFile> fileQueue;
    private ArrayList<DbxFile> deleteQueue;
    private boolean graderAfter;
    private DbxFile fileToRun;
    private int timesToRun;
    public WorkerThread(FileManager man,Gui gui){
        manager=man;
        this.gui=gui;
        fileQueue=new ArrayList();
        deleteQueue=new ArrayList();
    }
    @Override
    public void run() {
        while(true){
            int size=fileQueue.size();
            for(int x=0;x<size;x++){
                DbxFile f=fileQueue.remove(0);
                gui.setStatus("Downloading "+f.getFileName());
                f.download();
                int progress=(int)((double)(x+1)/size*100);
                gui.updateProgress(progress);
                if(size==1){
                    gui.updateProgress(50);
                }
                gui.repaintTable();
            }
            for(DbxFile f:deleteQueue){
                f.delete();
            }
            if(!deleteQueue.isEmpty()){
                deleteQueue.clear();
                gui.refreshTable();
            }
            if(graderAfter){
                gui.setupGraderGui();
                graderAfter=false;
            }
            if(fileToRun!=null){
                fileToRun.run(gui.getRunner(), timesToRun);
                fileToRun=null;
                timesToRun=0;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void downloadAll(){
        fileQueue.addAll(manager.getFiles());
    }
    public void download(int index,boolean graderAfter){
        fileQueue.add(manager.getFile(index));
        setGradeAfter(graderAfter);
    }
    public void download(ArrayList<Integer> fileIndexes,boolean gradeAfter){
        for(Integer i:fileIndexes){
            fileQueue.add(manager.getFile(i));
        }
        setGradeAfter(gradeAfter);
    }
    private void setGradeAfter(boolean g){
        if(g){
            graderAfter=true;
        }
    }
    public void runFile(int file,int times){
        fileToRun=manager.getFile(file);
        timesToRun=times;
    }
    public void delete(ArrayList<Integer> files){
        for(int x=0;x<files.size();x++){
            deleteQueue.add(manager.getFile(files.get(x)));
        }
    }
    
}
