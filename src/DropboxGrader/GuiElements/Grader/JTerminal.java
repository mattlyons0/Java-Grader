/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.Grader;

import DropboxGrader.Gui;
import DropboxGrader.GuiHelper;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 *
 * @author Matt
 */
public class JTerminal extends JTextPane implements KeyListener,MouseListener,ActionListener{
    private PrintWriter writer;
    private String input;
    public JTerminal(){
        super();
        addKeyListener(this);
        addMouseListener(this);
        input="";
        
        setMargin(new Insets(5,5,5,5));
    }
    public void setInputStream(OutputStream out){
        if(writer!=null){
            writer.close();
            writer=null;
        }
        writer=new PrintWriter(out);
    }
    public void append(String s){
        append(s,Color.BLACK);
    }
    public void append(String s,Color c){
        addText(s,c);
    }
    private void addText(String s,Color c){
        StyleContext context= StyleContext.getDefaultStyleContext();
        AttributeSet set=context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        set=context.addAttribute(set, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        int length=getDocument().getLength();
        setCaretPosition(length);
        setCharacterAttributes(set,false);
        replaceSelection(s);
        length=getDocument().getLength();
        setCaretPosition(length);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if(e.getKeyChar()=='\n'||e.getKeyChar()=='\r')
            return;
        
        input+=e.getKeyChar();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.isActionKey()||e.isAltDown()||e.isAltGraphDown()||e.isControlDown()||
                e.isMetaDown()&&e.getKeyCode()!=KeyEvent.VK_BACK_SPACE){ //these are the allowed keys
            
        }
        else{ //anything else we will put the cursor at the end and unhighlight before you can delete/write things
            setCaretPosition(getDocument().getLength()); //stop trying to delete things, its not gonna work
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {
       char c=e.getKeyChar();
       if(c==KeyEvent.VK_ENTER){
           String[] lines=getText().split("\n");
           if(lines.length==0){
               return;
           }
           if(writer==null){
               return;
           }
           writer.append(input+"\n");
           writer.flush();
           input="";
           //writer.write("");
           //writer.flush();
       }
    }
    public void stop(){
        if(writer!=null)
            writer.close();
    }
    private JPopupMenu createRightClickMenu(){
        JPopupMenu m=new JPopupMenu();
        JMenuItem m1=new JMenuItem("Copy");
        JMenuItem m2=new JMenuItem("Paste");
        m1.addActionListener(this);
        m2.addActionListener(this);
        m.add(m1);
        m.add(m2);
        return m;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON3){
            JPopupMenu popup = createRightClickMenu();
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("Copy")){
            Clipboard c=Toolkit.getDefaultToolkit().getSystemClipboard();
            try{
                c.setContents(new StringSelection(getSelectedText()),null);
            } catch(IllegalStateException ex){
                GuiHelper.alertDialog("Error Accessing Clipboard.");
                System.err.println("Error Copying into Terminal.");
                ex.printStackTrace();
            }
        }
        else if(e.getActionCommand().equals("Paste")){
            try{
                Clipboard c=Toolkit.getDefaultToolkit().getSystemClipboard();
                Object clipData=c.getData(DataFlavor.stringFlavor);
                if(clipData instanceof String){
                    append((String)clipData);
                }
            } catch(Exception ex){ //its a risky operation to assume the data ont he clipboard is a string, so we are ok if that fails
                System.err.println("Error Pasting into Terminal.");
                ex.printStackTrace();
            }
        }
    }
}
