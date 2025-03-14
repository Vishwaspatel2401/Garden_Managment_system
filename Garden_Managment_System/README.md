# Garden Simulation System 🌱

A Java-based interactive garden simulation that allows users to create and manage a virtual garden with various plants, insects, and environmental conditions.

## Features 🌟

### Plant Management
- Plant different types of plants:
  - 🍏 Apple Trees
  - 🍒 Cherry Trees
  - 🌸 Lavender
  - 🎍 Bamboo
  - 🌻 Sunflowers
- Each plant has unique characteristics:
  - Water requirements
  - Temperature tolerance
  - Growth patterns
  - Pest vulnerabilities

### Environmental Controls
- Temperature control (10°C to 35°C)
- Water management system
- Automatic watering when water levels drop below 50%
- Health monitoring and recovery system

### Pest Management
- Insect attacks from:
  - 🪲 Aphids
  - 🐜 Ants
  - 🦗 Grasshoppers
  - 🐞 Ladybugs
- Automatic pest control system
- Manual pest control options
- Plant-specific pest vulnerabilities

### Real-time Monitoring
- Live activity log
- Plant health status
- Water levels
- Temperature conditions
- Day/night cycle simulation

## Technical Requirements 🛠️

- Java Development Kit (JDK) 11 or higher
- JavaFX library
- Maven (for dependency management)

## Installation 📥

1. Clone the repository:
```bash
git clone https://github.com/yourusername/garden-simulation.git
```

2. Navigate to the project directory:
```bash
cd Project
```

3. Build the project using Maven:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn javafx:run
```

## Terminal Commands 💻

### For Windows:
```bash
# Navigate to project directory
cd Project

# Clean and build the project
mvn clean install

# Run the application
mvn javafx:run
```

### For macOS/Linux:
```bash
# Navigate to project directory
cd Project

# Clean and build the project
mvn clean install

# Run the application
mvn javafx:run
```

### Alternative Command (if Maven wrapper is available):
```bash
# For Windows
.\mvnw javafx:run

# For macOS/Linux
./mvnw javafx:run
```

### Common Terminal Issues:
1. If you get "command not found" for mvn:
   - Ensure Maven is installed and added to your system's PATH
   - Try using the full path to Maven: `/path/to/maven/bin/mvn javafx:run`

2. If you get JavaFX-related errors:
   - Make sure JavaFX is properly installed
   - Try running with explicit module path:
   ```bash
   mvn javafx:run -Djavafx.sdk=/path/to/javafx-sdk
   ```

## Starting the Application 🚀

### Method 1: Using Maven (Recommended)
1. Open a terminal/command prompt
2. Navigate to the project directory
3. Run the following command:
```bash
mvn javafx:run
```

### Method 2: Using IDE (IntelliJ IDEA)
1. Open the project in IntelliJ IDEA
2. Navigate to `src/main/java/com/project`
3. Right-click on the file
4. Select "Run 'GardenSimulation.main()'"

### Method 3: Using IDE (Eclipse)
1. Open the project in Eclipse
2. Navigate to `src/main/java/com/project`
3. Right-click on the file
4. Select "Run As" → "Java Application"

### Troubleshooting Common Issues 🔧

If you encounter any issues while starting the application:

1. **JavaFX not found error**
   - Ensure JavaFX is properly installed
   - Check if the JavaFX module path is correctly set in your IDE
   - Verify the JavaFX dependencies in your `pom.xml`

2. **Maven build fails**
   - Run `mvn clean` to clear any cached files
   - Check if all dependencies are properly downloaded
   - Verify your Maven installation

3. **Application window not showing**
   - Check if another instance is already running
   - Verify your display settings
   - Check the console for any error messages

4. **Performance issues**
   - Ensure you have sufficient system resources
   - Close other resource-intensive applications
   - Check your Java heap size settings

## Usage Guide 📖

### Starting the Simulation
1. Launch the application
2. Select a plant type from the sidebar
3. Click on any empty grid cell to plant
4. Monitor plant health and water levels

### Managing Your Garden
- Use the "Water Garden" button to add water to all plants
- Adjust temperature using the "Set Temperature" button
- Monitor insect attacks and apply pest control as needed
- Watch the activity log for real-time updates

### Plant Care Tips
- Keep water levels between 40-80% for optimal health
- Maintain temperature between 20-30°C for best growth
- Monitor for insect attacks and apply pest control promptly
- Different plants have different water requirements

## Project Structure 📁

```
garden-simulation/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── project/
│                   ├── controller/
│                   │   └── GardenSimulation.java
│                   ├── factory/
│                   │   ├── PlantFactory.java
│                   │   └── PlantType.java
│                   ├── modules/
│                   │   ├── Plant.java
│                   │   ├── Insect.java
│                   │   ├── Sunflower.java
│                   │   └── ... (other plant classes)
│                   └── logger/
│                       └── Logger.java
└── pom.xml
```

## Contributing 🤝

Contributions are welcome! Please feel free to submit a Pull Request.

## License 📄

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments 🙏

- JavaFX for the GUI framework
- All contributors who have helped with the project
- The gardening community for inspiration

## Support 💬

If you encounter any issues or have questions, please open an issue in the GitHub repository. 