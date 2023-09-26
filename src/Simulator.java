import java.io.*;

public class Simulator {
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Simulator <file_name>");
            System.exit(1);
        }
        
        String fileName = args[0];
        File inputFile = new File(fileName);

        boolean isExists = inputFile.exists();
        if (!isExists) {
            System.out.println("File '" + fileName + "' does not exist.");
            System.exit(1);
        }

        // Stage
        Stage stage = new Stage();
        int[] memory = stage.getMemory();
        int pc = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    memory[pc] = Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    System.out.println("Error: Cannot parse as an integer. Exception: " + e.getMessage());
                    System.exit(1);
                }
                pc++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //stage.simulate();
    }
}
