public class JInstruction {
    
    private static JInstruction instance ;

    private JInstruction(){} ;

    public static JInstruction getInstance(){
        if(instance == null){
            instance = new JInstruction();
        }

        return instance ;
    }
    
    public void executeJ(Stage stage,int opcode,int rs,int rd){
        
        int []reg = stage.getRegister() ;
         
        if(opcode == 5){   // jalr instruction
            if(rd != 0){
                reg[rd] = stage.getPc() + 1 ;
            }
            stage.setNextPc(reg[rs]) ;
            if(rs == rd){
                int newPc = stage.getPc() + 1 ;
                stage.setNextPc(newPc) ;
            }
        }
        
    }

    
}
