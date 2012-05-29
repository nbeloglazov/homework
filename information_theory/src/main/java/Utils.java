import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Utils {

    private Utils () {}


    public static String encodeNumber(int number) {
        String binary = Integer.toBinaryString(number);
        String lenBinary = Integer.toBinaryString(binary.length());
        String bPref = StringUtils.repeat("0", lenBinary.length() - 1) + lenBinary;
        String bElias = bPref + binary.substring(1);
        return bElias;
    }

    public static List<Character> getAlphabet() {
        List<Character> alphabet = new ArrayList<Character>(32);
        for (int i = 0; i < 32; i++) {
            char ch = (char) (i + 'а');
            if (ch != 'ё' && ch != 'ъ') {
                alphabet.add(ch);
            }
        }
        alphabet.add(' ');
        return alphabet;
    }

    public static double averageLength(List<String> codes) {
        double sum = 0;
        for (String code : codes) {
            sum += code.length();
        }
        return sum / codes.size();
    }

    public static List<String> split(String text, int n) {
        List<String> parts = new ArrayList<String>();
        for (int i = 0; i + n < text.length(); i += n) {
            parts.add(text.substring(i, i + n));
        }
        return parts;
    }

    public static int totalLength(List<String> list) {
        int sum = 0;
        for (String element : list) {
            sum += element.length();
        }
        return sum;
    }

    public static <T> Map<T, Double> getFrequences(Collection<T> collection) {
        Map<T, Double> frequences = new HashMap<T, Double>();
        for (T obj : collection) {
            if (!frequences.containsKey(obj)) {
                frequences.put(obj, 0.0);
            }
            frequences.put(obj, frequences.get(obj) + 1);
        }
        for (T obj : frequences.keySet()) {
            frequences.put(obj, frequences.get(obj) / collection.size());
        }
        return frequences;
    }

    public static <T> double calcEntropy(Map<T, Double> frequences) {
        double result = 0;
        for (double prob : frequences.values()) {
            result += prob * Math.log(prob) / Math.log(2);
        }
        return -result;
    }

    public static List<Character> getCharacters(String str) {
        List<Character> characters = new ArrayList<Character>(str.length());
        for (char ch : str.toCharArray()) {
            characters.add(ch);
        }
        return characters;
    }
}
