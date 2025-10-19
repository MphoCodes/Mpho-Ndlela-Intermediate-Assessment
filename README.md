# TodoWeather App  
**By Mpho Ndlela – Glucode Intermediate Android Developer Assessment**

---

## Overview  
**TodoWeather** is an Android application that combines a task manager with real-time weather data.  
Built with **Jetpack Compose**, **MVVM architecture**, and **Room + Retrofit**, it demonstrates clean architecture, asynchronous data handling, and a modern Material 3 UI.

🎥 **Watch the demo video here:**  
[▶️ View Demo on GitHub](https://github.com/MphoCodes/Mpho-Ndlela-Intermediate-Assessment/blob/main/glucode-demo.mp4)

---

## Features  

### Task Management  
- Create, view, delete, and complete tasks  
- Priority levels (High, Medium, Low) with color indicators  
- Persistent local storage using Room  
- Live UI updates using StateFlow  

### Weather Integration  
- Real-time weather via GPS or manual location input  
- Displays temperature, humidity, wind, and conditions  
- Handles location permissions and network errors gracefully  

### UI & Experience  
- Material Design 3 with dynamic theming  
- Animated splash screen with Glucode branding  
- Tab-based navigation between Tasks and Weather  
- Responsive layouts for multiple screen sizes  

---

## Technical Stack  
- **Architecture:** MVVM + Repository pattern  
- **UI:** Jetpack Compose, Material 3  
- **Database:** Room (with TypeConverters)  
- **Networking:** Retrofit, OkHttp, Gson  
- **Async:** Kotlin Coroutines, StateFlow  
- **Location:** FusedLocationProviderClient  
- **CI/CD:** GitHub Actions (build + test + APK artifact)  

---

## Future Improvements  
- 7-day weather forecast  

---

**Developed by Mpho Ndlela**  
_For Glucode Intermediate Android Engineer Assessment_
