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

import gr.demokritos.iit.jinsect.storage.INSECTDB;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;

/**
 *Contains important utilities which we use to do the mail classifications
 * @author panagiotis giotis 
 * 
 */
public class util {
    
    
    
     /**
     * Change characters from a given string
     *
     * @param input The input string that we want to change a character
     * @param charFirst The primary char that we want to change
     * @param charLast The finally char that we wish to replace the primary char
     * @return
     */
    public static String changeChar(String input, String charFirst, String charLast) {
        String newString = "-none-";

        String[] stringTable = input.trim().split(charFirst);
        for (String index : stringTable) {
            if (newString.equals("-none-")) {
                newString = index;
            } else {
                newString = newString + charLast + index;
            }

        }


        return newString;
    }
    
    
    /**
     * Process to delete instance 
     * @param id The mail id from instance that i want to delete
     */
    public static void deleteInstanse(String id,INSECTDB file){
        
        file.deleteObject(id, "ig");                                                // delete the instance graph
        removeLineFromFile(id);                                                     // delete instance info from the info.txt
        
    }
    
    /**
     * remove the instance record of given id
     * @param idToRemove  the mail id that i want to delete
     */
    public  static void removeLineFromFile(String idToRemove) {
    
    try {
          String file = "./data/MachineLearningData/info.txt";
          File inFile = new File(file);


          File tempFile = new File(inFile.getAbsolutePath() + ".tmp");              //Construct the new file that will later be renamed to the original filename.

          BufferedReader br = new BufferedReader(new FileReader(file));
          PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
          
          String line = null;



          while ((line = br.readLine()) != null) {                                  //Read from the original file and write to the new. unless content matches data to be removed.


             String[] recordTable= line.trim().split("\\s+");
             String recordId = recordTable[0];

            if (recordId.equals(idToRemove)) {
                    continue;
            }
            pw.println(line);
            pw.flush();
          }
          pw.close();
          br.close();


          if (!inFile.delete()) {                                                   //Delete the original file
            System.out.println("Could not delete file");
            return;
          }


          if (!tempFile.renameTo(inFile))                                           //Rename the new file to the filename the original file had.
            System.out.println("Could not rename file");

    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }
    
     
  
    /**
     * Make validation between the confirmation label and the label that {@link labelTagging labelTagging} method gave
     * @param id  The unique mail id 
     * @param ConfLabels a HashSet with the labels that client gave as a confirm
     * @return  A HashSet with labels  which no contained in the record
     */
    public static HashSet<String> validation(String id, HashSet<String> confLabels){

        
        try{
          
          FileInputStream fstream = new FileInputStream("./data/MachineLearningData/info.txt");
          DataInputStream in = new DataInputStream(fstream);
          BufferedReader br = new BufferedReader(new InputStreamReader(in));
          String strLine;
          HashSet<String> hasRecordLabels = new HashSet<String>();                  //create a HashSet with all labels from info record
          HashSet<String> tempHas = new HashSet<String>();                          //create a temp hashSet with the given confLabels
          tempHas=(HashSet<String>) confLabels.clone();
          while ((strLine = br.readLine()) != null){                                // read line by line the info record file
              
             
             String[] recordTable= strLine.trim().split("\\s+");
              
              if (id.equals(recordTable[0])){
                    
                    hasRecordLabels.addAll(Arrays.asList(recordTable[1].trim().split(":")));
                    
                    for(String confLabel:confLabels){
                        if(hasRecordLabels.contains(confLabel)){
                          tempHas.remove(confLabel);                                //remove the label from temp hashset 
                        }
                    }
                    confLabels=tempHas;
                    break;
              } 
  
          }
          in.close();
          return confLabels;
         }catch (IOException e){
                e.printStackTrace();
                System.err.println("Error: " + e.getMessage());
          }
        
        
        
        return confLabels;
        
    }
    /**
     * Find a registered info for the specific id
     * @param id The id for the record that i want
     * @return The line from registered info
     */
    public static String recordLine(String id){

        String strLine,record = null;
        try{
          
          FileInputStream fstream = new FileInputStream("./data/MachineLearningData/info.txt");
          DataInputStream in = new DataInputStream(fstream);
          BufferedReader br = new BufferedReader(new InputStreamReader(in));
          
         
          while ((strLine = br.readLine()) != null){                                // read line by line
              
             
             String[] recordTable= strLine.trim().split(":");
              
              if (id.equals(recordTable[0])){
                 record=strLine;                                                    // if find id then take the line and put it in the record
                 break;                                                             // break while loop when find id
              } 
  
          }
          in.close();
          return record;
         }catch (IOException e){
                e.printStackTrace();
                System.err.println("Error: " + e.getMessage());
          }
        
        
        
        return record;
        
    }
    
    
    
}
