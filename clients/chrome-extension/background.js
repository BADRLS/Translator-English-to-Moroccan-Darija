/**
 * Chrome Extension Manifest V3 Background Service Worker.
 * - Registers the right-click Context Menu item for selected text.
 * - Opens the Chrome Side Panel (chrome.sidePanel) on toolbar click or context menu click.
 * - Automatically copies the selected text to the system clipboard and dispatches it
 *   to the Side Panel for immediate translation against the Java REST API.
 */

const CONTEXT_MENU_ID = "translate-selection-to-darija";

chrome.runtime.onInstalled.addListener(() => {
  // Allow clicking the extension action icon to open the Side Panel directly
  chrome.sidePanel
    .setPanelBehavior({ openPanelOnActionClick: true })
    .catch((err) => console.error("sidePanel behavior error:", err));

  // Create right-click context menu for selected text
  chrome.contextMenus.create({
    id: CONTEXT_MENU_ID,
    title: "Translate \"%s\" to Moroccan Darija",
    contexts: ["selection"]
  });
});

chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  if (info.menuItemId !== CONTEXT_MENU_ID || !info.selectionText) {
    return;
  }

  const selectedText = info.selectionText.trim();

  // 1. Open the Side Panel in the current window alongside the main webpage content
  if (tab && tab.windowId) {
    try {
      await chrome.sidePanel.open({ windowId: tab.windowId });
    } catch (e) {
      console.warn("Could not auto-open sidePanel:", e);
    }
  }

  // 2. Copy the selected text to the system clipboard in the active tab
  if (tab && tab.id) {
    try {
      await chrome.scripting.executeScript({
        target: { tabId: tab.id },
        func: (textToCopy) => {
          navigator.clipboard.writeText(textToCopy).catch(() => {});
        },
        args: [selectedText]
      });
    } catch (e) {
      // Restricted pages (e.g. chrome://) may disallow content script injection
    }
  }

  // 3. Persist pending selection in chrome.storage.local so Side Panel receives it even if cold-started
  await chrome.storage.local.set({
    pendingTranslationText: selectedText,
    pendingTimestamp: Date.now()
  });

  // 4. Broadcast runtime message to Side Panel if already open
  chrome.runtime.sendMessage({
    type: "AUTO_PASTE_AND_TRANSLATE",
    text: selectedText
  }).catch(() => {
    // Side panel is still initializing; it will read pendingTranslationText from storage on load
  });
});

