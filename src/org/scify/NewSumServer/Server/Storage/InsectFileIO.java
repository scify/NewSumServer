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

import gr.demokritos.iit.jinsect.storage.INSECTFileDBWithDir;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scify.NewSumServer.Server.Structures.Article;
import org.scify.NewSumServer.Server.Structures.Sentence;
import org.scify.NewSumServer.Server.Structures.Topic;
import org.scify.NewSumServer.Server.Utils.Main;

/**
 * Class for File IO operations.
 * @author George K. <gkiom@scify.org>
 */
public class InsectFileIO implements IDataStorage {

    private final String        sLinks     = "Links";
    private final String        sTopics    = "Topics";
    private final String        sCategories= "Categories";
    private final String        sGeneric   = "generic";
    private static InsectFileIO        instance = null;
    private INSECTFileDBWithDir db;
    /**
     * The Directory where the Files are Saved
     */
    private String              BaseDir;
    private final static Logger LOGGER      = Main.getLogger();

    public static class AlreadyInitializedException extends IOException{

        public AlreadyInitializedException(String message) {
            super(message);
        }
        
    }
    
    public static class NotInitializedException extends IOException{
        public NotInitializedException(String message) {
            super(message);
        }
    }
    /**
     * Main Constructor. Singleton. Accepts a path to where the instance will store files.
     * @param sBaseDir The Full path to the Directory where the Files are Saved.
     * If null, the current directory is used. 
     */
    private InsectFileIO(String sBaseDir) {
        if (sBaseDir == null) {
            LOGGER.log(Level.WARNING, "No Base Directory passed");
            this.BaseDir = new File("").getAbsolutePath();
            db = new INSECTFileDBWithDir("_INSECT", this.BaseDir);
        } else {
            File f = new File(sBaseDir);
            if (f.isDirectory()) {
                this.BaseDir = sBaseDir;
                db = new INSECTFileDBWithDir("INSECT ", this.BaseDir);
            } else {
                LOGGER.log(Level.WARNING, "Path Is not a directory");
                this.BaseDir = new File("").getAbsolutePath();
                db = new INSECTFileDBWithDir("_INSECT", this.BaseDir);
            }
        }
    }

    public static InsectFileIO initialize(String sBaseDir) throws AlreadyInitializedException{
        if(instance==null){
            instance= new InsectFileIO(sBaseDir);
            return instance;
        }
        throw new AlreadyInitializedException("An instance of this class already exists");
    }
    
    public static InsectFileIO getInstance() throws NotInitializedException{
        if(instance==null){
            throw new NotInitializedException("Data storage has not been initialized");
        }
        return instance;
    }
    
    @Override
    public void saveObject(Serializable sObj, String sObjName, String sObjCategory) {
        this.db.saveObject(sObj, sObjName, sObjCategory);
    }

    @Override
    public Serializable loadObject(String sObjName, String sObjCategory) {
        if (objectExists(sObjName, sObjCategory)) {
            return this.db.loadObject(sObjName, sObjCategory);
        } else {
            LOGGER.log(Level.SEVERE, "File does not exist ", new FileNotFoundException());
            return null;
        }
    }

    @Override
    public void deleteObject(String sObjName, String sObjCat) {
        if (objectExists(sObjName, sObjCat)) {
            this.db.deleteObject(sObjName, sObjCat);
        }
    }
    @Override
    public void writeTopics(Map<Article, String> hmTopics, String sCategory) {
        //make a copy of the map
        HashMap<Article, String> cphmTopics = new HashMap<Article, String>(hmTopics);
        //save to the DB
        this.db.saveObject(cphmTopics, sTopics, sCategory);
    }

    @Override
    public Map<Article, String> getTopics(String sCategory) {
        try {
            return (HashMap<Article, String>) this.db.loadObject(sTopics, sCategory);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not get Topics ", ex.getMessage());
            return null;
        }

    }
    @Override
    public void writeSources(Map<String, String> mSources, String sUserID) {
        // make a copy of the list (so that it's serializable)
        HashMap<String, String> copy = new HashMap(mSources);
        //create a new db object and save
        if (this.db.existsObject(sLinks, sUserID)) {
            this.db.deleteObject(sLinks, sUserID);
        }
        this.db.saveObject(copy, sLinks, sUserID);
    }
    @Override
    public void writeGenericSources(Map<String, String> mSources) {
        // make a copy of the list (so that it's serializable)
        HashMap<String, String> copy = new HashMap(mSources);
        //create a new db object and save
        this.db.saveObject(copy, sLinks, sGeneric);
    }
    @Override
    public void writeLinksByCategory(Set<String> hsLinks, String sCategory) {
        HashSet<String> cp = new HashSet(hsLinks);
        if (this.db.existsObject(sCategory, sLinks)) {
            this.db.deleteObject(sCategory, sLinks);
        }
        this.db.saveObject(cp, sCategory, sLinks);
    }
    @Override
    public Set<String> getLinksByCategory(String sCategory) {
        try {
            return (HashSet<String>) this.db.loadObject(sCategory, sLinks);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not read links by category ", ex.getMessage());
            return null;
        }
    }
    @Override
    public HashMap<String, String> readSources(String sUUID) {
        try {
            return (HashMap<String, String>) this.db.loadObject(sLinks, sUUID);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not load Sources ", ex.getMessage());
            return null;
        }
    }
    @Override
    public boolean objectExists(String sObjName, String sUserID) {
        return this.db.existsObject(sObjName, sUserID);
    }
    @Override
    public String getLinksName() {
        return this.sLinks;
    }
    @Override
    public String getStorageDirectory() {
        return this.BaseDir;
    }
    @Override
    public void writeCategories(Collection<String> cCategories, String sUserID) {
        ArrayList<String> cp = new ArrayList<String>(cCategories);
        if (this.db.existsObject(sCategories, sUserID)) {
            this.db.deleteObject(sCategories, sUserID);
        }
        this.db.saveObject(cp, sCategories, sUserID);
    }
    @Override
    public void writeGenericCategories(Collection<String> cCategories) {
        ArrayList<String> cp = new ArrayList<String>(cCategories);
        this.db.saveObject(cp, sCategories, sGeneric);
    }
    @Override
    public Collection<String> readCategories(String sUserID) {
        try {
            return (ArrayList<String>) this.db.loadObject(sCategories, sUserID);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not read Categories ", ex.getMessage());
            return null;
        }
    }
    @Override
    public Collection<String> readGenericCategories() {
        try {
            return (ArrayList<String>) this.db.loadObject(sCategories, sGeneric);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not read Generic Categories ", ex.getMessage());
            return null;
        }
    }
    @Override
    public void writeClusteredTopics(HashMap<String, Topic> hsArticlesPerCluster) {
        HashMap<String, Topic> cp =
                new HashMap<String, Topic>(hsArticlesPerCluster);
        this.db.saveObject(cp, "ClusteredTopics", sGeneric);
    }
    @Override
    public HashMap<String, Topic> readClusteredTopics() {
        try {
            return (HashMap<String, Topic>) this.db.loadObject("ClusteredTopics", sGeneric);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not read Clustered Topics ", ex.getMessage());
            return null;
        }
    }
    @Override
    public void saveSummary(List<Sentence> lsSummary, String sTopicID) {
        ArrayList<Sentence> cp = new ArrayList<Sentence>(lsSummary);
        this.db.saveObject(cp, "Summary", sTopicID);
    }
    @Override
    public List<Sentence> loadSummary(String sTopicID) {
        try {
            ArrayList<Sentence> lsSummary =
                    (ArrayList<Sentence>) this.db.loadObject("Summary", sTopicID);
            return lsSummary;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not load Summary ", ex.getMessage());
            return null;
        }
    }
    @Override
    public String getCategoriesName() {
        return this.sCategories;
    }
    @Override
    public String getGeneric() {
        return sGeneric;
    }
}
