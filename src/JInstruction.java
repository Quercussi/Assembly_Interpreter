public class JInstruction {
    
    private static JInstruction instance ;

    private JInstruction(){}

    /**
     * Return the singleton instance of the class
     * @return is the instance of the class.
     */
    public static JInstruction getInstance(){
        if(instance == null){
            instance = new JInstruction();
        }

        return instance ;
    }

    /**
     * Execute a J-type instruction for the input state.
     * @param stage is the Stage object that is to be computed.
     * @param opcode is operation code of the instruction.
     * @param rs is the index of the source register.
     * @param rd is the index of the destination register.
     */
    public void executeJ(Stage stage,int opcode,int rs,int rd){
        
        int []reg = stage.getRegister() ;
         
        if(opcode == 5){   // jalr instruction
            if(rd != 0){
                reg[rd] = stage.getPc() + 1 ;
            }
            stage.setNextPc(reg[rs]) ;
        }
    }
}
