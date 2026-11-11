import React, { useState } from "react";
import {
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  ActivityIndicator,
  ScrollView,
  Alert,
  Platform
} from "react-native";
import * as Speech from "expo-speech";

/**
 * React Native Mobile & Web Client consuming the Jakarta RESTful Web Service
 * (TranslatorResource) secured with HTTP Basic Authentication.
 *
 * Automatically detects web/iOS (localhost:8080) vs Android emulator (10.0.2.2:8080).
 */
const DEFAULT_API_BASE =
  Platform.OS === "android"
    ? "http://10.0.2.2:8080/api/translator"
    : "http://localhost:8080/api/translator";

const encodeBasicAuth = (username, password) => {
  const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
  const str = `${username}:${password}`;
  let output = "";
  for (
    let block = 0, charCode, i = 0, map = chars;
    str.charAt(i | 0) || ((map = "="), i % 1);
    output += map.charAt(63 & (block >> (8 - (i % 1) * 8)))
  ) {
    charCode = str.charCodeAt((i += 3 / 4));
    block = (block << 8) | charCode;
  }
  return `Basic ${output}`;
};

export default function App() {
  const [apiUrl, setApiUrl] = useState(DEFAULT_API_BASE);
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("password123");
  const [sourceText, setSourceText] = useState("Hello, how are you?");
  const [loading, setLoading] = useState(false);
  const [translation, setTranslation] = useState(null);

  const handleTranslate = async () => {
    if (!sourceText.trim()) {
      Alert.alert("Validation", "Please enter text to translate.");
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${apiUrl.replace(/\/+$/, "")}/translate`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Accept": "application/json",
          "Authorization": encodeBasicAuth(username, password)
        },
        body: JSON.stringify({
          text: sourceText,
          sourceLanguage: "English",
          includeArabizi: true
        })
      });

      if (!response.ok) {
        const errText = await response.text();
        throw new Error(`HTTP ${response.status}: ${errText}`);
      }

      const data = await response.json();
      setTranslation(data);
    } catch (error) {
      Alert.alert("REST API Error", error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleReadAloud = () => {
    if (!translation) return;
    Speech.stop();
    Speech.speak(translation.darijaArabic || translation.darijaLatin, {
      language: "ar-MA",
      rate: 0.92
    });
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.title}>🇲🇦 Darija Translator</Text>
        <Text style={styles.subtitle}>React Native Mobile Client • Jakarta REST + Gemini</Text>

        <View style={styles.card}>
          <Text style={styles.label}>REST Endpoint URL</Text>
          <TextInput
            style={styles.smallInput}
            value={apiUrl}
            onChangeText={setApiUrl}
            autoCapitalize="none"
          />

          <Text style={styles.label}>Source Text (English)</Text>
          <TextInput
            style={styles.textArea}
            value={sourceText}
            onChangeText={setSourceText}
            multiline
            numberOfLines={4}
            placeholder="Type English text to translate into Moroccan Darija..."
            placeholderTextColor="#64748b"
          />

          <TouchableOpacity
            style={styles.translateBtn}
            onPress={handleTranslate}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="#ffffff" />
            ) : (
              <Text style={styles.translateBtnText}>Translate to Moroccan Darija</Text>
            )}
          </TouchableOpacity>
        </View>

        {translation && (
          <View style={styles.card}>
            <View style={styles.resultHeader}>
              <Text style={styles.badge}>الدارجة المغربية</Text>
              <TouchableOpacity style={styles.ttsBtn} onPress={handleReadAloud}>
                <Text style={styles.ttsBtnText}>🔊 Read Aloud</Text>
              </TouchableOpacity>
            </View>

            <Text style={styles.arabicText}>{translation.darijaArabic}</Text>
            <Text style={styles.latinText}>{translation.darijaLatin}</Text>
            <Text style={styles.phoneticText}>Pronunciation: {translation.phoneticGuide}</Text>
            <Text style={styles.metaText}>Model: {translation.modelUsed}</Text>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: "#0f172a"
  },
  container: {
    padding: 20
  },
  title: {
    fontSize: 24,
    fontWeight: "800",
    color: "#f8fafc",
    marginBottom: 4
  },
  subtitle: {
    fontSize: 13,
    color: "#94a3b8",
    marginBottom: 20
  },
  card: {
    backgroundColor: "#1e293b",
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: "#334155"
  },
  label: {
    fontSize: 12,
    fontWeight: "600",
    color: "#94a3b8",
    marginBottom: 6
  },
  smallInput: {
    backgroundColor: "#0f172a",
    color: "#f8fafc",
    padding: 10,
    borderRadius: 8,
    marginBottom: 14,
    borderWidth: 1,
    borderColor: "#334155"
  },
  textArea: {
    backgroundColor: "#0f172a",
    color: "#f8fafc",
    padding: 12,
    borderRadius: 8,
    minHeight: 96,
    textAlignVertical: "top",
    fontSize: 16,
    marginBottom: 14,
    borderWidth: 1,
    borderColor: "#334155"
  },
  translateBtn: {
    backgroundColor: "#c1272d",
    paddingVertical: 14,
    borderRadius: 8,
    alignItems: "center"
  },
  translateBtnText: {
    color: "#ffffff",
    fontWeight: "700",
    fontSize: 16
  },
  resultHeader: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 12
  },
  badge: {
    backgroundColor: "#006233",
    color: "#ffffff",
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    fontSize: 12,
    fontWeight: "700"
  },
  ttsBtn: {
    backgroundColor: "#0f172a",
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: "#334155"
  },
  ttsBtnText: {
    color: "#f8fafc",
    fontSize: 12,
    fontWeight: "600"
  },
  arabicText: {
    fontSize: 26,
    fontWeight: "700",
    color: "#fde047",
    textAlign: "right",
    marginBottom: 8
  },
  latinText: {
    fontSize: 18,
    fontWeight: "600",
    color: "#38bdf8",
    marginBottom: 6
  },
  phoneticText: {
    fontSize: 14,
    fontStyle: "italic",
    color: "#cbd5e1",
    marginBottom: 10
  },
  metaText: {
    fontSize: 11,
    color: "#64748b",
    textAlign: "right"
  }
});

