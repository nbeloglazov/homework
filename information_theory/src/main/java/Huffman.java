import java.io.*;
import java.util.*;

public class Huffman {

    private String getText() throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
        while (reader.ready()) {
            builder.append(reader.readLine());
        }
        reader.close();
        return builder.toString();
    }

    private Map<Character, Double> getFrequences(String text) {
        Map<Character, Double> frequences = new HashMap<Character, Double>();
        for (char ch : text.toCharArray()) {
            if (!frequences.containsKey(ch)) {
                frequences.put(ch, 0.0);
            }
            frequences.put(ch, frequences.get(ch) + 1);
        }
        for (char ch : frequences.keySet()) {
            frequences.put(ch, frequences.get(ch) / text.length());
        }
        return frequences;
    }

    private double calcEntropy(Map<Character, Double> frequences) {
        double result = 0;
        for (double prob : frequences.values()) {
            result += prob * Math.log(prob) / Math.log(2);
        }
        return -result;
    }

    private Map<Character, String> huffman(Map<Character, Double> frequences) {
        List<List<Character>> groups = new ArrayList<List<Character>>();
        List<Double> probs = new ArrayList<Double>();
        Map<Character, String> codes = new HashMap<Character, String>();
        for (char ch : frequences.keySet()) {
            codes.put(ch, "");
            probs.add(frequences.get(ch));
            List<Character> group = new ArrayList<Character>();
            group.add(ch);
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
            List<Character> group = groups.remove(mn2);
            for (char ch : group) {
                codes.put(ch, "1" + codes.get(ch));
            }
            for (char ch : groups.get(mn)) {
                codes.put(ch, "0" + codes.get(ch));
            }
            group.addAll(groups.remove(mn));
            groups.add(group);
            double prob = probs.remove(mn2) + probs.remove(mn);
            probs.add(prob);
        }
        return codes;
    }

    private double averageLength(Map<Character, Double> frequences, Map<Character, String> codes) {
        double result = 0;
        for (char ch : codes.keySet()) {
            result += codes.get(ch).length() * frequences.get(ch);
        }
        return result;
    }

    private void solve() throws Exception {
        String text = getText();
        Map<Character, Double> frequences = getFrequences(text);
        System.out.println(frequences);
        Map<Character, String> codes = huffman(frequences);
        System.out.println(codes);
        System.out.println("Averate length: " + averageLength(frequences, codes));
        System.out.println("Entropy: " + calcEntropy(frequences));
    }

    public static void main(String[] args) throws Exception {
        new Huffman().solve();
    }
}