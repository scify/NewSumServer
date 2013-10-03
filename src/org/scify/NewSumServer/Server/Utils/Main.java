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
//Switch for english sources
//-PathToSources=./data/Sources/v1.0.RSSSourcesEN.txt


import gr.demokritos.iit.conceptualIndex.structs.Distribution;
import gr.demokritos.iit.jinsect.storage.INSECTDB;
import gr.demokritos.iit.jinsect.storage.INSECTFileDB;
import gr.demokritos.iit.jinsect.utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Pattern;
import org.apache.commons.feedparser.FeedParserException;
import org.apache.commons.feedparser.network.NetworkException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.scify.NewSumServer.Server.Comms.Communicator;
import org.scify.NewSumServer.Server.MachineLearning.classificationModule;
import org.scify.NewSumServer.Server.Searching.Indexer;
import org.scify.NewSumServer.Server.Sources.ISourceParser;
import org.scify.NewSumServer.Server.Sources.RSSSources;
import org.scify.NewSumServer.Server.Sources.RssParser;
import org.scify.NewSumServer.Server.Storage.IDataStorage;
import org.scify.NewSumServer.Server.Storage.InsectFileIO;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Sentence;
import org.scify.NewSumServer.Server.Structures.Topic;
import org.scify.NewSumServer.Server.Summarisation.ArticleClusterer;
import org.scify.NewSumServer.Server.Summarisation.RedundancyRemover;
import org.scify.NewSumServer.Server.Summarisation.Summariser;
import static org.scify.NewSumServer.Server.Utils.Main.UserDir;

/**
 *
 * @author George K. <gkiom@scify.org>
 */
public class Main {

    @SuppressWarnings("NonConstantLogger")
    static Logger       LOGGER = Logger.getLogger(Main.class.getName());
    static final String CustomUserDir = System.getProperty("user.dir");
    static final String UserDir = ".";
    static final String fileSep = System.getProperty("file.separator");
    /**
     * The Path to the Log file
     */
    static String       sLogFile =
            UserDir + fileSep + "data" + fileSep + "Logger" + fileSep + "NewSumServerLog.txt";
    /**
     * The folder where the FileINSECTDB saves
     */
    public static String sBaseDir =
            UserDir + fileSep + "data" + fileSep + "BaseDir" + fileSep;
    /**
     * The file containing the RSS Sources
     */
    public static String sPathToSources =
            UserDir + fileSep + "data" + fileSep + "Sources" + fileSep + "v1.0.RSSSourcesGR.txt";
    /**
     * The folder where the Indexer Class saves it's data
     */
    public static String sindexPath =
            UserDir + fileSep + "data" + fileSep + "Indexed" + fileSep;
    /**
     * The Folder where the Summariser class stores it's summaries
     */
    public static String sSummaryPath =
            UserDir + fileSep + "data" + fileSep + "Summaries" + fileSep;
    /**
     * The folder where the Clusterer saves the Articles
     */
    public static String sArticlePath =
            UserDir + fileSep + "data" + fileSep + "Articles" + fileSep;
    /**
     * Folder for misc tool files
     */
    public static String sToolPath =
            UserDir + fileSep + "data" + fileSep + "Tools" + fileSep;
    /**
     * The maximum number of sentences returned by the summarizer
     */
    public static int iOutputSize = 10;
    /**
     * Default value for the Accepted Article Date until now. If an article
     * has an older Date in days than this number, it will not be accepted.
     * Use it on command line with 'ArticleMaxDays'
     */
    public static long iArticleDays = 5L;

    /**
     * The path where the classification module stores data
     */
    public static String sClassModPath = UserDir + fileSep + "ClassificationServerModule/";

    public static boolean bUseInputDirData = false;
    /**
     * True if a run of a certain category must be applied. Defaults to false
     * (all categories loaded)
     */
    public static boolean bDebugRun = false;
    /**
     * Path to the File that holds the data [Category-days]/line, used by Utilities
     * class to save
     */
    public static final String  sPathToCatsPerDaysFile =
        UserDir + fileSep + "data" + fileSep + "Sources" + fileSep + "DaysPerCategory.txt";
    //should not be used in final version, only for testing
    public static String sSep = " *** ";

    /**
     * The plain text summary storage folder (debug)
     */
    protected static String sTxtSumPath =
            CustomUserDir + fileSep + "data" + fileSep + "txtSummaries" + fileSep;

    public static Integer threshold;

    protected static classificationModule clm;

    //TODO Currently, the ServerConfig.txt that Main class creates is located
    //at ./data/BaseDir. So if User changes this Dir, the freeService won't work
    public static void main(String[] args)
            throws FeedParserException, NetworkException, IOException {
        //initialize logger
        Handler h;
        try {
            h = new FileHandler(sLogFile);
            SimpleFormatter f = new SimpleFormatter();
            h.setFormatter(f);
            LOGGER.addHandler(h);
            LOGGER.setLevel(Level.FINE);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SecurityException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        //program info
        System.out.print("NewSumServer Switches:\n\n"
                + "-BaseDir: The full path to the folder where the storage module stores data\n"
                + "-PathToSources: The file named RSSSources.txt with it's full path\n"
                + "\t(e.g. /home/pathtosources/RSSSources.txt)\n"
                + "-indexPath: The full path to the folder where the Indexer Class stores data\n"
                + "-SummaryPath: The full path to the folder where the Summarisation package stores data\n"
                + "-ArticlePath: The full path to the folder where the "
                + "Summarisation package stores the Clustered Articles\n"
                + "-ToolPath: The full path to the folder for misc Tools\n"
                + "-iOutputSize: The max number of Sentences the Summariser prints\n"
                + "-ArticleMaxDays: The Number of Max Days to Accept an article (until now)\n"
                + "-useInputDirData: true or false, defaults to false\n"
                + "-DebugRun: true if you want to run with category switching (for quicker runs)\n\n"
                + "Example Usage: java -jar NewSumServer.jar -BaseDir=./data/Dir -iOutputSize=50\n\n");
        //Parse and check Command Line arguments
        parseCommandLine(args);
        //Write Configuration file so that NewSumFreeService reads statics
        writeConfigFile();
        // check if splitterTraining file was changed since the previous run
        if (SplitterTrainingFileChanged()) {
            // if so, delete the Model file, in order to get recreated with the new data
            File sDat = new File (sToolPath + "splitModel.dat"); 
            try {
                sDat.delete();
                LOGGER.log(Level.INFO, "deleted {0} cause splitterTrainer was updated", sDat.toString());
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Could not delete {0}, although file Was Changed. "
                        + "Please do it manually", sDat.toString());
            }
        }
        //init data storage
        IDataStorage ids = new InsectFileIO(sBaseDir);
//        justWaitABit(50000);
        
        //init rssSources 
        RSSSources r = new RSSSources(sPathToSources);
        // initialize sources: read the sources file
        r.initialize(ids);
        //get the sources
        HashMap<String, String> Sources = r.getRssLinks();//link,category
        //get categories
        Collection<String> sCategories = r.getCategories(); // TODO: Ignore UNCLASSIFIED CATEGORY
        ArrayList<String> lCategories = new ArrayList<String>(sCategories);
        //init rssparser
        ISourceParser isp = new RssParser(ids, iArticleDays);
        //DEBUG LINES //get user input
        List al = new ArrayList(sCategories);
        ArrayList<String> subSources = null;
        ArrayList<Article> Articles = new ArrayList<Article>();
        String sCurCateg = "0";
        if (bDebugRun) { // if only one category needed (quick run)
            System.out.println("Choose Category by number: \nIf -1, all categories are loaded");
            for (int i = 0; i < lCategories.size(); i++) {
                System.out.println(String.valueOf(i) + ": " + lCategories.get(i));
            }
            Scanner user_input = new Scanner(System.in);
            sCurCateg = user_input.next();
            /////////////

            if (Integer.valueOf(sCurCateg) != -1) {
                subSources = new ArrayList<String>(
                        (HashSet<String>) Utilities.getKeysByValue(Sources,
                        (String) al.get(Integer.valueOf(sCurCateg))));
                //accept all articles from each category
                Articles = (ArrayList<Article>) isp.getAllNewsByCategory(subSources,
                        (String) al.get(Integer.valueOf(sCurCateg)));

            } else if (Integer.valueOf(sCurCateg) == -1) {

                //get all articles
                Articles = (ArrayList<Article>) isp.getAllArticles(Sources);
            }
        } else { //if all categories by default (no user choosing - normal mode)
            Articles = (ArrayList<Article>) isp.getAllArticles(Sources);
        }
        // check for spam sentences
        Utilities.checkForPossibleSpam(Articles);
        //Save Article List to Drive, so that the clusterer loads it
        isp.saveAllArticles(); //Name: "AllArticles", Category: "feeds"
//        ArticleClusterer ac = new ArticleClusterer(subArticles, ids, sArticlePath);
        //get least occurencies of articles
//        threshold = Utilities.getLeastOccurencies(Articles);
//        //Train Classification Module
//        clm = new classificationModule();
//
//
//        //initialize Distribution category set
//        Distribution<String> dArticleCategory = new Distribution<String>();
//
//        for (int i = 0; i < Articles.size(); i++) {
//            if (Articles.get(i).getToWrap()) {
//                boolean mergeGraph = true;
//                //increase Distribution set 1.0
//                dArticleCategory.increaseValue(Articles.get(i).getCategory(), 1.0);
//                //check threshold
//                double dInstanceCount = dArticleCategory.getValue(Articles.get(i).getCategory());
//                if (dInstanceCount < threshold) {
//
//                    //check mergeGraph threshold --> threshold/2  turn mergeGraph from true to false
//                    if (dInstanceCount > (threshold / 2)) {
//                        mergeGraph = false;
//                    }
//                    clm.feedClassifier(Articles.get(i).getCategory(), Articles.get(i).getText(), mergeGraph);
//                }
//
//            }
//        }
        // Initialize Clusterer
        ArticleClusterer ac = new ArticleClusterer(
                (ArrayList<Article>) ids.loadObject("AllArticles", "feeds"), ids, sArticlePath);
        // Perform clustering calculations
        ac.calculateClusters();

        //specify the locale for the indexer
        Locale loc = sPathToSources.endsWith("GR.txt") ? new Locale("el")
                : new Locale("en");
        // Create a new indexer
        Indexer ind = new Indexer(sArticlePath, sindexPath, loc);
        // Create the Index
        try {
            ind.createIndex();
        } catch (CorruptIndexException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (LockObtainFailedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        // Init the summarizer and obtain summaries
        INSECTDB idb = new INSECTFileDB("", sSummaryPath);
        Summariser sum = new Summariser(new HashSet<Topic>(
                ac.getArticlesPerCluster().values()), idb);
        // Perform summarization for all clusters
        Map<String, List<Sentence>> AllSummaries;
        AllSummaries = sum.getSummaries();

        if (bDebugRun) {

            // DEBUG LINES
            // Delete files in "data/txtSummaries" and write all the summaries extracted
            File f = new File(sTxtSumPath);
            if (f.exists()) {
                for (File k : f.listFiles()) {
                    k.delete();
                }
            }
            for (Map.Entry mp : AllSummaries.entrySet()) {
                String sUID = (String) mp.getKey();
                List<Sentence> lsSen = (List<Sentence>) mp.getValue();
                if (getNumberOfSources(lsSen) > 1) {
                    writeSummaryToFile(lsSen, sUID, ac.getArticlesPerCluster());
                }
            }

            // debug communicator
            Communicator cm = new Communicator(ids, ac, sum, ind);
            int bb = Integer.valueOf(sCurCateg);
            if (bb == -1) { bb = 0; }
            // print summary from communicator
            int iSummarizedClusterCnt = 0;
            for (Topic tTopic : ac.getArticlesPerCluster().values()) {
                if (tTopic.size() > 1) {
                    System.out.println("Printing summary for topic: " + tTopic.getTitle());
                    String[] eachSnippet = cm.getSummary(tTopic.getID(), "All").split(cm.getFirstLevelSeparator());
                    int iAllTmpSourcesCount = eachSnippet[0].split(cm.getSecondLevelSeparator()).length;
                    System.out.println("With Summary Sources: " + iAllTmpSourcesCount);
                    for (int i = 1; i<eachSnippet.length; i++) {
                        String[] eachSent = eachSnippet[i].split(cm.getSecondLevelSeparator());
                        System.out.println(eachSent[0]);
                        System.out.println("-----------------------------------");
                        iSummarizedClusterCnt++;
                    }
                    System.out.println("===========================");
                }
            }



        }
        //DEBUG LINES //get user input
//        System.out.println("Enter Search String\n");
//        Scanner imp = new Scanner(System.in);
//        String term = imp.next();
//

//        String sTop = cm.getTopicIDsByKeyword(ind, term, "All");
//        System.out.println(sTop);
//        System.out.println(cm.getTopicTitlesByIDs(sTop));

        // last debug
//        String sUserSources = "http://rss.in.gr/feed/news/culture/" +
//            "http://www.tovima.gr/feed/culture/" +
//            "http://www.naftemporiki.gr/rssFeed?mode=section&id=6&atype=story";
//        System.out.println(cm.getTopics(sUserSources, (String) al.get(bb)));
//        System.out.println(cm.getTopics("All", (String) al.get(bb)));
        // last debug

//        System.out.println("Found a total of " + iSummarizedClusterCnt + " summaries"
//                + " from more than one texts.");
//        System.out.println(cm.getTopicIDs("All", (String) al.get(bb)));
//        System.out.println("===============printing topic titles");
//        System.out.println(cm.getTopicTitles("All", (String) al.get(bb)));
//        System.out.println("===============ending printing topic titles");
//        String sUserSources = "http://rss.in.gr/feed/news/world/;;;"
//                + "http://www.naftemporiki.gr/news/static/rss/news_pol_pol-world.xml;;;"
//                + "http://ws.kathimerini.gr/xml_files/worldnews.xml;;;"
//                + "http://feeds.feedburner.com/skai/aqOL?format=xml";
//        System.out.println(cm.getTopicIDs(sUserSources, (String) al.get(bb)));
//        System.out.println(cm.getTopicTitles(sUserSources, (String) al.get(bb)));

//        int counter = 0;
//        for (Topic tTopic : ac.getArticlesPerCluster().values()) {
//            System.out.println("====================");
//            System.out.println(cm.getSummary(tTopic.getID(), sUserSources));
//            counter ++;
//            if (counter == 3) {
//                break;
//            }
//        }

    }

    private static void parseCommandLine(String[] args) {
        // Parse command line
        Hashtable hSwitches;
        hSwitches = utils.parseCommandLineSwitches(args);

        sPathToSources = utils.getSwitch(hSwitches, "PathToSources", sPathToSources);
        sBaseDir = addSuffix(utils.getSwitch(hSwitches, "BaseDir", sBaseDir));
        sindexPath = addSuffix(utils.getSwitch(hSwitches, "indexPath", sindexPath));
        sSummaryPath = addSuffix(utils.getSwitch(hSwitches, "SummaryPath", sSummaryPath));
        sArticlePath = addSuffix(utils.getSwitch(hSwitches, "ArticlePath", sArticlePath));
        sToolPath = addSuffix(utils.getSwitch(hSwitches, "ToolPath", sToolPath));
        iOutputSize = Integer.valueOf(utils.getSwitch(hSwitches, "outputSize",
                String.valueOf(iOutputSize))).intValue();
        iArticleDays = Integer.valueOf(utils.getSwitch(hSwitches, "ArticleMaxDays",
                String.valueOf(iArticleDays))).intValue();
        bUseInputDirData = Boolean.valueOf(utils.getSwitch(hSwitches,
                "useInputDirData", Boolean.FALSE.toString()));
        bDebugRun = Boolean.valueOf(utils.getSwitch(hSwitches,
                "DebugRun", Boolean.FALSE.toString()));
        //checking user input
        checkPaths(hSwitches.values().toArray()); // Check Switches
    }

    private static String addSuffix(String in) {
        if (!in.endsWith(fileSep)) {
            in += fileSep;
        }
        return in;
    }

    private static void checkPaths(Object[] args) {
        Iterator iIter = Arrays.asList(args).iterator();
        while (iIter.hasNext()) {
            String sCurSwitch = (String) iIter.next();
            if (!sCurSwitch.endsWith(".txt") && !sCurSwitch.equals("true") && !sCurSwitch.equals("false")
                    && !Pattern.matches("[1-9]+", sCurSwitch)) {
                File fsw = new File(sCurSwitch);
                if (!fsw.isDirectory()) {
                    LOGGER.log(Level.WARNING, "Error: {0} is not a directory", fsw);
                    LOGGER.log(Level.INFO, "Trying to create Dir...");
                    boolean happy = (new File(sCurSwitch)).mkdir();
                    if (happy) {
                        LOGGER.log(Level.INFO, "Directory {0} created", sCurSwitch);
                    } else {
                        LOGGER.log(Level.WARNING, "Could not create Dir...");
                    }
                }
            }
        }
        // check if file for categories exist, else create
        File sCatsDays = new File(sPathToCatsPerDaysFile);
        // delete the categories / days to keep file, so that new data
        // may be appended to it. 
        if (sCatsDays.exists()) {
            sCatsDays.delete();
        }
        // check if can read Sources File, else abort
        File SourcesFile = new File(sPathToSources);
        if (!SourcesFile.exists() || !SourcesFile.canRead()) {
            LOGGER.log(Level.SEVERE, "{0} does not exist\nAborting...", SourcesFile);
            System.exit(0);
        }
        String[] paths = {sBaseDir, sArticlePath, sSummaryPath, sToolPath, sindexPath};
        for (String eachPath : paths) {
            File chkPath = new File(eachPath);
            if (!chkPath.isDirectory() || !chkPath.exists()) {
                LOGGER.log(Level.SEVERE, "{0} is not a directory\nAborting...", chkPath);
                System.exit(0);
            }
        }
    }
    private static boolean SplitterTrainingFileChanged() {
        boolean bChanged = false;
        String sStartsWith = "FileSize"; String sLocalSep = "=";
        // get value of current size
        File fSentenceTrainer = new File("./src/org/scify/NewSumServer/Server/Summarisation/SentenceSplitterTraining.txt");
        long lCurrentSize = fSentenceTrainer.length();
        // read file that has stored data about previous file size
        File fFileSize = new File(sToolPath + "FileSize.txt");
        if (fFileSize.exists()) {
            long lOldFileSize;
            // read file and get the file size from the previous run
            if (fFileSize.canRead()) {
                FileInputStream fstream;
                try {
                    fstream = new FileInputStream(fFileSize);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    // Get the object of DataInputStream
                    String sLine;
                    while ((sLine = br.readLine()) != null) {
                        if (sLine.startsWith(sStartsWith)) {
                            // get value of previous file size
                            lOldFileSize = Long.valueOf(sLine.split("=")[1].trim());
                            // check if different values
                            bChanged = (lCurrentSize != lOldFileSize);
                            // overwrite data with new value
                            BufferedWriter bw = null;
                            bw = new BufferedWriter(new FileWriter(fFileSize, false));
                            // update data to file
                            bw.append(sStartsWith).append(sLocalSep);
                            bw.append(String.valueOf(lCurrentSize));
                            bw.close();
                        }
                    }
                    in.close();
                } catch (FileNotFoundException ex) {
                    LOGGER.log(Level.SEVERE, "File Not Found (Exception): {0}", fFileSize.toString());
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex.getMessage());
                }

            } else {
                LOGGER.log(Level.SEVERE, "Error: Cannot read from file: {0}", fFileSize.toString());
            }
        } else {
            try {
                // create it for the first time
                fFileSize.createNewFile();
                BufferedWriter bw = null;
                bw = new BufferedWriter(new FileWriter(fFileSize, false));
                // write initial data to file
                bw.append(sStartsWith).append(sLocalSep);
                bw.append(String.valueOf(lCurrentSize));
                bw.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Could not create file {0}", fFileSize.toString());
            }
        }
        return bChanged;
    }
    
    private static void writeConfigFile() {
        HashMap switches = new HashMap<String, String>();
        switches.put("BaseDir", sBaseDir);
        switches.put("PathToSources", sPathToSources);
        switches.put("indexPath", sindexPath);
        switches.put("SummaryPath", sSummaryPath);
        switches.put("ArticlePath", sArticlePath);
        switches.put("ToolPath", sToolPath);
        switches.put("sCatsDaysFile", sPathToCatsPerDaysFile);
        switches.put("useInputDirData", String.valueOf(bUseInputDirData));
        switches.put("ArticleMaxDays", String.valueOf(iArticleDays));
        switches.put("DebugRun", String.valueOf(bDebugRun));
//        switches.put("SplitterTraining", String.valueOf(lFileSize));
        //write Config File, so that FreeService reads values from it
        File fConfig = new File(sBaseDir + "ServerConfig.txt");
        if (fConfig.exists()) {
            fConfig.delete();
        }
        try {
            fConfig.createNewFile();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        if (fConfig.canWrite()) {
            try {
                BufferedWriter bw = null;
                bw = new BufferedWriter(new FileWriter(fConfig, true));
                Iterator Iter = switches.entrySet().iterator();
                while (Iter.hasNext()) {
                    Map.Entry tmpS = (Map.Entry) Iter.next();
                    bw.append((String) tmpS.getKey() + "=" + (String) tmpS.getValue());
                    bw.append("\n");
                }
                bw.close();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } else {
            try {
                throw new IOException("Cannot write to file " + fConfig.getName());
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    /**
     * @return The Logger that is used
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     *
     * @return The full path where the sources file is stored
     */
    public static String getPathToSources() {
        return sPathToSources;
    }
    ///DEBUGGING function

    private static void writeSummaryToFile(List<Sentence> lsSen, String sCluster, HashMap<String, Topic> hsTopics)
            throws IOException {
        File f = new File(sTxtSumPath);
        if (!f.exists()) {
            System.err.println("FILE " + sTxtSumPath +" DOES NOT EXIST");
            if (!f.mkdirs()) {
                System.err.println("FILE " + sTxtSumPath +" Could not be created");
            }
        }
        if (f.isDirectory()) {
            f.setWritable(true);
        }
        String sFullFileName =
                sTxtSumPath + sCluster + ".txt";
        File fFile = new File(sFullFileName);
        fFile.createNewFile();
        BufferedWriter bw = new BufferedWriter(new FileWriter(fFile, false));
        bw.write("ClusterID" + sSep + sCluster);
        bw.newLine();
        bw.write("Title: " + hsTopics.get(sCluster).getTitle());
        bw.write(("\n========================================\n"));
        StringBuilder sb = new StringBuilder();
        ListIterator<Sentence> li = lsSen.listIterator();
        while (li.hasNext()) {
            Sentence sCur = li.next();
            if (sCur.getSnippet().split("[;,. ]").length < 5) {
                li.remove();
            }
        }
        lsSen = new RedundancyRemover().removeRedundantSentences(lsSen);
        for (Sentence each : lsSen) {
            sb.append(each.getSnippet());
            sb.append("\n========================================\n");
//            sb.append(each.getLinkToSource());
//            sb.append("\n");
//            sb.append(each.getFeed());
//            sb.append("\n");
        }
        bw.write(sb.toString());
        bw.close();
    }

    private static int getNumberOfSources(List<Sentence> lsSen) {
        HashSet<String> hsSources = new HashSet<String>();
        for (Sentence each : lsSen) {
            hsSources.add(each.getLinkToSource());
        }
        return hsSources.size();
    }

    /**
     *
     * @return The classification module instance
     */
    public static classificationModule getClassificationModule() {
        return clm;
    }

    private static void justWaitABit(int seconds) {
        long l = seconds *1000;
        try {
            Thread.sleep(l);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
