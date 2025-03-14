package com.project.modules;

import com.project.factory.PlantType;
import com.project.logger.Logger;

import java.util.List;

/**
 * Represents a Cherry tree.
 */
public class Cherry extends Tree {
    private int fruitYield; // Number of cherries per season
    private static final String emoji = "🍒";

    public Cherry(String name, int waterRequirement, List<String> pestVulnerabilities, int tempLow, int tempHigh, int initialHeight, int growthRate, int fruitYield) {
        super(PlantType.CHERRY, name, waterRequirement, pestVulnerabilities, tempLow, tempHigh, initialHeight, growthRate);
        this.fruitYield = fruitYield;
    }

    public static String getEmoji() {
        return emoji;
    }

    public int getFruitYield() {
        return fruitYield;
    }

    public void setFruitYield(int fruitYield) {
        this.fruitYield = fruitYield;
        Logger.log(Logger.LogLevel.INFO, getName() + " fruit yield set to " + fruitYield + " cherries per season.");
    }

    @Override
    public void grow() {
        super.grow();
        fruitYield += 15; // Increase yield with each growth cycle
        Logger.log(Logger.LogLevel.INFO, getName() + " is producing cherries. 🍒 Total yield: " + fruitYield + " cherries.");
    }

    @Override
    public void dailyCheck() {
        super.dailyCheck();
        Logger.log(Logger.LogLevel.INFO, "Checking fruit yield for " + getName() + ".");
    }

    @Override
    public void displaySpecialCareInstructions() {
        System.out.println(getName() + " requires specific care: Regular watering, pest control, pruning for fruit production, and ensuring optimal sunlight.");
    }
}
