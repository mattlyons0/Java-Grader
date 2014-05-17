package DropboxGrader;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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
     * Puts text in JOptionPane
     * @param message text to display in pane.
     */
    public static void alertDialog(final String message){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null,message);
            }
        });
    }

    public static boolean yesNoDialog(String question) {
        String[] yesno={"Yes","No"};
        int choice=multiOptionPane(question,yesno);
        return choice==0;
    }
}