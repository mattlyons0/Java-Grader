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
    private ArrayList<Runnable> queue;
    private boolean closeAfterDone;
    
    public WorkerThread(Gui gui){
        this.gui=gui;
        queue=new ArrayList();
        closeAfterDone=false;
    }
    @Override
    public void run() {
        while(true){
            try{
                while(!queue.isEmpty()){
                    Runnable r=queue.remove(0);
                    try{
                        r.run();
                    } catch(Exception e){
                        System.err.println("Exception was logged running queued code on the backgroundThread.");
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(closeAfterDone){
                    gui.getListener().windowClosing(null);
                }
            } catch(Exception e){
                System.err.println("An exception occured on the background thread and it was caught from crashing.");
                e.printStackTrace();
            }
        }
    }
    public void downloadAll(){
        queue.add(new Runnable() {
            @Override
            public void run() {
                ArrayList<DbxFile> files=manager.getFiles();
                for(int i=0;i<files.size();i++){
                    DbxFile f=files.get(i);
                    if(f!=null){
                        gui.setStatus("Downloading "+f.getFileName());
                        int progress=(int)((double)(i+1)/files.size()*100);
                        gui.updateProgress(progress);
                        if(files.size()==1){
                            gui.updateProgress(50);
                        }
                        f.download();
                        gui.repaintTable();
                    }
                }
            }
        });
    }
    public void download(final int index,final boolean gradeAfter){
        final DbxFile f=manager.getFile(index);
        queue.add(new Runnable() {
            @Override
            public void run() {
                if(f!=null){
                    gui.updateProgress(50);
                    f.download();
                    gui.repaintTable();
                }
                if(gradeAfter){
                    gui.setupGraderGui();
                }
            }
        });
    }
    public void download(ArrayList<Integer> fileIndexes,final boolean gradeAfter){
        final ArrayList<DbxFile> files=new ArrayList();
        for(int i=0;i<fileIndexes.size();i++){
            files.add(manager.getFile(fileIndexes.get(i)));
        }
        queue.add(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<files.size();i++){
                    DbxFile f=files.get(i);
                    if(f!=null){
                        gui.setStatus("Downloading "+f.getFileName());
                        int progress=(int)((double)(i+1)/files.size()*100);
                        gui.updateProgress(progress);
                        if(files.size()==1){
                            gui.updateProgress(50);
                        }
                        f.download();
                        gui.repaintTable();
                    }
                }
                if(gradeAfter){
                    gui.setupGraderGui();
                }
            }
        });
    }
    public void runFile(int file,final int times){
        final DbxFile f=manager.getFile(file);
        queue.add(new Runnable() {
            @Override
            public void run() {
                boolean success=f.run(times,gui.getCodeBrowser());
                if(!success){
                    gui.proccessEnded();
                }
            }
        });
    }
    public void delete(ArrayList<Integer> fileIndexes){
        final ArrayList<DbxFile> files=new ArrayList();
        for(int i=0;i<fileIndexes.size();i++){
            files.add(manager.getFile(fileIndexes.get(i)));
        }
        queue.add(new Runnable() {
            @Override
            public void run() {
                for(DbxFile f:files){
                    if(f!=null)
                        f.delete();
                }
            }
        });
    }
    public void delete(Integer file){
        if(file==null){
            return;
        }
        final DbxFile f=manager.getFile(file);
        queue.add(new Runnable() {
            @Override
            public void run() {
                if(f!=null){
                    f.delete();
                }
            }
        });
    }
    public void invokeLater(Runnable run){
        queue.add(run);
    }
    public void refreshData(){
        queue.add(new Runnable() {
            @Override
            public void run() {
                if(manager!=null){
                    manager.refresh();
                    gui.refreshFinished();
                    if(gui.getGrader()!=null){
                        gui.getGrader().refresh();
                        gui.repaint();
                    }
                }
            }
        });
    }
    public void setFileManager(FileManager man){
        manager=man;
    }
    public boolean hasWork(){
        return !queue.isEmpty();
    }
    public void setCloseAfterDone(boolean close){
        closeAfterDone=close;
    }
}
