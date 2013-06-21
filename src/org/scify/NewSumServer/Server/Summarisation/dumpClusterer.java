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

import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramSymWinGraph;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;
import gr.demokritos.iit.jinsect.structs.Pair;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.scify.NewSumServer.Server.Storage.IDataStorage;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Topic;
import org.scify.NewSumServer.Server.Utils.Utilities;

/**
 * Human comparison of articles for data acquisition
 * @author George K. <gkiom@scify.org>
 */
public class dumpClusterer  {

    protected final static String               sSeparator = " === ";
    /**
     * The Set containing Topics
     */
    protected HashMap<String, Topic>            hsArticlesPerCluster;
    /**
     * An Article,UUID map
     */
    protected HashMap<Article, String>          hsClusterPerArticle;
    protected List<Article>                     origArticles;
    /** The folder where the Articles will be saved */
    protected String                            ArticlePath;

    protected IDataStorage                      ids;

    protected List<Pair>                        lsfeeds;

    public dumpClusterer(List<Article> lsArticles,
            IDataStorage ids,
            String ArticlePath) {
        origArticles            = new ArrayList(lsArticles);
        hsArticlesPerCluster    = new HashMap<String, Topic>();
        hsClusterPerArticle     = new HashMap<Article, String>();
        this.ids                = ids;
        this.ArticlePath        = ArticlePath;
        this.lsfeeds            = getPairs(lsArticles);
    }
    protected GraphSimilarity compareArticles(Article aOne,
        Article aTwo) {
        DocumentNGramSymWinGraph dgFirstGraph =
                new DocumentNGramSymWinGraph();
        DocumentNGramSymWinGraph dgSecondGraph =
                new DocumentNGramSymWinGraph();
        dgFirstGraph.setDataString(aOne.getText());
        dgSecondGraph.setDataString(aTwo.getText());
        NGramCachedGraphComparator ngc = new NGramCachedGraphComparator();
        return ngc.getSimilarityBetween(dgFirstGraph, dgSecondGraph);
    }
    private List<Pair> getPairs(List<Article> lsArticleList) {
        // Create a list of Pairs
        List lsArticlePairs = new LinkedList();
        System.out.println("Creating Pairs...");
        for (int i=0; i < lsArticleList.size()-1; i++) {
            Article aFirst = lsArticleList.get(i); // first feed
            for (int j=i+1; j < lsArticleList.size(); j++) {
                Article aSecond = lsArticleList.get(j); // second feed
                int One = aFirst.getText().length();
                int Two = aSecond.getText().length();
                // create feed pair
                if (aFirst.getCategory().equals(aSecond.getCategory()) &&
                        !aFirst.getSource().equals(aSecond.getSource()) &&
                        !aFirst.getTitle().equals(aSecond.getTitle())) {
//                        Math.max(One, Two) / Math.min(One, Two) < 2) {
                    Pair<Article, Article> tmpPair = new Pair(aFirst, aSecond);
                    Pair<Article, Article> reverse = new Pair(aSecond, aFirst);
                    if (!lsArticlePairs.contains(tmpPair) && !lsArticlePairs.contains(reverse)) {
                        lsArticlePairs.add(0, tmpPair);
                    }
                }
            }
        }
        Collections.shuffle(lsArticlePairs);
        System.out.println("Created " + lsArticlePairs.size() + " Pairs");
        return lsArticlePairs;
    }
    public void ClusterFeeds(double iValue) {
        int i=0;
        for (Pair each: this.lsfeeds) {
            i += 1;
            Article First = (Article) each.getFirst(); // First Pair(Title, article)
            Article Second= (Article) each.getSecond(); // First Pair(Title, article)
            String sCat = First.getCategory();
            String s1 = First.getTitle() + "\n" + First.getText();
            String s2 = Second.getTitle() + "\n" + Second.getText();
            GraphSimilarity gs = compareArticles(First, Second);
            double NVS =
            (gs.SizeSimilarity == 0.0) ? 0.0 : gs.ValueSimilarity / gs.SizeSimilarity;
            //Create String with feed pairs so that user can evaluate.
            if (NVS < iValue) { continue; }
            else {
                JTextArea text = new JTextArea(s1 + "\n\n" + s2 + "\nNVS: " + Double.toString(NVS));
                text.setLineWrap(true);
                JScrollPane scroll = new JScrollPane(text);
                scroll.setPreferredSize(new Dimension(1000, 500));
                int iTmpIndex = lsfeeds.size() - i;
                String sTitle = "Only " + Integer.toString(iTmpIndex) + " Comparisons Remaining...";
                boolean bMatch = JOptionPane.showConfirmDialog(null, scroll,
                        sTitle, 1) == JOptionPane.YES_OPTION;
                String sMatches = (bMatch == true) ? "Match":"NotMatch";
                // Add Data to The List
                String sTmpLine = Utilities.MakeTmpHumanLine(",",
                        gs.ValueSimilarity, gs.ContainmentSimilarity,
                        gs.SizeSimilarity, NVS, sMatches);
                Utilities.writeClusterCheckFile(sCat, sTmpLine);
            }
        }
    }
}