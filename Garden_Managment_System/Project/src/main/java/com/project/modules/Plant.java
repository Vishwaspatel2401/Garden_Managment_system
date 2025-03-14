package com.project.modules;

import com.project.factory.PlantType;
import com.project.logger.Logger;

import java.util.List;

/**
 * Abstract Plant class representing a generic plant in the garden simulation.
 */
public abstract class Plant {
    private String name;
    private PlantType plantType;
    private int waterRequirement;
    private int currentWaterLevel;
    private int health;
    private List<String> pestVulnerabilities;
    private int temperatureToleranceLow;
    private int temperatureToleranceHigh;
    private boolean isAlive;

    /**
     * Constructor for the Plant class.
     *
     * @param plantType              The type of the plant.
     * @param name                   The name of the plant.
     * @param waterRequirement       The water requirement of the plant.
     * @param pestVulnerabilities    List of pests that the plant is vulnerable to.
     * @param tempLow                The lower bound of temperature tolerance.
     * @param tempHigh               The upper bound of temperature tolerance.
     */
    public Plant(PlantType plantType, String name, int waterRequirement, List<String> pestVulnerabilities, int tempLow, int tempHigh) {
        this.plantType = plantType;
        this.name = name;
        this.waterRequirement = waterRequirement;
        this.currentWaterLevel = 100; // Start with full water level
        this.health = 100; // Start with full health
        this.pestVulnerabilities = pestVulnerabilities;
        this.temperatureToleranceLow = tempLow;
        this.temperatureToleranceHigh = tempHigh;
        this.isAlive = true;
    }

    // Getters for necessary fields
    public String getName() {
        return name;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public int getCurrentWaterLevel() {
        return currentWaterLevel;
    }

    public int getWaterRequirement() {
        return waterRequirement;
    }

    public List<String> getPestVulnerabilities() {
        return pestVulnerabilities;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getHealth() {
        return health;
    }

    // Water management methods
    public void decreaseWaterLevel(int decreaseBy) {
        currentWaterLevel = Math.max(currentWaterLevel - decreaseBy, 0);
        if (currentWaterLevel == 0) {
            setHealth(0);
            Logger.log(Logger.LogLevel.WARNING, name + " has no water left and has died.");
        }
    }

    public void water(int amount) {
        if (!isAlive) return;

        currentWaterLevel = Math.min(currentWaterLevel + amount, 100); // Cap at 100%
        Logger.log(Logger.LogLevel.INFO, name + " watered with " + amount + " units.");

        // Optional: Overwatering logic
        if (currentWaterLevel > waterRequirement * 2) {
            decreaseHealth(10); // Decrease health due to overwatering
            Logger.log(Logger.LogLevel.WARNING, name + " has been overwatered and its health decreased.");
        }
    }

    // Health management methods
    public void decreaseHealth(int amount) {
        if (!isAlive) return;

        health = Math.max(health - amount, 0);
        Logger.log(Logger.LogLevel.INFO, name + "'s health decreased by " + amount + ". Current health: " + health + "%");

        if (health == 0) {
            isAlive = false;
            Logger.log(Logger.LogLevel.WARNING, name + " has died.");
        }
    }

    public void increaseHealth(int amount) {
        if (!isAlive) return;

        health = Math.min(health + amount, 100);
        Logger.log(Logger.LogLevel.INFO, name + "'s health increased by " + amount + ". Current health: " + health + "%");
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, 100));
        if (this.health == 0) {
            isAlive = false;
            Logger.log(Logger.LogLevel.WARNING, name + " has died.");
        }
    }

    // Pest management methods
    public void pestAttack(String pest) {
        if (!isAlive) return;

        Logger.log(Logger.LogLevel.INFO, "Simulating pest attack on " + name + " by " + pest + ".");
        if (pestVulnerabilities.contains(pest)) {
            decreaseHealth(20); // Decrease health by a fixed amount or calculate dynamically
            Logger.log(Logger.LogLevel.WARNING, name + " was attacked by " + pest + " and health decreased.");
        }
    }

    public void setPestVulnerabilities(List<String> pestVulnerabilities) {
        this.pestVulnerabilities = pestVulnerabilities;
    }

    // Temperature management method
    public void adjustTemperature(int temperature) {
        if (!isAlive) return;

        Logger.log(Logger.LogLevel.INFO, "Adjusting temperature for " + name + " to " + temperature + " degrees.");
        if (temperature < temperatureToleranceLow || temperature > temperatureToleranceHigh) {
            decreaseHealth(15); // Decrease health due to unsuitable temperature
            Logger.log(Logger.LogLevel.WARNING, name + " could not tolerate the temperature and health decreased.");
        }
    }

    // Abstract methods to be implemented by subclasses
    public abstract void grow();

    public abstract void dailyCheck();

    // Miscellaneous methods
    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }

    // Default implementation for special care instructions
    public void displaySpecialCareInstructions() {
        Logger.log(Logger.LogLevel.INFO, name + " requires general care: Water as needed and maintain a suitable temperature.");
    }
}
