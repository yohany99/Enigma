package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Yohan Yan
 */
class MovingRotor extends Rotor {

    /** A string of notches of the rotor. */
    private String _notches;

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    boolean atNotch() {
        for (int x = 0; x < _notches.length(); x++) {
            char letter = _notches.charAt(x);
            if (alphabet().toInt(letter) == setting()) {
                return true;
            }
        }
        return false;
    }

    @Override
    void advance() {
        set((setting() + 1) % size());
    }
}
