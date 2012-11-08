/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Statistics.HouseInformation;
import Statistics.NeighbourHoodInformation;
import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.upc.edu.Agents.Environment;

/**
 *
 * @author pjmartorell
 */
public class SmartGridGui extends JFrame {

    private Environment myAgent;
    private boolean DEBUG = false;
    TableStat ct, nt, ht;

    public SmartGridGui(Environment a) {
        super("SmartGrid");

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 1));
//
//        /* TABLE */
//
//        JTable table = new JTable(new MyTableModel());
//        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
//        table.setFillsViewportHeight(true);
//        p.setLayout(new BorderLayout());
//        p.add(table.getTableHeader(), BorderLayout.PAGE_START);
//        p.add(table, BorderLayout.CENTER);
//
//        //Create the scroll pane and add the table to it.
//        JScrollPane scrollPane = new JScrollPane(table);
//
//        //Add the scroll pane to this panel.
//        p.add(scrollPane);
//
//        /* END TABLE */

        String[] Hcols = {"Name", "Neighbourhood", "Credits", "Consumption", "Production", "Storage", "Buy/Sell",
            "Adversary", "Qty", "Price",
            "Battery", "Qty", "Price",
            "Central", "Qty", "Price"};
        String[] Ncols = {"Name", "Storage Level", "Negotiations", "Gets Batt", "Store Batt", "Company Buy", "Company Sell"};
        String[] Ccols = {"Batt Level Mean", "# Gets Batt", "# Store Batt",
            "# Company Buys", "# Company Sells"};
        //ct = new TableStat(Ccols);
        nt = new TableStat(Ncols);
        ht = new TableStat(Hcols);
       // p.add(ct, BorderLayout.CENTER);
        p.add(nt, BorderLayout.CENTER);
        p.add(ht, BorderLayout.CENTER);

        getContentPane().add(p, BorderLayout.PAGE_START);

        JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                try {

                    myAgent.pause();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SmartGridGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JButton resumeButton = new JButton("Resume");
        resumeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                try {
                    myAgent.resume();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SmartGridGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        p = new JPanel();
        p.add(pauseButton);
        p.add(resumeButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 5;
        int centerY = (int) screenSize.getHeight() / 5;
        setLocation(centerX, centerY);
        super.setVisible(true);
        super.setSize(800, 600);
    }

    public void updateGui(Vector housesVector, Vector neighVector) {

        Object[][] data = new Object[housesVector.size()][16];
        Object[][] data2 = new Object[neighVector.size()][7];

        String seller;
        double price;
        boolean company;

        for (int i = 0; i < housesVector.size(); i++) {

            HouseInformation hi = (HouseInformation) housesVector.get(i);

            data[i][0] = hi.getName();
            data[i][1] = hi.getNeighbourhoodName();
            data[i][2] = hi.getCredits();
            data[i][3] = new Double(hi.getConsumptionLevel());
            data[i][4] = new Double(hi.getProductionLevel());
            data[i][5] = new Double(hi.getStorageLevel());
            data[i][6] = hi.getTransactionType();
            data[i][7] = hi.getAdversary();
            data[i][8] = new Double(hi.getQtyTransaction());
            data[i][9] = new Double(hi.getPriceTransaction());
            data[i][10] = hi.getBatteryOperationType();
            data[i][11] = new Double(hi.getQtyBattery());
            data[i][12] = new Double(hi.getPriceBattery());
            data[i][13] = hi.getCentralOperationType();
            data[i][14] = new Double(hi.getQtyCentral());
            data[i][15] = new Double(hi.getPriceCentral());
        }

        for (int i = 0; i < neighVector.size(); i++) {
            NeighbourHoodInformation ni = (NeighbourHoodInformation) neighVector.get(i);

            data2[i][0] = ni.getName();
            data2[i][1] = ni.getStorageLevel();
            data2[i][2] = ni.getReqBuy();
            data2[i][3] = ni.getReqGetBattery();
            data2[i][4] = ni.getReqStoreBattery();
            data2[i][5] = ni.getReqBuyCentral();
            data2[i][6] = ni.getReqSellCentral();
        }

        ht.updateTable(data);
        nt.updateTable(data2);
    }
}
