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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Sentence;
import org.scify.NewSumServer.Server.Structures.Topic;

/**
 * The module for File IO operations. It implements the FileINSECTDB class
 * @author George K. <gkiom@scify.org>
 */
public interface IDataStorage {

    /**
     *
     * @param sObj The object to be saved
     * @param sObjName Name of the object
     * @param sObjCategory Category of the object
     */
    void SaveObject(Serializable sObj, String sObjName, String sObjCategory);
    /**
     *
     * @param sObjName The name of the object to load
     * @param sObjCategory The category of the object
     * @return The object, or null if it not exists
     */
    Serializable loadObject(String sObjName, String sObjCategory);
    /**
     * Deletes the specified object
     * @param sObjName The name of the Object to delete
     * @param sObjCat The Category of the Object
     */
    void deleteObject(String sObjName, String sObjCat);
    /**
     * Saves the Topics according to their category
     * @param hmTopics The topics to save
     * @param sCategory The category to save
     */
    void writeTopics(Map<Article, String> hmTopics, String sCategory);

    /**
     *
     * @param sCategory The category of interest
     * @return The Topics that are mapped to the specified category
     */
    Map<Article, String> getTopics(String sCategory);

    /**
    * Writes the list of links to the DB
    * @param mSources The structure containing the sources
    * @param sUserID The UserID
    */
    void writeSources(Map<String, String> mSources, String sUserID);

    /**
    * Writes the list of links to the DB
    * @param mSources the sources, independent of user
    */
    void writeGenericSources(Map<String, String> mSources);
    /**
    * Reads the Sources that are contained in the specified
    * User ID from the database
    * @param sUUID The User's ID or "generic" for generic
    * @return The map containing each source and it's category
    * or null if Error / null map
    */
    HashMap<String, String> readSources(String sUUID);

    /**
     *
     * @param hsLinks The Set containing the links for the specified category
     * @param sCategory The category that the links belong to
     */
    void writeLinksByCategory(Set<String> hsLinks, String sCategory);

    /**
     *
     * @param sCategory The category of interest
     * @return The Sources contained in the specified category
     */
    Set<String> getLinksByCategory(String sCategory);
    /**
     *
     * @param sObjName The name that the object is saved as
     * @param sUserID The User ID, or the generic
     * @return True if the object exists, false otherwise
     */
    boolean objectExists(String sObjName, String sUserID);

    /**
     *
     * @return The name that the Link objects are saved as
     */
    String getLinksName();

//    String getTopicFromFile(int iClusterID);
    /**
     * Writes the Categories to a file
     * @param cCategories The Collection of the Categories.
     * @param sUserID The User ID
     */
    void writeCategories(Collection<String> cCategories, String sUserID);

    /**
     *
     * @param cCategories The Collection of the Categories.
     */
    void writeGenericCategories(Collection<String> cCategories);

    /**
     *
     * @param sUserID The User ID
     * @return The categories, as they are stored in a file.
     */
    Collection<String> readCategories(String sUserID);

    /**
     *
     * @return The Generic Categories.
     */
    Collection<String> readGenericCategories();
    /**
     *
     * @return The Full path to the folder where the files are saved to
     */
    String getStorageDirectory();
    /**
     * Saves the Clustered topics map to file
     * @param hsArticlesPerCluster The map containing the Clustered Articles
     */
    void writeClusteredTopics(HashMap<String, Topic> hsArticlesPerCluster);

    /**
     * Saves the Summary of a given Topic, using it's unique Topic ID
     * @param lsSummary The Summary for the given topic ID
     * @param sTopicID The Topic ID of interest
     */
    void saveSummary(List<Sentence> lsSummary, String sTopicID);

    /**
     *
     * @param sTopicID The Topic ID that represents the Topic of interest
     * @return The saved summary of the Topic
     */
    List<Sentence> loadSummary(String sTopicID);
    /**
     *
     * @return The saved (TopicID, Topic) Map
     */
    HashMap<String, Topic> readClusteredTopics();
    /**
     *
     * @return The name of the Categories file
     */
    String getCategoriesName();
    /**
     *
     * @return The generic sources name
     */
    String getGeneric();


}
