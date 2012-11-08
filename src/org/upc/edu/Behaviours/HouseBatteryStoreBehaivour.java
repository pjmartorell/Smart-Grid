/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Locale;
import java.util.Scanner;
import org.upc.edu.Agents.House;

/**
 *
 * @author Ferran
 */
public class HouseBatteryStoreBehaivour extends Behaviour {

    private final int EXIT = 99; 
    House agent;
    int step;
    AID neighbourHoodId;
    double energy;
    AID centralId;
    //MESSAGE TEMPLATES
    MessageTemplate centralTemplate;
    double initialEnergy;

    public HouseBatteryStoreBehaivour(AID neighbourHoodId, House agent, double energy, AID centralId) {
        
        super(agent);

        this.agent=agent;
        this.neighbourHoodId=neighbourHoodId;
        this.energy=energy;
        this.centralId=centralId;
        step=0;
        MessageTemplate template1 = MessageTemplate.MatchSender(centralId);
        MessageTemplate template2 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        this.centralTemplate = MessageTemplate.and(template1, template2);
        initialEnergy = energy;
    }

    @Override
    public void action() {
        
        switch (step) {

            case 0:
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(neighbourHoodId);
                request.setContent(String.valueOf(energy));
                request.setConversationId("BATTERY_STORE_REQUEST");
                agent.send(request);
                        System.out.println(agent.getLocalName() + " send BATTERY STORE REQUEST TO NEIGHBOURHOOD OF " +energy +" UNITS ");
                step=1;
            case 1:
                // waiting response from neighbourhood
                MessageTemplate template1 = MessageTemplate.MatchSender(neighbourHoodId);
                MessageTemplate template2 = MessageTemplate.MatchConversationId("BATTERY_STORE_REQUEST");

                MessageTemplate template = MessageTemplate.and(template1, template2);
                System.out.println(agent.getLocalName() + " waiting for a BATTERY_STORE_REQUEST response from NEIGHBOURHOOD "+ neighbourHoodId.getLocalName());

                ACLMessage response = agent.receive(template);
                if (response != null) {

                    double price = 0;

                    //Si ens respon amb un accept, es que tenia prou energia i ens l'ha donada
                    if (response.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                        price = Double.valueOf(response.getContent());
                        System.out.println(agent.getLocalName() + " positive response received from neighbourhood for "+price);
                        agent.setQtyBattery(initialEnergy);

                        step=EXIT;
                    }
                    else if (response.getPerformative() == ACLMessage.INFORM){
                        Scanner c = new Scanner(response.getContent());
                        c.useLocale(Locale.ENGLISH);

                        //amount of energy not allocated
                        energy= c.nextDouble();
                        price = c.nextDouble();
                        agent.setQtyBattery(initialEnergy - energy);

                        System.out.println(agent.getLocalName() + " negative response received from neighbourhood, bought some units from battery for: " + price +"and is buying "+energy +" units from central");
                        step=2;
                    }

                     //get credits used to store at the battery
                     agent.addCredits(price);
                     agent.setBatteryOperationType("Store");
                     agent.setPriceBattery(price);
                } else {
                    block();
                }
                break;
            case 2:
                request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(centralId);
                request.setContent(String.valueOf(energy));
                request.setConversationId("SELL_ENERGY");
                agent.send(request);
                        System.out.println(agent.getLocalName() + " send SELL REQUEST TO CENTRAL OF " +energy +" UNITS ");
                step=3;
                break;//selling to central
              
            case 3:
                waitCentralResponse();
                break;
        }
    }

    @Override
    public boolean done() {
        return step==EXIT;
    }


       private void waitCentralResponse(){

//                System.out.println(agent.getLocalName() + " waiting for a BUYING_ENERGY response from CENTRAL "+ centralId.getLocalName());

        ACLMessage response = agent.receive(centralTemplate);

        if (response != null) {

            double price = Double.valueOf(response.getContent());
            //update level of "money
            agent.addCredits(price);
            System.out.println(agent.getLocalName() + " sold "+ energy+" for " + price + " credits to the central --- credits available: " + agent.getCredits());
            step = EXIT;

            agent.setCentralOperationType("Sell");
            agent.setQtyCentral(energy);
            agent.setPriceCentral(price);

            //Si ens respon amb un accept, es que tenia prou energia i ens l'ha donada
//                    if (response.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
//                        String price = response.getContent();
//                        System.out.println(agent.getLocalName() + " bought " + energy + " units from the central with price = " + price);
//                        step=EXIT;
//                    }
//                    else if (response.getPerformative() == ACLMessage.REFUSE){
//                        System.out.println(agent.getLocalName() + " negative response received from central");
//                        step=EXIT;
//                    }
        } else {
            block();
        }
    }
}
