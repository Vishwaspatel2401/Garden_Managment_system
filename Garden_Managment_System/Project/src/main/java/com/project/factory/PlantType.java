package com.project.factory;

/**
 * Enum representing different types of plants, each associated with a unique emoji.
 */
public enum PlantType {
    APPLE("🍏"),
    CHERRY("🍒"),
    LAVENDER("🌸"),
    BAMBOO("🎍"),
    SUNFLOWER("🌻");
    // Add other plant types and their corresponding emojis as needed.

    private final String emoji;

    /**
     * Constructor to associate an emoji with a PlantType.
     *
     * @param emoji The emoji representing the plant type.
     */
    PlantType(String emoji) {
        this.emoji = emoji;
    }

    /**
     * Retrieves the emoji associated with the plant type.
     *
     * @return The emoji as a String.
     */
    public String getEmoji() {
        return emoji;
    }
}
