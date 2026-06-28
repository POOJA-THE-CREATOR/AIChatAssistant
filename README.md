# AI Chat Assistant

A production-style Android chatbot that integrates with **Google Gemini** and ships with a built-in **demo mode**, so reviewers and recruiters can run the app instantly without an API key.

Built with **MVVM**, **Clean Architecture**, and modern Android tooling — designed as a portfolio/resume project to demonstrate real-world app structure, networking, persistence, and UI patterns.

---

## Highlights (for reviewers)

- **Works out of the box** — demo mode provides mock AI replies with no setup
- **Real AI ready** — plug in a free Gemini API key to switch to live responses
- **Clean Architecture** — separated `data`, `domain`, and `presentation` layers
- **Offline-first history** — full conversation persisted locally with Room
- **Material chat UI** — RecyclerView with user/bot bubble types, loading state, retry, and clear chat

---

## Demo Mode vs Live Mode

The app supports two modes automatically:

| Mode | API key required? | What happens |
|------|-------------------|--------------|
| **Demo mode** | No | Uses local mock responses (`DemoChatDataSource`) so you can test the full chat flow, UI, and Room persistence without signing up for anything |
| **Live mode** | Yes (free) | Sends conversation history to Google Gemini and displays real AI-generated replies |

**Demo mode is the default.** A banner at the top of the chat screen indicates when demo mode is active.

When a valid Gemini API key is configured, the banner disappears and all new messages use the live Gemini API (with automatic model fallback across `gemini-2.5-flash`, `gemini-2.0-flash`, and `gemini-2.0-flash-lite`).

---

## Getting Real Gemini Responses (Live Mode)

Google does not provide shared or dummy API keys — each developer gets their own **free** key from Google AI Studio.

### Step 1 — Create a free API key

1. Go to [Google AI Studio](https://aistudio.google.com/apikey)
2. Sign in with your Google account
3. Click **Create API key**
4. Copy the key (it starts with `AIza…`)

### Step 2 — Add the key (choose one method)

**Option A — In the app (no rebuild)**

1. Run the app
2. Tap the **lock icon** in the toolbar
3. Paste your key → **Save**
4. Send a message — the app switches to live Gemini responses immediately

**Option B — `local.properties` (build-time)**

1. Copy [`local.properties.example`](local.properties.example) if needed
2. Add your key to `local.properties`:

```properties
sdk.dir=/path/to/your/Android/sdk
GEMINI_API_KEY=AIzaSy_your_actual_key_here
```

3. **File → Sync Project with Gradle Files**
4. Rebuild and run

> **Security:** Never commit `local.properties` or your API key to Git. The file is already git-ignored.

---

## Features

- Chat screen with **RecyclerView** — user messages (right), bot messages (left)
- **Input bar** with send button and keyboard support
- **Loading indicator** while waiting for a response
- **Error handling** with snackbar retry
- **Conversation history** saved in Room DB and restored on app launch
- **Clear chat** from toolbar
- **Edge-to-edge UI** with proper status bar inset handling
- **Demo mode** for zero-config testing

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation                           │
│  MainActivity · ChatFragment · ChatAdapter · ViewModel  │
└─────────────────────────┬───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│                      Domain                             │
│  ChatMessage · ChatRepository · UseCases                │
└─────────────────────────┬───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│                       Data                              │
│  ChatRepositoryImpl · Room · Retrofit (Gemini API)      │
│  DemoChatDataSource (fallback when no API key)          │
└─────────────────────────────────────────────────────────┘
```

**Patterns & principles**

- MVVM with `StateFlow` for UI state
- Repository pattern — single source of truth for chat data
- Use cases for each user action (send, retry, clear, observe)
- Hilt for dependency injection
- ViewBinding (no DataBinding)
- Single Activity + Navigation Component

---

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | XML, ConstraintLayout, Material Design 3, RecyclerView |
| Architecture | MVVM, Clean Architecture |
| DI | Hilt |
| Async | Kotlin Coroutines, StateFlow |
| Networking | Retrofit, OkHttp, Gson |
| Local DB | Room |
| Navigation | Jetpack Navigation Component |
| AI | Google Gemini API (`generativelanguage.googleapis.com`) |

---

## Project Structure

```
app/src/main/java/com/example/aichatassisstant/
├── data/
│   ├── local/          # Room DB, DAO, entities, ApiKeyManager
│   ├── remote/         # Retrofit API, DTOs, Gemini + Demo data sources
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # ChatMessage, MessageRole
│   ├── repository/     # Repository interfaces
│   └── usecase/        # SendMessage, Retry, Clear, Observe
├── presentation/
│   ├── ui/             # MainActivity, ChatFragment, ChatAdapter
│   └── viewmodel/      # ChatViewModel, ChatUiState
└── di/                 # Hilt modules (Database, Network, Repository)
```

---

## Getting Started

### Prerequisites

- Android Studio (latest stable recommended)
- Android SDK — minSdk 24, targetSdk 36
- JDK 11+

### Run the app (demo mode — no key needed)

1. Clone the repository
2. Open the project in Android Studio
3. **File → Sync Project with Gradle Files**
4. Run on an emulator or physical device (**Run ▶**)
5. Send a message — you'll receive a demo reply

That's it. No API key, no account, no configuration required for demo mode.

---

## Skills Demonstrated

This project is intended to showcase:

- Structured Android app architecture (not a single-Activity spaghetti codebase)
- REST API integration with proper error mapping and model fallback
- Local persistence with Room and reactive `Flow` observation
- Modern UI patterns (ViewBinding, Material components, window insets)
- Pragmatic product thinking — demo mode lowers the barrier for anyone evaluating the project

---

## License

This project was built as a personal portfolio/resume application. Feel free to use it as a reference for learning purposes.

---

## Author

Built as a resume/portfolio project demonstrating Android development with Kotlin, Clean Architecture, and Google Gemini integration.
