<?php
/**
 * PHP Client Application for the Moroccan Darija LLM Translator REST Service.
 * Communicates with the Jakarta RESTful Web Service using HTTP Basic Authentication.
 *
 * Run locally with: php -S localhost:8000
 */

$apiBaseUrl = getenv('DARIJA_API_URL') ?: 'http://localhost:8080/api/translator';
$username   = getenv('DARIJA_API_USER') ?: 'admin';
$password   = getenv('DARIJA_API_PASS') ?: 'password123';

$sourceText = '';
$sourceLang = 'English';
$result     = null;
$errorMsg   = null;

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['text'])) {
    $sourceText = trim($_POST['text']);
    $sourceLang = $_POST['sourceLanguage'] ?? 'English';

    if ($sourceText === '') {
        $errorMsg = 'Please enter some text to translate.';
    } else {
        $payload = json_encode([
            'text'           => $sourceText,
            'sourceLanguage' => $sourceLang,
            'includeArabizi' => true
        ]);

        $ch = curl_init($apiBaseUrl . '/translate');
        curl_setopt_array($ch, [
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST           => true,
            CURLOPT_POSTFIELDS     => $payload,
            CURLOPT_HTTPAUTH       => CURLAUTH_BASIC,
            CURLOPT_USERPWD        => $username . ':' . $password,
            CURLOPT_HTTPHEADER     => [
                'Content-Type: application/json',
                'Accept: application/json'
            ],
            CURLOPT_TIMEOUT        => 25
        ]);

        $responseBody = curl_exec($ch);
        $httpCode     = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $curlError    = curl_error($ch);
        curl_close($ch);

        if ($responseBody === false) {
            $errorMsg = 'Failed to connect to Java REST server: ' . $curlError;
        } elseif ($httpCode >= 400) {
            $errorMsg = "HTTP $httpCode Error from REST server: " . $responseBody;
        } else {
            $result = json_decode($responseBody, true);
        }
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Moroccan Darija LLM Translator — PHP Web Client</title>
    <style>
        body {
            font-family: system-ui, -apple-system, sans-serif;
            background: #0f172a;
            color: #f8fafc;
            max-width: 760px;
            margin: 40px auto;
            padding: 0 20px;
        }
        .card {
            background: #1e293b;
            border: 1px solid #334155;
            border-radius: 12px;
            padding: 24px;
            margin-bottom: 24px;
        }
        h1 { margin-top: 0; color: #f8fafc; }
        label { display: block; margin-bottom: 8px; font-weight: 600; color: #94a3b8; }
        textarea, select {
            width: 100%;
            box-sizing: border-box;
            padding: 12px;
            border-radius: 8px;
            border: 1px solid #334155;
            background: #0f172a;
            color: #f8fafc;
            font-size: 16px;
            margin-bottom: 16px;
        }
        button {
            background: #c1272d;
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
        }
        .arabic-output {
            font-size: 28px;
            color: #fde047;
            direction: rtl;
            margin: 12px 0;
            font-weight: 700;
        }
        .latin-output { font-size: 18px; color: #38bdf8; font-weight: 600; }
        .phonetic-output { font-size: 14px; color: #cbd5e1; font-style: italic; }
        .error { background: #7f1d1d; padding: 14px; border-radius: 8px; margin-bottom: 18px; }
    </style>
</head>
<body>
    <div class="card">
        <h1>🇲🇦 Moroccan Darija Translator (PHP Client)</h1>
        <p style="color:#94a3b8;">Connected to Jakarta RESTful Web Service: <code><?= htmlspecialchars($apiBaseUrl) ?></code></p>

        <?php if ($errorMsg): ?>
            <div class="error"><?= htmlspecialchars($errorMsg) ?></div>
        <?php endif; ?>

        <form method="POST" action="">
            <label for="sourceLanguage">Source Language</label>
            <select name="sourceLanguage" id="sourceLanguage">
                <option value="English" <?= $sourceLang === 'English' ? 'selected' : '' ?>>English</option>
                <option value="French" <?= $sourceLang === 'French' ? 'selected' : '' ?>>French</option>
                <option value="Spanish" <?= $sourceLang === 'Spanish' ? 'selected' : '' ?>>Spanish</option>
            </select>

            <label for="text">Text to Translate</label>
            <textarea name="text" id="text" rows="4" placeholder="Enter text (e.g., Hello, how are you?)"><?= htmlspecialchars($sourceText) ?></textarea>

            <button type="submit">Translate to Darija</button>
        </form>
    </div>

    <?php if ($result): ?>
        <div class="card">
            <h2 style="margin-top:0;">Translation Result</h2>
            <p class="arabic-output" id="arabicText"><?= htmlspecialchars($result['darijaArabic'] ?? '') ?></p>
            <p class="latin-output">Arabizi: <?= htmlspecialchars($result['darijaLatin'] ?? '') ?></p>
            <p class="phonetic-output">Pronunciation: <?= htmlspecialchars($result['phoneticGuide'] ?? '') ?></p>
            <p style="font-size:12px;color:#64748b;">Model: <?= htmlspecialchars($result['modelUsed'] ?? '') ?> | Timestamp: <?= htmlspecialchars($result['timestamp'] ?? '') ?></p>
            <button type="button" onclick="readAloud()" style="background:#006233;margin-top:8px;">🔊 Read Aloud (TTS)</button>
        </div>
        <script>
            function readAloud() {
                const text = document.getElementById('arabicText').innerText;
                const utterance = new SpeechSynthesisUtterance(text);
                utterance.lang = 'ar-MA';
                utterance.rate = 0.92;
                window.speechSynthesis.speak(utterance);
            }
        </script>
    <?php endif; ?>
</body>
</html>

