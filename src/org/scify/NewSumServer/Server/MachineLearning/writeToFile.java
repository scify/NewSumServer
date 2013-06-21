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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class writeToFile {

    /**
     * creates a txt file to keep info records for all mail that we receive
     *
     * @param infoRecord The info record that it contains the info that i want
     * to save.\
     * @param appendToFile Is a boolean variable to inform as if the file
     * already exists
     */
    public static String createTxtFile(String infoRecord, boolean appendToFile) {

        PrintWriter pw = null;
        String sUUID = String.valueOf(UUID.randomUUID());
        try {

            if (appendToFile) {

                pw = new PrintWriter(new FileWriter("./data/MachineLearningData/info.txt", true));  //If the file already exists, start writing at the end of it.

            } else {

                pw = new PrintWriter(new FileWriter("./data/MachineLearningData/info.txt"));        //this is equal to:pw = new PrintWriter(new FileWriter(filename, false));                 

            }

            pw.println(sUUID + ":" + infoRecord);                                          // write to info.txt the info record = id:label:
            pw.flush();


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Close the PrintWriter
            if (pw != null) {
                pw.close();
            }
            return sUUID;
        }

    }
}
