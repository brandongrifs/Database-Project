package gitlet;
import java.io.File;
/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Brandon Griffin
 */
public class Main {

    /** The main gitlet directory. */
    private static final String DIRECTORY = ".gitlet" + File.separator;

    /** The directory which holds the repo as a file. */
    private static final String REPODIR = ".gitlet"
            + File.separator + "repo" + File.separator;

    /** Loads an already initialized gitlet Repo.
     *
     * @return The existing gitlet Repo.
     */
    public static Repo load() {
        File f = new File(DIRECTORY);
        if (f.exists()) {
            File repo = new File(REPODIR + "gitlet.txt");
            return Utils.readObject(repo, Repo.class);
        } else {
            throw new GitletException(
                    "Not in an initialized Gitlet directory.");
        }
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            }
            String command = args[0];
            String[] inputs = getArgs(args);
            Repo gitlet = null;
            if (!command.equals("init")) {
                gitlet = load();
            }
            switch (command) {
            case "init":
                gitlet = init();
                break;
            case "add":
                gitlet.currentBranch().add(inputs[1]);
                break;
            case "commit":
                gitlet.commit(inputs[1]);
                gitlet.currentBranch().getStage().clear();
                break;
            case "rm":
                gitlet.currentBranch().remove(inputs[1]);
                break;
            case "log":
                System.out.print(gitlet.toLog());
                break;
            case "global-log":
                System.out.print(gitlet.globalLog());
                break;
            case "find":
                System.out.println(gitlet.find(inputs[1]));
                break;
            case "status":
                System.out.print(gitlet.status());
                break;
            case "checkout":
                checkoutMaster(gitlet, args);
                break;
            case "branch":
                gitlet.branch(inputs[1]);
                break;
            case "rm-branch":
                gitlet.rmBranch(inputs[1]);
                break;
            case "reset":
                gitlet.reset(inputs[1]);
                break;
            case "merge":
                break;
            default:
                throw new GitletException("No command with that name exists.");
            }
            Utils.writeObject(new File(REPODIR + "gitlet.txt"), gitlet);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /** Parses arguments from user input.
     * @param args Arguments input by user.
     * @return String Array of these args. */
    public static String[] getArgs(String... args) {
        String[] inputs = new String[4];
        if (args.length > 4) {
            throw new GitletException("Incorrect operands.");
        } else if (args.length > 3) {
            inputs[1] = args[1];
            inputs[2] = args[2];
            if (!inputs[2].equals("--")) {
                throw new GitletException("Incorrect operands.");
            }
            inputs[3] = args[3];
        } else if (args.length > 2) {
            inputs[1] = args[1];
            inputs[2] = args[2];
        } else if (args.length > 1) {
            inputs[1] = args[1];
        }
        return inputs;
    }

    /** Calls the correct Checkout command based on args.
     *
     * @param g The current repo.
     * @param args The arguments input by the user.
     */
    private static void checkoutMaster(Repo g, String... args) {
        try {
            if (args.length == 4) {
                g.checkout2(args[1], args[3]);
            } else if (args.length == 3) {
                g.checkout1(args[2]);
            } else if (args.length == 2) {
                g.checkout3(args[1]);
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /** Initializes a gitlet repo if it has not been initialized already.
     *
     * @return A new gitlet Repo object.
     */
    private static Repo init() {
        File file = new File(DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
            return Repo.init();
        } else {
            throw new GitletException("A Gitlet version-control"
                    + " system already exists in the current directory.");
        }
    }
}
