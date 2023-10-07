public class RInstruction {
   
    private static RInstruction instance ;

    private RInstruction(){}

    /**
     * Return the singleton instance of the class
     * @return is the instance of the class.
     */
    public static RInstruction getInstance(){
        if(instance == null){
            instance = new RInstruction();
        }

        return instance ;
    }

    /**
     * Execute an R-type instruction for the input state.
     * @param stage is the Stage object that is to be computed.
     * @param opcode is operation code of the instruction.
     * @param rs is the index of the primary source register.
     * @param rt is the index of the secondary source register.
     * @param rd is the index of the destination register.
     */
    public void executeR(Stage stage,int opcode,int rs,int rt,int rd){
        
        int []reg = stage.getRegister() ;

        if(rd == 0){
            return; // does nothing.
        }

        if(opcode == 0){        // add instruction
            reg[rd] = reg[rs] + reg[rt] ;
        }else if(opcode == 1){  // nand instruction
            reg[rd] = ~(reg[rs] & reg[rt]) ;
        }
    }
}
