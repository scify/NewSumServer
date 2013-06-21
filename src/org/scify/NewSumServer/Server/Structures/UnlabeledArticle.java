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

import org.scify.NewSumServer.Server.MachineLearning.classificationModule;
import org.scify.NewSumServer.Server.Utils.Main;

/**
 *
 * @author George K. <gkiom@scify.org>
 */
public class UnlabeledArticle extends Article {

    /**
     * Constructor of the unlabeledUrticle. Uses same as it's parent class.
     * @param Source The Source that contains the article
     * @param Title The title of the Article
     * @param Text The text body of the Article
     * @param Category The Category that this article belongs to
     * @param Feed The feed url that this article is derived from
     * @param ToWrap if true, the article will be used to train the Category Classifier
     */
    public UnlabeledArticle(String Source, String Title, String Text, String Category, String Feed, Boolean ToWrap) {

        super(Source, Title, Text, Category, Feed, ToWrap);

    }

    @Override
    public String getCategory() {
//        if (this.Category == null) {
//            this.Category = obtainCategory();
//        }
        //TODO ask Classification Module for Category
        classificationModule cm = Main.getClassificationModule();
        //TODO check for errors
        this.Category = cm.getCategory(this.getText());


        return this.Category;
    }
}
