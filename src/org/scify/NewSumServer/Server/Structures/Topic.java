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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Describes a specific subject. It is made of all the {@link Article}s that are
 * in the same cluster, i.e. in the same thematic category.
 * It's main characteristics are the ID, the Title and it's date.
 * @author ggianna
 * @author George K. <gkiom@scify.org>`
 */
public class Topic extends ArrayList<Article> {

//    private static final long serialVersionUID = -7658801588686556924L;
    /**
     * The Clustered Topic Title
     */
    protected String Title = null;
    /**
     * The Clustered Topic Unique Identifier
     */
    protected String ID = null;

    /**
     * The Date of the Topic.
     * Initially assigned using {@link #setNewestDate(boolean)}
     */
    protected Calendar date = null;

    /**
     * Empty constructor of a Topic Object. Upon calling this constructor,
     * the topic is assigned a unique random {@link #ID}
     */
    public Topic() {
        super();
        this.ID = String.valueOf(UUID.randomUUID());
    }

    /**
     * Constructor with a specified ID
     * @param sID The Unique ID of the Topic
     */
    public Topic(String sID) {
        super();
        this.ID = sID;
    }
    /**
     * Full constructor
     * @param sID The ID of the Clustered Topic
     * @param sTitle The Title of the Clustered Topic
     * @param lArticles The Articles that are contained in this Topic
     */
    public Topic(String sID, String sTitle, List<Article> lArticles) {
        super(lArticles);
        this.ID = sID;
        if (sTitle != null) {
            this.Title = sTitle + " (" + String.valueOf(this.size()) + ")";
        }
    }
    /**
     * Constructs a Topic with the specified parameters but with a random ID
     * @param sTitle The Title of the Clustered Topic
     * @param lArticles The articles describing that Topic
     */
    public Topic(String sTitle, List<Article> lArticles) {
        super(lArticles);
        this.ID = String.valueOf(UUID.randomUUID());
        this.Title = sTitle + " (" + String.valueOf(this.size()) + ")";
    }
    /**
     *
     * @return The topic Title
     */
    public String getTitle() {
        // Check if already assigned a title
        if (this.Title != null) {
            return this.Title;
        }

        if (this.isEmpty()) {
            return null;
        }
        // Update title
        this.Title = this.get(0).getTitle() + " (" + String.valueOf(this.size()) + ")";
        return this.Title;
    }
    /**
     * Sets as the Topic Title the title from the Newest Article
     */
    public void setTitleFromNewest() {
        if (this.Title == null) {
            setTitle(this.get(0).getTitle());
        } else if (!this.Title.equals(this.get(0).getTitle())) {
            setTitle(this.get(0).getTitle());
        }
    }
    /**
     * Overwrites the title (if exists) with the specified one.
     * @param sNewTitle Assigns the Topic a Title
     */
    public void setTitle(String sNewTitle) {
        this.Title = sNewTitle + " (" + String.valueOf(this.size()) + ")";
    }
    /**
     * Overwrites the Topic ID (if exists) with the specified one
     * @param sID The ID of the Topic
     */
    public void setID(String sID) {
        this.ID = sID;
    }
    /**
     *
     * @return The topic ID
     */
    public String getID() {
        return this.ID;
    }
    /**
     * Sets as Topic date the newest or oldest date of the Articles contained
     * @param Newest true if we want the newest date of the articles to be
     * set as the topic date, false otherwise
     * @return true if newest date was set, false if oldest date was set
     */
    public boolean setNewestDate(final boolean Newest) {
        //set newest or oldest date
        if (this.size() == 1) {
            this.setDate(this.get(0).getDate());
            return true;
        } else {
            Collections.sort(this, new Comparator<Article>() {

                @Override
                public int compare(Article a1, Article a2) {
                    long lDiff = a1.getDate().getTimeInMillis() - a2.getDate().getTimeInMillis();
                    if (!Newest) {
                        if (lDiff > 0)
                            return 1;
                        if (lDiff < 0)
                            return -1;
                        return 0;
                    } else {
                        if (lDiff > 0)
                            return -1;
                        if (lDiff < 0)
                            return 1;
                        return 0;
                    }
                }
            });
            this.setDate(this.get(0).getDate());
//            System.out.println("Just Set newest date : " + this.printableWithDate()); // debug
        }
        return Newest;
    }
    /**
     * Sets the date of the Topic
     * @param cDate The date of the Topic
     */
    public void setDate(Calendar cDate) {
        this.date = cDate;
    }
    /**
     *
     * @return The Date of the Topic, in {@link Calendar} format
     */
    public Calendar getDate() {
        return this.date;
    }
    /**
     *
     * @return A printable and humanly understandable
     * representation of the Topic's Date
     */
    public String getDateToString() {
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("dd.MM.yyyy - HH:mm:ss z");
        return df.format(this.date.getTime());
    }
    /**
     *
     * @return The hashcode generated for this Topic's ID
     */
    @Override
    public int hashCode() {
        return this.ID.hashCode();
    }
    /**
     *
     * @return A helpful String representation of this Topic,
     * Using it's Unique ID and Title
     */
    @Override
    public String toString() {
        return this.ID + ": " + this.Title;
    }
    /**
     *
     * @return A String representation of this Topic using
     * it's ID and it's date
     */
    public String printableWithDate() {
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("dd.MM.yyyy - HH:mm:ss z");
        return this.ID + ": " + df.format(this.date.getTime());
    }
    /**
     * returns a YYYY-MM-DD representation of the Topic's Date.
     * @return A shortable date representation
     */
    public String getSortableDate() {
        SimpleDateFormat df = new SimpleDateFormat();
//        df.applyPattern("dd.MM.yyyy - HH:mm:ss z");
        df.applyPattern("yyyy-MM-dd");
        return df.format(this.date.getTime());
    }
    /**
     *
     * @return The number of Articles that this Topic comes from.
     */
    public int getArticlesCount() {
        return this.size();
    }
    /**
     *
     * @return The Category that this topic belongs to
     */
    public String getCategory() {
        return this.get(0).getCategory();
    }
}
