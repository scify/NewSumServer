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
import gr.demokritos.iit.jinsect.utils;
import java.util.*;
import org.scify.NewSumServer.Server.Structures.Sentence;

/**
 * Removes redundancy from a summary.
 * @author ggianna
 */
public class RedundancyRemover {

    /**
     * Removes redundant sentences from a given list of sentences (supposed
     * to constitute a summary).
     * @param lToCleanUp The list to cleanup.
     * @return A list containing only non-redundant sentences.
     */
    public List<Sentence> removeRedundantSentences(final List<Sentence> lToCleanUp) {
        LinkedList<Sentence> llRes = new LinkedList<Sentence>(lToCleanUp);
        // Order by size DESCENDING
        Collections.sort(llRes, new Comparator<Sentence>() {

            @Override
            public int compare(Sentence t, Sentence t1) {
                int iRes = t1.getSnippet().length() - t.getSnippet().length();
                if (iRes == 0)
                    return t.getSnippet().compareTo(t1.getSnippet());
                return iRes;
            }
        });

        // For every sentence
        ListIterator<Sentence> isCur = llRes.listIterator();
        NGramCachedGraphComparator ngc = new NGramCachedGraphComparator();

        while (isCur.hasNext()) {
            // Init changed flag
            boolean bChanged = false;
            // Create graph
            DocumentNGramSymWinGraph dg = new DocumentNGramSymWinGraph();
            dg.setDataString(isCur.next().getSnippet());
            // For every sentence following
            ListIterator<Sentence> isSecondCur = llRes.listIterator(isCur.nextIndex());
            if (isSecondCur.hasNext()) {
                while (isSecondCur.hasNext())
                {
                    // Create graph
                    DocumentNGramSymWinGraph dg2 = new DocumentNGramSymWinGraph();
                    dg2.setDataString(isSecondCur.next().getSnippet());
                    // If NVS over threshold, consider redundant
                    GraphSimilarity gs = ngc.getSimilarityBetween(dg, dg2);
                    double dNVS = gs.SizeSimilarity == 0.0 ? 0.0 :
                            gs.ValueSimilarity / gs.SizeSimilarity;
                    // If very similar
                    if (dNVS > 0.3) {
                        // Remove shorter sentence
                        isSecondCur.remove();
                        bChanged = true;
                    }
                }
            }
            // On change
            if (bChanged)
                // If more things to check, reset original list iterator to before removal
                if (isCur.nextIndex() < llRes.size())
                    // Continue from the last valid item
                    isCur = llRes.listIterator(isCur.nextIndex());
                else
                    // Set the iterator to the last available object
                    isCur  = llRes.listIterator(llRes.size() -1);
        }

        // Reorder remaining sentences according to original order
        Collections.sort(llRes, new Comparator<Sentence> () {

            @Override
            public int compare(Sentence t, Sentence t1) {
                return lToCleanUp.indexOf(t) - lToCleanUp.indexOf(t1);
            }
        });

        return llRes;
    }

//    public static void main(String[] sArgs) {
//        List<Sentence> al = new ArrayList<Sentence>();
//        for (int iCnt = 0; iCnt < 2; iCnt++) {
//            Sentence s1 = new Sentence(utils.getNormalString(), "", "");
//            if (iCnt == 0)
//                s1 = new Sentence("======", "", "");
//            al.add(s1);
////            if (new Random().nextBoolean())
//                al.add(s1);
//        }
//        al.add(new Sentence("Testing", "", ""));
//        al.add(new Sentence("Testing", "", ""));
//        al.add(new Sentence("hica", "", ""));
//        al.add(new Sentence("hica", "", ""));
//        System.out.println(utils.printIterable(al, "\n") + "\n");
//
//        RedundancyRemover rr = new RedundancyRemover();
//        al = rr.removeRedundantSentences(al);
//
//        System.out.println(utils.printIterable(al, "\n"));
//    }
}
