import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Simulator {
    
    public static void main(String[] args) {
        if (args.length == 1) {
            String fileName = args[0];
            File inputFile = new File(fileName);

            // Task 1: Check if the file exists
            boolean fileExists = checkFileExists(inputFile);
            if (fileExists) {
                System.out.println("File '" + fileName + "' exists.");

                // Task 2: Check if the file is empty
                boolean isEmpty = checkFileEmpty(inputFile);
                if (isEmpty) {
                    System.out.println("File '" + fileName + "' is empty.");
                } else {
                    System.out.println("File '" + fileName + "' is not empty.");

                    // Task 3: Read the contents of the file
                    String fileContents = readFileContents(inputFile);

                    // Task 4: Handle parsing as integers and exceptions
                    String[] lines = fileContents.split("\n");
                    List<Integer> integers = new ArrayList<>();

                    for (String line : lines) {
                        try {
                            int intValue = Integer.parseInt(line.trim());
                            System.out.println(intValue);
                            integers.add(intValue);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Cannot parse as an integer. Exception: " + e.getMessage());
                        }
                    }

                    // Task 5: Send them to the Stage class
                    if (containsNumbersOnly(fileContents)) {
                        Stage stage = new Stage();
                        stage.processFileContents(fileContents);
                    }
                }
            } else {
                System.out.println("File '" + fileName + "' does not exist.");
            }
        } else {
            System.out.println("Usage: java Simulator <file_name>");
            System.exit(1); // Exit with code 1
        }
    }

    public static boolean checkFileExists(File file) {
        return file.exists();
    }

    // Function to check if a file is empty
    public static boolean checkFileEmpty(File file) {
        return file.length() == 0;
    }

    // Function to read the contents of a file
    public static String readFileContents(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static boolean containsNumbersOnly(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            try {
                Integer.parseInt(line.trim());
            } catch (NumberFormatException e) {
                return false; // Return false if any line is not a valid integer
            }
        }
        return true; // Return true if all lines are valid integers
    }
}
