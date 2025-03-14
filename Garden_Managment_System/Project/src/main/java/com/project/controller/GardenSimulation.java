package com.project.controller;

import com.project.factory.PlantFactory;
import com.project.factory.PlantType;
import com.project.modules.Insect;
import com.project.modules.Plant;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GardenSimulation extends Application {

    private final int GRID_SIZE = 8;
    private Button[][] gridButtons = new Button[GRID_SIZE][GRID_SIZE];
    private PlantType selectedPlantType = null;
    private Map<Button, Plant> plantMap = new HashMap<>();
    private Map<String, List<String>> pestVulnerabilities = new HashMap<>();
    private Map<String, Map<String, Integer>> insectDamageMap = new HashMap<>();
    private Map<Button, List<String>> activeInsectsMap = new HashMap<>();
    private Map<Button, Boolean> wateringStatusMap = new HashMap<>();
    private Map<Button, Boolean> pestControlStatusMap = new HashMap<>();
    private TextArea logArea;
    private Label currentDayLabel;
    private Label currentDateLabel;
    private Label currentTimeLabel;
    private Label currentTempLabel;
    private int currentTemperature = 25; // Default temperature
    private VBox leftSidebar; // Add leftSidebar as a class field
    private int daysElapsed = 0;
    private static final int PERFORMANCE_CHECK_INTERVAL = 24; // Check performance every 24 days
    private Map<String, Integer> plantStats = new HashMap<>();
    private int totalPlantsPlanted = 0;
    private int totalPlantsDied = 0;
    private int totalWaterAdded = 0;
    private int totalPestControlsApplied = 0;
    private PrintWriter logWriter; // Add PrintWriter for file logging
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
    private final Map<Button, ScheduledFuture<?>> wateringTasks = new ConcurrentHashMap<>();
    private final Map<Button, ScheduledFuture<?>> pestControlTasks = new ConcurrentHashMap<>();

    @Override
    public void start(Stage primaryStage) {
        // Initialize log file
        try {
            logWriter = new PrintWriter(new FileWriter("garden_log.txt", true));
            logWriter.println("\n=== New Garden Simulation Started at " + getCurrentDate() + " " + getCurrentTime() + " ===\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializePestVulnerabilities();
        initializeInsectData();
        insects = initializeInsects();

        // Initialize status maps
        wateringStatusMap = new HashMap<>();
        pestControlStatusMap = new HashMap<>();

        // Create main BorderPane layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #E8F5E9;");

        // Initialize top section
        HBox topSection = initializeTopSection();
        BorderPane.setMargin(topSection, new Insets(0, 0, 5, 0));
        mainLayout.setTop(topSection);

        // Create a horizontal SplitPane for main content
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setStyle("-fx-background-color: transparent;");

        // Initialize left sidebar with ScrollPane
        leftSidebar = initializeLeftSidebar();
        ScrollPane sidebarScroll = new ScrollPane(leftSidebar);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setStyle("-fx-background-color: transparent;");
        sidebarScroll.setPrefWidth(250); // Reduced width

        // Create right section with garden grid and log
        SplitPane rightSplitPane = new SplitPane();
        rightSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        // Create garden grid section with enhanced styling
        VBox gardenSection = new VBox(5);
        gardenSection.setPadding(new Insets(15));
        gardenSection.setStyle("-fx-background-color: white; " +
                             "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 0); " +
                             "-fx-background-radius: 10px; " +
                             "-fx-border-radius: 10px;");

        HBox gardenHeader = new HBox(10);
        gardenHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label gardenIcon = new Label("🌱");
        gardenIcon.setStyle("-fx-font-size: 20px;");
        Label gardenLabel = new Label("Garden Grid");
        gardenLabel.setFont(new Font("Arial", 20));
        gardenLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        gardenHeader.getChildren().addAll(gardenIcon, gardenLabel);
        
        GridPane gardenGrid = initializeGarden();
        gardenSection.getChildren().addAll(gardenHeader, gardenGrid);
        VBox.setVgrow(gardenGrid, Priority.ALWAYS);
        
        // Create log section with reduced height
        VBox logSection = createLogSection();
        
        // Add both sections to right split pane with adjusted proportions
        rightSplitPane.getItems().addAll(gardenSection, logSection);
        rightSplitPane.setDividerPositions(0.75); // More space for grid

        // Add components to main split pane
        mainSplitPane.getItems().addAll(sidebarScroll, rightSplitPane);
        mainSplitPane.setDividerPositions(0.2);

        // Style the split pane dividers
        mainSplitPane.lookupAll(".split-pane-divider").forEach(div -> {
            div.setStyle("-fx-background-color: #81C784; -fx-padding: 0 1 0 1;");
        });
        rightSplitPane.lookupAll(".split-pane-divider").forEach(div -> {
            div.setStyle("-fx-background-color: #81C784; -fx-padding: 1 0 1 0;");
        });

        // Set the split pane as the center of main layout
        mainLayout.setCenter(mainSplitPane);

        // Create scene with responsive sizing
        Scene scene = new Scene(mainLayout, 1400, 900);
        
        // Add window resize listeners
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            adjustUIForWidth(width);
            
            // Adjust split pane dividers
            double sidebarRatio = Math.max(0.15, Math.min(0.25, 250.0 / width));
            mainSplitPane.setDividerPositions(sidebarRatio);
            rightSplitPane.setDividerPositions(0.75);
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double height = newVal.doubleValue();
            adjustUIForHeight(height);
            
            // Maintain grid/log proportion
            rightSplitPane.setDividerPositions(0.75);
        });

        primaryStage.setTitle("Garden Simulation System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();

        startWaterReductionTimer();
        startDayIncrementTimer();
        startRandomInsectAttack();
    }

    @Override
    public void stop() {
        // Shutdown thread pools
        scheduler.shutdown();
        taskExecutor.shutdown();
        try {
            if (!scheduler.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
            if (!taskExecutor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            taskExecutor.shutdownNow();
        }
        
        // Close the log file when application stops
        if (logWriter != null) {
            logWriter.println("\n=== Garden Simulation Ended at " + getCurrentDate() + " " + getCurrentTime() + " ===\n");
            logWriter.close();
        }
    }

    private List<Insect> insects;

    private List<Insect> initializeInsects() {
        List<Insect> insectList = new ArrayList<>();
        
        insectList.add(new Insect("Aphid", "🪲", Map.of(
                PlantType.APPLE.name(), 10,
                PlantType.CHERRY.name(), 15,
                PlantType.SUNFLOWER.name(), 12
        )));
        
        insectList.add(new Insect("Ant", "🐜", Map.of(
                PlantType.APPLE.name(), 8,
                PlantType.BAMBOO.name(), 12
        )));
        
        insectList.add(new Insect("Grasshopper", "🦗", Map.of(
                PlantType.CHERRY.name(), 10,
                PlantType.BAMBOO.name(), 7,
                PlantType.SUNFLOWER.name(), 15
        )));
        
        insectList.add(new Insect("Ladybug", "🐞", Map.of(
                PlantType.LAVENDER.name(), 20
        )));
        
        return insectList;
    }
    


    private void startRandomInsectAttack() {
        Thread insectAttackThread = new Thread(() -> {
            Random random = new Random();
    
            while (true) {
                try {
                    int delay = (random.nextInt(30) + 10) * 1000; // Random interval: 10 to 40 seconds
                    Thread.sleep(delay);
    
                    Platform.runLater(() -> {
                        if (plantMap.isEmpty()) return;
    
                        // Select a random insect
                        Insect randomInsect = insects.get(random.nextInt(insects.size()));
    
                        // Select random plants to attack
                        List<Button> targetButtons = new ArrayList<>(plantMap.keySet());
                        int numTargets = random.nextInt(targetButtons.size()) + 1;
                        Collections.shuffle(targetButtons);
    
                        for (int i = 0; i < numTargets; i++) {
                            Button gridButton = targetButtons.get(i);
                            Plant plant = plantMap.get(gridButton);
    
                            if (plant != null && randomInsect.getDamage(plant.getPlantType()) > 0) {
                                int damage = randomInsect.getDamage(plant.getPlantType());
    
                                // Apply damage
                                plant.decreaseHealth(damage);
                                activeInsectsMap.computeIfAbsent(gridButton, k -> new ArrayList<>()).add(randomInsect.getName());
    
                                // Log only severe damage to file
                                if (damage > 15 || plant.getHealth() < 30) {
                                    logImportant(getCurrentTime() + " 🚨 SEVERE INSECT DAMAGE: " + randomInsect.getName() + 
                                        " has caused significant damage to " + plant.getName() + 
                                        " at coordinates (" + ((int[]) gridButton.getUserData())[0] + ", " + 
                                        ((int[]) gridButton.getUserData())[1] + "). Damage: -" + damage + 
                                        " HP. Current health: " + plant.getHealth() + "%\n");
                                } else {
                                    log(getCurrentTime() + " 🚨 ALERT: " + randomInsect.getName() + " has launched an attack on " + 
                                        plant.getName() + " at coordinates (" + ((int[]) gridButton.getUserData())[0] + ", " + 
                                        ((int[]) gridButton.getUserData())[1] + "). Damage inflicted: -" + damage + 
                                        " HP. Current health: " + plant.getHealth() + "%\n");
                                }
    
                                // Update the grid button
                                updateGridButtonText(gridButton, plant);
                                
                                // Check if plant has died after insect attack
                                checkAndHandlePlantDeath(gridButton, plant);
                            }
                        }
    
                        // Start automatic pest control after attack
                        startAutomaticPestControl();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    
        insectAttackThread.setDaemon(true);
        insectAttackThread.start();
    }
    
    private void startAutomaticPestControl() {
        taskExecutor.submit(() -> {
            try {
                Thread.sleep(10000);
                Platform.runLater(() -> {
                    for (Map.Entry<Button, List<String>> entry : new HashMap<>(activeInsectsMap).entrySet()) {
                        Button gridButton = entry.getKey();
                        List<String> insects = entry.getValue();

                        if (!insects.isEmpty()) {
                            // Log the start of pest control
                            log(getCurrentTime() + " 🧴 AUTOMATIC PEST CONTROL: Starting pest control at coordinates (" +
                                ((int[]) gridButton.getUserData())[0] + ", " + ((int[]) gridButton.getUserData())[1] +
                                "). Active insects: " + String.join(", ", insects) + "\n");

                            insects.clear();
                            pestControlStatusMap.put(gridButton, true);
                            updateGridButtonText(gridButton, plantMap.get(gridButton));

                            // Schedule removal of pest control symbol
                            scheduler.schedule(() -> {
                                Platform.runLater(() -> {
                                    pestControlStatusMap.remove(gridButton);
                                    updateGridButtonText(gridButton, plantMap.get(gridButton));
                                    
                                    // Log completion of pest control
                                    log(getCurrentTime() + " ✅ PEST CONTROL COMPLETE: Successfully eliminated insects at coordinates (" +
                                        ((int[]) gridButton.getUserData())[0] + ", " + ((int[]) gridButton.getUserData())[1] + ")\n");
                                });
                            }, 3, TimeUnit.SECONDS);

                            startHealthRecovery(gridButton);
                        }
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    


    private void initializePestVulnerabilities() {
        pestVulnerabilities.put(PlantType.APPLE.name(), Arrays.asList("Aphid", "Ant"));
        pestVulnerabilities.put(PlantType.CHERRY.name(), Arrays.asList("Aphid", "Grasshopper"));
        pestVulnerabilities.put(PlantType.LAVENDER.name(), Collections.singletonList("Ladybug"));
        pestVulnerabilities.put(PlantType.BAMBOO.name(), Arrays.asList("Ant", "Grasshopper"));
        pestVulnerabilities.put(PlantType.SUNFLOWER.name(), Arrays.asList("Aphid", "Grasshopper"));
    }

    private void initializeInsectData() {
        insectDamageMap.put("Aphid", Map.of(PlantType.APPLE.name(), 10, PlantType.CHERRY.name(), 15, PlantType.SUNFLOWER.name(), 12));
        insectDamageMap.put("Ant", Map.of(PlantType.APPLE.name(), 8, PlantType.BAMBOO.name(), 12));
        insectDamageMap.put("Grasshopper", Map.of(PlantType.CHERRY.name(), 10, PlantType.BAMBOO.name(), 7, PlantType.SUNFLOWER.name(), 15));
        insectDamageMap.put("Ladybug", Map.of(PlantType.LAVENDER.name(), 20));

    }

    private HBox initializeTopSection() {
        HBox topSection = new HBox();
        topSection.setSpacing(30);
        topSection.setPadding(new Insets(15));
        topSection.setStyle("-fx-background-color: #81C784;");
        topSection.setAlignment(javafx.geometry.Pos.CENTER);

        String labelStyle = "-fx-text-fill: white; " +
                          "-fx-font-size: 16px; " +
                          "-fx-font-family: 'Arial'; " +
                          "-fx-font-weight: bold;";

        currentDayLabel = new Label("Day: 1");
        currentDayLabel.setStyle(labelStyle);
        
        currentDateLabel = new Label("Date: " + getCurrentDate());
        currentDateLabel.setStyle(labelStyle);
        
        currentTimeLabel = new Label("Time: " + getCurrentTime());
        currentTimeLabel.setStyle(labelStyle);
        
        currentTempLabel = new Label("Current Temp: " + currentTemperature + "°C");
        currentTempLabel.setStyle(labelStyle);

        // Create containers for each label with consistent spacing
        HBox dayBox = createTopSectionBox(currentDayLabel, "📅");
        HBox dateBox = createTopSectionBox(currentDateLabel, "📆");
        HBox timeBox = createTopSectionBox(currentTimeLabel, "⏰");
        HBox tempBox = createTopSectionBox(currentTempLabel, "🌡️");

        topSection.getChildren().addAll(dayBox, dateBox, timeBox, tempBox);

        Thread timeUpdater = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> currentTimeLabel.setText("Time: " + getCurrentTime()));
                } catch (InterruptedException ignored) {}
            }
        });
        timeUpdater.setDaemon(true);
        timeUpdater.start();

        return topSection;
    }

    private HBox createTopSectionBox(Label label, String icon) {
        HBox box = new HBox(10);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");
        box.getChildren().addAll(iconLabel, label);
        return box;
    }

    private VBox initializeLeftSidebar() {
        VBox sidebar = new VBox(10); // Reduced spacing
        sidebar.setPadding(new Insets(10)); // Reduced padding
        sidebar.setStyle("-fx-background-color: white;");

        // Create title for sidebar
        Label sidebarTitle = new Label("Garden Controls");
        sidebarTitle.setFont(new Font("Arial", 20)); // Reduced font size
        sidebarTitle.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        
        // Create sections with reduced spacing
        VBox addPlantSection = createAddPlantSection();
        VBox addWaterSection = createAddWaterSection();
        VBox setTempSection = createSetTemperatureSection();
        VBox insectAttackSection = createInsectAttackSection();
        VBox applyPestControlSection = createApplyPestControlSection();

        // Add sections with minimal separators
        sidebar.getChildren().addAll(
            sidebarTitle,
            new Separator(),
            addPlantSection,
            addWaterSection,
            setTempSection,
            insectAttackSection,
            applyPestControlSection
        );

        return sidebar;
    }

    private Separator createSectionSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #81C784;");
        VBox.setMargin(separator, new Insets(5, 0, 5, 0));
        return separator;
    }

    private VBox createAddPlantSection() {
        VBox section = new VBox();
        section.setSpacing(12);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: #F1F8E9; " +
                        "-fx-border-color: #2E7D32; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 0);");

        // Create header with icon
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label iconLabel = new Label("🌱");
        iconLabel.setStyle("-fx-font-size: 20px;");
        Label label = new Label("Garden Plants");
        label.setFont(new Font("Arial", 18));
        label.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        header.getChildren().addAll(iconLabel, label);

        // Initialize ToggleGroup
        ToggleGroup plantToggleGroup = new ToggleGroup();

        // Enhanced style for toggle buttons
        String toggleButtonStyle = "-fx-pref-width: 140px; " +
                                 "-fx-pref-height: 35px; " +
                                 "-fx-font-size: 14px; " +
                                 "-fx-background-color: white; " +
                                 "-fx-border-color: #81C784; " +
                                 "-fx-border-width: 2px; " +
                                 "-fx-border-radius: 8px; " +
                                 "-fx-background-radius: 8px; " +
                                 "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 0);";
        
        String toggleButtonHoverStyle = "-fx-pref-width: 140px; " +
                                      "-fx-pref-height: 35px; " +
                                      "-fx-font-size: 14px; " +
                                      "-fx-background-color: #E8F5E9; " +
                                      "-fx-border-color: #66BB6A; " +
                                      "-fx-border-width: 2px; " +
                                      "-fx-border-radius: 8px; " +
                                      "-fx-background-radius: 8px; " +
                                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 0);";
        
        String toggleButtonSelectedStyle = "-fx-pref-width: 140px; " +
                                         "-fx-pref-height: 35px; " +
                                         "-fx-font-size: 14px; " +
                                         "-fx-background-color: #81C784; " +
                                         "-fx-text-fill: white; " +
                                         "-fx-border-color: #388E3C; " +
                                         "-fx-border-width: 2px; " +
                                         "-fx-border-radius: 8px; " +
                                         "-fx-background-radius: 8px; " +
                                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 5, 0, 0, 0);";

        // Create enhanced plant buttons
        ToggleButton appleButton = createEnhancedToggleButton("🍏 Apple", toggleButtonStyle, toggleButtonHoverStyle, toggleButtonSelectedStyle);
        ToggleButton cherryButton = createEnhancedToggleButton("🍒 Cherry", toggleButtonStyle, toggleButtonHoverStyle, toggleButtonSelectedStyle);
        ToggleButton lavenderButton = createEnhancedToggleButton("🌸 Lavender", toggleButtonStyle, toggleButtonHoverStyle, toggleButtonSelectedStyle);
        ToggleButton bambooButton = createEnhancedToggleButton("🎍 Bamboo", toggleButtonStyle, toggleButtonHoverStyle, toggleButtonSelectedStyle);
        ToggleButton sunflowerButton = createEnhancedToggleButton("🌻 Sunflower", toggleButtonStyle, toggleButtonHoverStyle, toggleButtonSelectedStyle);

        // Set up toggle group
        appleButton.setToggleGroup(plantToggleGroup);
        cherryButton.setToggleGroup(plantToggleGroup);
        lavenderButton.setToggleGroup(plantToggleGroup);
        bambooButton.setToggleGroup(plantToggleGroup);
        sunflowerButton.setToggleGroup(plantToggleGroup);

        appleButton.setUserData(PlantType.APPLE);
        cherryButton.setUserData(PlantType.CHERRY);
        lavenderButton.setUserData(PlantType.LAVENDER);
        bambooButton.setUserData(PlantType.BAMBOO);
        sunflowerButton.setUserData(PlantType.SUNFLOWER);

        // Selection listener
        plantToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                selectedPlantType = (PlantType) newToggle.getUserData();
                log(getCurrentTime() + " 🌱 Selected Plant Type: " + selectedPlantType.name() + "\n");
            } else {
                selectedPlantType = null;
                log(getCurrentTime() + " ❌ No Plant Type Selected.\n");
            }
        });

        // Enhanced clear selection button
        Button clearSelectionButton = new Button("❌ Clear Selection");
        clearSelectionButton.setStyle("-fx-background-color: #EF5350; " +
                                    "-fx-text-fill: white; " +
                                    "-fx-font-size: 14px; " +
                                    "-fx-pref-width: 140px; " +
                                    "-fx-pref-height: 35px; " +
                                    "-fx-border-radius: 8px; " +
                                    "-fx-background-radius: 8px; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 0);");

        clearSelectionButton.setOnMouseEntered(e -> 
            clearSelectionButton.setStyle("-fx-background-color: #E53935; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-size: 14px; " +
                                        "-fx-pref-width: 140px; " +
                                        "-fx-pref-height: 35px; " +
                                        "-fx-border-radius: 8px; " +
                                        "-fx-background-radius: 8px; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 0);"));

        clearSelectionButton.setOnMouseExited(e -> 
            clearSelectionButton.setStyle("-fx-background-color: #EF5350; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-font-size: 14px; " +
                                        "-fx-pref-width: 140px; " +
                                        "-fx-pref-height: 35px; " +
                                        "-fx-border-radius: 8px; " +
                                        "-fx-background-radius: 8px; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 0);"));

        clearSelectionButton.setOnAction(e -> plantToggleGroup.selectToggle(null));

        // Add spacing between buttons
        VBox buttonContainer = new VBox(10);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        buttonContainer.setPadding(new Insets(10, 0, 10, 0));
        buttonContainer.getChildren().addAll(
            appleButton, cherryButton, lavenderButton, 
            bambooButton, sunflowerButton, clearSelectionButton
        );

        section.getChildren().addAll(header, buttonContainer);
        return section;
    }

    private ToggleButton createEnhancedToggleButton(String text, String defaultStyle, String hoverStyle, String selectedStyle) {
        ToggleButton button = new ToggleButton(text);
        button.setStyle(defaultStyle);
        
        button.setOnMouseEntered(e -> {
            if (!button.isSelected()) {
                button.setStyle(hoverStyle);
            }
        });
        
        button.setOnMouseExited(e -> {
            if (!button.isSelected()) {
                button.setStyle(defaultStyle);
            }
        });
        
        button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            button.setStyle(isSelected ? selectedStyle : defaultStyle);
        });
        
        return button;
    }

    private VBox createAddWaterSection() {
        VBox section = new VBox();
        section.setSpacing(12);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label label = new Label("Water Plants");
        label.setFont(new Font("Arial", 20));
        label.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");

        Button addWaterButton = new Button("💧 Water Garden");
        String buttonStyle = "-fx-pref-width: 160px; " +
                           "-fx-pref-height: 45px; " +
                           "-fx-font-size: 15px; " +
                           "-fx-background-color: linear-gradient(to right, #29B6F6, #0288D1); " +
                           "-fx-text-fill: white; " +
                           "-fx-border-radius: 10; " +
                           "-fx-background-radius: 10; " +
                           "-fx-cursor: hand; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 0);";
        
        addWaterButton.setStyle(buttonStyle);
        
        addWaterButton.setOnMouseEntered(e -> 
            addWaterButton.setStyle(buttonStyle.replace("#29B6F6, #0288D1", "#4FC3F7, #039BE5")));
        
        addWaterButton.setOnMouseExited(e -> 
            addWaterButton.setStyle(buttonStyle));
        
        addWaterButton.setOnAction(e -> showAddWaterPopup());

        section.getChildren().addAll(label, addWaterButton);
        return section;
    }

    private VBox createSetTemperatureSection() {
        VBox section = new VBox();
        section.setSpacing(12);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label label = new Label("Temperature");
        label.setFont(new Font("Arial", 20));
        label.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");

        Button setTempButton = new Button("🌡️ Set Temperature");
        String buttonStyle = "-fx-pref-width: 160px; " +
                           "-fx-pref-height: 45px; " +
                           "-fx-font-size: 15px; " +
                           "-fx-background-color: linear-gradient(to right, #FF7043, #F4511E); " +
                           "-fx-text-fill: white; " +
                           "-fx-border-radius: 10; " +
                           "-fx-background-radius: 10; " +
                           "-fx-cursor: hand; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 0);";
        
        setTempButton.setStyle(buttonStyle);
        
        setTempButton.setOnMouseEntered(e -> 
            setTempButton.setStyle(buttonStyle.replace("#FF7043, #F4511E", "#FF8A65, #FF5722")));
        
        setTempButton.setOnMouseExited(e -> 
            setTempButton.setStyle(buttonStyle));
        
        setTempButton.setOnAction(e -> showSetTemperaturePopup());

        section.getChildren().addAll(label, setTempButton);
        return section;
    }

    private VBox createInsectAttackSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: #F1F8E9; -fx-border-color: #2E7D32; -fx-border-width: 2px; -fx-border-radius: 5px;");
        Label label = new Label("Insect Attack");
        label.setFont(new Font("Arial", 16));
        label.setStyle("-fx-text-fill: #2E7D32;");

        Button insectAttackButton = new Button("🪲 Attack");
        String buttonStyle = "-fx-pref-width: 120px; -fx-pref-height: 40px; -fx-background-color: #81C784; " +
                           "-fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;";
        
        insectAttackButton.setStyle(buttonStyle);
        
        // Add hover and pressed effects
        insectAttackButton.setOnMouseEntered(e -> 
            insectAttackButton.setStyle(buttonStyle.replace("#81C784", "#66BB6A")));
        
        insectAttackButton.setOnMouseExited(e -> 
            insectAttackButton.setStyle(buttonStyle));
        
        insectAttackButton.setOnMousePressed(e -> 
            insectAttackButton.setStyle(buttonStyle.replace("#81C784", "#43A047")));
        
        insectAttackButton.setOnMouseReleased(e -> 
            insectAttackButton.setStyle(buttonStyle));

        insectAttackButton.setOnAction(e -> showInsectAttackPopup());

        section.getChildren().addAll(label, insectAttackButton);
        return section;
    }

    private VBox createApplyPestControlSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setPadding(new Insets(10));
        section.setStyle("-fx-background-color: #F1F8E9; -fx-border-color: #2E7D32; -fx-border-width: 2px; -fx-border-radius: 5px;");
        Label label = new Label("Apply Pest Control");
        label.setFont(new Font("Arial", 16));
        label.setStyle("-fx-text-fill: #2E7D32;");

        Button applyPestControlButton = new Button("🧴 Pest Control");
        String buttonStyle = "-fx-pref-width: 120px; -fx-pref-height: 40px; -fx-background-color: #81C784; " +
                           "-fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5px; -fx-background-radius: 5px;";
        
        applyPestControlButton.setStyle(buttonStyle);
        
        // Add hover and pressed effects
        applyPestControlButton.setOnMouseEntered(e -> 
            applyPestControlButton.setStyle(buttonStyle.replace("#81C784", "#66BB6A")));
        
        applyPestControlButton.setOnMouseExited(e -> 
            applyPestControlButton.setStyle(buttonStyle));
        
        applyPestControlButton.setOnMousePressed(e -> 
            applyPestControlButton.setStyle(buttonStyle.replace("#81C784", "#43A047")));
        
        applyPestControlButton.setOnMouseReleased(e -> 
            applyPestControlButton.setStyle(buttonStyle));

        applyPestControlButton.setOnAction(e -> showApplyPestControlPopup());

        section.getChildren().addAll(label, applyPestControlButton);
        return section;
    }

    private void showApplyPestControlPopup() {
        Stage popup = new Stage();
        popup.setTitle("Select Pest Control");

        VBox layout = new VBox();
        layout.setSpacing(10);
        layout.setPadding(new Insets(10));

        CheckBox pestA = new CheckBox("🧴 Pest A (Aphid Control)");
        CheckBox pestB = new CheckBox("🧴 Pest B (Ant Control)");
        CheckBox pestC = new CheckBox("🧴 Pest C (Grasshopper Control)");
        CheckBox pestD = new CheckBox("🧴 Pest D (Ladybug Control)");

        Button applyButton = new Button("Apply");
        applyButton.setOnAction(e -> {
            List<String> selectedPests = new ArrayList<>();
            if (pestA.isSelected()) selectedPests.add("Aphid");
            if (pestB.isSelected()) selectedPests.add("Ant");
            if (pestC.isSelected()) selectedPests.add("Grasshopper");
            if (pestD.isSelected()) selectedPests.add("Ladybug");

            if (!selectedPests.isEmpty()) {
                totalPestControlsApplied++;
                // Log the start of manual pest control
                log(getCurrentTime() + " 🧴 MANUAL PEST CONTROL: Starting application of " + 
                    String.join(", ", selectedPests) + " control measures\n");

                for (Button gridButton : activeInsectsMap.keySet()) {
                    List<String> activeInsects = activeInsectsMap.get(gridButton);
                    if (activeInsects != null && !activeInsects.isEmpty()) {
                        List<String> removedInsects = new ArrayList<>();
                        for (String insect : activeInsects) {
                            switch (insect) {
                                case "Aphid":
                                    if (selectedPests.contains("Aphid")) removedInsects.add(insect);
                                    break;
                                case "Ant":
                                    if (selectedPests.contains("Ant")) removedInsects.add(insect);
                                    break;
                                case "Grasshopper":
                                    if (selectedPests.contains("Grasshopper")) removedInsects.add(insect);
                                    break;
                                case "Ladybug":
                                    if (selectedPests.contains("Ladybug")) removedInsects.add(insect);
                                    break;
                            }
                        }
                        activeInsects.removeAll(removedInsects);

                        if (removedInsects.size() > 0) {
                            Plant plant = plantMap.get(gridButton);
                            // Show pest control symbol and update immediately
                            pestControlStatusMap.put(gridButton, true);
                            updateGridButtonText(gridButton, plant);

                            // Log detailed pest control action
                            log(getCurrentTime() + " 🎯 PEST CONTROL ACTION: Applied " + 
                                String.join(", ", selectedPests) + " control to " + plant.getName() +
                                " at coordinates (" + ((int[]) gridButton.getUserData())[0] + ", " +
                                ((int[]) gridButton.getUserData())[1] + "). Eliminated: " +
                                String.join(", ", removedInsects) + "\n");

                            // Schedule removal of pest control symbol
                            scheduler.schedule(() -> {
                                Platform.runLater(() -> {
                                    pestControlStatusMap.remove(gridButton);
                                    updateGridButtonText(gridButton, plant);
                                });
                            }, 3, TimeUnit.SECONDS);

                            startHealthRecovery(gridButton);
                        }
                    }
                }
                // Log completion of manual pest control
                log(getCurrentTime() + " ✅ MANUAL PEST CONTROL COMPLETE: Successfully applied selected pest control measures\n");
            } else {
                log(getCurrentTime() + " ⚠️ WARNING: No pest control measures selected\n");
            }
            popup.close();
        });

        layout.getChildren().addAll(
            new Label("Select Pest Control Measures:"), 
            pestA, pestB, pestC, pestD,
            new Separator(),
            applyButton
        );

        Scene popupScene = new Scene(layout, 300, 250);
        popup.setScene(popupScene);
        popup.show();
    }

    private void startHealthRecovery(Button gridButton) {
        Plant plant = plantMap.get(gridButton);
        if (plant == null || plant.getHealth() >= 100) return;

        ScheduledFuture<?> recoveryTask = scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                if (plant.getHealth() < 100) {
                    int recoveryAmount = calculateRecoveryAmount(plant);
                    plant.increaseHealth(recoveryAmount);
                    updateGridButtonText(gridButton, plant);
                }
            });
        }, 15, 15, TimeUnit.SECONDS);

        // Store the task for potential cancellation
        pestControlTasks.put(gridButton, recoveryTask);
    }

    private int calculateRecoveryAmount(Plant plant) {
        int recoveryAmount;
        
        // Base recovery based on current health
        if (plant.getHealth() < 30) {
            recoveryAmount = 7;
        } else if (plant.getHealth() < 50) {
            recoveryAmount = 5;
        } else {
            recoveryAmount = 3;
        }
        
        // Adjust for water level
        if (plant.getCurrentWaterLevel() >= 60) {
            recoveryAmount += 2;
        } else if (plant.getCurrentWaterLevel() < 40) {
            recoveryAmount = Math.max(1, recoveryAmount - 1);
        }
        
        // Adjust for temperature
        if (currentTemperature >= 20 && currentTemperature <= 30) {
            recoveryAmount += 1;
        } else if (currentTemperature > 35 || currentTemperature < 10) {
            recoveryAmount = Math.max(1, recoveryAmount - 1);
        }
        
        return recoveryAmount;
    }
    

    private void showInsectAttackPopup() {
        Stage popup = new Stage();
        popup.setTitle("Select Insects");

        VBox layout = new VBox();
        layout.setSpacing(10);
        layout.setPadding(new Insets(10));

        // Dynamically create checkboxes for all insects
        List<CheckBox> insectCheckBoxes = new ArrayList<>();
        for (Insect insect : insects) {
            CheckBox checkBox = new CheckBox(insect.toString());
            checkBox.setUserData(insect); // Store insect object
            insectCheckBoxes.add(checkBox);
        }

        Button attackButton = new Button("Attack");
        attackButton.setOnAction(e -> {
            List<Insect> selectedInsects = new ArrayList<>();
            for (CheckBox checkBox : insectCheckBoxes) {
                if (checkBox.isSelected()) {
                    selectedInsects.add((Insect) checkBox.getUserData());
                }
            }

            if (!selectedInsects.isEmpty()) {
                for (Button gridButton : plantMap.keySet()) {
                    Plant plant = plantMap.get(gridButton);
                    if (plant != null) {
                        for (Insect insect : selectedInsects) {
                            if (insect.getDamage(plant.getPlantType()) > 0) {
                                int damage = insect.getDamage(plant.getPlantType());

                                // Apply damage
                                plant.decreaseHealth(damage);
                                activeInsectsMap.computeIfAbsent(gridButton, k -> new ArrayList<>()).add(insect.getName());

                                // Log the attack
                                log(getCurrentTime() + " 🚨 ALERT: Manual insect attack initiated! " + insect + " has attacked " + plant.getPlantType().name() +
                                        " at coordinates (" + ((int[]) gridButton.getUserData())[0] + ", " + ((int[]) gridButton.getUserData())[1] + 
                                        "). Damage inflicted: " + damage + " HP\n");

                                // Update the grid button
                                updateGridButtonText(gridButton, plant);
                                
                                // Check if plant has died after manual insect attack
                                checkAndHandlePlantDeath(gridButton, plant);
                            }
                        }
                    }
                }
            }
            popup.close();
        });

        layout.getChildren().addAll(
            new Label("Select Insects to Attack:"), 
            new VBox(insectCheckBoxes.toArray(new CheckBox[0])), 
            attackButton
        );

        Scene popupScene = new Scene(layout, 300, 250);
        popup.setScene(popupScene);
        popup.show();
    }


    private GridPane initializeGarden() {
        GridPane gardenGrid = new GridPane();
        gardenGrid.setPadding(new Insets(15));
        gardenGrid.setHgap(8);
        gardenGrid.setVgap(8);
        gardenGrid.setStyle("-fx-background-color: #F1F8E9; " +
                           "-fx-border-color: #81C784; " +
                           "-fx-border-width: 2px; " +
                           "-fx-background-radius: 12px; " +
                           "-fx-border-radius: 12px; " +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 0);");

        // Make grid cells responsive with larger size
        for (int i = 0; i < GRID_SIZE; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setHgrow(Priority.ALWAYS);
            colConstraints.setFillWidth(true);
            colConstraints.setPrefWidth(85); // Slightly larger cells
            gardenGrid.getColumnConstraints().add(colConstraints);

            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            rowConstraints.setFillHeight(true);
            rowConstraints.setPrefHeight(85); // Slightly larger cells
            gardenGrid.getRowConstraints().add(rowConstraints);
        }

        String baseButtonStyle = "-fx-background-color: #A5D6A7; " +
                               "-fx-border-color: #81C784; " +
                               "-fx-border-width: 2px; " +
                               "-fx-background-radius: 8px; " +
                               "-fx-border-radius: 8px; " +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 0); " +
                               "-fx-font-size: 16px;";

        String hoverButtonStyle = "-fx-background-color: #81C784; " +
                                "-fx-border-color: #66BB6A; " +
                                "-fx-border-width: 2px; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-border-radius: 8px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 0); " +
                                "-fx-font-size: 16px;";

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Button gridButton = new Button();
                gridButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                gridButton.setStyle(baseButtonStyle);

                // Add hover effects
                gridButton.setOnMouseEntered(e -> {
                    if (!plantMap.containsKey(gridButton)) {
                        gridButton.setStyle(hoverButtonStyle);
                    }
                });

                gridButton.setOnMouseExited(e -> {
                    if (!plantMap.containsKey(gridButton)) {
                        gridButton.setStyle(baseButtonStyle);
                    }
                });

                int row = i;
                int col = j;
                gridButton.setUserData(new int[]{row, col});

                // Create context menu for right-click
                ContextMenu contextMenu = new ContextMenu();
                MenuItem removeItem = new MenuItem("🗑️ Remove Plant");
                removeItem.setStyle("-fx-font-size: 14px;");
                removeItem.setOnAction(e -> {
                    if (plantMap.containsKey(gridButton)) {
                        Plant plant = plantMap.get(gridButton);
                        // Remove the plant from all maps
                        plantMap.remove(gridButton);
                        activeInsectsMap.remove(gridButton);
                        wateringStatusMap.remove(gridButton);
                        pestControlStatusMap.remove(gridButton);

                        // Reset button style
                        gridButton.setText("");
                        gridButton.setStyle(baseButtonStyle);
                        gridButton.setOnMouseEntered(ev -> gridButton.setStyle(hoverButtonStyle));
                        gridButton.setOnMouseExited(ev -> gridButton.setStyle(baseButtonStyle));

                        // Log the removal
                        log(getCurrentTime() + " 🗑️ PLANT REMOVED: " + plant.getName() + " has been removed from coordinates (" +
                            row + ", " + col + ")\n");
                    }
                });
                contextMenu.getItems().add(removeItem);

                // Add context menu to button
                gridButton.setOnContextMenuRequested(e -> {
                    if (plantMap.containsKey(gridButton)) {
                        contextMenu.show(gridButton, e.getScreenX(), e.getScreenY());
                    }
                });

                gridButton.setOnAction(e -> {
                    if (selectedPlantType != null && !plantMap.containsKey(gridButton)) {
                        Plant plant = createPlant(selectedPlantType, selectedPlantType.name() + " " + (row * GRID_SIZE + col));
                        plantMap.put(gridButton, plant);
                        updateGridButtonText(gridButton, plant);
                        log(getCurrentTime() + " 🌱 PLANTING: Successfully planted " + plant.getPlantType().name() + 
                            " at coordinates (" + row + ", " + col + "). Plant ID: " + plant.getName() + "\n");
                    } else if (plantMap.containsKey(gridButton)) {
                        log(getCurrentTime() + " ⚠️ ERROR: Planting failed at coordinates (" + row + ", " + col + 
                            "). Location is already occupied.\n");
                    } else {
                        log(getCurrentTime() + " ⚠️ ERROR: Planting failed. No plant type selected.\n");
                    }
                });

                gridButtons[i][j] = gridButton;
                gardenGrid.add(gridButton, j, i);
            }
        }
        return gardenGrid;
    }

    private void updateGridButtonText(Button gridButton, Plant plant) {
        if (plant != null) {
            StringBuilder buttonText = new StringBuilder();
            buttonText.append(plant.getPlantType().getEmoji());

            List<String> activeInsects = activeInsectsMap.get(gridButton);
            if (activeInsects != null && !activeInsects.isEmpty()) {
                for (String insect : activeInsects) {
                    switch (insect) {
                        case "Aphid": buttonText.append("🪲"); break;
                        case "Ant": buttonText.append("🐜"); break;
                        case "Grasshopper": buttonText.append("🦗"); break;
                        case "Ladybug": buttonText.append("🐞"); break;
                    }
                }
            }

            boolean isWatering = Boolean.TRUE.equals(wateringStatusMap.get(gridButton));
            boolean hasPestControl = Boolean.TRUE.equals(pestControlStatusMap.get(gridButton));
            
            if (isWatering) buttonText.append("💧");
            if (hasPestControl) buttonText.append("🧴");

            gridButton.setText(buttonText.toString());

            // Create and set custom tooltip
            updatePlantTooltip(gridButton, plant);

            String plantedStyle = "-fx-background-color: #66BB6A; " +
                                "-fx-border-color: #43A047; " +
                                "-fx-border-width: 2px; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-border-radius: 8px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 0); " +
                                "-fx-font-size: 16px;";

            String plantedHoverStyle = "-fx-background-color: #4CAF50; " +
                                     "-fx-border-color: #388E3C; " +
                                     "-fx-border-width: 2px; " +
                                     "-fx-background-radius: 8px; " +
                                     "-fx-border-radius: 8px; " +
                                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 0); " +
                                     "-fx-font-size: 16px;";

            gridButton.setStyle(plantedStyle);
            gridButton.setOnMouseEntered(e -> gridButton.setStyle(plantedHoverStyle));
            gridButton.setOnMouseExited(e -> gridButton.setStyle(plantedStyle));
        }
    }

    private void updatePlantTooltip(Button gridButton, Plant plant) {
        // Create tooltip content with VBox layout
        VBox tooltipContent = new VBox(5);
        tooltipContent.setPadding(new Insets(5));
        tooltipContent.setStyle("-fx-background-color: white;");

        // Plant name and type
        Label nameLabel = new Label(plant.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        tooltipContent.getChildren().add(nameLabel);

        // Health bar
        VBox healthBar = createStatusBar(
            "Health: " + plant.getHealth() + "%",
            plant.getHealth(),
            plant.getHealth() >= 80 ? "#4CAF50" :  // Green for good health
            plant.getHealth() >= 50 ? "#FFA726" :  // Orange for medium health
            "#F44336"                              // Red for low health
        );
        tooltipContent.getChildren().add(healthBar);

        // Water bar
        VBox waterBar = createStatusBar(
            "Water: " + plant.getCurrentWaterLevel() + "%",
            plant.getCurrentWaterLevel(),
            plant.getCurrentWaterLevel() >= 80 ? "#2196F3" :  // Blue for high water
            plant.getCurrentWaterLevel() >= 50 ? "#64B5F6" :  // Light blue for medium water
            "#90CAF9"                                         // Very light blue for low water
        );
        tooltipContent.getChildren().add(waterBar);

        // Create custom tooltip
        Tooltip tooltip = new Tooltip();
        tooltip.setGraphic(tooltipContent);
        tooltip.setStyle("-fx-background-color: white; -fx-font-size: 12px;");
        
        // Set show/hide delays
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setHideDelay(Duration.millis(100));

        gridButton.setTooltip(tooltip);
    }

    private VBox createStatusBar(String text, int percentage, String color) {
        VBox container = new VBox(5);  // Increased spacing between elements
        
        // Create status text label
        Label statusLabel = new Label(text.split(":")[0]);  // Just the label part (e.g., "Health" or "Water")
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        
        // Create the progress bar container
        StackPane barContainer = new StackPane();
        barContainer.setMinHeight(20);  // Increased height
        barContainer.setMaxHeight(20);
        barContainer.setMinWidth(150);
        barContainer.setPadding(new Insets(0, 5, 0, 5));  // Add some padding
        
        // Background of the bar
        Rectangle background = new Rectangle(150, 20);  // Increased height
        background.setFill(Color.LIGHTGRAY);
        background.setArcWidth(10);
        background.setArcHeight(10);
        
        // Foreground (filled portion) of the bar
        Rectangle foreground = new Rectangle(percentage * 1.5, 20);  // Increased height
        foreground.setFill(Color.web(color));
        foreground.setArcWidth(10);
        foreground.setArcHeight(10);
        
        // Create percentage text with background for better visibility
        Label percentageLabel = new Label(percentage + "%");
        percentageLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; " +
                               "-fx-padding: 2 5 2 5; -fx-background-radius: 5;");
        
        // Center the percentage text
        StackPane.setAlignment(foreground, Pos.CENTER_LEFT);
        StackPane.setAlignment(percentageLabel, Pos.CENTER);
        
        // Add drop shadow effect to the bar
        barContainer.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 0);");
        
        // Add all components to the bar container
        barContainer.getChildren().addAll(background, foreground, percentageLabel);
        
        // Add status label and bar container to main container
        container.getChildren().addAll(statusLabel, barContainer);
        
        return container;
    }

    private void showAddWaterPopup() {
        Stage popup = new Stage();
        popup.setTitle("Add Water");

        VBox layout = new VBox();
        layout.setSpacing(10);
        layout.setPadding(new Insets(10));

        Spinner<Integer> waterSpinner = new Spinner<>(0, 100, 10);
        Button addWaterButton = new Button("Add Water");
        addWaterButton.setOnAction(e -> {
            int waterAmount = waterSpinner.getValue();
            totalWaterAdded += waterAmount;
            for (Button gridButton : plantMap.keySet()) {
                Plant plant = plantMap.get(gridButton);
                plant.water(waterAmount);
                
                // Set watering status to true and update immediately
                wateringStatusMap.put(gridButton, true);
                updateGridButtonText(gridButton, plant);

                // Schedule removal of water symbol after 3 seconds
                scheduler.schedule(() -> {
                    Platform.runLater(() -> {
                        wateringStatusMap.remove(gridButton);
                        updateGridButtonText(gridButton, plant);
                    });
                }, 3, TimeUnit.SECONDS);

                // Check for overwatering damage
                if (plant.getCurrentWaterLevel() > 80) {
                    int overWaterDamage = (plant.getCurrentWaterLevel() - 80) / 2; // Damage increases with excess water
                    plant.decreaseHealth(overWaterDamage);
                    log(getCurrentTime() + " ⚠️ WARNING: " + plant.getName() + " is showing signs of overwatering! Health decreased by " +
                        overWaterDamage + "%. Current health: " + plant.getHealth() + "%\n");
                    
                    // Check if plant died from overwatering
                    checkAndHandlePlantDeath(gridButton, plant);
                }

                log(getCurrentTime() + " 💧 WATERING: Added " + waterAmount + " units of water to " + plant.getName() + 
                    ". Current water level: " + plant.getCurrentWaterLevel() + "%\n");
            }
            popup.close();
        });

        // Add warning label
        Label warningLabel = new Label("⚠️ Warning: Overwatering (>80%) can damage plants!");
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        layout.getChildren().addAll(
            new Label("Select Water Amount:"), 
            waterSpinner, 
            warningLabel,
            addWaterButton
        );

        Scene popupScene = new Scene(layout, 300, 200);
        popup.setScene(popupScene);
        popup.show();
    }

    private void showSetTemperaturePopup() {
        Stage popup = new Stage();
        popup.setTitle("Set Temperature");

        VBox layout = new VBox();
        layout.setSpacing(10);
        layout.setPadding(new Insets(10));

        Spinner<Integer> tempSpinner = new Spinner<>(-50, 50, currentTemperature);
        Button setTempButton = new Button("Set Temperature");
        setTempButton.setOnAction(e -> {
            currentTemperature = tempSpinner.getValue();
            currentTempLabel.setText("Current Temp: " + currentTemperature + "°C");
            log(getCurrentTime() + " 🌡️ TEMPERATURE UPDATE: Garden temperature has been adjusted to " + currentTemperature + "°C.\n");
            popup.close();
        });

        layout.getChildren().addAll(new Label("Select Temperature:"), tempSpinner, setTempButton);

        Scene popupScene = new Scene(layout, 300, 200);
        popup.setScene(popupScene);
        popup.show();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    private Plant createPlant(PlantType type, String name) {
        totalPlantsPlanted++;
        List<String> vulnerabilities = pestVulnerabilities.getOrDefault(type.name(), new ArrayList<>());
        Object additionalParam = getAdditionalParamForPlantType(type);

        // Create the plant with the necessary parameters
        Plant plant = PlantFactory.createPlant(
                type,
                name,
                type == PlantType.SUNFLOWER ? 8 : 5, // Higher water requirement for sunflower
                vulnerabilities,
                15, // temperatureToleranceLow
                35, // temperatureToleranceHigh
                additionalParam // Additional parameter based on PlantType
        );

        return plant;
    }

    private Object getAdditionalParamForPlantType(PlantType type) {
        switch (type) {
            case APPLE:
                return 150; // Estimated fruit yield for Apple
            case CHERRY:
                return 200; // Estimated fruit yield for Cherry
            case LAVENDER:
                return "Calming Scent"; // Fragrance for Lavender
            case BAMBOO:
                return 20; // Growth rate in cm/day for Bamboo
            case SUNFLOWER:
                return "Heliotropic"; // Property for Sunflower
            default:
                return null; // No additional parameters required
        }
    }
    

    private void startDayIncrementTimer() {
        Thread dayIncrementThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3600000); // 3600 seconds = 1 hour of real time
                    Platform.runLater(() -> {
                        // Increment the day counter
                        int currentDay = Integer.parseInt(currentDayLabel.getText().replace("Day: ", ""));
                        currentDay++;
                        daysElapsed++;
                        currentDayLabel.setText("Day: " + currentDay);

                        // Log the start of the new day
                        log(getCurrentTime() + " 📅 DAY UPDATE: Day " + currentDay + " has begun! New opportunities await in the garden.\n");

                        // Check system performance every 24 days
                        if (daysElapsed % PERFORMANCE_CHECK_INTERVAL == 0) {
                            getState();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        dayIncrementThread.setDaemon(true);
        dayIncrementThread.start();
    }

    private void getState() {
        // Calculate current statistics
        int currentPlantCount = plantMap.size();
        int healthyPlants = 0;
        int stressedPlants = 0;
        int dyingPlants = 0;
        double averageHealth = 0;
        double averageWaterLevel = 0;

        // Update plant type statistics
        plantStats.clear();
        for (Plant plant : plantMap.values()) {
            String plantType = plant.getPlantType().name();
            plantStats.put(plantType, plantStats.getOrDefault(plantType, 0) + 1);

            // Calculate health statistics
            if (plant.getHealth() >= 80) {
                healthyPlants++;
            } else if (plant.getHealth() >= 50) {
                stressedPlants++;
            } else {
                dyingPlants++;
            }

            averageHealth += plant.getHealth();
            averageWaterLevel += plant.getCurrentWaterLevel();
        }

        // Calculate averages
        if (currentPlantCount > 0) {
            averageHealth /= currentPlantCount;
            averageWaterLevel /= currentPlantCount;
        }

        // Create performance report
        StringBuilder report = new StringBuilder();
        report.append("\n📊 SYSTEM PERFORMANCE REPORT (Day ").append(daysElapsed).append(")\n");
        report.append("==========================================\n");
        report.append("Current Plant Count: ").append(currentPlantCount).append("\n");
        report.append("Total Plants Planted: ").append(totalPlantsPlanted).append("\n");
        report.append("Total Plants Died: ").append(totalPlantsDied).append("\n");
        report.append("Survival Rate: ").append(String.format("%.2f%%", 
            totalPlantsPlanted > 0 ? ((totalPlantsPlanted - totalPlantsDied) * 100.0 / totalPlantsPlanted) : 0)).append("\n");
        report.append("\nHealth Status:\n");
        report.append("- Healthy Plants (>80%): ").append(healthyPlants).append("\n");
        report.append("- Stressed Plants (50-80%): ").append(stressedPlants).append("\n");
        report.append("- Dying Plants (<50%): ").append(dyingPlants).append("\n");
        report.append("- Average Health: ").append(String.format("%.1f%%", averageHealth)).append("\n");
        report.append("\nWater Status:\n");
        report.append("- Average Water Level: ").append(String.format("%.1f%%", averageWaterLevel)).append("\n");
        report.append("- Total Water Added: ").append(totalWaterAdded).append(" units\n");
        report.append("\nPest Control:\n");
        report.append("- Total Pest Controls Applied: ").append(totalPestControlsApplied).append("\n");
        report.append("\nPlant Distribution:\n");
        for (Map.Entry<String, Integer> entry : plantStats.entrySet()) {
            report.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        report.append("\nEnvironmental Conditions:\n");
        report.append("- Current Temperature: ").append(currentTemperature).append("°C\n");
        report.append("==========================================\n");

        // Log the report to both UI and file
        logImportant(report.toString());

        // Show alert for critical conditions
        if (dyingPlants > 0 || averageHealth < 50) {
            showAlert("⚠️ Critical Garden Status", 
                "Warning: Your garden is showing signs of stress!\n" +
                "- " + dyingPlants + " plants are dying\n" +
                "- Average health is " + String.format("%.1f%%", averageHealth) + "\n" +
                "Please take immediate action to improve conditions.");
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void startWaterReductionTimer() {
        Thread waterReductionThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // Every 30 seconds
                    Platform.runLater(() -> {
                        Iterator<Map.Entry<Button, Plant>> iterator = plantMap.entrySet().iterator();
    
                        while (iterator.hasNext()) {
                            Map.Entry<Button, Plant> entry = iterator.next();
                            Button gridButton = entry.getKey();
                            Plant plant = entry.getValue();
    
                            if (plant != null) {
                                // Calculate water reduction based on temperature
                                int baseRate = plant.getWaterRequirement();
                                double temperatureMultiplier;
                                
                                // Temperature affects water consumption more significantly
                                if (currentTemperature > 35) {
                                    temperatureMultiplier = 2.5; // High temperature causes more water loss
                                } else if (currentTemperature > 30) {
                                    temperatureMultiplier = 2.0;
                                } else if (currentTemperature > 25) {
                                    temperatureMultiplier = 1.5;
                                } else if (currentTemperature < 15) {
                                    temperatureMultiplier = 1.2; // Cold temperature also increases water loss
                                } else if (currentTemperature < 10) {
                                    temperatureMultiplier = 1.5;
                                } else {
                                    temperatureMultiplier = 1.0; // Optimal temperature range
                                }
                                
                                int reduction = (int) (baseRate * temperatureMultiplier);
                                plant.decreaseWaterLevel(reduction);
    
                                log(getCurrentTime() + " 💧 WATER STATUS: " + plant.getName() + " water level decreased by " + reduction +
                                        "%. Current water level: " + plant.getCurrentWaterLevel() + "%\n");
    
                                // Start automatic watering if water level is too low
                                if (plant.getCurrentWaterLevel() < 50) { // Changed threshold to 50%
                                    startAutomaticWatering(gridButton);
                                }
    
                                // Health impact based on water level
                                if (plant.getCurrentWaterLevel() < 30) {
                                    // Calculate health impact based on water level
                                    int healthImpact;
                                    if (plant.getCurrentWaterLevel() < 20) {
                                        healthImpact = 4; // Severe health impact
                                    } else if (plant.getCurrentWaterLevel() < 25) {
                                        healthImpact = 3; // Moderate health impact
                                    } else {
                                        healthImpact = 2; // Light health impact
                                    }
                                    
                                    // Additional health impact in extreme temperatures
                                    if (currentTemperature > 35 || currentTemperature < 10) {
                                        healthImpact += 2;
                                    }
                                    
                                    plant.decreaseHealth(healthImpact);
                                    log(getCurrentTime() + " ⚠️ WARNING: " + plant.getName() + " is showing signs of water stress! Health decreased by " +
                                        healthImpact + "%. Current health: " + plant.getHealth() + "%\n");
                                    
                                    updateGridButtonText(gridButton, plant);
                                    checkAndHandlePlantDeath(gridButton, plant);
                                }
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        waterReductionThread.setDaemon(true);
        waterReductionThread.start();
    }
    
    private void startAutomaticWatering(Button gridButton) {
        // Cancel any existing watering task for this button
        ScheduledFuture<?> existingTask = wateringTasks.get(gridButton);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
        }

        // Schedule new watering task
        ScheduledFuture<?> wateringTask = scheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                Plant plant = plantMap.get(gridButton);
                if (plant != null && plant.getCurrentWaterLevel() < 50) {
                    // Calculate and apply watering (existing logic)
                    int targetWaterLevel = 75;
                    int currentLevel = plant.getCurrentWaterLevel();
                    int waterNeeded = targetWaterLevel - currentLevel;
                    
                    double temperatureMultiplier = calculateTemperatureMultiplier();
                    double healthMultiplier = calculateHealthMultiplier(plant);
                    
                    int waterAdded = (int) (waterNeeded * temperatureMultiplier * healthMultiplier);
                    waterAdded = Math.min(waterAdded, 25);
                    plant.water(waterAdded);
                    
                    wateringStatusMap.put(gridButton, true);
                    updateGridButtonText(gridButton, plant);

                    // Schedule removal of water symbol
                    scheduler.schedule(() -> {
                        Platform.runLater(() -> {
                            wateringStatusMap.remove(gridButton);
                            updateGridButtonText(gridButton, plant);
                        });
                    }, 3, TimeUnit.SECONDS);

                    log(getCurrentTime() + " 💦 AUTOMATIC WATERING: Applied " + waterAdded + 
                        " units of water to " + plant.getName() + "\n");
                }
            });
        }, 0, 5, TimeUnit.SECONDS);

        wateringTasks.put(gridButton, wateringTask);
    }
    
    private double calculateTemperatureMultiplier() {
        if (currentTemperature > 35) return 1.5;
        if (currentTemperature > 30) return 1.3;
        if (currentTemperature > 25) return 1.1;
        if (currentTemperature < 15) return 0.9;
        if (currentTemperature < 10) return 0.7;
        return 1.0;
    }

    private double calculateHealthMultiplier(Plant plant) {
        if (plant.getHealth() < 30) return 1.5;
        if (plant.getHealth() < 50) return 1.3;
        return 1.0;
    }
    
    private void checkAndHandlePlantDeath(Button gridButton, Plant plant) {
        if (plant.getHealth() <= 0) {
            totalPlantsDied++;
            // Remove the plant from all maps
            plantMap.remove(gridButton);
            activeInsectsMap.remove(gridButton);
            wateringStatusMap.remove(gridButton);
            pestControlStatusMap.remove(gridButton);

            // Reset the button to its original empty state with enhanced styling
            gridButton.setText("");
            String baseButtonStyle = "-fx-background-color: #A5D6A7; " +
                                   "-fx-border-color: #81C784; " +
                                   "-fx-border-width: 2px; " +
                                   "-fx-background-radius: 8px; " +
                                   "-fx-border-radius: 8px; " +
                                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 0); " +
                                   "-fx-font-size: 16px;";

            String hoverButtonStyle = "-fx-background-color: #81C784; " +
                                    "-fx-border-color: #66BB6A; " +
                                    "-fx-border-width: 2px; " +
                                    "-fx-background-radius: 8px; " +
                                    "-fx-border-radius: 8px; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 0); " +
                                    "-fx-font-size: 16px;";

            gridButton.setStyle(baseButtonStyle);

            // Add hover effects
            gridButton.setOnMouseEntered(e -> gridButton.setStyle(hoverButtonStyle));
            gridButton.setOnMouseExited(e -> gridButton.setStyle(baseButtonStyle));

            // Log the plant's death to both UI and file
            logImportant(getCurrentTime() + " 💀 PLANT LOSS: " + plant.getName() + " has perished at coordinates (" +
                ((int[]) gridButton.getUserData())[0] + ", " + ((int[]) gridButton.getUserData())[1] + 
                "). Plant has been removed from the garden.\n");
        }
    }

    private void adjustUIForWidth(double width) {
        // Adjust button sizes based on window width
        double buttonSize = Math.max(60, Math.min(120, width * 0.08));
        double fontSize = Math.max(12, Math.min(24, width * 0.015));

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Button button = gridButtons[i][j];
                if (button != null) {
                    button.setPrefSize(buttonSize, buttonSize);
                    button.setStyle(button.getStyle() + String.format("-fx-font-size: %.0fpx;", fontSize));
                }
            }
        }

        // Adjust other UI elements
        double sidebarWidth = Math.max(200, Math.min(300, width * 0.2));
        leftSidebar.setPrefWidth(sidebarWidth);
    }

    private void adjustUIForHeight(double height) {
        // Adjust spacing in sidebar based on height
        double spacing = height < 800 ? 10 : 20;
        leftSidebar.setSpacing(spacing);
        
        // Adjust padding
        double padding = Math.max(10, Math.min(20, height * 0.02));
        leftSidebar.setPadding(new Insets(padding));
    }

    private VBox createLogSection() {
        VBox logSection = new VBox(8);
        logSection.setPadding(new Insets(15));
        logSection.setStyle("-fx-background-color: white; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 0); " +
                          "-fx-background-radius: 12px; " +
                          "-fx-border-radius: 12px; " +
                          "-fx-border-color: #81C784; " +
                          "-fx-border-width: 2px;");

        // Create header with icon
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        Label iconLabel = new Label("📝");
        iconLabel.setStyle("-fx-font-size: 22px;");
        
        Label logLabel = new Label("Activity Log");
        logLabel.setFont(new Font("Arial", 22));
        logLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        
        header.getChildren().addAll(iconLabel, logLabel);

        // Configure log area with enhanced styling
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(8);
        logArea.setStyle("-fx-font-family: 'Arial'; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-color: #FAFAFA; " +
                        "-fx-border-color: #81C784; " +
                        "-fx-border-width: 2px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-effect: innershadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Make log area expand to fill available space
        VBox.setVgrow(logArea, Priority.ALWAYS);
        logSection.getChildren().addAll(header, logArea);

        // Set minimum height for the log section
        logSection.setMinHeight(250);
        
        return logSection;
    }

    private void log(String message) {
        // Log to UI
        logArea.appendText(message);
    }

    private void logImportant(String message) {
        // Log to both UI and file
        logArea.appendText(message);
        
        // Log to file
        if (logWriter != null) {
            logWriter.println(message);
            logWriter.flush(); // Ensure immediate writing to file
        }
    }

    public Map<String, Map<String, Object>> getPlants() {
        Map<String, Map<String, Object>> plantDetails = new HashMap<>();
        
        for (Map.Entry<Button, Plant> entry : plantMap.entrySet()) {
            Plant plant = entry.getValue();
            Map<String, Object> details = new HashMap<>();
            
            // Add plant name
            details.put("name", plant.getName());
            
            // Add water requirement
            details.put("waterRequirement", plant.getWaterRequirement());
            
            // Add current water level
            details.put("currentWaterLevel", plant.getCurrentWaterLevel());
            
            // Add health
            details.put("health", plant.getHealth());
            
            // Add vulnerabilities
            List<String> vulnerabilities = pestVulnerabilities.getOrDefault(plant.getPlantType().name(), new ArrayList<>());
            details.put("vulnerabilities", vulnerabilities);
            
            // Add active insects if any
            List<String> activeInsects = activeInsectsMap.getOrDefault(entry.getKey(), new ArrayList<>());
            details.put("activeInsects", activeInsects);
            
            // Add coordinates
            int[] coords = (int[]) entry.getKey().getUserData();
            details.put("coordinates", new int[]{coords[0], coords[1]});
            
            plantDetails.put(plant.getName(), details);
        }
        
        return plantDetails;
    }
}    
