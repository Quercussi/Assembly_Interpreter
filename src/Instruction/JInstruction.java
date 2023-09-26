package Instruction ;

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
        
        int []reg = stage.register ;
        
        if(rd == 0){
            return ;
        }
        
        if(opcode == 5){   // jalr instruction
            reg[rd] = stage.nextpc ;
            stage.pc = reg[rs]  ;
            if(rs == rd){
                stage.pc = reg[rs] + 1 ;
            }
        }
        
    }
    
}
