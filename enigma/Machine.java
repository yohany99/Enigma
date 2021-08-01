package enigma;
import java.util.Collection;
import java.util.HashSet;
import static enigma.EnigmaException.*;


/** Class that represents a complete enigma machine.
 *  @author Yohan Yan
 */
class Machine {

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** The number of rotors. */
    private int _numRotors;

    /** The number of pawls. */
    private int _pawls;

    /** A collection of the rotors that the machine has access to. */
    private Collection<Rotor> _allRotors;

    /** An array of rotors that the machine uses. */
    private Rotor[] _selectedRotors;

    /** Permutation of the plugboard. */
    private Permutation _plugboard;

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
        _selectedRotors = new Rotor[numRotors];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        int count = 0;
        int secondCount = 0;
        for (int x = 0; x < rotors.length; x++) {
            for (Rotor r : _allRotors) {
                if (rotors[x].equals(r.name())) {
                    _selectedRotors[x] = r;
                    if (r.rotates()) {
                        secondCount += 1;
                    }
                    count = 0;
                    break;
                } else {
                    count += 1;
                    if (count >= _allRotors.size()) {
                        throw error("Rotor does not exist");
                    }
                }
            }
        }
        if (numPawls() != secondCount) {
            throw new EnigmaException("Not all moving rotors have a pawl");
        }
        for (int i = 0; i < _selectedRotors.length; i++) {
            for (int j = i + 1; j < _selectedRotors.length; j++) {
                if (_selectedRotors[i].name()
                        .equals(_selectedRotors[j].name())) {
                    throw new EnigmaException("Duplicate");
                }
            }
        }
        if (!_selectedRotors[0].reflecting()) {
            throw new EnigmaException("First rotor is not a reflector");
        }
        for (int x = 1; x < _selectedRotors.length; x++) {
            if (_selectedRotors[x].reflecting()) {
                throw new EnigmaException("More than one reflector");
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 upper-case letters. The first letter refers to the
     *  leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != numRotors() - 1) {
            throw new EnigmaException("Settings and rotors do not match");
        }
        for (int x = 1; x < numRotors(); x++) {
            if (_alphabet.contains(setting.charAt(x - 1))) {
                _selectedRotors[x].set(setting.charAt(x - 1));
            } else {
                throw new EnigmaException("Not in alphabet");
            }
        }
    }

    /** @return an array. */
    Rotor[] selectRotors() {
        return _selectedRotors;
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        if (_plugboard != null) {
            c = _plugboard.permute(c);
        }
        HashSet<Integer> rotatedRotors = new HashSet<>();
        rotatedRotors.add(_selectedRotors.length - 1);
        for (int x = _selectedRotors.length - 1; x > -1; x--) {
            if (_selectedRotors[x].atNotch()) {
                if (_selectedRotors[x - 1].rotates()) {
                    rotatedRotors.add(x);
                    rotatedRotors.add(x - 1);
                }
            }
        }
        for (int x : rotatedRotors) {
            _selectedRotors[x].advance();
        }
        for (int y = _selectedRotors.length - 1; y > -1; y--) {
            c = _selectedRotors[y].convertForward(c);
        }
        for (int y = 1; y < _selectedRotors.length; y++) {
            c = _selectedRotors[y].convertBackward(c);
        }
        if (_plugboard != null) {
            c = _plugboard.permute(c);
        }
        return c;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        if (_selectedRotors[0] == null) {
            throw new EnigmaException("No rotors");
        }
        msg = msg.replaceAll("\\s+", "");
        if (msg != null) {
            msg = msg.toUpperCase();
        }
        String message = "";
        for (int y = 0; y < msg.length(); y++) {
            char c = msg.charAt(y);
            message += _alphabet.toChar(convert(_alphabet.toInt(c)));
        }
        return message;
    }
}
