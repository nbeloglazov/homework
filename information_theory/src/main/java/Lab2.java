import java.util.*;

public class Lab2 {

    private static double p = 2.0/3;
    private static double eps = 0.138;
    private static int n = 5;
    private static double entropy = -(p * log2(p) + (1 - p) * log2(1 - p));
    private static double lowerBound, upperBound;
    private static double lowerCardBound, upperCardBound;

    private static void recalculate() {
        lowerBound = Math.pow(2, -n * (entropy + eps));
        upperBound = Math.pow(2, -n * (entropy - eps));

        lowerCardBound = (1 - eps) * Math.pow(2, n * (entropy - eps));
        upperCardBound = Math.pow(2, n * (entropy + eps));
    }


    private static List<String> sequences(int length) {
        if (length == 0) {
            return Arrays.asList("");
        }
        List<String> result = new ArrayList<String>();
        for (String seq : sequences(length - 1)) {
            result.add(seq + "0");
            result.add(seq + "1");
        }
        return result;
    }

    private static double prob(String seq) {
        double result = 1;
        for (char ch : seq.toCharArray()) {
            result *= ch == '0' ? (1 - p) : p;
        }
        return result;
    }

    private static boolean isTypical(String seq) {
        double p = prob(seq);
        return  lowerBound <= p && upperBound >= p;
    }


    private static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }

    private static List<String> filterTypical(List<String> seqs) {
        List<String> result = new ArrayList<String>();
        for (String seq : seqs) {
            if (isTypical(seq)) {
                result.add(seq);
            }
        }
        return result;
    }

    private static double prob(List<String> seqs) {
        double result = 0;
        for (String seq : seqs) {
            result += prob(seq);
        }
        return result;
    }

    public static void main(String[] args) {
        recalculate();
        l("Entropy is", entropy);
        l("Lower bound is", lowerBound);
        l("Upper bound is", upperBound);
        l();
        List<String> seqs = sequences(n);
        int count = 0;
        for (String seq : seqs) {
            l(seq, isTypical(seq), prob(seq));
            if (isTypical(seq)) {
                count++;
            }
        }
        l();
        l("Lower card bound is", lowerCardBound);
        l("Upper card bound is", upperCardBound);
        l("Typical sequences:", count);
        l("Probability:", prob(filterTypical(seqs)));

        for (n = 5; n < 30; n++) {
            recalculate();
            double p = prob(filterTypical(sequences(n)));
            l("For n", n, "probability is", p, "satisfies", p >= 1 - eps);
        }
    }

    public static void l(Object... objs) {
        for (Object obj : objs) {
            System.out.print(obj + " ");
        }
        System.out.println();
    }
}