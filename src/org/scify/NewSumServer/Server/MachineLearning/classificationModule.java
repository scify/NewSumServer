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

package org.scify.NewSumServer.Server.MachineLearning;

import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramSymWinGraph;
import gr.demokritos.iit.jinsect.storage.INSECTDB;
import gr.demokritos.iit.conceptualIndex.structs.Distribution;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.text.StyledEditorKit;

/**
 * Create a server to do all important actions for the Article labeling Contains
 * two methods. One for feeding the classifier with data and one for labeling
 *
 * @author panagiotis
 */
public class classificationModule {

    public INSECTDB file = new INSECTDBWithDir("", "./data/MachineLearningData");

//    public static  void main(String[] args){
//        
//        
//        
//        System.out.println(getCategory("Ο ηθοποιός Ντάνιελ Ντέι Λούις έρχεται ξανά στην Αθήνα προκειμένου να βοηθήσει τους σκοπούς της Εταιρείας Προστασίας Σπαστικών "));
//        
//        //{Τεχνολογία,Ελλάδα,Αθλητισμός,Κόσμος,Πολιτισμός,Οικονομία,Επιστήμη}
//        
//    }
    /**
     *
     * @param categoryName The category that the text belongs to
     * @param text The text that belongs to the specified category
     */
    public void feedClassifier(String categoryName, String text, boolean mergeGraph) {

        DocumentNGramSymWinGraph gTextGraph = new DocumentNGramSymWinGraph();
        // define graph for the text received
        gTextGraph.setDataString(text);
        //read all class names
        if (mergeGraph) {
            String[] aCategories = file.getObjectList("cg");
            //search if categoryName exists in the .cg list
            ArrayList<String> lsCategories = new ArrayList<String>(Arrays.asList(aCategories));
            if (lsCategories.contains(categoryName)) {
                //if true merge between the two graphs
                DocumentNGramSymWinGraph CategoryG =
                        (DocumentNGramSymWinGraph) file.loadObject(categoryName, "cg");
                Distribution<String> dClassCounts;
                String[] counterNames;
                counterNames = file.getObjectList("counter");
                if (counterNames.length == 0) {
                    dClassCounts = new Distribution<String>();
                    dClassCounts.increaseValue(categoryName, 1.0);
                    file.saveObject(dClassCounts, "mergeCounter", "counter");
                    double dInstanceCount = dClassCounts.getValue(categoryName);
                    CategoryG.mergeGraph(gTextGraph, 1 / dInstanceCount);
                } else {
                    dClassCounts = (Distribution<String>) file.loadObject("mergeCounter", "counter");
                    dClassCounts.increaseValue(categoryName, 1.0);
                    file.saveObject(dClassCounts, "mergeCounter", "counter");
                    double dInstanceCount = dClassCounts.getValue(categoryName);
                    CategoryG.mergeGraph(gTextGraph, 1 / dInstanceCount);
                }
//                        file.saveObject(CategoryG, categoryName, "cg");
            } else {
                //if false create new .cg with the current graph and categoryName as a name
                file.saveObject(gTextGraph, categoryName, "cg");
            }
        }


        //save in info.txt record with the text and the category name
        String sID = writeToFile.createTxtFile(categoryName, true);

        //save the current graph as .ig and name the serial number
        file.saveObject(gTextGraph, sID, "ig");

    }

    /**
     *
     * @param text The article text
     * @return the category that this text belongs to
     */
    public String getCategory(String text) {

        /* here begins the labelling process */

        String label;

        DocumentNGramSymWinGraph Textg = new DocumentNGramSymWinGraph();            // define graph for the Text tha i recive


        Textg.setDataString(text);                                                  //Create the text graph
        String[] categoryArray = file.getObjectList("cg");              //read all class names  and we put it in categoryArray 


        if (categoryArray.length == 0) {
            label = "-none-";

        } else {

            //semLabelling.acquire();
            //recommendation for the text
            label = labelTagging.recommendation(file, text);
            //semLabelling.release();
        }

        return label;                                             // send the label to client

    }
}
