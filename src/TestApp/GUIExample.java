package TestApp;

import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Manages gui stuffs
 * @author Matt
 */
public class GUIExample extends JFrame implements ActionListener{
    private TableModel tableData;
    private JButton populateData;
    private JTextField dataNum;
    private JLabel dataHint;
    private JTable table;
    private String[] database;
    private File file;
    /**
     * Creates new gui with phonebook.txt file
     */
    public GUIExample(){
        file=new File("phonebook.txt");
        init();
    }
        JFrame frame=new JFrame();
        
        /**
         * Inits widgets
         */
	public void init() {
            frame=new JFrame();
            tableData=new AbstractTableModel() {
                @Override
                public int getRowCount() {
                    if(database!=null) {
                        return database.length;
                    }
                    else {
                        return 0;
                    }
                }

                @Override
                public int getColumnCount() {
                    return 2;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex) {
                    if(database==null)
                        return null;
                    else
                        return Main.getAt(rowIndex, columnIndex, database);
                }
                @Override
                public String getColumnName(int col){
                    if(col==0){
                        
                        if(Main.nameFirst())
                            return "Name";
                        else
                            return "Number";
                    }
                    else{
                        if(Main.nameFirst())
                            return "Number";
                        else
                            return "Name";
                    }
                }
            };
            table=new JTable(tableData);
            JScrollPane scrollpane=new JScrollPane(table);
            
            populateData=new JButton();
            populateData.setText("Populate File");
            populateData.setActionCommand("Search");
            populateData.addActionListener(this);
            
            dataNum=new JTextField(10);
            dataHint=new JLabel();
            dataHint.setText("Type how many numbers you would like in the phonebook or type nothing to read from text file.");
            frame.setLayout(new FlowLayout(4));
            frame.setSize(600, 520);
            frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
            
            frame.add(dataHint);
            frame.add(dataNum);
            frame.add(populateData);
            frame.add(scrollpane);
            
            postInit();
	}
        /**
         * Stuff to do after init
         */
	public void postInit(){
            frame.setVisible(true);
        }
        /**
         * Is called each time some action is recorded who's action listener is attached to this class
         * @param e event
         */
    @Override
	public void actionPerformed(ActionEvent e) {
	
		if (e.getActionCommand().equals("Search")) {
                    if(populateData.getText().equals("Populate File")){
                        if(!dataNum.getText().equals("")){
                            try{
                                Main.populate(file, Long.parseLong(dataNum.getText()));
                                database=Main.parseFile(file);
                                Arrays.sort(database);

                                table.revalidate();
                                
                                dataHint.setText("Enter a name or number to search for");
                                populateData.setText("Search");
                            } catch(NumberFormatException ex){
                                dataHint.setText("Enter a valid number of items to put in phonebook or leave the field empty.");
                            }
                            
                        }
                        else{
                            database=Main.parseFile(file);
                            Arrays.sort(database);
                            table.revalidate();
                            
                            dataHint.setText("Enter a name or number to search for");
                            populateData.setText("Search");
                        }
                    }
                    else if(populateData.getText().equals("Search")){
                        if(!dataNum.getText().equals("")){
                            try{
                                Integer results=Main.search(database, Long.parseLong(dataNum.getText()));
                                table.clearSelection();
                                if(results!=null){
                                    table.setRowSelectionInterval(results,results);
                                    table.scrollRectToVisible(new Rectangle(table.getCellRect(results, 0, true)));
                                }
                            } catch(NumberFormatException ex){
                                Integer results=Main.search(database, dataNum.getText());
                                table.clearSelection();
                                if(results!=null){
                                    table.setRowSelectionInterval(results,results);
                                    table.scrollRectToVisible(new Rectangle(table.getCellRect(results, 0, true)));
                                }
                            }
                        }
                    }
                    table.updateUI();
                    table.repaint();
                    table.revalidate();
                    //it wont repaint the freaking headers...
		}
	}

/**
     * Not used but was used in my other program to log downlaod issues.
     * @param ex exception
     */
    public void crash(Exception ex){
        if(ex!=null){
            dataHint.setText(ex.toString());
        }
    }
}
