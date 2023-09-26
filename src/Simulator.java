import java.io.*;

public class Simulator {
    
    public static void main(String[] args) {
        if (args.length == 1) {
            String fileName = args[0];
            File inputFile = new File(fileName);

            boolean isExists = checkFileExists(inputFile);
            if (!isExists) {
                System.out.println("File '" + fileName + "' does not exist.");
                System.exit(1);
            }

            String fileContents = readFileContents(inputFile);
            String[] lines = fileContents.split("\n");

            // Get the memory array from the Stage class
            Stage stage = new Stage();
            int[] memory = stage.getMemory();
            int pc = 0;

            for (String line : lines) {
                try {
                    memory[pc] = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error: Cannot parse as an integer. Exception: " + e.getMessage());
                    System.exit(1);
                }
                pc++;
            }
        } else {
            System.out.println("Usage: java Simulator <file_name>");
            System.exit(1);
        }
    }

    public static boolean checkFileExists(File file) {
        return file.exists();
    }

    public static boolean checkFileEmpty(File file) {
        return file.length() == 0;
    }

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
}