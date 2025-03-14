package com.project.modules;

import com.project.factory.PlantType;
import com.project.logger.Logger;

import java.util.List;

/**
 * Represents a Sunflower plant.
 */
public class Sunflower extends Bush {
    private String heliotropic;
    private static final String emoji = "🌻";

    public static String getEmoji() {
        return emoji;
    }

    public Sunflower(String name, int waterRequirement, List<String> pestVulnerabilities, int tempLow, int tempHigh, int initialDensity, int trimmingFrequency, String heliotropic) {
        super(PlantType.SUNFLOWER, name, waterRequirement, pestVulnerabilities, tempLow, tempHigh, initialDensity, trimmingFrequency);
        this.heliotropic = heliotropic;
    }

    // Getter and Setter for heliotropic
    public String getHeliotropic() {
        return heliotropic;
    }

    public void setHeliotropic(String heliotropic) {
        this.heliotropic = heliotropic;
        Logger.log(Logger.LogLevel.INFO, getName() + " heliotropic property set to " + heliotropic + ".");
    }

    @Override
    public void grow() {
        super.grow();
        Logger.log(Logger.LogLevel.INFO, getName() + " is following the sun and growing taller. 🌻");
    }

    @Override
    public void dailyCheck() {
        super.dailyCheck();
        Logger.log(Logger.LogLevel.INFO, "Checking sun tracking for " + getName() + ".");
    }

    @Override
    public void displaySpecialCareInstructions() {
        System.out.println(getName() + " requires specific care: Regular watering, full sunlight exposure, and protection from strong winds.");
    }
} 