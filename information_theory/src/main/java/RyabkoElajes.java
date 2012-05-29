import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RyabkoElajes {



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
        alphabet = Utils.getAlphabet();
    }

    public List<String> encode(String text) {
        bookStack = new LinkedList<String>();
        generateBookStack("");
        List<String> parts = Utils.split(text, k);
        List<String> codes = new ArrayList<String>(parts.size());
        l(parts.size());
        int counter = 0;
        for (String part : parts) {
            int position = bookStack.indexOf(part);
            bookStack.remove(part);
            bookStack.add(0, part);
            codes.add(Utils.encodeNumber(position + 1));
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
        l(Utils.averageLength(codes));
        l(Utils.totalLength(codes));
    }

    private static void l(Object obj) {
        System.out.println(obj);
    }
}
