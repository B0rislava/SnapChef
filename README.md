# SnapChef

> **Reduce food waste. Cook better together.** AI-powered ingredient recognition and collaborative recipe generation for the modern kitchen.

SnapChef is a mobile application that combines artificial intelligence with social collaboration to transform how people plan meals. Snap a photo of your fridge, share your pantry with friends, and let AI suggest recipes that prioritize ingredients nearing expiration.

---

## 1. Key Features

### 1.1 AI Ingredient Recognition
Snap a photo of your kitchen inventory and let our computer vision model instantly identify ingredients. No more manual pantry updates—just point, shoot, and cook.

### 1.2 **Collaborative Groups**
Create shared cooking groups with friends, family, or roommates. Combine individual pantries to unlock group recipes and eliminate duplicate groceries.

### 1.3 **Intelligent Recipe Generation**
Advanced LLMs generate recipes that prioritize ingredients nearing expiration. Each recipe includes:
- Step-by-step cooking instructions
- Nutritional information
- Attribution of which group member provides each ingredient
- Customization based on dietary preferences

### 1.4 **Personalized Recommendations**
Your app learns from saved recipes and interactions to build a personalized feed tailored to your taste, dietary needs, and cooking style.

### 1.5 **Secure Authentication**
Multiple sign-in options including Google Sign-In and email-based registration with secure verification.

---

## 2. Architecture

### 2.1 Frontend
Built with **Kotlin Multiplatform** and **Compose Multiplatform**, enabling:
- Shared business logic across platforms
- Native performance on Android and iOS
- Consistent, modern UI powered by Jetpack Compose

### 2.2 Backend
**Python FastAPI** server handles:
- User and group management
- AI processing and recipe generation
- Real-time inventory synchronization
- Secure RESTful APIs

### 2.3 Data Management
Structured data model tracking:
- User profiles and preferences
- Group memberships and pantries
- Recipe history and ratings
- Real-time synchronization across all devices

---

## 3. System Requirements

| Platform | Minimum Version |
|----------|-----------------|
| Android  | API 24+         |
| iOS      | 15.0+           |
| General  | Internet connection required |

---

## 4. Quick Start

### 4.1 Android
```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Or on Windows
.\gradlew.bat :composeApp:assembleDebug
```

### 4.2 iOS
```bash
# Option 1: Open in Xcode
open iosApp

# Option 2: Build via Gradle
./gradlew :composeApp:iosDeploy
```

---

## 5. How It Works

1. **Capture** → Take a photo of your ingredients
2. **Identify** → AI recognizes and catalogs items
3. **Share** → Invite friends to your cooking group
4. **Generate** → Get recipes optimized for your group's combined pantry
5. **Cook** → Follow step-by-step instructions and track progress
6. **Discover** → Get personalized recipe recommendations over time

---

## 6. Sustainability Impact

SnapChef addresses food waste in two ways:
- **Ingredient Prioritization**: Recipes suggest using ingredients that expire soon
- **Smart Sharing**: Group planning reduces duplicate purchases and grocery waste
- **Real-time Tracking**: Know exactly what you have to avoid buying duplicates

---

## 7. HackTUES 12 Submission

**Theme**: Code to Care  
**Focus**: Zero Left

SnapChef tackles food waste reduction through intelligent inventory management and community-driven meal planning. By leveraging AI and social features, we empower users to cook smarter, waste less, and enjoy better meals together.

---

