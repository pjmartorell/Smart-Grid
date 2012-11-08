/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.edu.Behaviours;

import Statistics.HouseInformation;
import Statistics.NeighbourHoodInformation;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.upc.edu.Agents.Environment;

/**
 *
 * @author sergio.jurado
 */
public class EnvironmentStatisticsBehaviour extends CyclicBehaviour {

    Environment agent;
    int numReplies;
    int step;
    MessageTemplate statisticsTemplate;
    Vector<HouseInformation> housesVector;
    Vector<NeighbourHoodInformation> neighVector;
    
    //vars for seller lookup

    public EnvironmentStatisticsBehaviour(Environment agent) {

        super(agent);
        
        this.agent=agent;
        numReplies=0;
        step=0;
       
        this.statisticsTemplate= MessageTemplate.MatchConversationId("STATISTICS");
        this.statisticsTemplate= MessageTemplate.or(statisticsTemplate, MessageTemplate.MatchConversationId("STATISTICS_NEIGHBOURHOOD"));
        housesVector = new Vector (agent.getNumHouses());
        housesVector.setSize(agent.getNumHouses());
        neighVector = new Vector (agent.getNumNeighbourhoods());
        neighVector.setSize(agent.getNumNeighbourhoods());

    }

    public void initVars(){

        numReplies = 0;
        housesVector.clear();
        housesVector.setSize(agent.getNumHouses());
        
    }

    public void action() {
        
        switch (step) {

            case 0:               
                //wait for all messages for this round
                waitForStatisticsMessage();
                break;

            case 1:
                // Sending the cfp to all houses
                processStatistics();
                initVars();
                break;

        }
    }

    private void waitForStatisticsMessage() {

        ACLMessage statistics = agent.receive(this.statisticsTemplate);
        if (statistics != null) {
            
            //Obtenim la informació
            try {
                if (statistics.getConversationId().equals("STATISTICS")){
                    HouseInformation houseInfo=(HouseInformation) statistics.getContentObject();
                    this.housesVector.setElementAt(houseInfo, Integer.valueOf(houseInfo.getName().substring(1))-1);
                }
                else if (statistics.getConversationId().equals("STATISTICS_NEIGHBOURHOOD")){
                    NeighbourHoodInformation neighInfo=(NeighbourHoodInformation) statistics.getContentObject();
                    this.neighVector.setElementAt(neighInfo, Integer.valueOf(neighInfo.getName().substring(1))-1);
                }
                //System.out.println("Message Recieved");
            } catch (UnreadableException ex) {
                Logger.getLogger(EnvironmentStatisticsBehaviour.class.getName()).log(Level.SEVERE, null, ex);
            }
            numReplies++;

            //Mirem si hem rebut totes les respostes
            if (numReplies>=(agent.getNumHouses()+agent.getNumNeighbourhoods())) step=1;
            
        } else {
            block();
        }
    }

    private void processStatistics() {
        agent.sendGui(housesVector, neighVector);
        step=0;
    }
}
