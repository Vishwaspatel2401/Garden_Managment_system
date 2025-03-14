package com.project.modules;

import com.project.factory.PlantType;
import com.project.logger.Logger;
import java.util.List;

public class Tree extends Plant {
    private int height; // in centimeters
    private int growthRate; // centimeters per day

    public Tree(PlantType plant, String name, int waterRequirement, List<String> pestVulnerabilities, int tempLow, int tempHigh, int initialHeight, int growthRate) {
        super(plant, name, waterRequirement, pestVulnerabilities, tempLow, tempHigh);
        this.height = initialHeight;
        this.growthRate = growthRate;
    }

    // Getter and Setter for height
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height >= 0) {
            this.height = height;
            Logger.log(Logger.LogLevel.INFO, getName() + " height set to " + height + " cm.");
        } else {
            Logger.log(Logger.LogLevel.WARNING, "Invalid height value for " + getName() + ".");
        }
    }

    // Getter and Setter for growthRate
    public int getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(int growthRate) {
        if (growthRate >= 0) {
            this.growthRate = growthRate;
            Logger.log(Logger.LogLevel.INFO, getName() + " growth rate set to " + growthRate + " cm/day.");
        } else {
            Logger.log(Logger.LogLevel.WARNING, "Invalid growth rate for " + getName() + ".");
        }
    }

    @Override
    public void grow() {
        if (!isAlive()) return;

        height += growthRate;
        Logger.log(Logger.LogLevel.INFO, getName() + " has grown by " + growthRate + " cm. Current height: " + height + " cm.");
    }

    @Override
    public void dailyCheck() {
        if (!isAlive()) return;

        Logger.log(Logger.LogLevel.INFO, "Performing daily check for tree: " + getName());

        // Example checks specific to trees
        if (getCurrentWaterLevel() < getWaterRequirement()) {
            Logger.log(Logger.LogLevel.WARNING, getName() + " is under-watered.");
        }

        // Additional tree-specific daily checks can be added here
    }

    @Override
    public void displaySpecialCareInstructions() {
        System.out.println(getName() + " requires specific care: Regular watering, pruning, and monitoring for pests.");
    }
}
