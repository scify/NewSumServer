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

package org.scify.NewSumServer.Server.Summarisation;

import gr.demokritos.iit.conceptualIndex.structs.Distribution;
import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramSymWinGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentWordGraph;
import gr.demokritos.iit.jinsect.events.WordEvaluatorListener;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;
import gr.demokritos.iit.jinsect.structs.Pair;
import gr.demokritos.iit.jinsect.utils;
import java.io.*;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.search.ScoreDoc;
import org.scify.NewSumServer.Server.Searching.Indexer;
import org.scify.NewSumServer.Server.Searching.Searcher;
import org.scify.NewSumServer.Server.Storage.IDataStorage;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Topic;
import static org.scify.NewSumServer.Server.Summarisation.ArticleClusterer.LOGGER;
import org.scify.NewSumServer.Server.Utils.Main;
import org.scify.NewSumServer.Server.Utils.Utilities;

/**
 * The Clusterer Class. Parses a given list of {@link Article}s and groups all
 * articles talking about the same subject in one {@link Topic}.
 * @author ggianna
 */
public class ArticleClusterer {

    // Will change when clusterer is updated
    private final int VERSION = 1;

    /**
     * The Logger Class used for logging various info and higher level messages
     */
    protected final static Logger               LOGGER     = Main.getLogger();
    /**
     * The separator used for creating the Article Text files.
     */
    protected final static String               sSeparator = " === ";
    /**
     * The Set containing Topics
     */
    protected HashMap<String, Topic>            hsArticlesPerCluster;
    /**
     * The Set containing the Topics from the previous run
     */
    protected HashMap<String, Topic>            PreviousClusteredTopics;
    /**
     * An Article,UUID map
     */
    protected HashMap<Article, String>          hsClusterPerArticle;
    /**
     * The Original List of Articles to process
     */
    protected List<Article>                     origArticles;
    /** The folder where the Articles will be saved */
    protected String                            ArticlePath;

    /**
     * The Data Storage Module for various I/O operations
     */
    protected IDataStorage                      ids;
    /**
     * Counts the Topics that were assigned an older ID
     */
    private Integer tChanged = 0;
    /**
     * The list containing all the pairs of articles to be fed to the 
     * cluster calculation engine
     */
    private List<Pair> lsArticlePairs = Collections.synchronizedList(new LinkedList());

    /**
     * Main Constructor of The ArticleClusterer Class.
     * After the variables are initialized, the Clusters are being calculated
     * @param lsArticles The Articles that will be clustered
     * @param ids The The package used for storage
     * @param ArticlePath The path where the CLustered
     * Articles will be stored as text files
     */
    public ArticleClusterer(List<Article> lsArticles,
            IDataStorage ids,
            String ArticlePath) {
        // should be constructed with the list of Articles
        // that the method getAllNews() of the SourceParser class returns

        // Keep copy of articles
        origArticles            = new ArrayList(lsArticles);
        // Init maps
        hsArticlesPerCluster    = new HashMap<String, Topic>();
        hsClusterPerArticle     = new HashMap<Article, String>();
        this.ids                = ids;
        this.ArticlePath        = ArticlePath;

//        // DEBUG LINES
//        System.out.println("Input " + lsArticles.size() + " articles");
//        //////////////
    }

    /**
     *
     * @param aOne The First Article
     * @param aTwo The Second Article
     * @return A graph similarity object between the two articles
     */
    protected GraphSimilarity compareArticles(Article aOne,
        Article aTwo) {

        // Changed to WORD GRAPHS
        DocumentWordGraph dgFirstGraph =
                new DocumentWordGraph();
        DocumentWordGraph dgSecondGraph =
                new DocumentWordGraph();

        dgFirstGraph.WordEvaluator = new WordEvaluatorListener() {

            @Override
            public boolean evaluateWord(String string) {
                // Keep only capitalized words!
                // TODO: IMPROVE!!!
//                boolean bPass = (string.matches("\\p{javaUpperCase}+.*"));
//                // DEBUG LINES
//                if (bPass)
//                    System.out.println(string);
                //////////////
                return ((string.length() > 3) && (string.matches("\\p{javaUpperCase}+.*")))
                        || (string.matches("\\d+"));
            }
        };
        dgSecondGraph.WordEvaluator = dgFirstGraph.WordEvaluator;


        dgFirstGraph.setDataString(aOne.getTitle() + " " + aOne.getText());
        dgSecondGraph.setDataString(aTwo.getTitle() + " " + aTwo.getText());
        // DEBUG LINES
//        if ((dgFirstGraph.length() < 10) || (dgSecondGraph.length() < 10)) {
//            System.out.println("1st Graph size:" + dgFirstGraph.length());
//            System.out.println("2nd Graph size:" + dgSecondGraph.length());
//        }
        NGramCachedGraphComparator ngc = new NGramCachedGraphComparator();
        return ngc.getSimilarityBetween(dgFirstGraph, dgSecondGraph);
    }
    /**
     * Clusters the Articles and updates the
     * {@link #hsClusterPerArticle} and {@link #hsArticlesPerCluster}  Maps.
     */
    public void calculateClusters() {
        // DEBUG LINES
        LOGGER.log(Level.INFO, "Clustering Version :{0}", VERSION);
//        LOGGER.log(Level.INFO,"JISNECT splitToWords:" +
//                utils.printIterable(Arrays.asList(
//                utils.splitToWords("This is a test...")), " "));
        ///////////////

        // Get pairs of clusters, without repetitions or in-cluster pairs
        List<Pair> lsPairs = getPairs(origArticles);

        // Init parallel execution
        ExecutorService es = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
        final ConcurrentHashMap<Pair<Article,Article>,Boolean> hmResults = new
                ConcurrentHashMap<Pair<Article, Article>, Boolean>();

        // For every pair
        LOGGER.log(Level.INFO, "Examining pairs...");
        for (final Pair p : lsPairs) {
            es.submit(new Runnable() {

                @Override
                public void run() {
                    // Get first article of pair
                    Article aA = (Article) p.getFirst();
                    // Get second article from pair
                    Article aB = (Article) p.getSecond();
                    // Check whether articles match
                    boolean bMatch = getMatch(aA, aB);
                    synchronized (hmResults) {
                        // DEBUG LINES
//                        if (bMatch)
//                            System.out.println("Match " + aA + "\n" + aB);
                        //
                        hmResults.put(p, bMatch);
                    }

                }
            });
        }
        // Await completion
        es.shutdown();
        try {
            es.awaitTermination(1, TimeUnit.DAYS);
            LOGGER.log(Level.INFO, "Examining pairs DONE.");
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return;
        }
        // Assign clusters
        // For every pair
        for (Pair<Article,Article> p: hmResults.keySet()) {
            Article aA = p.getFirst();
            Article aB = p.getSecond();
            boolean bMatch = hmResults.get(p);

            // DEBUG LINES
//                    if (!tmpCateg.equals(aA.getCategory())) { //debugging only
//                        tmpCateg = aA.getCategory();
//                        System.out.println("Calculating Clusters for " + tmpCateg);
//                    }
            //////////////

            String sClusterID;
            // On a match
            if (bMatch) {
                // If both aA and aB belong to a cluster
                if (hsClusterPerArticle.containsKey(aA) &&
                 hsClusterPerArticle.containsKey(aB)) {
                    // collapse their clusters.
                    collapseTopics(hsClusterPerArticle.get(aA), hsClusterPerArticle.get(aB));
                    // Go on with next pair
                    continue;
                }
                // If a is not in a cluster
                if (!hsClusterPerArticle.containsKey(aA)) {
                    // create a new cluster
                    // init cluster
                    Topic tNew = new Topic();
                    sClusterID = tNew.getID();
                    // add article there
                    tNew.add(aA);
                    // update mappings
                    hsArticlesPerCluster.put(sClusterID, tNew);
                    hsClusterPerArticle.put(aA, sClusterID);
                }

                // If aB already belongs to a cluster
                if (hsClusterPerArticle.containsKey(aB)) {
                    // collapse the aA and aB clusters.
                    collapseTopics(hsClusterPerArticle.get(aA), hsClusterPerArticle.get(aB));
                    // continue with next test
                    continue;
                }
                else {
                    // create a new cluster with a RANDOM UUID
                    Topic tNew = new Topic();
                    sClusterID = tNew.getID();
                    // init cluster
                    hsArticlesPerCluster.put(sClusterID, tNew);
                    // add articles there
                    hsArticlesPerCluster.get(sClusterID).add(aB);
                    // update mappings
                    hsClusterPerArticle.put(aB, sClusterID);
                }
            }
            else // if there is no match
            {
                // If a is not in a cluster
                if (!hsClusterPerArticle.containsKey(aA)) {
                    // create a new cluster
                    // init cluster
                    Topic tNew = new Topic();
                    sClusterID = tNew.getID();
                    // add article there
                    tNew.add(aA);
                    // update mappings
                    hsArticlesPerCluster.put(sClusterID, tNew);
                    hsClusterPerArticle.put(aA, sClusterID);
                }

                // If aB does not belong to a cluster
                if (!hsClusterPerArticle.containsKey(aB)) {
                    // create a new cluster with a RANDOM UUID
                    Topic tNew = new Topic();
                    sClusterID = tNew.getID();
                    // add articles there
                    tNew.add(aB);
                    // update mappings
                    hsArticlesPerCluster.put(sClusterID, tNew);
                    hsClusterPerArticle.put(aB, sClusterID);
                }
            }
        }

        // debugging Method
        checkForInconsistencies();
        //add as Topic Date the date of it's newest Article
        Iterator nit = hsArticlesPerCluster.entrySet().iterator();
        while (nit.hasNext()) {
            Map.Entry mp = (Map.Entry) nit.next();
            Topic tmpTopic = (Topic) mp.getValue();
            tmpTopic.setNewestDate(true);
            // Also set as the Topic Title for each Topic the Title from it's newest Article
            tmpTopic.setTitleFromNewest();
        }
        // remove some single topics, if older than two days, and with respect
        // to keeping the same size of single topics for each category
//        removeSingleTopics(30, 2);
        // Save all articles to file, in Article Path in order to be indexed by lucene
        // Also saves the hsArticlesPerCluster Map to file, for future access
        //
        try {
            LOGGER.log(Level.INFO, "Saving Clusters...");
            saveAllClusteredArticles();
            LOGGER.log(Level.INFO, "Clusters saved succesfully");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not save CLustered Articles ", ex.getMessage());
        }
        // debugging Method
//        checkForInconsistencies();
    }
    /**
     * Collapses (i.e., merges) two topics (clusters) into a single one,
     * updating corresponding structures as required.
     * @param sTopic1ID The first topic. This topic will be updated.
     * @param sTopic2ID The second topic. This topic will be deleted.
     * @return True if a modification took place
     */
    protected boolean collapseTopics(String sTopic1ID, String sTopic2ID) {
        Topic t1 = hsArticlesPerCluster.get(sTopic1ID);
        Topic t2 = hsArticlesPerCluster.get(sTopic2ID);

        // If topics identical
        if (t1 == t2)
            // No need for collapse
            return false;

        // For every article in topic t2
        for (Article aCur: t2) {
            // Add it into topic t1
            t1.add(aCur);
            // Update indices
            hsClusterPerArticle.put(aCur, t1.getID());
            hsArticlesPerCluster.put(t1.getID(), t1);
        }
        // Remove t2 from structures
        t2.clear();
        hsArticlesPerCluster.remove(t2.getID());

        return true;
    }

    /**
     * Checks whether two articles talk about the same subject
     * @param aA The First Article
     * @param aB The Second Article
     * @return true if two articles talk about the same subject,
     * false otherwise.
     */
    public boolean getMatch(Article aA, Article aB) {

        //Create ifs for each category
        GraphSimilarity gs = compareArticles(aA, aB);
        double NVS = gs.SizeSimilarity == 0.0 ? 0.0 : gs.ValueSimilarity / gs.SizeSimilarity;
        // Updated rule for matching
        boolean bMatch = (NVS > 0.20) && (gs.SizeSimilarity > 0.10);
        // DEBUG LINES
//        if (bMatch)
//            System.out.println("**** Match (NVS=" + NVS + ", SS=" + gs.SizeSimilarity +
//                    ") : \n" + aA + "\n---\n" + aB);
        //////////////
        // check titles for word similarity
        boolean TitleMatch = isPossiblySameSentence(
                aA.getTitle(), aB.getTitle());
        // debug lines
//        if (test) {
//            Utilities.appendToFile("/home/gkioumis/Programming/Java/NewSum/NewSumServer/data/temp/TestingTitles.csv",
//                bMatch + " : " + TitleMatch + " === " + aA.getTitle() + " : " + aB.getTitle());
//        }
        //////////////
        return bMatch || TitleMatch;
    }
    private boolean isPossiblySameSentence(String s1, String s2) {
        // split to words
        String[] as1 = s1.split("[ :-;!?]+");
        String[] as2 = s2.split("[ :-;!?]+");
        // remove words smaller than 4 letters
        ArrayList<String> ls1 = new ArrayList<String>();
        for (String a : as1) {
            if (a.length() > 3) {
                ls1.add(a);
            }
        }
        ArrayList<String> ls2 = new ArrayList<String>();
        for (String b : as2) {
            if (b.length() > 3) {
                ls2.add(b);
            }
        }
        int iEqual = 0;
        // for each word, compare similarity of words
        for (int i=0; i < ls1.size(); i++) {
            for (String bWord : ls2) {
                if (isPossiblyEqual(ls1.get(i), bWord)) {
                    iEqual ++;
                    break; // continue from another base word
                }
            }
        }

        // measure similarity > 0.50
        //  = 2 * sum of words equal / (Len of Words 1 + Len of Words 2)
        float fSim = (float) 2 * iEqual / (ls1.size() + ls2.size());

        return fSim > 0.50;

    }
    /**
     *
     * @param aWord the first word
     * @param bWord the second word
     * @return true whether both words are greek, i.e. they all
     * consist of Greek characters
     */
    private boolean isBothGreekLocale(String aWord, String bWord) {
        return Utilities.isGreekWord(aWord) && Utilities.isGreekWord(bWord);
    }
    /**
     *
     * @param aWord The first word
     * @param bWord The second word
     * @return True when the two words are possibly similar,
     * by counting letter equality
     */
    private boolean isPossiblyEqual(String aWord, String bWord) {
        // set collator locale and strength
        Collator col;
        if (isBothGreekLocale(aWord, bWord)) {
            col = Collator.getInstance(new Locale("el", "gr"));
        } else {
            col = Collator.getInstance(Locale.ENGLISH);
        }
        col.setStrength(Collator.PRIMARY);
        // trim words
        aWord = aWord.trim(); bWord = bWord.trim();
        // if words equal return
        if (aWord.equals(bWord)) {
            return true;
        }
        // get the max number of characters
        int iMax = Math.max(aWord.length(), bWord.length());
        int iMin = Math.min(aWord.length(), bWord.length());
        int iSame = 0;
        // compare each character (string)
        boolean bCon = true; // must be continuous match, else abort
        for (int i = 0; i < iMin; i++) {
            if (col.compare(aWord.substring(i, i+1), bWord.substring(i, i+1)) == 0) {
                iSame ++;
            } else {
                bCon = false;
            }
            if (!bCon) {
                break;
            }
        }
        if ((iSame == iMin) || ((float) iSame / iMax) >= 0.65 ) {
            return true;
        }
        return false;
    }


    /**
     * Use to create Article Pairs
     * @param lsArticleList the List of Articles to mess
     * @return A list of article Pairs
     */
    private List<Pair> getPairs(final List<Article> lsArticleList) {
        // Create a list of Pairs
        long time = System.currentTimeMillis();
        // get available processors
        int iThreads = Runtime.getRuntime().availableProcessors();
        LOGGER.log(Level.INFO, "Creating Pairs on {0} threads...", iThreads);
        // Create executor service
        ExecutorService es = Executors.newFixedThreadPool(iThreads);
        // divide list into iThreads parts
        int iParts = lsArticleList.size() / iThreads;
        final List allLists = new ArrayList<List<Article>>();
        // create sublists
        for (int i = 0; i < lsArticleList.size(); i += iParts) {
            allLists.add(lsArticleList.subList(i, i + Math.min(iParts, lsArticleList.size() - i)));
        }
        // for every sublist
        for (final ListIterator<List<Article>> it = allLists.listIterator(); it.hasNext();) {
            // call new thread
            es.submit(new Runnable() {

                @Override
                public void run() {
                    // create a set of Pairs
                    HashSet<Pair<Article, Article>> tmpPairs = new HashSet<Pair<Article, Article>>();
                    // process every sublist
                    List tmpList = it.next();
                    // for every sublist's article
                    for (ListIterator<Article> curListIter = tmpList.listIterator(); curListIter.hasNext();) {
                        // get article
                        Article aFirst = curListIter.next();
                        // compare with all articles from main list
                       for (ListIterator<Article> mainListIter = lsArticleList.listIterator(); mainListIter.hasNext();) {
                            // get article
                            Article aSecond = mainListIter.next();
                            // compare category and source
                            if (aFirst.getCategory().equals(aSecond.getCategory())
                                    && !aFirst.getSource().equals(aSecond.getSource())) {                            
                                // create and add pair
                                Pair<Article, Article> tmpPair = new Pair(aFirst, aSecond);
                                if (!tmpPairs.contains(new Pair(aSecond, aFirst))) {
                                    tmpPairs.add(tmpPair);
                                }
                            }
                        }
                    }
                    // when done, add to final list
                    synchronized (lsArticlePairs) {
                        
                        lsArticlePairs.addAll(tmpPairs);
                        
                    }
                }
            });
        }
        es.shutdown();
        try {
            es.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }

        time = System.currentTimeMillis() - time;
        LOGGER.log(Level.INFO, "Created {0} Pairs in {1} seconds",
                new Object[] {lsArticlePairs.size(), time/1000});
        return lsArticlePairs;
    }

    /**
     *
     * @return A map containing a Unique identifier for
     * each Cluster and article list that the cluster is about
     */
    public HashMap<String, Topic> getArticlesPerCluster() {
        if (this.hsArticlesPerCluster != null) {
            if (!this.hsArticlesPerCluster.isEmpty()) {
                return this.hsArticlesPerCluster;
            }
        }
        try {
            return this.ids.readClusteredTopics();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                return null;
            }
    }
    /**
     *
     * @return A map containing an Article and the Unique Identifier
     * for the exact article
     */
    public HashMap<Article, String> getClusterPerArticle() {
        return this.hsClusterPerArticle;
    }
    /**
     * Initializes a new {@link org.scify.NewSumServer.Server.Searching.Searcher} object and
     * searches the Index with the specified query.
     * @param ind The Indexer to be used
     * @param sKeyword The Search Query
     * @param sUserSources The separator-delimited URL sources accepted by user
     * @param iMaxHits The max number of hits to accept
     * @param loc The locale of the text to process
     * @return A list of Topic IDs that contain articles related to the
     * search query, in descending order
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ArrayList<String> getTopicIDsByKeyword(Indexer ind, String sKeyword,
            String sUserSources, int iMaxHits, Locale loc)
                throws FileNotFoundException, IOException {

        LOGGER.log(Level.INFO, "Searching for {0}...", sKeyword);
        // Initialise a new Searcher and get the ScoreDocs found for the query
        Searcher se = new Searcher();
        List<ScoreDoc> lsResults;
        if (Utilities.isGreekWord(sKeyword)) {
            lsResults = se.searchIndex(ind.getIndexDirectory(),  //lower case with greek locale
                loc, sKeyword.toLowerCase(loc), iMaxHits);
        } else {
            lsResults = se.searchIndex(ind.getIndexDirectory(),
                loc, sKeyword.toLowerCase(), iMaxHits);
        }
        if (lsResults == null || lsResults.isEmpty()) {
            return null;
        }
        //get the <docId, filename> mappings
        HashMap<Integer, String> docFiles = se.getDocFiles();
        // debug
//        for (Map.Entry each : docFiles.entrySet()) {
//            Integer i = (Integer) each.getKey();
//            String e = (String) each.getValue();
//            System.out.println(String.valueOf(i) + ": " + e);
//        }
        // debug end
        //Initialize the <ClusterID, List<filename>> mapping
        HashMap<String, List<String>> docClusters = new HashMap<String, List<String>>();
        //Create the <UUID, TotalScore> Distribution and update it according to the data
        //Also update the <clusterID, list<filename>> map
        Distribution<String> d = new Distribution<String>();
        if ("All".equals(sUserSources) || sUserSources == null) { //Accept all user sources
            for (ScoreDoc sd: lsResults) {
                String ClusterID = getInfofromFile(docFiles.get(sd.doc), "ClusterID");
                d.increaseValue(ClusterID, sd.score);
                updateDocClusters(docClusters, ClusterID, docFiles, sd);
            }
        } else {
            for (ScoreDoc sd: lsResults) {
                String ArticleFeed = getInfofromFile(docFiles.get(sd.doc), "Feed");
                if (sUserSources.contains(ArticleFeed)) { //only if feed is accepted by user
                    String ClusterID = getInfofromFile(docFiles.get(sd.doc), "ClusterID");
                    d.increaseValue(ClusterID, sd.score);
                    updateDocClusters(docClusters, ClusterID, docFiles, sd);
                }
            }
        }
        SortedSet<Map.Entry> sorted_d = (SortedSet) Utilities.entriesSortedByValues(d.asTreeMap());
        ArrayList<String> TopicIDsHits = new ArrayList<String>();
        for (Map.Entry each : sorted_d) {
            TopicIDsHits.add((String) each.getKey());
        }
        if (!TopicIDsHits.isEmpty()) {
            // debug
//            for (String each : TopicIDsHits) {
//                System.out.println(each);
//            }
            // debug end
            return TopicIDsHits;
        } else {
            LOGGER.log(Level.INFO, " No Topics Found");
            return null;
        }
    }

    private void updateDocClusters(HashMap<String, List<String>> docClusters,
            String ClusterID, HashMap<Integer, String> docFiles,
            ScoreDoc sd) {
        if (!docClusters.containsKey(ClusterID)) {
            docClusters.put(ClusterID, new ArrayList<String>());
            docClusters.get(ClusterID).add(docFiles.get(sd.doc));
        } else {
            docClusters.get(ClusterID).add(docFiles.get(sd.doc));
        }
    }
    /**
     * Used by the getTopicIDsByKeyword method
     * to retrieve info about the Cluster ID, title, etc
     * @param sFileName The filename to read
     * @param Info The information we want to retrieve from the file
     * @return The Information that the file possesses about the article
     */
    private String getInfofromFile(String sFileName, String Info)
            throws FileNotFoundException, IOException {
        String sFullName = this.ArticlePath + sFileName;
        File fFile = new File(sFullName);
        if (fFile.canRead()) {
            FileInputStream fstream = new FileInputStream(fFile);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String sLine;
            while ((sLine = br.readLine()) != null) {
                if (sLine.startsWith(Info)) {
                    return sLine.split(sSeparator)[1].trim();
                }
            }
            in.close();
        } else {
            LOGGER.log(Level.SEVERE, "Error: Cannot read from file: {0}", fFile.toString());
                return null;
            } return null;
    }

    /**
     * <p>Saves all Clustered Articles to file, one Article per file.</p>
     * <p>- Stores data about the ClusterID, the feed, and the Category
     * in the beginning of the file</p>
     * <p>- Before saving the Clustered Topics Map, it calls
     * {@link #compareTopics(java.util.HashMap, java.util.HashMap)} first</p>
     * <p>- Also stores the {@link #hsArticlesPerCluster} map to file,
     * using the {@link #ids} module</p>
     * @throws IOException
     */
    private void saveAllClusteredArticles() throws IOException {
        // Save the Map that contains the list of articles per cluster
        try {
            // load the old topics map in memory before deleting
            this.PreviousClusteredTopics = (HashMap<String, Topic>) this.ids.readClusteredTopics();
            // Before saving the new map, compare the two runs in order to look for same topics,
            // and if such, assign the same Topic IDs from the previous run to the new Map
            boolean Changed = compareTopics(this.PreviousClusteredTopics, this.hsArticlesPerCluster);  // returns true or false
            if (Changed) {
                LOGGER.log(Level.INFO, "Found {0} Identical Topics and switched to old IDs", String.valueOf(tChanged));
            }
            // debugging
//            Utilities.writeTopicsToFile(hsArticlesPerCluster, "CurrentTopics");
//            Utilities.writeTopicsToFile(PreviousClusteredTopics, "PreviousTopics");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could Not Load Clustered Topics from Previous Run: {0}", ex.getMessage());
        } finally {
            // delete the old map
            this.ids.deleteObject("ClusteredTopics", this.ids.getGeneric());
            // Save the final Map, either updated with the comparison results or not
            this.ids.writeClusteredTopics(this.hsArticlesPerCluster);
        }
        // delete all files in Article Directory, in order to write the new
        // ones afterwards
        File f = new File(this.ArticlePath);
        if (f.isDirectory()) {
            f.setWritable(true);

            for (File each : f.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.getPath().endsWith(".txt");
                }
            })) {
                if (!each.delete()) {
                    LOGGER.log(Level.WARNING, "File {0} could not be deleted", each.getName());
                }
            }
        }
        // Save Each Article to a single Text file, so that it is used by the indexer later
        // Each Text File has the ClusterID information in it
        int counter = 1; // used for distinction between articles in the same topic
        Iterator it = this.hsClusterPerArticle.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            writeArticleToFile((Article) pair.getKey(),
                    this.ArticlePath, (String) pair.getValue(), counter);
            counter ++;
        }
    }

    /**
     * Saves An Article to a simple file. The File contains the ClusterID
     * information in it's first line
     * @param aArt The Article to store
     * @param sPathToFile The path where the file is saved
     * @param sCluster The Cluster ID of the Article
     * @throws IOException
     */
    private void writeArticleToFile(Article aArt, String sPathToFile,
             String sCluster, int counter) throws IOException {
        try {
            String sFullFileName = sPathToFile + sCluster +
                    "-" + String.valueOf(counter) + ".txt";
            File fFile = new File(sFullFileName);
            fFile.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(fFile, true));
            bw.write("ClusterID" + sSeparator + sCluster);
            bw.newLine();
            bw.write("Κατηγορία" + sSeparator + aArt.getCategory());
            bw.newLine();
            bw.write("Feed" + sSeparator + aArt.getFeed());
            bw.newLine();
            bw.write("Πηγή" + sSeparator + aArt.getSource());
            bw.newLine();
            bw.write("Date" + sSeparator + aArt.getDatetoString());
            bw.newLine();
            bw.write(aArt.getTitle());
            bw.newLine();
            bw.write(aArt.getText());
            bw.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could Not Write Article " + aArt.getTitle() + " to File",
            ex.getMessage());
        }
    }
    /**
     * Compares two Topic maps and searches for equal topics, using the
     * {@link #isTopicEqual(org.scify.NewSumServer.Server.Structures.Topic, org.scify.NewSumServer.Server.Structures.Topic) }
     * method. If an equality is found, the {@link Topic} on the newer map is assigned the
     * ID of the Topic from the older map.
     * @param Prev The map containing the topics from the previous run
     * @param Current The map containing the topics from the current run
     * @return true if even one topic had it's ID changed, false otherwise
     */
    private boolean compareTopics(HashMap<String, Topic> Prev, HashMap<String, Topic> Current) {
        // The hsRes map will map the current ID values to the
        // old ID values, if the topics are the same
        HashMap<String, String> hsRes = new HashMap<String, String>();
        // the hsTops map will keep the (previousID, currentTopic) mapping
        // in order to update the global maps afterwards.
        HashMap<String, Topic> hsTops = new HashMap<String, Topic>();
        // Iterate over the Current Map
        for (Map.Entry<String, Topic> cEntry : Current.entrySet()) {
            String cID = cEntry.getKey();
            Topic cTopic = cEntry.getValue();
            // Iterate over the Previous Map (The map from the previous run)
            for (Map.Entry<String, Topic> pEntry : Prev.entrySet()) {
                String pID = pEntry.getKey();
                Topic pTopic = pEntry.getValue();
                if (isTopicEqual(cTopic, pTopic)) {
//                    System.out.println(cTopic.getTitle() + "==" + pTopic.getTitle());
                    hsRes.put(cID, pID);                        // keep the ID pairs
                    hsTops.put(pID, cTopic);                    // Keep the articles for this ID
                    break;                                      // proceed to the next current topic
                }
            }
        }
//        while (cIt.hasNext()) {
//            Map.Entry cPair = (Map.Entry) cIt.next();
//            String cID = (String) cPair.getKey();
//            Topic cTopic = (Topic) cPair.getValue();
//            cur++;
//            System.err.println("Processing Current topic " + cur + " : " + cID);
//            while (pIt.hasNext()) {
//                Map.Entry pPair = (Map.Entry) pIt.next();
//                String pID = (String) pPair.getKey();
//                Topic pTopic = (Topic) pPair.getValue();
//                pre++;
//                System.err.println("\tWith previous topic " + pre + " : " + pID);
//                // check for Topic equality
//                if (isTopicEqual(cTopic, pTopic)) {
//                    System.err.println("\t\tMatch found");
//                    pre=0;
////                    System.err.println("Found an equal Topic " + cID + " ---- " + cTopic.getTitle());
//                    hsRes.put(cID, pID);                        // keep the ID pairs
////                    System.err.println("Keeping Current ID " + cID + " ------ to change to ---- " + pID);
//                    hsTops.put(pID, cTopic);                    // Keep the articles for this ID
//                    break;                                      // proceed to the next current topic
//                }
//            }
//        }
        if (hsRes.isEmpty()) { return false; }                  // no same topics, nothing to change
        // iterate over the (currentID, previousID) mapping and make the required changes
        Iterator nIt = hsRes.entrySet().iterator();
        while (nIt.hasNext()) {
            Map.Entry nPair = (Map.Entry) nIt.next();
            String cID = (String) nPair.getKey();               // the current ID
            String pID = (String) nPair.getValue();             // the ID from the old map, to restore
            if (this.hsArticlesPerCluster.containsKey(cID)) {   // should always contain that key
//                System.err.println("Changed Topic " + this.hsArticlesPerCluster.get(cID).getID());
                Topic tmpTopic = this.hsArticlesPerCluster.get(cID);
                tmpTopic.setID(pID);                            // Assign the old ID to this Topic
                this.hsArticlesPerCluster.remove(cID);          // remove the entry from the map and add the new one
                this.hsArticlesPerCluster.put(tmpTopic.getID(), hsTops.get(pID));
//                System.err.println("To " + this.hsArticlesPerCluster.get(pID).getID() + " -- " + this.hsArticlesPerCluster.get(pID).getTitle());
                tChanged++;                                     // Counter of operations done
                // update the reverse map for this topic
                for (Article each : this.hsArticlesPerCluster.get(pID)) {
                    if (this.hsClusterPerArticle.containsKey(each)) { // should always be true
                        // update mappings with new ID
                        this.hsClusterPerArticle.put(each, pID);
                    } else {
                        LOGGER.log(Level.WARNING, "Unexpected behaviour: {0} -- {1}",new Object[] {each, pID});
                    }
                }
            }
        }
        return true;                                            // changed IDs for same topics
    }
    /**
     * Compares two given topics, using Ordered Text Concatenation and Topic Date.
     * @param tA The first {@link Topic}
     * @param tB The Second {@link Topic}
     * @return true if the two topics are the same, false otherwise
     */
    private boolean isTopicEqual(Topic tA, Topic tB) {
        boolean match;
        // they have to be in the same category to compare
        if (tA.get(0).getCategory().equals(tB.get(0).getCategory())) {
            // if the topic has only one article, check title, date and return
            if (tA.size() == 1 && tB.size() == 1) {
                if (tA.getTitle().hashCode() == tB.getTitle().hashCode()) {
                    match = tA.getDate().hashCode() == tB.getDate().hashCode();
                } else {
                    match = false;
                }
            } else if (tA.size() == tB.size()) {
                // Otherwise get all text from the topic articles, and sort (simple unicode sorting)
                ArrayList<String> lsA = (ArrayList<String>) Utilities.getListOfStrings(tA);
                Collections.sort(lsA, String.CASE_INSENSITIVE_ORDER);
                ArrayList<String> lsB = (ArrayList<String>) Utilities.getListOfStrings(tB);
                Collections.sort(lsB, String.CASE_INSENSITIVE_ORDER);
                // get date for each topic
                String sdA = tA.getDateToString();
                String sdB = tB.getDateToString();
                // for every text and date, construct a single string
                StringBuilder sbA = new StringBuilder();
                for (String each : lsA) {
                    sbA.append(each);
                }
                // append date at the end
                sbA.append(sdA);
                // same for Topic B
                StringBuilder sbB = new StringBuilder();
                for (String each : lsB) {
                    sbB.append(each);
                }
                sbB.append(sdB);
                // Compare the two constructs and return
                match = sbA.hashCode() == sbB.hashCode();
            } else {
                match = false;
            }
        } else { // not in the same category
            match = false;
        }
        return match;
    }
    private void checkForInconsistencies() {
        // DEBUG LINES // Checking if maps are indeed reverse
        int iCnt = 0;
        for (Article aCur : hsClusterPerArticle.keySet()) {
            if (!hsArticlesPerCluster.get(hsClusterPerArticle.get(aCur)).contains(aCur)) {
            LOGGER.log(Level.SEVERE, "Mismatch found!");
            }
            iCnt++;
        }
        LOGGER.log(Level.INFO, "Checked {0} items.", iCnt);
        for (String sCurCluster : hsArticlesPerCluster.keySet()) {
            for (Article aCurArticle: hsArticlesPerCluster.get(sCurCluster)) {
                if (hsClusterPerArticle.get(aCurArticle).trim().compareTo(
                        sCurCluster.trim()) != 0) {
                    LOGGER.log(Level.SEVERE, "Mismatch found (reverse)!\n{0} != \n{1}\n", new
                                Object[] {hsClusterPerArticle.get(aCurArticle), sCurCluster});
                }
            }
        }
        LOGGER.log(Level.INFO, "Reversed Checked Mappings Done");
    }
    /**
     * Parses the {@link #hsArticlesPerCluster} map and removes some single
     * topics. The topics are removed if they are older than iDays from the
     * current date and the topic limit has not been reached
     * @param iMinSingleTopics the minimum number of single topics to keep
     * @param iDays the distance in days from the current date
     * per category
     */
    private void removeSingleTopics(int iMinSingleTopics, int iDays) {
//        System.out.println("initial " + this.hsArticlesPerCluster.size());
        int initial = this.hsArticlesPerCluster.size();
        Collection<String> sCategs = this.ids.readGenericCategories();
        Distribution<String> Count = new Distribution<String>();
        // get Single Topics Count per Category
        for (String sCurCateg : sCategs) {
            for (Map.Entry each : this.hsArticlesPerCluster.entrySet()) {
                Topic tmpTopic = (Topic) each.getValue();
                if (tmpTopic.getCategory().equals(sCurCateg) && tmpTopic.size() == 1) {
                    Count.increaseValue(sCurCateg, 1);
                }
            }
        }
        Calendar now = Calendar.getInstance();
        for (String sCurCateg : sCategs) {
            Iterator it = this.hsArticlesPerCluster.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry mp = (Map.Entry) it.next();
                Topic tmpTopic = (Topic) mp.getValue();
                if (tmpTopic.getCategory().equals(sCurCateg) && tmpTopic.size() == 1) {
                    if (Count.getValue(sCurCateg) > iMinSingleTopics) {
                        if (now.getTimeInMillis() - tmpTopic.getDate().getTimeInMillis() > (iDays*1000*60*60*24)) {
                            Count.setValue(sCurCateg, Count.getValue(sCurCateg) - 1); // decrease count by one
                            it.remove();
                            // update the reverse map
                            this.hsClusterPerArticle.remove(tmpTopic.get(0));
                        }
                    }
                }
            }
        }
        int iFinal = this.hsArticlesPerCluster.size();
        LOGGER.log(Level.INFO, "Removed {0} single Topics", initial - iFinal);
    }
    // DEBUG LINES
//    public static void main(String[] args) {
//        String[] saWords = {"testing", "USA", "Γιώργος", " ΜΑΡΙΝΑ ΦΛΟΙΣΒΟΥ", "Έξυπνος"};
//        DocumentWordGraph dgFirstGraph =
//            new DocumentWordGraph();
//        dgFirstGraph.WordEvaluator = new WordEvaluatorListener() {
//
//            @Override
//            public boolean evaluateWord(String string) {
//                // Keep only capitalized words!
//                // TODO: IMPROVE!!!
//                boolean bPass = (string.matches("\\p{javaUpperCase}+.*"));
//                // DEBUG LINES
//                if (bPass)
//                    System.out.println(string);
//                //////////////
//                return bPass;
//            }
//        };
//        dgFirstGraph.setDataString("Αυτή είναι μία χαρακτηριστική δοκιμή. Νομίζω. Γιώργος Γ.");
//
//        for (String sWord : saWords)
//            System.out.println(sWord + ":" +
//                    String.valueOf(sWord.matches("\\p{javaUpperCase}+.*")));
//    }
    /**
     * @deprecated
     * @param sSent
     * @param iCharCount
     * @return
     */
    private String[] ommitSmallWords(String[] sSent, int iCharCount) {
        List<String> lsSent = new ArrayList<String>(Arrays.asList(sSent));
        lsSent.removeAll(findSmallWords(lsSent, iCharCount));
        String[] aSent = lsSent.toArray(new String[0]);

        return aSent;
    }
    /**
     * @deprecated
     * @param lsSen
     * @param iCount
     * @return
     */
    private Collection<String> findSmallWords(List<String> lsSen, int iCount) {
        Collection<String> lsStr = new ArrayList<String>();
        for (String each: lsSen) {
            if (each.length() <= iCount) {
                lsStr.add(each);
            }
        }
        return lsStr;
    }
    /**
     * @deprecated
     * @param a
     * @param b
     * @param c
     * @return
     */
    private int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b),c);
    }
    /**
     * @deprecated
     * @param str1
     * @param str2
     * @return
     */
    private int computeLevenshteinDistance(CharSequence str1,
                    CharSequence str2) {
            int[][] distance = new int[str1.length() + 1][str2.length() + 1];

            for (int i = 0; i <= str1.length(); i++) {
                            distance[i][0] = i;
            }
            for (int j = 1; j <= str2.length(); j++) {
                            distance[0][j] = j;
            }
            for (int i = 1; i <= str1.length(); i++) {
                for (int j = 1; j <= str2.length(); j++) {
                    distance[i][j] = minimum(
                                    distance[i-1][j] + 1,
                                    distance[i][j-1] + 1,
                                    distance[i-1][j-1]
                + ((str1.charAt(i-1) == str2.charAt(j-1)) ? 0 : 1));
                }
            }
            return distance[str1.length()][str2.length()];
    }
    /**
     * @deprecated
     * @param sSenA
     * @param sSenb
     * @return
     */
    private float compareSentences(String sSenA, String sSenb) {
        String[] a = ommitSmallWords(sSenA.split(" "), 2);
        String[] b = ommitSmallWords(sSenb.split(" "), 2);
        String aa = getStringFromArray(a);
        String bb = getStringFromArray(b);
        int maxLen = aa.length() >= bb.length()
            ? aa.length() : bb.length();
        float distance = 0;
        distance += computeLevenshteinDistance(aa, bb);
        float deriv = (float) distance/maxLen;
        return deriv;
    }
    /**
     * @deprecated
     * @param sSenA
     * @param sSenB
     * @return
     */
    private double nggCompare(String sSenA, String sSenB) {
        DocumentNGramSymWinGraph dgA = new DocumentNGramSymWinGraph();
        dgA.setDataString(sSenA);
        DocumentNGramSymWinGraph dgB = new DocumentNGramSymWinGraph();
        dgA.setDataString(sSenB);
        NGramCachedGraphComparator ngc = new NGramCachedGraphComparator();
        double dRes = ngc.getSimilarityBetween(dgA, dgB).ValueSimilarity;
        return dRes;
    }
    /**
     * @deprecated
     */
    private void compareAllSentences() {
        for (Pair each : getAllTitlePairs()) {
            compareSentences((String) each.getFirst(), (String) each.getSecond());
        }
    }
    /**
     * @deprecated
     * @param sStr
     * @return
     */
    private String getStringFromArray(String[] sStr) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i< sStr.length; i++) {
            sb.append(sStr[i]);
        }
        return sb.toString();
    }
    /**
     * @deprecated
     * @return
     */
    private List<Pair> getAllTitlePairs() {
        List<Article> lsArticleList = this.origArticles;
        // Create a list of Pairs
        List lsArticleTitlePairs = new ArrayList();
        for (int i=0; i < lsArticleList.size()-1; i++) {
            Article aFirst = lsArticleList.get(i); // first feed
            for (int j=i+1; j < lsArticleList.size(); j++) {
                Article aSecond = lsArticleList.get(j); // second feed
                // create feed pair
                if (aFirst.getCategory().equals(aSecond.getCategory()) &&
                        !aFirst.getFeed().equals(aSecond.getFeed())) {
                    Pair<String, String> tmpPair = new Pair(aFirst.getTitle(),
                            aSecond.getTitle());
                    lsArticleTitlePairs.add(tmpPair);
                }
            }
        }
        return lsArticleTitlePairs;
    }

}


