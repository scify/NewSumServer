/*
 * Copyright 2013 SciFY NPO <info@scify.org>.
 *
 * This product is part of the NewSum Free Software.
 * For more information about NewSum visit
 * 
 * 	http://www.scify.gr/site/en/our-projects/completed-projects/newsum-menu-en
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * If this code or its output is used, extended, re-engineered, integrated, 
 * or embedded to any extent in another software or hardware, there MUST be 
 * an explicit attribution to this work in the resulting source code, 
 * the packaging (where such packaging exists), or user interface 
 * (where such an interface exists). 
 * The attribution must be of the form "Powered by NewSum, SciFY"
 */ 

package org.scify.NewSumServer.Server.Utils;

import gr.demokritos.iit.jinsect.structs.Pair;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.scify.NewSumServer.Server.Comms.Communicator;
import org.scify.NewSumServer.Server.Storage.IDataStorage;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Sentence;
import org.scify.NewSumServer.Server.Structures.Topic;
import static org.scify.NewSumServer.Server.Utils.Utilities.isAlphabetic;
import static org.scify.NewSumServer.Server.Utils.Utilities.isGreekLetter;
import static org.scify.NewSumServer.Server.Utils.Utilities.writeStringListToFile;

/**
 * Contains Various utility methods
 *
 * @author George K. <gkiom@scify.org>
 */
public class Utilities {

    public static final String  sCatsDaysFile = Communicator.getSwitches().get("sCatsDaysFile");

    private static final Logger LOGGER = Main.getLogger();
    /**
     * The Delimiter Used in the Sources File, in ./data/Sources/RSSSources.txt
     */
    private static final String sDelimiter = "[*]{3}"; // regex pattern, for split

    private static final String sDelimiterSimple = "***"; // simple format, 'contains'
    
        private static List<Pair> lsArticlePairs = Collections.synchronizedList(new LinkedList());


    /**
     * Checks if a URL is Valid
     *
     * @param sURL A string containing the URL to check
     * @return true if the URL is valid. False Otherwise
     */
    public static boolean ValidURL(String sURL) {
        try {
            URL uTmp = new URL(sURL);
            return true;
        } catch (MalformedURLException me) {
            LOGGER.log(Level.INFO, "Malformed URL ignored", me.getMessage());
            return false;
        }
    }

    /**
     * Reads A Simple Text File.
     *
     * @param sPathToFile The Absolute Path to the File
     * @return The File Contents
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFromFile(String sPathToFile, String del) {
        File fFile = new File(sPathToFile);
        StringBuilder sb = new StringBuilder();
        if (fFile.canRead()) {
            FileInputStream fstream = null;
            try {
                fstream = new FileInputStream(fFile);
                // Get the object of DataInputStream
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String sLine;
                //Read File Line By Line
                while ((sLine = br.readLine()) != null) {
                    sb.append(sLine).append(del);
                }
                //Close the input stream
                in.close();
                return sb.toString();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                return null;
            } finally {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    return null;
                }
            }
        } else {
            LOGGER.log(Level.SEVERE, "Unable To Read From File {0}", fFile.getName());
            return sb.toString(); //null
        }
    }

    /**
     * @param sPathToFile The absolute link to the file where the sources are
     * saved
     * @return The map containing the (RSSFeed, category) data
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String, String> getSourcesFromFile(String sPathToFile)
            throws FileNotFoundException, IOException {

        File fFile = new File(sPathToFile);
        if (!fFile.exists()) {
            throw new FileNotFoundException(fFile.getAbsolutePath() + " cannot be found.");
        }
        if (fFile.canRead()) {
            FileInputStream fstream = new FileInputStream(fFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String sLine;
            ArrayList<String> alCategories = new ArrayList<String>();
            HashMap<String, String> hmSources = new HashMap<String, String>();
            //if line does not start with 'http', it's a category
            //else it's a link of the last category, containing the label
            //separated by '***'
            //if line starts with '?' is a comment
            while ((sLine = br.readLine()) != null) {
                // if not a commnet
                if (!sLine.startsWith("?")) {
                    // if not a link
                    if (!sLine.startsWith("http")) {
                        // it's a category
                        // a category line, holds it's category name, and
                            // MAY hold a number after a separator (sDelimiter).
                        String tmpCat;
                        if (sLine.contains(sDelimiterSimple)) {
                            tmpCat = sLine.split(sDelimiter)[0];
                            // The number after the delimiter
                                // represents the number of days old news to fetch for that category.
                            // Write categories - days file
                            appendToFile(sCatsDaysFile, tmpCat + "=" + sLine.split(sDelimiter)[1]);
                        } else {
                            tmpCat = sLine;
                        }
                            if (!alCategories.contains(tmpCat)) {
                                alCategories.add(tmpCat);
                            }
                    } else {
                        // add links for that category
                        hmSources.put(sLine.split(sDelimiter)[0],
                                alCategories.get(alCategories.size() - 1));
                    }
                }
            }
            in.close();
            return hmSources;
        } else {
            LOGGER.log(Level.SEVERE, "Unable To Read From File {0}", fFile.getName());
            return null;
        }
    }

    public static HashMap<String, String> getLinkLabelsFromFile(String sPathToFile)
            throws FileNotFoundException, IOException {
        File fFile = new File(sPathToFile);
        if (fFile.canRead()) {
            FileInputStream fstream = new FileInputStream(fFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String sLine;
            ArrayList<String> alCategories = new ArrayList<String>();
            LinkedHashMap<String, String> hmLinkLabels = new LinkedHashMap<String, String>();
            //if line does not start with 'http', it's a category
            //else it's a link of the last category, containing the label
            //separated by '***'
            //Lines starting with '?'are comment lines
            while ((sLine = br.readLine()) != null) {
                if (!sLine.startsWith("?")) {
                    if (!sLine.startsWith("http")) {
                        String stmpCateg;
                        if (sLine.contains(sDelimiterSimple)) {
                            stmpCateg = sLine.split(sDelimiter)[0];
                        } else {
                            stmpCateg = sLine;
                        }
                        if (!alCategories.contains(stmpCateg)) {
                            alCategories.add(stmpCateg);
                        }
                    } else {
                        hmLinkLabels.put(sLine.split(sDelimiter)[0],
                                alCategories.get(alCategories.size() - 1) + "-" + sLine.split(sDelimiter)[1]);
                    }
                }
            }
            in.close();
            return hmLinkLabels;
        } else {
            LOGGER.log(Level.SEVERE, "Unable To Read From File {0}", fFile.getName());
            return null;
        }
    }

    /**
     *
     * @param sPathToFile the file containing the sources
     * @return the mapping between the rssFeedLinks and their applied labels.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String, String> getSourceLabelsFromFile(String sPathToFile)
            throws FileNotFoundException, IOException {
        File fFile = new File(sPathToFile);
        if (fFile.canRead()) {
            FileInputStream fstream = new FileInputStream(fFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String sLine;
            LinkedHashMap<String, String> hmSourceLabels = new LinkedHashMap<String, String>();
            while ((sLine = br.readLine()) != null) {
                if (sLine.startsWith("http")) {
                    hmSourceLabels.put(sLine.split(sDelimiter)[0],
                            sLine.split(sDelimiter)[1]);
                }
            }
            in.close();
            return hmSourceLabels;
        } else {
            LOGGER.log(Level.SEVERE, "Unable To Read From File {0}", fFile.getName());
            return null;
        }
    }

    protected static String createSourceLabelsFromFile(String sPathToFile)
            throws FileNotFoundException, IOException {
        File fFile = new File(sPathToFile);
        if (fFile.canRead()) {
            FileInputStream fstream = new FileInputStream(fFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String sLine;
            LinkedHashMap<String, String> hmSourceLabels = new LinkedHashMap<String, String>();
            while ((sLine = br.readLine()) != null) {
                if (!sLine.startsWith("?")) {
                    hmSourceLabels.put("\"" + sLine.split(sDelimiter)[0],
                            sLine.split(sDelimiter)[1] + "\"");
                }
            }
            in.close();
            return hmSourceLabels.toString();
        } else {
            LOGGER.log(Level.SEVERE, "Unable To Read From File {0}", fFile.getName());
            return null;
        }
    }

    /**
     *
     * @param <T> Map key
     * @param <E> Map value
     * @param map The map to filter
     * @param value The value to filter by
     * @return A set containing the keys of the map assigned to the specified
     * value
     */
    public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        Set<T> keys = new HashSet<T>();
        for (Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }
    public static <K, Double extends Comparable<? super Double>> SortedSet<Map.Entry<K, Double>> entriesSortedByValues(Map<K, Double> map) {
        LOGGER.log(Level.INFO, "Initial Map: {0}", map.size());
        SortedSet<Map.Entry<K, Double>> sortedEntries = new TreeSet<Map.Entry<K, Double>>(
                new Comparator<Map.Entry<K, Double>>() {
            @Override
            public int compare(Map.Entry<K, Double> e1, Map.Entry<K, Double> e2) {
                if (e2.getValue().equals(e1.getValue())) {
                    return 1;
                } else {
                    return e2.getValue().compareTo(e1.getValue());
                }
            }
        });
        sortedEntries.addAll(map.entrySet());
        LOGGER.log(Level.INFO, "Sorted Map: {0}", sortedEntries.size());
        return sortedEntries;
    }

    /**
     *
     * @param aStr The array of strings to be joined
     * @param sSeparator The separator to be used to distinguish the strings
     * @return A separator-delimited string containing all the elements of the
     * Array
     */
    public static String joinArrayToString(String[] aStr, String sSeparator) {
        StringBuilder builder = new StringBuilder();
        boolean firstOcc = true; //first occurence
        for (String s : aStr) {
            if (firstOcc) {
                firstOcc = false;
            } else {
                builder.append(sSeparator);
            }
            builder.append(s);
        }
        return builder.toString();
    }
    public static String joinListToString(List<? extends Object> lsStr, String sSeparator) {
        StringBuilder builder = new StringBuilder();
        boolean firstOcc = true; //first occurence
        if (lsStr.size() == 1 && lsStr.get(0).equals("")) {
            return "";
        }
        for (int i = 0; i < lsStr.size(); i++) {
            if (firstOcc) {
                firstOcc = false;
            } else {
                builder.append(sSeparator);
            }
            builder.append(lsStr.get(i).toString());
        }
        return builder.toString();
    }

    public static String joinMapToString(Map<? extends Object, ? extends Object> map,
            String sSeparator, String sMidSeparator) {

        StringBuilder sb = new StringBuilder();
        Iterator it = map.entrySet().iterator();
        boolean First = true;
        while (it.hasNext()) {
            Map.Entry tmpEntry = (Map.Entry) it.next();
            if (First) {
                First = false;
            } else {
                sb.append(sSeparator);
            }
            sb.append(tmpEntry.getKey());
            sb.append(sMidSeparator);
            sb.append(tmpEntry.getValue());
        }
        return sb.toString();
    }

    public static void print(Object O) { //debug
        System.out.println(O.toString());
    }
    /**
     * Used only by dumpClusterer
     *
     * @param sCat the category of interest
     * @param line the line to append to the file
     */
    public static void writeClusterCheckFile(String sCat, String line) {
        String sPathtoFile =
                System.getProperty("user.dir")
                + System.getProperty("file.separator") + "data"
                + System.getProperty("file.separator") + sCat + "-ClusterCheck.csv";
        File fFile = new File(sPathtoFile);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(fFile, true));
            bw.write(line);
            bw.newLine();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error: {0}", e.getMessage());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Writes a list of strings to file, one line per entry. Deletes previous
     * file
     *
     * @param lsToWrite the list of strings to store to file, line by line for
     * each entry.
     */
    public static void writeStringListToFile(List<String> lsToWrite) {
        String sPathtoFile =
                System.getProperty("user.dir")
                + System.getProperty("file.separator") + "data"
                + System.getProperty("file.separator") + "Tools"
                + System.getProperty("file.separator") + "PatternCheck.txt";
        File fFile = new File(sPathtoFile);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(fFile, false));
            for (String each : lsToWrite) {
                bw.write(each);
                bw.newLine();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error: {0}", e.getMessage());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * used for training the classifier
     *
     * @param sToWrite a single line for the train set of the classifier
     */
    public static void appendToFile(String sToWrite) {
        String sPathtoFile =
                System.getProperty("user.dir")
                + System.getProperty("file.separator") + "data"
                + System.getProperty("file.separator") + "MachineLearningData"
                + System.getProperty("file.separator") + "Classification_Results.txt";
        File fFile = new File(sPathtoFile);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(fFile, true)); // append
            bw.write(sToWrite);
            bw.newLine();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error: {0}", e.getMessage());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Appends a single line to a specified text file
     *
     * @param sPathToFile the full path to the file
     * @param sToWrite the line to write to the file
     */
    public static void appendToFile(String sPathToFile, String sToWrite) {
        File fFile = new File(sPathToFile);
        if (!fFile.exists()) {
            try {
                new File(sPathToFile).createNewFile();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
            }
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(fFile, true)); // append
            bw.write(sToWrite);
            bw.newLine();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error: {0}", e.getMessage());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Reads a specified file containing the (category - Days to keep) map and
     * returns it
     *
     * @param sPathToFile the path where the file is located
     * @return the mapping between the category and it's max days to keep
     * articles
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static HashMap<String, Integer> readDaysPerCategoryFile(String sPathToFile)
            throws FileNotFoundException, IOException {
        String sDel = "=";
        File fFile = new File(sPathToFile);
        if (!fFile.exists()) {
            throw new FileNotFoundException(fFile.getAbsolutePath() + " cannot be found.");
        }
        if (fFile.canRead()) {
            FileInputStream fstream = new FileInputStream(fFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String sLine;
            HashMap<String, Integer> hmDaysPerCateg = new HashMap<String, Integer>();

            while ((sLine = br.readLine()) != null) {
                if (sLine.contains(sDel)) {
                    hmDaysPerCateg.put(sLine.split(sDel)[0],
                            Integer.valueOf(sLine.split(sDel)[1]));
                }
            }
            in.close();
            return hmDaysPerCateg;
        } else {
            LOGGER.log(Level.SEVERE, "Unable To Read From File {0}", fFile.getName());
            return null;
        }
    }
    public static String MakeTmpHumanLine(String sSep,
            double ValueSimilarity, double ContainmentSimilarity,
            double SizeSimilarity, double NVS, String sMatches) {
        String sTmpLine;
        sTmpLine = Double.toString(ValueSimilarity)
                + sSep + Double.toString(ContainmentSimilarity)
                + sSep + Double.toString(SizeSimilarity)
                + sSep + Double.toString(NVS)
                + sSep + sMatches;
        return sTmpLine;
    }

    /**
     * Adds an object to the list, only if it is not already contained in the
     * list, avoiding duplicates
     *
     * @param <T> a Type that extends Object
     * @param lsArt the list to add to
     * @param toAdd the object to add to the list
     */
    public static <T extends Object> void addItemToList(List<T> lsArt, T toAdd) {

        if (lsArt.isEmpty()) {
            lsArt.add(toAdd);
        } else {
            if (!lsArt.contains(toAdd)) {
                lsArt.add(toAdd);
            }
        }
    }

    public static int countDiffArticles(String[] Summary) {
        int counter = 1;
        if (Summary.length <= 2) {
            return counter;
        } else {
            String InitialSource = Summary[1].split(Sentence.getSentenceSeparator())[1];
            for (int i = 2; i < Summary.length; i++) {
                String[] tmps = Summary[i].split(Sentence.getSentenceSeparator());
                if (!InitialSource.contains(tmps[1])) {
                    counter++;
                }
                InitialSource += tmps[1];
            }
            return counter;
        }
    }
    public static void checkForPossibleSpam(List<Article> lsArticleList) {
        List lsSame = new LinkedList();
        for (int i = 0; i < lsArticleList.size() - 1; i++) {
            Article aFirst = lsArticleList.get(i); // first feed
            for (int j = i + 1; j < lsArticleList.size(); j++) {
                Article aSecond = lsArticleList.get(j); // second feed
                if (aFirst.getFeed().equals(aSecond.getFeed())) {
                    String t1 = aFirst.getText();
                    List<String> at1 = splitNoEmpty(t1, "[;,.]");
                    String t2 = aSecond.getText();
                    List<String> at2 = splitNoEmpty(t2, "[;,.]");
                    if (at1.size() > 1 && at2.size() > 1) {
                        for (String each : at1) {
                            each = each.trim();
                            for (String each2 : at2) {
                                each2 = each2.trim();
                                if (each.equalsIgnoreCase(each2)) {
                                    lsSame.add(0, each);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (lsSame.size() > 0) {
            ArrayList<String> lsRes = sortByOccurencies(lsSame);
            if (lsRes != null) {
                if (!lsRes.isEmpty()) {
                    LOGGER.log(Level.WARNING, "Found possible SPAM sentences, check 'Tools' folder");
                    System.out.println(lsRes.toString());
                    writeStringListToFile(lsRes);
                }
            } else {
                LOGGER.info("No SPAM occurencies");
            }
        } else {
            LOGGER.info("No SPAM occurencies");
        }
    }

    private static ArrayList<String> sortByOccurencies(List<String> lsSame) {
        HashMap<String, Integer> hsOccs = new HashMap<String, Integer>();
        for (String each : lsSame) {
            if (!hsOccs.containsKey(each)) {
                hsOccs.put(each, 1);
            } else {
                hsOccs.put(each, hsOccs.get(each) + 1);
            }
        }
        Iterator it = hsOccs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry mp = (Map.Entry) it.next();
            Integer val = (Integer) mp.getValue();
            if (val < 4) {
                it.remove();
            }
        }
        if (hsOccs.isEmpty()) {
            return null;
        }
        ArrayList<Map.Entry<String, Integer>> lsRes = new ArrayList<Map.Entry<String, Integer>>(hsOccs.entrySet());
        Collections.sort(lsRes, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        ArrayList<String> lsRet = new ArrayList<String>();
        for (Map.Entry mp : lsRes) {
            lsRet.add((String) mp.getKey() + "="
                    + (String) Integer.toString((Integer) mp.getValue()));
        }
        return lsRet;
    }

    private static ArrayList<String> splitNoEmpty(String sStr, String regex) {
        String[] aIn = sStr.split(regex);
        ArrayList<String> lRes = new ArrayList<String>();
        if (aIn.length == 0) {
            lRes.add("");
            return lRes;
        }
        for (int i = 0; i < aIn.length; i++) {
            if (!aIn[i].isEmpty() && !aIn[i].matches("\\s+") && aIn[i].length() > 1) {
                lRes.add((String) aIn[i]);
            }
        }
        return lRes;
    }

    /**
     * traverses the list of articles and searches for the fewest in a category.
     * E.g. if a list named myList contains 20 articles from "science" category
     * and 10 articles from "Europe" category, getLeastOccurencies(myList) will
     * return 10
     *
     * @param lsList the list of articles
     * @return the number of occurencies in the list with the fewest articles in
     * a category
     */
    public static Integer getLeastOccurencies(List<Article> lsList) {
        int max = 10000;
        int j = 1;
        String tmp1 = lsList.get(0).getCategory();
        for (Article each : lsList) {
            if (each.getToWrap()) {
                String tmpCat = each.getCategory();
                if (tmpCat.equals(tmp1)) {
                    j++;
                } else {
                    tmp1 = tmpCat;
                    if (j < max) {
                        max = j;
                    }
                    j = 1;
                }
//                System.out.println(tmpCat +" :::  " + j);
            }
        }
        return max;
    }

    /**
     * True if character is a Greek letter.
     *
     * @param c Character to check for being a Greek letter.
     * @return true if character is a Greek letter.
     */
    public static boolean isGreekLetter(char c) {
        return (((c >= 0x0370) && (c < 0x0400)) || ((c >= 0x1f00) && (c < 0x2000)));
    }

    /**
     * True if any characters in a string are Greek letters.
     *
     * @param s String to check for Greek letters.
     * @return true if any characters are Greek letters.
     */
    public static boolean hasGreekLetters(String s) {
        boolean result = false;
        String ts = s.trim();
        for (int i = 0; i < ts.length(); i++) {
            char ch = ts.charAt(i);
            if (isGreekLetter(ch)) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Imitates the Java7 isAlphabetic function
     *
     * @param c The character to test.
     * @return True if the character is any letter number.
     */
    public static boolean isAlphabetic(Character c) {
        switch (Character.getType(c)) {
            case Character.UPPERCASE_LETTER:
            case Character.LOWERCASE_LETTER:
            case Character.TITLECASE_LETTER:
            case Character.MODIFIER_LETTER:
            case Character.OTHER_LETTER:
            case Character.LETTER_NUMBER:
                return true;
            default:
                return false;
        }
    }

    public static boolean isGreekWord(String s) {
        s = s.trim();
        for (Character a : s.toCharArray()) {
            if (!Character.isWhitespace(a) && isAlphabetic(a)) {
                if (!isGreekLetter(a)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Converts date to Calendar format
     *
     * @param date The date in Date format
     * @return A calendar instance of the specified date
     */
    public static Calendar convertDateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static List<String> getListOfStrings(List<Article> lsArts) {
        ArrayList<String> lsRes = new ArrayList<String>();
        for (Article each : lsArts) {
            lsRes.add(each.getText());
        }
        return lsRes;
    }

    public static HashMap<String, Topic> getTopicsMap(IDataStorage ids) {
        try {
            HashMap<String, Topic> hsM = ids.readClusteredTopics();
            System.out.println(hsM.toString());
            return hsM;
        } catch (Exception ex) {
            System.err.println(ex);
            return null;
        }
    }

    public static void writeTopicsToFile(HashMap<String, Topic> hsTopics, String sFolderName)
            throws IOException {
        String sTopicPath = System.getProperty("user.dir") + System.getProperty("file.separator")
                + "data" + System.getProperty("file.separator") + sFolderName + System.getProperty("file.separator");
        File f = new File(sTopicPath);
        if (!f.exists()) {
            System.err.println("FILE " + sTopicPath + " DOES NOT EXIST");
            if (!f.mkdirs()) {
                System.err.println("FILE " + sTopicPath + " Could not be created");
            }
        }
        if (f.isDirectory()) {
            f.setWritable(true);
            for (File k : f.listFiles()) {
                k.delete();
            }
        }
        Iterator It = hsTopics.entrySet().iterator();
        while (It.hasNext()) {
            Map.Entry Pair = (Map.Entry) It.next();
            String tmpID = (String) Pair.getKey();
            Topic tmpTopic = (Topic) Pair.getValue();
            String sFullFileName =
                    sTopicPath + tmpID + ".txt";
            File fFile = new File(sFullFileName);
            fFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(fFile, false));
            bw.write("ClusterID: " + tmpID);
            bw.newLine();
            bw.write("Title: " + hsTopics.get(tmpID).getTitle());
            bw.write(("\n========================================\n"));
            StringBuilder sb = new StringBuilder();
            ListIterator<Article> li = hsTopics.get(tmpID).listIterator();
            while (li.hasNext()) {
                Article sCur = li.next();
                sb.append(sCur.getTitle()).append(": ").append(sCur.getText()).append("---").append(sCur.getDatetoString()).append("\n");
            }
            bw.write(sb.toString());
            bw.close();
        }
    }

    public static int getSourcesNum(String sTitle, String sRegex) {
        Matcher m = Pattern.compile(sRegex).matcher(sTitle);
        if (m.find()) {
            return Integer.valueOf(m.group(1));
        }
        return 0;
    }

    /**
     *
     * @param early First {@link Topic} object
     * @param late Second {@link Topic} object
     * @return The difference in days between the two {@link Topic} objects
     */
    public static int getDiffInDays(Topic early, Topic late) {
        // Compare using formatted date
        return early.getSortableDate().compareTo(late.getSortableDate());
    }
    public static void printStringMap(Map<String, String> hsMap, String sDel) {
        int i=1;
        for (Map.Entry<String, String> entry : hsMap.entrySet()) {
            String sKey = entry.getKey();
            String sValue = entry.getValue();
            System.out.println(i+": " + sKey + "=" + sValue);
            i++;
        }
    }

    public static void main(String[] args) {

        ///////////////////CHECK SEARCH/////////////////////////////////////
//        String sBaseDir = Main.sBaseDir;
//        System.out.println("Enter Search String\n");
//        Scanner imp = new Scanner(System.in);
//        String term = imp.next();
//        IDataStorage ids = new InsectFileIO(sBaseDir);
//                ArticleClusterer ac = new ArticleClusterer(
//                (ArrayList<Article>) ids.loadObject("AllArticles", "feeds"), ids, Main.sArticlePath);
//                Locale loc = Main.sPathToSources.endsWith("GR.txt") ? new Locale("el")
//                : new Locale("en");
//        Indexer ind = new Indexer(Main.sArticlePath, Main.sindexPath, loc);
//        INSECTDB idb = new INSECTFileDBWithDir("", Main.sSummaryPath);
//        Summariser sum = new Summariser(new HashSet<Topic>(
//                ac.getArticlesPerCluster().values()), idb);
//        Communicator cm = new Communicator(ids, ac, sum, ind);
//        String sTop = cm.getTopicsByKeyword(ind, term, "All");
//        System.out.println(sTop);
        /////////////////CHECK SEARCH END///////////////////////////////////

    }
//    public class debugLogger {
//
//        public void log(String sMessage, String sPathToFile) {
//            PrintWriter out = null;
//            try {
//                out = new PrintWriter(new FileWriter(sPathToFile), true);
//                out.write(sMessage);
//                out.close();
//            } catch (IOException ex) {
//                LOGGER.log(Level.SEVERE, null, ex);
//            } finally {
//                out.close();
//            }
//        }
//    }





}