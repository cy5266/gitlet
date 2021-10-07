package enigma;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Cindy Yang
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
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
    void insertRotors(String[] rotors)
    {
//        if (_numRotors != _allRotors.size()) {
//            throw new EnigmaException("not the correct size");
//        }
        for (String element: rotors) {
            for (Rotor r: _allRotors) {
                if (r.name().equals(element)) {
                    _selectedRotors.add(r);
                }
            }
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
//        if (setting.length() != numRotors()-1) {
//            throw new EnigmaException("length not equal");
//        }
        for (int i = 0; i < _selectedRotors.size(); i ++) {
            _selectedRotors.get(i).set(_alphabet.toInt(setting.charAt(i -1)));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        int plugNum = _plugboard.permute(c);
        for (int i = 0; i < _selectedRotors.size(); i ++) {
            //if it's the right most rotor OR the one on the right is at a notch AND it rotates, then rotate
            //if it's NOT at the right most rotar, then you advance the right one, and SKIP i
            Rotor r = _selectedRotors.get(i);
            if (r.rotates() && (i == _selectedRotors.size()-1
                    || _selectedRotors.get(i+1).atNotch())){
                r.advance();
            }
            if (i < _selectedRotors.size()-1){
                _selectedRotors.get(i+1).advance();
                i ++;
            }
        }

        for (int i = _selectedRotors.size() - 1; i >= 0; i--) {
            _selectedRotors.get(i).convertForward(plugNum);
        }

        for (int i = 1; i < _selectedRotors.size() - 1; i ++) {
            _selectedRotors.get(i).convertBackward(plugNum);
        }

    return _plugboard.permute(plugNum);
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String finalStr = "";
       char[] chars = msg.toCharArray();
       for (char c: chars){
           int passIn = _alphabet.toInt(c);
           char newChar = _alphabet.toChar(convert(passIn));
           finalStr += newChar;
       }
        return finalStr; // FIXME
    }

    public ArrayList<Rotor> get_selectedRotors(){
        return _selectedRotors;
    }

    public Collection<Rotor> getallRotors(){
        return _allRotors;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;
    private int _numRotors;
    private int _pawls;
    private Collection<Rotor> _allRotors;
    ArrayList<Rotor> _selectedRotors = new ArrayList<Rotor>();
    private Permutation _plugboard;
    // FIXME: ADDITIONAL FIELDS HERE, IF NEEDED.
}
