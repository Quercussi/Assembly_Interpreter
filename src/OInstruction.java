public class OInstruction {
    
    private static OInstruction instance;

    private OInstruction(){}

    /**
     * Return the singleton instance of the class
     * @return is the instance of the class.
     */
    public static OInstruction getInstance(){
        if(instance == null){
            instance = new OInstruction();
        }

        return instance ;
    }

    /**
     * Execute an O-type instruction for the input state.
     * @param stage is the Stage object that is to be computed.
     * @param opcode is operation code of the instruction.
     */
    public void executeO(Stage stage,int opcode){
        if(opcode == 6){       // halt instruction
            stage.setHalt();
        }else if(opcode == 7){ // noop instruction
            // does nothing.
        }
    }
}
