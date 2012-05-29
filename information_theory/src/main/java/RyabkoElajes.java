import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RyabkoElajes {


    private static String encodeNumber(int number) {
        String binary = Integer.toBinaryString(number);
        String bPref = StringUtils.repeat("0", binary.length() / 2) + Integer.toBinaryString(binary.length());
        String bElias = bPref + binary.substring(1);
        return bElias;
    }

    private static List<Character> getAlphabet() {
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

    private static List<String> split(String text, int n) {
        List<String> parts = new ArrayList<String>();
        for (int i = 0; i + n < text.length(); i += n) {
            parts.add(text.substring(i, i + n));
        }
        return parts;
    }

    private List<String> bookStack;
    private List<Character> alphabet;
    private int k;

    private void generateBookStack(String word) {
        if (word.length() == k) {
            bookStack.add(word);
        } else {
            for (char ch : alphabet) {
                generateBookStack(word + ch);
            }
        }
    }

    public RyabkoElajes(int k) {
        this.k = k;
        alphabet = getAlphabet();
    }

    public List<String> encode(String text) {
        bookStack = new LinkedList<String>();
        generateBookStack("");
        List<String> parts = split(text, k);
        List<String> codes = new ArrayList<String>(parts.size());
        l(parts.size());
        int counter = 0;
        for (String part : parts) {
            int position = bookStack.indexOf(part);
            bookStack.remove(part);
            bookStack.add(0, part);
            codes.add(encodeNumber(position + 1));
            if (counter++ %  1000 == 0) {
                l(counter);
            }
        }
        return codes;
    }

    public static void main(String[] args) throws Exception {
        RyabkoElajes encoder = new RyabkoElajes(2);
        String text = IOUtils.readLines(new FileReader("text.txt")).get(0);
        List<String> codes = encoder.encode(text);
        l(averageLength(codes));
    }

    private static void l(Object obj) {
        System.out.println(obj);
    }
}
