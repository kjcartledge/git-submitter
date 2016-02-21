package org.cs1331.gitsubmitter;

import java.awt.Desktop;
import java.net.UnknownHostException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Application {

    private static final String DEFAULT_CONFIG = "json";

    private static final String FORCE_SUBMIT_OPT = "F";
    private static final String OVERRIDE_MESSAGE_OPT = "m";
    private static final String ADD_COLLABS_OPT = "c";
    private static final String OVERRIDE_FILES_OPT = "f";
    private static final String SHOW_SUBMISSION_OPT = "s";
    private static final String REPO_NAME_OPT = "r";
    private static final String USERNAME_OPT = "u";
    private static final String PASSWORD_OPT = "p";

    private static final Options options = createOptions();


    public static void main(String... args) throws Exception {
        LogManager.getLogManager().reset();
        Logger.getLogger("").addHandler(new FileHandler("%t/git-submitter%g.log"));

        CommandLine line = parseArgs(args);

        try {

            String[] files = {"src/main/java"};
            if (line.hasOption(OVERRIDE_FILES_OPT)) {
                files = line.getOptionValues(OVERRIDE_FILES_OPT);
            }
            files = Arrays.stream(files).map(s -> s.replaceAll("'", "")).toArray(String[]::new);

            if (!line.hasOption(FORCE_SUBMIT_OPT)) {
                System.out.print("Compiling files...");
                if (!checkCompile(files)) {
                    System.out.println("\nYour submission does not compile. "
                        + "Please fix your compile errors before proceeding.");
                    System.exit(1);
                }
                System.out.println("...done");
            }

            String repoName = line.getOptionValue(REPO_NAME_OPT);

            String username = null;
            if (line.hasOption(USERNAME_OPT)) {
                username = line.getOptionValue(USERNAME_OPT);
            }

            String password = null;
            if (line.hasOption(PASSWORD_OPT)) {
                password = line.getOptionValue(PASSWORD_OPT);
            }

            AuthenticatedUser user = username != null
                ? AuthenticatedUser.create(username, password)
                : AuthenticatedUser.create();

            StudentSubmission sub = new StudentSubmission(user, repoName);
            sub.createRepo();

            if (line.hasOption(ADD_COLLABS_OPT)) {
                for (String collab : line.getOptionValues(ADD_COLLABS_OPT)) {
                    System.out.println("Adding: " + collab);
                    sub.addCollab(collab);
                }
            }

            String commitMsg = "Solution progress at " + LocalDateTime.now();
            if (line.hasOption(OVERRIDE_MESSAGE_OPT)) {
                commitMsg = line.getOptionValue(OVERRIDE_MESSAGE_OPT);
            }

            boolean success = false;
            try {
                success = sub.pushFiles(commitMsg, files);
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
                System.exit(1);
            }
            if (success) {
                System.out.println("Code submitted successfully!");
                if (line.hasOption(SHOW_SUBMISSION_OPT)) {
                    System.out.println("Launching browser to view repo...");
                    Desktop.getDesktop().browse(new URI(
                        String.format("http://github.gatech.edu/%s/%s",
                            sub.getUser(),
                        sub.getRepo())));
                }
            } else {
                System.out.println("Submission unsuccessful. :(");
            }
        } catch (UnknownHostException ex) {
            System.err.println("Internet connection failed. Please repair your"
                + " connection and try again.");
        } catch (Exception e) {
            System.err.println("Something went wrong. Please check your"
                + " internet connection and that the source files are present."
                + " Call a TA for help if you need further assistance.");

            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean checkCompile(String[] files) throws Exception {
        String combined = Arrays.stream(files).collect(Collectors.joining(" "));
        List<String> args = Arrays.stream(combined.split(" ")).filter(f -> f
            .endsWith(".java")).collect(Collectors.toList());
        if (args.size() == 0) {
            return true;
        }
        args.add(0, "javac");
        ProcessBuilder pcb = new ProcessBuilder(args);
        Process p = pcb.start();
        p.waitFor();
        return p.exitValue() == 0;
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(Option.builder(FORCE_SUBMIT_OPT)
                .longOpt("force")
                .hasArg(false)
                .required(false)
                .desc("force the submission of files even if they do not "
                    + "compile")
                .build());
        options.addOption(Option.builder(OVERRIDE_MESSAGE_OPT)
                .longOpt("message")
                .hasArg(true)
                .optionalArg(false)
                .argName("\"message\"")
                .required(false)
                .desc("override the default commit message with a custom one")
                .build());
        options.addOption(Option.builder(ADD_COLLABS_OPT)
                .longOpt("collabs")
                .hasArgs()
                .optionalArg(false)
                .argName("user1 user2 user3...")
                .required(false)
                .desc("add other users as collaborators to the repo with fork "
                    + "privileges")
                .build());
        options.addOption(Option.builder(OVERRIDE_FILES_OPT)
                .longOpt("files")
                .hasArgs()
                .optionalArg(false)
                .argName("file1 file2 file3...")
                .required(false)
                .desc("submit a list of files instead of all Java source "
                    + "files found via recursive search (default)")
                .build());
        options.addOption(Option.builder(SHOW_SUBMISSION_OPT)
                .longOpt("show")
                .hasArg(false)
                .required(false)
                .desc("attempt to open the submission in a web browser")
                .build());
        options.addOption(Option.builder(REPO_NAME_OPT)
                .longOpt("repo")
                .hasArg(true)
                .optionalArg(false)
                .argName("repository")
                .required()
                .desc("the name of the repository to submit files to")
                .build());
        options.addOption(Option.builder(USERNAME_OPT)
                .longOpt("username")
                .hasArg(true)
                .optionalArg(false)
                .argName("username")
                .required(false)
                .desc("the username of the account to submit files to, if not "
                    + "provided, this value will be prompted for")
                .build());
        options.addOption(Option.builder(PASSWORD_OPT)
                .longOpt("password")
                .hasArg(true)
                .optionalArg(false)
                .argName("password")
                .required(false)
                .desc("the password of the account to submit files to, if not "
                    + "provided, this value will be prompted for")
                .build());
        return options;
    }

    private static void printHelp() {
        final String header = "Submit files to a Github repository. Will "
            + "submit all Java source files found via recursive search as "
            + "long as they compile with a timestamp as the commit message "
            + "if none of -FmcfC are specified.\n\n";
        final String footer = "\nPlease report issues at http://github.com/"
            + "thomas4j/git-submitter";
        final HelpFormatter help = new HelpFormatter();
        help.printHelp("java -jar git-submitter", header, options, footer, true);
    }

    private static CommandLine parseArgs(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
             line = parser.parse(options, args);
        } catch (ParseException e) {
            printHelp();
            System.exit(1);
        }
        return line;
    }
}
