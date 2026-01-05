import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

/**
 * Simple HelloWorld program (clear of Checkstyle and SpotBugs warnings).
 *
 * @author William Christian, Reyhan Can, Carson Grube
 */
public final class TagCloudGenerator {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
        // no code needed here
    }

    /**
     * Max font size to be used tag cloud.
     */
    private static final int MAX_FONT_SIZE = 48;
    /**
     * Min font size to be used in tag cloud.
     */
    private static final int MIN_FONT_SIZE = 11;

    /**
     * Compares Strings in alphabetical order.
     *
     * @ensures that capitalized words are placed before their non-capitalized
     *          variants, if applicable.
     */
    private static final class StringLT implements Comparator<Entry<String, Integer>> {
        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
            int ignoreCase = o1.getKey().compareToIgnoreCase(o2.getKey());
            if (ignoreCase == 0) {
                return (o1.getKey().compareTo(o2.getKey()));
            }
            return (ignoreCase);
        }
    }

    /**
     * Compares integers in greater than order.
     */
    private static final class IntegerGT implements Comparator<Entry<String, Integer>> {
        @Override
        public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
            return (o2.getValue().compareTo(o1.getValue()));
        }
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    private static void generateElements(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!charSet.contains(c)) {
                charSet.add(c);
            }
        }

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        char start = text.charAt(position);
        boolean separator = separators.contains(start);
        int end = position;

        if (separator) {
            while (end < text.length() && separators.contains(text.charAt(end))) {
                end++;
            }
        } else {

            while (end < text.length() && !separators.contains(text.charAt(end))) {
                end++;
            }
        }
        return text.substring(position, end);

    }

    /**
     * Creates an html file for the corresponding word and definition.
     *
     * @param file
     *            File to output to.
     *
     * @param wordCounts
     *            A map that has all of the counts for the words
     *
     * @param separators
     *            A set of all of the separators between words
     *
     *
     * @return
     */
    private static void gatherWords(BufferedReader file, Map<String, Integer> wordCounts,
            Set<Character> separators) {

        //go until the file is at the end of it
        try {
            String line = file.readLine();
            while (line != null) {
                int position = 0;

                while (position < line.length()) {
                    //find the next word
                    String temp = nextWordOrSeparator(line, position, separators);
                    position += temp.length();
                    if (!separators.contains(temp.charAt(0))) {
                        String word = temp.toLowerCase();
                        //if it has the word, do not put it in the queue
                        if (wordCounts.containsKey(word)) {
                            int count = wordCounts.get(word);
                            wordCounts.replace(word, count + 1);
                            //if the word is new add it to the queue
                        } else {
                            wordCounts.put(word, 1);
                        }

                    }

                }

                line = file.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading file.");
        }
    }

    /**
     * Creates an html file for the list.
     *
     * @param out
     *            The output file
     *
     * @param inFile
     *            The input file name
     *
     * @param wordCounts
     *            A map with the word and their counts
     *
     * @param numWords
     *            Number of words in tag cloud
     *
     * @param maxCount
     *            Of all word counts in tag cloud, the biggest
     *
     * @param minCount
     *            Of all word counts in tag cloud, the smallest
     * @requires numWords > 0
     * @requires maxCount >= minCount > 0;
     * @return
     */
    private static void createList(PrintWriter out, String inFile,
            ArrayList<Entry<String, Integer>> wordCounts, int numWords, int maxCount,
            int minCount) {
        assert numWords > 0 : "Violation of : numWords > 0";
        assert maxCount >= minCount && minCount > 0
                : "Violation of : maxCount >= minCount > 0";

        // Output html header
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("  <head>");
        out.println("    <title>Top " + numWords + " words in " + inFile + "</title>");
        out.println("    <link href=\"https://cse22x1.engineering.osu.edu/2231/web-sw2/"
                + "assignments/projects/tag-cloud-generator/"
                + "data/tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println(
                "    <link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("  </head>");
        // Output body header
        out.println("  <body>");
        out.println("    <h2>Top " + numWords + " words in " + inFile + "</h2>");
        // Output tag cloud header
        out.println("    <hr>");
        out.println("    <div class=\"cdiv\">");
        out.println("      <p class=\"cbox\">");
        // Output tag cloud words
        while (wordCounts.size() > 0) {
            Entry<String, Integer> pair = wordCounts.removeFirst();
            int fontSize;
            if (maxCount == minCount) {
                fontSize = MIN_FONT_SIZE;
            } else {
                fontSize = (((MAX_FONT_SIZE - MIN_FONT_SIZE)
                        * (pair.getValue() - minCount)) / (maxCount - minCount))
                        + MIN_FONT_SIZE;
            }
            out.println("        <span style=\"cursor:default\" class=\"f" + fontSize
                    + "\" title=\"count: " + pair.getValue() + "\">" + pair.getKey()
                    + "</span>");
        }
        // Output tag cloud footer
        out.println("      </p>");
        out.println("    </div>");
        // Output body footer
        out.println("  </body>");
        // Output html footer
        out.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // Getting the file names
        System.out.print("Enter input file name: ");
        String inFileName = in.nextLine();
        System.out.print("Enter output file name: ");
        String outFileName = in.nextLine();
        System.out.print("Enter number of words: ");
        int numWords = in.nextInt();
        // Close input
        in.close();
        // Crash if not positive
        if (numWords <= 0) {
            System.err.println("Violation of : numWords > 0");
            return;
        }

        //create the seperator set
        final String spuriousStr = " \t\n\r,-.!?[]';:/()";
        HashSet<Character> separatorSet = new HashSet<>();
        generateElements(spuriousStr, separatorSet);
        //creating the map and the queue to store the word and counts in
        HashMap<String, Integer> wordCounts = new HashMap<>();
        // Open input file
        BufferedReader inFile;
        try {
            inFile = new BufferedReader(new FileReader(inFileName));
        } catch (IOException e) {
            System.err.println("Error opening input file.");
            return;
        }
        //calling gatherWords to get the words in the queue and map
        gatherWords(inFile, wordCounts, separatorSet);
        // Close input file
        try {
            inFile.close();
        } catch (IOException e) {
            System.err.println("Error closing input file.");
        }
        // Crash if not enough words
        if (wordCounts.size() < numWords) {
            System.err.println("Fatal Error : Not enough words in input file.");
            return;
        }

        //sorting the words numerically
        ArrayList<Entry<String, Integer>> sortedNumbers = new ArrayList<Entry<String, Integer>>();
        // Move entries into "sorting machine"
        Iterator<Entry<String, Integer>> it = wordCounts.entrySet().iterator();
        while (it.hasNext()) {
            sortedNumbers.add(it.next());
            it.remove();
        }
        // Sort entries numerically
        Comparator<Entry<String, Integer>> numberOrder = new IntegerGT();
        sortedNumbers.sort(numberOrder);

        // Sorting the words alphabetically
        ArrayList<Entry<String, Integer>> sortedWords = new ArrayList<Entry<String, Integer>>();
        // Move numWords entrie into "sorting machine"
        for (int i = 0; i < numWords; i++) {
            sortedWords.add(sortedNumbers.removeFirst());
        }
        // Gets maxCount and minCount (since insertion order is preserved)
        int maxCount = sortedWords.getFirst().getValue();
        int minCount = sortedWords.getLast().getValue();
        // Sort entries alphabetically
        Comparator<Entry<String, Integer>> wordOrder = new StringLT();
        sortedWords.sort(wordOrder);

        //writing the html file
        PrintWriter outFile;
        try {
            outFile = new PrintWriter(new BufferedWriter(new FileWriter(outFileName)));
        } catch (IOException e) {
            System.err.println("Error opening output file.");
            return;
        }
        createList(outFile, inFileName, sortedWords, numWords, maxCount, minCount);
        outFile.close();
    }

}
