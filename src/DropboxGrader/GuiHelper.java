package DropboxGrader;

import javax.swing.JOptionPane;
import java.util.Random;
import java.util.Arrays;

public class GuiHelper{
    /**
     * Makes popup with multiple options to choose.
     * @param question Message to display above options
     * @param choices options to choose from
     * @return number corresponding to array index of selected choice
     */
    public static int multiOptionPane(String question,String[] choices){
        return JOptionPane.showOptionDialog(null,""+question,"",
                JOptionPane.OK_OPTION,JOptionPane.QUESTION_MESSAGE,null,
                choices,-1);
    }
    /**
     * Creates input dialog
     * @param question text to display above text box.
     * @return input
     */
    public static String inputDialog(String question){
        String choice=JOptionPane.showInputDialog(question);
        //Can't seem to get rid of cancel, so this'll do...
        if(choice!=null){
            return choice;
        }
        else{
            String[] yesno={"Yes","No"};
            int close=multiOptionPane("Are you sure?",yesno);
            if(close==0){
                System.exit(0);
            }
            return inputDialog(question);
        }

    }
    /**
     * Puts text in JOptionPane
     * @param message text to display in pane.
     */
    public static void alertDialog(String message){
        JOptionPane.showMessageDialog(null,message);
    }
}