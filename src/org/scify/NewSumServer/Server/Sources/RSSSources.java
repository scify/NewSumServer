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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scify.NewSumServer.Server.Storage.IDataStorage;
import org.scify.NewSumServer.Server.Structures.User;
import org.scify.NewSumServer.Server.SystemFactory.Configuration;
import org.scify.NewSumServer.Server.Utils.Main;
import org.scify.NewSumServer.Server.Utils.Utilities;

/**
 * A Class describing the default Sources or for each
 * user specifically.
 * @author George K. <gkiom@scify.org>
 */
public class RSSSources {

    private static final Logger LOGGER = Main.getLogger();
    
    
    private Configuration conf;
    
    /**
     * The Structure that maps each RSS link to its relative Category.
     */
    private HashMap<String, String> hmRssSources = new HashMap<String, String>();
                // <sLink, sCategory>

    /**
     * The Collection of the Categories
     */
    private Collection<String>      sCategories;

    /**
     * The User that maybe connected to the specified Sources
     */
    private User                    uUser;

    /**
     * The string used for loading the common sources
     */
    private final static String     sGeneric = "generic";
    /**
     * The Path to the file where the Sources (RSS feeds) are stored
     */
    private String                  sPathToSources;

    
    private String                  sCatsDaysFilePath;
    
    /**
     * Initializes the RSS Sources taking into account their categories,
     * using a default collection of sources.
     * @param ids The data storage module for file IO operations
     * @param sSourcesPath The full path to the file where the sources are declared
     */
    public RSSSources(IDataStorage ids, Configuration config) {
        
        // get current configuration
        this.conf = config;
        
        this.sPathToSources = this.conf.getSourcesPath();
        
        this.sCatsDaysFilePath = this.conf.getCategoriesDaysFileLocation();
        
        try {
            LOGGER.log(Level.INFO, "NOW Reading Sources From {0}", this.sPathToSources);//debug
            initializeSources();//load sources from the text file
        } catch (Exception ex) {//if cannot load sources from the text file,
            //try to load from the stored (from last session)
            LOGGER.log(Level.SEVERE, ex.getMessage());
            if (ids.objectExists(ids.getLinksName(), sGeneric)) {
                //load the generic map
                LOGGER.log(Level.INFO, "Could not initialise Sources. Loading the generic map");
                this.hmRssSources = ids.readSources(sGeneric);
                this.sCategories = getCurrentCategories();
                saveCurrentCategories(ids, null); //save generic categories to file
                saveLinks(ids);
                saveLinkLabels(ids);
                saveSourceLabels(ids);
            } else {
                LOGGER.log(Level.SEVERE, "Cannot Load Sources.\nAborting...");
                System.exit(0);
            }
        } finally {
            this.sCategories = getCurrentCategories();
            saveCurrentCategories(ids, null); //save generic categories to file
            saveLinks(ids);
            saveLinkLabels(ids);
            saveSourceLabels(ids);
        }
    }
    /**
     * Initializes the RSS Sources taking into account their categories,
     * according to each User's likings.
     * @param ids The data storage module for file IO operations
     * @param sSourcesPath The full path to the file where the sources are declared
     * @param uUser the user connected to the specified sources
     */
    public RSSSources(IDataStorage ids, String sSourcesPath, User uUser) {
        this.sPathToSources = sSourcesPath;
        this.uUser = uUser;
        //Get the user ID
        String sUserID = this.uUser.getUserID();
        // Check if Sources file exists for that user
        if (ids.objectExists(ids.getLinksName(), sUserID)) {
            //load it
            this.hmRssSources = ids.readSources(sUserID);
        } else { // give the guy some default sources
            initializeSources();
        }
        sCategories = getCurrentCategories();
        saveCurrentCategories(ids, uUser);   //save user categories to file
        saveLinks(ids);
        saveLinkLabels(ids);
    }

    /**
     * Appends an RSS link to the link collection, unless the link already exists.
     * If it exists, then it is assigned a new category.
     * @param sURLLink a string containing the RSS link to add to the list
     * @param sCategory The category that this link belongs to
     */
    public void appendRssLink(String sURLLink, String sCategory) {
        // If the map already contains the key, then the value is replaced
        this.hmRssSources.put(sURLLink, sCategory);
    }
    /**
     * Removes the given RSS link from the list
     * @param sURLLink a String containing the RSS link to be removed
     * @return The previous value associated with key,
     * or null if there was no mapping for key.
     */
    public String removeRssLink(String sURLLink) {
        return this.hmRssSources.remove(sURLLink);
    }
    /**
     * Removes All the Links of the specified Category
     * @param sCategory The Category to remove.
     */
    public void removeCategory(String sCategory) {
        this.hmRssSources.values().removeAll(Collections.singleton(sCategory));
    }
    /**
     *
     * @return The map containing the RSS links of the object, each link
     * mapped to its category.
     */
    public HashMap<String, String> getRssLinks() {
        return this.hmRssSources;
    }
    /**
     * Saves the Sources to file
     * @param ids The Data Storage framework
     * @param uUser The User for whom the Sources will be saved
     */
    public void saveLinks(IDataStorage ids, User uUser) {
            ids.writeSources(this.hmRssSources, uUser.getUserID());
    }
    /**
     * Saves the generic Sources to file
     * @param ids The Data Storage framework
     */
    private void saveLinks(IDataStorage ids) {
        //save the generic file
        ids.writeSources(this.hmRssSources, sGeneric);
        //Save Sources File for each category
        for (String each: getCurrentCategories()) {
            HashSet<String> tmpSet = new HashSet<String>(
            (HashSet<String>) Utilities.getKeysByValue(this.hmRssSources, each));
            ids.writeLinksByCategory(tmpSet, each);
        }

    }
    /**
     * Replaces the Sources Map with the saved one, for the specified user
     * @param ids The Data Storage framework
     * @param uUser The User from whom the file will be loaded. If null, the
     * generic Sources will be loaded
     */
    public void loadLinks(IDataStorage ids, User uUser) {
        this.hmRssSources =
        (uUser != null) ? ids.readSources(uUser.getUserID()) : ids.readSources(sGeneric);
    }
    /**
     * Updates the Sources Map with the ones loaded from the file
     * @param ids The Data Storage framework
     * @param uUser The User from who's file the links will be loaded.
     * If null, the generic Sources will be loaded
     */
    public void updateAllLinks(IDataStorage ids, User uUser) {
        HashMap<String, String> hmNewRssSources =
        (uUser != null) ? ids.readSources(uUser.getUserID()) : ids.readSources(sGeneric);
        this.hmRssSources.putAll(hmNewRssSources);
    }

    /**
     *
     * @return the Sources that the program uses
     */
    public HashMap<String, String> getRssSources() {
        return this.hmRssSources;

    }
    /**
     *
     * @return The Categories
     */
    public Collection<String> getCategories() {
        return this.sCategories;
    }
    /**
     * Use this method to get the Categories if the map already exists.
     * @return A collection of the categories as string
     */
    private Collection<String> getCurrentCategories() {
        // Get a collection of Categories, eliminating Duplicates
        Collection<String> cCategories = new HashSet<String>(this.hmRssSources.values());
        this.sCategories = cCategories;
        return this.sCategories;
    }

    /**
     * Save the User categories to file. If user is null, saves the
     * generic categories.
     * @param ids The Data storage module
     * @param uUser The user for whom the categories are about.
     */
    private void saveCurrentCategories(IDataStorage ids, User uUser) {
        if (uUser == null) {
            ids.writeCategories(this.sCategories, sGeneric);
        } else {
            ids.writeCategories(this.sCategories, uUser.getUserID());
        }
    }
    /**
     * Reads the provided sources file and updates the {@link #hmRssSources}
     * map accordingly.
     */
    private void initializeSources() {
        if (!this.hmRssSources.isEmpty()) {
            this.hmRssSources.clear();
        }
        try {
            this.hmRssSources = Utilities.getSourcesFromFile(this.sPathToSources, this.sCatsDaysFilePath);
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "File not found ", ex.getMessage());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IO Error ", ex.getMessage());
        }
    }
    /**
     * Reads the specified sources file, and saves the (URL, label)
     * map using the storage module provided.
     * @param ids The storage module
     */
    private void saveLinkLabels(IDataStorage ids) {
        try {
            HashMap<String, String> hsLinkLabels =
                    Utilities.getLinkLabelsFromFile(this.sPathToSources);
            if (ids.objectExists("LinkLabels", "generic")) {
                ids.deleteObject("LinkLabels", "generic");
            }
            ids.saveObject(hsLinkLabels, "LinkLabels", "generic");
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    /**
     * Reads the specified sources file, and saves the (URL, SourceLabel)
     * map using the storage module provided.
     * @param ids The storage module
     */
    private void saveSourceLabels(IDataStorage ids) {
        try {
            HashMap<String, String> hsSourceLabels =
                    Utilities.getSourceLabelsFromFile(this.sPathToSources);
            if (ids.objectExists("SourceLabels", "generic")) {
                ids.deleteObject("SourceLabels", "generic");
            }
            ids.saveObject(hsSourceLabels, "SourceLabels", "generic");
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
