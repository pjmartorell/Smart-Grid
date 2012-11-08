package org.upc.edu.Agents;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import jade.core.AID;
import jade.domain.DFService;
import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 
 */

public class MyAgent extends Agent {

    AID df;
   double credits; //"money available"


    @Override
    protected void setup() {
        //Get the default directory facilitator
        AID df = getDefaultDF();

        //Register on the DF
        DFAgentDescription register_template = new DFAgentDescription();
        register_template.setName(getAID());

        try {
            DFService.register(this, df, register_template);
        } catch (FIPAException ex) {
            Logger.getLogger(House.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected AID getAID(String agentName) {

        AID id = new AID();
        id.setLocalName(agentName);
        DFAgentDescription search_template = new DFAgentDescription();
        search_template.setName(id);

        DFAgentDescription[] search_results;
        try {
            search_results = DFService.search(this, df, search_template);
            if (search_results.length <= 0) {
                System.out.println(agentName + "not found");
            }
        } catch (FIPAException ex) {
            Logger.getLogger(Neighbourhood.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;
    }

    public void setBroadcastReceiver(ACLMessage msg) {

        //set as receiver all the houses
        ArrayList houses = getHouseAIDs();
        ArrayList neighs = getNeighAIDs();
        for (int i = 0; i < houses.size(); i++) {
            msg.addReceiver((AID) houses.get(i));
        }
        for (int i = 0; i < neighs.size(); i++) {
            msg.addReceiver((AID) neighs.get(i));
        }
    }

    public void broadcast(String content) {

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setContent(content);
        setBroadcastReceiver(msg);
        send(msg);
    }

    private ArrayList getHouseAIDs() {

        AMSAgentDescription[] agents = null;

        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults(new Long(-1));
            agents = AMSService.search(this, new AMSAgentDescription(), c);
        } catch (Exception e) {
        }

        ArrayList agentAIDs = new ArrayList();
        for (int i = 0; i < agents.length; i++) {
            AID agentID = agents[i].getName();

            if (agents[i].getName().getLocalName().startsWith("H")) /**IS A HOUSE**/
            {
                agentAIDs.add(agentID);
            }
        }
        return agentAIDs;
    }

    private ArrayList getNeighAIDs() {

        AMSAgentDescription[] agents = null;

        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults(new Long(-1));
            agents = AMSService.search(this, new AMSAgentDescription(), c);
        } catch (Exception e) {
        }

        ArrayList agentAIDs = new ArrayList();
        for (int i = 0; i < agents.length; i++) {
            AID agentID = agents[i].getName();

            if (agents[i].getName().getLocalName().startsWith("N")) /**IS A HOUSE**/
            {
                agentAIDs.add(agentID);
            }
        }
        return agentAIDs;
    }

   public void removeCredits(double credits) {
        this.credits -= credits;
    }

    public void addCredits(double credits) {
        this.credits += credits;
    }

    public double getCredits() {
        return credits;
    }
}
