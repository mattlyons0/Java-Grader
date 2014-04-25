/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.UnitTesting.UnitTestOverlay;
import DropboxGrader.TextGrader.TextAssignment;
import java.util.ArrayList;

/**
 *
 * @author Matt
 */
public class UnitTestManager implements Runnable{
    private Gui gui;
    private Thread unitTestThread;
    private ArrayList<Runnable> tests;
    
    public UnitTestManager(Gui gui){
        this.gui=gui;
        
        tests=new ArrayList();
        createThread();
    }
    private void createThread(){
        unitTestThread=new Thread(this);
        unitTestThread.setName("UnitTestThread");
    }
    public void test(){
        tests.add(new Runnable() {
            @Override
            public void run() {
                UnitTestOverlay overlay=new UnitTestOverlay(gui);
                gui.getViewManager().addOverlay(overlay);
                TextAssignment[] assignments=gui.getGrader().getSpreadsheet().getAllAssignments();
                for(int i=0;i<assignments.length;i++){
                    if(assignments[i].simpleUnitTests!=null||assignments[i].junitTests!=null){
                        UnitTester tester=new UnitTester(gui,assignments[i],overlay);
                        tester.runTests();
                    }
                }
                overlay.finished();
            }
        });
        if(!unitTestThread.isAlive()){
            createThread();
            unitTestThread.start();
        }
    }

    @Override
    public void run() {
        while(!tests.isEmpty()){
            Runnable currentTest=tests.remove(0);
            currentTest.run();
        }
    }
    
}
