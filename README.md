# AI Chat Assisstant

Android chatbot app powered by Google Gemini (`gemini-1.5-flash`), built with MVVM + Clean Architecture.

## Prerequisites

- Android Studio (latest stable)
- Android SDK (API 24+)
- A free Gemini API key

## Gemini API key setup

1. Open [Google AI Studio](https://aistudio.google.com/apikey).
2. Sign in with your Google account.
3. Click **Create API key**.
4. Copy the generated key.

## Configure the project

Open `local.properties` in the project root (create it from the template if needed) and add your key:

```properties
sdk.dir=/path/to/your/Android/sdk
GEMINI_API_KEY=your_actual_api_key_here
```

A template is provided in [`local.properties.example`](local.properties.example).

> **Important:** Never commit `local.properties` or your API key to version control.

Gradle reads `GEMINI_API_KEY` at build time and exposes it via `BuildConfig.GEMINI_API_KEY`.

## Run the app

1. Open the project in Android Studio.
2. **File → Sync Project with Gradle Files**
3. Run on a device or emulator (**Run ▶**).

## Features

- Chat UI with user (right) and bot (left) message bubbles
- Conversation history persisted in Room across sessions
- Loading indicator while waiting for Gemini
- Error handling with retry
- Clear chat from the toolbar

## Tech stack

- MVVM + Clean Architecture + Repository pattern
- Hilt, Kotlin Coroutines, StateFlow
- Retrofit + OkHttp (Gemini API)
- Room
- XML layouts, ViewBinding, Navigation Component
