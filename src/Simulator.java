import java.io.*;

public class Simulator {
    
    public static void main(String[] args) {
        // Check whether there is an invalid number of arguments
        if (args.length != 1) {
            System.out.println("Usage: java Simulator <file_name>");
            System.exit(1);
        }
        
        String fileName = args[0];
        File inputFile = new File(fileName);

        // Check whether the file exists.
        boolean isExists = inputFile.exists();
        if (!isExists) {
            System.out.println("File '" + fileName + "' does not exist.");
            System.exit(1);
        }

        // Construct a stage.
        Stage stage = new Stage();
        int[] memory = stage.getMemory();
        int pc = 0;

        // Extract machine code from the input file,
        // then store it into the stage memory.
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
            stage.setInstructionCount(pc);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Simulate the stage.
        stage.simulate();
        System.exit(0);
    }
}
