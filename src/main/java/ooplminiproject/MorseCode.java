package ooplminiproject;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

class CipherConvertor {
    private final char[] alphabets = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ',', '?', '!', '\n'};
    private final String[] morseCode = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--..", "/", "-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.", ".-.-.-", "--..--", "..--..", "-.-.--", ".-.-"};

    String encoder(String input) {
        StringBuilder encodedMorseCode = new StringBuilder();
        input = input.toUpperCase().trim();
        for (int i = 0; i < input.length(); i++) {
            char currentCharacter = input.charAt(i);
            boolean found = false;
            for (byte j = 0; j < alphabets.length; j++) {
                if (currentCharacter == alphabets[j]) {
                    encodedMorseCode.append(morseCode[j]).append(" ");
                    found = true;
                    break;
                }
            }
            if (found == false) {
                throw new IllegalArgumentException("Invalid character '" + currentCharacter + "' found in the input text. Kindly enter a valid text containing only alphabets and spaces.");
            }
        }
        return encodedMorseCode.toString().trim();
    }

    String decoder(String input) {
        StringBuilder decodedText = new StringBuilder();
        input = input.trim();
        String[] morseWords = input.split("\\s+");
        for (int i = 0; i < morseWords.length; i++) {
            String currentString = morseWords[i];
            boolean found = false;
            for (byte j = 0; j < morseCode.length; j++) {
                if (currentString.equals(morseCode[j])) {
                    decodedText.append(alphabets[j]);
                    found = true;
                    break;
                }
            }
            if (found == false) {
                throw new IllegalArgumentException("Invalid code '" + currentString + "' found in the morse code. Kindly enter a valid code.");
            }
        }
        String lowerText = decodedText.toString().trim().toLowerCase();
        return java.util.regex.Pattern.compile("(^|[.!?]\\s*)([a-z])").matcher(lowerText).replaceAll(m -> m.group(1) + m.group(2).toUpperCase());
    }
}

class FileHandler {
    static String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder();
        try (Scanner fileReader = new Scanner(file)) {
            while (fileReader.hasNextLine()) {
                fileContent.append(fileReader.nextLine()).append("\n");
            }
        }
        return fileContent.toString().trim();
    }
}

class AudioPlayer {
    private static final int HERTZ = 800;
    private static final int DOT_MS = 100;
    private static final float SAMPLE_RATE = 8000f;

    static void playMorseCode(String morseCode) {
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
            sdl.open(af);
            sdl.start();
            System.out.println("Playing Morse Code Audio...");
            for (int i = 0; i < morseCode.length(); i++) {
                char symbol = morseCode.charAt(i);
                if (symbol == '.') {
                    generateTone(sdl, DOT_MS);
                    generateSilence(sdl, DOT_MS);
                } else if (symbol == '-') {
                    generateTone(sdl, DOT_MS * 3);
                    generateSilence(sdl, DOT_MS);
                } else if (symbol == ' ') {
                    generateSilence(sdl, DOT_MS * 3);
                } else if (symbol == '/') {
                    generateSilence(sdl, DOT_MS * 7);
                }
            }
            sdl.drain();
            System.out.println("Audio Finished");
        } catch (LineUnavailableException e) {
            System.out.println("\nAudio Error: Your sound card is busy, in use by another app or not found.");
            System.out.println("System Detail: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("\nAudio Error: Your system does not support the 8000Hz mono audio format.");
        }
    }

    private static void generateTone(SourceDataLine sdl, int durationMs) {
        int totalSamples = (int)((durationMs / 1000.0) * SAMPLE_RATE);
        byte buf[] = new byte[1];

        for (int i = 0; i < totalSamples; i++) {
            double angle = i / (SAMPLE_RATE / HERTZ) * 2.0 * Math.PI;
            buf[0] = (byte)(Math.sin(angle) * 127);
            sdl.write(buf, 0, 1);
        }
    }

    private static void generateSilence(SourceDataLine sdl, int durationMS) {
        int totalSamples = (int)((durationMS / 1000.0) * SAMPLE_RATE);
        byte buf[] = new byte[1];
        for (int i = 0; i < totalSamples; i++) {
            sdl.write(buf, 0, 1);
        }
    }
}

class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/morse_code";
    private static final String USER = "postgres";
    private static String dbPassword = "";
    private static Connection conn;

    static boolean initialize(Scanner scanner) {
        System.out.print("Enter your PostgreSQL Password for user 'postgres': ");
        dbPassword = scanner.nextLine();

        try {
            conn = DriverManager.getConnection(URL, USER, dbPassword);
            try (Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS transmission_logs (id Serial PRIMARY KEY, operation VARCHAR(10), input_text TEXT NOT NULL, output_text TEXT NOT NULL, log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
                stmt.executeUpdate(sql);
            }
            System.out.println("Database Connected Successfully!");
            return true;
        } catch (SQLException e) {
            System.out.println("Database Error: Could not connect or create tables. Check your password. " + e.getMessage());
            return false;
        }
    }

    static void log_transmission(String operation, String input, String output) {
        if (conn == null) {
            return;
        }
        String sql = "INSERT INTO transmission_logs (operation, input_text, output_text) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, operation);
            pstmt.setString(2, input);
            pstmt.setString(3, output);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Log Error: Could not save transmission.");
        }
    }

    static void viewLogs() {
        if (conn == null) {
            return;
        }
        String sql = "SELECT * FROM transmission_logs ORDER BY log_time DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\nTransmission Logs (Newest Date First)");
            System.out.printf("%-19s | %-10s | %-45s | %-45s%n", "Timestamp", "Operation", "Input", "Output");
            boolean hasLogs = false;

            while (rs.next() == true) {
                hasLogs = true;
                System.out.printf("%-19s | %-10s | %-45s | %-45s%n", rs.getTimestamp("log_time").toString().substring(0, 19), rs.getString("operation"), rs.getString("input_text").replace("\n", " ").transform(t -> (t.length() > 38) ? t.substring(0, 38) + " [more]" : t), rs.getString("output_text").replace("\n", " ").transform(t -> (t.length() > 38) ? t.substring(0, 38) + " [more]" : t));
            }
            if (hasLogs == false) {
                System.out.println("No logs found. The station log book is empty.");
            }
        } catch (SQLException e) {
            System.out.println("Database Error: Could not retrieve logs.");
        }
    }

    static void clearLogs() {
        if (conn == null) {
            return;
        }
        String sql = "TRUNCATE TABLE transmission_logs RESTART IDENTITY";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("Logbook successfully cleared. All station records erased.");
        } catch (SQLException e) {
            System.out.println("Database Error: Could not clear logs.");
        }
    }

    static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Database Error: Could not close database connection.");
            }
        }
    }
}

public class MorseCode {
    private static void encoding(String input, char sourceType, CipherConvertor myCipher, Scanner scanner) {
        byte audioChoice = 0;
        String output = myCipher.encoder(input);
        System.out.println("\nEncoded Morse Code:\n" + output);
        DatabaseManager.log_transmission("Encode (" + sourceType + ")", input, output);
        System.out.print("Do you want to play audio (1 for Yes/2 for No): ");
        try {
            audioChoice = scanner.nextByte();
            if (audioChoice == 1) {
                AudioPlayer.playMorseCode(output);
            } else if (audioChoice == 2) {
                System.out.println("Audio playback skipped.");
            } else {
                System.out.println("Unrecognised Choice! Audio playback skipped.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid choice! Audio playback skipped");
        } finally {
            scanner.nextLine();
        }
    }

    private static void decoding(String input, char sourceType, CipherConvertor myCipher) {
        String output = myCipher.decoder(input);
        System.out.println("\nDecoded Text:\n" + output);
        DatabaseManager.log_transmission("Decode (" + sourceType + ")", input, output);
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CipherConvertor myCipher = new CipherConvertor();
        String filePath = "";
        byte choice = 0;

        System.out.println("Object-Oriented Programming Lab Mini Project: Morse Code");
        System.out.println("By Devansh Gala (BC14) | Kiana Hirani (BC02) | Priyank Mehta (BC32) | Riya Bhansali (BC22)");
        DatabaseManager.initialize(scanner);

        while (true) {
            System.out.println("\nMorse Code Menu:\n1. Encode Text to Morse Code\n2. Decode Morse Code to Text\n3. Encode File to Morse Code\n4. Decode File to Text\n5. View Station Logs\n6. Clear Logbook\n7. Exit");
            System.out.print("Enter your choice (1-7): ");
            try {
                choice = scanner.nextByte();
            } catch (InputMismatchException e) {
                System.out.println("Invalid choice! Kindly enter a valid choice (1-7).");
                continue;
            } finally {
                scanner.nextLine();
            }

            try {
                switch (choice) {
                    case 1:
                        System.out.println("Enter the text to be encoded:");
                        encoding(scanner.nextLine(), 'T', myCipher, scanner);
                        break;
                    case 2:
                        System.out.println("Enter the morse code to be decoded:");
                        decoding(scanner.nextLine(), 'T', myCipher);
                        break;
                    case 3:
                        System.out.println("Enter the file path to be encoded:");
                        filePath = scanner.nextLine().trim();
                        encoding(FileHandler.readFile(filePath), 'F', myCipher, scanner);
                        break;
                    case 4:
                        System.out.println("Enter the file path to be decoded:");
                        filePath = scanner.nextLine().trim();
                        decoding(FileHandler.readFile(filePath), 'F', myCipher);
                        break;
                    case 5:
                        DatabaseManager.viewLogs();
                        break;
                    case 6:
                        DatabaseManager.clearLogs();
                        break;
                    case 7:
                        System.out.println("Exiting the program. Bye!");
                        DatabaseManager.closeConnection();
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice! Kindly enter a valid choice (1-7).");
                        break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Cipher Error: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("File Error: " + "Could not read " + filePath + ". Make sure it exists, the path is correct and it is accessible.");
            }
        }
    }
}