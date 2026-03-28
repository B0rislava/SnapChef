# SnapChef

SnapChef is a mobile application designed to simplify the cooking experience through artificial intelligence and social collaboration. The application allows users to scan ingredients using their camera, manage shared cooking groups, and receive personalized recipe suggestions based on available inventory.

## Core Features

### AI Ingredient Recognition
Users can take photos of their kitchen inventory. The integrated AI identifies ingredients and automatically populates the user's digital pantry, reducing manual entry time and improving accuracy.

### Collaborative Groups
The platform supports social cooking through the creation of shared groups. Members of a group can combine their individual pantries to receive collective recipe suggestions. This feature is designed to facilitate communal meal planning and reduce food waste.

### Intelligent Recipe Generation
By leveraging advanced large language models, SnapChef generates recipes that prioritize ingredients nearing their expiration. The system provides step-by-step instructions, nutritional information, and highlights which group member provides each ingredient.

### Personalized Recommendations
The application learns from user interactions and saved recipes to provide a personalized feed of culinary suggestions tailored to individual preferences and dietary needs.

### Secure Authentication
SnapChef supports multiple authentication methods including Google Sign-In and traditional email-based registration with secure verification processes.

## Technical Architecture

### Frontend
The mobile client is built using Kotlin Multiplatform and Compose Multiplatform. This architecture allows for shared business logic while maintaining high-performance, platform-specific user experiences for both Android and iOS devices.

### Backend
The backend infrastructure is powered by a Python-based FastAPI server. It manages the database, handles AI processing requests, and provides secure RESTful APIs for the mobile client.

### Data Management
The project uses a structured data model to track user profiles, group memberships, and recipe history. Real-time synchronisation ensures that group inventories are always up-to-date across all member devices.

## Requirements

- Android API level 24 or higher
- iOS 15.0 or higher
- Internet connectivity for AI processing and synchronization

## Build Instructions

### Android
To build the Android application, use the Gradle wrapper:
- Windows: .\gradlew.bat :composeApp:assembleDebug
- macOS/Linux: ./gradlew :composeApp:assembleDebug

### iOS
The iOS application can be built by opening the /iosApp directory in Xcode or using the Gradle task :composeApp:iosDeploy.

---

SnapChef is developed for the HackTues competition, focusing on sustainable living and social technology.