import exceptions.DuplicatedLabel;
import exceptions.OverflowingField;
import exceptions.UndefinedLabel;
import exceptions.UnknownInstruction;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Assembler {
    static Map<String, Integer> labels = new HashMap<>();
    static List<String> lines;

    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("only 2 arguments are required. <assembly-code-file> <machine-code-file>");
            System.exit(1);
        }

        File inputFile = new File("src\\" + args[0]);
        File outputFile = new File("src\\" + args[1]);

        try {
            assemble(inputFile,outputFile);
        } catch (FileNotFoundException | DuplicatedLabel | UnknownInstruction | UndefinedLabel | OverflowingField e) {
            System.out.println("error: " + e.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }

    public static void assemble(File inputFile, File outputFile) throws DuplicatedLabel, FileNotFoundException,
            UnknownInstruction, UndefinedLabel, OverflowingField {

        Charset charset = StandardCharsets.UTF_8;
        Path inputPath;
        try {
            inputPath = Paths.get(inputFile.getAbsolutePath()).normalize();
        } catch (InvalidPathException e) {
            throw new FileNotFoundException(e.getMessage() + " cannot be converted to a path.");
        } catch (SecurityException e) {
            throw new FileNotFoundException(e.getMessage() + " cannot be accessed.");
        }

        String code;
        try { code = Files.readString(inputPath, charset); }
        catch (IOException e) { throw new FileNotFoundException(e.getMessage() + " cannot be found."); }

        lines = code.lines().toList();

        // Setting up labels
        int lineCount = 0;
        for(String line : lines) {
            lineCount++;
            String label = line.split(tab)[0];
            if(label.isBlank())
                continue;

            if(labels.put(label, lineCount-1) != null)
                throw new DuplicatedLabel("duplicated label \"" + label + "\" at line " + lineCount + ".");
        }

        // Creating machine-code
        StringBuilder sb = new StringBuilder();
        lineCount = 0;
        for(String line : lines) {
            String[] tokens = line.split(tab);
            int tokensLength = Math.min(tokens.length, 5);
            lineCount++;

            // empty line with no instruction
            if(tokensLength <= 1)
                throw new UnknownInstruction("no operator found at line " + lineCount + ".");

            int opcodeShift = 22;
            int field0Shift = 19;
            int field1Shift = 16;
            int field2Shift = 0;
            short opcode = getOpcode(tokens[1], lineCount);

            // O-type instructions
            if(opcode >= 0b110) {
                checkExcessTokens(tokens, 2, tokensLength, lineCount);
                sb.append(opcode << opcodeShift).append('\n');
                continue;
            }

            int i_field0 = variableInstance(tokens, 2, lineCount);

            // .fill instruction
            if(opcode == -1) {
                checkExcessTokens(tokens, 3, tokensLength, lineCount);
                // Since i_field0 is already 32 bits, there is no need to check for overflow.
                sb.append(i_field0).append('\n');
                continue;
            }

            int i_field1 = variableInstance(tokens, 3, lineCount);

            // check for overflowing fields
            checkOverflow(i_field0,0,7,tokens[2],lineCount);
            checkOverflow(i_field1,0,7,tokens[3],lineCount);
            short field0 = (short) i_field0;
            short field1 = (short) i_field1;

            int basicFields = (opcode<<opcodeShift) | (field0<<field0Shift) | (field1<<field1Shift);

            // J-type instruction
            if(opcode == 0b101) {
                checkExcessTokens(tokens, 4, tokensLength, lineCount);
                sb.append(basicFields).append('\n');
                continue;
            }

            // I-type instructions
            if(opcode >= 0b010) {
                int filter = 65535; // MAGIC!!!!
                int field2 = variableInstance(tokens, 4, lineCount, (opcode == 0b100));
                checkOverflow(field2,-32768,32767,tokens[4], lineCount);
                field2 &= filter;
                sb.append(basicFields | (field2 << field2Shift)).append('\n');
                continue;
            }

            // R-type instructions
            if(opcode >= 0b000) {
                int i_field2 = variableInstance(tokens, 4, lineCount);
                checkOverflow(i_field2, 0, 7, tokens[4], lineCount);
                short field2 = (short) i_field2;
                sb.append(basicFields | (field2 << field2Shift)).append('\n');
                continue;
            }

            // Program should not reach here if opcode is decoded properly.
            throw new UnknownInstruction("unexpected error has occurred during compilation.");
        }

        // Remove the last \n
        if(sb.length() > 0)
            sb.deleteCharAt(sb.length()-1);

        // Writing output file
        try {
            FileWriter outputWriter = new FileWriter(outputFile, false);
            outputWriter.write(sb.toString());
            outputWriter.close();
        } catch (IOException e) {
            throw new FileNotFoundException("unable to write " + e.getMessage() + ".");
        }
    }

    private static int variableInstance(String[] tokens, int index, int lineCount, boolean isLabelRelative) throws UndefinedLabel {
        String str;

        // Missing operand (Array out of bound)
        try {
            str = tokens[index];
        } catch (ArrayIndexOutOfBoundsException ignore) {
            throw new UndefinedLabel("missing operand at line " + lineCount + ".");
        }

        // Missing operand
        if(str.isBlank())
            throw new UndefinedLabel("missing operand at line " + lineCount + ".");

        try {
            if(index != 0)
                str = str.strip();
            return Integer.parseInt(str);
        } catch (NumberFormatException ignored) {}

        Integer parsedLabel = labels.get(str);
        if (parsedLabel != null)
            return parsedLabel - (isLabelRelative ? lineCount : 0);

        // Undefined label
        throw new UndefinedLabel("undefined label \"" + str + "\" at line " + lineCount + ".");
    }

    private static short getOpcode(String operator, int lineCount) throws UnknownInstruction {
        short opcode;
        switch (operator.strip().toUpperCase()) {
            case "ADD" -> opcode = 0b000;
            case "NAND" -> opcode = 0b001;
            case "LW" -> opcode = 0b010;
            case "SW" -> opcode = 0b011;
            case "BEQ" -> opcode = 0b100;
            case "JALR" -> opcode = 0b101;
            case "HALT" -> opcode = 0b110;
            case "NOOP" -> opcode = 0b111;
            case ".FILL" -> opcode = -1;
            case "" -> throw new UnknownInstruction("no operator found at line " + lineCount + ".");
            default -> throw new UnknownInstruction("unknown operator \"" + operator + "\" at line " + lineCount + ".");
        }
        return opcode;
    }

    private static int variableInstance(String[] tokens, int index, int lineCount) throws UndefinedLabel {
        return variableInstance(tokens,index,lineCount,false);
    }

    private static void checkExcessTokens(String[] tokens, int beginIndex, int endIndex, int lineCount) throws UnknownInstruction {
        for(int i = beginIndex; i < endIndex; i++)
            try {
                if (!tokens[i].isBlank())
                    throw new UnknownInstruction("excess operand " + tokens[i] + " at line " + lineCount + ".");
            } catch (ArrayIndexOutOfBoundsException ignore) { return; }
    }

    private static void checkOverflow(int number, int min, int max, String token, int lineCount) throws OverflowingField {
        if(number < min || number > max)
            throw new OverflowingField("overflowing field0 \"" + token + "\" at line " + lineCount + "\n" +
                    "The required field must be in between " + min + " and " + max + ".");
    }

    private static final String tab = "\t";
}
