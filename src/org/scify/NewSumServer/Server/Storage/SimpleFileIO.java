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

package org.scify.NewSumServer.Server.Storage;

import java.io.*;
import java.util.*;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Sentence;
import org.scify.NewSumServer.Server.Structures.Topic;


/**Class FileIO contains all I/O methods and properties.
 * @author George K. <gkiom@scify.org>
 */
public class SimpleFileIO implements IDataStorage {

    /**
     * @property sSeparator used in
     * [Methods that it is used]
     */
    public final static String sSeparator = " = ";


    /** Creates/Updates The file containing the Topics /data/Topics.txt
     * @param hmTopics the map containing all the Topics (TopicID, Topic)
     */
    @Override
    public void writeTopics(Map<Article, String> hmTopics, String sCategory) {
        // should write all topics to file
        // topics are in (map?) structure (ID, topic)
    }
    /**
     *
     * @param sCategory The Category of Interest
     * @return The topics contained in the specified category
     */
    @Override
    public Map<Article, String> getTopics(String sCategory) {
        // should read the file
        // and return the topics in a map (ID, topic) structure
        return null;
    }
    /**
     * Writes the Links File
     * @param sUUID The User ID
     */
    @Override
    public void writeSources(Map<String, String> mSources, String sUUID) {
    }
    /**
     * reads the file containing the links
     * @return a list containing the links
     */
    @Override
    public HashMap<String, String> readSources(String sUUID) {
        String sPathToFile = "path to file";
        List lsLines = new ArrayList<String>();
        try {

            File fFile = new File(sPathToFile);
                if (fFile.exists()) {
                    if (fFile.canRead()) {
                        FileInputStream fstream = new FileInputStream(fFile);
                        // Get the object of DataInputStream
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String sLine;
                        //Read File Line By Line
                        while ((sLine = br.readLine()) != null) {
                            lsLines.add(sLine);
                        }
                    } else { System.err.println("Cannot read file " + sPathToFile); }

                } else { System.err.println("File " + sPathToFile + " does not exist"); }

        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
        System.out.println(lsLines.toString());
        return null;
    }
    @Override
    public boolean objectExists(String sObjName, String sUserID) {
        return false;
    }
    /**
     *
     * @param iClusterID The Clustered Topic Index
     * @return The Topic that is represented by this ID
     */
    private static String getTopicFromFile(int iClusterID) {
        // should return the topic by it's ID, reading the topic file
        return "";
    }

    private void readFromFile(String sPathToFile) {
//        List lsLines = new ArrayList<String>();
//        File fFile = new File(sPathToFile);
//            if (fFile.exists()) {
//                if (fFile.canRead()) {
//                    FileInputStream fstream = new FileInputStream(fFile);
//                    // Get the object of DataInputStream
//                    DataInputStream in = new DataInputStream(fstream);
//                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
//                    String sLine;
//                    //Read File Line By Line
//                    while ((sLine = br.readLine()) != null) {
//                        String[] sSplit = sLine.split(sSeparator);
//                        lsLines.add(sLine);
//                         3rd field contains category
//                         If we have asked for this category
//                        if (sSplit[2].trim().equals(sCategory.trim()))
//                            // add it
//                            hmTopics.put(sSplit[0], sSplit[1]);
//                    }
//                    //Close the input stream
//                    in.close();
//                } else {
//                    System.err.println("Error: Unable to read from file");
//                }
//            } else {
//                System.err.println("Error: File" + fFile.toString()  + "does not exist");
//            }
//        } catch (Exception e) {
//            System.err.println("Error: " + e.getMessage());
//        }
//        return hmTopics; // if reached

    }

    @Override
    public String getLinksName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeCategories(Collection<String> cCategories, String sUserID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//    @Override
    /**
     *
     * @return the categories
     */
    public Collection<String> readCategories() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getStorageDirectory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCategoriesName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<String> readCategories(String sUserID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeGenericSources(Map<String, String> mSources) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeGenericCategories(Collection<String> cCategories) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<String> readGenericCategories() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getGeneric() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeLinksByCategory(Set<String> hsLinks, String Category) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<String> getLinksByCategory(String sCategory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeClusteredTopics(HashMap<String, Topic> hsArticlesPerCluster) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HashMap<String, Topic> readClusteredTopics() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void SaveObject(Serializable sObj, String sObjName, String sObjCategory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Serializable loadObject(String sObjName, String sObjCategory) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveSummary(List<Sentence> lsSummary, String sTopicID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Sentence> loadSummary(String sTopicID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteObject(String sObjName, String sObjCat) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
