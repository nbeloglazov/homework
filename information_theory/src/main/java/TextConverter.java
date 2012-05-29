import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class TextConverter {

    public static void main(String[] args) throws Exception {
        List<String> lines = IOUtils.readLines(new FileReader("original_text.txt"));
        List<String> converted = new ArrayList<String>();
        for (String line : lines) {
            line = StringUtils.normalizeSpace(line);
            line = line.replaceAll("[^\\p{L} ]","")
                    .toLowerCase()
                    .replaceAll("\\w", "")
                    .replace('ъ', 'ь')
                    .replace('ё', 'е');

            if (!StringUtils.isEmpty(line)) {
                converted.add(line);
            }
        }
        String result = StringUtils.join(converted, " ");
        Set<Character> set = new TreeSet<Character>();
        for (char ch : result.toCharArray()) {
            set.add(ch);
        }
        IOUtils.write(result, new FileWriter("text.txt"));
    }
}
