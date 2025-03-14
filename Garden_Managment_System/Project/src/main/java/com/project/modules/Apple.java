package com.project.modules;

import com.project.factory.PlantType;
import com.project.logger.Logger;

import java.util.List;

public class Apple extends Tree {
    private int fruitYield;
    private static final String emoji = "🍏";

    public Apple(String name, int waterRequirement, List<String> pestVulnerabilities, int tempLow, int tempHigh, int initialHeight, int growthRate, int fruitYield) {
        super(PlantType.APPLE, name, waterRequirement, pestVulnerabilities, tempLow, tempHigh, initialHeight, growthRate);
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
        Logger.log(Logger.LogLevel.INFO, getName() + " fruit yield set to " + fruitYield + ".");
    }

    @Override
    public void grow() {
        super.grow();
        Logger.log(Logger.LogLevel.INFO, getName() + " is growing and producing apples. 🍏");
    }

    @Override
    public void dailyCheck() {
        super.dailyCheck();
        Logger.log(Logger.LogLevel.INFO, "Checking apple tree health for " + getName() + ".");
    }

    @Override
    public void displaySpecialCareInstructions() {
        System.out.println(getName() + " requires specific care: Regular watering, pruning, pest monitoring, and sufficient sunlight.");
    }
}
