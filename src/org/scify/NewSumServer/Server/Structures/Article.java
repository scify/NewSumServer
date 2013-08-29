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
import java.util.Calendar;
import java.util.Date;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.scify.NewSumServer.Server.Utils.Utilities;

/**
 *
 * Describes an article fetched from a URL feed.
 * Use simple methods to set or to get values
 * @author ggianna
 * @author George K. <gkiom@scify.org>
 *
 */
public class Article implements java.io.Serializable {

    /**
     * The Source URL that the article is derived from.
     */
    protected String        Source;
    /**
     * The article. The RSS description of the article fetched.
     */
    protected String        Text;
    /**
     * The Title of the Article. The title fetched from the RSS feed.
     */
    protected String        Title;
    /**
     * The Category that this Article belongs to. E.g. Sports, Top News, etc.
     */
    protected String        Category;
    /**
     * The exact URL to the feed where the article was found at.
     */
    protected String        Feed;
    /**
     * The date of the Article, in string format.
     */
    protected String        sdate;
    /**
     * The url specifying an image for the article.
     */    
    protected String        imageUrl;
    /**
     * The date that the Article was created.
     * Most of the times, it is fetched from the Feed Provider.
     * Otherwise (if the feed does not provide the article date), it
     * is the date the article was retrieved
     */
    protected Calendar      date;
    /**
     * set True if this article is to be accessed by the classification trainer
     */
    protected Boolean toWrap;

    /**
     * The Constructor of the Article Class. Initializes a new Article Object,
     * with the below parameters.
     * @param Source    The source containing the article (ie the permalink)
     * @param Title     The title of the article
     * @param Text      The description of the article
     * @param Category  The category that the article belongs to
     * @param Feed      The feed that the article came from
     * @param toWrap    If true, the article's category will used to train the
     * machine learning algorithm
     */
    public Article(String Source, String Title, String Text, String Category,
            String Feed, String imageUrl, Boolean toWrap) {
        this.Source = Source;
        if (Text != null) {
            this.Text = cleanUp(Text.trim());
        } else {
            this.Text ="";
        }
        this.Title = cleanUp(Title);
        this.Category = Category;
        this.Feed = Feed;
        this.imageUrl = imageUrl;
        this.toWrap = toWrap;
    }
    /**
     *
     * @return the (String) permalink that contains the article.
     */
    public String getSource() {
        return Source;
    }
    /**
     *
     * @return the (String) description of the Article
     */
    public String getText() {
        return Text;
    }
    /**
     *
     * @return the imageUrl of the Article
     */
    public String getImageUrl() {
        return imageUrl;
    }
    /**
     *
     * @return the (String) Title of the Article
     */
    public String getTitle() {
        return Title;
    }
    /**
     *
     * @return The category that the article belongs to
     */
    public String getCategory() {
        return Category;
    }
    /**
     *
     * @return The Feed link that the Article came from
     */
    public String getFeed() {
        return Feed;
    }
    /**
     *
     * @return The date the article was created
     */
    public Calendar getDate() {
        return this.date;
    }
    /**
     *
     * @return true if the Article will be used to train the
     * classification package
     */
    public boolean getToWrap() {
        return this.toWrap;
    }
    
    /**
     * Sets the source that contains the article
     * @param Source The Source containing the Article
     */
    public void setSource(String Source) {
        this.Source = Source;
    }
    /**
     * Sets the description of the Article (the Article Body)
     * @param Text the Description of the Article
     */
    public void setText(String Text) {
        if (Text != null) {
            this.Text = cleanUp(Text);
        } else {
            this.Text = "";
        }
    }
    /**
     * Sets the title of the Article
     * @param Title The Title of the article
     */
    public void setTitle(String Title) {
        this.Title = cleanUp(Title);
    }
    /**
     * Sets the Category that the article belongs to
     * @param Category The category that the article belongs to
     */
    public void setCategory(String Category) {
        this.Category = Category;
    }

    /**
     *
     * @param Cal the calendar to set
     */
    public void setDate(Calendar Cal) {
        this.date = Cal;
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("dd.MM.yyyy - HH:mm:ss z");
        this.sdate = df.format(date.getTime());
    }
    /**
     * Sets the Date that the article was created, in Calendar format
     * @param date The Date the article was created
     */
    public void setDate(Date date) {
        this.date = Utilities.convertDateToCalendar(date);
        this.sdate = date.toString();
    }
    /**
     *
     * @return The date the article was created in string representation
     */
    public String getDatetoString() {
        SimpleDateFormat df = new SimpleDateFormat();
        df.applyPattern("dd.MM.yyyy - HH:mm:ss z");
        return df.format(this.date.getTime());
    }
    @Override
    public String toString() {
        return Title + "\n" + Text;
    }
    /**
     * Cleans up extra whitespace from the given text
     * @param sText the Text to cleanup
     * @return the text without any extra whitespace
     */
    private String cleanUp(String sText) {
        if (sText != null) {
            sText = Jsoup.clean(sText, Whitelist.none());
            sText = sText.replaceAll("«|»", "");
            sText = sText.replaceAll("&quot;", "");
            sText = sText.replaceAll("&nbsp;", "");
            sText = sText.replaceAll("&gt;", "");
            sText = sText.replaceAll("&[lr]aquo;", "");
            return sText;
        } else { return ""; }
    }
}