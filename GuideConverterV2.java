//  Copyright (c) 2020 IBM Corporation and others.
// Licensed under Creative Commons Attribution-NoDerivatives
// 4.0 International (CC BY-ND 4.0)
//   https://creativecommons.org/licenses/by-nd/4.0/
//
// Contributors:
//     IBM Corporation
//


import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class GuideConverterV2 {

    //asks user for the guide name
    public static void main(String[] args) throws Exception {

        String guideName = args[0];
        String branch = args[1];
        getMD(guideName, branch);
        System.out.println("Guide converted");
        System.out.println("Find markdown in " + guideName + ".md");

    }

    // Reads the adoc from github, and writes it to an arraylist
    public static void getMD( String guideName, String branch) {
        try {
            //read adoc file from the open liberty guide
            URL url = new URL("https://raw.githubusercontent.com/openliberty/" + guideName + "/" + branch + "/README.adoc");
            Scanner s = new Scanner(url.openStream());
            //initialise variables
            int Counter = 0;
            int positionNumber = 0;
            String[] startingPhrases = {"//", ":", "[source", "NOTE:", "include::", "[role=", "[.tab_", "image::"};
//          ArrayList for whole text file
            ArrayList<String> listOfLines = new ArrayList<>();

            Properties prop = new Properties();
            Properties props = new Properties();

            FileInputStream ip = new FileInputStream("loopReplacements.properties");
            FileInputStream ips = new FileInputStream("replacements.properties");

            prop.load(ip);
            props.load(ips);


//          stores the start of irrelevant lines
//          String[] startingPhrases = {"//", ":", "[source", "NOTE:", "include::", "[role=", "[.tab_", "image::"};
//          write each line into the file
            while (s.hasNextLine()) {
                listOfLines.add(s.nextLine() + "\n");
            }
            s.close();
            ip.close();
            ips.close();

            // Runs the second class
            functions.second(listOfLines, branch, guideName, prop, props);

            //String builder to format the arraylist
            StringBuilder builder = new StringBuilder();
            for (String value : listOfLines) {
                builder.append(value);
            }

            String text = builder.toString();

            writeToFile(text, guideName);

        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    // append to md file
    public static void writeToFile(String str, String guideName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(guideName + ".md"));
        writer.append("\n" + str);
        writer.close();
    }
}
