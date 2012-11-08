package org.upc.edu.Agents;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import jade.lang.acl.UnreadableException;
import org.upc.edu.Behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 
 */

public class House extends MyAgent {

    private static final int BATTERY_MAXIMUM = 100;
    AID neighbourHoodId;
    double productionLevel;
    double consumLevel;
    double storageLevel;
    double reservedPrice, desiredPrice;
    HouseBehaviour houseBehaviour;
    int coordX;
    int coordY;
    private AID centralId;
    // Nomes es posa a true quan hem acabat la request de compra i no s'ha trobat
    // ningu, i anem a la bateria o central
    private boolean useBattery;
    ArrayList neighbourhoodProximity; //aids of neighbourhood sorted by proximity with mine

    //Statistics
    String transactionType;   
    String adversary;
    double qtyTransaction;
    double priceTransaction;

    double transactionDistance;
    double transactionQuantity;
    double price;
    
    String batteryOperationType;
    double qtyBattery;
    double priceBattery;

    String centralOperationType;
    double qtyCentral;
    double priceCentral;
    

    //Statistical variables
    @Override
    protected void setup() {

        super.setup();
        String neighbourHoodName;

        // Get ARGUMENTS (productionLevel, consumLevel, storageLevel,biomassLevel)
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            productionLevel = ((Number) args[0]).intValue();
            consumLevel = ((Number) args[1]).intValue();
            storageLevel = ((Number) args[2]).intValue();
            reservedPrice = ((Number) args[3]).intValue();
            neighbourHoodName = (String) args[4];
            coordX = ((Number) args[5]).intValue();
            coordY = ((Number) args[6]).intValue();
            centralId = new AID((String)args[7], AID.ISGUID);
            credits = ((Number) args[8]).intValue();
            
            System.out.println(this.getLocalName() + " created with Production("
                    + productionLevel + "); Consumption(" + consumLevel + "); Storage("
                    + storageLevel + "); Reserved-Price("
                    + reservedPrice + ") | Belongs to Neighbourhood: " + neighbourHoodName + " with coords["
                    + coordX + ", " + coordY + "] and has "+ credits +" " );

            neighbourHoodId = getAID(neighbourHoodName);
            useBattery = true;
            
            //Statistics
            transactionType = "";
            adversary = "";
            qtyTransaction = 0;
            priceTransaction = 0;
            batteryOperationType = "";
            qtyBattery = 0;
            priceBattery = 0;
            centralOperationType = "";
            qtyCentral = 0;
            priceCentral = 0;

        } else {
            // Make the agent terminate immediately
            System.out.println(this.getLocalName() + "NOT CREATED, SPECIFY ARGS!");
            doDelete();
        }

        System.out.println(this.getLocalName() + ": enviando mensaje de bienvenida");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setConversationId("house-welcome");
        msg.addReceiver(neighbourHoodId);
        send(msg);

        MessageTemplate template1 = MessageTemplate.MatchSender(neighbourHoodId);
        ACLMessage message = this.blockingReceive(template1);

        try {
            neighbourhoodProximity = (ArrayList) message.getContentObject();
//            System.out.println(this.getLocalName() + ": recibido mensaje de "
//                    + message.getSender().getLocalName() + " con contenido " + neighbourhoodProximity);

        } catch (UnreadableException ex) {
            Logger.getLogger(House.class.getName()).log(Level.SEVERE, null, ex);
        }

        houseBehaviour = new HouseBehaviour(this.neighbourHoodId, this, neighbourhoodProximity);
        this.addBehaviour(houseBehaviour);
    }

    public AID getNeighbourhoodAID() {
        return neighbourHoodId;
    }



    public void setConsumLevel(double consumLevel) {
        this.consumLevel = consumLevel;
    }

    public void setNeighbourHoodId(AID neighbourHoodId) {
        this.neighbourHoodId = neighbourHoodId;
    }

    public void setProductionLevel(double productionLevel) {
        this.productionLevel = productionLevel;
    }

    public void setStorageLevel(double storageLevel) {
        this.storageLevel = storageLevel;
    }

    public double getConsumLevel() {
        return consumLevel;
    }

    public AID getNeighbourHoodId() {
        return neighbourHoodId;
    }

    public double getProductionLevel() {
        return productionLevel;
    }

    public double getStorageLevel() {
        return storageLevel;
    }

    public AID getDf() {
        return df;
    }

    @Override
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

    public double getExcess() {
        return productionLevel - consumLevel;
    }

    public double getReservedPrice() {
        return reservedPrice;
    }

    public double getDesiredPrice() {
        return desiredPrice;
    }

    public void setDesiredPrice(double value) {
        desiredPrice = value;
    }

    public void setReservedPrice(double value) {
        reservedPrice = value;
    }

    public HouseBehaviour getHouseBehaviour() {
        return houseBehaviour;
    }

    public int CoordX() {
        return coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public void setBuyerTemplate() {
        //aixo ho fa un comprador o una casa sense rol (perque acaba de vendre o comprar)
        //processo missatges update i rebutjo (not understood) missatges cfp
        houseBehaviour.setBuyerTemplate();
    }

    public void updateLevels() {
        //Actualitzem nivells de consum/produccio
        Random rnd = new Random();
        int diff1 = rnd.nextInt(41);
        int diff2 = rnd.nextInt(41);
        diff1 -= 20;
        diff2 -= 20;
        //System.out.println("Randoms generated: " + diff1 + " | " + diff2);
        this.productionLevel = Math.abs(productionLevel + diff1);
        this.consumLevel = Math.abs(consumLevel + diff2);
        System.out.println("Balance :" + (this.productionLevel - this.consumLevel));

        transactionType = "";
        adversary = "";
        qtyTransaction = 0;
        priceTransaction = 0;
        batteryOperationType = "";
        qtyBattery = 0;
        priceBattery = 0;
        centralOperationType = "";
        qtyCentral = 0;
        priceCentral = 0;

        this.useBattery = true;
        System.out.println(this.getLocalName()+": / Battery Level=" +this.storageLevel );

    }

    public boolean getUseBattery() {
        return useBattery;
    }

    public void setUseBattery(boolean useBattery) {
        this.useBattery = useBattery;
    }

    /*Funcio que emmagatzema energia a la bateria. Torna 0 si ha anat be, o un double amb la quantitat que no s'ha pogut emmagatzemar*/
    public double addToBattery(double energy) {

        if (this.storageLevel + energy <= BATTERY_MAXIMUM) {
            this.storageLevel = this.storageLevel + energy;
            energy = 0;
        } else {
            energy = energy - (BATTERY_MAXIMUM - this.storageLevel);
            this.storageLevel = BATTERY_MAXIMUM;
        }
        return energy;
    }

    /*Funcio que agafa energia de la bateria. Torna 0 si ha anat be, o un double amb la quantitat que falta demanar a la central*/
    public double useFromBattery(double energy) {

        if (this.storageLevel >= energy) {
            this.storageLevel = this.storageLevel - energy;
            energy = 0;
        } else {
            energy = energy - this.storageLevel;
            this.storageLevel = 0;
        }
        return energy;

    }

    public void useBattery() {

        //Utilitzem la bateria local

        double energy = this.useFromBattery(Math.abs(this.getExcess()));
        System.out.println(this.getLocalName()+"gets " +(this.getExcess()-energy)+" from local battery");

        //Si encara ens fa falta energia, demanarem al neighbourhood i/o a la central
        if (energy > 0) {
            System.out.println(this.getLocalName()+" still needs "+ energy + " units -- resquesting to get from neighbourhood battery");
            this.addBehaviour(new HouseBatteryGetBehaivour(neighbourHoodId, this, energy, centralId));
        }
    }

    public void storeBattery() {
        //Emmagatzemem a la bateria local
        double energy = this.addToBattery(Math.abs(this.getExcess()));
        System.out.println(this.getLocalName()+"stores " +(this.getExcess()-energy)+" at local battery");

        //Si encara ens fa falta energia, demanarem al neighbourhood i/o a la central
        if (energy > 0) {
            System.out.println(this.getLocalName()+" still needs "+ energy + " units -- resquesting to store at neighbourhood battery");
            this.addBehaviour(new HouseBatteryStoreBehaivour(neighbourHoodId, this, energy, centralId));
        }
    }

    public String getAdversary() {
        return adversary;
    }

    public void setAdversary(String adversary) {
        this.adversary = adversary;
    }

    public String getCentralOperationType() {
        return centralOperationType;
    }

    public void setCentralOperationType(String centralOperationType) {
        this.centralOperationType = centralOperationType;
    }

    public double getPriceBattery() {
        return priceBattery;
    }

    public void setPriceBattery(double priceBattery) {
        this.priceBattery = priceBattery;
    }

    public double getPriceTransaction() {
        return priceTransaction;
    }

    public void setPriceTransaction(double priceTransaction) {
        this.priceTransaction = priceTransaction;
    }

    public double getQtyBattery() {
        return qtyBattery;
    }

    public void setQtyBattery(double qtyBattery) {
        this.qtyBattery = qtyBattery;
    }

    public double getQtyCentral() {
        return qtyCentral;
    }

    public void setQtyCentral(double qtyCentral) {
        this.qtyCentral = qtyCentral;
    }

    public double getQtyTransaction() {
        return qtyTransaction;
    }

    public void setQtyTransaction(double qtyTransaction) {
        this.qtyTransaction = qtyTransaction;
    }
    
     public String getBatteryOperationType() {
        return batteryOperationType;
    }

    public void setBatteryOperationType(String batteryOperationType) {
        this.batteryOperationType = batteryOperationType;
    }

    public double getPriceCentral() {
        return priceCentral;
    }

    public void setPriceCentral(double priceCentral) {
        this.priceCentral = priceCentral;
    }

    public double getTransactionDistance() {
        return transactionDistance;
    }

    public void setTransactionDistance(double transactionDistance) {
        this.transactionDistance = transactionDistance;
    }

    public double getTransactionQuantity() {
        return transactionQuantity;
    }

    public void setTransactionQuantity(double transactionQuantity) {
        this.transactionQuantity = transactionQuantity;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
