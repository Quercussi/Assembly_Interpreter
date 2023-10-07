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

    // A map storing each label and its correlated address
    static Map<String, Integer> labels = new HashMap<>();
    // A list storing each lines in the assembly code.
    static List<String> lines;

    public static void main(String[] args) {
        // Check whether there is an invalid number of arguments.
        if(args.length != 2) {
            System.out.println("only 2 arguments are required. <assembly-code-file> <machine-code-file>");
            System.exit(1);
        }


        File inputFile = new File("src\\" + args[0]);
        File outputFile = new File("src\\" + args[1]);
        try {
            assemble(inputFile,outputFile); // Compile the code

        } catch (FileNotFoundException | DuplicatedLabel | UnknownInstruction | UndefinedLabel | OverflowingField | IllegalLabel e) {
            // Unable to open the file.
            System.out.println("error: " + e.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }

    /**
     * Compile the assembly code from input file and the return out the machine code in outputFile
     * @param inputFile is the file storing input assembly code.
     * @param outputFile is the file to which the output machine code will be stored.
     */
    public static void assemble(File inputFile, File outputFile) throws DuplicatedLabel, FileNotFoundException,
            UnknownInstruction, UndefinedLabel, OverflowingField, IllegalLabel {

        // Attempt to open the input file.
        Charset charset = StandardCharsets.UTF_8;
        Path inputPath;
        try {
            inputPath = Paths.get(inputFile.getAbsolutePath()).normalize();
        } catch (InvalidPathException e) {
            throw new FileNotFoundException(e.getMessage() + " cannot be converted to a path.");
        } catch (SecurityException e) {
            throw new FileNotFoundException(e.getMessage() + " cannot be accessed.");
        }

        // Get the assembly code from the input file.
        String code;
        try { code = Files.readString(inputPath, charset); }
        catch (IOException e) { throw new FileNotFoundException(e.getMessage() + " cannot be found."); }

        lines = code.lines().toList();

        // Setting up labels
        int lineCount = 0;
        for(String line : lines) {
            lineCount++;
            String label = line.split(tab)[0].strip();
            if(label.isBlank()) // Ignore lines without the label assignment.
                continue;

            // Check whether the label is not too long
            if(label.length() > 6)
                throw new IllegalLabel("illegal label \"" + label + "\" at line " + lineCount + ".\n" +
                        "Labels must be 6 characters or less.");

            // Check whether the label starts with a letter.
            if(!Character.isLetter(label.charAt(0)))
                throw new IllegalLabel("illegal label \"" + label + "\" at line " + lineCount + ".\n" +
                        "The first character in a label must be a letter.");

            // Check whether there are duplicated label assignments.
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

            // Check whether there is an operator at all.
            if(tokensLength <= 1)
                throw new UnknownInstruction("no operator found at line " + lineCount + ".");

            int opcodeShift = 22;
            int field0Shift = 19;
            int field1Shift = 16;
            int field2Shift = 0;
            Operator operator = getOperator(tokens[1], lineCount);

            // Store an O-type instruction.
            if(operator == Operator.HALT || operator == Operator.NOOP) {
                checkExcessTokens(tokens, 2, tokensLength, lineCount);
                sb.append(operator.ordinal() << opcodeShift).append('\n');
                continue;
            }

            // Get the zeroth field.
            int i_field0 = variableInstance(tokens, 2, lineCount);

            // Store a .fill assignment.
            if(operator == Operator.FILL) {
                checkExcessTokens(tokens, 3, tokensLength, lineCount);
                // Since .fill is for storing 32 bits, there is no need to check for overflow.
                sb.append(i_field0).append('\n');
                continue;
            }

            // Get the first field.
            int i_field1 = variableInstance(tokens, 3, lineCount);

            // check for overflowing fields
            checkOverflow(i_field0,0,7,tokens[2],lineCount);
            checkOverflow(i_field1,0,7,tokens[3],lineCount);
            short field0 = (short) i_field0;
            short field1 = (short) i_field1;

            // concatenate opcode, field0, and field1.
            int basicFields = (operator.ordinal()<<opcodeShift) | (field0<<field0Shift) | (field1<<field1Shift);

            // Store a J-type instruction.
            if(operator == Operator.JALR) {
                checkExcessTokens(tokens, 4, tokensLength, lineCount);
                sb.append(basicFields).append('\n');
                continue;
            }

            // Store an I-type instruction.
            if(operator == Operator.LW || operator == Operator.SW || operator == Operator.BEQ) {
                int filter = 65535; // MAGIC!!!!

                // Get the second field (16 bits).
                int field2 = variableInstance(tokens, 4, lineCount, (operator == Operator.BEQ));
                checkOverflow(field2,-32768,32767,tokens[4], lineCount);
                field2 &= filter;

                sb.append(basicFields | (field2 << field2Shift)).append('\n');
                continue;
            }

            // R-type instructions
            if(operator == Operator.ADD || operator == Operator.NAND) {
                // Get the second field (3 bits).
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

        // Write machine code onto the output file.
        try {
            FileWriter outputWriter = new FileWriter(outputFile, false);
            outputWriter.write(sb.toString());
            outputWriter.close();
        } catch (IOException e) {
            throw new FileNotFoundException("unable to write " + e.getMessage() + ".");
        }
    }

    /**
     * Get the value from a token and automatically check whether it is valid.
     * @param tokens is the list of tokens within a line.
     * @param index is the index of the token list that is to be evaluated.
     * @param lineCount is the line number that the token list is extracted from.
     * @param isLabelRelative isLabelRelative is used to specify whether the field
     *                        should be differentiated with the line address.
     *                        This argument is specifically used for the BEQ instruction.
     * @return the variable at index if there are no exceptions.
     * @throws UndefinedLabel if a label is called without any assignment.
     * @throws UnknownInstruction if there are missing operands.
     */
    private static int variableInstance(String[] tokens, int index, int lineCount, boolean isLabelRelative)
            throws UndefinedLabel, UnknownInstruction {
        String str;

        // Missing operand (Array out of bound)
        try {
            str = tokens[index].strip();
        } catch (ArrayIndexOutOfBoundsException ignore) {
            throw new UnknownInstruction("missing operand at line " + lineCount + ".");
        }

        // Missing operand (whitespace)
        if(str.isBlank())
            throw new UnknownInstruction("missing operand at line " + lineCount + ".");

        // Parse integer
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ignored) {}

        // Get the label address
        Integer parsedLabel = labels.get(str);
        if (parsedLabel != null)
            return parsedLabel - (isLabelRelative ? lineCount : 0);

        // Undefined label
        throw new UndefinedLabel("undefined label \"" + str + "\" at line " + lineCount + ".");
    }

    /**
     * Get the value from a token and automatically check whether it is valid
     * for non-BEQ instructions.
     * @param tokens is the list of tokens within a line.
     * @param index is the index of the token list that is to be evaluated.
     * @param lineCount is the line number that the token list is extracted from.
     * @return the variable at index if there are no exceptions.
     * @throws UndefinedLabel if a label is called without any assignment.
     * @throws UnknownInstruction if there are missing operands.
     */
    private static int variableInstance(String[] tokens, int index, int lineCount) throws UndefinedLabel, UnknownInstruction {
        return variableInstance(tokens,index,lineCount,false);
    }

    /**
     * Get the operator as an enumerated object
     * @param str is the input operator as string
     * @param lineCount is the line number that the token list is extracted from.
     * @return the operator as an enumerated object.
     * @throws UnknownInstruction if the input string is not a valid operator.
     */
    private static Operator getOperator(String str, int lineCount) throws UnknownInstruction {
        String strOperator = str.strip().toUpperCase();

        // Check whether there is an operator.
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

    /**
     * Check whether there are excess fields.
     * @param tokens is the list of tokens within a line.
     * @param beginIndex is the first index that will be checked.
     * @param endIndex is the last index that will be checked.
     * @param lineCount is the line number that the token list is extracted from.
     * @throws UnknownInstruction if there is an excess field.
     */
    private static void checkExcessTokens(String[] tokens, int beginIndex, int endIndex, int lineCount) throws UnknownInstruction {
        for(int i = beginIndex; i < endIndex; i++)
            try {
                if (!tokens[i].isBlank())
                    throw new UnknownInstruction("excess operand " + tokens[i] + " at line " + lineCount + ".");
            } catch (ArrayIndexOutOfBoundsException ignore) { return; }
    }

    /**
     * Check whether the input number is within the input range.
     * @param number is the input number that will be checked.
     * @param min is the lower bound of the input range.
     * @param max is the upper bound of the input range.
     * @param token is the additional string for exception message.
     * @param lineCount is the line number that the token is extracted from.
     * @throws OverflowingField if the input number is out of range.
     */
    private static void checkOverflow(int number, int min, int max, String token, int lineCount) throws OverflowingField {
        if(number < min || number > max)
            throw new OverflowingField("overflowing field0 \"" + token + "\" at line " + lineCount + "\n" +
                    "The required field must be in between " + min + " and " + max + ".");
    }

    private static final String tab = "\t";
}
