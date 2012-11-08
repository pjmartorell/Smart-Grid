/*
 *acts as RESPONDER in the contract net protocol implemented to find a seller
.
 */
package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Locale;
import java.util.Scanner;
import org.upc.edu.Agents.House;

/**
 *
 * @author sergio.jurado
 */
public class HouseSellerBehaviour extends CyclicBehaviour {

    House agent;
    AID neighbourHoodId;
    boolean transactionDone;
    int step;
    ACLMessage CFPmessage, resolution;
    double desiredPrice, reservedPrice; //local copy of the desiredPrice in order
    // to not override the desiredPrice parameter of the house (to be used in the next request)
    double increment, energyExcess;
    //for Propose message
    double energyRequested;
    AID sender;

    public HouseSellerBehaviour(AID neighbourHoodId, House a, double energyExcess) {

        super(a);
        this.agent = a;
        this.neighbourHoodId = neighbourHoodId;
        step = 0;
        desiredPrice = agent.getDesiredPrice();
        reservedPrice = agent.getReservedPrice();
        this.energyExcess = energyExcess;
        increment = 0.2 * (desiredPrice - reservedPrice);
    }

    public void action() {

        switch (step) {

            case 0:
                initVars();
                waitBuyProposal();
                break;

            case 1:
                sendResponse();
                break;

            case 2:
                waitResolution();
                break;
            case 3:
                waitNegotiation(resolution);
                break;
            case 4:
                //wait response
                //step 4 o last step
                break;

        }
    }

    private void initVars() {

        CFPmessage = null;
        resolution = null;
        // no cal aquesta variable, quan energyExcess = 0 vol dir que em venut
        transactionDone = false;
    }

    private void waitBuyProposal() {

        //block until a message of type CFP from neighbourhood is received
        MessageTemplate template1 = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        MessageTemplate template2 = MessageTemplate.MatchSender(neighbourHoodId);
        MessageTemplate template = MessageTemplate.and(template1, template2);
        //System.out.println(agent.getLocalName() + " waiting for a message CFP from neighbourhood");

        CFPmessage = agent.receive(template);
        if (CFPmessage != null) {
            step = 1;
        } else {
            block();
        }

    }

    void sendResponse() {
        Scanner c = new Scanner(CFPmessage.getContent());
        c.useLocale(Locale.ENGLISH);
        double energyRequested, priceRequested;
        energyRequested = c.nextDouble();
        priceRequested = c.nextDouble();

        System.out.println(agent.getLocalName() + " message CFP received with energy(" + energyRequested + ") and price(" + priceRequested + ")");
        if (energyExcess >= energyRequested && priceRequested >= reservedPrice) {

            sendProposal(CFPmessage);
            step = 2;

        } else {
            System.out.println(agent.getLocalName() + " sends REFUSE because my reserved price or not enough energy to negotiate");
            sendRefuse(CFPmessage);
            step = 0; //wait for another cfp
        }
    }

    protected void sendProposal(ACLMessage cfp) {

        double initialPrice = agent.getDesiredPrice();
        System.out.println(agent.getLocalName() + " sends propose with intial Price : " + initialPrice);
        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);
        propose.setContent(String.valueOf(initialPrice));
        agent.send(propose);
    }

    protected void sendRefuse(ACLMessage cfp) {

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.REFUSE);
        agent.send(propose);
    }

    protected void waitResolution() {

        MessageTemplate template1 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        MessageTemplate template2 = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        MessageTemplate template = MessageTemplate.or(template1, template2);
        //System.out.println(agent.getLocalName() + " waiting for RESOLUTION OF CFP");
        resolution = agent.receive(template);

        if (resolution != null) {
            if (resolution.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                System.out.println(agent.getLocalName() + " received resolution of CFP and has been ACCEPTED");
                step = 3; // wait for negotiation start message
            } else {
                System.out.println(agent.getLocalName() + " received resolution of CFP and has been REJECTED");
                step = 0;//start again
            }
        } else {
            block();
        }

    }

    protected void waitNegotiation(ACLMessage resolution) {

        //wait for buyer to start negotiation
        double priceRequested;

        MessageTemplate template = MessageTemplate.MatchConversationId("Negotiation");
        //System.out.println(agent.getLocalName() + " waiting for the buyer negotiation");
        ACLMessage message = agent.receive(template);
        if (message != null) {
            Scanner c = new Scanner(message.getContent());
            c.useLocale(Locale.ENGLISH);
            energyRequested = c.nextDouble();
            priceRequested = c.nextDouble();

            if (message.getPerformative() == ACLMessage.REQUEST) {

                System.out.println(agent.getLocalName() + " RECEIVED a request of energy("
                        + energyRequested + ") from " + message.getSender().getLocalName() + " at a price(" + priceRequested + ")");
                sender = message.getSender();
                negotiate(priceRequested);
            } else if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
             //   agent.setProductionLevel(agent.getProductionLevel() - energyRequested);
                agent.addCredits(desiredPrice * energyRequested);
                setStatistics(desiredPrice * energyRequested);
                System.out.println(agent.getLocalName() + " i " + message.getSender().getLocalName()
                        + " hem tancat acord");
                sender = message.getSender();
                energyExcess = 0;
                //Tornem a començar el procés pero només per contestar els CFPs amb refuse
                step = 0;
                agent.setUseBattery(false);
            }

        } else {
            block();
        }
    }

    protected void negotiate(double priceRequested) {

        //Negociar y actualizar var startNegotiation

        //System.out.println((desiredPrice - increment) + " < " + priceRequested + " ?");
        if (desiredPrice - increment > priceRequested) {
            // Cedeixo i aplico un decrement al meu desiredPrice
            desiredPrice = desiredPrice - increment;
            System.out.println(agent.getLocalName() + " he cedit a un preu de " + desiredPrice);
            sendPropose(); //Send propose

        } else {
            // Hem arribat a un acord
            closeNegotiation(priceRequested);
        }

    }

    private void sendPropose() {

        ACLMessage mensaje = new ACLMessage(ACLMessage.PROPOSE);
        mensaje.setConversationId("Negotiation");
        mensaje.setContent(Double.toString(energyRequested) + " " + Double.toString(desiredPrice));
        mensaje.addReceiver(sender);
        agent.send(mensaje);
    }

    private void closeNegotiation(double finalPrice) {


        System.out.println(agent.getLocalName() + " he finalitzat l'acord amb " + sender.getLocalName()
                + " un preu de " + desiredPrice + " per " + energyRequested + " unitats d'energia");
        // Hem arribat a un acord
        ACLMessage mensaje = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        mensaje.setConversationId("Negotiation");
        mensaje.setContent(Double.toString(energyRequested) + " " + Double.toString(finalPrice));
        mensaje.addReceiver(sender);
        agent.send(mensaje);
     //   agent.setProductionLevel(agent.getProductionLevel() - energyRequested)
        agent.addCredits(finalPrice * energyRequested);
        setStatistics(finalPrice * energyRequested);
        //Actualitzem l'exces d'energia
        energyExcess = 0;
        //Tornem a començar el procés pero només per contestar els CFPs amb refuse
        step = 0;
        agent.setUseBattery(false);
    }

    private void setStatistics(double totalPrice){

        agent.setTransactionType("Sell");
        agent.setAdversary(sender.getLocalName());
        agent.setQtyTransaction(energyRequested);
        agent.setPriceTransaction(totalPrice);


    }
}
