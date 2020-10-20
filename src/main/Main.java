package main;

import org.apache.commons.cli.*;

import java.io.File;


/**
 * Driver class. Parses command-line arguments and creates and runs the File Server.
 */
public class Main {

    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_PORT = 65535;
    private static final String PORT_ERROR = "Port out of range. Please select a port in range [0, 65535]";
    private static final String DIR_ERROR = "The path does not correspond to a directory.";

    public static void main(String[] args) {
        /**
         * Parse and validate the user's options.
         * Print appropriate message if invalid.
         */
        Options options = getParserOptions();
        DefaultParser cmdParser = new DefaultParser();
        CommandLine parsedOptions = null;

        try {
            parsedOptions = cmdParser.parse(options, args);

            //checking for irregular options
            var leftover = parsedOptions.getArgList();
            if (!leftover.isEmpty()) {
                System.out.println("\nInvalid options: " + leftover);
                printUsage();
                return;
            }
        }
        catch (ParseException e) {
            System.out.println("\nInvalid options");
            printUsage();
            return;
        }

        /**
         * Validate port
         */
        int port = DEFAULT_PORT;
        if (parsedOptions.hasOption('p')) {
            try {
                port = Integer.parseInt(parsedOptions.getOptionValue('p'));
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number.");
                printUsage();
            }
        }

        if (parsedOptions.hasOption('v')) {
            System.out.println("DEBUG: port number = " + port);
        }

        if (port < 0 || port > MAX_PORT) {
            System.out.println(PORT_ERROR + "\n");
            return;
        }


        /**
         * Validate path
         */
        String rootDir = System.getProperty("user.dir");
        File dir = new File(rootDir);

        if (parsedOptions.hasOption('d')) {
            rootDir = parsedOptions.getOptionValue('d');
            dir = new File(rootDir);
            if (!dir.isDirectory()) {
                System.out.println(DIR_ERROR + "\n" + rootDir);
                return;
            }
        }

        if (! dir.canRead()) {
            System.out.println("Cannot read directory: " + rootDir);
            return;
        }
        else if (! dir.canWrite()) {
            System.out.println("Cannot write to directory: " + rootDir);
            return;
        }

        if (parsedOptions.hasOption('v')) {
            System.out.println("DEBUG: root directory = " +  rootDir);
        }


    }

    static private Options getParserOptions() {


        Option debug = Option.builder("v")
                .required(false)
                .hasArg(false)
                .build();

        Option directory = Option.builder("d")
                .required(false)
                .hasArg()
                .build();

        Option path = Option.builder("p")
                .required(false)
                .hasArg()
                .build();

        return new Options()
                .addOption(debug)
                .addOption(directory)
                .addOption(path);

    }

    private static String usage =
            "\nusage: httpfs [-v] [-p Port] [-d PATH-TO-DIR]\n" +
                    "\n" +
                    "-v   Prints debugging messages\n" +
                    "-p   Specifies the port number that the server will listen and serve at.\n" +
                    "     Default is 8080.\n" +
                    "-d   Specifies the directory that the server will use to read/write requested files.\n" +
                    "     Default is the current directory when launching the application.\n";

    private static void printUsage()  {
        System.out.println(usage);
    }

    private static void printHelp() {
        System.out.println("httpfs is a simple file server." + usage);
    }


}
