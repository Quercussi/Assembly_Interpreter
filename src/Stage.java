public class Stage {
    private final int[] memory;      // Memory array
    private final int[] register;    // Register array
    private int pc;            // Program counter
    private int nextPc;        // Next program counter (automatically set to Pc+1)
    private boolean isHalt;    // Halt flag
    private int stepCount;
    private final Decoder decoder;
    private int instructionCount;
    private final StringBuilder sb = new StringBuilder();

    /**
     * Constructor for the stage class.
     */
    public Stage() {
        memory = new int[65536];
        register = new int[8];
        pc = 0;
        nextPc = 0;
        isHalt = false;
        stepCount = 0;
        decoder = Decoder.getInstance();
    }

    /**
     * Call the decoder to decode the stage,
     * and setting the program counter to the next one.
     */
    public void iterate() {
            nextPc = (pc + 1) % memory.length;
            decoder.execute(this);
            register[0] = 0;
            pc = nextPc;
            stepCount++;
    }

    /**
     * Execute the stage until it halts.
     */
    public void simulate() {
        while (!isHalt) {
            printState();
            iterate();
        }

        // print the last state.
        System.out.println("machine halted\ntotal of " + stepCount + " instructions executed");
        System.out.println("final state of machine:\n");
        printState();
    }

    /**
     * Get the current instruction code
     * @return the current instruction code.
     */
    public int getInstruction() {
        // Ensure the program counter (pc) is within the memory bounds
        if (pc >= 0 && pc < memory.length) {
            return memory[pc];
        } else {
            // Handle an out-of-bounds access
            return 0x1C00000; // Force halt
        }
    }

    /**
     * Get the memory array.
     * @return the memory.
     */
    public int[] getMemory() {
        return memory;
    }

    /**
     * Get the register array.
     * @return the registers.
     */
    public int[] getRegister() {
        return register;
    }

    /**
     * Get the current program counter.
     * @return the current program counter
     */
    public int getPc(){
        return pc ;
    }

    /**
     * Set the next program counter.
     * @param newPc is the next program counter to be set.
     */
    public void setNextPc(int newPc) {
        nextPc = newPc;
    }

    /**
     * Call the stage to halt.
     */
    public void setHalt() {
        isHalt = true;
    }

    /**
     * Set the number of memory addresses that is to be printed.
     * @param newInstructionCount is the updating instruction count.
     */
    public void setInstructionCount(int newInstructionCount) {
        instructionCount = newInstructionCount;
    }

    /**
     * Print the details of the current state of the stage.
     */
    public void printState(){
        sb.setLength(0); // clears the string builder

        sb.append("@@@\nstate:\n").append("\tpc ").append(pc).append('\n');

        sb.append("\tmemory:\n");
        for(int i = 0; i < instructionCount; i++)
            sb.append("\t\tmem[").append(i).append("] ").append(memory[i]).append('\n');

        sb.append("\tregisters:\n");
        for(int i = 0; i < register.length; i++)
            sb.append("\t\treg[").append(i).append("] ").append(register[i]).append('\n');

        sb.append("end state\n\n");
        System.out.println(sb);
    }
}