/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.edu.Behaviours;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import org.upc.edu.Agents.House;

/**
 *
 * @author sergio.jurado
 */
public class HouseBuyBehaviour extends CyclicBehaviour {

    AID neighbourHoodId;
    House agent;
    int step;
    ACLMessage response;
    double desiredPrice, reservedPrice; //local copy of the desiredPrice in order
    //  to not override the desiredPrice parameter of the house (to be used in the next request)
    double energyRequested;
    double increment;
    ArrayList<AID> neighbourhoodProximity; //aids of neighbourhood sorted by proximity with mine
    int neighbourhoodIndex;
    AID seller;

    public HouseBuyBehaviour(AID neighbourHoodId, House agent, double energyRequested, ArrayList neighbourhoodProximity) {
        this.neighbourHoodId = neighbourHoodId;
        this.agent = agent;
        desiredPrice = agent.getDesiredPrice();
        reservedPrice = agent.getReservedPrice();
        increment = 0.2 * (reservedPrice - desiredPrice);
        this.energyRequested = energyRequested;
        step = 0;
        this.neighbourhoodProximity = neighbourhoodProximity;
        this.neighbourhoodIndex = 0;

    }

    public void action() {

        switch (step) {

            case 0:
                initVars();
                //Enviamos peticion de compra al Neighbourhood
                sendBuyRequest();
                step = 1;
                break;

            case 1:
                waitResolution();
                break;

            case 2:
                sendResponse();
                break;
            case 3:
                //Starting negotiation with seller
                sendPropose();
                break;

            //aquests 2 passos següents es repeteixen fins a acordar un preu, amb un cert deathline
            case 4:
                waitNegotiation();
                break;
            case 5:
                // Negocacio acceptada => Actualitzar valors
                break;
        }
    }

    void sendBuyRequest() {

        if (this.neighbourhoodIndex < this.neighbourhoodProximity.size()) {
            //content message format : <energyRequested priceRequested>
            String reqContent = Double.toString(energyRequested) + " " + Double.toString(desiredPrice);
            neighbourHoodId = this.neighbourhoodProximity.get(this.neighbourhoodIndex);
            ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
            mensaje.addReceiver(neighbourHoodId);
            mensaje.setContent(reqContent);
            mensaje.setConversationId("BUY_REQUEST");
            //System.out.println(agent.getLocalName() + " " + this.neighbourhoodProximity);
            //System.out.println(agent.getLocalName() + " " + this.neighbourHoodId);

            //Envia el mensaje al neighbourhood más cercano
            agent.send(mensaje);
            System.out.println(agent.getLocalName() + " send BUY REQUEST to " + neighbourHoodId.getLocalName());
            this.neighbourhoodIndex++;
        } else {
            step = 5;
        }
    }

    private void waitResolution() {

        MessageTemplate template1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate template2 = MessageTemplate.MatchSender(neighbourHoodId);
        MessageTemplate template = MessageTemplate.and(template1, template2);
        //System.out.println(agent.getLocalName() + " waiting for a message INFORM from neighbourhood");

        response = agent.receive(template);
        if (response != null) {
            step = 2;
        } else {
            block();
        }
    }

    private void sendResponse() {

        String content = response.getContent();

        if (content.equals("seller NOT FOUND")) {
            System.out.println(agent.getLocalName() + " received 'seller NOT found' from " + response.getSender().getLocalName());
            //vuelvo a solicitar compra con un aumento 2% (de mi margen) del dinero que ofrezco
            //NOOOOOOOOOOO (no se vuelve a solicitar compra)
            //step = 0; //try to find a seller again, start protocol again
//            desiredPrice = desiredPrice + increment;
//            if (desiredPrice > reservedPrice) {
//                   System.out.println(agent.getLocalName() + " no puedo continuar subiendo -- END OF buyer --------------------------------------------------------- ");
//                   step = 3;
//            }
//            else {
//                step = 0; //try to find a seller again, start protocol again
//            }
            step = 0;
        } else {
            //Starting negotiation with seller
            step = 3;
        }
    }

    protected void sendPropose() {
        //esperem si el seller accepta o fa una altra oferta millor
        String content = response.getContent();
        seller = new AID(content, AID.ISLOCALNAME);

        System.out.println(agent.getLocalName() + " negotiating with " + seller.getLocalName()
                + " for energy(" + energyRequested + ") at desired price(" + desiredPrice + ")");

        ACLMessage mensaje = new ACLMessage(ACLMessage.REQUEST);
        mensaje.setConversationId("Negotiation");
        mensaje.setContent(Double.toString(energyRequested) + " " + Double.toString(desiredPrice));
        mensaje.addReceiver(seller);
        agent.send(mensaje);
        step = 4;
    }

    private void initVars() {
        response = null;
    }

    protected void waitNegotiation() {
        //waiting for seller offer or acceptance of my desired price
        double energyOffered, priceOffered;

        MessageTemplate template = MessageTemplate.MatchConversationId("Negotiation");
        //System.out.println(agent.getLocalName() + " waiting for a seller offer or acceptance of my desired price");
        ACLMessage message = agent.receive(template);
        if (message != null) {
            if (message.getPerformative() == ACLMessage.PROPOSE) {
                Scanner c = new Scanner(message.getContent());
                c.useLocale(Locale.ENGLISH);
                energyOffered = c.nextDouble();
                priceOffered = c.nextDouble();
                System.out.println(agent.getLocalName() + " RECEIVED an offer of energy("
                        + energyOffered + ") from " + message.getSender().getLocalName() + " at a price(" + priceOffered + ")");
                // Negociem la oferta
                negotiate(energyOffered, priceOffered, message.getSender());
            } else if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                System.out.println(agent.getLocalName() + " i " + message.getSender().getLocalName()
                        + " hem tancat acord");
                agent.removeCredits(desiredPrice * energyRequested);
                setStatistics(desiredPrice * energyRequested);
                //   agent.setProductionLevel(agent.getProductionLevel() + energyRequested);
                step = 5;
                agent.setUseBattery(false);

            }
        } else {
            block();
        }
    }

    protected void negotiate(double energyOffered, double priceOffered, AID sender) {
        //Negociar y actualizar var startNegotiation
        // Cedeixo i aplico un increment al meu desiredPrice


        if (desiredPrice + increment <= priceOffered) {
            desiredPrice = desiredPrice + increment;
            System.out.println(agent.getLocalName() + " he cedit a un preu de " + desiredPrice);
            step = 3;
        } else {
            //Accepto comprar por el ultimo precio que me ha dicho (priceOffered)
            closeNegatiation(priceOffered, sender);

        }
    }

    private void closeNegatiation(double finalPrice, AID sender) {


        System.out.println(agent.getLocalName() + " he finalitzat l'acord amb " + sender.getLocalName()
                + " un preu de " + desiredPrice + " per " + energyRequested + " unitats d'energia");
        // Hem arribat a un acord
        ACLMessage mensaje = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        mensaje.setConversationId("Negotiation");
        mensaje.setContent(Double.toString(energyRequested) + " " + Double.toString(finalPrice));
        mensaje.addReceiver(sender);
        agent.send(mensaje);
        agent.removeCredits(finalPrice * energyRequested);
        setStatistics(finalPrice * energyRequested);
     //   agent.setProductionLevel(agent.getProductionLevel() + energyRequested);
        step = 5;
        agent.setUseBattery(false);
    }

    private void setStatistics(double totalPrice){

        agent.setTransactionType("Buy");
        agent.setAdversary(seller.getLocalName());
        agent.setQtyTransaction(energyRequested);
        agent.setPriceTransaction(totalPrice);


    }
}
