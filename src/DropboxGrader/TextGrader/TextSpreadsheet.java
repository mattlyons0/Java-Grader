/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.TextGrader;

import DropboxGrader.DbxSession;
import DropboxGrader.FileManagement.Date;
import DropboxGrader.Gui;
import DropboxGrader.GuiHelper;
import DropboxGrader.Util.NamedRunnable;
import DropboxGrader.Util.StaticMethods;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Matt
 */
public class TextSpreadsheet {
    public static final String GRADEDELIMITER="¶"; //Can't use most keyboard symbols, because I don't want someone to break everything by using it, So I had to get creative
    public static final String COMMENTDELIMITER="«"; //These symbols are banned from all forms of input dealing with the grades spreadsheet
    public static final String INDIVIDUALDELIMITER="÷"; //See validateString(String) for more info on banning them
    public static final String INDIVIDUALDELIMITER2="☃"; //lol snowman
    public static final String INDIVIDUALDELIMITER3="☑";
    public static final String INDIVIDUALDELIMITER4="★";
    
    private Gui gui;
    
    private ArrayList<TextAssignment> assignments;
    private ArrayList<TextName> names;
    private ArrayList<ArrayList<TextGrade>> grades; //in the same order as names, then the same order as assignments
    
    public TextSpreadsheet(Gui gui){
        this.gui=gui;
    }
    public void parse(File f){
        if(f==null){
            clearData();
            return;
        }
        String code=DbxSession.readFromFile(f);
        
        //Clear current data and initialize everything except grades
        clearData();
        
        //Start parsing
        String[] lines=code.split("\n");
        //Initialize Grades

        int row=0; //keeps track of where we are in terms of real lines
        for (String line : lines) {
            if(!line.contains(COMMENTDELIMITER)){ //if its not a comment and the line contains delimitors
                String[] gradesInLine=line.split(GRADEDELIMITER);
                for(int col=0;col<gradesInLine.length;col++){
                    if(row==0){ //We are on the line that declares assignments
                        if(!gradesInLine[col].equals(""))
                            assignments.add(new TextAssignment(gradesInLine[col])); //Add to list of assignments
                    }
                    else{
                        if(col==0){ //this is a name
                            names.add(new TextName(gradesInLine[col]));
                            grades.add(new ArrayList());
                        }
                        else{
                            ArrayList<TextGrade> grade=grades.get(names.size()-1);
                            if(grade==null){
                                grade=new ArrayList();
                        }
                            if(!gradesInLine[col].equals("null"))
                                grade.add(new TextGrade(gradesInLine[col]));
                            else
                                grade.add(null);
                        }
                    }
                }
                row++;
            }
        }
        //check if it is overdue
        for(TextAssignment assign:assignments)
            checkOverdue(assign);
    }
    public void writeToFile(File f){
        //Migrate comments from old sheet to the new sheet
        String[] oldFileLines=DbxSession.readFromFile(f).split("\n");
        String code="";
        for(String line:oldFileLines){
            if(line.contains("/`/")){ //its a comment, add it to the new sheet
                code+=line+"\n";
            }
        }
        //Write the assignments
        for(int i=0;i<assignments.size();i++){
            code+=assignments.get(i).toText();
            code+=GRADEDELIMITER;
        }
        code+="\n";
        //Write names then grades
        for(int row=0;row<names.size();row++){
            TextName name=names.get(row);
            if(grades.get(row).isEmpty())
                code+=name.toText()+GRADEDELIMITER;
            for(int i=0;i<grades.get(row).size();i++){
                if(i==0){ //name entry
                    code+=name.toText()+GRADEDELIMITER;
                }
                //grade entry
                boolean ignore=false;
                TextGrade g;
                if(grades.get(row).size()==grades.get(0).size())
                    g=grades.get(row).get(i);
                else{
                    g=null;
                    grades.remove(row);
                    row--;
                    ignore=true;
                }
                if(g==null&&!ignore){
                    code+="null";
                }
                else if(!ignore){
                    code+=g.toText();
                }
                if(i<grades.get(0).size()&&!ignore){
                        code+=GRADEDELIMITER;
                }
            }
            code+="\n";
        }
        
        DbxSession.writeToFile(f, code);
    }
    public boolean isInitialized(){
        return assignments!=null;
    }
    public void addAssignment(int assignmentNum,String assignmentName,Date date){
        assignmentName=validateString(assignmentName);
        
        TextAssignment assign=new TextAssignment(assignmentNum,assignmentName,date);
        assignments.add(assign);
        
        for(ArrayList<TextGrade> grade:grades){
            grade.add(null);
        }
    }
    public void addName(String firstName,String lastName,String email){ //TODO: store email and email when wrongly submitted file
        firstName=validateString(firstName);
        lastName=validateString(lastName);
        while(getName(firstName+lastName)!=null){
            if(Character.isDigit(lastName.charAt(lastName.length()-1))){
                int num=Integer.parseInt(lastName.charAt(lastName.length()-1)+"");
                num++;
                lastName=lastName.substring(0,lastName.length()-1)+num;
            }
            else{
                lastName+=1;
            }
        }
        names.add(new TextName(firstName,lastName,email));
        
        int numAssignments=assignments.size();
        grades.add(new ArrayList());
        int nameIndex=names.indexOf(names.get(names.size()-1));
        for(int i=0;i<numAssignments;i++){
            grades.get(nameIndex).add(null);
        }
    }
    public boolean setGrade(TextName name,TextAssignment assignment,double grade,String comment, boolean overwrite){
        comment=validateString(comment,true);
        int assignmentIndex=assignments.indexOf(assignment);
        int nameIndex=names.indexOf(name);
        if(assignmentIndex==-1||nameIndex==-1){
            GuiHelper.alertDialog("Error grading, try again.");
            System.err.println("Error grading "+assignment+" by "+name+"\nAssignmentIndex: "+assignmentIndex+" NameIndex: "+nameIndex);
            return false;
        }
        TextGrade currentGrade=grades.get(nameIndex).get(assignmentIndex);
        if(currentGrade!=null){
            if(currentGrade.grade==grade&&currentGrade.comment.equals(comment)){ //if they have the same data
                return true; //we can say we are done because there is nothing to change.
            }
            if(!overwrite){
                overwrite=GuiHelper.yesNoDialog("There is already a grade written: "+getGrade(name,assignment)+"\nWould you like to overwrite this grade?");
                if(!overwrite){
                    return false;
                }
            }
        }
        TextGrade newGrade=new TextGrade(grade,comment);
        grades.get(nameIndex).set(assignmentIndex,newGrade); //TODO: record date and timestamp with this.
        return true;
    }
    public boolean setGradeAt(int nameIndex,int assignmentIndex,double grade,String comment,boolean overwrite){
        comment=validateString(comment,true);
        if(getGradeAt(nameIndex,assignmentIndex)!=null&&!overwrite){
            overwrite=GuiHelper.yesNoDialog("There is already a grade written: "+getGradeAt(nameIndex,assignmentIndex)+"\nWould you like to overwrite this grade?");
            if(!overwrite){
                return false;
            }
        }
        grades.get(nameIndex).set(assignmentIndex,new TextGrade(grade,comment)); //TODO: record date and timestamp with this.
        return true;
    }
    public TextGrade getGrade(TextName name,TextAssignment assignment){
        int assignmentIndex=assignments.indexOf(assignment);
        int nameIndex=names.indexOf(name);
        if(assignmentIndex==-1||nameIndex==-1){
            return null;
        }
        return grades.get(nameIndex).get(assignmentIndex);
    }
    public TextGrade[] getAllGrades(int nameIndex){
        TextGrade[] g=new TextGrade[0];
        return grades.get(nameIndex).toArray(g);        
    }
    public TextAssignment[] getAllAssignments(){
        return assignments.toArray(new TextAssignment[0]);
    }
    public TextName getName(String name){
        
        String[] split=name.split("(?=\\p{Upper})"); //regex, splits by uppercase
        if(split.length==2){
            for(TextName tName:names){
                if(tName.firstName.toLowerCase().equals(split[0].toLowerCase())){
                    if(tName.lastName.toLowerCase().equals(split[1].toLowerCase()))
                        return tName;
                }
                else if(tName.firstName.toLowerCase().equals(split[1].toLowerCase())){
                    if(tName.lastName.toLowerCase().equals(split[0].toLowerCase()))
                        return tName;
                }
            }
        }
        for(TextName tName:names){
            if((tName.firstName+tName.lastName).toLowerCase().equals(name.toLowerCase()))
                return tName;
            else if((tName.lastName+tName.firstName).toLowerCase().equals(name.toLowerCase()))
                return tName;
        }
        
        return null;
    }
    public int indexOfName(String name){
        for(int i=0;i<names.size();i++){
            TextName tName=names.get(i);
            if(name.toLowerCase().contains(tName.firstName.toLowerCase())&&name.toLowerCase().contains(tName.lastName.toLowerCase())){
                return i;
            }
        }
        return -1;
    }
    public TextName[] indexesOfName(String name){
        String[] split={name};
        if(name.contains(" ")){
            split=name.split(" ");
        }
        HashSet<TextName> namesFound=new HashSet();
        for(TextName tName:names){
            if(split.length==1){
                if(tName.firstName.toLowerCase().contains(split[0].toLowerCase())||
                        tName.lastName.toLowerCase().contains(split[0].toLowerCase())||
                        (tName.firstName+tName.lastName).toLowerCase().contains(split[0])){
                    namesFound.add(tName);
                }
            }
            else if(split.length==2){
                if(tName.firstName.toLowerCase().contains(split[0].toLowerCase())){
                    if(tName.lastName.toLowerCase().contains(split[1].toLowerCase()))
                        namesFound.add(tName);
                }
                else if(tName.lastName.toLowerCase().contains(split[0].toLowerCase())){
                    if(tName.firstName.toLowerCase().contains(split[1].toLowerCase()))
                        namesFound.add(tName);
                }
            }
        }
        return namesFound.toArray(new TextName[namesFound.size()]);
    }
    public TextAssignment getAssignment(int assignmentNum){
        for(TextAssignment tAssignment:assignments){
            if(tAssignment.number==assignmentNum){
                return tAssignment;
            }
        }
        return null;
    }
    public boolean changeName(TextName name,String newNames[]){
        TextName desiredName=null;
        int desiredIndex=-1;
        for(int i=0;i<names.size();i++){
            if(names.get(i)!=name&&names.get(i).equals(new TextName(newNames[0],newNames[1],null))){ //if it isn't the same pointer, but it has the same first/last name
                desiredName=names.get(i);
                desiredIndex=i;
                break;
            }
        }
        int nameIndex=-1;
        for(int i=0;i<names.size();i++){
            if(names.get(i)!=desiredName&&names.get(i).equals(name)){ //if its the same pointer
                nameIndex=i;
                break;
            }
        }
        if(desiredName==null){
            name.firstName=newNames[0];
            name.lastName=newNames[1];
            return true;
        }
        else if(desiredName.equals(name)&&!isDuplicateName(desiredName.firstName,desiredName.lastName)){ //if someone is trying to rename something to itself
            //yea we don't have to do anything, user is too lazy to hit X
            return true;
        }
        else{ //merge names
            TextGrade[] desiredGrades=getAllGrades(desiredIndex);
            TextGrade[] currentGrades=getAllGrades(nameIndex);
            for(int i=0;i<desiredGrades.length;i++){
                if(desiredGrades[i]!=null&&currentGrades[i]==null){
                    //cool we already have this grade
                }
                else if(currentGrades[i]!=null&&desiredGrades[i]==null){
                    //copy that grade to the desired
                    setGradeAt(desiredIndex, i, currentGrades[i].grade, currentGrades[i].comment, false);
                }
                else if(desiredGrades[i]==null&&currentGrades[i]==null){
                    //awesome, nothing to merge
                }
                else{ //they both have data
                    GuiHelper.alertDialog("There are conflicting grades when trying to merge "
                            +name+" with "+newNames[0]+" "+newNames[1]+"."
                            + "\nDelete the conflicting grades then try again.");
                    return false;
                }
            }
            deleteNameAt(nameIndex);
            return true;
        }
    }
    public boolean deleteName(TextName name){
        int nameIndex=names.indexOf(name);
        if(nameIndex==-1){
            return false;
        }
        ArrayList<TextGrade> nameGrades=grades.get(nameIndex);
        for(int i=0;i<nameGrades.size();i++){ //must not have any existing grades, as a failsafe
            if(nameGrades.get(i)!=null){
                return false;
            }
        }
        names.remove(nameIndex);
        grades.remove(nameIndex);
        return true;
    }
    public boolean deleteNameAt(int nameIndex){
        //        ArrayList<TextGrade> nameGrades=grades.get(nameIndex);
//        for(int i=0;i<nameGrades.size();i++){ //must not have any existing grades, as a failsafe
//            if(nameGrades.get(i)!=null){
//                return false;
//            }
//        }
        names.remove(nameIndex);
        grades.remove(nameIndex);
        return true;
    }
    public boolean deleteAssignmentAt(int assignmentIndex){
        for(int name=0;name<names.size();name++){
            if(grades.get(name).get(assignmentIndex)!=null){
                return false;
            }
        }
        assignments.remove(assignmentIndex);
        for(int name=0;name<names.size();name++){
            grades.get(name).remove(assignmentIndex);
        }
        return true;
    }
    public boolean deleteGrade(TextName name,TextAssignment assign,TextGrade grade){
        int nameIndex=names.indexOf(name);
        if(nameIndex==-1){
            return false;
        }
        int assignmentIndex=assignments.indexOf(assign);
        if(assignmentIndex==-1){
            return false;
        }
        TextGrade assumedGrade=grades.get(nameIndex).get(assignmentIndex);
        if(!grade.equals(assumedGrade)){ //double check we are deleting the correct grade
            return false;
        }
        grades.get(nameIndex).set(assignmentIndex, null); //switch assignment to be null instead
        return true;
    }
    public void moveAssignment(int oldIndex,int newIndex){
        TextAssignment assign=assignments.get(oldIndex);
        for(ArrayList<TextGrade> name:grades){
            if(!name.isEmpty()){
                TextGrade grade=name.get(oldIndex);
                name.remove(oldIndex);
                name.add(newIndex,grade);
            }
        }
        assignments.remove(oldIndex);
        assignments.add(newIndex, assign);
    }
    public void moveName(int oldIndex,int newIndex){
        if(newIndex>=names.size()){
            newIndex--;
        }
        TextName name=names.get(oldIndex);
        ArrayList<TextGrade> grade=grades.get(oldIndex);
        grades.remove(oldIndex);
        grades.add(newIndex,grade);
        names.remove(oldIndex);
        names.add(newIndex,name);
    }
    public boolean isDuplicateName(String firstName,String lastName){
        int repeatCount=0;
        for(int i=0;i<names.size();i++){
            TextName name=names.get(i);
            if(name.firstName.equals(firstName)&&name.lastName.equals(lastName)){
                repeatCount++;
            }
        }
        return repeatCount>1; //true if repeat count is larger than one
    }
    public boolean nameDefined(String name){
        return getName(name)!=null;
    }
    public boolean assignmentDefined(int assignmentNum){
        return getAssignment(assignmentNum)!=null;
    }
    public int numNames(){
        return names.size();
    }
    public int numAssignments(){
        return assignments.size();
    }
    public TextGrade getGradeAt(int assignmentIndex,int nameIndex){
        if(nameIndex<0||assignmentIndex<0||nameIndex>=names.size()||assignmentIndex>=assignments.size()){
            System.err.println("Attempted to get grade out of bounds. AssignmentIndex:"+assignmentIndex+
                    " Size:"+assignments.size()+" NameIndex:"+nameIndex+" Size:"+names.size());
            StaticMethods.printStackTrace();
            return null;
        }
        return grades.get(nameIndex).get(assignmentIndex);
    }
    public TextAssignment getAssignmentAt(int assignmentIndex){
        if(assignmentIndex<0||assignments.size()<=assignmentIndex){
            System.err.println("Attempted to get assignment out of bounds. AssignmentIndex:"+assignmentIndex+
                    " Size:"+assignments.size());
            StaticMethods.printStackTrace();
            return null;
        }
        return assignments.get(assignmentIndex);
    }
    public TextName getNameAt(int nameIndex){
        if(nameIndex<0||names.size()<=nameIndex){
            System.err.println("Attempted to get name out of bounds. NameIndex:"+nameIndex+" Size:"+names.size());
            StaticMethods.printStackTrace();
            return null;
        }
        return names.get(nameIndex);
    }
    public void checkOverdue(final TextAssignment assign){
        gui.getBackgroundThread().invokeLater(new Runnable() {
            @Override
            public void run() {
                gui.getGrader().downloadSheet();
                boolean changed=false;
                
                int assignIndex=assignments.indexOf(assign);
                TextAssignment assign=assignments.get(assignIndex);
                if(assign.dateDue!=null&&(!Date.before(Date.currentDate(), assign.dateDue))){ //if the duedate has passed
                    if(!assign.overdue){ //we havent sent late emails out yet
                        assign.overdue=true;
                        changed=true;
                        for(int nameIndex=0;nameIndex<names.size();nameIndex++){
                            TextName name=names.get(nameIndex);
                            TextGrade grade=grades.get(nameIndex).get(assignIndex);
                            if(grade==null){ //we havent graded it yet, but it might be submitted
                                if(!gui.getManager().fileExists(assign.number,name)){ //they don't have anything submitted that hasnt been graded
                                    gui.getEmailer().emailLateAssignment(assign,name);
                                }
                            }
                        }
                    }
                }
                if(changed){
                    gui.getBackgroundThread().removeQueued("UploadGrades");
                    gui.getBackgroundThread().invokeLater(new NamedRunnable() {

                    @Override
                    public void run() {
                        gui.getGrader().uploadTable();
                    }

                    @Override
                    public String name() {
                        return "UploadGrades";
                    }
                });
                }
            }
        });
    }
    public TextName[] getAllNames(){
        return names.toArray(new TextName[0]);
    }
    public void deleteAllNames(){
        deleteAllGrades();
        names.clear();
        grades.clear();
    }
    public void deleteAllAssignments(){
        deleteAllGrades();
        assignments.clear();
        for(int i=0;i<grades.size();i++){
            grades.get(i).clear();
        }
    }
    public void deleteAllGrades(){
        for(int x=0;x<grades.size();x++){
            for(int y=0;y<grades.get(x).size();y++){
                grades.get(x).set(y, null);
            }
        }
    }
    private void clearData(){
        if(assignments!=null)
            assignments.clear();
        else
            assignments=new ArrayList();
        if(names!=null)
            names.clear();
        else
            names=new ArrayList();
        if(grades!=null)
            grades.clear();
        else
            grades=new ArrayList();
        
        grades=new ArrayList();
        grades.add(new ArrayList());
    }
    public static String validateString(String s){
        return validateString(s,false);
    }
    public static String validateString(String s,boolean returns){
        s=s.replace(COMMENTDELIMITER,"");
        s=s.replace(GRADEDELIMITER, "");
        s=s.replace(INDIVIDUALDELIMITER, "");
        s=s.replace(INDIVIDUALDELIMITER2, "");
        s=s.replace(INDIVIDUALDELIMITER3, "");
        s=s.replace(INDIVIDUALDELIMITER4, "");
        if(!returns){
            s=s.replace("\n", "");
            s=s.replace("\r", "");
        } else{
            s=s.replaceAll("\r", "\n");
        }
        
        return s;
    }
}
