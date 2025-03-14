package com.project.factory;

import com.project.modules.*;
import com.project.logger.Logger;

import java.util.List;

/**
 * Factory class responsible for creating instances of Plant subclasses based on PlantType.
 */
public class PlantFactory {

    /**
     * Creates and returns an instance of a Plant subclass based on the provided PlantType.
     *
     * @param type                     The type of plant to create.
     * @param name                     The name of the plant.
     * @param waterRequirement         The water requirement of the plant.
     * @param pestVulnerabilities      A list of pests that can attack the plant.
     * @param temperatureToleranceLow  The lower bound of temperature tolerance.
     * @param temperatureToleranceHigh The upper bound of temperature tolerance.
     * @param additionalParams         Additional parameters required by specific PlantTypes.
     * @return An instance of a Plant subclass.
     * @throws IllegalArgumentException if the PlantType is unsupported or parameters are invalid.
     */
    public static Plant createPlant(
            PlantType type,
            String name,
            int waterRequirement,
            List<String> pestVulnerabilities,
            int temperatureToleranceLow,
            int temperatureToleranceHigh,
            Object... additionalParams // Varargs for additional parameters
    ) {
        switch (type) {
            case APPLE:
                if (additionalParams.length < 1) {
                    throw new IllegalArgumentException("Apple requires fruitYield parameter.");
                }
                int appleFruitYield;
                try {
                    appleFruitYield = (Integer) additionalParams[0];
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Invalid parameters for Apple. Expected integer for fruitYield.");
                }
                return new Apple(
                        name,
                        waterRequirement,
                        pestVulnerabilities,
                        temperatureToleranceLow,
                        temperatureToleranceHigh,
                        200,    // initialHeight in cm
                        12,     // growthRate in cm/day
                        appleFruitYield
                );

            case CHERRY:
                if (additionalParams.length < 1) {
                    throw new IllegalArgumentException("Cherry requires fruitYield parameter.");
                }
                int cherryFruitYield;
                try {
                    cherryFruitYield = (Integer) additionalParams[0];
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Invalid parameters for Cherry. Expected integer for fruitYield.");
                }
                return new Cherry(
                        name,
                        waterRequirement,
                        pestVulnerabilities,
                        temperatureToleranceLow,
                        temperatureToleranceHigh,
                        180,    // initialHeight in cm
                        10,     // growthRate in cm/day
                        cherryFruitYield
                );

            case LAVENDER:
                if (additionalParams.length < 1) {
                    throw new IllegalArgumentException("Lavender requires fragrance parameter.");
                }
                String fragrance;
                try {
                    fragrance = (String) additionalParams[0];
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Invalid parameters for Lavender. Expected String for fragrance.");
                }
                return new Lavender(
                        name,
                        waterRequirement,
                        pestVulnerabilities,
                        temperatureToleranceLow,
                        temperatureToleranceHigh,
                        20,     // initialDensity
                        30,     // trimmingFrequency in days
                        fragrance
                );

            case BAMBOO:
                if (additionalParams.length < 1) {
                    throw new IllegalArgumentException("Bamboo requires growthRate parameter.");
                }
                int bambooGrowthRate;
                try {
                    bambooGrowthRate = (Integer) additionalParams[0];
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Invalid parameters for Bamboo. Expected integer for growthRate.");
                }
                return new Bamboo(
                        name,
                        waterRequirement,
                        pestVulnerabilities,
                        temperatureToleranceLow,
                        temperatureToleranceHigh,
                        250,    // initialHeight in cm
                        bambooGrowthRate
                );

            case SUNFLOWER:
                if (additionalParams.length < 1) {
                    throw new IllegalArgumentException("Sunflower requires heliotropic parameter.");
                }
                String heliotropic;
                try {
                    heliotropic = (String) additionalParams[0];
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Invalid parameters for Sunflower. Expected String for heliotropic.");
                }
                return new Sunflower(
                        name,
                        waterRequirement,
                        pestVulnerabilities,
                        temperatureToleranceLow,
                        temperatureToleranceHigh,
                        60,     // initialDensity
                        30,     // trimmingFrequency in days
                        heliotropic
                );

            default:
                Logger.log(Logger.LogLevel.ERROR, "Unsupported PlantType: " + type);
                throw new IllegalArgumentException("Unsupported PlantType: " + type);
        }
    }
}
