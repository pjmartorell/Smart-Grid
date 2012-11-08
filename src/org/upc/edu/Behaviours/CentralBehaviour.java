/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.upc.edu.Agents.Central;

/**
 *
 * @author sergio.jurado
 */
public class CentralBehaviour extends CyclicBehaviour {

    Central agent;
    MessageTemplate template;
    double pricePerKWH;

    public CentralBehaviour(Central agent,  double pricePerKWH) {

        super(agent);

        this.agent = agent;
         this.pricePerKWH = pricePerKWH;
        MessageTemplate ReqTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate CFPtemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        this.template = MessageTemplate.or(ReqTemplate,CFPtemplate);
    }

    public void action() {
        

        //block until a message of type REQUEST is received
        ACLMessage message = agent.receive(template);
        
        if (message != null) {

            int performativeId =  message.getPerformative();
            String performative = ACLMessage.getPerformative(performativeId);

            if (performative.equals("REQUEST")) {

                //BUY AND SEll are treated equal because central does not have neither level of energy available (it can always sell) nor storage free level (it can always buy)
                String transactionType = message.getConversationId();
                double energy = Double.parseDouble(message.getContent());
                System.out.println(agent.getLocalName() + " received a " +transactionType +" of "+ energy+ " from " + message.getSender().getLocalName());
                //send price that house has to pay or that will receive
                sendPrice(message.getSender(), pricePerKWH*energy);
            }

            //le puede llegar un CFP si el cfp se hace realmente a todos los agentes (ahora solo a los q empiezan por H)
        }
    }

    private void processRequest() {

        
    }


    private void sendPrice(AID houseAID, double price) {

        //Reply BUYER with INFORM
        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(houseAID);
        reply.setContent(String.valueOf(price));
        agent.send(reply);
    }


}
