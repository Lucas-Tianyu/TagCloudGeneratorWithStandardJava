import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * This program can sort the words first in decreasing order of count (to find
 * the N most frequent words), and then in alphabetical order to output the tag
 * cloud.
 *
 * @input The input file can be an arbitrary text file. No special requirements
 *        are imposed.
 * @output The output shall be a single well-formed HTML file displaying the
 *         name of the input file in a heading followed by a tag cloud of the N
 *         words with the highest count in the input.
 *
 * @author Lucas Wu
 *
 * @feature Based on Java component
 * @feature common words (such as, for example, "a", "the", "and", etc.) and
 *          strings that are not words (such as, for example, "t", "s", etc.)
 *          are not included in the tag cloud.
 * @feature the capitalization displayed in the output is the one that occurs
 *          most often among the different capitalizations of the same word.
 *
 * @requirements must use the FileReader/BufferedReader and FileWriter/Buffered
 *               Writer/PrintWriter components for all the file input and output
 *               needed.
 * @requirements use the Java Collections Framework components for all the data
 *               storing and sorting needed.
 * @requirements catch and handle appropriately (e.g., by outputing meaningful
 *               error messages) all the IOExceptions that may be thrown by the
 *               file I/O code.
 *
 */
public final class TagcloudGenerator {

    /**
     * private constructor for static class.
     */
    private TagcloudGenerator() {
    }

    /**
     * Separator char setting.
     */
    private static final String CHAR_SEPARATOR = "\"\t\n\r, `-.!?[]';:/()*\\_";
    /**
     * Words that not count into Tagcloud.
     */
    private static final String[] SIMPLE_WORD = { "and", "a", "the" };

    /**
     * The smallest font size for tag cloud.
     */
    private static final int FONT_SIZE_SMALLEST = 11;
    /**
     * the difference between largest font size and smallest font size.
     */
    private static final int FONT_SIZE_DIFFERENCE = 26;

    /**
     * alphabetical order.
     */
    private static class AlphaOrder
            implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Entry<String, Integer> word1,
                Entry<String, Integer> word2) {
            int result = word1.getKey().toLowerCase()
                    .compareTo(word2.getKey().toLowerCase());
            if (result == 0) {
                result = word2.getValue().compareTo(word1.getValue());
            }
            return result;
        }

    }

    /**
     * count order.
     */
    private static class CountOrder
            implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Entry<String, Integer> word1,
                Entry<String, Integer> word2) {
            int result = word2.getValue().compareTo(word1.getValue());
            if (result == 0) {
                result = word1.getKey().toLowerCase()
                        .compareTo(word2.getKey().toLowerCase());
            }

            return result;
        }

    }

    /**
     * Count the time each word appears in the TXT file.
     *
     * @param textInput
     *            SimpleReader with TXT stream
     * @param words
     *            A map that stores all words with number appear in the txt file
     * @throws IOException
     *             file reading error
     * @requires |words|=0
     * @ensure words = all words from in
     */
    public static void countWordFromInput(BufferedReader textInput,
            Map<String, Integer> words) throws IOException {
        String line = null;
        Map<String, Integer> mergeCase = new HashMap<String, Integer>();
        line = textInput.readLine();

        while (line != null) {

            int position = 0;
            while (line.length() > position) {
                String word = readNextWordOrSep(line, position);

                if (!(isSeparator(word.charAt(0)) || isSimpleWord(word))) {
                    if (words.containsKey(word)) {
                        words.replace(word, words.get(word) + 1);
                    } else {
                        words.put(word, 1);
                    }
                }
                position += word.length();
            }
            line = textInput.readLine();

        }

        for (String x : words.keySet()) {
            String upperX = x.substring(0, 1).toUpperCase() + x.substring(1);
            String lowerX = x.toLowerCase();
            int num = words.get(x);
            if (words.containsKey(upperX) && words.containsKey(lowerX)) {
                num = words.get(upperX) + words.get(upperX);
                if (words.get(upperX) > words.get(upperX)) {
                    if (!mergeCase.containsKey(upperX)) {
                        mergeCase.put(upperX, num);
                    }

                } else {
                    if (!mergeCase.containsKey(lowerX)) {
                        mergeCase.put(lowerX, num);
                    }

                }
            } else {
                mergeCase.put(x, num);
            }
        }
        words.clear();
        words.putAll(mergeCase);

    }

    /**
     * Return a string of a word or a String consecutive separator.
     *
     * @param str
     *            Line String
     * @param position
     *            Starting position
     * @return A word or a String of consecutive
     * @requires |str|>0
     * @ensure returned String = substring({@code position}, position that the
     *         word or separators ends);
     *
     */
    public static String readNextWordOrSep(String str, int position) {
        String word = "";
        if (isSeparator(str.charAt(position))) { //separator string
            for (int i = position; i < str.length()
                    && isSeparator(str.charAt(i)); i++) {
                word += str.charAt(i);
            }
        } else { //word string
            for (int i = position; (i < str.length())
                    && !isSeparator(str.charAt(i)); i++) {
                word += str.charAt(i);
            }
        }

        return word;
    }

    /**
     * put those in a SortingMachine that sorts them in decreasing order of
     * count; then remove the top N in order from the first SortingMachine and
     * put them into a second SortingMachine that sorts them in alphabetical
     * order of word.(SortingMachine will be output in ExtractionMode).
     *
     * @param dic
     *            the Map saving all words and times of appearance.
     * @param num
     *            the number of SortingMachine after resize.
     * @return the resized SortingMachine with alphabetic order.
     * @requires n>0
     */
    private static List<Map.Entry<String, Integer>> sortAndResize(
            Map<String, Integer> dic, int num) {
        assert dic != null : "Violation of: dic is not null";
        assert num > 0 : "Violation of num > 0.";
        int resize = num;
        if (resize > dic.size()) {
            resize = dic.size();
        }
        List<Map.Entry<String, Integer>> countSort = new ArrayList<>();
        List<Map.Entry<String, Integer>> wordSort = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : dic.entrySet()) {
            countSort.add(entry);
        }
        countSort.sort(new CountOrder());
        for (int i = 0; i < resize; i++) {

            Map.Entry<String, Integer> pair = countSort.remove(0);
            wordSort.add(pair);
        }
        wordSort.sort(new AlphaOrder());
        return wordSort;
    }

    /**
     * Check if the given char is a separator.
     *
     * @param c
     *            char to be checked
     * @return if the given char is a separator
     */
    public static boolean isSeparator(Character c) {
        assert c != null : "Violation of: c is not null.";
        boolean result = true;
        if (CHAR_SEPARATOR.indexOf(c) == -1) {
            result = false;
        }

        return result;
    }

    /**
     * Check if the given sting is a simpleWord.
     *
     * @param c
     *            char to be checked
     * @return if the given char is a separator
     */
    public static boolean isSimpleWord(String c) {
        assert c != null : "Violation of: c is not null.";
        boolean result = false;
        for (String x : SIMPLE_WORD) {
            if (c.equals(x) || c.length() == 1) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Reenter the output path until it's correct.
     *
     *
     *
     * @param outputFile
     *            the path to be check
     * @return the correct input path
     * @throws IOException
     *             invalid output file name
     */
    public static String outputCorrection(String outputFile)
            throws IOException {
        String result = outputFile;

        int start = 0;
        int end = result.length();

        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '/') {
                start = i + 1;
            }
        }
        for (int j = 0; j < result.length(); j++) {
            if (result.charAt(j) == '.') {
                end = j;
            }

        }
        if (result.length() > 0 && start <= end) {
            result = result.substring(start, end);
            result = "data/" + result + ".html";
        } else {
            throw new IOException();
        }
        return result;

    }

    /**
     * Reenter the output path until it's correct.
     *
     *
     * @param in
     *            SimpleReader used to get the input from user
     * @param numStr
     *            the number to be check
     * @param size
     * @return the correct input path
     */
    public static int seachNumCorrection(Scanner in, String numStr, int size) {
        String temp = numStr;
        int result = -1;
        while (true) {
            try {
                result = Integer.parseInt(temp);
                if (result <= 0 || result > size) {
                    throw new NumberFormatException();
                }
                break;

            } catch (NumberFormatException e) {
                System.out.print(
                        "Invalid search number, please enter a postive number"
                                + "(no larger than total words number: " + size
                                + "): ");
                temp = in.nextLine();
            }

        }
        return result;
    }

    /**
     * Generate tag cloud in HTML.
     *
     * @param textOutput
     *            file to be output
     * @param sorted
     *            Sorted SortingMachine
     * @param inputPath
     *            Name of the given input file
     */

    private static void generateTagCloud(PrintWriter textOutput,
            List<Entry<String, Integer>> sorted, String inputPath) {
        assert sorted != null : "Violation of: sortedDic is not null.";
        assert textOutput != null : "Violation of: out is not null.";
        int fNum;

        int highest = 0;
        int lowest = 0;
        for (Map.Entry<String, Integer> entry : sorted) {
            if (lowest == 0) {
                lowest = entry.getValue();
            } else if (entry.getValue() > highest) {
                highest = entry.getValue();
            } else if (entry.getValue() < lowest) {
                lowest = entry.getValue();
            }
        }

        textOutput.println("<html>");
        textOutput.println("<head>");
        textOutput.print("<title>");
        textOutput.print("Top " + sorted.size() + " words in " + inputPath);
        textOutput.println("</title>");
        textOutput.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/"
                        + "web-sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        textOutput.println("</head>");
        textOutput.println("<body>");
        textOutput.print("<h2>");
        textOutput
                .print("Top " + sorted.size() + " words in " + inputPath + "");
        textOutput.println("</h2>");
        textOutput.println("<hr>");
        textOutput.println("<div class=\"cdiv\">");
        textOutput.println("<p class=\"cbox\">");
        while (sorted.size() > 0) {
            Map.Entry<String, Integer> entry = sorted.remove(0);
            if (highest != lowest) {
                double ratio = (entry.getValue() - lowest)
                        / ((highest - lowest) * 1.0);
                fNum = (int) (FONT_SIZE_SMALLEST
                        + (ratio * (FONT_SIZE_DIFFERENCE)));
            } else {
                fNum = FONT_SIZE_SMALLEST;
            }

            textOutput.println("<span style=\"cursor:default\" class=\"f" + fNum
                    + "\" title=\"count:" + entry.getValue() + "\">"
                    + entry.getKey() + "</span>");
        }
        textOutput.println("</p>");
        textOutput.println("</div>");
        textOutput.println("</body>");
        textOutput.println("</html>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {

        /*
         * Declare variable
         */
        Scanner in = new Scanner(System.in);
        BufferedReader textInput = null;
        PrintWriter textOutput = null;

        /*
         * Enter input filename
         */

        System.out.print("Enter the input file name:");
        String inputFile = in.nextLine();

        try {
            textInput = new BufferedReader(new FileReader(inputFile));
        } catch (IOException e1) {
            System.err.println("Invalid input file path: " + inputFile);
            in.close();
            return;
        }

        /*
         * Read from the input file
         */

        System.out.println("Your input file path: " + inputFile);
        System.out.println("Loading......couting");
        System.out.println();

        Map<String, Integer> words = new HashMap<String, Integer>();
        try {
            countWordFromInput(textInput, words);
        } catch (IOException e3) {
            System.err.println("Error reading from file: " + inputFile);
            in.close();
            return;
        }
        /*
         * Find the total number of word
         */
        int size = words.size();

        if (size == 0) {
            System.out
                    .println("The input file does not have any word to count.");
            in.close();
            return;
        }
        /*
         * Close the input file
         */
        try {
            textInput.close();
        } catch (IOException e) {
            System.err.println("Error closing file: " + inputFile);
            in.close();
            return;
        }

        /*
         * Enter output filename
         */
        System.out.print("Enter the output file name:");
        String outputFile = in.nextLine();

        try {
            outputFile = outputCorrection(outputFile);
            textOutput = new PrintWriter(
                    new BufferedWriter(new FileWriter(outputFile)));
        } catch (IOException e2) {
            System.err.println("Invalid output file path: " + outputFile);
            in.close();
            return;
        }

        System.out.println();

        /*
         * Enter search number
         */
        System.out.print("Enter the number you want to search: ");
        String numStr = in.nextLine();
        int num = seachNumCorrection(in, numStr, size);
        /*
         * print search number
         */
        System.out.println();
        System.out.println("Your search number: " + num);
        System.out.println();

        /*
         * Sort words and generate HTML
         */
        System.out.println("Loading......sorting");
        List<Map.Entry<String, Integer>> sorted = sortAndResize(words, num);

        System.out.println("Loading......generating");
        generateTagCloud(textOutput, sorted, inputFile);
        System.out.println();
        System.out.println("Your output file path: " + outputFile);
        System.out.println("Process finished");
        /*
         * Close output file and user input scanner
         */
        in.close();
        textOutput.close();

    }

}
