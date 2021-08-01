package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Scanner;
import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Yohan Yan
 */
public final class Main {

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }
        _config = getInput(args[0]);
        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        String[] certainRotors = new String[enigma.numRotors()];
        while (_input.hasNextLine()) {
            if (_input.hasNext("\\*")) {
                String line = _input.nextLine();
                if (line.trim().equals("")) {
                    _output.append("\n");
                    continue;
                }
                Scanner scanner = new Scanner(line);
                scanner.next();
                for (int x = 0; x < enigma.numRotors(); x++) {
                    String rotor = scanner.next().toUpperCase();
                    certainRotors[x] = rotor;
                }
                enigma.insertRotors(certainRotors);
                String setting = scanner.next();
                if (setting.length() != enigma.numRotors() - 1) {
                    throw new EnigmaException(
                            "Settings and rotors do not match");
                }
                enigma.setRotors(setting);
                if (scanner.hasNext()) {
                    enigma.setPlugboard(new Permutation(scanner.nextLine(),
                            _alphabet));
                }
            } else {
                String message = _input.nextLine();
                printMessageLine(enigma.convert(message));
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            int rotors = 0;
            int pawls = 0;
            if (_config.hasNext()) {
                String s = _config.nextLine();
                _alphabet = new CharacterRange(s.charAt(0),
                        s.charAt(s.length() - 1));
            }
            if (_config.hasNext()) {
                rotors = _config.nextInt();
                pawls = _config.nextInt();
            }
            Collection<Rotor> allRotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                ((ArrayList<Rotor>) allRotors).add(readRotor());
            }
            return new Machine(_alphabet, rotors, pawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String name = _config.next().toUpperCase();
            String toMove = _config.next();
            String perm = _config.nextLine();
            Permutation permute = new Permutation(perm, _alphabet);
            if (toMove.length() > 1) {
                return new MovingRotor(name, permute, toMove.substring(1));
            } else if (toMove.charAt(0) == 'N') {
                return new FixedRotor(name, permute);
            } else {
                if (!permute.derangement()) {
                    perm += _config.nextLine();
                    permute = new Permutation(perm, _alphabet);
                }
                return new Reflector(name, permute);
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        String finalMsg = "";
        if (msg.length() != 0) {
            finalMsg += msg.charAt(0);
            for (int i = 1; i < msg.length(); i++) {
                if (i % 5 == 0) {
                    finalMsg += " ";
                }
                finalMsg += msg.charAt(i);
            }
        }
        _output.append(finalMsg + "\n");
    }
}
