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
        if(getGrade(name,assignment)!=null&&!overwrite){
            overwrite=GuiHelper.yesNoDialog("There is already a grade written: "+getGrade(name,assignment)+"\nWould you like to overwrite this grade?");
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
    public TextName getName(String name){
        for(TextName tName:names){
            if(name.toLowerCase().contains(tName.firstName.toLowerCase())&&name.toLowerCase().contains(tName.lastName.toLowerCase())){
                return tName;
            }
        }
        return null;
    }
    public TextAssignment getAssignment(int assignmentNum){
        for(TextAssignment tAssignment:assignments){
            if(tAssignment.number==assignmentNum){
                return tAssignment;
            }
        }
        return null;
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
        if(nameIndex<0||assignmentIndex<0){
            return null;
        }
        TextAssignment assignment=assignments.get(assignmentIndex);
        TextName name=names.get(nameIndex);
        return getGrade(name,assignment);
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
