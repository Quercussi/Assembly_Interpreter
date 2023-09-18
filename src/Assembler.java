import exceptions.DuplicatedLabel;
import exceptions.OverflowingField;
import exceptions.UndefinedLabel;
import exceptions.UnknownInstruction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            String label = line.split(tab)[0].strip();
            if(label.equals(""))
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
            short opcode;
            String operator = tokens[1].strip().toUpperCase();
            switch (operator) {
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
                default -> throw new UnknownInstruction("unknown operator \"" + tokens[1] + "\" at line " + lineCount + ".");
            }

            // O-type instructions
            if(opcode >= 0b110) {
                checkExcessTokens(tokens, 2, tokensLength, lineCount);
                sb.append(opcode << opcodeShift).append('\n');
                continue;
            }

            int i_field0 = variableInstance(tokens[2].strip(), lineCount);

            // .fill instruction
            if(opcode == -1) {
                checkExcessTokens(tokens, 3, tokensLength, lineCount);

                // Since i_field0 is already 32 bits, there is no need to check for overflow.
                sb.append(i_field0).append('\n');
                continue;
            }

            int i_field1 = variableInstance(tokens[3].strip(), lineCount);

            // check for overflowing fields
            if(i_field0 < 0 || i_field0 > 7)
                throw new OverflowingField("overflowing field0 \"" + tokens[2] + "\" at line " + lineCount + "." +
                        "The required field must be in between 0 and 7.");
            if(i_field1 < 0 || i_field1 > 7)
                throw new OverflowingField("overflowing field1 \"" + tokens[3] + "\" at line " + lineCount + "." +
                        "The required field must be in between 0 and 7.");
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
                int field2 = variableInstance(tokens[4].strip(), lineCount, (opcode == 0b100));
                // BEQ use field2 differently.
                if(field2 < -32768 || field2 > 32767)
                    throw new OverflowingField("overflowing field2 \"" + tokens[4] + "\" at line " + lineCount + ".\n" +
                            "The required field for R-type instruction must be in between -32768 and 32676.");
                field2 &= filter;
                sb.append(basicFields | (field2 << field2Shift)).append('\n');
                continue;
            }

            // R-type instructions
            int i_field2 = variableInstance(tokens[4].strip(), lineCount);
            if(i_field2 < 0 || i_field2 > 7)
                throw new OverflowingField("overflowing field2 \"" + tokens[4] + "\" at line " + lineCount + ".\n" +
                        "The required field for R-type instruction must be in between 0 and 7.");
            short field2 = (short) i_field2;
            sb.append(basicFields | (field2 << field2Shift)).append('\n');
        }
        if(sb.length() > 0)
            sb.deleteCharAt(sb.length()-1); // Remove the last \n

        // Writing output file
        try {
            FileWriter outputWriter = new FileWriter(outputFile, false);
            outputWriter.write(sb.toString());
            outputWriter.close();
        } catch (IOException e) {
            throw new FileNotFoundException("unable to write " + e.getMessage() + ".");
        }
    }

    private static int variableInstance(String str, int line, boolean isLabelRelative) throws UndefinedLabel {
        if(str.equals(""))
            throw new UndefinedLabel("missing operand at line " + line + ".");

        try {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException ignored) {}

        Integer parsedLabel = labels.get(str);
        if (parsedLabel != null)
            return parsedLabel - (isLabelRelative ? line : 0);

        throw new UndefinedLabel("undefined label \"" + str + "\" at line " + line + ".");
    }

    private static int variableInstance(String str, int line) throws UndefinedLabel {
        return variableInstance(str,line,false);
    }

    private static void checkExcessTokens(String[] tokens, int beginIndex, int endIndex, int line) throws UnknownInstruction {
        for(int i = beginIndex; i < endIndex; i++)
            if(!tokens[i].isBlank())
                throw new UnknownInstruction("excess operand " + tokens[i] + " at line " + line + ".");
    }

    private static final String tab = "\t";
}
