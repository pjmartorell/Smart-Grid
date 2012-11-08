package org.upc.edu.Agents;

import jade.core.AID;
import java.util.Vector;
import org.upc.edu.Behaviours.NeighbourhoodBehaviour;

/**
 *
 * @author 
 */

public class Neighbourhood extends MyAgent {

    private final int BATTERY_MAXIMUM = 300;
    double storageLevel;
    Environment environment;
    int CPFduration;
    // The list of known houses of the neighbourhood
    private Vector<AID> housesAID;
    int numHouses;

    int reqBuy;
    int reqGetBattery;
    int reqStoreBattery;
    int reqSellCentral;
    int reqBuyCentral;

    double battery_pricePerKWH;

    @Override
    protected void setup() {
        super.setup();

        CPFduration = 5000;
        housesAID = new Vector();

        // Get ARGUMENTS (productionLevel, consumLevel, storageLevel)
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            environment = (Environment) args[0];
            numHouses = (Integer) args[1];
            credits = ((Number) args[2]).intValue();

            battery_pricePerKWH = 20.0;
          //  battery_pricePerKWH = ((Number)args[1]).doubleValue();
            System.out.println(this.getLocalName() + " created with " + numHouses + " houses and has "+ credits +" " );

        } else {
            // Make the agent terminate immediately
            System.out.println(this.getLocalName() + "NOT CREATED, SPECIFY ARGS!!!!!!!!!!!!!!!!");
            doDelete();
        }

         reqBuy = 0;
        reqGetBattery = 0;
        reqStoreBattery = 0;
        reqSellCentral = 0;
        reqBuyCentral = 0;
        this.addBehaviour(new NeighbourhoodBehaviour(this, numHouses));

        /**
         * Neighbourhood represents a buyer.
         * Acts as Initiator in the contract net protocol implemented to find a seller
         * 1) Accepts request to buy energy,
         * 2) starts contract net protocol to find a seller ()
         * 3) waits for NeighbourhoodSellBehaviour to process responses from sellers
         * 4) if sellers, selects the best one and sends his address to the buyer
         * if no sellers, informs buyer
         **/
    }

    public int getNumHouses() {

        return numHouses;
    }

        public double getPricePerKWH() {
            return battery_pricePerKWH;
    }

    public AID getEnvironmentAID() {

        return environment.getAID();
    }

    /* Funcio que emmagatzema energia a la bateria. Torna 0 si ha anat be, o un double amb la quantitat que no s'ha pogut emmagatzemar*/
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

    /* Funcio que agafa energia de la bateria. Torna 0 si ha anat be, o un double amb la quantitat que falta demanar a la central*/
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

    public Vector<AID> getHousesAID() {

        return this.housesAID;
    }

    public void setHousesAID(Vector<AID> housesAID) {

        this.housesAID = housesAID;
    }
    
    public int getReqBuy() {
        return reqBuy;
    }

    public void setReqBuy(int reqBuy) {
        this.reqBuy = reqBuy;
    }

    public int getReqGetBattery() {
        return reqGetBattery;
    }

    public void setReqGetBattery(int reqGetBattery) {
        this.reqGetBattery = reqGetBattery;
    }

    public int getReqStoreBattery() {
        return reqStoreBattery;
    }

    public void setReqStoreBattery(int reqStoreBattery) {
        this.reqStoreBattery = reqStoreBattery;
    }

    public double getStorageLevel() {
        return storageLevel;
    }

    public void setStorageLevel(double storageLevel) {
        this.storageLevel = storageLevel;
    }

    public int getReqBuyCentral() {
        return reqBuyCentral;
    }

    public void setReqBuyCentral(int reqBuyCentral) {
        this.reqBuyCentral = reqBuyCentral;
    }

    public int getReqSellCentral() {
        return reqSellCentral;
    }

    public void setReqSellCentral(int reqSellCentral) {
        this.reqSellCentral = reqSellCentral;
    }

}
