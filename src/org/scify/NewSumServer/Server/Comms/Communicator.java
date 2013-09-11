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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.scify.NewSumServer.Server.Comms.Communicator.LOGGER;
import org.scify.NewSumServer.Server.DataCollections.FeedSources;
import org.scify.NewSumServer.Server.JSon.CategoriesData;
import org.scify.NewSumServer.Server.JSon.JSon;
import org.scify.NewSumServer.Server.JSon.LanguageData;
import org.scify.NewSumServer.Server.JSon.SnippetData;
import org.scify.NewSumServer.Server.JSon.SourceData;
import org.scify.NewSumServer.Server.JSon.SummaryData;
import org.scify.NewSumServer.Server.JSon.TopicData;
import org.scify.NewSumServer.Server.JSon.TopicsData;
import org.scify.NewSumServer.Server.Searching.Indexer;
import org.scify.NewSumServer.Server.SystemFactory.Configuration;
import org.scify.NewSumServer.Server.Storage.IDataStorage;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Sentence;
import org.scify.NewSumServer.Server.Structures.Topic;
import org.scify.NewSumServer.Server.Summarisation.ArticleClusterer;
import org.scify.NewSumServer.Server.Summarisation.RedundancyRemover;
import org.scify.NewSumServer.Server.Summarisation.Summariser;
import org.scify.NewSumServer.Server.Utils.Main;
import org.scify.NewSumServer.Server.Utils.Utilities;

/**
 * The Class that contains all the methods required for interaction with the
 * Client.
 *
 * @author George K. <gkiom@scify.org>
 */
public class Communicator {

    /**
     * The Logger class, inherited from main
     */
    protected static Logger     LOGGER = (Main.getLogger() != null)
            ? Main.getLogger() : Logger.getAnonymousLogger();
    
    // get configuration handler
    protected Configuration     AllFilesConf;
    
    // get config per language
    protected Configuration     PerLangConfig;
    

    private static final String ALL_SOURCES = "All";

    /**
     * Communicators specific exceptions
     */
    /**
     * Exception that points out that an input source doesn't exist
     */
    public class InvalidSourceException extends Exception {

        private static final String message = "Our highly experienced source pickers haven't included"
                + " the source you asked for to our archive\n";

        public InvalidSourceException() {
            super(message);
        }
    }

    /**
     * Exception that points out that no relevant topics were found for current
     * input
     */
    public class NoRelevantTopicFoundException extends Exception {

        private static final String message = "Our highly trained personnel shuffled through the archives poignantly"
                + " but could not find such a topic"
                + " relevant with such a keyword\n";

        public NoRelevantTopicFoundException() {
            super(message);
        }
    }

    /**
     * Exception that points out that the current topicID was not found
     */
    public class TopicIDNotFoundException extends Exception {

        private static final String message = "Our highly trained personnel shuffled through the archives poignantly"
                + " but could not find such a topicID"
                + " in the current context\n";

        public TopicIDNotFoundException() {
            super(message);
        }
    }

    /**
     * Exception that points out the input keyword was a null string
     */
    public class NullKeywordException extends Exception {

        private static final String message = "We didn't know what to do with a null keyword search, so here is your"
                + " well deserved exception!\n";

        public NullKeywordException() {
            super(message);
        }
    }

    /**
     * Exception that points that no summary was found for current input
     */
    public class SummaryNotFoundException extends Exception {

        private static final String message = "We couldn't find a summary for this topic\n";

        public SummaryNotFoundException() {
            super(message);
        }
    }

    /**
     * Exception that points out that the current topicID was not found amongst
     * current sources
     */
    public class TopicIDNotInUserSourcesException extends Exception {

        private static final String message = "We couldn't find a summary from these user sources in the specified topic\n";

        public TopicIDNotInUserSourcesException() {
            super(message);
        }
    }

    /**
     * Exception that points out that something went wrong, usually some kind of
     * internal exception
     */
    public class ServerErrorException extends Exception {

        private static final String message = "Oops! Something went awfully wrong, some of our elves"
                + " must have slacked off again...\n";

        public ServerErrorException() {
            super(message);
        }
    }
    /**
     * The Data Storage Interface
     */
    protected IDataStorage ids;
    /**
     * The Indexer Used
     */
    protected Indexer ind;
    /**
     * The Article Clusterer
     */
    protected ArticleClusterer ac;
    /**
     * The Summariser
     */
    protected Summariser sum;
    /**
     * Max Sentences per summary fetched
     */
    public static int iOutputSize = Main.iOutputSize;
    
    private ArrayList<String> lsSortedIDs = null;

    
//    /**
//     * Reads the configuration file saved by the NewSum server
//     */
//    public static void initStaticVariables() {
//        LOGGER.info("initStaticVariables");
////        Config = new File("./data/BaseDir/ServerConfig.txt");
//        Switches = readConfigFile();
//        sSourcesPath = (String) Switches.get("PathToSources");
//        iOutputSize = Main.iOutputSize; //Max Sentences per summary
//        LOGGER = (Main.getLogger() != null) ? Main.getLogger() : Logger.getAnonymousLogger();
//
//        LOGGER.info("initStaticVariables done");
//    }
    

    /**
     * Main constructor
     *
     * @param ids The data storage module
     * @param ac The Article Clusterer
     * @param sum The Summariser
     * @param ind The indexer
     */
    public Communicator(IDataStorage ids, ArticleClusterer ac,
            Summariser sum, Indexer ind, String sLang) {
        this.ids = ids;
        this.ac = ac;
        this.sum = sum;
        this.ind = ind;
        
        this.AllFilesConf = new Configuration(Main.getPropertiesFilePath());
        
        this.PerLangConfig = new Configuration(this.AllFilesConf.getServerConfigFile(sLang));
        
        // TODO prop. will require Lang parameter from service, in order to search 
        // for proper path and locale.
    }
    /**
     * Returns the list of supported languages.
     *
     * @return An Array of String in JSON Format containing supported languages.
     */
    public String getAvailableLanguages() {
        LanguageData languages = 
            new LanguageData(new ArrayList(Arrays.asList(this.PerLangConfig.getAvailableLanguages())));
        return languages.jsonize();
    }
    
    /**
     * Returns the list of default sources the server uses.
     *
     * @throws ServerErrorException
     * @return An Array of FeedSources in JSON format containing all the links
     * and their associated labels
     */
    public String getLinkLabelsFromFile() throws Exception {
        FeedSources sources=null;
        try{
            sources=FeedSources.load();
        }catch(Exception e){
            throw new ServerErrorException(); //if there is a problem reading files throw ServerError
        }
        return sources.jsonize();
    }

    /**
     * Using the User's Sources, returns the Categories that these Sources
     * belong to.
     *
     * @param sUserSources an Array of Strings in JSON format containing the
     * User URL paths. if null or "All", all Sources are accepted.
     * @throws InvalidSourceException, JsonIOException, JsonSyntaxException.
     * @return The Categories for the specified user sources.
     * @since 1.1
     */
    public String getCategories(String JSONizedSources) throws Exception {
        //for every item in the array, if item is in the generic
        //sources map, add it to the categories set
        ArrayList<String> userSources =
                JSon.unjsonize(JSONizedSources, ArrayList.class); //throws JsonIOException, JsonSyntaxException
        //if user passes invalid JSON format or different JSON object format from what was expected
        if (userSources == null || ALL_SOURCES.equalsIgnoreCase(userSources.get(0)) || areAllSources(userSources)) {
            //Accept all sources and return categories from saved file
            Collection <String> tryRead;
            try{
                tryRead=this.ids.readGenericCategories(); //try to get Generic Categories
            }
            catch(Exception e){
                throw new ServerErrorException(); //if something goes wrong
            }
            if(tryRead==null){
                throw new ServerErrorException(); //if null is returned
            }
            CategoriesData genericCategs = new CategoriesData(tryRead);
            //TODO IGNORE UNCLASSIFIED CATEGORY

            //String[] aCats = genericCategs.toArray(new String[0]);
            //return Utilities.joinArrayToString(aCats, getFirstLevelSeparator());
            return genericCategs.jsonize();
        } else { //filter using User Sources
            HashSet uniqueCats = new HashSet<String>(); //the categories to send with hashSet to get                                       
            HashMap<String, String> hsSources = //^  rid of duplicates
                    this.ids.readSources("generic");
            // transform the string to an array, using the separator delimiter
            for (String each : userSources) {
                if (hsSources.containsKey(each)) {
                    uniqueCats.add(hsSources.get(each));
                }
            }
            if (uniqueCats.isEmpty()) {
                throw new InvalidSourceException();
            }
            CategoriesData finalCategories = new CategoriesData(uniqueCats); //create return type
            return finalCategories.jsonize();
        }
    }

    /**
     * Using the User's Sources and the preferred category, returns the matching
     * topics.
     *
     * @param JSONizedSources a string containing the userSources in JSON
     * format.
     * @param JSONizedCategory a string containing the category of interest in
     * JSON format.
     * @throws TopicIDNotFoundException, JsonIOException, JsonSyntaxException.
     * @return The Topics contained in this category that match the user sources
     * as a TopicsData object in JSON format.
     * @since 1.1
     */
    public String getTopics(String JSONizedCategory, String JSONizedSources) throws Exception {
        //load the Clustered Topics Map
        String sCategory = JSon.unjsonize(JSONizedCategory, String.class);//throws JsonIOException, JsonSyntaxException
        ArrayList<String> userSources = JSon.unjsonize(JSONizedSources, ArrayList.class);//throws JsonIOException, JsonSyntaxException
        //if user passes invalid JSON format or different JSON object format from what was expected
        HashMap<String, Topic> ClusteredTopics = this.ac.getArticlesPerCluster();
        //The Topics to return
        ArrayList<Topic> tTopics = new ArrayList<Topic>();
        TopicsData data = new TopicsData();
        if (ClusteredTopics == null) {
            LOGGER.log(Level.WARNING, "Could Not Load Articles Per Cluster");
            throw new ServerErrorException();
        }
        if (userSources == null || ALL_SOURCES.equalsIgnoreCase(userSources.get(0)) || areAllSources(userSources)) {
            Iterator it = ClusteredTopics.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry t = (Map.Entry) it.next();
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
                        if (userSources.contains(ls.getFeed())) {
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
            throw new TopicIDNotInUserSourcesException();
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
        for (Topic each : tTopics) { //TODO change
            data.add(new TopicData(each.getID(), each.getTitle(), each.getDate(), each.getArticlesCount(), each.getImageSrc()));
        }
        return data.jsonize();
    }

    /**
     * Searches the Articles folder with the specified keyword and returns the
     * articles containing the keyword sorted by the number of occurrences.
     *
     * @param ind The indexer that is used for indexing
     * @param jSOnizedKeyword The search query that the user enters as a string
     * in JSON format.
     * @param JSONizedSources A JSON formatted list of acceptable sources. if
     * "All", the User Accepts all known sources. The sources are in fact URL
     * patterns. the Topics in relation to the search term, in descending order.
     * @throws ServerErrorException,NullKeywordException,
     * NoRelevantTopicFoundException, JsonIOException, JsonSyntaxException.
     * @since 1.0
     */
    public String getTopicsByKeyword(Indexer ind, String JSONizedKeyword, String JSONizedSources, String sLang) throws Exception {
        try {
            TopicsData sRes = new TopicsData(); //initialize object to be returned through JSON
            //unjsonize everything
            String sKeyword = JSon.unjsonize(JSONizedKeyword, String.class);//throws JsonIOException, JsonSyntaxException
            ArrayList<String> userSources = JSon.unjsonize(JSONizedSources, ArrayList.class);//throws JsonIOException, JsonSyntaxException
            //if user passes invalid JSON format or different JSON object format from what was expected
            String sUserSources = ""; //create sUserSources as it would have been expected before JSON changes
            if (userSources == null) {
                sUserSources = null;
            } else {
                if (ALL_SOURCES.equalsIgnoreCase(userSources.get(0))) {
                    sUserSources = "All";
                } else {
                    for (String each : userSources) {
                        sUserSources += each;
                    }
                }
            }
            if (sKeyword == null) {
                LOGGER.log(Level.SEVERE, "User attempted to search using a null keyword as target");
                throw new NullKeywordException();
            }
            ArrayList<String> topicIDsByKeyword = null;
            // TODO change with Lang parameter Πωπω μπελάς!
            topicIDsByKeyword =
                this.ac.getTopicIDsByKeyword(ind, sKeyword.trim(), sUserSources, 8,
                        new Locale(sLang));
            //ArrayList<String> NullTopicIDs = new ArrayList<String>(); //handle no output
            if (topicIDsByKeyword == null) { //if nothing found
                //sRes NullTopicIDs.get(0); // No relevant topic found
                throw new NoRelevantTopicFoundException();
            } else {
                // create Topic for each TopicID, pack it and return it
                // load the Clustered Topics Map
                HashMap<String, Topic> ClusteredTopics =
                        this.ac.getArticlesPerCluster();
                // Init the Topics to return
                // for each ID, add the Topic to the List
                for (String eachID : topicIDsByKeyword) {
                    Topic temp = ClusteredTopics.get(eachID);
                    if (ClusteredTopics.containsKey(eachID)) {
                        sRes.add(new TopicData(temp.getID(), temp.getTitle(), temp.getDate(), temp.getArticlesCount(), null));
                    }
                }
            }
            return sRes.jsonize();
        } catch (FileNotFoundException e) {
            throw new ServerErrorException();
        } catch (IOException e) {
            throw new ServerErrorException();
        }
    }

    /**
     * Get the summary that corresponds to the selected topicID and the
     * specified user sources.
     *
     * @param JSONizedTopicID A JSON formatted string topic ID of interest
     * @param JSONizedSources A JSON formatted list of acceptable sources. if
     * "All", the User Accepts all known sources. The sources are in fact URL
     * patterns.
     * @throws SummaryNotFoundException, TopicIDNotInUserSourcesException,
     * TopicIDNotFoundException, JsonIOException, JsonSyntaxException.
     * @return The summary for that topic, using only sentences from acceptable
     * sources
     * @since 1.1
     */
    public String getSummary(String JSONizedTopicID, String JSONizedSources) throws Exception {
        // return the Summary of the given topic ID
        String sTopicID = JSon.unjsonize(JSONizedTopicID, String.class); //throws JsonIOException, JsonSyntaxException
        ArrayList<String> userSources = JSon.unjsonize(JSONizedSources, ArrayList.class);//throws JsonIOException, JsonSyntaxException
        //if user passes invalid JSON format or different JSON object format from what was expected
        ArrayList<SourceData> sources;
        ArrayList<SnippetData> snippets;
        // Returns the Summary of the given topic ID, according to user sources
        // Get the topic from the topic ID
        Topic tp;
        try {
            tp = this.ac.getArticlesPerCluster().get(sTopicID); //get The topic
            if (tp == null) {                                       //tp may be null
                throw new NullPointerException();
            }
        } catch (NullPointerException ex) { //if sTopicID not contained in map
            throw new TopicIDNotFoundException();
        }
        ArrayList<Sentence> lsSen = new ArrayList(this.sum.getSummary(tp));
        if (lsSen.isEmpty()) {
            LOGGER.log(Level.WARNING, "No summary for Topic {0}", sTopicID);
            throw new SummaryNotFoundException();
        } else { //ommit small sentences
            for (Iterator<Sentence> ite = lsSen.iterator(); ite.hasNext();) {
                Sentence ob = ite.next();
                if (ob.getSnippet().split("[;,. ]").length < 5) {
                    ite.remove();
                }
            }
        }
        // keep only iOutputSize Sentences in the List
        while (lsSen.size() > iOutputSize) {
            lsSen.remove(lsSen.size() - 1);
        }
        // Accept all user sources
        if (userSources == null
                || ALL_SOURCES.equalsIgnoreCase(userSources.get(0))
                || areAllSources(userSources)) {
            // Add all source links at start of sRes String,
            // independently of what will get removed from filtering
            sources = getSummarySources(lsSen);
            // Filter scrap sentences, remove redundant sentences
            lsSen = new ArrayList(new RedundancyRemover().removeRedundantSentences(filterScrapSentences(lsSen)));
            //get snippets!
            snippets = getSummarySnippets(lsSen);


        } else { // if user has limited Sources preference
            // the List containing all the article snippets of interest
            ArrayList<Sentence> lsSummary = new ArrayList<Sentence>();
            //filter Summary using User Sources

            for (Sentence each : lsSen) {
                if (userSources.contains(each.getFeed())) {
                    if (!lsSummary.contains(each)) {
                        lsSummary.add(each);
                    }
                }
            }
            if (!lsSummary.isEmpty()) {
                //Add all source links at start of sRes String
                sources = getSummarySources(lsSummary);
                // Filter scrap sentences,
                // remove redundant sentences and apply result
                lsSummary = new ArrayList(new RedundancyRemover().removeRedundantSentences(filterScrapSentences(lsSummary)));

                //get snippets!
                snippets = getSummarySnippets(lsSummary);
            } else {
                // User unlucky, all sources he kept had scrap content
                LOGGER.log(Level.INFO,
                        "No Summary from these user sources in the specified topic");
                throw new TopicIDNotInUserSourcesException();
            }
        }
        //Get the Feed link of each sentence and
        //Append Source Label For this feed to the sentence
        SummaryData summary = new SummaryData(sources, snippets);
        return summary.jsonize();
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
     * @param sSourceFeed the source feed of the article
     * @return the Label of the Source URL link
     */
    private String appendSourceLabel(String sSourceFeed) {
        HashMap<String, String> hsLabels = (HashMap<String, String>) this.ids.loadObject("SourceLabels", "generic");
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
     *
     * @return A link-separated string containing all the URL's and labels for
     * the specified summary
     */
    private ArrayList<SourceData> getSummarySources(ArrayList<Sentence> sentences) {
        ArrayList<SourceData> sources = new ArrayList();
        for (Sentence eachSen : sentences) {
            if (!sources.contains(new SourceData(eachSen.getSource(), null, null))) { //created object just to use contains
                sources.add(new SourceData(eachSen.getSource(), appendSourceLabel(eachSen.getFeed()), eachSen.getSourceImageUrl()));
            }
        }
        return sources;
    }

    private ArrayList<SnippetData> getSummarySnippets(ArrayList<Sentence> sentences) {
        ArrayList<SnippetData> snippets = new ArrayList();
        for (Sentence eachSen : sentences) {
            snippets.add(new SnippetData(eachSen.getSnippet(), eachSen.getSource(), appendSourceLabel(eachSen.getFeed()), eachSen.getFeed()));
        }
        return snippets;
    }

    /**
     *
     * @param sUserSources the user sources preferences
     * @return true if userSources are equal to all sources, therefore user has
     * not removed any sources.
     */
    private boolean areAllSources(ArrayList<String> userSources) {
        HashMap<String, String> hsAll = (HashMap<String, String>) this.ids.loadObject("Links", "generic");
        Set<String> hsUserSources = hsAll.keySet();
        return userSources.containsAll(hsUserSources);
    }

    private ArrayList<Sentence> filterScrapSentences(ArrayList<Sentence> lsSen) {

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
