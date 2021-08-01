package enigma;

import java.util.HashMap;
import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Yohan Yan
 */
class Permutation {

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** A permutation stored in a HashMap. */
    private HashMap<Character, Character> permutations;

    /** An inverse of the permutation stored in a HashMap. */
    private HashMap<Character, Character> inverse;

    /** Cycles stored in a string. */
    private String _cycles;

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        cycles = cycles.trim();
        cycles = cycles.replaceAll("\\s+", "");
        String reverse = "";
        for (int i = cycles.length() - 1; i > -1; i--) {
            reverse += cycles.charAt(i);
        }
        _cycles = cycles;
        permutations = new HashMap<>();
        if (cycles.length() > 0) {
            char first = cycles.charAt(1);
            for (int x = 0; x < cycles.length(); x++) {
                if (cycles.charAt(x) == '(') {
                    x++;
                    if (_alphabet.contains(cycles.charAt(x))) {
                        first = cycles.charAt(x);
                        if (cycles.charAt(x + 1) == ')') {
                            permutations.put(cycles.charAt(x), first);
                        } else {
                            permutations.put(cycles.charAt(x),
                                    cycles.charAt(x + 1));
                        }
                    }
                } else if (x + 1 < cycles.length()
                        && cycles.charAt(x + 1) == ')') {
                    if (_alphabet.contains(cycles.charAt(x))) {
                        permutations.put(cycles.charAt(x), first);
                        x += 1;
                    }
                } else if (cycles.charAt(x) == ')') {
                    if (_alphabet.contains(cycles.charAt(x - 1))) {
                        permutations.put(cycles.charAt(x - 1), first);
                    }
                } else if (_alphabet.contains(cycles.charAt(x))) {
                    permutations.put(cycles.charAt(x), cycles.charAt(x + 1));
                }
            }
        }
        addCycle(cycles);
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        String reverse = "";
        for (int i = cycle.length() - 1; i > -1; i--) {
            reverse += cycle.charAt(i);
        }
        inverse = new HashMap<>();
        if (reverse.length() > 0) {
            char first = reverse.charAt(1);
            for (int x = 0; x < reverse.length(); x++) {
                if (reverse.charAt(x) == ')') {
                    x++;
                    if (_alphabet.contains(reverse.charAt(x))) {
                        first = reverse.charAt(x);
                        if (reverse.charAt(x + 1) == '(') {
                            inverse.put(reverse.charAt(x), first);
                        } else {
                            inverse.put(reverse.charAt(x),
                                    reverse.charAt(x + 1));
                        }
                    }
                } else if (x + 1 < reverse.length()
                        && reverse.charAt(x + 1) == '(') {
                    if (_alphabet.contains(reverse.charAt(x))) {
                        inverse.put(reverse.charAt(x), first);
                        x += 1;
                    }
                } else if (reverse.charAt(x) == '(') {
                    if (_alphabet.contains(reverse.charAt(x - 1))) {
                        inverse.put(reverse.charAt(x - 1), first);
                    }
                } else if (_alphabet.contains(reverse.charAt(x))) {
                    inverse.put(reverse.charAt(x), reverse.charAt(x + 1));
                }
            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char letter = _alphabet.toChar(p);
        letter = permute(letter);
        return _alphabet.toInt(letter);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char letter = _alphabet.toChar(c);
        letter = invert(letter);
        return _alphabet.toInt(letter);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        Character letter = p;
        if (permutations != null) {
            if (permutations.containsKey(letter)) {
                return (char) permutations.get(letter);
            } else {
                return p;
            }
        } else {
            return p;
        }
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        Character letter = c;
        if (inverse != null) {
            if (inverse.containsKey(letter)) {
                return (char) inverse.get(letter);
            } else {
                return c;
            }
        } else {
            return c;
        }
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        return _alphabet.size() == permutations.size();
    }
}
