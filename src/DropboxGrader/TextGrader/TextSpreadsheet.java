/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.TextGrader;

import DropboxGrader.DbxSession;
import DropboxGrader.GuiHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author Matt
 */
public class TextSpreadsheet {
    public static final String GRADEDELIMITER="¶"; //Can't use most keyboard symbols with split because of regex... So I had to get creative
    public static final String COMMENTDELIMITER="«"; //These symbols are banned from all forms of input dealing with the grades spreadsheet
    public static final String INDIVIDUALDELIMITER="÷"; //See validateString(String) for more info on banning them
    
    private ArrayList<TextAssignment> assignments;
    private ArrayList<TextName> names;
    private ArrayList<ArrayList<TextGrade>> grades; //in the same order as names, then the same order as assignments
    
    public TextSpreadsheet(){
        
    }
    public void parse(File f){
        String code=DbxSession.readFromFile(f);
        
        //Clear current data and initialize everything except grades
        clearData();
        
        //Start parsing
        String[] lines=code.split("\n");
        //Initialize Grades

        int row=0; //keeps track of where we are in terms of real lines
        for (String line : lines) {
            if(!line.contains(COMMENTDELIMITER)&&line.contains(GRADEDELIMITER)){ //if its not a comment and the line contains delimitors
                String[] gradesInLine=line.split(GRADEDELIMITER);
                for(int col=0;col<gradesInLine.length;col++){
                    if(row==0){ //We are on the line that declares assignments
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
            for(int i=0;i<grades.get(0).size();i++){
                if(i==0){ //name entry
                    code+=name.toText()+GRADEDELIMITER;
                }
                //grade entry
                TextGrade g=grades.get(row).get(i);
                if(g==null){
                    code+="null";
                }
                else{
                    code+=g.toText();
                }
                if(i<grades.get(0).size()){
                        code+=GRADEDELIMITER;
                }
            }
            code+="\n";
        }
        
        DbxSession.writeToFile(f, code);
    }
    public void addAssignment(int assignmentNum,String assignmentName){ //TODO: store due date
        assignmentName=validateString(assignmentName);
        
        assignments.add(new TextAssignment(assignmentNum,assignmentName));
        
        for(ArrayList<TextGrade> grade:grades){
            grade.add(null);
        }
    }
    public void addName(String firstName,String lastName){ //TODO: store email and email when wrongly submitted file
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
        names.add(new TextName(firstName,lastName));
        
        int numAssignments=grades.get(0).size();
        grades.add(new ArrayList());
        int nameIndex=names.indexOf(names.get(names.size()-1));
        for(int i=0;i<numAssignments;i++){
            grades.get(nameIndex).add(null);
        }
    }
    public boolean setGrade(TextName name,TextAssignment assignment,String grade,String comment, boolean overwrite){
        grade=validateString(grade);
        comment=validateString(comment);
        int assignmentIndex=assignments.indexOf(assignment);
        int nameIndex=names.indexOf(name);
        if(assignmentIndex==-1||nameIndex==-1){
            GuiHelper.alertDialog("Error grading, try again.");
            System.err.println("Error grading "+assignment+" by "+name+"\nAssignmentIndex: "+assignmentIndex+" NameIndex: "+nameIndex);
            return false;
        }
        TextGrade currentGrade=grades.get(nameIndex).get(assignmentIndex);
        if(currentGrade!=null){
            if(currentGrade.grade.equals(grade)&&currentGrade.comment.equals(comment)){ //if they have the same data
                return true; //we can say we are done because there is nothing to change.
            }
            if(!overwrite){
                overwrite=GuiHelper.yesNoDialog("There is already a grade written: "+getGrade(name,assignment)+"\nWould you like to overwrite this grade?");
                if(!overwrite){
                    return false;
                }
            }
        }
        grades.get(nameIndex).set(assignmentIndex,new TextGrade(grade,comment)); //TODO: record date and timestamp with this.
        return true;
    }
    public boolean setGradeAt(int nameIndex,int assignmentIndex,String grade,String comment,boolean overwrite){
        grade=validateString(grade);
        comment=validateString(comment);
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
    public TextName getName(String name){
        for(TextName tName:names){
            if(name.toLowerCase().contains(tName.firstName.toLowerCase())&&name.toLowerCase().contains(tName.lastName.toLowerCase())){
                return tName;
            }
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
        HashSet<TextName> names=new HashSet();
        for(String s:split){
            for(TextName tName:this.names){
                if(tName.firstName.toLowerCase().contains(s.toLowerCase())){
                    names.add(tName);
                }
                if(tName.lastName.toLowerCase().contains(s.toLowerCase())){
                    names.add(tName);
                }
                if(name.toLowerCase().contains(tName.firstName.toLowerCase())&&
                        name.toLowerCase().contains(tName.lastName.toLowerCase())){
                    names.add(tName);
                }
            }
        }
        return names.toArray(new TextName[names.size()]);
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
            if(names.get(i)!=name&&names.get(i).equals(new TextName(newNames[0],newNames[1]))){ //if it isn't the same pointer, but it has the same first/last name
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
        if(nameIndex<0||assignmentIndex<0||nameIndex>names.size()||assignmentIndex>assignments.size()){
            return null;
        }
        return grades.get(nameIndex).get(assignmentIndex);
    }
    public TextAssignment getAssignmentAt(int assignmentIndex){
        return assignments.get(assignmentIndex);
    }
    public TextName getNameAt(int nameIndex){
        return names.get(nameIndex);
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
    private String validateString(String s){
        s=s.replace(COMMENTDELIMITER,"");
        s=s.replace(GRADEDELIMITER, "");
        s=s.replace(INDIVIDUALDELIMITER, "");
        
        return s;
    }
}
