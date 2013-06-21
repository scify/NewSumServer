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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.scify.NewSumServer.Server.Utils.Main;
import org.scify.NewSumServer.Server.Utils.Utilities;

/**
 * The Class used for Indexing
 * @author George K. <gkiom@scify.org>
 */
public class Indexer {

    private static final String FILE_FIELD = "file";
    private static final String TEXT_FIELD = "text";
    /**
     * The Directory containing the Index Files
     */
    private static File indexDir;
    /**
     * The Absolute path to the Directory where the Files to be indexed are
     */
    private String sFilesPath;
    /**
     * The Absolute path to the Directory where the Indexed Files are stored
     */
    private String sIndexPath;

    private Locale lLoc;

    private Analyzer anal;
    /**
     * The Global Logger Class.
     */
    protected final static Logger LOGGER = Main.getLogger();

//    public final String sFileSeparator = System.getProperty("file.separator");


    /**
     *
     * @param sFilesPath The Absolute path to the Directory where the Files to be indexed are
     * @param sIndexPath The Absolute path to the Directory where the Indexed Files are stored
     * @param loc The locale that the files will be indexed with
     */
    public Indexer(String sFilesPath, String sIndexPath, Locale loc) {
        this.sFilesPath = sFilesPath;
        this.sIndexPath = sIndexPath;
        this.lLoc       = loc;
        // The dir the Index files will be saved in
        indexDir        = new File(this.sIndexPath);
    }
    /**
     * The Main method of the Indexer Class.
     * Traverses a directory and creates the index files needed for the package to
     * operate.
     * @throws CorruptIndexException
     * @throws LockObtainFailedException
     * @throws IOException
     */
    public void createIndex()
            throws CorruptIndexException,
            LockObtainFailedException, IOException {
        // The dir containing the Files to Index
        File docDir = new File(this.sFilesPath);
        Directory FSDir = FSDirectory.open(indexDir);
        //init the Analyzer, according to locale
        if (lLoc.toString().equals("el")) {
            anal = new GreekAnalyzer(Version.LUCENE_36);
        } else if (lLoc.toString().equals("en")) {
            // The standard analyzer
            Analyzer stdAnal = new StandardAnalyzer(Version.LUCENE_36);
            // In order to index all the text in a field,
            // however long that field may be
            anal = new LimitTokenCountAnalyzer(stdAnal, Integer.MAX_VALUE);
        }
        // The configuration for the Index Writer
        IndexWriterConfig conf =
                new IndexWriterConfig(Version.LUCENE_36, anal);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        // The Index Writer
        IndexWriter indexWriter
                = new IndexWriter(FSDir, conf);
        // For each File in the dir, create a Document
        for (File file : getFilesFromFirstLeverSubdirs(docDir)) {
            String filename = file.getName();
            String fullFileName = file.getAbsolutePath();
            String tmpText = Utilities.readFromFile(fullFileName, " ");
            Document d = new Document(); //lucene Document
            // Add the "filename" field
            d.add(new Field
                (FILE_FIELD, filename,
                    Field.Store.YES, Field.Index.NOT_ANALYZED));
            // Add The "Text" Field
            d.add(new Field
                (TEXT_FIELD, tmpText,
                    Field.Store.YES, Field.Index.ANALYZED));

            // Add the Document to the Writer
            indexWriter.addDocument(d);
        }
        int numDocs = indexWriter.numDocs();
        // the index will be merged down into a single segment, resulting in
        // a smaller index with better search performance. Costly Operation,
        // DO NOT USE on large dirs or when low disk space (needs (2-3)*DirSize)
        indexWriter.forceMerge(1);
        // Syncs All referenced Index Files.
        // At this point old indexes will be deleted, freeing up space
        indexWriter.commit();
        // Terminate the Writer appropriately
        indexWriter.close();
//        LOGGER.log(Level.INFO, "Succesfully closed indexWriter with {0}", anal.toString());
    }

    /**
     *
     * @return The Directory that the Index Files are in
     */
    public File getIndexDirectory() {
//        Logger.getAnonymousLogger().log(Level.INFO, "INDEXER-->INDEXPATH: {0}", indexDir);
        return indexDir;
    }
//    public Analyzer getAnalyzer() {
//        return anal;
//    }

    /**
     * Get the files in the subdirs of a given directory.
     * If the given file is not a directory, return an empty list.
     * @param fDir The directory to analyze.
     * @return The list of (non-directory) files in the subdirectories.
     */
    protected List<File> getFilesFromFirstLeverSubdirs(File fDir) {
        ArrayList<File> lRes = new ArrayList<File>();
        // Only analyze, if a directory
        if (fDir.isDirectory()) {
            // Check only first level
            lRes.addAll(Arrays.asList(fDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    // Do NOT accept directories
                    return !file.isDirectory();
                }
            })));

            // For every subdir
            for (File fSubDir : fDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File file) {
                    // Do NOT accept directories
                    return file.isDirectory();
                }
            })) {
                // Get children files (Recursion)
                lRes.addAll(getFilesFromFirstLeverSubdirs(fSubDir));
            }
        }

        // Return result
        return lRes;
    }
}
//final class LuceneUtil {
//
//  private LuceneUtil() {}
//
//  public static List<String> tokenizeString(Analyzer analyzer, String string) {
//    List<String> result = new ArrayList<String>();
//    try {//or 'field' instead of null
//      TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
//      while (stream.incrementToken()) {
//        result.add(stream.getAttribute(CharTermAttribute.class).toString());
//      }
//    } catch (IOException e) {
//      // not thrown b/c we're using a string reader...
//      throw new RuntimeException(e);
//    }
//    return result;
//  }
//
//}
