# Storizon

Storizon is an Android application where users can share short stories, similar to a social media platform. Users can optionally include their location in their stories, which will be displayed on an interactive map powered by Google Maps API. The app supports night mode and multiple languages, enhancing user experience. Additionally, it tracks daily login streaks and the word count of stories saved locally using Room Database. Users can register, log in, and manage their accounts seamlessly.

## Screenshots

![Screenshots](https://github.com/user-attachments/assets/044cdc42-8a78-4d27-a18a-29e51edc7be8)

## Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [App Structure](#app-structure)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Features
- **Story Sharing**: Share short stories with optional location tagging.
- **Interactive Map**: View user stories pinned on a map using Google Maps API.
- **Daily Login Streaks**: Track daily app usage with streak counters.
- **Word Count Tracker**: Monitor the number of words written in stories.
- **Dark Mode**: Switch between light and dark themes.
- **Multi-Language Support**: Choose from multiple languages for a personalized experience.
- **User Authentication**: Register and log in to manage your account securely.

## Technologies Used
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Data Binding**: LiveData, ViewModel
- **Networking**: Retrofit
- **Local Storage**: Room Database
- **Map Integration**: Google Maps API
- **UI Components**: RecyclerView, Material Components, View Binding
- **Testing**: JUnit, Espresso
- **Others**: AndroidX, Navigation Component, Dark Mode, SharedPreferences

## Architecture
Storizon follows the MVVM (Model-View-ViewModel) architecture pattern for clean separation of concerns, better organization, and ease of testing:
- **Model**: Handles data operations (e.g., Room Database and API calls).
- **View**: Displays data on the UI and observes ViewModel changes.
- **ViewModel**: Acts as a bridge between the View and the Model, managing UI-related data and business logic.

## Getting Started
To set up the project locally, follow these steps:

### Prerequisites
Ensure you have the following installed:
- Android Studio (latest stable version recommended)
- Git (for cloning the repository)
- Google Maps API Key (required for map functionality)

### Installation
1. Clone the repository:
   
   ```bash
   git clone https://github.com/your-repository/storizon.git
   ```

2. Open the project in Android Studio:
   - Open Android Studio.
   - Select "Open an existing Android Studio project."
   - Navigate to the cloned repository and select it.

3. Configure the Google Maps API Key:
   - Obtain a key from the [Google Cloud Console](https://console.cloud.google.com/).
   - Add the key to your `local.properties` file:
     ```
     MAPS_API_KEY=your_api_key
     ```

4. Build the project:
   - Android Studio will automatically build the project.
   - If the build does not start automatically, select **Build > Make Project** from the menu.

5. Run the app:
   - Connect an Android device or use an emulator.
   - Click the green play button to run the app.

## App Structure
- **Home**: Displays user stories and provides access to the map view.
- **Map View**: Shows pins for stories with location tags on an interactive map.
- **Profile**: Allows users to manage their accounts, toggle dark mode, and change the app language.
- **Settings**: Provides access to account settings and logout functionality.

## Usage
1. **Register/Login**: Create an account or log in to access features.
2. **Share Stories**: Post short stories with optional location tags.
3. **Explore Stories**: View stories shared by others on the home page or map view.
4. **Track Progress**: Check your daily login streaks and word count.
5. **Personalize Settings**: Enable dark mode and switch languages.

## Contributing
Contributions are welcome! Follow these steps to contribute:
1. Fork the repository.
2. Create a new branch:
   
   ```bash
   git checkout -b feature/your-feature
   ```

3. Make your changes.
4. Commit your changes:
   
   ```bash
   git commit -m 'Add some feature'
   ```

5. Push to the branch:
   
   ```bash
   git push origin feature/your-feature
   ```

6. Create a pull request.

## License
This project is licensed under the MIT License. See the LICENSE file for details.

## Contact
- **Author**: Rezky Aditia Fauzan
- **Email**: rezkyaf246@gmail.com
- **GitHub**: https://github.com/zyrridian
