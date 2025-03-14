package com.project.modules;

import com.project.factory.PlantType;
import com.project.logger.Logger;

import java.util.List;

/**
 * Represents a Bamboo plant.
 */
public class Bamboo extends Tree {
    private int growthRate; // Growth rate in cm/day
    private static final String emoji = "🎍";

    public static String getEmoji() {
        return emoji;
    }

    public Bamboo(String name, int waterRequirement, List<String> pestVulnerabilities, int tempLow, int tempHigh, int initialHeight, int growthRate) {
        super(PlantType.BAMBOO, name, waterRequirement, pestVulnerabilities, tempLow, tempHigh, initialHeight, growthRate);
        this.growthRate = growthRate;
    }

    // Getter and Setter for growthRate
    public int getGrowthRate() {
        return growthRate;
    }

    public void setGrowthRate(int growthRate) {
        this.growthRate = growthRate;
        Logger.log(Logger.LogLevel.INFO, getName() + " growth rate set to " + growthRate + " cm/day.");
    }

    @Override
    public void grow() {
        super.grow();
        Logger.log(Logger.LogLevel.INFO, getName() + " is growing rapidly! 🎍 Current growth rate: " + growthRate + " cm/day.");
    }

    @Override
    public void dailyCheck() {
        super.dailyCheck();
        Logger.log(Logger.LogLevel.INFO, "Checking growth speed for " + getName() + ".");
    }

    @Override
    public void displaySpecialCareInstructions() {
        System.out.println(getName() + " requires specific care: Frequent watering, pest protection, and nutrient-rich soil.");
    }
}
