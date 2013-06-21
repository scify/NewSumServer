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

import org.scify.NewSumServer.Server.MachineLearning.util;
import gr.demokritos.iit.jinsect.documentModel.comparators.NGramCachedGraphComparator;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramSymWinGraph;
import gr.demokritos.iit.jinsect.storage.INSECTDB;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * The methods to create a vector for
 * {@link dataSets#labelingSet(gr.demokritos.iit.jinsect.storage.INSECTDB, java.lang.String, java.lang.String)  labeling}
 * or
 * {@link dataSets#trainingSet(gr.demokritos.iit.jinsect.storage.INSECTDB, java.lang.String)   Training}
 *
 * @author panagiotis giotis
 */
public class vector {

    /**
     * Get similarity between the mail graph and all class graphs
     *
     * @param text Is the mail for the labeling process
     * @param file The path file for the Insect db
     * @return A string with the vectors
     */
    public static String labellingVector(String text, INSECTDB file) {

        NGramCachedGraphComparator ngc = new NGramCachedGraphComparator();
        DocumentNGramSymWinGraph textg = new DocumentNGramSymWinGraph();           // define graph for the mail that recive
        DocumentNGramSymWinGraph categoryg = new DocumentNGramSymWinGraph();       // define graph for the class graph

        double NVS;
        String vector = "";

        textg.setDataString(text);                                                 // convert text to graph

        HashSet<String> hasGnames = new HashSet<String>();                         //create a HashSet with all class graph names
        hasGnames.addAll(Arrays.asList(file.getObjectList("cg")));

        for (String categoryname : hasGnames) {                                       // for each class graph find the similarity number
 
            categoryg = (DocumentNGramSymWinGraph) file.loadObject(categoryname, "cg");

            GraphSimilarity gs = ngc.getSimilarityBetween(textg, categoryg); //compare tow graphs

            NVS = (gs.SizeSimilarity == 0.0) ? 0.0 : gs.ValueSimilarity / gs.SizeSimilarity;
            if (vector.equals("")) {
                vector = Double.toString(NVS) + ",";
            } else {
                vector = vector + Double.toString(NVS) + ",";          // write the number of similarity to vector                 
            }
        }


        return vector;     //example 0.2,0.1,0,0,1,
    }

    /**
     * Get similarity between the instances with the given class name and all
     * class graphs
     *
     * @param file The path for the InsectDB file
     * @return A ArrayList with all vectors
     */
    public static ArrayList<String> trainingVector(INSECTDB file) {

        NGramCachedGraphComparator ngc = new NGramCachedGraphComparator();
        DocumentNGramSymWinGraph fisrtGraph = new DocumentNGramSymWinGraph();
        DocumentNGramSymWinGraph secondGraph = new DocumentNGramSymWinGraph();

        double NVS;
        ArrayList<String> vectors = new ArrayList<String>();

        

        HashSet<String> hasGnames = new HashSet<String>();                         //create a HashSet with all class graph names
        hasGnames.addAll(Arrays.asList(file.getObjectList("cg")));
        //------------------optimisation----------------------------------------
        ArrayList<DocumentNGramSymWinGraph> CategoryGraphs = new ArrayList<DocumentNGramSymWinGraph>();
         for (String index : hasGnames) {
     
           CategoryGraphs.add( (DocumentNGramSymWinGraph) file.loadObject(index, "cg"));
           
         }
        
         //-----------------optimisation----------------------------------------
         
        HashSet<String> hasInames = new HashSet<String>();                         //create a HashSet with all class graph names
        hasInames.addAll(Arrays.asList(file.getObjectList("ig")));

        for (String instanceGraph : hasInames) {                                      // for each instance find the vector and put it in the list
            String [] tempTable=util.recordLine(instanceGraph).trim().split(":");
            
            String CategoryName =tempTable[1] ;
            
            fisrtGraph = (DocumentNGramSymWinGraph) file.loadObject(instanceGraph, "ig");
            String vector = "";

            for (DocumentNGramSymWinGraph index : CategoryGraphs) {
//for (String index : hasGnames) {
//secondGraph = (DocumentNGramSymWinGraph) file.loadObject(index, "cg");
//GraphSimilarity gs = ngc.getSimilarityBetween(fisrtGraph, secondGraph);
                GraphSimilarity gs = ngc.getSimilarityBetween(fisrtGraph, index);

                NVS = (gs.SizeSimilarity == 0.0) ? 0.0 : gs.ValueSimilarity / gs.SizeSimilarity;
                if (vector.equals("")) {
                    vector = Double.toString(NVS) + ",";
                } else {
                    vector = vector + Double.toString(NVS) + ",";             // write the number of similarity to vector                 
                }

            }

            vector=vector+CategoryName;
            vectors.add(vector);                                               //add vector to Array List
            
        }


        return vectors;
    }
}
