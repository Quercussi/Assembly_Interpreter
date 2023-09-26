package Instruction ;

public class RInstruction {
   
    private static RInstruction instance ;

    private RInstruction(){} ;

    public static RInstruction getInstance(){
        if(instance == null){
            instance = new RInstruction();
        }

        return instance ;
    }

    public void executeR(Stage stage,int opcode,int rs,int rt,int rd){
        
        int []reg = stage.register ;

        if(rd == 0){
            return ;
        }

        if(opcode == 0){        // add instruction               
            reg[rd] = reg[rs] + reg[rt] ;
        }else if(opcode == 1){  // nand instruction
            reg[rd] = ~(reg[rs] & reg[rt]) ;
        }
    }

}
