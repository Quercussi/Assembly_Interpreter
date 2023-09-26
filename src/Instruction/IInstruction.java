package Instruction ;

public class IInstruction {
    
    private static IInstruction instance ;

    private IInstruction(){} ;

    public static IInstruction getInstance(){
        if(instance == null){
            instance = new IInstruction();
        }

        return instance ;
    }

    public void executeI(Stage stage,int opcode,int rs,int rt,int offset){
        
        int []reg = stage.register ;
        int []mem = stage.memory ;
        int offset_field = sign_extend(offset) ;
        int mem_address = offset_field + reg[rs] ;

        if(opcode == 2){         // lw instruction
            if(rt == 0){
                return ;
            }      
            reg[rt] = mem[mem_address] ;
        }else if(opcode == 3){   // sw instruction
            mem[mem_address] = reg[rt] ;
        }else if(opcode == 4){   // beq instruction
            if(reg[rs] == reg[rt]){
                stage.pc = stage.nextpc + offset_field ;
            }
        }


    }

    public int sign_extend(int num){
       
        if ((num & (1<<15)) != 0) {
            num -= (1<<16);
        }

        return num;
    }
    
}
