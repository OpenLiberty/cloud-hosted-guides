import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.util.Scanner;
import java.util.*;

public class GuideConverter {

    //asks user for the guide name
    public static void main(String[] args) throws Exception {

        String guideName = args[0];
        getMD(guideName);
        System.out.println("Guide converted");
        System.out.println("Find markdown in " + guideName + ".md");

    }

    // inserts gitclone.aoc from https://github.com/OpenLiberty/guides-common
    public static void clone(String guideName) {
        try {
            String addition = "\n# Getting Started\n\nIf a terminal window does not open navigate:\n\n> Terminal -> New Terminal\n\nCheck you are in the **home/project** folder:\n\n```\npwd\n```\n{: codeblock}\n\nThe fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:\n\n```\ngit clone https://github.com/openliberty/" + guideName + ".git\ncd " + guideName + "\n```\n{: codeblock}\n\nThe **start** directory contains the starting project that you will build upon.\n";
            writeToFile(addition, guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts ol-kernel-docker-pull.adoc from https://github.com/OpenLiberty/guides-common
    public static void kernelPull(String guideName) {
        try {
            String addition = "\nRun the following command to download or update to the latest **openliberty/open-liberty:kernel-java8-openj9-ubi** Docker image:\n\n```\ndocker pull openliberty/open-liberty:kernel-java8-openj9-ubi\n```\n{: codeblock}\n\n";
            writeToFile(addition, guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts devmode-start.adoc from https://github.com/OpenLiberty/guides-common
    public static void devMode(String guideName) {
        try {
            String dev = "When you run Open Liberty in dev mode, the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start in dev mode:\n\n```\nmvn liberty:dev\n```\n{: codeblock}\n\nAfter you see the following message, your application server in dev mode is ready:\n\n```\nPress the Enter key to run tests on demand.\n```\n\nDev mode holds your command line to listen for file changes. Open another command line to continue, or open the project in your editor.\n";
            writeToFile(dev, guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts twyb-end.adoc from  https://github.com/OpenLiberty/guides-common
    public static void end(String guideName) {
        try {
            String dev = "After you are finished checking out the application, stop the Open Liberty server by pressing `CTRL+C` in the shell session where you ran the server. Alternatively, you can run the `liberty:stop` goal from the `finish` directory in another shell session:\n\n```\nmvn liberty:stop\n```\n{: codeblock}";
            writeToFile(dev, guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts trywhatyoubuild-intro.adoc from  https://github.com/OpenLiberty/guides-common
    public static void tryBuildIntro(String guideName) {
        try {
            String dev = "## Try what you’ll build\nThe **finish** directory in the root of this guide contains the finished application. Give it a try before you proceed.\n\nTo try out the application, first navigate to the finish directory and then run the following Maven goal to build the application and run it inside Open Liberty:\n```\ncd finish\nmvn install liberty:start-server\n```\n{: codeblock}\n\n";
            writeToFile(dev, guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts trywhatyoubuild-intro.adoc from  https://github.com/OpenLiberty/guides-common
    public static void tryBuildEnd(String guideName) {
        try {
            String dev = "After you are done checking out the application, stop the Open Liberty server:\n\n```\nmvn liberty:stop-server\n```\n{: codeblock}";
            writeToFile(dev, guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts devmode-quit from  https://github.com/OpenLiberty/guides-common
    public static void devEnd(String guideName) {
        try {
            String end = "When you are done checking out the service, exit development mode by pressing `CTRL+C` in the shell session where you ran the server, or by typing `q` and then pressing the `enter/return` key.";
            writeToFile(end, guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //creates command for navigation
    public static void start(String guideName) {
        try {
            String startNav = "Navigate to the **start** directory to begin.\n\n```\ncd start\n```\n{: codeblock}\n\n";
            writeToFile(startNav, guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public static void removeLast(String guideName) {
        try {
            java.io.RandomAccessFile file = new java.io.RandomAccessFile(guideName + ".md", "rw");
            byte b = 0;
            long pos = file.length();
            while (b != '\n' && --pos >= 0) {
                file.seek(pos);
                b = file.readByte();
            }
            file.seek(++pos);
            file.setLength(pos);
            file.write("".getBytes());
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //configures instructions to replace file
    public static String replace(String inputLine, String guideName) {
        try {
            inputLine = inputLine.replaceAll("#", "");
            inputLine = inputLine.replaceAll("`", "**");
            writeToFile("\n> [File -> Open]" + guideName + "/start/" + inputLine.replaceAll("\\*\\*", "") + "\n\n\n", guideName);
            writeToFile("\n", guideName);
            writeToFile("```", guideName);
            codeSnippet(inputLine.replaceAll("\\*\\*", ""), guideName);
            String position = "main";
            return position;
        } catch (IOException ex) {
            System.out.println(ex);
            return "";
        }
    }

    //configures instructions to udate file
    public static String update(String inputLine, String guideName) {
        try {
            inputLine = inputLine.replaceAll("#", "");
            inputLine = inputLine.replaceAll("`", "**");
            writeToFile("\n> [File -> Open]" + guideName + "/start/" + inputLine.replaceAll("\\*\\*", "") + "\n\n\n", guideName);
            writeToFile("\n", guideName);
            writeToFile("```", guideName);
            codeSnippet(inputLine.replaceAll("\\*\\*", ""), guideName);
            String position = "main";
            return position;
        } catch (IOException ex) {
            System.out.println(ex);
            return "";
        }
    }

    //configures instructions to create file
    public static String touch(String inputLine, String guideName) {
        try {
            writeToFile("```", guideName);
            inputLine = "touch " + inputLine;
            inputLine = inputLine.replaceAll("`", "");
            writeToFile(inputLine, guideName);
            writeToFile("```", guideName);
            writeToFile("{: codeblock}\n\n", guideName);
            writeToFile("> [File -> Open]" + guideName + "/start/" + inputLine.replaceAll("touch ", "") + "\n\n\n\n```\n", guideName);
            codeSnippet(inputLine.replaceAll("touch ", ""), guideName);
            String position = "main";
            return position;

        } catch (IOException ex) {
            System.out.println(ex);
            return "";
        }
    }

    //configures link
    public static void link(String inputLine, String guideName) {
        try {
            System.out.println(inputLine);
            String linkParts[] = new String[2];
            String findLink[];
            String link;
            String description;
            String formattedLink;
            String localhostSplit[];
            String findDescription[];
            inputLine = inputLine.replaceAll("\\{", "");
            inputLine = inputLine.replaceAll("\\}", "");
            linkParts = inputLine.split("\\[");
            findDescription = linkParts[1].split("\\^");
            description = findDescription[0];
            findLink = linkParts[0].split(" ");
            link = findLink[findLink.length - 1];
            if (link.contains("localhost")) {
                if (inputLine.contains(".")) {
                    localhostSplit = inputLine.split("\\.");
                    System.out.println(localhostSplit[0]);
                    inputLine = inputLine.replaceAll(link + "\\[" + description + "\\^\\]", "");
                    inputLine = localhostSplit[0] + localhostSplit[1] + ("\n```\ncurl " + link + "\n```\n{: codeblock}\n\n");

                    writeToFile(inputLine, guideName);
                    return;
                } else {
                    inputLine = inputLine.replaceAll(link + "\\[" + description + "\\^\\]", ("\n```\ncurl " + link + "\n```\n{: codeblock}\n\n"));
                }
            }
            formattedLink = "[" + description + "](" + link + ")";
            inputLine = inputLine.replaceAll(link + "\\[" + description + "\\^\\]", formattedLink);
            writeToFile(inputLine, guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    // general text configuration
    public static void main(String inputLine, String guideName) {
        try {
            if (!inputLine.equals("[.hidden]")) {
                if (!inputLine.equals("irrelevant")) {
                    if (inputLine.equals("----")) {
                        writeToFile("```", guideName);
                    } else {
                        inputLine = inputLine.replaceAll("`", "**");
                        inputLine = inputLine.replaceAll("â””â", "|");
                        inputLine = inputLine.replaceAll("”€â”€", "__");
                        inputLine = inputLine.replaceAll("â”œâ", "  |");
                        inputLine = inputLine.replaceAll("â”‚", "");
                        inputLine = inputLine.replaceAll("â€™", "`");
                        inputLine = inputLine.replaceAll("\\[hotspot(.*?)\\]", "");
                        inputLine = inputLine.replaceAll("`", "\'");
                        inputLine = inputLine.replaceAll("�", "\"");
                        inputLine = inputLine.replaceAll("â€", "");
                        inputLine = inputLine.replaceAll("--", "```");

                        if (inputLine.equals("******")) {
                            inputLine = "```";
                        }
                        if (inputLine.startsWith("== ")) {
                            if (!inputLine.equals("== What you'll learn")) {
                                inputLine = inputLine.replaceAll("==", "#");
                            }
                        }
                        if (inputLine.startsWith("==")) {
                            inputLine = inputLine.replaceAll("=", "#");
                        }
                        writeToFile(inputLine, guideName);
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts code snippet
    public static void codeSnippet(String path, String guideName) {
        try {
            String httpsURL = "https://raw.githubusercontent.com/openliberty/" + guideName + "/master/finish/" + path;
            String FILENAME = "temp.adoc";
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME));
            URL myurl = new URL(httpsURL);
            HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
            InputStream ins = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(ins, "Windows-1252");
            BufferedReader in = new BufferedReader(isr);
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (!inputLine.replaceAll(" ", "").startsWith("/")) {
                    if (!inputLine.startsWith("*")) {
                        if (!inputLine.startsWith(" *")) {
                            if (!inputLine.startsWith("#")) {
                                writeToFile(inputLine, guideName);
                            }
                        }
                    }
                }
            }
            writeToFile("```\n{: codeblock}\n\n", guideName);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //configures table UNFINISHED
    public static String table(String inputLine, String guideName) {
        try {
            if (inputLine.equals("|===")) {
                return "main";
            }
            if (inputLine.startsWith("     ")) {
                inputLine = inputLine.replaceAll("                            ", "");
                writeToFile(inputLine, guideName);
                return "main";
            }
            if (inputLine.startsWith("| *")) {
                int count = 0;
                for (int i = 0; i < inputLine.length(); i++) {
                    if (inputLine.charAt(i) == '*') {
                        count += 1;
                    }
                }
                writeToFile(inputLine, guideName);
                String split = "";
                for (int j = 0; j < count / 2; j++) {
                    split = split + "|---";
                }
                writeToFile(split, guideName);
                return "table";
            }
            writeToFile(inputLine, guideName);
            return "table";
        } catch (IOException ex) {
            return "";
        }
    }

    public static void getMD(String guideName) {
        try {
            //read adoc file from the open liberty guide
            String httpsURL = "https://raw.githubusercontent.com/openliberty/" + guideName + "/master/README.adoc";
            String FILENAME = "temp.adoc";
            BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME));
            URL myurl = new URL(httpsURL);
            HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
            InputStream ins = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(ins, "Windows-1252");
            BufferedReader in = new BufferedReader(isr);
            //initialise variables
            String position = "";
            String inputLine;
            int Counter = 0;
            int positionNumber = 0;
            ArrayList<String> list = new ArrayList<String>();
            //stores the start of irrelevant lines
            String[] startingPhrases = {"//", ":", "[source", "NOTE:", "include::", "[role=", "[.tab_", "image::"};
            //write each line into the file
            //write each line into the file
            while ((inputLine = in.readLine()) != null) {

                // Removes references to images
                if (inputLine.indexOf("diagram") != -1) {
                    int BUFFER_SIZE = 1000;

                    in.mark(BUFFER_SIZE);

                    for (int i = 0; i <= 5; i++) {
                        String temp = new String();
                        temp = in.readLine();

                        list.add(temp);
                    }
                    for (String i : list) {
                        if (i.startsWith("image::")) {
                            in.reset();

                            if (inputLine.indexOf("diagram") != -1) {
                                String noDiagram = inputLine.substring(0, inputLine.indexOf("."));
                                inputLine = inputLine.replace(inputLine, noDiagram + ".");
                                while (!in.readLine().startsWith("image::")) {
                                    continue;
                                }
                            }
                        }
                    }
                }

                //Removes Additional prerequisites section
                if (inputLine.startsWith("== Additional prerequisites")) {
                    while (!in.readLine().startsWith("[role='command']")) {
                        continue;
                    }
                    continue;
                }

                //configure table
                if (position.equals("table")) {
                    position = table(inputLine, guideName);
                    continue;
                }

                //Skip instructions for windows command
                if (position.equals("windows")) {
                    if (positionNumber == 5) {
                        position = "main";
                        continue;
                    } else {
                        positionNumber += 1;
                        continue;
                    }
                }

                //Current line is an example output of a mvn test
                if (position.equals("testBlock")) {
                    if (!inputLine.startsWith("[INFO]")) {
                        writeToFile("```\n", guideName);
                        position = "main";
                    } else {
                        writeToFile(inputLine, guideName);
                    }
                    continue;
                }

                //Identifies an instruction for windows only and skips the current line
                if (inputLine.equals("[.tab_content.windows_section]")) {
                    position = "windows";
                    positionNumber = 0;
                    continue;
                }

                //Identifies that line is the start of an example output of a mvn test
                if (inputLine.startsWith("[INFO]")) {
                    position = "testBlock";
                    writeToFile("```\n" + inputLine, guideName);
                    continue;
                }

                //Identifies that line is the start of a code block
                if (inputLine.equals("```")) {
                    position = "code";
                    continue;
                }

                //Identifies that line is the start of a table
                if (inputLine.equals("|===")) {
                    position = "table";
                    continue;
                }

                //Skips over lines with ----
                if (inputLine.equals("----")) {
                    continue;
                }

                //Line is part of a code block
                if (position.equals("code")) {
                    if (inputLine.equals("----")) {
                        writeToFile("```", guideName);
                        continue;
                    }
                    if (inputLine.equals("--")) {
                        writeToFile("```\n{: codeblock}\n", guideName);
                        continue;
                    }
                    //Code is a mvn instruction
                    else if (inputLine.startsWith("mvn")) {
                        //Adds copy button
                        writeToFile("```\n" + inputLine + "\n```\n{: codeblock}\n\n", guideName);
                        continue;
                    }
                    //Identifies end of code block so carries out formatting
                    else {
                        position = "main";
                    }
                }

                //Finds title so we skip over irrelevant lines
                if (inputLine.startsWith("= ")) {
                    writeToFile(inputLine.replaceAll("=", "#"), guideName);
                    position = "intro";
                }

                //Identifies another heading after the intro so we stop skipping over lines
                if (inputLine.startsWith("== ")) {
                    position = "main";
                }


                if (inputLine.startsWith("[source")) {
                    removeLast(guideName);
                    continue;
                }

                //User is instructed to replace a file
                if (inputLine.startsWith("#Replace")) {
                    writeToFile(inputLine.replaceAll("#", ""), guideName);
                    position = "replacePath"; //next lines need configuring so position is changed
                    continue;
                }

                //User is instructed to create a file
                if (inputLine.startsWith("#Create")) {
                    writeToFile(inputLine.replaceAll("#", ""), guideName);
                    position = "new"; //next lines need configuring so position is changed
                    continue;
                }

                //User is instructed to update a file
                if (inputLine.startsWith("#Update")) {
                    writeToFile(inputLine.replaceAll("#", ""), guideName);
                    position = "update"; //next lines need configuring so position is changed
                    continue;
                }

                //Skips line, resets positionNumber and configures the replacement instructions
                if (position.equals("replacePath")) {
                    positionNumber = 0;
                    position = replace(inputLine, guideName);
                    continue;
                }

                //Skips line, resets positionNumber and configures the instructions for a new file
                if (position.equals("new")) {
                    positionNumber = 0;
                    position = touch(inputLine, guideName);
                    continue;
                }

                //Skips line, resets positionNumber and configures the instructions for a file update
                if (position.equals("update")) {
                    positionNumber = 0;
                    position = update(inputLine, guideName);
                    continue;
                }

                //Identifies a link in the file line and configures it
                if (inputLine.contains("^]")) {
                    link(inputLine, guideName);
                    continue;
                }

                //identfies if instructions from github.com/OpenLiberty/guides-common need to be inserted
                if (inputLine.equals("include::{common-includes}/ol-kernel-docker-pull.adoc[]")) {
                    kernelPull(guideName);
                }

                if (inputLine.equals("include::{common-includes}/devmode-start.adoc[]")) {
                    devMode(guideName);
                }

                if (inputLine.equals("include::{common-includes}/twyb-end.adoc[]")) {
                    end(guideName);
                }

                if (inputLine.equals("include::{common-includes}/gitclone.adoc[]")) {
                    clone(guideName);
                }

                if (inputLine.equals("include::{common-includes}/devmode-quit.adoc[]")) {
                    devEnd(guideName);
                }

                if (inputLine.equals("include::{common-includes}/twyb-intro.adoc[]")) {
                    tryBuildIntro(guideName);
                }

                if (inputLine.equals("include::{common-includes}/twyb-end.adoc[]")) {
                    tryBuildEnd(guideName);
                }

                //Identifies the start of a table
                if (inputLine.startsWith("[cols")) {
                    position = "table";
                    continue;
                }

                //Identifies the start of a table
                if (inputLine.equals("Navigate to the `start` directory to begin.")) {
                    start(guideName);
                    continue;
                }

                //compares line with the irrelevant ones in startingPhrases
                for (int i = 0; i < startingPhrases.length; i++) {
                    if (inputLine.startsWith(startingPhrases[i])) {
                        inputLine = "irrelevant";
                        break;
                    }
                }

                //skips line if it is irrelevant
                if (inputLine.equals("irrelevant")) {
                    continue;
                }

                //end of guidee
                if (inputLine.startsWith("== Great work!")) {
                    String finish = "# Summary\n\n## Clean up your environment\n\nDelete the **" + guideName + "** project by navigating to the **/home/project/** directory\n\n```\ncd ../..\nrm -r -f " + guideName + "\nrmdir " + guideName + "\n```\n{: codeblock}\n\n";
                    writeToFile(finish, guideName);
                }

                //inputLine contains info that needs general configuration and is not a special case
                if (position.equals("main")) {
                    main(inputLine, guideName);
                }

            }
            in.close();
            bw.close();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    // append to md file
    public static void writeToFile(String str, String guideName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("instructions/"+guideName+"/"+"instructions.md", true));
        writer.append("\n" + str);
        writer.close();
    }
}
