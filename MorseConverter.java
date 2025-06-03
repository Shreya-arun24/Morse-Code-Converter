import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MorseConverter {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean repeat = true;
        List<String> history = loadHistory(); // Load conversion history from file
        while (repeat) {
            System.out.println("Choose an option:");
            System.out.println("1. Text to Morse Code");
            System.out.println("2. Morse Code to Text");
            System.out.println("3. Upload a text file and convert");
            System.out.println("4. View Conversion History");
            System.out.println("5. Generate QR Code for Morse Code");
            System.out.println("6. Save Converted Text to File");
            System.out.println("7. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            switch (choice) {
                case 1:
                    System.out.println("Enter the text to convert to Morse Code:");
                    String textToConvert = scanner.nextLine();
                    System.out.println("Choose the conversion option:");
                    System.out.println("1. Normal playback");
                    System.out.println("2. Custom playback");
                    int playbackOption = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    if (playbackOption == 1) {
                        String morseCode = textToMorse(textToConvert);
                        System.out.println("Morse Code: " + morseCode);
                        playMorseCode(morseCode);
                        history.add("Text to Morse: " + textToConvert + " => " + morseCode);
                    } else if (playbackOption == 2) {
                        System.out.println("Enter speed (1-10): ");
                        int speed = scanner.nextInt();
                        System.out.println("Enter pitch (100-1000): ");
                        int pitch = scanner.nextInt();
                        System.out.println("Enter volume (1-100): ");
                        int volume = scanner.nextInt();
                        String morseCode = textToMorse(textToConvert);
                        System.out.println("Morse Code: " + morseCode);
                        playMorseCode(morseCode, speed, pitch, volume);
                        history.add("Text to Morse (custom playback): " + textToConvert + " => " + morseCode);
                    } else {
                        System.out.println("Invalid playback option.");
                    }
                    break;
                case 2:
                    System.out.println("Enter the Morse Code to convert to text:");
                    String morseToConvert = scanner.nextLine();
                    String text = morseToText(morseToConvert);
                    System.out.println("Text: " + text);
                    history.add("Morse to Text: " + morseToConvert + " => " + text);
                    break;
                case 3:
                    System.out.println("Enter the path to the text file:");
                    String filePath = scanner.nextLine();
                    try {
                        String fileContent = readFileContent(filePath);
                        System.out.println("File Content:\n" + fileContent);
                        System.out.println("Choose the conversion option:");
                        System.out.println("1. Normal playback");
                        System.out.println("2. Custom playback");
                        int conversionOption = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        if (conversionOption == 1) {
                            String morseCodeFromFile = textToMorse(fileContent);
                            System.out.println("Morse Code: " + morseCodeFromFile);
                            playMorseCode(morseCodeFromFile);
                            history.add("Text to Morse (from file): " + fileContent + " => " + morseCodeFromFile);
                        } else if (conversionOption == 2) {
                            System.out.println("Enter speed (1-10): ");
                            int speed = scanner.nextInt();
                            System.out.println("Enter pitch (100-1000): ");
                            int pitch = scanner.nextInt();
                            System.out.println("Enter volume (1-100): ");
                            int volume = scanner.nextInt();
                            String morseCodeFromFile = textToMorse(fileContent);
                            System.out.println("Morse Code: " + morseCodeFromFile);
                            playMorseCode(morseCodeFromFile, speed, pitch, volume);
                            history.add("Text to Morse (custom playback, from file): " + fileContent + " => " + morseCodeFromFile);
                        } else {
                            System.out.println("Invalid conversion option.");
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("File not found.");
                    }
                    break;
                case 4:
                    System.out.println("Conversion History:");
                    for (String entry : history) {
                        System.out.println(entry);
                    }
                    break;
                case 5:
                    System.out.println("Enter the Morse Code to generate QR code:");
                    String morseCodeForQR = scanner.nextLine();
                    generateQRCode(morseCodeForQR);
                    break;

                case 6:
                System.out.println("Enter the text to save to file:");
                String textToSave = scanner.nextLine();
                saveToFile(textToSave);
                break;
                case 7:
                    repeat = false;
                    saveHistory(history); // Save conversion history to file before exiting
                    break;
                default:
                    System.out.println("Invalid option. Please choose again.");
                    break;
            }

        }

        scanner.close();
    }

    private static void saveToFile(String textToSave) {
        String desktopPath = System.getProperty("user.home") + "/Desktop/";
        String fileName = "converted_text.txt";
        String filePath = desktopPath + fileName;

        try (PrintWriter writer = new PrintWriter(filePath)) {
            writer.println(textToSave);
            System.out.println("Text saved to file: " + filePath);
        } catch (FileNotFoundException e) {
            System.out.println("Error saving text to file: " + e.getMessage());
        }
    }

    public static String readFileContent(String filePath) throws FileNotFoundException {
        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent.toString();
    }

    public static String textToMorse(String text) {
        // Define Morse code mappings
        String[] morseCodes = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---",
                "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-",
                "..-", "...-", ".--", "-..-", "-.--", "--..", "/"};
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ/".toCharArray();

        // Convert text to Morse code
        text = text.toUpperCase();
        StringBuilder morseCode = new StringBuilder();
        for (char letter : text.toCharArray()) {
            int index = -1;
            for (int i = 0; i < alphabet.length; i++) {
                if (alphabet[i] == letter) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                morseCode.append(morseCodes[index]).append(" ");
            } else if (letter == ' ') {
                morseCode.append("/ ");
            }
        }
        return morseCode.toString();
    }

    public static String morseToText(String morseCode) {
        // Define Morse code mappings
        String[] morseCodes = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---",
                "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-",
                "..-", "...-", ".--", "-..-", "-.--", "--..", "/"};
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ/".toCharArray();

        // Convert Morse code to text
        StringBuilder text = new StringBuilder();
        String[] words = morseCode.split("/");
        for (String word : words) {
            String[] letters = word.trim().split("\\s+");
            for (String letter : letters) {
                int index = -1;
                for (int i = 0; i < morseCodes.length; i++) {
                    if (morseCodes[i].equals(letter)) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    text.append(alphabet[index]);
                }
            }
            text.append(" ");
        }
        return text.toString();
    }
    public static void playMorseCode(String morseCode) {
        playMorseCode(morseCode, 1, 800, 100);
    }
    public static void playMorseCode(String morseCode, int speed, int pitch, int volume) {
        try {
            AudioFormat audioFormat = new AudioFormat(8000 * speed, 8, 1, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(audioFormat);
            line.open(audioFormat);
            line.start();

            String[] codes = morseCode.split("\\s+");
            for (String code : codes) {
                for (char c : code.toCharArray()) {
                    if (c == '.') {
                        playSound(line, 100 * speed, speed, pitch, volume);
                    } else if (c == '-') {
                        playSound(line, 300 * speed, speed, pitch, volume);
                    }
                    Thread.sleep(100 * speed);
                }
                Thread.sleep(300 * speed);
            }
            line.drain();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playSound(SourceDataLine line, int ms, int speed, int freq, int volume) throws InterruptedException {
        byte[] buf = new byte[1];
        for (int i = 0; i < ms * 8; i++) {
            double angle = i / ((8000.0 * speed) / freq) * 2.0 * Math.PI;
            buf[0] = (byte) (Math.sin(angle) * volume);
            line.write(buf, 0, 1);
            Thread.sleep(1000 / (8000 * speed)); // Adjust light blinking speed
        }
    }

    public static void saveHistory(List<String> history) {
        try (PrintWriter writer = new PrintWriter("history.txt")) {
            for (String entry : history) {
                writer.println(entry);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static List<String> loadHistory() {
        List<String> history = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("history.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                history.add(line);
            }
        } catch (IOException e) {
            // File not found or error reading file, ignore and return empty list
        }
        return history;
    }
    public static void generateQRCode(String morseCode) {
        try {
            // Encode the Morse code as text
            String qrText = "Morse Code: " + morseCode;
            // Set up QR code parameters
            int width = 300;
            int height = 300;
            String fileType = "png";
            // Create QR code writer
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            // Encode the QR code
            BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, width, height);
            // Convert BitMatrix to BufferedImage
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            // Save QR code as PNG image
            File qrFile = new File("qrcode.png");
            javax.imageio.ImageIO.write(image, fileType, qrFile);
            // Display QR code location
            System.out.println("QR code saved as: " + qrFile.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error generating QR code: " + e.getMessage());
        }
    }
}
