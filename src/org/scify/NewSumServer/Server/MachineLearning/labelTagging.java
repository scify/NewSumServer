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

import org.scify.NewSumServer.Server.MachineLearning.dataSets;
import org.scify.NewSumServer.Server.MachineLearning.vector;
import gr.demokritos.iit.jinsect.storage.INSECTDB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.scify.NewSumServer.Server.Utils.Utilities;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 *
 * @author panagiotis giotis All methods that we need to take the recommended
 * labels from classifier
 *
 */
public class labelTagging {

    /**
     * Find the recommend labels from classifier
     *
     * @return the recommend labels
     */
    public static String recommendation(INSECTDB file, String text) {

        String labelList = "-none-";
        //create IVector
        String Ivector = vector.labellingVector(text, file);                        // take the similarity vectors for each class graph

        try {

            Instances dataTrainSet = dataSets.trainingSet(file);     //take the train  dataset 
            Instances dataLabelSet = dataSets.labelingSet(file, Ivector);//take tha labe  dataset
            ArffSaver saver = new ArffSaver();
            saver.setInstances(dataTrainSet);
            saver.setFile(new File("./data/dataTrainSet.arff"));
            saver.writeBatch();

            ArffSaver saver2 = new ArffSaver();
            saver2.setInstances(dataLabelSet);
            saver2.setFile(new File("./data/dataLabelSet.arff"));
            saver2.writeBatch();

            File temp = File.createTempFile("exportFile", null);
            //TODO: creat classifier

//            String option = "-S 2 -K 2 -D 3 -G 0.0 -R 0.0 -N 0.5 -M 40.0 -C 1.0 -E 0.001 -P 0.1"; // classifier options
//            String[] options = option.split("\\s+");

            if (dataTrainSet.classIndex() == -1) {
                dataTrainSet.setClassIndex(dataTrainSet.numAttributes() - 1);
            }

            // Create a  classifier LibSVM

//            NaiveBayes nb = new NaiveBayes();
//            RandomForest nb = new RandomForest();
           J48 nb = new J48();
//            nb.setOptions(options);
            nb.buildClassifier(dataTrainSet);

            // End train method

            if (dataLabelSet.classIndex() == -1) {
                dataLabelSet.setClassIndex(dataLabelSet.numAttributes() - 1);
            }


            StringBuffer writer = new StringBuffer();

            PlainText output = new PlainText();
            output.setBuffer(writer);
            output.setHeader(dataLabelSet);
            output.printClassifications(nb, dataLabelSet);

//            PrintStream ps2 = new PrintStream(classGname);
//            ps2.print(writer.toString());
//            ps2.close();
            PrintStream ps = new PrintStream(temp);                                //Add to temp file the results of classifying
            ps.print(writer.toString());
            ps.close();

            //TODO: export result
//            labelList = result(temp);                                                    //if result is true adds the current class graph name in label list
            labelList = result(temp) + " --------->> " + text;                                                    //if result is true adds the current class graph name in label list
            Utilities.appendToFile(labelList);
            

        } catch (Exception ex) {
            Logger.getLogger(labelTagging.class.getName()).log(Level.SEVERE, null, ex);
        }


        return labelList;
    }

    /**
     * find if classifier give to mail the current class graph name or not
     *
     * @param tmp the temp file which contains the results of classifying
     * @return true if classifier gave class name false if not
     */
    public static String result(File tmp) {
        String label = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(tmp));

            String strLine = br.readLine();
            String[] split1 = strLine.trim().split("\\s+");
            String[] split2 = split1[2].trim().split(":");

//            label = split2[1];
            label = strLine;

        } catch (IOException ex) {
            Logger.getLogger(labelTagging.class.getName()).log(Level.SEVERE, null, ex);
        }

        return label;
    }
}
