
    import RInstruction;
    import IInstruction;
    import JInstruction;
    import OInstruction;
    import Stage;

public class Decoder {

    private static Decoder instance;

    private Decoder() {}

    public static Decoder getInstance() {
        if(instance == null)
            instance = Decoder();
        return instance;
    }
public void execute(Stage stage) {
    int instruction = stage.getInstruction();

    // einstructiontract opcode

    // find instruction type
    // einstructiontract rs,rt,rd,offset, whatever.

    // call Instruction singleron
    // i.e. RInstructionInstance.instructionexecute(stage,rs,rt,rd);
    RInstruction rInstance = RInstruction.getInstance();
    IInstruction iInstance = IInstruction.getInstance();
    JInstruction jInstance = JInstruction.getInstance();
    OInstruction oInstance = OInstruction.getInstance();

    int instuction = stage.getInstruction();
    int opcode = (instruction >> 22) & 0b111;

    int rs, rt, rd;
        // R-Type
        switch (opcode) {
            case 0b000:
            case 0b001:
                // R-Type
                 
                 rs = (instruction >> 19) & 0b111;
                 rt = (instruction >> 16) & 0b111;
                 rd = instruction & 0b111;
                rInstance.execute(stage, opcode, rs, rt, rd);
                break;
        // I-type
            case 0b010:
            case 0b011:
            case 0b100:
                // I-Type
           
                 rs = (instruction >> 19) & 0b111;
                 rt = (instruction >> 16) & 0b111;
                int offset = instruction & 0xFFFF;
                iInstance.execute(stage, opcode, rs, rt, offset);
                break;
         // J-Type
                case 0b101:
               
                 rs = (instruction >> 19) & 0b111;
                 rd = (instruction >> 16) & 0b111;
                jInstance.execute(stage, opcode, rs, rd);
                break;
        // O-type
                case 0b110:
                case 0b111:
               
                oInstance.execute(stage, opcode);
                break;
            }
}

}
