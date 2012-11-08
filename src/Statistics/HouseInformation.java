/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Statistics;

import jade.core.AID;
import java.io.Serializable;

/**
 *
 * @author ferran.obiol
 */
public class HouseInformation implements Serializable{

    String name;
    String neighbourhoodName;

    double credits;
    double consumptionLevel;
    double productionLevel;
    double storageLevel;

    
    String transactionType;   
    String adversary;
    double qtyTransaction;
    double priceTransaction;
    
    String batteryOperationType;
    double qtyBattery;
    double priceBattery;
    
    String centralOperationType;
    double qtyCentral;
    double priceCentral;

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

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
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

    public double getConsumptionLevel() {
        return consumptionLevel;
    }

    public void setConsumptionLevel(double consumptionLevel) {
        this.consumptionLevel = consumptionLevel;
    }

    public String getNeighbourhoodName() {
        return neighbourhoodName;
    }

    public void setNeighbourhoodName(String neighbourhoodName) {
        this.neighbourhoodName = neighbourhoodName;
    }



    public double getPriceCentral() {
        return priceCentral;
    }

    public void setPriceCentral(double priceCentral) {
        this.priceCentral = priceCentral;
    }

    public double getProductionLevel() {
        return productionLevel;
    }

    public void setProductionLevel(double productionLevel) {
        this.productionLevel = productionLevel;
    }

    public double getStorageLevel() {
        return storageLevel;
    }

    public void setStorageLevel(double storageLevel) {
        this.storageLevel = storageLevel;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HouseInformation(String name, String neighbourhoodName, double credits, double consumptionLevel, double productionLevel, double storageLevel, String transactionType, String adversary, double qtyTransaction, double priceTransaction, String batteryOperationType, double qtyBattery, double priceBattery, String centralOperationType, double qtyCentral, double priceCentral) {
        this.name = name;
        this.neighbourhoodName = neighbourhoodName;
        this.credits = credits;
        this.consumptionLevel = consumptionLevel;
        this.productionLevel = productionLevel;
        this.storageLevel = storageLevel;
        this.transactionType = transactionType;
        this.adversary = adversary;
        this.qtyTransaction = qtyTransaction;
        this.priceTransaction = priceTransaction;
        this.batteryOperationType = batteryOperationType;
        this.qtyBattery = qtyBattery;
        this.priceBattery = priceBattery;
        this.centralOperationType = centralOperationType;
        this.qtyCentral = qtyCentral;
        this.priceCentral = priceCentral;
    }

    
    
}
