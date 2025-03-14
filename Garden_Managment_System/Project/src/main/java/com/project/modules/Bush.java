package com.project.modules;

import com.project.factory.PlantType;
import com.project.logger.Logger;
import java.util.List;

public class Bush extends Plant {
    private int density; // Represents how dense the bush is (arbitrary units)
    private int trimmingFrequency; // Days between required trimmings

    public Bush(PlantType plant, String name, int waterRequirement, List<String> pestVulnerabilities, int tempLow, int tempHigh, int initialDensity, int trimmingFrequency) {
        super(plant, name, waterRequirement, pestVulnerabilities, tempLow, tempHigh);
        this.density = initialDensity;
        this.trimmingFrequency = trimmingFrequency;
    }

    // Getter and Setter for density
    public int getDensity() {
        return density;
    }


    public void setDensity(int density) {
        if (density >= 0) {
            this.density = density;
            Logger.log(Logger.LogLevel.INFO, getName() + " density set to " + density + ".");
        } else {
            Logger.log(Logger.LogLevel.WARNING, "Invalid density value for " + getName() + ".");
        }
    }

    // Getter and Setter for trimmingFrequency
    public int getTrimmingFrequency() {
        return trimmingFrequency;
    }

    public void setTrimmingFrequency(int trimmingFrequency) {
        if (trimmingFrequency > 0) {
            this.trimmingFrequency = trimmingFrequency;
            Logger.log(Logger.LogLevel.INFO, getName() + " trimming frequency set to every " + trimmingFrequency + " days.");
        } else {
            Logger.log(Logger.LogLevel.WARNING, "Invalid trimming frequency for " + getName() + ".");
        }
    }

    @Override
    public void grow() {
        if (!isAlive()) return;

        // Example growth behavior: Increase density over time
        density += 1; // Increment density
        Logger.log(Logger.LogLevel.INFO, getName() + " has grown denser. Current density: " + density + ".");
    }

    @Override
    public void dailyCheck() {
        if (!isAlive()) return;

        Logger.log(Logger.LogLevel.INFO, "Performing daily check for bush: " + getName());

        // Example checks specific to bushes
        if (getCurrentWaterLevel() < getWaterRequirement()) {
            Logger.log(Logger.LogLevel.WARNING, getName() + " is under-watered.");
        }

        // Additional bush-specific daily checks can be added here
    }

    @Override
    public void displaySpecialCareInstructions() {
        System.out.println(getName() + " requires specific care: Regular trimming, adequate watering, and monitoring for pests.");
    }
}
