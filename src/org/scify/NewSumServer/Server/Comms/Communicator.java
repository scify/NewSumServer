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

package org.scify.NewSumServer.Server.Comms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.scify.NewSumServer.Server.Comms.Communicator.LOGGER;
import static org.scify.NewSumServer.Server.Comms.Communicator.readConfigFile;
import org.scify.NewSumServer.Server.Searching.Indexer;
import org.scify.NewSumServer.Server.Storage.IDataStorage;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Sentence;
import org.scify.NewSumServer.Server.Structures.Topic;
import org.scify.NewSumServer.Server.Summarisation.ArticleClusterer;
import org.scify.NewSumServer.Server.Summarisation.RedundancyRemover;
import org.scify.NewSumServer.Server.Summarisation.Summariser;
import org.scify.NewSumServer.Server.Utils.Main;
import org.scify.NewSumServer.Server.Utils.Utilities;
import static org.scify.NewSumServer.Server.Utils.Utilities.getDiffInDays;

/**
 * The Class that contains all the methods required for interaction with
 * the Client.
 * @author George K. <gkiom@scify.org>
 */
public class Communicator {
    /**
     * The Logger class, inherited from main
     */
    protected static Logger           LOGGER                   = (Main.getLogger() != null) ?
            Main.getLogger() : Logger.getAnonymousLogger();
    private static File               Config = new File("./data/BaseDir/ServerConfig.txt");
    private static HashMap            Switches = readConfigFile();
    private static String             sSourcesPath = (String) Switches.get("PathToSources");
    /**
     * The Data Storage Interface
     */
    protected IDataStorage            ids;
    /**
     * The Indexer Used
     */
    protected Indexer                 ind;
    /**
     * The Article Clusterer
     */
    protected ArticleClusterer        ac;
    /**
     * The Summariser
     */
    protected Summariser              sum;
    /**
     * Max Sentences per summary fetched
     */
    public static int                 iOutputSize              = Main.iOutputSize;
    public static String             NO_IDS_FOUND              = sSourcesPath.endsWith("EN.txt") ?
    "No relevant Topic IDs found for the specified User Sources" : "Δε βρέθηκαν ID για τις συγκεκριμένες πηγές";
    public static String             NO_TOPICS_FOUND           = sSourcesPath.endsWith("EN.txt") ?
    "No relevant Topic Titles found for the specified User Sources" : "Δε βρέθηκαν άρθρα για τις συγκεκριμένες πηγές που δώσατε";
    public static String             NO_IDS_ONSEARCH_FOUND     = sSourcesPath.endsWith("EN.txt") ?
    "No relevant Topic IDs found for the specified Keyword" : "Δε βρέθηκαν IDs για τις συκεκριμένες πηγές";
    public static String             NO_TOPICS_ONSEARCH_FOUND  = sSourcesPath.endsWith("EN.txt") ?
    "No relevant Topic Titles found for the specified Keyword" : "Δε βρέθηκαν άρθρα για τη συγκεκριμένη αναζήτηση";
    private static final String FIRST_LEVEL_SEPARATOR = ";,;";
    private static final String SECOND_LEVEL_SEPARATOR = Sentence.getSentenceSeparator();
    private static final String THIRD_LEVEL_SEPARATOR = "=;=";


    /**
     * Reads the configuration file saved by the NewSum server
     */
    public static void initStaticVariables() {
        LOGGER.info("initStaticVariables");
        Config = new File("./data/BaseDir/ServerConfig.txt");
        Switches = readConfigFile();
        sSourcesPath = (String) Switches.get("PathToSources");
        iOutputSize               = Main.iOutputSize; //Max Sentences per summary
        LOGGER = (Main.getLogger() != null) ? Main.getLogger() : Logger.getAnonymousLogger();

        // TODO: Use RESOURCES of some kind. Do NOT use hardcoded messages.
        NO_IDS_FOUND              = sSourcesPath.endsWith("EN.txt") ?
        "No relevant Topic IDs found for the specified User Sources" : "Δε βρέθηκαν ID για τις συγκεκριμένες πηγές";
        NO_TOPICS_FOUND           = sSourcesPath.endsWith("EN.txt") ?
        "No relevant Topic Titles found for the specified User Sources" : "Δε βρέθηκαν άρθρα για τις συγκεκριμένες πηγές που δώσατε";
        NO_IDS_ONSEARCH_FOUND     = sSourcesPath.endsWith("EN.txt") ?
        "No relevant Topic IDs found for the specified Keyword" : "Δε βρέθηκαν IDs για τις συκεκριμένες πηγές";
        NO_TOPICS_ONSEARCH_FOUND  = sSourcesPath.endsWith("EN.txt") ?
        "No relevant Topic Titles found for the specified Keyword" : "Δε βρέθηκαν άρθρα για τη συγκεκριμένη αναζήτηση";
        LOGGER.info("initStaticVariables done");
    }
    private ArrayList<String> lsSortedIDs=null;

    /**
     * Main constructor
     * @param ids The data storage module
     * @param ac The Article Clusterer
     * @param sum The Summariser
     * @param ind The indexer
     */
    public Communicator(IDataStorage ids, ArticleClusterer ac,
                        Summariser sum, Indexer ind) {
        this.ids = ids;
        this.ac  = ac;
        this.sum = sum;
        this.ind = ind;
    }

    /**
     *
     * @return The First Level Separator
     */
    public String getFirstLevelSeparator() {
        return FIRST_LEVEL_SEPARATOR;
    }
    /**
     *
     * @return The Second level Separator, used in the getSummary and
     * getLinkLabels methods
     */
    public String getSecondLevelSeparator() {
        return SECOND_LEVEL_SEPARATOR;
    }
    /**
     * The Third level separator is used only in the first element of the getSummary
     * returned String, which is for the (SourceLink=;=Label) data.
     * @return the link separator
     */
    public String getThirdLevelSeparator() {
        return THIRD_LEVEL_SEPARATOR;
    }
    /**
     *
     * @param sCategory The category of interest
     * @return The Sources in the specified category,
     * or null if error occurs. The Sources are URL patterns
     */
    public String getCategorySources(String sCategory) {
        try{
            if (this.ids.objectExists(sCategory, "Links")) {
                HashSet<String> cSources =
                    (HashSet<String>) this.ids.getLinksByCategory(sCategory);
                StringBuilder sStr = new StringBuilder();
                boolean firstOcc = true;
                for (String s : cSources) {
                    if (firstOcc) {
                        firstOcc = false;
                    } else {
                        sStr.append(getFirstLevelSeparator());
                    }
                    sStr.append(s);
                }
                return sStr.toString();
            } else {
                LOGGER.log(Level.SEVERE,
                "Error: file containing Sources for {0} not found", sCategory);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
        return null;
    }
    /**
     * Using the User's Sources, returns the Categories that these Sources
     * belong to
     * @param sUserSources A separator-delimited String containing
     * the User URL paths. if null or "All", all Sources are accepted
     * @return The Categories for the specified user, or null on Error.
     */
    public String getCategories(String sUserSources) {
        try {
            //for every item in the array, if item is in the generic
            //sources map, add it to the categories set
            if (sUserSources == null || "All".equals(sUserSources) || areAllSources(sUserSources)) {
                //Accept all sources and return categories from saved file
                Collection<String> genericCategs =
                    this.ids.readGenericCategories();
                //TODO IGNORE UNCLASSIFIED CATEGORY
                String[] aCats = genericCategs.toArray(new String[0]);
                return Utilities.joinArrayToString(aCats, getFirstLevelSeparator());
            } else { //filter using User Sources
                HashSet<String> hsCats = new HashSet<String>(); //the categories to send
                HashMap<String, String> hsSources =
                this.ids.readSources("generic");
                // transform the string to an array, using the separator delimiter
                String[] aUserSources = sUserSources.trim().split(getFirstLevelSeparator());
                for (String each: aUserSources) {
                    if (hsSources.containsKey(each)) {
                        hsCats.add(hsSources.get(each));
                    }
                }
                String[] aCats = hsCats.toArray(new String[0]);//convert to array
                //transform to string and return
                return Utilities.joinArrayToString(aCats, getFirstLevelSeparator());
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE,
                "Could not load categories: " + ex.getMessage(), ex.getMessage());
            return null;
        }
    }
    /**
     * @param sUserSources A separator-delimited String containing the user
     * URL paths. if null or "All", all sources are considered valid and
     * all Topic IDs are returned for the specified category
     * @param sCategory The category the client is interested in
     * @return The topic IDs contained in this category
     */
    public String getTopicIDs(String sUserSources, String sCategory) {
        //load the Clustered Topics Map
        HashMap<String, Topic> ClusteredTopics = this.ac.getArticlesPerCluster();
        ArrayList<String> TopicIDs = new ArrayList<String>(); //The Topic IDs to return
        if (ClusteredTopics == null) {
            LOGGER.log(Level.WARNING, "Could Not get Articles Per Cluster");
            TopicIDs.add(NO_IDS_FOUND);
            return Utilities.joinListToString(TopicIDs, getFirstLevelSeparator());
        }
        //Accept all user sources
            if (sUserSources == null || "All".equals(sUserSources) || areAllSources(sUserSources)) {
            Iterator it = ClusteredTopics.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry t  = (Map.Entry) it.next();
                String sID = (String) t.getKey();
                Topic tp = (Topic) t.getValue();
                //filter by category
                if (tp.getCategory().equals(sCategory)) {
                    //every Topic in each Topic ID is in the same category
                    TopicIDs.add(sID);
                }
            }
        } else { // filter according to user sources
            for (String each : ClusteredTopics.keySet()) {
                Topic To = ClusteredTopics.get(each);
                if (To.getCategory().equals(sCategory)) {
                    Iterator l = To.iterator();
                    while (l.hasNext()) {
                        Article ls = (Article) l.next();
                        if (sUserSources.contains(ls.getFeed())) {
                            if (!TopicIDs.contains(each)) {
                                TopicIDs.add(each);
                            }
                        }
                    }
                }
            }
        }
        //If no Topic IDs Found for the specified User Sources, return smth logical
        if (TopicIDs.isEmpty()) {
            LOGGER.log(Level.INFO, "No Topic IDs found for the specified User Sources");
            TopicIDs.add(NO_IDS_FOUND);
//            return Utilities.joinListToString(TopicIDs, getFirstLevelSeparator());
        }
        final HashMap hsIDsTitles = getTopicsMap(); //the (ID, title) map
        //sort the IDs according to their titles
        Collections.sort(TopicIDs, new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                String tmpTit1 = (String) hsIDsTitles.get(o1);
                String tmpTit2 = (String) hsIDsTitles.get(o2);
                String sRegex = ".*\\((\\d+)\\)$";
                int i1 = Utilities.getSourcesNum(tmpTit1, sRegex);
                int i2 = Utilities.getSourcesNum(tmpTit2, sRegex);
                return i2-i1;
            }
        });
        lsSortedIDs = TopicIDs;
        return Utilities.joinListToString(TopicIDs, getFirstLevelSeparator());
    }
    /**
     * @param sUserSources A separator-delimited String containing the user
     * URL paths. If null or "All", all sources are considered valid and
     * all Topic Titles are returned for the specified category
     * @param sCategory The category of interest
     * @return The Topic Titles contained in this category, or null on error
     */
    public String getTopicTitles(String sUserSources, String sCategory) {
        HashMap IDTopicsMap = getTopicsMap(); //The (ID, TopicTitles) Map
        //load the Clustered Topics Map
        HashMap<String, Topic> ClusteredTopics = this.ac.getArticlesPerCluster();
        //Populate the Topic Titles List according to the ID Map
        ArrayList<String> TopicTitles = new ArrayList<String>();
        if (lsSortedIDs == null) {
            getTopicIDs(sUserSources, sCategory);
        }
        //If no Topic IDs Found for the specified User Sources, return smth logical
        if (lsSortedIDs.isEmpty() || lsSortedIDs.get(0).equals(NO_IDS_FOUND)) {
            LOGGER.log(Level.INFO, "No Topic Titles found for the specified User Sources");
            TopicTitles.add(NO_TOPICS_FOUND);
            LOGGER.info(TopicTitles.toString());
        } else {
            for (int i=0; i<lsSortedIDs.size(); i++) {
                // get the title for this ID and add it to the list
                TopicTitles.add(i, (String) lsSortedIDs.get(i) + getSecondLevelSeparator()
                        + IDTopicsMap.get(lsSortedIDs.get(i)) + getSecondLevelSeparator()
                        + ClusteredTopics.get(lsSortedIDs.get(i)).getDate().getTimeInMillis());
                // Also add date for each topic title
            }
        }
        return Utilities.joinListToString(TopicTitles, getFirstLevelSeparator());
    }
    /**
     * @param sUserSources A separator-delimited String containing the user
     * URL paths. If "All", all sources are considered valid and
     * all Topics are returned for the specified category
     * @param sCategory The category of interest
     * @return A string containing the (ID-Title-Date) info for each
     * Topic contained in this category, or null on error.
     * Uses {@link #getFirstLevelSeparator()} for splitting Topics,
     * and {@link #getSecondLevelSeparator()}for splitting data for each Topic
     * @since 1.0
     */
    public String getTopics(String sUserSources, String sCategory) {
        //load the Clustered Topics Map
        HashMap<String, Topic> ClusteredTopics = this.ac.getArticlesPerCluster();
        //The Topics to return
        ArrayList<Topic> tTopics = new ArrayList<Topic>();
        if (ClusteredTopics == null) {
            LOGGER.log(Level.WARNING, "Could Not Load Articles Per Cluster");
            return "";
        }
        //Accept all user sources
        if (sUserSources == null || "All".equals(sUserSources) || areAllSources(sUserSources)) {
            Iterator it = ClusteredTopics.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry t  = (Map.Entry) it.next();
                String sID = (String) t.getKey();
                Topic tp = (Topic) t.getValue();
                //filter by category
                if (tp.getCategory().equals(sCategory)) {
                    tTopics.add(tp);
                }
            }
        } else { // filter according to user sources
            // Create a Temporary ID Set and fill it
            // with the appropriate data.
            HashSet<String> hsIDs = new HashSet<String>();
            for (Map.Entry each : ClusteredTopics.entrySet()) {
                String sID = (String) each.getKey();
                Topic tmpTopic = (Topic) each.getValue();
                // if we reach the specified category
                if (tmpTopic.getCategory().equals(sCategory)) {
                    // keep only Topics from accepted User Sources
                    Iterator l = tmpTopic.iterator();
                    while (l.hasNext()) {
                        Article ls = (Article) l.next();
                        if (sUserSources.contains(ls.getFeed())) {
                            // if even only one source from this topic
                            // interests the user, we keep it.
                            // the final filtering goes on in getSummary
                            hsIDs.add(sID);
                        }
                    }
                }
            }
            // if user Sources
            if (!hsIDs.isEmpty()) {
                // fill the Topics with Appropriate info
                Iterator<String> iit = hsIDs.iterator();
                while (iit.hasNext()) {
                    String tmpID = iit.next();
                    // add to the topics List, according to Topic ID
                    tTopics.add(ClusteredTopics.get(tmpID));
                }
            }
        }
        //If no Topic IDs Found for the specified User Sources, return smth logical
        if (tTopics.isEmpty()) {
            LOGGER.log(Level.INFO, "No Topic IDs found for the specified User Sources");
//            tTopics.add(NO_IDS_FOUND);
            return "";
        }
        // sort Topics According to Date and Article Count
        Collections.sort(tTopics, new Comparator<Topic>() {

            @Override
            public int compare(Topic o1, Topic o2) {
                // get the date difference in days
                int iDiff = Utilities.getDiffInDays(o2, o1);
                // First compare their dates
                if (iDiff != 0) {
                    return iDiff;
                }
                // get the articles number difference
                int iSourceDiff = o2.getArticlesCount() - o1.getArticlesCount();
                // else compare their Source Count
                if (iSourceDiff != 0) {
                    return iSourceDiff;
                }
                // else Sort Alphabetically
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        //debugging
//        for (Topic each : tTopics) { // works well
//            System.out.println("Date: " + each.getSortableDate() + " : " + each.getTitle());
//        }
        return getTopicsAsString(tTopics);
    }
    /**
     *
     * @param sTopicID The topic ID of interest
     * @param sUserSources A separator-delimited list of acceptable sources.
     * if null or "All", the User Accepts all known sources.
     * The sources are in fact URL patterns.
     * @param iMaxSnippets The maximum number of sentences for the summary
     * @return The summary for that topic, using only sentences from acceptable sources
     * @deprecated
     */
    protected String getSummary(String sTopicID, String sUserSources, int iMaxSnippets) {
        // return the Summary of the given topic ID
        String sRes = "";
        // Returns the Summary of the given topic ID, according to user sources
        // Get the topic from the topic ID
        Topic tp = null;
        try {
            tp = this.ac.getArticlesPerCluster().get(sTopicID); //get The topic
        } catch (NullPointerException ex) { //if sTopicID not contained in map
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return "";
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return "";
        }
        LinkedList<Sentence> lsSen = //Get the Summary
            (LinkedList<Sentence>) this.sum.getSummary(tp);
        if (lsSen == null || lsSen.isEmpty()) {
            LOGGER.log(Level.WARNING, "No summary for Topic {0}", sTopicID);
//            lsSen.add(new Sentence("No Summary Found", " "," "));
            return "";
        } else { //ommit small sentences
            for (Iterator<Sentence> ite = lsSen.iterator(); ite.hasNext();) {
                Sentence ob = ite.next();
                if (ob.getSnippet().split("[;,. ]").length < 5) {
                    ite.remove();
                }
            }
        }
        //keep only iOutputSize Sentences in the List
        while (lsSen.size() > iMaxSnippets) {
            lsSen.removeLast();
        }
        // the List containing all the article snippets of interest
        LinkedList<Sentence> lsSummary = new LinkedList<Sentence>();
        if (sUserSources == null || "All".equals(sUserSources) || areAllSources(sUserSources)) { //Accept all user sources
            //Add all source links at start of sRes String
            sRes = getSummarySources(lsSen);
            // Remove redundant sentences and apply result
            sRes += Utilities.joinListToString(
                new RedundancyRemover().removeRedundantSentences(lsSen),
                getFirstLevelSeparator());
        } else { //filter Summary using User Sources
            for (Sentence each: lsSen) {
                if (sUserSources.contains(each.getFeed())) {
                    if (!lsSummary.contains(each)) {
                        lsSummary.add(each);
                    }
                }
            }
            if (!lsSummary.isEmpty()) {
                //Keep only iMaxSnippets Sentences from the List
                while (lsSummary.size() > iMaxSnippets) {
                    lsSummary.removeLast();
                }
                //Add all source links at start of sRes String
                sRes = getSummarySources(lsSummary);
                // Remove redundant sentences and apply result
                sRes += Utilities.joinListToString(
                        new RedundancyRemover().removeRedundantSentences(lsSummary),
                        getFirstLevelSeparator());
            } else {
                LOGGER.log(Level.INFO,
                    "No Summary from these user sources in the specified topic");
//                lsSummary.add(new Sentence("No Summary Found", " "," "));
                return "";
            }
        }
        //Get the Feed link of each sentence and
        //Append Source Label For this feed to the sentence
        String[] aRes = sRes.split(getFirstLevelSeparator());
        String toRes = aRes[0] + getFirstLevelSeparator(); //keep 1st element (all sources - labels)
        for (int i=1; i<aRes.length; i++) {
            String[] sStr = aRes[i].split(getSecondLevelSeparator());
            String sLabel = appendSourceLabel(sStr[2]); // get the label for this feed
            toRes += aRes[i] + getSecondLevelSeparator() + sLabel + getFirstLevelSeparator(); // append to string
        }
//        System.err.println(toRes); // debug
        return toRes;
    }
    /**
     *
     * @param sTopicID The topic ID of interest
     * @param sUserSources A separator-delimited list of acceptable sources.
     * if "All", the User Accepts all known sources.
     * The sources are in fact URL patterns.
     * @return The summary for that topic, using only sentences from acceptable sources
     */
    public String getSummary(String sTopicID, String sUserSources) {
        String sRes = "";
        // Returns the Summary of the given topic ID, according to user sources
        // Get the topic from the topic ID
        Topic tp = null;
        try {
            tp = this.ac.getArticlesPerCluster().get(sTopicID); //get The topic
        } catch (NullPointerException ex) { 
            //if sTopicID not contained in map, then news have been updated
            LOGGER.log(Level.WARNING, ex.getMessage());
            return "";
        } catch (Exception ex) {
            // TODO This ERROR should be logged elsewhere?
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return "";
        }
        LinkedList<Sentence> lsSen = //Get the Summary
            (LinkedList<Sentence>) this.sum.getSummary(tp);
        if (lsSen == null || lsSen.isEmpty()) {
            LOGGER.log(Level.WARNING, "No summary for Topic {0}", sTopicID);
//            lsSen.add(new Sentence("No Summary Found", " "," "));
            return "";
        } else { 
            for (Iterator<Sentence> ite = lsSen.iterator(); ite.hasNext();) {
                Sentence ob = ite.next();
                // Ommit small sentences
                if (ob.getSnippet().split("[;,. ]").length < 5) {
                        ite.remove();
                }
            }
        }
        // keep only iOutputSize Sentences in the List
        while (lsSen.size() > iOutputSize) {
            lsSen.removeLast();
        }
        // the List containing all the article snippets of interest
        LinkedList<Sentence> lsSummary = new LinkedList<Sentence>();
        // Accept all user sources
        if (sUserSources == null || 
                "All".equals(sUserSources)
                    || areAllSources(sUserSources)) {
            // Add all source links at start of sRes String,
            // independently of what will get removed from filtering
            sRes = getSummarySources(lsSen);
            // Filter scrap sentences, remove redundant sentences and apply result
            sRes += Utilities.joinListToString(
                new RedundancyRemover().removeRedundantSentences(filterScrapSentences(lsSen)),
                    getFirstLevelSeparator());
        } else { // if user has limited Sources preference
            //filter Summary using User Sources
            for (Sentence each: lsSen) {
                if (sUserSources.contains(each.getFeed())) {
                    if (!lsSummary.contains(each)) {
                        lsSummary.add(each);
                    }
                }
            }
            if (!lsSummary.isEmpty()) {
                //Add all source links at start of sRes String
                sRes = getSummarySources(lsSummary);
                // Filter scrap sentences,
                // remove redundant sentences and apply result
                sRes += Utilities.joinListToString(
                        new RedundancyRemover().removeRedundantSentences(filterScrapSentences(lsSummary)),
                        getFirstLevelSeparator());
            } else {
                // User unlucky, all sources he kept had scrap content
                // TODO Possibly alter result in order to Inform USER!?
                LOGGER.log(Level.INFO,
                    "No Summary from these user sources in the specified topic");
//                lsSummary.add(new Sentence("No Summary Found", " "," "));
                return "";
            }
        }
        //Get the Feed link of each sentence and
        //Append Source Label For this feed to the sentence
        String[] aRes = sRes.split(getFirstLevelSeparator());
        String toRes = aRes[0] + getFirstLevelSeparator(); //keep 1st element (all sources - labels)
        for (int i=1; i<aRes.length; i++) {
            String[] sStr = aRes[i].split(getSecondLevelSeparator());
            String sLabel = appendSourceLabel(sStr[2]); // get the label for this feed
            toRes += aRes[i] + getSecondLevelSeparator() + sLabel + getFirstLevelSeparator(); // append to string
        }
        return toRes;
    }
    /**
     * Searches the Articles folder with the specified keyword
     * @param ind The indexer that is used for indexing
     * @param sKeyword The search query that the user enters.
     * @param sUserSources The separator-delimited String containing the acceptable
     * User Sources. if null or "All", All URL sources are considered acceptable
     * @return A separator-delimited String containing a List of the Topic IDs
     * in relation to the search term, in descending order,
     * or null if no result is found
     * @deprecated
     */
    public String getTopicIDsByKeyword(Indexer ind, String sKeyword, String sUserSources) {
        String sRes = null;
        try {//Call the Clusterer to get the topic IDs for the specified keyword
            ArrayList<String> topicIDsByKeyword=null;
            if (sSourcesPath.endsWith("GR.txt")) {
                topicIDsByKeyword =
                    this.ac.getTopicIDsByKeyword(ind, sKeyword.trim(), sUserSources, 8,
                        new Locale("el"));
            } else if (sSourcesPath.endsWith("EN.txt")) {
                topicIDsByKeyword =
                    this.ac.getTopicIDsByKeyword(ind, sKeyword.trim(), sUserSources, 8,
                        new Locale("en"));
            }
            ArrayList<String> NullTopicIDs = new ArrayList<String>(); //handle no output
            if (topicIDsByKeyword == null) { //if nothing found
                NullTopicIDs.add(NO_IDS_ONSEARCH_FOUND);
                sRes = NullTopicIDs.get(0); // No relevant topic found
//                sRes = ""; // No relevant topic found
            } else { //pack to String and return it
                sRes = Utilities.joinListToString(topicIDsByKeyword, getFirstLevelSeparator());
            }
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            List nulls = new ArrayList<String>();
            nulls.add("Server Error"); //trying not to mess too much with the Client
            sRes = Utilities.joinListToString(nulls, getFirstLevelSeparator());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            List nulls = new ArrayList<String>();
            nulls.add("Server Error");
            sRes = Utilities.joinListToString(nulls, getFirstLevelSeparator());
        }
        return sRes;
    }
    /**
     * Searches the Articles folder with the specified keyword and returns the
     * articles containing the keyword sorted by the number of occurrences.
     * @param ind The indexer that is used for indexing
     * @param sKeyword The search query that the user enters.
     * @param sUserSources The separator-delimited String containing the acceptable
     * User Sources. if "All", All URL sources are considered acceptable
     * @return A {@link #getFirstLevelSeparator() } delimited String containing 
     * the Topics in relation to the search term, in descending order,
     * or null if no result is found. Each Topic is splitted by
     * {@link #getSecondLevelSeparator() } to it's ID - Title  - Date values.
     * @since 1.0
     */
    public String getTopicsByKeyword(Indexer ind, String sKeyword, String sUserSources) {
        String sRes = null;
        try {//Call the Clusterer to get the topic IDs for the specified keyword
            ArrayList<String> topicIDsByKeyword=null;
            if (sSourcesPath.endsWith("GR.txt")) {
                topicIDsByKeyword =
                    this.ac.getTopicIDsByKeyword(ind, sKeyword.trim(), sUserSources, 8,
                        new Locale("el"));
            } else if (sSourcesPath.endsWith("EN.txt")) {
                topicIDsByKeyword =
                    this.ac.getTopicIDsByKeyword(ind, sKeyword.trim(), sUserSources, 8,
                        new Locale("en"));
            }
            ArrayList<String> NullTopicIDs = new ArrayList<String>(); //handle no output
            if (topicIDsByKeyword == null) { //if nothing found
                NullTopicIDs.add(NO_IDS_ONSEARCH_FOUND);
                sRes = NullTopicIDs.get(0); // No relevant topic found
//                sRes = ""; // No relevant topic found
            } else {
                // create Topic for each TopicID, pack it and return it
                // load the Clustered Topics Map
                HashMap<String, Topic> ClusteredTopics =
                        this.ac.getArticlesPerCluster();
                // Init the Topics to return
                ArrayList<Topic> tTopics = new ArrayList<Topic>();
                // for each ID, add the Topic to the List
                for (String eachID : topicIDsByKeyword) {
                    if (ClusteredTopics.containsKey(eachID)) {
                        tTopics.add(ClusteredTopics.get(eachID));
                    }
                }
                // debug
                for (Topic eT : tTopics) {
                    System.out.println(eT.getID() + " " + eT.getTitle() + " " + eT.getSortableDate());
                }
                // Convert to string
                sRes = getTopicsAsString(tTopics);
            }
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            List nulls = new ArrayList<String>();
            nulls.add("Server Error"); //trying not to mess too much with the Client
            sRes = Utilities.joinListToString(nulls, getFirstLevelSeparator());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            List nulls = new ArrayList<String>();
            nulls.add("Server Error");
            sRes = Utilities.joinListToString(nulls, getFirstLevelSeparator());
        }
        return sRes;
    }
    /**
     *
     * @param sTopicID The UUID of interest
     * @return A Separator delimited String composed from the List of Clustered Topics
     * that correspond to the specified UUID
     * @deprecated 
     */
    public String getTopicTitlesByID(String sTopicID) {
        List<Article> lsArticles= this.ac.getArticlesPerCluster().get(sTopicID);
        List<String> lsTopicTitles = new ArrayList<String>();
        for (Article each : lsArticles) {
            lsTopicTitles.add(each.getTitle());
        }
        return Utilities.joinListToString(lsTopicTitles, getFirstLevelSeparator());
    }
    /**
     *
     * @param sTopicID The Cluster ID of interest
     * @return The Topic Title for the Specified Cluster, with the date
     * in milliseconds appended after {@link #getSecondLevelSeparator()}
     */
    public String getTopicTitleByID(String sTopicID) {
        HashMap ClArts = this.ac.getArticlesPerCluster();
        if (ClArts == null) {
            return NO_TOPICS_FOUND;
        }
        if (ClArts.containsKey(sTopicID)) {
            Topic to = (Topic) ClArts.get(sTopicID);
            String STitleAndDate = to.getTitle() + getSecondLevelSeparator()
                    + to.getDate().getTimeInMillis();
            return STitleAndDate;
        } else {
            return NO_TOPICS_FOUND;
        }
    }
    /**
     *
     * @param sTopicIDs The Separator-delimited String containing all the UUIDs
     * @return A separator-delimited String containing the Topic for each UUID
     * @deprecated 
     */
    public String getTopicTitlesByIDs(String sTopicIDs) {
        String sRes;
        StringBuilder sb = new StringBuilder();
        if (sTopicIDs == null || sTopicIDs.equals(NO_IDS_ONSEARCH_FOUND)
                || sTopicIDs.equals("")) {
            LOGGER.log(Level.WARNING, "No Topic IDs Passed");
            sRes = "";
        } else {
            String[] IDs = sTopicIDs.split(getFirstLevelSeparator());
            String tmpTitleAndDate=null;
            boolean First = true;
            for (int i = 0; i<IDs.length; i++) {
                tmpTitleAndDate = getTopicTitleByID(IDs[i]);
                if (First) {
                    First = false;
                } else {
                    sb.append(getFirstLevelSeparator());
                }
                sb.append(tmpTitleAndDate);
            }
            sRes = sb.toString();
        }
        return sRes;
    }
    /**
     *
     * @return A Separator delimited String containing all the links
     * and their associated labels
     */
    public String getLinkLabelsFromFile() {
        HashMap<String, String> hsLabels =
            (HashMap<String, String>) this.ids.loadObject("LinkLabels", "generic");
            // construct a Sentence-Separator - Separator delimited String (as
            // in the getSummary() method and return it
        if (hsLabels != null)     {
            return Utilities.joinMapToString(hsLabels, getFirstLevelSeparator(), getSecondLevelSeparator());
        } else {
            LOGGER.log(Level.SEVERE, "Unable to load Link Labels. Returning Null");
            return null;
        }
    }
    /**
     *
     * @param sTopicIDs the topic IDs
     * @return a map containing (ID, topic title) for the specified IDs
     */
    public HashMap<String, String> getTopicsMap(String sTopicIDs) {
        HashMap<String, String> Titles = new HashMap<String, String>();
        String[] IDs = sTopicIDs.split(getFirstLevelSeparator());
        for (int i=0; i<IDs.length; i++) {
            Topic TmpTopic = this.ac.getArticlesPerCluster().get(IDs[i]);
            Titles.put(IDs[i], TmpTopic.getTitle());
        }
        return Titles;
    }
    /**
     *
     * @return A map containing (clusterID, TopicTitle) data
     */
    protected HashMap<String, String> getTopicsMap() {
        HashMap<String, Topic> hsTopics = this.ac.getArticlesPerCluster();
        HashMap<String, String> hsTitles = new HashMap<String, String>();
        for (Map.Entry each : hsTopics.entrySet()) {
            Topic top = (Topic) each.getValue();
            hsTitles.put((String) each.getKey(), top.getTitle());
        }
        return hsTitles;
    }
    /**
     *
     * @param alTopics the topics to process
     * @return the (ID - title - date In milliseconds) data for each topic, separated
     * by {@link #getFirstLevelSeparator()} for each topic, and by
     * {@link #getSecondLevelSeparator() } for each value in a topic.
     * @since 1.0
     */
    private String getTopicsAsString(ArrayList<Topic> alTopics) {
        StringBuilder sb = new StringBuilder();
        boolean firstOcc = true;
        for (Topic each : alTopics) {
            if (firstOcc) {
                firstOcc = false;
            } else {
                sb.append(getFirstLevelSeparator());
            }
            // append ID, Title, Date separated
            sb.append(each.getID()).append(getSecondLevelSeparator());
            sb.append(each.getTitle()).append(getSecondLevelSeparator());
            sb.append(each.getDate().getTimeInMillis());
        }
        return sb.toString();
    }
    /**
     * Just for debugging matters.
     * @param sUserSources the user sources
     * @param sCategory the category of interest
     * @deprecated (from v1.0+)
     */
    public void checkTopicTitles(String sUserSources, String sCategory) {
        String sIDs = getTopicIDs(sUserSources, sCategory);
        String[] aIDs = sIDs.split(getFirstLevelSeparator());
        String sTopicTitles = getTopicTitles(sUserSources, sCategory);
        String[] aTitles = sTopicTitles.split(getFirstLevelSeparator());
        if (sTopicTitles.equals(NO_TOPICS_FOUND) || sIDs.equals(NO_IDS_FOUND)) {
            LOGGER.log(Level.INFO, "Please Alter your Sources Preferences");
            return;
        }
        if (aIDs.length != aTitles.length) {
            System.err.println("LENGTH DIFF");
            System.err.println(aIDs.length + " : " + aTitles.length);
        }
        HashMap<String, String> titlesPerCluster = getTopicsMap();
        for (int i=0; i<aIDs.length; i++) {
            String sID = aIDs[i];
            String sTit = aTitles[i];
            if (!titlesPerCluster.containsKey(sID)) {
                System.err.println(sID + " not Contained in Full Map");
            } else {
                if (!titlesPerCluster.get(sID).equals(sTit)) {
                    LOGGER.log(Level.SEVERE, "MISMATCH");
                    System.out.println("Index: " + i);
                    System.out.println("ID in IDArray: " + sID);
                    System.out.println("Title in TitleArray: " + sTit);
                    System.out.println("ID in Map: " + sID);
                    System.out.println("Title in Map: " + titlesPerCluster.get(sID));
                } else { continue; }
            }
        }
    }
    /**
     *
     * @return The Map containing the needed static variables for the communicator
     * to perform
     */
    public static HashMap<String, String> readConfigFile() {
        HashMap switches = new HashMap<String, String>();
        LOGGER.log(Level.INFO, "Looking for settings file: {0}", Config.getAbsolutePath());
        if (Config.canRead()) {
            FileInputStream fstream = null;
            try {
                fstream = new FileInputStream(Config);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String sLine;
                while ((sLine = br.readLine()) != null) {
                    switches.put(sLine.split("=")[0].trim(), sLine.split("=")[1].trim());
                    }
                in.close();
                LOGGER.info("Settings loaded.");
                return switches;
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                return null;
            } finally {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        } else {
            LOGGER.log(Level.SEVERE, "Error: Cannot read from file: {0}", Config.toString());
            return null;
            }
    }
    /**
     * Reads the switches configuration file and returns the map containing them
     * @return The map containing the path Switches
     */
    public static HashMap<String, String> getSwitches() {
        return (Switches != null) ? Switches : readConfigFile();
    }
    /**
     *
     * @param sSourceFeed the source feed of the article
     * @return the Label of the Source URL link
     */
    private String appendSourceLabel(String sSourceFeed) {
        HashMap<String, String> hsLabels = (HashMap<String, String>)
                this.ids.loadObject("SourceLabels", "generic");
        Iterator it = hsLabels.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry mp = (Map.Entry) it.next();
            String sFeed = (String) mp.getKey();
            if (sFeed.equals(sSourceFeed)) {
                return (String) mp.getValue();
            }
        }
        try {
            return new java.net.URL(sSourceFeed).getHost();
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.INFO, "Could not get Host for {0}", sSourceFeed);
            LOGGER.log(Level.INFO, ex.getMessage());
            return "Source";
        }
    }
    /**
     * Gets all different source URL's and relevant labels for the summary
     * specified
     * @return A link-separated string containing all the URL's and labels for
     * the specified summary
     */
    private String getSummarySources(LinkedList<Sentence> lsSummary) {
        String sRes = "";
        boolean firstOcc = true;
        for (Sentence eachSen : lsSummary) {
            if (!sRes.contains(eachSen.getLinkToSource())) {
                if (firstOcc) {
                    firstOcc = false;
                } else {
                    sRes += getSecondLevelSeparator();
                }
                sRes += eachSen.getLinkToSource();
                sRes += getThirdLevelSeparator();
                sRes += appendSourceLabel(eachSen.getFeed());
            }
        }
        sRes += getFirstLevelSeparator();
        return sRes;
    }
    /**
     *
     * @param sUserSources the user sources preferences
     * @return true if userSources are equal to all sources, therefore
     * user has not removed any sources.
     */
    private boolean areAllSources(String sUserSources) {
        HashMap<String, String> hsAll = (HashMap<String, String>) this.ids.loadObject("Links", "generic");
        Set<String> hsUserSources = hsAll.keySet();
        String[] saUserSources = sUserSources.split(getFirstLevelSeparator());
        if (saUserSources.length == hsUserSources.size()) {
            return true;
        }
        return false;
    }

    private LinkedList<Sentence> filterScrapSentences(LinkedList<Sentence> lsSen) {

        for (Iterator<Sentence> iSi = lsSen.iterator(); iSi.hasNext();) {
            Sentence ob = iSi.next();
            // remove unmeaningfull sentences that have [...] at their ending
            // e.g. "He stated that he does not [...]"
            if (ob.getSnippet().matches(".*\\[\\.{3}\\]\\Z")
                    // or "He stated that he does not..." (... ... ... ...)
                    || ob.getSnippet().matches(".*\\.{3}\\s*(\\s*\\.{2,})*\\s*\\Z")) {
                if (lsSen.size() > 1) {
                    iSi.remove();
                }
            }
        }
        return lsSen;
    }
}


