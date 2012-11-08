/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author pjmartorell
 */
public class TableStat extends JPanel {

    private boolean DEBUG = false;
    JTable table;
    MyTableModel tm;


    public TableStat(String[] _columnNames) {

        super(new GridLayout(1, 0));

        tm = new MyTableModel(_columnNames);
        table = new JTable(tm);
        table.setPreferredScrollableViewportSize(new Dimension(800, 250));
        table.setFillsViewportHeight(true);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        //Add the scroll pane to this panel.
        add(scrollPane);
    }

//    public void tableChanged(TableModelEvent e) {
//        int row = e.getFirstRow();
//        int column = e.getColumn();
//        TableModel model = (TableModel) e.getSource();
//        String columnName = model.getColumnName(column);
//        Object data = model.getValueAt(row, column);
//        System.out.println("Data Changed");
//        // Do something with the data...
//    }

    public void updateTable(Object[][] d){
        tm.updateData(d);
    }

    class MyTableModel extends AbstractTableModel {

        String[] columnNames;
        Object[][] data = new Object[0][0];

        private MyTableModel(String[] _columnNames) {
            columnNames = _columnNames;
        }
         
//        Object[][] data = {{"H1", "N1", new Integer(100), new Integer(150),
//                new Integer(0), new Integer(50), "Store", new Boolean(false), "Seller", new Integer(20)}};

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
//        @Override
//        public boolean isCellEditable(int row, int col) {
//            //Note that the data/cell address is constant,
//            //no matter where the cell appears onscreen.
//            if (col < 2) {
//                return false;
//            } else {
//                return true;
//            }
//        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                        + " to " + value
                        + " (an instance of "
                        + value.getClass() + ")");
            }

            data[row][col] = value;
            fireTableCellUpdated(row, col);

            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i = 0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j = 0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }

        public void updateData(Object[][] d){
            System.out.println("Setting DATA!!!");
            data = d.clone();
            fireTableDataChanged();
        }
    }
}
