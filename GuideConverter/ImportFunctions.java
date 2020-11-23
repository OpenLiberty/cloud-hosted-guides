/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

class ImportFunctions {

    // inserts gitclone.aoc from https://github.com/OpenLiberty/guides-common
    public static void clone(ArrayList<String> listOfLines, String guideName, int i, String CommonURL) {
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL(CommonURL);
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            int counter = 0;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";
                if (inputLine.startsWith("----")) {
                    counter++;
                }
                if (counter == 1) {
                    inputLine = inputLine.replaceAll("----", "```");
                }
                if (counter == 2) {
                    inputLine = inputLine.replaceAll("----", "```\n{: codeblock}\n");
                }
                inputLine = inputLine.replace("guide-{projectid}", guideName);
                temp.add(inputLine);
            }
            temp.subList(0, 7).clear();
            listOfLines.addAll(i + 1, temp);

            s.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts try what you build-intro from  https://github.com/OpenLiberty/guides-common
    public static void OtherGuidesCommon(ArrayList<String> listOfLines, String guideName, int i, String CommonURL) {
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL(CommonURL);
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1 , temp);
            s.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
}
