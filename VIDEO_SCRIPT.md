# 5-Minute Video Presentation Script
## LLM-Powered RESTful Web Service for Moroccan Darija Translation

**Total Target Duration:** 05:00 minutes  
**Presenter:** Badr Ejjably  
**Course:** Advanced & Distributed Paradigms — Mini Project 2  

---

### Timeline Overview
| Timestamp | Segment | Focus Area |
| :--- | :--- | :--- |
| **00:00 – 00:40** | Introduction & Problem Statement | Objectives, Scope, and Tech Stack |
| **00:40 – 01:50** | 3-Tier Architecture Walkthrough | Presentation, Logic, Persistence & Security Code |
| **01:50 – 02:40** | Server Startup & Endpoint Security Testing | Grizzly start, cURL/Postman demo, 401 vs 200 OK |
| **02:40 – 03:50** | Chrome Extension Live Demo | SidePanel API, Context Menu, Auto-Paste, Read-Aloud |
| **03:50 – 04:40** | Multi-Client Showcase | Python GUI/CLI, PHP App, React Native Mobile |
| **04:40 – 05:00** | Conclusion & GitHub Repository | Summary of Deliverables & Wrap-Up |

---

## [00:00 – 00:40] Scene 1: Introduction & Problem Statement

#### 🎬 Visual On Screen:
* Display the project title slide or the GitHub repository homepage: [`BADRLS/Translator-English-to-Moroccan-Darija`](https://github.com/BADRLS/Translator-English-to-Moroccan-Darija).
* Show the project architecture diagram from the `README.md`.

#### 🎙️ Spoken Script:
> *"Hello everyone and welcome! In this presentation, I am presenting Mini Project 2: an **LLM-powered RESTful Web Service** designed to translate text into **Moroccan Arabic Dialect**, commonly known as **Darija**.*
>
> *Unlike standard Arabic, Darija is a spoken dialect with unique vocabulary and syntax, often written in both Arabic script and Latin Arabizi numbers. To solve this, our system connects modern Large Language Models—specifically **Google Gemini**—with a robust, enterprise-grade Java backend.*
>
> *The project strictly respects Client-Server separation and implements a rigorous **3-tier architecture**, secured with the **Jakarta Authentication Specification**, and consumed across four distinct clients: a **Chrome Extension with the Manifest V3 SidePanel API**, a **Python Desktop GUI**, a **PHP web application**, and a **React Native mobile app**."*

---

## [00:40 – 01:50] Scene 2: 3-Tier Backend Architecture Walkthrough

#### 🎬 Visual On Screen:
* Open your IDE (VS Code / IntelliJ) with the project tree visible.
* Expand `backend/src/main/java/com/darija/translator/`.
* Briefly highlight the files:
  1. `presentation/TranslatorResource.java` and `BasicAuthFilter.java`
  2. `logic/TranslatorService.java` and `GeminiClient.java`
  3. `persistence/TranslationDao.java` and `InMemoryTranslationDao.java`

#### 🎙️ Spoken Script:
> *"Let us examine the backend codebase. As required by the architectural guidelines, the system is separated into three distinct layers:*
>
> *First, the **Presentation Layer**: Here in `TranslatorResource.java`, we expose the Jakarta RESTful Web Service endpoint under `/api/translator`. The core business method `translate()` consumes JSON requests and returns structured translations with Arabic script, Latin Arabizi, and phonetic pronunciation.*
>
> *For security, we implemented `BasicAuthFilter.java`. This class implements the **Jakarta Authentication Specification** via `ServerAuthModule` as well as a JAX-RS `ContainerRequestFilter`. It intercepts every incoming request and validates HTTP Basic Authentication headers against realm credentials before routing.*
>
> *Second, the **Logic (Business) Layer**: Orchestrated by `TranslatorService.java`, this tier isolates all business rules. It delegates LLM inference to `GeminiClient.java`, which sends structured JSON prompts to Google's Gemini Flash model and handles error recovery with a local Darija linguistic fallback. We also implemented `SpeechExtensionService.java` for the bonus features: Voice-to-Voice translation and W3C SSML-based Read-Aloud metadata generation.*
>
> *Third, the **Persistence Layer**: Defined by `TranslationDao.java` and implemented in `InMemoryTranslationDao.java`. Every translation transaction is logged in a thread-safe record store containing UUIDs, caller identity, and timestamps, fulfilling the 3-tier contract without requiring external database dependencies."*

---

## [01:50 – 02:40] Scene 3: Server Startup & Endpoint Security Testing

#### 🎬 Visual On Screen:
* Open a terminal in `TranslationApp/`.
* Run `.\run-backend.ps1` (or double-click `run-backend.bat`).
* Show the terminal output: *Grizzly HTTP server listening on `http://0.0.0.0:8080/api/translator`*.
* Open PowerShell or Postman side-by-side to test the endpoints.

#### 🎙️ Spoken Script:
> *"Now let's launch the backend. We use an embedded container—**Eclipse Jersey deployed on Grizzly2 HTTP Server** via `ServerMain.java`.*
>
> *I run `./run-backend.ps1`, and within seconds the Grizzly server is active on port 8080.*
>
> *To test the endpoint security, let's attempt an unauthenticated GET request first:*
> `curl -i http://localhost:8080/api/translator/translate?text=Hello`
>
> *As expected, the server immediately rejects the request with **HTTP 401 Unauthorized** and returns the `WWW-Authenticate: Basic` header.*
>
> *Now, let's supply valid Basic Auth credentials (`admin:password123`) in a POST request with the text 'organization':*
>
> *Instantly, we receive **HTTP 200 OK** with a JSON response: `darijaArabic` is 'منظمة', `darijaLatin` is 'mounaddama', alongside the model metadata. When we query `/api/translator/history`, we can see the persistence layer successfully logged the record."*

---

## [02:40 – 03:50] Scene 4: Chrome Extension (Manifest V3 Side Panel)

#### 🎬 Visual On Screen:
* Open Google Chrome with Wikipedia or a news article.
* Highlight a sentence on the page (e.g., *"The university library is open until midnight every day"*).
* Right-click the text and select **"Translate '[selection]' to Moroccan Darija"**.
* Show the Chrome Side Panel automatically sliding open on the right side of the window, auto-pasting the text, and displaying the translated Arabic script, Arabizi, and pronunciation guide.
* Click the **🔊 Read Aloud** button to demonstrate voice output.

#### 🎙️ Spoken Script:
> *"Next, let's demonstrate the headline client: the **Chrome Extension built with Manifest V3**.*
>
> *Notice that we use Chrome's modern **SidePanel API** (`chrome.sidePanel`), allowing the translation interface to live seamlessly alongside any webpage rather than disappearing in a popup.*
>
> *Watch this workflow: I browse an article and highlight any text. I simply right-click to open the context menu and select 'Translate to Moroccan Darija'.*
>
> *The background service worker immediately captures the selection, copies it to the system clipboard, opens the side panel, and initiates the authenticated API request. Within a second, we have the complete translation: Arabic script, Moroccan Arabizi, and a phonetic pronunciation guide.*
>
> *As a bonus extension, I can click **Read Aloud**, and the browser synthesizes authentic Moroccan pronunciation using speech synthesis."*

---

## [03:50 – 04:40] Scene 5: Multi-Platform Clients (Python, PHP, React Native)

#### 🎬 Visual On Screen:
* **Python Desktop App (15s)**: In terminal, run `python translator_client.py`. Type a phrase (e.g., *"Thank you very much"*), click **Translate**, and show the resulting fields.
* **PHP App (15s)**: Show the PHP interface in browser (`localhost:8000`), submit a sentence, and show the cURL response with Darija output.
* **React Native (15s)**: Show the React Native app running in Expo Web / Mobile preview. Type text and tap **Translate**.

#### 🎙️ Spoken Script:
> *"In addition to the browser extension, the service is consumed by three other client platforms to prove architectural decoupling:*
>
> *First, here is the **Python Client**. It comes with both a CLI mode and a desktop GUI built with Tkinter. When I enter 'Thank you very much' and translate, it queries our Java backend using standard HTTP libraries with Base64 authentication headers, returning 'Chokran bezzaf'.*
>
> *Second, we have the **PHP Client**, which uses PHP cURL with basic auth to send requests and render the Moroccan Darija output with built-in voice reader controls.*
>
> *And third, our **React Native Mobile App** running on Expo. It provides a touch-friendly interface with automatic environment detection for Android emulators and web preview, bringing Darija translation to mobile devices."*

---

## [04:40 – 05:00] Scene 6: Conclusion & Repository

#### 🎬 Visual On Screen:
* Switch back to the GitHub Repository: `BADRLS/Translator-English-to-Moroccan-Darija`.
* Scroll through the `README.md` showing the architecture diagrams, file structure, and documentation.
* Show the final slide with your name, student ID, and project details.

#### 🎙️ Spoken Script:
> *"To summarize: we have built a complete, production-ready translation system that fulfills every requirement: a strictly separated 3-tier Jakarta REST service, embedded container deployment, Jakarta specification basic authentication, Google Gemini LLM integration, and four fully functional clients.*
>
> *All source code, commit history, and detailed documentation are publicly available on GitHub at the link provided below.*
>
> *Thank you very much for watching!"*

---

### 💡 Pro-Tips for Recording Your Video
1. **Resolution**: Record at 1080p (1920x1080) at 30 or 60 fps using OBS Studio or Windows Game Bar (`Win + G`).
2. **Audio**: Use a headset or external microphone; ensure background noise is minimal.
3. **Pre-launch Windows**: Before hitting record, have your terminal open, Chrome open with the extension loaded, and your IDE opened to save time during transitions.
4. **Pacing**: Speak at a steady, confident pace. The script above is calculated for approximately **130 words per minute**, fitting the 5-minute requirement.
