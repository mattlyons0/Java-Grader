/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import DropboxGrader.GuiElements.Grader.TextFile;
import DropboxGrader.GuiElements.Grader.JavaCodeBrowser;
import DropboxGrader.RunCompileJava.JavaFile;
import DropboxGrader.GuiElements.FileBrowser.CellLocation;
import DropboxGrader.TextGrader.TextGrade;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import net.lingala.zip4j.exception.ZipException;

/**
 *
 * @author Matt
 */
public class DbxFile {
    private FileManager fileManager;
    private DbxEntry.File entry;
    private DbxClient client;
    private File downloadedFile;
    private JavaFile[] javaFiles;
    private TextFile[] textFiles;
    private boolean invalidZip;
    private int fileVersion;
    private final String ERRORMSG;
    private boolean changedFolder;
    public DbxFile(DbxEntry.File entry,FileManager fileMan,DbxClient client){
        this.entry=entry;
        fileManager=fileMan;
        this.client=client;
        
        fileVersion=0;
        ERRORMSG="File Naming Error: "+entry.name;
        
        if(entry.name.indexOf(".")!=entry.name.length()-4){
            String newName=entry.name.replace(".", "");
            if(newName.contains("zip"))
                newName=newName.substring(0, newName.length()-3);
            newName+=".zip";
            rename(newName);
        }
        
        checkExists();
    }
    public void movedFiles(){
        changedFolder=true;
        checkExists();
        if(fileManager.getGui().getRunner()!=null)
            fileManager.getGui().getRunner().stopProcess();
    }
    private void checkExists(){ //ties reference if it is already downloaded
        String zipPath=fileManager.getDownloadFolder()+"/"+entry.name;
        int dotIndex=zipPath.indexOf('.');
        if(dotIndex!=-1)
            zipPath=zipPath.substring(0,dotIndex);
        File file=new File(zipPath);
        if(file.exists()){ //could lead to different copy locally than remotely, but files arent supposed to be overwritten anyway.
            setFile(file);
        }
    }
    public File forceDownload(){
        return download(true,0);
    }
    public File download(){
        if(downloadedFile==null||downloadedFile.listFiles()==null||downloadedFile.listFiles().length==0){
            return download(true,0);
        }
        return download(false,0);
    }
    private File download(boolean force,int retryNum){
        String zipPath=fileManager.getDownloadFolder()+"/"+entry.name;
        zipPath=zipPath.substring(0,zipPath.indexOf('.'));
        File file=new File(zipPath);
        if(force&&file.exists()){
            deleteDirectory(file);
            file.delete();
        }
        if(!force&&file.exists()){
            setFile(file);
            return file;
        }
        try {
            FileOutputStream f = new FileOutputStream(fileManager.getDownloadFolder()+"/"+entry.name);
            client.getFile(entry.path, null, f); //downloads from dropbox server
            f.close();
            Unzip.unzip(file.getPath()+".zip", fileManager.getUnzipFolder());
            new File(fileManager.getDownloadFolder()+"/"+entry.name).delete();
            setFile(file);
            return file;
        } catch (DbxException | IOException |ZipException ex) {
            System.err.println("Exception when unzipping "+ex);
            if(ex instanceof ZipException){
                if(retryNum>0){
                    download(true,1);
                }
                setInvalidZip();
            }
            else if(ex instanceof DbxException){
                if(ex instanceof DbxException.ServerError||ex instanceof DbxException.RetryLater){
                    GuiHelper.alertDialog("Dropbox is under heavy load. File failed to download.\n"+ex);
                }
                else if(ex instanceof DbxException.InvalidAccessToken){
                    GuiHelper.alertDialog("Access to dropbox has been revoked, please relaunch this program.\n"+ex);
                }
                else{
                    GuiHelper.alertDialog("Error downloading from dropbox.\n"+ex);
                }
            }
            else if(ex instanceof IOException){
                GuiHelper.alertDialog("Writing file to disk failed, You probably don't have permission to download here.\n"+ex);
            }
            return null;
        }
    }
    public int getAssignmentNumber(){
        String s=entry.name;
        
        int num=safeStringToInt(s.split("_")[2]);
        if(num==-1){
            //theres no number supplied
        }
        return num;//assignment number is the 3rd underscore
    }
    public String getAssignmentName(int row,int col){
        String s=entry.name;
        if(!s.endsWith(".zip")){
            return "Error \""+entry.name+"\" doesn't end with .zip!";
        }
        String[] splits=s.split("_");
        if(splits.length<4){
            return ERRORMSG;
        }
        if(!isNotFirstYear(splits[2])&&splits.length==5||isCorrection(splits)){
            fileManager.getTableData().setColorAt(Color.YELLOW, new CellLocation(fileManager.getAttributes()[col],row));
            return splits[3]+" (Resubmit)";
        }
        else if(splits.length==5){
            fileManager.getTableData().setColorAt(Color.CYAN.darker(), new CellLocation(fileManager.getAttributes()[col],row));
            int year=safeStringToInt(splits[2]);
            return splits[4].substring(0, splits[4].length()-4)+" (Year "+year+")";
        }
        else if(splits.length==4&&splits[3].length()>4){
            return splits[3].substring(0, splits[3].length()-4);//assignment name is 4th underscore, .zip is the last 4 characters
        }
        else if(splits.length>3){
            return splits[3];
        }
        else{
            return ERRORMSG;
        }
    }
    private boolean isNotFirstYear(String s){
        if(s.contains("Yr")||s.contains("yr")||s.contains("YR")||s.contains("Year")||s.contains("year")||s.contains("YEAR")){
            return true;
        }
        return false;
    }
    private boolean isCorrection(String[] assignment){
        if(assignment.length==6){
            if(assignment[5].contains("Correction")||assignment[5].contains("CORRECTION")||assignment[5].contains("correction")||
                    assignment[5].contains("Resubmit")||assignment[5].contains("resubmit")||assignment[5].contains("RESUBMIT")){
                return true;
            }
        }
        return false;
    }
    /**
     * The submission time on the dropbox server. This is the the most recent revision date.
     * @param checkRevisions if true, will check if the file has been modified since initial submission
     * @param row set by manager
     * @param col set by manager
     * @return if checkRevisions is false will return the date of the newest version, otherwise will
     * return the date or if it has been modified both dates.
     */
    public String getSubmitDate(boolean checkRevisions,int row,int col){
        if(checkRevisions&&entry.lastModified.after(entry.clientMtime)&&row>-1&&col>-1){
            fileManager.getTableData().setColorAt(new Color(230,120,120), new CellLocation(fileManager.getAttributes()[col],row));
            return "Originally "+entry.clientMtime+" Modified "+entry.lastModified;
        }
        return entry.clientMtime.toString();
    }
    public String getFirstLastName(){
        String s=entry.name;
        return s.split("_")[1];
    }
    public String getStatus(int row,int col){
        int num=getAssignmentNumber();
        if(fileManager.getGrader()!=null){
            TextGrade grade=fileManager.getGrader().getGrade(getFirstLastName(), num);
            if(grade!=null){
                fileManager.getTableData().setColorAt(Color.GREEN, new CellLocation(fileManager.getAttributes()[col],row));
                return "Graded: "+grade;
            }
        }
        if(downloadedFile==null){
            return "On Server";
        }
        if(downloadedFile.exists()&&invalidZip){
            return "Invalid Zip File";
        }
        if(downloadedFile.exists()){
            return "Downloaded";
        }
        downloadedFile=null;
        return getStatus(row,col);
    }
    public static int safeStringToInt(String s){
        char[] chars=s.toCharArray();
        String num="";
        for(int x=0;x<s.length();x++){
            if(Character.isDigit(chars[x])){
                num+=chars[x];
            }
        }
        if(num.length()==0){
            return -1;
        }
        return Integer.parseInt(num);
    }
    private void setFile(File f){
        downloadedFile=f;
        searchJavaFiles();
        searchTextFiles();
        fileVersion++;
    }
    private void searchJavaFiles(){
        File[] newArr=searchForFiles(downloadedFile.getPath()+"/",".java");
        javaFiles=new JavaFile[newArr.length];
        for(int x=0;x<newArr.length;x++){
            javaFiles[x]=(JavaFile)newArr[x];
        }
        for(JavaFile j:javaFiles){
            j.setOtherClasses(javaFiles);
        }
    }
    private void searchTextFiles(){
        File[] newArr=searchForFiles(downloadedFile.getPath()+"/",".txt");
        textFiles=new TextFile[newArr.length];
        for(int x=0;x<newArr.length;x++){
            textFiles[x]=(TextFile)newArr[x];
        }
    }
    /**
     * Recursive file search
     * @param directory directory to search in
     * @param fileType files which end in this will be returned.
     * @return an array of files with specified ending characters.
     * If type is .java it will return JavaFile[]
     * If type is .txt it will return TextFile[]
     */
    private File[] searchForFiles(String directory,String fileType){
        //System.out.println("Searching in "+directory);
        ArrayList<File> files=new ArrayList();
        ArrayList<File> filesWithType;
        filesWithType=new ArrayList();
        File folder=new File(directory);
        files.addAll(Arrays.asList(folder.listFiles()));
        
        for(int x=0;x<files.size();x++){
            File f=files.get(x);
            if(f.isFile()){
                if(f.getName().endsWith(fileType.toLowerCase())||f.getName().endsWith(fileType.toUpperCase())){
                    //if file is .Java it wont get added, but that is stupid capitalization that nothing would save as anyway.
                    if(fileType.equalsIgnoreCase(".java"))
                        f=new JavaFile(f,this);
                    else if(fileType.equalsIgnoreCase(".txt"))
                        f=new TextFile(f);
                    filesWithType.add(f);
                    //System.out.println("Adding "+f);
                }
            }
            else if(f.isDirectory()){
                if(!f.getName().endsWith(".git")&&!f.getName().equals("__MACOSX")){ //skip git folder for performance, skip mac cache folder in zips
                    filesWithType.addAll(Arrays.asList(searchForFiles(directory+"/"+f.getName(),fileType)));
                }
            }
        }
        
        File[] fileArr=new File[filesWithType.size()];
        return filesWithType.toArray(fileArr);
    }
    public String getFileStructure(){
        String zipPath=entry.name;
        zipPath=zipPath.substring(0,zipPath.indexOf('.'));
        String str=getFileStructure(fileManager.getDownloadFolder()+"/"+zipPath,null);
        if(str.equals("")){
            str="No files exist in the zip.";
        }
        return str;
    }
    private String getFileStructure(String directory,String output){
        if(output==null){
            output="";
        }
        File dir=new File(directory);
        File[] files=dir.listFiles();
        if(files!=null){
            for(File f:files){
                String tabs="";
                for(int x=0;x<occurancesOf('/',f.getPath());x++){
                    tabs+="  ";
                }
                if(f.isDirectory()){
                    output=getFileStructure(directory+"/"+f.getName(),output)+output;
                    output=tabs+f.getName()+"/\n"+output;
                }
                else if(f.isFile()){
                    output+=tabs+f.getName()+"\n";
                }
            }
        }
        return output;
    }
    private int occurancesOf(char c,String s){
        int occurance=0;
        for(char ch:s.toCharArray()){
            if(ch==c){
                occurance++;
            }
        }
        return occurance;
    }
    private File[] searchForFiles(String directory){
        ArrayList<File> files=new ArrayList();
        ArrayList<File> filesWithType;
        filesWithType=new ArrayList();
        File folder=new File(directory);
        if(folder.listFiles()!=null)
            files.addAll(Arrays.asList(folder.listFiles()));
        
        for(int x=0;x<files.size();x++){
            File f=files.get(x);
            if(f.isFile()){
                filesWithType.add(f);
            }
            else if(f.isDirectory()){
                if(!f.getName().endsWith(".git")){ //skip git folder for performance
                    filesWithType.addAll(Arrays.asList(searchForFiles(directory+"/"+f.getName())));
                }
            }
        }
        File[] fileArr=new File[(filesWithType.size())];
        return filesWithType.toArray(fileArr);
    }
    public JavaFile[] getJavaFiles(){
        return javaFiles;
    }
    public TextFile[] getTextFiles(){
        return textFiles;
    }
    
    public boolean run(int times,JavaCodeBrowser codeView){
        int currentVersion=fileVersion;
        ArrayList<JavaFile> mainMethods=new ArrayList();
        for(JavaFile f:javaFiles){
            if(f.containsMain()){
                mainMethods.add(f);
            }
        }
        if(mainMethods.isEmpty()){
            GuiHelper.alertDialog("No classes contain main methods.");
            return false;
        }
        int choice=0;
        if(mainMethods.size()>1){
            String[] choices=new String[mainMethods.size()];
            for(int x=0;x<mainMethods.size();x++){
                String path="";
                if(mainMethods.get(x).packageFolder()!=null)
                    path+=mainMethods.get(x).packageFolder()+".";
                path+=mainMethods.get(x).getName();
                choices[x]=path;
            }
            choice=GuiHelper.multiOptionPane("There are multiple main methods, which would you like to run?", choices);
        }
        if(choice==-1){
            return false;
        }
        if(codeView!=null)
            codeView.setRunningFile(mainMethods.get(choice));
        boolean success = fileManager.getGui().getRunner().runFile(javaFiles,mainMethods.get(choice),times,downloadedFile.getPath());
        if(!success&&currentVersion!=fileVersion){ //if we moved it between saving/running try again
            return run(times,codeView);
        }
        return success;
    }
    public String getFileName(){
        return entry.name;
    }
    public void delete(){
        if(downloadedFile!=null)
            searchForFilesToDelete(downloadedFile.getPath());
        try {
            client.delete(entry.path);
        } catch (DbxException ex) {
            System.err.println("Error occured deleting "+entry.name+" from dropbox.");
            ex.printStackTrace();
        }
    }
    private void searchForFilesToDelete(String directory){
        ArrayList<File> files=new ArrayList();
        File folder=new File(directory);
        files.addAll(Arrays.asList(folder.listFiles()));
        
        for(int x=0;x<files.size();x++){
            File f=files.get(x);
            if(f.isFile()){
                f.delete();
            }
            else if(f.isDirectory()){
                searchForFilesToDelete(directory+"/"+f.getName());
                f.delete();
            }
        }
        new File(directory).delete();
    }
    public void rename(String newName){
        System.out.println(entry.path);
        String directory=entry.path;
        String[] split=directory.split("/");
        int len=split[split.length-1].length();
        directory=directory.substring(0, directory.length()-len);
        boolean moved=false;
        try {
            client.move(entry.path, directory+newName);
            moved=true;
        } catch (DbxException ex) { //try again with a number after the name
            int num=1;
            int dotIndex=getLastIndex(newName,'.');
            if(Character.isDigit(newName.charAt(dotIndex-1)))
                num=safeStringToInt(Character.toString(newName.charAt(dotIndex-1)));
            rename(newName.substring(0, dotIndex)+num+newName.substring(dotIndex, newName.length()));
        }
        if(moved&&downloadedFile!=null){
            searchForFilesToDelete(downloadedFile.getPath());
            downloadedFile=null;
        }
        if(moved){
            fileManager.getGui().refreshTable();
        }
    }
    public static int getLastIndex(String s,char c){
        for(int x=s.length()-1;x>0;x--){
            if(s.charAt(x)==c){
                return x;
            }
        }
        return -1;
    }
    @Override
    public String toString(){
        String zipPath=entry.name;
        zipPath=zipPath.substring(0,zipPath.indexOf('.'));
        return zipPath+" submitted on "+getSubmitDate(true,-1,-1);
    }
    public boolean isDownloaded(){
        if(downloadedFile!=null){
            return true;
        }
        return false;
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.entry);
        hash = 29 * hash + Objects.hashCode(this.downloadedFile);
        hash = 29 * hash + Arrays.deepHashCode(this.javaFiles);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DbxFile other = (DbxFile) obj;
        if (!Objects.equals(this.entry, other.entry)) {
            return false;
        }
        if (!Objects.equals(this.downloadedFile, other.downloadedFile)) {
            return false;
        }
        if (!Arrays.deepEquals(this.javaFiles, other.javaFiles)) {
            return false;
        }
        return true;
    }
    public void setInvalidZip(){
        invalidZip=true;
    }
    public boolean isInvalidZip(){
        return invalidZip;
    }
    public boolean changedFolder(){
        return changedFolder;
    }
    public static void deleteDirectory(File directory){
        if(directory.isFile()){
            directory.delete();
        }
        else if(directory.isDirectory()){
            File[] files=directory.listFiles();
            for(File f:files){
                if(f.isFile()){
                    f.delete();
                }
                else if(f.isDirectory()){
                    deleteDirectory(f);
                    f.delete();
                }
            }
        }
    }
    
}
