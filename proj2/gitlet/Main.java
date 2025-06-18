package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Sihao Wong
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                initHandler(args);
                break;
            case "add":
                addHandler(args);
                break;
            case "commit":
                commitHandler(args);
                break;
            case "rm":
                rmHandler(args);
                break;
            case "log":
                logHandler(args);
                break;
            case "global-log":
                globalLogHandler(args);
                break;
            case "find":
                findHandler(args);
                break;
            case "status":
                statusHandler(args);
                break;
            case "checkout":
                checkoutHandler(args);
                break;
            case "branch":
                branchHandler(args);
                break;
            case "rm-branch":
                rmBranchHandler(args);
                break;
            case "reset":
                resetHandler(args);
                break;
            case "merge":
                mergeHandler(args);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }


    public static void initHandler(String[] args) {
        if (args.length == 1 && args[0].equals("init")) {
            Repository.glInit();
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void addHandler(String[] args) {
        if (args.length == 2 && args[0].equals("add")) {
            Repository.stagedForAddition(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void commitHandler(String[] args) {

        if (args.length == 2 && args[0].equals("commit")) {
            String commitMessage = args[1];
            if (commitMessage.trim().isEmpty()) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            Repository.commitCommands(commitMessage);
        } else if (args.length == 1 && args[0].equals("commit")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void rmHandler(String[] args) {
        if (args.length == 2 && args[0].equals("rm")) {
            Repository.stagedForRemoval(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void logHandler(String[] args) {
        if (args.length == 1 && args[0].equals("log")) {
            Repository.printLog();
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void globalLogHandler(String[] args) {
        if (args.length == 1 && args[0].equals("global-log")) {
            Repository.printGlobalLog();
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }


    public static void findHandler(String[] args) {
        if (args.length == 2 && args[0].equals("find")) {
            Repository.find(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void statusHandler(String[] args) {
        if (args.length == 1 && args[0].equals("status")) {
            Repository.status();
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void checkoutHandler(String[] args) {
        if (args.length == 3 && args[1].equals("--")) {
            Repository.checkoutFileFromHead(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            Repository.checkoutFileFromCommit(args[1], args[3]);
        } else if (args.length == 2) {
            Repository.checkoutBranch(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void branchHandler(String[] args) {
        if (args.length == 2 && args[0].equals("branch")) {
            String branchName = args[1];
            if (branchName.trim().isEmpty()) {
                System.out.println("Please enter a branch name.");
                System.exit(0);
            }
            Repository.branch(branchName);
        } else if (args.length == 1 && args[0].equals("branch")) {
            System.out.println("Please enter a branch name.");
            System.exit(0);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void rmBranchHandler(String[] args) {
        if (args.length == 2 && args[0].equals("rm-branch")) {
            String branchName = args[1];
            if (branchName.trim().isEmpty()) {
                System.out.println("Please enter a branch name.");
                System.exit(0);
            }
            Repository.rmBranch(branchName);
        } else if (args.length == 1 && args[0].equals("branch")) {
            System.out.println("Please enter a branch name.");
            System.exit(0);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void resetHandler(String[] args) {
        if (args.length == 2 && args[0].equals("reset")) {
            String commitMessage = args[1];
            if (commitMessage.trim().isEmpty()) {
                System.out.println("Please enter a commit id.");
                System.exit(0);
            }
            Repository.reset(commitMessage);
        } else if (args.length == 1 && args[0].equals("reset")) {
            System.out.println("Please enter a commit id.");
            System.exit(0);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void mergeHandler(String[] args) {
        if (args.length == 2 && args[0].equals("merge")) {
            String branchName = args[1];
            if (branchName.trim().isEmpty()) {
                System.out.println("Please enter a branch name.");
                System.exit(0);
            }
            Repository.merge(branchName);
        } else if (args.length == 1 && args[0].equals("merge")) {
            System.out.println("Please enter a branch name.");
            System.exit(0);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }






}
