import exceptions.*;

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
    public enum Operator {
        ADD, NAND, LW, SW, BEQ, JALR, HALT, NOOP, FILL
    }
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
        } catch (FileNotFoundException | DuplicatedLabel | UnknownInstruction | UndefinedLabel | OverflowingField | IllegalLabel e) {
            System.out.println("error: " + e.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }

    public static void assemble(File inputFile, File outputFile) throws DuplicatedLabel, FileNotFoundException,
            UnknownInstruction, UndefinedLabel, OverflowingField, IllegalLabel {

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
            String label = line.split(tab)[0].strip();
            if(label.isBlank())
                continue;

            if(label.length() > 6)
                throw new IllegalLabel("illegal label \"" + label + "\" at line " + lineCount + ".\n" +
                        "Labels must be 6 characters or less.");

            if(!Character.isLetter(label.charAt(0)))
                throw new IllegalLabel("illegal label \"" + label + "\" at line " + lineCount + ".\n" +
                        "The first character in a label must be a letter.");

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
            Operator operator = getOperator(tokens[1], lineCount);

            // O-type instructions
            if(operator == Operator.HALT || operator == Operator.NOOP) {
                checkExcessTokens(tokens, 2, tokensLength, lineCount);
                sb.append(operator.ordinal() << opcodeShift).append('\n');
                continue;
            }

            int i_field0 = variableInstance(tokens, 2, lineCount);

            // .fill instruction
            if(operator == Operator.FILL) {
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

            int basicFields = (operator.ordinal()<<opcodeShift) | (field0<<field0Shift) | (field1<<field1Shift);

            // J-type instruction
            if(operator == Operator.JALR) {
                checkExcessTokens(tokens, 4, tokensLength, lineCount);
                sb.append(basicFields).append('\n');
                continue;
            }

            // I-type instructions
            if(operator == Operator.LW || operator == Operator.SW || operator == Operator.BEQ) {
                int filter = 65535; // MAGIC!!!!
                int field2 = variableInstance(tokens, 4, lineCount, (operator == Operator.BEQ));
                checkOverflow(field2,-32768,32767,tokens[4], lineCount);
                field2 &= filter;
                sb.append(basicFields | (field2 << field2Shift)).append('\n');
                continue;
            }

            // R-type instructions
            if(operator == Operator.ADD || operator == Operator.NAND) {
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
        if(!sb.isEmpty() && sb.charAt(sb.length()-1) == '\n')
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

    private static int variableInstance(String[] tokens, int index, int lineCount, boolean isLabelRelative) throws UndefinedLabel, UnknownInstruction {
        String str;

        // Missing operand (Array out of bound)
        try {
            str = tokens[index];
        } catch (ArrayIndexOutOfBoundsException ignore) {
            throw new UnknownInstruction("missing operand at line " + lineCount + ".");
        }

        // Missing operand
        if(str.isBlank())
            throw new UnknownInstruction("missing operand at line " + lineCount + ".");

        try {
            return Integer.parseInt(str.strip());
        } catch (NumberFormatException ignored) {}

        Integer parsedLabel = labels.get(str);
        if (parsedLabel != null)
            return parsedLabel - (isLabelRelative ? lineCount : 0);

        // Undefined label
        throw new UndefinedLabel("undefined label \"" + str + "\" at line " + lineCount + ".");
    }

    private static Operator getOperator(String str, int lineCount) throws UnknownInstruction {
        String strOperator = str.strip().toUpperCase();

        if(strOperator.isBlank())
            throw new UnknownInstruction("no operator found at line " + lineCount + ".");

        if(strOperator.equals(".FILL"))
            return Operator.FILL;

        try {
            return Operator.valueOf(strOperator);
        } catch (IllegalArgumentException ignore) {
            throw new UnknownInstruction("unknown operator \"" + strOperator + "\" at line " + lineCount + ".");
        }
    }

    private static int variableInstance(String[] tokens, int index, int lineCount) throws UndefinedLabel, UnknownInstruction {
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
