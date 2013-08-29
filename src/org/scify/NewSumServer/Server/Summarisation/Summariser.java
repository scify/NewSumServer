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

import Jama.Matrix;
import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedNonSymmGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramSymWinGraph;
import gr.demokritos.iit.jinsect.storage.INSECTDB;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;
import gr.demokritos.iit.jinsect.utils;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceSample;
import opennlp.tools.sentdetect.SentenceSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Sentence;
import org.scify.NewSumServer.Server.Structures.Topic;
import static org.scify.NewSumServer.Server.Summarisation.Summariser.LOGGER;
import org.scify.NewSumServer.Server.Utils.Main;

/**
 *
 * @author ggianna
 */
public class Summariser {

    /**
     * The Logger, inherited from main
     */
    protected final static Logger LOGGER = Main.getLogger();

    // Sentence model constants
    protected final String SENTENCE_MODEL_OBJNAME = "SentenceModel";
    protected final String SENTENCE_MODEL_OBJTYPE = "NLPModel";
    protected final String SUMMARY_OBJTYPE = "Summary";

    //DONE SAVE SUMMARY, for each Summary Created using InsectFileIO.saveSummary
    /**
     * The map containing the summaries
     */
    protected Map<String, List<Sentence>>   hsSentencesPerCluster;

    /**
     * The Topics
     */
    protected Set<Topic>    stTopics;

    /**
     * Storage for summaries and related index
     */
    protected INSECTDB SummaryStorage;

    /**
     * Sentence splitter model
     */
    protected SentenceModel smSplitter = null;

    /**
     * Main Constructor of the Summariser Class.
     * @param stTopics The topics to summarize from
     * @param SummaryStorage The module used for storage
     */
    public Summariser(Set<Topic> stTopics,
            INSECTDB SummaryStorage) {
        this.stTopics = stTopics;
        this.SummaryStorage = SummaryStorage;

        // Init splitter
        initSplitter();
    }

    private void initSplitter() {
        // TODO: Check whether model already exists
        SentenceModel model = null;
        boolean bModelExisted = false;

        File fTmp = new File(Main.sToolPath +  "splitModel.dat");
        // If file exists
        if (fTmp.exists()) {
            // Try to load it
            InputStream modelIn = null;
            try {
                modelIn = new FileInputStream(fTmp);
                model = new SentenceModel(modelIn);
                // On success
                if (model != null)
                    // note that it already existed
                    bModelExisted = true;
            }
            catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not load sentence splitter model.", e);
            }
            finally {
                // Finalize model file access, if possible
                if (modelIn != null) {
                    try {
                        modelIn.close();
                    }
                    catch (IOException e) {
                        model = null;
                    }
                }
            }
        }

        // If the model was not loaded normally
        if (model == null)
        {
            Charset charset = Charset.forName("UTF-8");
            ObjectStream<String> lineStream = new PlainTextByLineStream(
                    getClass().getResourceAsStream("SentenceSplitterTraining.txt"),
                charset);
            ObjectStream<SentenceSample> sampleStream = new SentenceSampleStream(lineStream);


            try {
                try {
                    model = SentenceDetectorME.train("gr", sampleStream, true, null);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not create sentence splitter model.", ex);
                    return;
                }
            }
            finally {
                try {
                    sampleStream.close();
                } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Could not create sentence splitter model.", ex);
                }
            }
        }

        OutputStream modelOut = null;
        boolean bSuccess = false;
        try {
            //File fTmp = File.createTempFile("splitModel", null);
            FileOutputStream fsOut = new FileOutputStream(fTmp);
            modelOut = new BufferedOutputStream(fsOut);
            model.serialize(modelOut);
            bSuccess = true;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not create sentence splitter model.", ex);
        } finally {
        if (modelOut != null)
            try {
                modelOut.close();
            } catch (IOException ex) {
                bSuccess = false;
                LOGGER.log(Level.SEVERE, "Could not finalize sentence splitter model.", ex);
            }
        }

        if (bSuccess)
            this.smSplitter = model;
    }

    /**
     * Creates all Summaries
     * @return A map containing the Summary for each ID
     */
    public Map<String, List<Sentence>> getSummaries() {
        LOGGER.log(Level.INFO, "Obtaining Summaries...");
        // Init result
        Map<String, List<Sentence>> mRes = new HashMap<String, List<Sentence>>();
        // For every cluster
        for (Topic tCurTopic : stTopics) {
            // Add its summary to the result map
            mRes.put(tCurTopic.getID(), getSummary(tCurTopic));
        }
        hsSentencesPerCluster = mRes;
        LOGGER.log(Level.INFO, "Summaries obtained Succesfully");
        // Return summary map
        return mRes;
    }
    /**
     * Creates a summary of the Articles of interest
     * @param tTopic The Topic that will be processed
     * @return A List of Sentence Objects for the specified UUID.
     */
    public List<Sentence> getSummary(Topic tTopic) {
        // Check if already loaded in-memory
        if (hsSentencesPerCluster != null) {
            if (!hsSentencesPerCluster.isEmpty()) {
                if (hsSentencesPerCluster.containsKey(tTopic.getID())) {
                    return hsSentencesPerCluster.get(tTopic.getID());
                }
            }
        }

        // Init document graphs and sentences
        LinkedList<Sentence> lAllSentences = null;

        // Check if on disk
        boolean bLoadedOK = false;
        if (SummaryStorage.existsObject(tTopic.getID(), SUMMARY_OBJTYPE)) {
            lAllSentences = (LinkedList<Sentence>)SummaryStorage.loadObject(
                    tTopic.getID(), SUMMARY_OBJTYPE);
        }
        // If unsuccessfully loaded
        if (lAllSentences == null) {
            bLoadedOK = false; // Update loaded variable
            // Init to empty
            lAllSentences = new LinkedList<Sentence>();
        }
        else // else
        {
            // Return summary
            return lAllSentences;
        }



        // If only a single document
        if (tTopic.size() == 1)
        {
            Article aCur = tTopic.get(0);

            // Split into sentences
            String[] saSentences;
            // If we do not have a model
            if (smSplitter == null) {
                // Use plain splitting
                saSentences = aCur.getText().split("[.!?;:\"']");
            }
            else // else use the model
            {
                SentenceDetectorME sentenceDetector = new SentenceDetectorME(smSplitter);
                saSentences = sentenceDetector.sentDetect(aCur.getText());
            } // end If we do not have a model

            // For each sentence
            for (String sCurSentence : saSentences) {
                // If not empty
                if (sCurSentence.trim().length() > 0)
                {
                    // Create sentence object
                    Sentence sCur = 
                        new Sentence(sCurSentence.trim(), 
                                    aCur.getSource(), 
                                    aCur.getFeed(),
                                    aCur.getImageUrl());
                    // Add to all sentences list
                    lAllSentences.add(sCur);
                }
            }
            return lAllSentences;
        }

        // For every article in cluster
        for (Article aCur : tTopic) {
            // Split into sentences
            String[] saSentences;
            // If we do not have a model
            if (smSplitter == null) {
                // Use plain splitting
                saSentences = aCur.getText().split("[.!?;:\"']");
            }
            else // else use the model
            {
                SentenceDetectorME sentenceDetector = new SentenceDetectorME(smSplitter);
                saSentences = sentenceDetector.sentDetect(aCur.getText());
            } // end If we do not have a model

            // For each sentence
            for (String sCurSentence : saSentences) {
                // If not empty
                if (sCurSentence.trim().length() > 0)
                {
                    // Create sentence object
                    Sentence sCur = 
                        new Sentence(sCurSentence.trim(), 
                                    aCur.getSource(), 
                                    aCur.getFeed(),
                                    aCur.getImageUrl());
                    // Add to all sentences list
                    lAllSentences.add(sCur);
                }
            }
        }

        // Get sentence clusters
        Set<Set<Sentence>> sSentenceClusters = getClusters(lAllSentences);

        // For each cluster
        double dCnt = 0.0;
        final DocumentNGramSymWinGraph dgContentGraph = new DocumentNGramSymWinGraph();
        for (Set<Sentence> ssCurCluster : sSentenceClusters) {
            // Create common n-gram graph
            DocumentNGramGraph dgCluster = getGraphFromCluster(ssCurCluster);
            // and add to content graph
            dgContentGraph.merge(dgCluster, 1.0 / ++dCnt);
        }

        // Order sentences by Value Similarity (and not NVS) to the content graph
        Collections.sort(lAllSentences, new Comparator<Sentence>() {

            @Override
            public int compare(Sentence t, Sentence t1) {
                // Init sentence graphs
                DocumentNGramGraph dg = new DocumentNGramSymWinGraph();
                dg.setDataString(t.getSnippet());
                DocumentNGramGraph dg1 = new DocumentNGramSymWinGraph();
                dg1.setDataString(t1.getSnippet());
                // Compare to content graph
                NGramCachedGraphComparator ngc = new NGramCachedNonSymmGraphComparator();
                double dVS = ngc.getSimilarityBetween(dg, dgContentGraph).ValueSimilarity;
                double dVS1 = ngc.getSimilarityBetween(dg1, dgContentGraph).ValueSimilarity;

                // Return order based on similarity comparison
                return (int)Math.signum(dVS - dVS1);
            }
        });
        // TODO: Check sentences with most Named Entities?
        // TODO: Extract other features?

        // Save summary
        try {
            // Only if it is not already available and valid
            if (!bLoadedOK)
                if (!SummaryStorage.existsObject(tTopic.getID(), SUMMARY_OBJTYPE))
                    SummaryStorage.saveObject(lAllSentences,
                      tTopic.getID(), SUMMARY_OBJTYPE);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Could Not Save Summary with Topic ID {0} ", tTopic.getID());
        }
        // Return sorted sentences
        return lAllSentences;
    }

    /**
     *
     * @param ssCluster The Sentences to process
     * @return The graph for the specified set of sentences
     */
    protected DocumentNGramGraph getGraphFromCluster(Set<Sentence> ssCluster) {
        // Init result graph
        DocumentNGramSymWinGraph dgRes = new DocumentNGramSymWinGraph();
        double dCnt = 0.0;
        // For every sentence
        for (Sentence sCur : ssCluster) {
            // If first sentence
            if (dCnt == 0.0) {
                // Initialize graph
                dgRes.setDataString(sCur.getSnippet());
                dCnt++;
            }
            else {
                // else intersect
                DocumentNGramSymWinGraph dgNew = new DocumentNGramSymWinGraph();
                dgNew.setDataString(sCur.getSnippet());
                dgRes.intersectGraph(dgNew);
            }
        }

        // Return result graph
        return dgRes;
    }

    /**
     * Clusters a set of sentences. Uses Markov Clustering (MCL).
     * @param lAllSentences The List of sentences to cluster.
     * @return A set of Set&lt;Sentence&gt; objects, which constitute clusters
     * of a set of given sentences.
     */
    protected NavigableSet<Set<Sentence>> getClusters(List<Sentence> lAllSentences) {
        // Create navigable set
        TreeSet<Set<Sentence>> tsRes = new TreeSet<Set<Sentence>>(new Comparator<Set<Sentence>>() {
            @Override
            public int compare(Set<Sentence> t, Set<Sentence> t1) {
                // Use string representations for comparison
                return utils.printIterable(t, "***").compareTo(utils.printIterable(t1, "***"));
            }
        });

        // Get similarities
        Matrix mSims = getSimilarityMatrix(lAllSentences);
        // Initial step
        // Normalize per column to render stochastic
        normalizeMatrixPerColumn(mSims, 1.0);
        Matrix mLastRes = null;

        // Until convergence or 100 iterations
        for (int iIter = 0; iIter < 100; iIter++) {
            // Expand by squaring the matrix
            mLastRes = mSims.times(mSims);
            // Inflate
            normalizeMatrixPerColumn(mLastRes, 2.0);
            // If convergence has been achieved
            if (mSims.minus(mLastRes).normInf() < 0.001)
                break;
            // Update sim matrix by copying last result
            mSims = mLastRes.copy();
        }

        // Final step: Interprete results
        // For each row
        for (int iRow = 0; iRow < mLastRes.getRowDimension(); iRow++) {
            Set<Sentence> sCluster = new HashSet<Sentence>();
            // For all columns
            for (int iCol = 0; iCol < mLastRes.getColumnDimension(); iCol++)
            {
                // If it contains a non-zero element (above 0.01)
                if (mLastRes.get(iRow, iCol) > 0.01)
                    // Add it to the current cluster
                    sCluster.add(lAllSentences.get(iCol));
            }
            // Add cluster to result set
            tsRes.add(sCluster);
        }

        // Return map
        return tsRes;
    }

    /**
     * Calculates a similarity matrix (including self-similarity), by using NVS
     * calculation.
     * @param lAllSentences
     * @return
     */
    protected Matrix getSimilarityMatrix(List<Sentence> lAllSentences) {
        // Init sim matrix
        final Matrix mSims = new Matrix(lAllSentences.size(), lAllSentences.size());
        // Perform parallel execution
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // Init final vars
        final List<Sentence> lAllSentencesArg = lAllSentences;
        int iFirstCnt = 0;

        // For every sentence pair in cluster
        for (final Sentence sFirst : lAllSentences) {
            final int iFirstCntArg = iFirstCnt;
            es.submit(new Runnable() {

                @Override
                public void run() {
                    double dSim = 0.0;
                    int iSecondCnt = 0;
                    NGramCachedGraphComparator ngc = new NGramCachedGraphComparator();
                    // Create first graph
                    DocumentNGramGraph gFirst = new DocumentNGramSymWinGraph();
                    gFirst.setDataString(sFirst.getSnippet());

                    for (Sentence sSecond : lAllSentencesArg) {
                        if (iSecondCnt == iFirstCntArg)
                            dSim = 1.0;
                        else {
                            // Create second graph
                            // TODO: Use cache?
                            DocumentNGramGraph gSecond = new DocumentNGramSymWinGraph();
                            gSecond.setDataString(sSecond.getSnippet());
                            // Calculate Normalized Value Similarity
                            GraphSimilarity gsCur = ngc.getSimilarityBetween(gFirst, gSecond);
                            dSim = gsCur.SizeSimilarity == 0.0 ? 0.0 :
                                    gsCur.ValueSimilarity / gsCur.SizeSimilarity;
                        }
                        // Set to matrix
                        synchronized (mSims) {
                            mSims.set(iFirstCntArg, iSecondCnt, dSim);
                        }
                        iSecondCnt++;
                    }
                }
            });
            iFirstCnt++;
        }
        // Complete comparisons
        es.shutdown();
        try {
            es.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(Summariser.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        return mSims;
    }

    /**
     * Normalizes a matrix on a per column basis.
     * @param mToNormalize The matrix to normalize <b>in place</b>.
     * @param dPower The power to raise the elements to, before normalization
     * @return The normalized matrix.
     */
    protected Matrix normalizeMatrixPerColumn(Matrix mToNormalize, double dPower) {

        // For every column
        for (int iColumnCnt=0; iColumnCnt < mToNormalize.getColumnDimension(); iColumnCnt++) {
            // Determine sum
            double dColSum = 0.0;
            // For every row
            for (int iRowCnt=0; iRowCnt < mToNormalize.getRowDimension(); iRowCnt++) {
                double dPowered = Math.pow(mToNormalize.get(iRowCnt, iColumnCnt), dPower);
                // Update matrix value
                mToNormalize.set(iRowCnt, iColumnCnt, dPowered);
                // Update sum
                dColSum += dPowered;
            }

            // For every row
            for (int iRowCnt=0; iRowCnt < mToNormalize.getRowDimension(); iRowCnt++) {
                double dNormalized = mToNormalize.get(iRowCnt, iColumnCnt) / dColSum;
                // Update matrix value to normalized value
                mToNormalize.set(iRowCnt, iColumnCnt, dNormalized);
            }

        }

        return mToNormalize;
    }
}
