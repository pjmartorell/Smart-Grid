/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.edu.Behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.upc.edu.Agents.Neighbourhood;

/**
 *
 * @author ferran.obiol
 */
public class NeighbourhoodBatteryStoreResponderBehaviour extends CyclicBehaviour {

    Neighbourhood agent;

    public NeighbourhoodBatteryStoreResponderBehaviour(Neighbourhood agent) {

        super(agent);
        this.agent=agent;

    }

    public void action() {
        
            MessageTemplate template = MessageTemplate.MatchConversationId("BATTERY_STORE_REQUEST");

            ACLMessage request = agent.receive(template);
            if (request != null) {

                 double price = 0;

                //Emmagatzemem l'energia demanada
               // double energy=Double.valueOf(request.getContent());
                double energyRequested = Double.valueOf(request.getContent());
                double energyNotAllocated = agent.addToBattery(energyRequested);
                        
                //Si no l'hem pogut emmagatzemar tota
                ACLMessage reply = request.createReply();
                if (energyNotAllocated > 0) {
                     //Send how many energy has not been allocated and how much has cost the allocated one.
                    double energyAllocated = energyRequested - energyNotAllocated;
                    //compute "money" I am going to pay
                    price = agent.getPricePerKWH() *energyAllocated;

                    reply.setContent(String.valueOf(energyNotAllocated)+  " " +String.valueOf(price));
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(request.getSender().getLocalName() +"got "+energyAllocated +" units of energy for price: "+price + "--- still needs "+energyNotAllocated+ "units");
                    agent.setReqSellCentral(agent.getReqSellCentral() + 1);

                    if(energyNotAllocated != energyRequested){
                        agent.setReqStoreBattery(agent.getReqStoreBattery() + 1);
                    }
                }

                else{
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    //compute "money" I am going to pay
                    price = agent.getPricePerKWH() * energyRequested;
                    reply.setContent(String.valueOf(price));
                    System.out.println(agent.getLocalName() + " got the requested energy, price: "+price);
                    agent.setReqStoreBattery(agent.getReqStoreBattery() + 1);
                }
                agent.send(reply);

               //remove credits used to get from the battery
                agent.removeCredits(price);
            } else {
                block();
            }
    }
}
