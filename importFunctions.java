//  Copyright (c) 2020 IBM Corporation and others.
// Licensed under Creative Commons Attribution-NoDerivatives
// 4.0 International (CC BY-ND 4.0)
//   https://creativecommons.org/licenses/by-nd/4.0/
//
// Contributors:
//     IBM Corporation
//

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

class importFunctions {

    // inserts gitclone.aoc from https://github.com/OpenLiberty/guides-common
    public static void clone(ArrayList<String> listOfLines, String guideName, int i) {
//        String addition = "\n# Getting Started\n\nIf a terminal window does not open navigate:\n\n> Terminal -> New Terminal\n\nCheck you are in the **home/project** folder:\n\n```\npwd\n```\n{: codeblock}\n\nThe fastest way to work through this guide is to clone the Git repository and use the projects that are provided inside:\n\n```\ngit clone https://github.com/openliberty/" + guideName + ".git\ncd " + guideName + "\n```\n{: codeblock}\n\nThe **start** directory contains the starting project that you will build upon.\n";
//        listOfLines.set(i, addition);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/gitclone.adoc");
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
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts ol-kernel-docker-pull.adoc from https://github.com/OpenLiberty/guides-common
    public static void kernelPull(ArrayList<String> listOfLines, String guideName, int i) {
//        String addition = "\nRun the following command to download or update to the latest **openliberty/open-liberty:kernel-java8-openj9-ubi** Docker image:\n\n```\ndocker pull openliberty/open-liberty:kernel-java8-openj9-ubi\n```\n{: codeblock}\n\n";
//        listOfLines.set(i, addition);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/ol-kernel-docker-pull.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts devmode-start.adoc from https://github.com/OpenLiberty/guides-common
    public static void devMode(ArrayList<String> listOfLines, String guideName, int i) {
//        String dev = "When you run Open Liberty in dev mode, the server listens for file changes and automatically recompiles and deploys your updates whenever you save a new change. Run the following goal to start in dev mode:\n\n```\nmvn liberty:dev\n```\n{: codeblock}\n\nAfter you see the following message, your application server in dev mode is ready:\n\n```\nPress the Enter key to run tests on demand.\n```\n\nDev mode holds your command line to listen for file changes. Open another command line to continue, or open the project in your editor.\n";
//        listOfLines.set(i, dev);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/devmode-start.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts twyb-end.adoc from  https://github.com/OpenLiberty/guides-common
    public static void end(ArrayList<String> listOfLines, String guideName, int i) {
//        String dev = "After you are finished checking out the application, stop the Open Liberty server by pressing `CTRL+C` in the shell session where you ran the server. Alternatively, you can run the `liberty:stop` goal from the `finish` directory in another shell session:\n\n```\nmvn liberty:stop\n```\n{: codeblock}\n";
//        listOfLines.set(i, dev);
        ArrayList<String> temp = new ArrayList<>();
        String codeblock = "{: codeblock}\n\n";
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/twyb-end.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            temp.add(codeblock);
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts trywhatyoubuild-intro.adoc from  https://github.com/OpenLiberty/guides-common
    public static void tryBuildIntro(ArrayList<String> listOfLines, String guideName, int i) {
//        String dev = "## Try what you’ll build\nThe **finish** directory in the root of this guide contains the finished application. Give it a try before you proceed.\n\nTo try out the application, first navigate to the **finish** directory and then run the following Maven goal to build the application and run it inside Open Liberty:\n```\ncd finish\nmvn install liberty:start-server\n```\n{: codeblock}\n\n";
//        listOfLines.set(i, dev);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/twyb-intro.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";
                if (!inputLine.startsWith("//")) {

                    temp.add(inputLine);
                }
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts trywhatyoubuild-intro.adoc from  https://github.com/OpenLiberty/guides-common
    public static void tryBuildEnd(ArrayList<String> listOfLines, String guideName, int i) {
//        String dev = "After you are done checking out the application, stop the Open Liberty server:\n\n```\nmvn liberty:stop-server\n```\n{: codeblock}\n";
//        listOfLines.set(i, dev);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/twyb-end.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public static void devEnd(ArrayList<String> listOfLines, String guideName, int i) {
//        String end = "When you are done checking out the service, exit development mode by pressing `CTRL+C` in the shell session where you ran the server, or by typing `q` and then pressing the `enter/return` key.\n";
//        listOfLines.set(i, end);
        ArrayList<String> temp = new ArrayList<>();

        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/devmode-quit.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts devTest from  https://github.com/OpenLiberty/guides-common
    public static void devTest(ArrayList<String> listOfLines, String guideName, int i) {
//        String end = "### Running the tests\n\nSince you started Open Liberty in dev mode, press the enter/return key to run the tests.\n";
//        listOfLines.set(i, end);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/devmode-test.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts dev mode-build from  https://github.com/OpenLiberty/guides-common
    public static void devmodeBuild(ArrayList<String> listOfLines, String guideName, int i) {
//        String end = "### Running the tests\n\nSince you started Open Liberty in dev mode, press the enter/return key to run the tests.\n";
//        listOfLines.set(i, end);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/devmode-build.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts dev mode start-cd from  https://github.com/OpenLiberty/guides-common
    public static void devmodeStartCD(ArrayList<String> listOfLines, String guideName, int i) {
//        String end = "### Running the tests\n\nSince you started Open Liberty in dev mode, press the enter/return key to run the tests.\n";
//        listOfLines.set(i, end);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/devmode-start-cd.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts try what you build-beg from  https://github.com/OpenLiberty/guides-common
    public static void tryWhatYouBuildBeg(ArrayList<String> listOfLines, String guideName, int i) {
//        String end = "### Running the tests\n\nSince you started Open Liberty in dev mode, press the enter/return key to run the tests.\n";
//        listOfLines.set(i, end);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/trywhatyoubuild-beg.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //inserts try what you build-intro from  https://github.com/OpenLiberty/guides-common
    public static void tryWhatYouBuildIntro(ArrayList<String> listOfLines, String guideName, int i) {
//        String end = "### Running the tests\n\nSince you started Open Liberty in dev mode, press the enter/return key to run the tests.\n";
//        listOfLines.set(i, end);
        ArrayList<String> temp = new ArrayList<>();
        try {
            URL url = new URL("https://raw.githubusercontent.com/OpenLiberty/guides-common/master/trywhatyoubuild-intro.adoc");
            Scanner s = new Scanner(url.openStream());
            String inputLine = null;
            while (s.hasNextLine()) {
                inputLine = s.nextLine() + "\n";

                temp.add(inputLine);
            }
            listOfLines.addAll(i + 1, temp);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    //creates command for navigation
    public static void start(ArrayList<String> listOfLines, String guideName, int i) {
        String startNav = "Navigate to the **start** directory to begin.\n\n```\ncd start\n```\n{: codeblock}\n\n";
        listOfLines.set(i, startNav);
    }
}
