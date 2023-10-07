public class Decoder {
    private static Decoder instance;
    private Decoder() {}

    /**
     * Return the singleton instance of the class
     * @return is the instance of the class.
     */
    public static Decoder getInstance() {
        if(instance == null)
            instance = new Decoder();
        return instance;
    }

    /**
     * Retrieve the instruction from the stage,
     * dissect it to extract a collection of fields,
     * and then assign an instruction class to execute.
     * @param stage is the Stage object that is to be computed.
     */
    public void execute(Stage stage) {
        int instruction = stage.getInstruction();
        int opcode = (instruction >> 22) & 0b111;

        switch (opcode) {
            // R-Type
            case 0b000, 0b001 -> {
                int rs, rt, rd;
                rs = (instruction >> 19) & 0b111;
                rt = (instruction >> 16) & 0b111;
                rd = instruction & 0b111;
                RInstruction.getInstance().executeR(stage, opcode, rs, rt, rd);
            }
            // I-type
            case 0b010, 0b011, 0b100 -> {
                int rs, rt, offset;
                rs = (instruction >> 19) & 0b111;
                rt = (instruction >> 16) & 0b111;
                offset = instruction & 0xFFFF;
                IInstruction.getInstance().executeI(stage, opcode, rs, rt, offset);
            }
            // J-Type
            case 0b101 -> {
                int rs,rd;
                rs = (instruction >> 19) & 0b111;
                rd = (instruction >> 16) & 0b111;
                JInstruction.getInstance().executeJ(stage, opcode, rs, rd);
            }
            // O-type
            case 0b110, 0b111 -> OInstruction.getInstance().executeO(stage, opcode);
        }
    }
}
