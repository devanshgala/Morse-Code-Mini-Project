# 📟 Morse Code Convertor & Station Logbook
This is a Java-based Object-Oriented Programming (OOP) lab mini-project that encodes and decodes text and files into Morse code. It features live audio playback for encoded messages and a PostgreSQL database to maintain a permanent "station logbook" of all transmissions.

## ✨ Features
* **Text Encoding & Decoding**: Instantly convert standard text to Morse code and vice versa.
* **File Processing**: Read long messages directly from text files and encode/decode them.
* **Audio Playback**: Uses the Java Sound API to physically play the generated Morse code (800Hz tone).
* **Database Logging**: Automatically logs every transmission (input, output, and timestamp) using a PostgreSQL database.
* **Exception Handling**: Gracefully catches invalid characters, missing files, and unavailable audio hardware.

## 🛠️ Tech Stack
* **Language**: Java
* **Database**: PostgreSQL (JDBC)
* **Audio**: `javax.sound.sampled` library

## 📋 Prerequisites
To run this project on your local machine, you will need:
1. **Java Development Kit (JDK)** installed.
2. **PostgreSQL** installed and running on `localhost:5432`.
3. A PostgreSQL user named `postgres` (the app will ask for your password on startup).
4. The **PostgreSQL JDBC Driver** added to your Maven dependencies (`pom.xml`) or project classpath.

## 🚀 How to Run
1. Clone this repository or download the ZIP file.
2. Ensure your local PostgreSQL server is running.
3. Open the project in your preferred Java IDE (VS Code, IntelliJ, Eclipse).
4. Run the `MorseCode.java` main class.
5. Enter your database password when prompted to establish the connection and auto-create the tables.
6. Follow the on-screen console menu to encode, decode, and view logs!
