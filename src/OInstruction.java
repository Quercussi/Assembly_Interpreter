public class OInstruction {
    
    private static OInstruction instance ;

    private OInstruction(){}

    public static OInstruction getInstance(){
        if(instance == null){
            instance = new OInstruction();
        }

        return instance ;
    }
    
    public void executeO(Stage stage,int opcode){
        if(opcode == 6){       // halt instruction
            stage.setHalt() ;
        }else if(opcode == 7){ // noop instruction

        }
    }
}
