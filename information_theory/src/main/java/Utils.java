import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

    private Utils () {}


    public static String encodeNumber(int number) {
        String binary = Integer.toBinaryString(number);
        String bPref = StringUtils.repeat("0", binary.length() / 2) + Integer.toBinaryString(binary.length());
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
}
