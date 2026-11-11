#!/usr/bin/env python3
"""
Python Client Application (Desktop Tkinter GUI + CLI) for the Moroccan Darija
Jakarta RESTful Web Service.

Usage:
  1. GUI Mode (default):
       python translator_client.py
  2. CLI Mode:
       python translator_client.py --text "Hello, how are you?"
"""

import argparse
import base64
import json
import sys
import urllib.error
import urllib.request
from typing import Dict, Any

DEFAULT_API_URL = "http://localhost:8080/api/translator"
DEFAULT_USERNAME = "admin"
DEFAULT_PASSWORD = "password123"


class DarijaRestClient:
    """HTTP Client communicating with TranslatorResource using Basic Authentication."""

    def __init__(self, base_url: str = DEFAULT_API_URL, username: str = DEFAULT_USERNAME, password: str = DEFAULT_PASSWORD):
        self.base_url = base_url.rstrip("/")
        self.username = username
        self.password = password

    def _auth_header(self) -> str:
        token = base64.b64encode(f"{self.username}:{self.password}".encode("utf-8")).decode("ascii")
        return f"Basic {token}"

    def translate(self, text: str, source_language: str = "English") -> Dict[str, Any]:
        url = f"{self.base_url}/translate"
        payload = json.dumps({
            "text": text,
            "sourceLanguage": source_language,
            "includeArabizi": True
        }).encode("utf-8")

        req = urllib.request.Request(
            url,
            data=payload,
            headers={
                "Content-Type": "application/json; charset=UTF-8",
                "Accept": "application/json",
                "Authorization": self._auth_header()
            },
            method="POST"
        )
        try:
            with urllib.request.urlopen(req, timeout=25) as response:
                return json.loads(response.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            err_body = e.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"HTTP {e.code}: {err_body}") from e
        except urllib.error.URLError as e:
            raise RuntimeError(f"Connection failed: {e.reason}") from e


def launch_gui(client: DarijaRestClient) -> None:
    import tkinter as tk
    from tkinter import ttk, messagebox

    root = tk.Tk()
    root.title("Moroccan Darija LLM Translator — Python Desktop Client")
    root.geometry("620x480")
    root.configure(bg="#0f172a")

    style = ttk.Style()
    style.theme_use("clam")

    header = tk.Label(
        root,
        text="Moroccan Darija Translator (Jakarta REST + Gemini)",
        font=("Segoe UI", 15, "bold"),
        bg="#0f172a",
        fg="#f8fafc"
    )
    header.pack(pady=(16, 8))

    input_frame = tk.Frame(root, bg="#1e293b", padx=14, pady=14)
    input_frame.pack(fill="x", padx=20, pady=8)

    tk.Label(input_frame, text="Enter English Text:", bg="#1e293b", fg="#94a3b8", font=("Segoe UI", 10, "bold")).pack(anchor="w")
    text_input = tk.Text(input_frame, height=4, font=("Segoe UI", 11), bg="#0f172a", fg="#f8fafc", insertbackground="white")
    text_input.pack(fill="x", pady=8)
    text_input.insert("1.0", "Hello, how are you?")

    output_frame = tk.Frame(root, bg="#1e293b", padx=14, pady=14)
    output_frame.pack(fill="both", expand=True, padx=20, pady=8)

    arabic_var = tk.StringVar(value="—")
    latin_var = tk.StringVar(value="—")
    phonetic_var = tk.StringVar(value="—")

    tk.Label(output_frame, text="Moroccan Darija (Arabic Script):", bg="#1e293b", fg="#94a3b8").pack(anchor="w")
    tk.Label(output_frame, textvariable=arabic_var, font=("Segoe UI", 18, "bold"), bg="#1e293b", fg="#fde047", wraplength=540).pack(anchor="e", pady=4)

    tk.Label(output_frame, text="Arabizi / Latin Script:", bg="#1e293b", fg="#94a3b8").pack(anchor="w")
    tk.Label(output_frame, textvariable=latin_var, font=("Segoe UI", 13, "bold"), bg="#1e293b", fg="#38bdf8", wraplength=540).pack(anchor="w", pady=4)

    tk.Label(output_frame, text="Phonetic Guide:", bg="#1e293b", fg="#94a3b8").pack(anchor="w")
    tk.Label(output_frame, textvariable=phonetic_var, font=("Segoe UI", 11, "italic"), bg="#1e293b", fg="#cbd5e1", wraplength=540).pack(anchor="w", pady=4)

    def on_translate():
        raw_text = text_input.get("1.0", "end").strip()
        if not raw_text:
            messagebox.showwarning("Validation", "Please enter text to translate.")
            return
        try:
            res = client.translate(raw_text)
            arabic_var.set(res.get("darijaArabic", ""))
            latin_var.set(res.get("darijaLatin", ""))
            phonetic_var.set(res.get("phoneticGuide", ""))
        except Exception as exc:
            messagebox.showerror("Translation Error", str(exc))

    btn = tk.Button(
        input_frame,
        text="Translate to Moroccan Darija",
        command=on_translate,
        bg="#c1272d",
        fg="white",
        font=("Segoe UI", 11, "bold"),
        padx=14,
        pady=6,
        relief="flat"
    )
    btn.pack(fill="x")

    root.mainloop()


def main():
    parser = argparse.ArgumentParser(description="Moroccan Darija Translator Python Client")
    parser.add_argument("--text", type=str, help="Translate text directly in CLI mode")
    parser.add_argument("--url", type=str, default=DEFAULT_API_URL, help="REST API Base URL")
    parser.add_argument("--user", type=str, default=DEFAULT_USERNAME, help="Basic Auth username")
    parser.add_argument("--password", type=str, default=DEFAULT_PASSWORD, help="Basic Auth password")
    args = parser.parse_args()

    client = DarijaRestClient(base_url=args.url, username=args.user, password=args.password)

    if args.text:
        result = client.translate(args.text)
        print(json.dumps(result, indent=2, ensure_ascii=False))
    else:
        launch_gui(client)


if __name__ == "__main__":
    main()

