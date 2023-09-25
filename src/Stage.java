import java.util.Arrays;

public class Stage {
    private int[] memory;      // Memory array
    private int[] register;    // Register array
    private int pc;            // Program counter
    private int nextPc;        // Next program counter (automatically set to Pc+1)
    private boolean isHalt;    // Halt flag
    private int stepCount = 0;
    private Decoder decoder;
    private int instructionCount = 0;

    public Stage() {
        memory = new int[65536];
        register = new int[8];
        pc = 0;
        nextPc = 0;
        isHalt = false;
        decoder = Decoder.getInstance();
    }

    public void iterate() {
            // Fetch the instruction from memory at the current PC
            nextPc = (pc + 1) % memory.length;
            int instruction = memory[pc];

            // Decode and execute the instruction (You need to implement this part)
            // For simplicity, let's assume the instruction is a no-op (do nothing).
            // You should replace this with actual instruction execution logic.
            decoder.execute(this);
            // Increment the program counter to the next instruction
            pc = nextPc;
            instructionCount++;
            stepCount++;
    }

    public void setNextPc(int newPc) {
        nextPc = newPc;
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

    public void setInstructionCount(int count) {
        instructionCount = count;
    }

    // Method to get the current instruction
    public int getInstruction() {
        // Ensure the program counter (pc) is within the memory bounds
        if (pc >= 0 && pc < memory.length) {
            return memory[pc];
        } else {
            // Handle an out-of-bounds access or return an error code
            return -1; // You can choose an appropriate error code
        }
    }
}