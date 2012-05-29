import org.apache.commons.io.IOUtils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LempelZiv {

    private Map<Character, Integer> letters;

    public LempelZiv() {
        letters = new HashMap<Character, Integer>();
        List<Character> alphabet = Utils.getAlphabet();
        for (int i = 0; i < alphabet.size(); i++) {
            letters.put(alphabet.get(i), i);
        }
    }

    public List<String> encode(String text) {

        Map<String, Integer> prefixes = new HashMap<String, Integer>();
        int count = 0;
        prefixes.put("", 0);
        List<String> codes = new ArrayList<String>();
        String prefix = "";
        for (char ch : text.toCharArray()) {
            int letter = letters.get(ch);
            for (int i = 0; i < 5; i++) {
                char bit = (letter & (1 << i)) == 0 ? '0' : '1';
                String next = prefix + bit;
                if (!prefixes.containsKey(next)) {
                    int code = prefixes.get(prefix) + 1;
                    codes.add(Utils.encodeNumber(code) + bit);

                    prefixes.put(next, ++count);
                    prefix = "";
                } else {
                    prefix = next;
                }
            }
        }
        int n = text.length() * 5;
        System.out.println("C(n) = " + (prefixes.size() - 1));
        System.out.println("Should be less than " + (n / Math.log(n)));
        return codes;
    }

    public static void main(String[] args) throws Exception {
        LempelZiv lempelZiv = new LempelZiv();
        String text = IOUtils.readLines(new FileReader("text.txt")).get(0);
        List<String> codes = lempelZiv.encode(text);
        System.out.println(Utils.totalLength(codes));

    }
}
