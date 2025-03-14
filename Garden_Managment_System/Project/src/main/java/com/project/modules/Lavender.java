package com.project.modules;

import com.project.factory.PlantType;
import com.project.logger.Logger;

import java.util.List;

/**
 * Represents a Lavender plant.
 */
public class Lavender extends Bush {
    private String fragrance;
    private static final String emoji = "💜";

    public static String getEmoji() {
        return emoji;
    }

    public Lavender(String name, int waterRequirement, List<String> pestVulnerabilities, int tempLow, int tempHigh, int initialDensity, int trimmingFrequency, String fragrance) {
        super(PlantType.LAVENDER, name, waterRequirement, pestVulnerabilities, tempLow, tempHigh, initialDensity, trimmingFrequency);
        this.fragrance = fragrance;
    }

    // Getter and Setter for fragrance
    public String getFragrance() {
        return fragrance;
    }

    public void setFragrance(String fragrance) {
        this.fragrance = fragrance;
        Logger.log(Logger.LogLevel.INFO, getName() + " fragrance set to " + fragrance + ".");
    }

    @Override
    public void grow() {
        super.grow();
        Logger.log(Logger.LogLevel.INFO, getName() + " is growing beautifully with a calming " + fragrance + " fragrance. 💜");
    }

    @Override
    public void dailyCheck() {
        super.dailyCheck();
        Logger.log(Logger.LogLevel.INFO, "Checking fragrance levels for " + getName() + ".");
    }

    @Override
    public void displaySpecialCareInstructions() {
        System.out.println(getName() + " requires specific care: Occasional watering, pruning, ensuring proper sunlight, and monitoring fragrance levels.");
    }
}
