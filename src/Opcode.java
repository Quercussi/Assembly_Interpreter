public class Opcode {

    private static Opcode instance;

    private Opcode() {}

    public static Opcode getInstance() {
        if(instance == null)
            Opcode();
        return instance;
    }

public void einstructionecute(Stage stage) {
    int instruction = stage.getInstruction();

    // einstructiontract opcode

    // find instruction type
    // einstructiontract rs,rt,rd,offset, whatever.

    // call Instruction singleron
    // i.e. RInstructionInstance.einstructionecute(stage,rs,rt,rd);
    RInstruction rInstance = rInstruction.getInstance();
    IInstruction iInstance = iInstruction.getInstance();
    JInstruction jInstance = jInstruction.getInstance();
    Oinstruction oInstance = oInstruction.getInstanve();
    if (instruction == 0b000 || instruction == 0b001) {
            int opcode = (instruction >> 12) & 0b111;
            int Rs = (instruction >> 19) & 0b111;
            int Rt = (instruction >> 16) & 0b111;
            int Rd = instruction & 0b111;
            rInstance.einstructionecute(stage,opcode,Rs,Rt,Rd);
            // System.out.println("opcode = " +opcode);
            // System.out.println("rs = " + Rs);
            // System.out.println("rt = " + Rt);
            // System.out.println("rd = " + Rd);
        }
        // I-type
        if (instruction >= 0b010 && instruction <= 0b100) {
            int opcode = (instruction >> 22) & 0b111;
            int rs = (instruction >> 19) & 0b111;
            int rt = (instruction >> 16) & 0b111;
            iInstance.einstructionecute(stage,rs,rt);
            // System.out.println("opcode = " + opcode);
            // System.out.println("rd = " + rs);
            // System.out.println("rt = " + rt);
        }
        // J-type
        if (instruction == 0b101) {
            int opcode = (instruction >> 22) & 0b111;
            int rs = (instruction >> 19) & 0b111;
            int rd = (instruction >> 16) & 0b111;
            jInstance.einstructionecute(stage,rs,rd);
            // System.out.println("opcode = " + opcode);
            // System.out.println("rs = " + rs);
            // System.out.println("rd = " + rd);
        }
        // O-type
        if (instruction == 0b110 || instruction == 0b111) {
            int opcode = (instruction >> 22) & 0b111;
            oInstance.einstructionecute(stage,opcode);
            // System.out.println("opcode = " + opcode);
        }
            }


    public static void main(String[] args) {
   
    }

}










