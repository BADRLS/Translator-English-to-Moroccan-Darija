/**
 * Side Panel Client Logic for the Moroccan Darija Translator Chrome Extension.
 * - Listens for right-click context menu selections and automatically pastes & translates.
 * - Authenticates with the Jakarta RESTful Web Service using HTTP Basic Auth.
 * - Supports Read-Aloud (TTS) and Speech-to-Darija (Voice-to-Voice) bonus extensions.
 */

const sourceTextEl = document.getElementById("sourceText");
const translateBtn = document.getElementById("translateBtn");
const pasteBtn = document.getElementById("pasteBtn");
const micBtn = document.getElementById("micBtn");
const speakArabicBtn = document.getElementById("speakArabicBtn");
const copyResultBtn = document.getElementById("copyResultBtn");
const statusBanner = document.getElementById("statusBanner");
const resultCard = document.getElementById("resultCard");

const darijaArabicOutput = document.getElementById("darijaArabicOutput");
const darijaLatinOutput = document.getElementById("darijaLatinOutput");
const phoneticOutput = document.getElementById("phoneticOutput");
const modelMeta = document.getElementById("modelMeta");

const toggleSettingsBtn = document.getElementById("toggleSettingsBtn");
const settingsPanel = document.getElementById("settingsPanel");
const apiUrlInput = document.getElementById("apiUrlInput");
const usernameInput = document.getElementById("usernameInput");
const passwordInput = document.getElementById("passwordInput");
const saveSettingsBtn = document.getElementById("saveSettingsBtn");

function showStatus(message, isError = false) {
  statusBanner.textContent = message;
  statusBanner.style.background = isError ? "#7f1d1d" : "#1e3a8a";
  statusBanner.classList.remove("hidden");
}

function hideStatus() {
  statusBanner.classList.add("hidden");
}

function getBasicAuthHeader() {
  const user = usernameInput.value.trim() || "admin";
  const pass = passwordInput.value || "password123";
  return "Basic " + btoa(`${user}:${pass}`);
}

async function performTranslation(textToTranslate) {
  const text = (textToTranslate !== undefined ? textToTranslate : sourceTextEl.value).trim();
  if (!text) {
    showStatus("Please enter or select text to translate.", true);
    return;
  }

  sourceTextEl.value = text;
  showStatus("Translating to Moroccan Darija via Gemini LLM...");
  translateBtn.disabled = true;

  try {
    const baseUrl = apiUrlInput.value.replace(/\/+$/, "");
    const response = await fetch(`${baseUrl}/translate`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": getBasicAuthHeader()
      },
      body: JSON.stringify({
        text: text,
        sourceLanguage: "English",
        includeArabizi: true
      })
    });

    if (!response.ok) {
      const errText = await response.text();
      throw new Error(`HTTP ${response.status}: ${errText}`);
    }

    const data = await response.json();
    darijaArabicOutput.textContent = data.darijaArabic || "";
    darijaLatinOutput.textContent = data.darijaLatin || "";
    phoneticOutput.textContent = data.phoneticGuide || "";
    modelMeta.textContent = `Model: ${data.modelUsed || "gemini-1.5-flash"}`;

    resultCard.classList.remove("hidden");
    hideStatus();
  } catch (err) {
    showStatus(`Translation error: ${err.message}`, true);
  } finally {
    translateBtn.disabled = false;
  }
}

// 1. Check storage on Side Panel startup for any text selected via right-click Context Menu
async function checkPendingSelection() {
  const stored = await chrome.storage.local.get([
    "pendingTranslationText",
    "apiUrl",
    "apiUser",
    "apiPass"
  ]);

  if (stored.apiUrl) apiUrlInput.value = stored.apiUrl;
  if (stored.apiUser) usernameInput.value = stored.apiUser;
  if (stored.apiPass) passwordInput.value = stored.apiPass;

  if (stored.pendingTranslationText) {
    const text = stored.pendingTranslationText;
    await chrome.storage.local.remove(["pendingTranslationText", "pendingTimestamp"]);
    sourceTextEl.value = text;
    await performTranslation(text);
  }
}

// 2. Listen for live right-click selections while the Side Panel is already open
chrome.runtime.onMessage.addListener((message) => {
  if (message && message.type === "AUTO_PASTE_AND_TRANSLATE" && message.text) {
    chrome.storage.local.remove(["pendingTranslationText", "pendingTimestamp"]);
    sourceTextEl.value = message.text;
    performTranslation(message.text);
  }
});

// 3. UI Event Listeners
translateBtn.addEventListener("click", () => performTranslation());

pasteBtn.addEventListener("click", async () => {
  try {
    const clipText = await navigator.clipboard.readText();
    if (clipText) {
      sourceTextEl.value = clipText;
      performTranslation(clipText);
    }
  } catch (e) {
    showStatus("Clipboard permission needed or paste with Ctrl+V.", true);
  }
});

// Task 3 Extension: Read-Aloud (TTS) using backend /tts metadata + Web Speech Synthesis
speakArabicBtn.addEventListener("click", async () => {
  const arabic = darijaArabicOutput.textContent;
  const latin = darijaLatinOutput.textContent;
  if (!arabic && !latin) return;

  try {
    const baseUrl = apiUrlInput.value.replace(/\/+$/, "");
    await fetch(`${baseUrl}/tts`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": getBasicAuthHeader()
      },
      body: JSON.stringify({ darijaArabic: arabic, darijaLatin: latin })
    });
  } catch (_) {}

  window.speechSynthesis.cancel();
  const utterance = new SpeechSynthesisUtterance(arabic || latin);
  utterance.lang = "ar-MA";
  utterance.rate = 0.92;
  window.speechSynthesis.speak(utterance);
});

// Task 3 Extension: Speech-to-Darija (Voice-to-Voice translation)
micBtn.addEventListener("click", () => {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) {
    showStatus("Web Speech API is not supported in this browser context.", true);
    return;
  }

  const recognition = new SpeechRecognition();
  recognition.lang = "en-US";
  recognition.interimResults = false;

  showStatus("Listening... Speak in English to translate and read aloud in Darija.");

  recognition.onresult = async (event) => {
    const spokenTranscript = event.results[0][0].transcript;
    sourceTextEl.value = spokenTranscript;
    await performTranslation(spokenTranscript);
    speakArabicBtn.click(); // Complete voice-to-voice loop
  };

  recognition.onerror = (event) => {
    showStatus(`Microphone error: ${event.error}`, true);
  };

  recognition.start();
});

copyResultBtn.addEventListener("click", async () => {
  const combined = `${darijaArabicOutput.textContent}\n(${darijaLatinOutput.textContent})`;
  await navigator.clipboard.writeText(combined);
  showStatus("Copied Darija translation to clipboard!");
  setTimeout(hideStatus, 2000);
});

toggleSettingsBtn.addEventListener("click", () => {
  settingsPanel.classList.toggle("hidden");
});

saveSettingsBtn.addEventListener("click", async () => {
  await chrome.storage.local.set({
    apiUrl: apiUrlInput.value.trim(),
    apiUser: usernameInput.value.trim(),
    apiPass: passwordInput.value
  });
  settingsPanel.classList.add("hidden");
  showStatus("Saved API settings.");
  setTimeout(hideStatus, 1500);
});

document.addEventListener("DOMContentLoaded", checkPendingSelection);

