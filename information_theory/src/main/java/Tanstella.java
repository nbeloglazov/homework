import java.util.*;

public class Tanstella {

    private static Map<String, Double> createTree(int q, double oneProb) {
        List<String> codes = new ArrayList<String>();
        List<Double> probs = new ArrayList<Double>();
        codes.add("");
        probs.add(1.0);
        for (int i = 0; i < q; i++) {
            int mx = 0;
            for (int j = 1; j < probs.size(); j++) {
                if (probs.get(mx) < probs.get(j)) {
                    mx = j;
                }
            }
            String code = codes.remove(mx);
            double prob = probs.remove(mx);
            codes.add(code + "1");
            probs.add(prob * oneProb);
            codes.add(code + "0");
            probs.add(prob * (1 - oneProb));
        }
        Map<String, Double> result = new HashMap<String, Double>();
        for (int i = 0; i < codes.size(); i++) {
            result.put(codes.get(i), probs.get(i));
        }
        return result;
    }

    private static double averageLength(Map<String, Double> codes) {
        double result = 0;
        for (String code : codes.keySet()) {
            result += code.length() * codes.get(code);
        }
        return result;
    }


    private static double calcEntropy(double p) {
        double q = 1 - p;
        return - (p * Math.log(p) / Math.log(2) + q * Math.log(q) / Math.log(2));
    }

    public static void main(String[] args) {
        double probOne = 0.4;
        int n = 3;
        Map<String, Double> codes = createTree(7, probOne);
        System.out.println(codes);
        System.out.println("Average length: " + averageLength(codes));
        System.out.println("Every symbol takes: " + n / averageLength(codes));
        System.out.println("Lower bound: " + calcEntropy(probOne));
    }
}