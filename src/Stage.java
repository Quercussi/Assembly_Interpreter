import java.util.Arrays;

public class Stage {
    private int[] memory;      // Memory array
    private int[] register;    // Register array
    private int pc;            // Program counter
    private int nextPc;        // Next program counter (automatically set to Pc+1)
    private boolean isHalt;    // Halt flag

    public Stage() {
        memory = new int[65536];
        register = new int[8];
        pc = 0;
        nextPc = 0;
        isHalt = false;
    }

    public void iterate() {
        if (!isHalt) {
            // Fetch the instruction from memory at the current PC
            int instruction = memory[pc];

            // Decode and execute the instruction (You need to implement this part)
            // For simplicity, let's assume the instruction is a no-op (do nothing).
            // You should replace this with actual instruction execution logic.

            // Increment the program counter to the next instruction
            pc = nextPc;
            nextPc = pc + 1;
        }
    }

    public void setPc(int newPc) {
        pc = newPc;
        nextPc = pc + 1;  // Automatically set nextPc to the next instruction
    }

    public void setHalt() {
        isHalt = true;
    }

    public void simulate() {
        while (!isHalt) {
            iterate();
        }
    }

    public int[] getMemory() {
        return memory;
    }

    public int[] getRegister() {
        return register;
    }

    public int getInstructionCount() {
        int instructionCount = 0;
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != 0) {
                instructionCount++;
            }
        }
        return instructionCount;
    }

    //test decoder = test.getInstance();
    //decoder.execute(this);

    public static void main(String[] args) {
        Stage stage = new Stage();

        // Set memory values (for example, load a program into memory)
        stage.memory[0] = 0x1000;
        stage.memory[1] = 0x2000;
        stage.memory[2] = 0x3000;

        // Set the initial program counter
        stage.setPc(0);

        // Simulate the program until halted
        stage.simulate();

        // Print the final state of the registers and memory
        System.out.println("Registers: " + Arrays.toString(stage.register));
        System.out.println("Memory: " + Arrays.toString(stage.memory));
    }
}