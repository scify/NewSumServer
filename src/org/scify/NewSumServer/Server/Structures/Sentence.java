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

package org.scify.NewSumServer.Server.Structures;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.scify.NewSumServer.Server.Structures.Sentence.LOGGER;
import org.scify.NewSumServer.Server.Utils.Main;
import org.scify.NewSumServer.Server.Utils.Utilities;

/**
 * @version 1, %G%
 * Sentence is the class that describes an Article
 * Snippet and it's specified parameters.
 * @author George K. <gkiom@scify.org>
 */
public class Sentence implements Serializable {
    /**
     * The logger class, inherited from main
     */
    protected static final Logger LOGGER = Main.getLogger();
    /**
     * The Article Snippet, ie the important sentence of an article
     */
    protected String    sArtSnippet;                // The Article Snippet
    /**
     * The permalink that the article, from which the sentence was taken,
     * comes from
     */
    protected String    sLink;                      // The permalink the article comes from
    /**
     * The RSS Feed link that the article containing the Sentence was found in
     */
    protected String    sFeed;                      // The RSSFeed that the article came from
    /**
     * The Image URL link in the article, as string
     */
    protected String    sSourceImageUrl;

    /**
     * The static Sentence Separator
     */
    protected static String    sSentenceSeparator
                                    = "===";       // The Sentence Separator

    /**
     * Constructor of the Sentence Class. Initializes a new Sentence object with
     * the specified attributes.
     * @param sArtSnippet   The Article Snippet (ie the important sentence)
     * @param sLink         The permalink that links to the article this sentence comes from
     * @param sFeed         The Feed that the sentence came from
     */
    public Sentence(String sArtSnippet, String sLink, String sFeed, String sSourceImageUrl) {

        this.sArtSnippet        = sArtSnippet;
        this.sLink              = sLink;
        this.sFeed              = sFeed;
        this.sSourceImageUrl    = sSourceImageUrl;
    }
    /**
     *
     * @return the Article Snippet
     */
    public String getSnippet() {
        return sArtSnippet;
    }
    /**
     *
     * @return the Article Snippet
     */
    public String getSourceImageUrl() {
        return this.sSourceImageUrl;
    }
    
    public void setSourceImageUrl(String sSourceImageUrlArg) {
        this.sSourceImageUrl = sSourceImageUrlArg;
    }
    /**
     *
     * @return The link that the Sentence comes from
     */
    public String getLinkToSource() {
        return sLink;
    }
    /**
     *
     * @return The Feed That the Article was found in
     */
    public String getFeed() {
        return sFeed;
    }
    /**
     * Sets The Article Snippet of the Sentence
     * @param sSnippet The String that represents the Article snippet
     */
    public void setSnippet(String sSnippet) {
        sArtSnippet = sSnippet;
    }
    /**
     * Sets The link to the Article that contains the Sentence
     * @param sLinkToSet The link that the sentence was found in.
     */
    public void setLinktoSentence(String sLinkToSet) {
        if (Utilities.ValidURL(sLinkToSet)) {
            sLink = sLinkToSet;
        } else {
            sLink = "";
            LOGGER.log(Level.INFO, "Invalid permalink for {0}", this.getSnippet());
        }
    }
    /**
     * Sets the feed URL of the Sentence, if valid.
     * Sets null if the feed to set is invalid.
     * @param sFeedToSet The feed that contains the article snippet
     */
    public void setFeed(String sFeedToSet) {
        if (Utilities.ValidURL(sFeedToSet)) {
            sFeed = sFeedToSet;
        } else {
            sFeed = "";
            LOGGER.log(Level.INFO, "Invalid Link for {0}", this.getSnippet());
        }
    }
    /**
     *
     * @return The Sentence Separator (i.e. SECOND_LEVEL_SEPARATOR)
     */
    public static String getSentenceSeparator() {
        return sSentenceSeparator;
    }
    @Override
    public String toString() {
        return sArtSnippet + sSentenceSeparator + sLink + sSentenceSeparator + sFeed;
    }
}
