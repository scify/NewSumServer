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

package org.scify.NewSumServer.Server.Searching;

import gr.demokritos.iit.jinsect.utils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.scify.NewSumServer.Server.Utils.Main;

/**
 * The Class used for searching in an indexed directory.
 * @author George K. <gkiom@scify.org>
 */
public class Searcher {

    private final static Logger LOGGER = Main.getLogger();

    private Analyzer anal;

    /**
     * The map containing the (docId, filename) data
     */
    private HashMap<Integer, String> docFiles = new HashMap<Integer, String>();

    /**
     * Searches the index directory for the specified query.
     * @param fIndexDir The directory where the indexed files are stored
     * @param lLoc The locale that the indexed is created in
     * @param sQuery The search term
     * @param iMaxHits The max number of results to be returned.
     * @return A list of scoredocs which correspond to the search entry
     */
    public List<ScoreDoc> searchIndex(File fIndexDir, Locale lLoc, String sQuery, int iMaxHits) {
        try {
            // Open the Directory of the Indexed Files, using
            // the FSDirectory class
            Directory FSDir = FSDirectory.open(fIndexDir);
            // Create the reader class on the Dir
            IndexReader reader = IndexReader.open(FSDir);
            IndexSearcher searcher = new IndexSearcher(reader);
            String dField = "text"; // Pass this from the Indexer Class?
            // Must Use the Same Analyzer as the index Class, otherwise
            // results will be awkward. So it get's analyzer from Indexer class
            // Create the query Parser on the Field that we want to parse
            if (lLoc.toString().equals("el")) {
                anal = new GreekAnalyzer(Version.LUCENE_36);
            } else if (lLoc.toString().equals("en")) {
                // The standard analyzer
                Analyzer stdAnal = new StandardAnalyzer(Version.LUCENE_36);
                anal = new LimitTokenCountAnalyzer(stdAnal, Integer.MAX_VALUE);
            }
            QueryParser parser =
                    new QueryParser(Version.LUCENE_36, dField, anal);
            try {
                Query q = parser.parse(sQuery);
                // Search the Index with the Query
                TopDocs hits = searcher.search(q, iMaxHits);
                ScoreDoc[] scoreDocs = hits.scoreDocs;
                //debug start
                System.out.println("files found: " + scoreDocs.length);
                //debug end
                // Iterate over the scoredocs
                for (int n = 0; n < scoreDocs.length; n++) {
                    ScoreDoc sd = scoreDocs[n];
                    float score = sd.score;
                    int docId = sd.doc;
                    Document d = searcher.doc(docId);
                    String filename = d.get("file");
//                    System.out.println //debug
//                    (filename+": "+"Score: "+score+" - "+ "DocID: "+ docId);
                    //Save the <docID, filename> data to the map
                    this.docFiles.put(docId, filename);
                }
                // Sort the Docs according to their scores
                List<ScoreDoc> returnList
                        = sortScoreDocs(scoreDocs);
                Collections.reverse(returnList);
                return returnList;
            } catch (ParseException ex) {
                LOGGER.log(Level.SEVERE, "Could not parse query {0}", sQuery);
            } catch (NullPointerException ex) {
                LOGGER.log(Level.WARNING, ex.getMessage());
                return null;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not open Directory {0}", fIndexDir.getPath());
        } return null;
    }
    /**
     * Sorts The ScoreDocs according to their score values.
     * @param scoreDocs The Documents returned by the searcher
     * @return A sorted List of the scoreDocs
     * @throws CorruptIndexException
     * @throws IOException
     */
    private List<ScoreDoc> sortScoreDocs(ScoreDoc[] scoreDocs)
            throws CorruptIndexException, IOException {

        List<ScoreDoc> scoreDocsList = Arrays.asList(scoreDocs);
        Collections.sort(scoreDocsList, new Comparator<ScoreDoc>() {

            @Override
            public int compare(ScoreDoc o1, ScoreDoc o2) {
                return
            (o1.score >= o2.score) ? ((o1.score > o2.score) ? 1:0 ) : -1;
            }
        });
        // debug
//        for (ScoreDoc i : scoreDocsList ) {
//            System.out.println(i.toString() + " :: ");
//        }
        // debug end
        return scoreDocsList;
        }
    /**
     *
     * @return The Map containing the (docId, filename) info
     */
    public HashMap<Integer, String> getDocFiles() {
        return this.docFiles;
    }

}

