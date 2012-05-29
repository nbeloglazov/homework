import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;

public class Huffman {

    private String getText() throws Exception {
        return IOUtils.readLines(new FileReader("text.txt")).get(0);
    }

    private <T> Map<T, String> huffman(Map<T, Double> frequences) {
        List<List<T>> groups = new ArrayList<List<T>>();
        List<Double> probs = new ArrayList<Double>();
        Map<T, String> codes = new HashMap<T, String>();
        for (T obj : frequences.keySet()) {
            codes.put(obj, "");
            probs.add(frequences.get(obj));
            List<T> group = new ArrayList<T>();
            group.add(obj);
            groups.add(group);
        }
        while (groups.size() > 1) {
            int mn = 0;
            int mn2 = 1;
            for (int i = 2; i < probs.size(); i++) {
                if (probs.get(mn) > probs.get(i)) {
                    mn = i;
                } else if (probs.get(mn2) > probs.get(i)) {
                    mn2 = i;
                }
            }
            if (mn > mn2) {
                int tmp = mn;
                mn = mn2;
                mn2 = tmp;
            }
            List<T> group = groups.remove(mn2);
            for (T obj : group) {
                codes.put(obj, "1" + codes.get(obj));
            }
            for (T obj : groups.get(mn)) {
                codes.put(obj, "0" + codes.get(obj));
            }
            group.addAll(groups.remove(mn));
            groups.add(group);
            double prob = probs.remove(mn2) + probs.remove(mn);
            probs.add(prob);
        }
        return codes;
    }

    private <T> List<String> encode(Map<T, String> codes, Collection<T> collection) {
        List<String> result = new ArrayList<String>(collection.size());
        for (T obj : collection) {
            result.add(codes.get(obj));
        }
        return result;
    }

    private <T> double averageLength(Map<T, Double> frequences, Map<T, String> codes) {
        double result = 0;
        for (T obj : codes.keySet()) {
            result += codes.get(obj).length() * frequences.get(obj);
        }
        return result;
    }

    private <T> Map<T, String> runFrequencies(Map<T, Double> frequencies) throws Exception {
        l(frequencies);
        Map<T, String> codes = huffman(frequencies);
        l(codes);
        l("Averate length: " + averageLength(frequencies, codes));
        l("Entropy: " + Utils.calcEntropy(frequencies));
        return codes;
    }

    private <T> void encodeAndLog(Map<T, String> codes, Collection<T> collection) {
        List<String> encoded = encode(codes, collection);
        l("Total length " + Utils.totalLength(encoded));
    }

    private void solve() throws Exception {
        l("Standard frequencies");
        Map<Character, String> codes = runFrequencies(getRussianAlphabetFrequences());
        encodeAndLog(codes, Utils.getCharacters(getText()));

        l("");
        l("Based on text");
        codes = runFrequencies(Utils.getFrequences(Utils.getCharacters(getText())));
        encodeAndLog(codes, Utils.getCharacters(getText()));

        l("");
        l("Double letters");
        List<String> parts = Utils.split(getText(), 2);
        Map<String, String> codes2 = runFrequencies(Utils.getFrequences(parts));
        encodeAndLog(codes2, parts);
    }

    public static void main(String[] args) throws Exception {
        new Huffman().solve();
    }

    private static void l(Object obj) {
       System.out.println(obj);
    }

    private static Map<Character, Double> getRussianAlphabetFrequences() {
        return new HashMap<Character, Double>() {{
            put(' ', 0.175);
            put('о', 0.090);
            put('е', 0.072);
            put('а', 0.062);
            put('и', 0.062);
            put('т', 0.053);
            put('н', 0.053);
            put('с', 0.045);
            put('р', 0.040);
            put('в', 0.038);
            put('л', 0.035);
            put('к', 0.028);
            put('м', 0.026);
            put('д', 0.025);
            put('п', 0.023);
            put('у', 0.021);
            put('я', 0.018);
            put('ы', 0.016);
            put('з', 0.016);
            put('ь', 0.014);
            put('б', 0.014);
            put('г', 0.013);
            put('ч', 0.012);
            put('й', 0.010);
            put('х', 0.009);
            put('ж', 0.007);
            put('ю', 0.006);
            put('ш', 0.006);
            put('ц', 0.004);
            put('щ', 0.003);
            put('э', 0.003);
            put('ф', 0.002);
        }};
    }
}