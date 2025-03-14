package com.project.modules;


import com.project.factory.PlantType;

import java.util.Map;

public class Insect {
    private String name;
    private String emoji;
    private Map<String, Integer> damageMap; // PlantType -> Damage

    public Insect(String name, String emoji, Map<String, Integer> damageMap) {
        this.name = name;
        this.emoji = emoji;
        this.damageMap = damageMap;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getDamage(PlantType plantType) {
        return damageMap.getOrDefault(plantType.name(), 0);
    }

    // Utility method for string representation
    @Override
    public String toString() {
        return name + " " + emoji;
    }
}
