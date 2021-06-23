package dev.aureliogrb;


import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class Main {

    public static void main(String[] args) {
        // write your code here
        long startTime, endTime;

        String anagram ="";
        int minlength=3;
        String wordListPath ="";
        String mustInclude= "";

        startTime = System.nanoTime();


        //Parse the options
        Options options = new Options();

        options.addOption(Option.builder("a")
                                .required(true)
                                .longOpt("anagram")
                                .desc("Letters available to build words")
                                .hasArg(true)
                                .type(String.class)
                                .build());

        options.addOption(Option.builder("m")
                .longOpt("min-size")
                .desc("Shortest word size. Default is 3")
                .hasArg(true)
                .type(Integer.class)
                .type(Integer.class)
                .build());

        options.addOption(Option.builder("w")
                .hasArg(true)
                .longOpt("word-list")
                .desc("Path to the list of words")
                .type(String.class)
                .build());

        options.addOption(Option.builder("i")
                .longOpt("include")
                .desc("Sequence that must be included in the answers")
                .hasArg(true)
                .type(String.class)
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Print help message")
                .build());


        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options,args);
            if (cmd.hasOption("h")) {
                formatter.printHelp("AnagramSolver",options,true);
                System.exit(0);
            }

            if (cmd.hasOption("a")) {
                anagram = cmd.getOptionValue("a");
            } else {
                System.out.println("You must specify an anagram using -a or --anagram");
                System.exit(0);
            }

            if (cmd.hasOption("m")) {
                minlength = Integer.parseInt(cmd.getOptionValue("m"));
            }

            if (cmd.hasOption("w")) {
                wordListPath = cmd.getOptionValue("w");
            } else
            {
                wordListPath = "./wordlist.txt";
            }
            System.out.println("Wordlist: " + wordListPath);

            if (cmd.hasOption("i")) {
                mustInclude = cmd.getOptionValue("i");
            }

        } catch (ParseException e) {
            formatter.printHelp("AnagramSolver",options,true);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
        }

        try {
            System.out.println("Word count: " + anagramSolver(anagram
                    , minlength
                    , "/Users/Aurelio/Documents/git/AnagramSolver/src/wordlist.txt"
                    ,mustInclude ));

        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        endTime = System.nanoTime();
        System.out.printf("Time: %1$,.3f microseconds", (endTime - startTime) / 1000.0);

    }

    private static long anagramSolver(String anagram, int minSize, String WordListPath, String mustInclude) {
        //For a given set of characters return the list of words in english, of minsize chars or more
        //that can be written with those letters.
        //
        //   e.g. for arey, 3 return:
        //are    aye    ayr    ear    era
        //ray    rya    rye    yea
        //yare    year


        char[] letters = anagram.toCharArray();
        //The algorithm expects the letter array to be sorted
        //More efficient to sort now outside rather than for every word
        Arrays.sort(letters);

        //Load the dictionary
        ArrayList<String> answers = new ArrayList<>();

        if (mustInclude.length() > 0)
            System.out.println("Must include: " + mustInclude);

        //list available at:
        // http://www-personal.umich.edu/~jlawler/wordlist.html
        // "corncob_lowercase.txt"; //from http://www.mieliestronk.com/wordlist.html
        // "words_alpha.txt"; //from https://github.com/dwyl/english-words/
        Path path = FileSystems.getDefault().getPath(WordListPath);
        try {
            List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
            answers = lines.stream()
                    .filter(x -> x.length() >= minSize
                            && x.length() <= letters.length
                            && lettersMatch(x, letters)
                            && x.contains(mustInclude))
                    .collect(Collectors.toCollection(ArrayList::new));


        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

        }


        answers.sort(Comparator.comparing(String::length)
                .thenComparing(String::toString));
        if (!answers.isEmpty()) {

            int i = 0;
            int len = answers.get(0).length();


            for (String answer : answers) {
                if (len != answer.length()) {
                    len = answer.length();
                    if (i != 0) {
                        System.out.println();
                        i = 0;
                    }
                }
                System.out.print(answer + "    ");
                i++;
                if (i % 5 == 0) {
                    i = 0;
                    System.out.println();
                }
            }
            System.out.println();
        }

        return answers.size();
    }

    private static boolean lettersMatch(String word, char[] letterChars) {

        //Compares a word with an array of letters
        //The algorithm assumes the array of letters is already sorted.  Duplicate letters are ok.

        //Returns true if all the letters that comprise the word are included in the array
        //If a word uses the same letter more than once the array has to contain
        //that letter at least the same number of times to be a match.


        char[] wordChars = word.toCharArray();

        if (wordChars.length > letterChars.length)
            return false;
        else {
            Arrays.sort(wordChars);

            //Arrays.sort(letterChars); presume they come in sorted.
            int l = 0;
            for (int w = 0; w < wordChars.length; w++) {
                while (letterChars[l] != wordChars[w]) {
                    l++;
                    if (l == letterChars.length) {
                        //We passed the last letter in the array. Comparison is zero-based.
                        //so last l would be letterChars.length-1
                        return false;
                    }
                }
                if ((letterChars.length - l) < (wordChars.length - w))
                    //If there are more letters to match in the word
                    //than letters left in the array its not a match
                    return false;
                l++;
            }
            return true;
        }
    }
}
