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

package org.scify.NewSumServer.Server.Sources;

import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom.Attribute;
import org.jdom.Element;
import org.scify.NewSumServer.Server.SystemFactory.Configuration;
import org.scify.NewSumServer.Server.Storage.IDataStorage;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.UnlabeledArticle;
import org.scify.NewSumServer.Server.Utils.Utilities;

/**
 * Class RssParser parses a URL
 *
 * @author ggianna
 * @author George K. <gkiom@scify.org>
 */
public class RssParser implements ISourceParser {

    
    
    private Configuration               conf;
    /**
     * The Storage module used for various I/O operations
     */
    private IDataStorage                ids;
    /**
     * Filtered Articles counter.
     */
    private static int                  f = 0;
    /**
     * Counter for Dates taken from previous runs.
     */
    private int                         ifromOld = 0;
    /**
     * Counter for new Dates.
     */
    private int                         inewDate = 0;
    /**
     * Counter for articles removed due to their Old Date values
     */
    private int                         iOldDate = 0;
    /**
     * Regular expressions PATTERN separator.
     */
    private static String               sPatternSep = "===";
    /**
     * The label for the articles that should be parsed by the Classifier.
     */
    public static final String          UNCLASSIFIED = "UNCLASSIFIED";
    /**
     * The String that null XML attributes get initialized to (We don't 
     * want null pointer exceptions now, do we?).
     */
    public static final String          NULLATTRIBUTE = "";
    /**
     * The String that is returned if image src cannot be found.
     */
    public static final String          IMAGESRCNOTFOUND = "";
    /**
     * Formats of image links accepted.
     */    
    public static final String[]        IMAGEFORMATS = {".jpg","jpeg",".png",".gif"};
     /**
     * Limit number in days to keep articles. Articles older that this
     * number of days are ignored
     */
    private final long                  iArticleDays;
    /**
     * A List Containing each Item found in the feed in an {@link Article} form.
     */
    private List<Article>               lsItems;
    /**
     * All the Articles fetched by the Parser.
     */
    private List<Article>               lsFullItems = new ArrayList<Article>();

    /**
     * The Logger class.
     */
    protected static final Logger       LOGGER =
            org.scify.NewSumServer.Server.Utils.Main.getLogger();

    protected String                    sLang;
    
    /**
     * The String containing various regular expression
     * patterns for the Articles.
     */
    protected String                    PATTERN;


    /**
     * Constructor of the RssParser Class. Initializes the {@link #lsItems} list
     *
     * @param iDataS The Data Storage module to use
     * @param fServerConfig The configuration file
     */
    public RssParser(IDataStorage iDataS, Configuration config) {
        
        this.ids = iDataS;
        
        this.conf = config;
        
        this.iArticleDays = this.conf.getMaxDaysToFetchForArticles();
        
        this.sLang = this.conf.getCurrentLanguage();
        
        this.PATTERN = readPattern(this.sLang);
        
        this.lsItems = new ArrayList<Article>();
        
//        LOGGER.log(Level.INFO, "Processing pattern {0}", sLang);
        
    }
    /**
     * Processes the feeds from the given URL string and adds them to a List
     * containing an {@link Article} for each item found. Uses Rome.
     *
     * @param urlString the URL string to parse
     * @param sCategory The category that the specified URL is about
     * @throws NetworkException
     * @throws IOException
     */
    public void ProcessFeed(final String urlString, final String sCategory)
            throws FeedException,IOException{
        String title;
        String permalink;
        String description;
        String imageUrl;
        
        URL feedUrl = new URL(urlString);
        SyndFeedInput input = new SyndFeedInput();
        try{
            //for the rss corresponding to the link parse the XML
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            List <SyndEntryImpl> entries=feed.getEntries();
            //for each entry in the XML get the data
            for(SyndEntryImpl entry : entries){
                //we don't like null, map null to NULLATTRIBUTE
                title= entry.getTitle()==null? NULLATTRIBUTE : entry.getTitle();
                permalink=entry.getLink()==null? NULLATTRIBUTE : entry.getLink();
                description=entry.getDescription().getValue()==null? NULLATTRIBUTE
                        : entry.getDescription().getValue();
                imageUrl=getImageUrls(entry);
                //depeding on the type of sCategory 
                if (sCategory.equals(UNCLASSIFIED)) {
                        // Initiate an Unlabeled Article (null Category) with boolean
                        // toWrap = false, so that
                        // it is not accessed by the classification trainer
                        UnlabeledArticle tmpUnArt =
                                new UnlabeledArticle(permalink, title.trim(),
                                description, null, urlString,imageUrl, false);
                        //filter Article text
                        tmpUnArt = (UnlabeledArticle) preProcessArticle(tmpUnArt, 9);
                        // Add the Article found to the list, avoid possible duplicates
                        if (tmpUnArt != null) {
                            Utilities.addItemToList(lsItems, tmpUnArt);
                        }
                        // Otherwise procceed normally with provided category
                    }
                else{
                    // Initiate a new article with toWrap = true,
                    // so that it feeds the classification trainer
                    Article tmpArt =
                            new Article(permalink, title.trim(),
                            description, sCategory, urlString,imageUrl, true);
                    //filter article text
                    tmpArt = preProcessArticle(tmpArt, 10);
                    // Add the Article found to the list, avoid possible duplicates
                    if (tmpArt != null) {
                        Utilities.addItemToList(lsItems, tmpArt);
                    }
                }
            }
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        catch(Exception ex){
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
    }
    
    @Override
    public List<Article> getAllArticles(final HashMap<String, String> Sources) {
        final List<Article> AllArticles = 
                new ArrayList<Article>();
        Collection<String> sCategories = new HashSet<String>(Sources.values());
        for (final String each : sCategories) {
            List<String> Links = new ArrayList<String>(
                    (HashSet<String>) Utilities.getKeysByValue(Sources, each));
            List<Article> lsAr = getAllNewsByCategory(Links, each);
            if (!lsAr.isEmpty()) {
                AllArticles.addAll(lsAr);
            }
        }
        return AllArticles;
    }

    /**
     *
     * @return the Articles of a specific parse
     */
    public List<Article> getArticles() {
        return this.lsItems;
    }

    @Override
    public List<Article> getAllNewsByCategory(List<String> LinksToLoad, String sCategory) {
        LOGGER.log(Level.INFO, "Processing category {0}", sCategory);
        // Create and Initialize the Article List
        List<Article> lsResults = new ArrayList<Article>();
        // Ommit bad URLs from the input list
        LinksToLoad = getValidLinks(LinksToLoad);
        // Iterate the list and getArticles for each Link
        for (String each : LinksToLoad) {
            List<Article> tmpList;
            try {
                tmpList = getNewsFromFeed(each, sCategory);
                if (tmpList != null || !tmpList.isEmpty()) {
                    // Add the Articles to the list
                    lsResults.addAll(tmpList);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            } catch (FeedException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
//        System.err.println("Category " + sCategory + " : "+ lsResults.size());
        this.lsFullItems.addAll(lsResults);//add all to save to file

        return lsResults;
    }

    @Override
    public List<Article> getNewsFromFeed(String sLinkToFeed, String sCategory)
            throws IOException, FeedException{

        List<Article> lsArticles = null;
        try {
            ProcessFeed(sLinkToFeed, sCategory);
            lsArticles = new ArrayList<Article>(getArticles());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        lsItems.clear();//reset list for next feed
        List<Article> lsNewArticles = postProcessArticles(lsArticles); // filter
        //debug
//        System.out.println( lsNewArticles.size() + " articles from feed " + sLinkToFeed);
        //debug
        return lsNewArticles;
    }

    @Override
    public void saveAllArticles() {
        ArrayList co = new ArrayList(this.lsFullItems);
        //debug
        LOGGER.log(Level.INFO, "\nFiltered a total of {0} matches\nAdded "
                + "new Date to {1} articles\nAcquired date from previous run to {2} articles\n"
                + "Removed {3} Articles older than {4} days",
                new Object[]{String.valueOf(f), String.valueOf(inewDate), String.valueOf(ifromOld),
            String.valueOf(iOldDate), String.valueOf(iArticleDays)});
        //debug
        LOGGER.log(Level.INFO, "Saving {0} Articles...", this.lsFullItems.size());
        if (this.ids.objectExists("AllArticles", "feeds")) {
            this.ids.deleteObject("AllArticles", "feeds");
        }
        this.ids.SaveObject(co, "AllArticles", "feeds");
    }

    /**
     * Executes various filtering operations in the article list
     *
     * @param lsArts the Articles upon the operations will be executed
     * @return the list of the Articles filtered, removed unwanted articles.
     */
    private List<Article> postProcessArticles(List<Article> lsArts) {
        //check if list has duplicates
        //Items cannot be Set, because i need index searching to getDate
        //so this conversion is unavoidable

        HashSet<Article> hsArts = new HashSet<Article>(new ArrayList(lsArts));
        if (hsArts.size() != lsArts.size()) {
            LOGGER.log(Level.INFO,
                    "{0} Duplicates omitted...", (lsArts.size() - hsArts.size()));
        }
        ArrayList<Article> clear = new ArrayList<Article>(hsArts);
        int Initial = clear.size();
        // Specific filtering
        Iterator it = clear.iterator();
        if (sLang.equals("EN")) { //english articles
            while (it.hasNext()) {
                Article each = (Article) it.next();
                if (each.getFeed().contains("bbci")) {
                    if (each.getText().matches("\\A[Pp]rovides an[d]* [oO]verview(.|\\\n)*")) {
                        it.remove(); //BBC europe
                    }
                }
                if (each.getFeed().contains("euronews")) {
                    if (each.getText().
                            matches("\\A[Aa]t the [Ll]e[Ww]eb\\s*\\d+\\s*conference in\\s*\\w+(.|\\\n)*")) {
                        it.remove(); //Euronews Leweb
                    }
                }
                if (each.getFeed().contains("scientist")) {
                    if (each.getTitle().matches("(?is)\\Aimage\\s*of\\s*[the ]*day.*")) {
                        it.remove(); // remove articles 'Image of the Day from scientist'
                        //FIXME maybe need to accept again when we have images
                    }
                }
            }
        } else if (sLang.equals("EL")) { // greek pattern
            while (it.hasNext()) {
                Article each = (Article) it.next();
                if (each.getFeed().contains("epikaira")) { // remove [category] tags from titles for these feeds
                    each.setTitle(filterTitle(each.getTitle(), "\\A\\[\\S+[-]*\\S+\\]"));
                } else if (each.getFeed().contains("enikos")) { // remove 'ΒΙΝΤΕΟ' from some titles
                    each.setTitle(filterTitle(each.getTitle(), "(?iu)\\A[BΒ][ΙI][ΝND][ΤT]*[ΕE][ΟO]\\s*-\\s*"));
                }
            }
        }
        // add date to articles that do not have one, avoiding
        // articles older than iArticleDays from now
        List<Article> lsFinalArticles = addDateToArticles(clear);

        int Final = lsFinalArticles.size();
        if (Final < Initial) {
            LOGGER.log(Level.INFO, "Removed {0} Articles", (Initial - Final));
        }
        return lsFinalArticles;
    }

    /**
     * Filters the given list removing invalid URL links
     *
     * @param LinksToLoad The List containing all the links
     * @return a list with the invalid URL links omitted
     */
    private List<String> getValidLinks(List<String> LinksToLoad) {
        List<String> ValidLinks = new ArrayList<String>();
        ListIterator<String> iter = LinksToLoad.listIterator();
        while (iter.hasNext()) {
            String nextLink = iter.next().trim();
            if (Utilities.ValidURL(nextLink)) {
                ValidLinks.add(nextLink);
            }
        }
        return ValidLinks;
    }

    /**
     * filters a given text, removing all ocurrencies of unwanted text
     *
     * @param des The text to check
     * @param pattern The SPAM text PATTERN that is unwanted
     * @return A text with the unwanted part omitted
     */
    private String filter(String des, String pattern) {
        String[] aPat = pattern.split(sPatternSep);
        for (String regex : aPat) {
            Pattern MyPattern = Pattern.compile(regex);
            Matcher MatchFound = MyPattern.matcher(des);
            if (MatchFound.find()) {
                f++;
                des = des.replaceAll(regex, "");
            }
        }
        return des;
    }

    /**
     * Filters a specific text from a title.
     *
     * @param sTitle the title to process
     * @param PATTERN the PATTERN to apply
     * @return the title with the PATTERN text filtered
     */
    private String filterTitle(String sTitle, String pattern) {
        String[] aPat = pattern.split(sPatternSep);
        for (String regex : aPat) {
            Pattern MyPattern = Pattern.compile(regex);
            Matcher MatchFound = MyPattern.matcher(sTitle);
            if (MatchFound.find()) {
                f++;
                sTitle = sTitle.replaceAll(regex, "");
            }
        }
        return sTitle;
    }

    /**
     *
     * @param lang the language of the txt PATTERN file
     * @return all the Regular Expression patterns contained in the PATTERN
     * file, splitted by a separator
     */
    private String readPattern(String lang) {
        String sRes = null;
        String sToolPath = this.conf.getToolPath();
        String sPath = sToolPath + "regexPat_" + lang + ".txt";
        File fFile = new File(sPath);
        if (fFile.exists()) {
            if (fFile.canRead()) {
                sRes = Utilities.readFromFile(sPath, sPatternSep);
            } else {
                LOGGER.log(Level.SEVERE, "Could not read file {0}", fFile.getPath());
            }
        } else {
            LOGGER.log(Level.SEVERE, "File {0} does not Exist\n... Will "
                    + "continue without Article pre-filtering.", fFile.getPath());
        }
        return sRes;
    }

    /**
     * Does some preprocessing operations in the article provided, such as
     * clearing garbage data from Article description, etc
     *
     * @param aArt the article to be processed
     * @param minWords The minimum number of words to accept
     * @return a cleaner article, or null if Article does not fulfill certain
     * criteria
     */
    private Article preProcessArticle(Article aArt, int minWords) {

        //filter Article text
        if (aArt.getText() != null && !aArt.getText().trim().isEmpty()) {
            if (PATTERN != null) {
                String sClearDescription = filter(aArt.getText(), PATTERN);
                // Accept article only if description length has more than 9 words
                if (sClearDescription.
                        replaceAll("[.,;:!@]", " ")
                            .split("\\s+").length > minWords) {
                    aArt.setText(sClearDescription);
                    return aArt;
                }
            } else { // no regex filtering pattern specified
                
                if (aArt.getText().trim().
                        replaceAll("[.,;:!@]", " ").
                            split("\\s+").length > minWords) {
                    
                    return aArt;
                    
                }
                
            }
        }
        return null;
    }

    /**
     * Adds a date to each Article in the list passed
     *
     * @param lsArts The Articles to process
     * @return the list of articles, with articles older than
     * {@link #iArticleDays} days removed.
     */
    private List<Article> addDateToArticles(List<Article> lsArts) {
        // get Current date in millis
        Calendar now = Calendar.getInstance();
        long iDays;
        ArrayList<Article> oldArticles;
        try {
            // get the category-daystokeep map from file to memory
            HashMap<String, Integer> hsCategoryDays =
                    Utilities.readDaysPerCategoryFile(this.conf.getCategoriesDaysFileLocation());
            // load articles from previous run
            oldArticles = (ArrayList<Article>) this.ids.loadObject("AllArticles", "feeds");
            // for each current article
            for (ListIterator<Article> arit = lsArts.listIterator(); arit.hasNext();) {
                Article each = arit.next();
                // if it has not acquired a date yet
                if (each.getDate() == null) {
                    // iterate the articles from previous run
                    Iterator it = oldArticles.iterator();
                    while (it.hasNext()) {
                        Article old = (Article) it.next();
                        // if the same article
                        if (each.getText().hashCode() == old.getText().hashCode()) {
                            // Set Current Article's Date from the previous run
                            if (old.getDate() != null) {
                                each.setDate(old.getDate());
                                ifromOld++;     // update counter
                                break; // continue to the next current article
                            } else {
                                // set Date as Now
                                each.setDate(new Date());
                                inewDate++;
                                break; // continue to the next article
                            }
                        }
                    }
                    // Remove all articles from date older than now - iArticleDays
                } else {

                    // check if article category is in the Categories - Days Map.
                    // and if so, assing days to keep from this file, not from global
                    if (hsCategoryDays.containsKey(each.getCategory())) {
                        iDays = hsCategoryDays.get(each.getCategory());
//                        System.err.println("Found DAYS LIMIT FOR CATEGORY " + each.getCategory() );
//                        System.err.println("DAYS " + iDays);
                    } else {
                        iDays = iArticleDays;
                    }
                    // if after the now - iArticleDays Date, accept, else, ignore
                    if ((now.getTimeInMillis() - each.getDate().getTimeInMillis())
                            > (iDays * 1000 * 60 * 60 * 24)) {

                        iOldDate++;
                        arit.remove();
                    }
                }
            }
            // if old articles cannot be loaded to memory
            // (1st run or problem with file)
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            for (Article each : lsArts) {
                // if current article does not have date
                if (each.getDate() == null) {
                    // add date as now
                    each.setDate(new Date());
                    inewDate++;
                }
            }
            // process possible missed articles
        } finally {
            // again
            for (Article each : lsArts) {
                if (each.getDate() == null) { // if null date and not in previous run
                    each.setDate(new Date());
                    inewDate++;
                }
            }
            return lsArts;
        }
    }

    private String getImageUrls(SyndEntryImpl entry){
        Attribute url;
        String target;
        //attempt to get image url from "url"
        List<Element> foreignMarkups = (List<Element>) entry.getForeignMarkup();
        for(Element each: foreignMarkups){
            url=each.getAttribute("url");
            if(url!=null){
                target=url.getValue();
                if(isImage(target)){
                    return target;
                }
            }
        }
        //attempt to get image url from  Enclosure
        List<SyndEnclosure> enclosures = entry.getEnclosures();
        for(SyndEnclosure each: enclosures){
            if(each.getType().contains("image")){
               return each.getUrl();             
            }
        }
        return IMAGESRCNOTFOUND;
    }
    
    private boolean isImage(String url){
        for(int i=0;i<IMAGEFORMATS.length;i++){
            if(url.endsWith(IMAGEFORMATS[i])){
                return true;
            }
        }
        return false;
    }
}

