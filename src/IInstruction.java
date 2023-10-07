public class IInstruction {
    
    private static IInstruction instance ;

    private IInstruction(){}

    /**
     * Return the singleton instance of the class
     * @return is the instance of the class.
     */
    public static IInstruction getInstance(){
        if(instance == null){
            instance = new IInstruction();
        }

        return instance ;
    }

    /**
     * Execute an I-type instruction for the input state.
     * @param stage is the Stage object that is to be computed.
     * @param opcode is operation code of the instruction.
     * @param rs is the index of the primary source register.
     * @param rt is the index of the secondary source register.
     * @param offset is the offset of the instruction.
     */
    public void executeI(Stage stage,int opcode,int rs,int rt,int offset){
        
        int []reg = stage.getRegister() ;
        int []mem = stage.getMemory() ;
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
                int newPc = stage.getPc() + 1 + offset_field ;
                stage.setNextPc(newPc) ;
            }
        }
    }

    /**
     * Convert a 16-bit integer into a 32-bit integer.
     * @param num is the converting integer.
     * @return the converted integer.
     */
    private int sign_extend(int num){
        if ((num & (1<<15)) != 0) {
            num -= (1<<16);
        }

        return num;
    }
}
