public class Decoder {
    private static Decoder instance;
    private Decoder() {}

    public static Decoder getInstance() {
        if(instance == null)
            instance = new Decoder();
        return instance;
    }
    public void execute(Stage stage) {
        int instruction = stage.getInstruction();

        RInstruction rInstance = RInstruction.getInstance();
        IInstruction iInstance = IInstruction.getInstance();
        JInstruction jInstance = JInstruction.getInstance();
        OInstruction oInstance = OInstruction.getInstance();

        int opcode = (instruction >> 22) & 0b111;

        int rs, rt, rd;
        switch (opcode) {
            // R-Type
            case 0b000, 0b001 -> {
                rs = (instruction >> 19) & 0b111;
                rt = (instruction >> 16) & 0b111;
                rd = instruction & 0b111;
                rInstance.executeR(stage, opcode, rs, rt, rd);
            }
            // I-type
            case 0b010, 0b011, 0b100 -> {
                rs = (instruction >> 19) & 0b111;
                rt = (instruction >> 16) & 0b111;
                int offset = instruction & 0xFFFF;
                iInstance.executeI(stage, opcode, rs, rt, offset);
            }
            // J-Type
            case 0b101 -> {
                rs = (instruction >> 19) & 0b111;
                rd = (instruction >> 16) & 0b111;
                jInstance.executeJ(stage, opcode, rs, rd);
            }
            // O-type
            case 0b110, 0b111 -> oInstance.executeO(stage, opcode);
        }
    }
}
