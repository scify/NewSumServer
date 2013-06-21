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

import org.scify.NewSumServer.Server.MachineLearning.vector;
import gr.demokritos.iit.jinsect.storage.INSECTDB;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;

/**
 *
 * @author panagiotis giotis
 *
 * Includes methods which creates the train and test dataset
 *
 */
public class dataSets {

    /**
     * Generate the train dataset
     *
     * @param file the path for the InsectDB file
     * @return the train dataset as Instance
     */
    public static Instances trainingSet(INSECTDB file) {
        ArrayList<Attribute> atts;
        ArrayList<String> attVals = new ArrayList<String>();
        ArrayList<String> vectors;
        Instances data;
        double[] vals;


        atts = new ArrayList<Attribute>();                                      // Set up attributes

        HashSet<String> hasGnames = new HashSet<String>();                      //create a HashSet with all class graph names 
        hasGnames.addAll(Arrays.asList(file.getObjectList("cg")));
        for (String index : hasGnames) {                                         // for each class graph name add a attribute
            atts.add(new Attribute(index));
            attVals.add(index);
        }

        atts.add(new Attribute("Class", attVals));                              // fill the attribute class with with given  class graph name

        data = new Instances("train Set for Category Classification ", atts, 0);//create Instances object


       
        vectors = vector.trainingVector(file);                         // take all instance vectors 

        // fill with data

        for (String vi : vectors) {                                               // for each instance

            String[] vectorTable = vi.trim().split(",");

            vals = new double[data.numAttributes()];

            for (int i = 0; i < vectorTable.length - 2; i++) {
                vals[i] = Double.parseDouble(vectorTable[i]);

            }

            vals[vectorTable.length - 1] = attVals.indexOf(vectorTable[vectorTable.length - 1]);                             //Class name


            data.add(new DenseInstance(1.0, vals));                              // add data to Instance


        }

        return data;
    }

    /**
     * Generate the label dataset
     *
     * @param file path for insectDB file
     * @param ClassGname The name for current class
     * @param Ivector the similarity vector between given mail and all class
     * graphs
     * @return the label dataset as instance
     */
    public static Instances labelingSet(INSECTDB file, String Ivector) {

        ArrayList<Attribute> atts;
        ArrayList<String> attVals = new ArrayList<String>();
        Instances data;
        double[] vals;


        atts = new ArrayList<Attribute>();                                      // Set up attributes

        HashSet<String> hasGnames = new HashSet<String>();                      // create a HashSet with all class graph names 
        hasGnames.addAll(Arrays.asList(file.getObjectList("cg")));
        for (String index : hasGnames) {
            atts.add(new Attribute(index));
            attVals.add(index);
        }

        atts.add(new Attribute("Class", attVals));                              // fill the attribute with the given class graph name

        data = new Instances("label Set for Category Classification ", atts, 0);//create Instances object


        //fill with data


        String[] vectorTable = Ivector.trim().split(",");

        vals = new double[data.numAttributes()];
        int count = 0;
        for (String value : vectorTable) {                                          //for each vector
            vals[count] = Double.parseDouble(value);
            count++;
        }

        vals[count] = Utils.missingValue();                                        //add missingValue in place for the class graph name


        data.add(new DenseInstance(1.0, vals));                                   // add data to Instance




        return data;

    }
}
