package gitlet;

import java.util.HashMap;
import java.util.Date;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/** Internal structure to handle commits. Includes log message,
 * timestamp, a mapping of file names to blob references, and a
 * parent reference  (or 2 for merges?).
 *  @author Brandon Griffin */
public class Commit implements Serializable {

    /** The message associated with this commit. */
    private String logMessage;

    /** The timestamp of this commit. */
    private Date timestamp;

    /** Maps file name to hash of the file. */
    private HashMap<String, String> files;

    /** The last commit before this one. */
    private Commit parent;

    /** The SHA-1 hashed id of this commit, used to name saved commits. */
    private String hashID;

    /** The first few characters of the hashID. */
    private String shortID;

    /** Tells whether the given commit is a merge or not. */
    private boolean isMerge;

    /** If the commit is a merge, this array holds the shortIDs
     *  of both parents. */
    private String[] mergeParents;

    /** The directory which holds all files. */
    private static final String OBJECTDIR = ".gitlet"
            + File.separator + "objects" + File.separator;

    /** Creates a new commit object, saving it to .gitlet/commits/ and to
     * the respective branch directory in which it was created.
     * @param message The commit message.
     * @param parent2 This commit's second parent, if a merge.
     * @param current The current Stage.
     * @param merge Tells whether the commit is a merge or not. */
    public Commit(String message, String parent2,
                  Stage current, boolean merge)  {
        if (message.equals("") || message.isEmpty()) {
            throw new GitletException("Please enter a commit message.");
        }

        this.logMessage = message;
        this.isMerge = merge;


        if (current != null) {
            this.parent = current.getLastCommit();
            this.files = current.getStaged();
            this.timestamp = new java.util.Date();
        } else {
            this.files = new HashMap<>();
            this.timestamp = new Date(0);
        }

        if (this.isMerge && parent2 != null) {
            mergeParents = new String[2];
            mergeParents[0] = parent.getHash().substring(0, 6);
            mergeParents[1] = parent2.substring(0, 6);
        }

        String toHash;
        if (((files == null) || files.isEmpty())) {
            toHash = message + timestamp;
        } else {
            toHash = message + timestamp + files.keySet();
        }
        this.hashID = gitlet.Utils.sha1(toHash);
        this.shortID = hashID.substring(0, 6);
    }

    /** Default constructor, used for a Commit that is not a merge.
     * @param current The current Stage.
     * @param message The commit message. */
    public Commit(Stage current, String message) {
        this(message, null, current, false);
    }

    /** Returns whether this commit contains the given file.
     * @param fileName The name of the file.
     * @return boolean denoting if the Commit contains the given file. */
    public boolean contains(String fileName) {
        return (files != null && files.containsKey(fileName));
    }

    /** Return the hash of this commit.
     * @return The hash of this commit. */
    public String getHash() {
        return hashID;
    }

    /** Return the commits merged to form this commit (if merge).
     * @return String array of the shortIDs of merged commits. */
    public String[] getMergeParents() {
        return mergeParents;
    }

    /** Return the shortID of this commit.
     * @return String of this commit's shortID. */
    public String getShort() {
        return shortID;
    }

    /** Return the mapping of files contained in this commit.
     * @return HashMap of the files in this commit. */
    public HashMap<String, String> files() {
        return files;
    }

    /** Return the hash of this commit.
     * @return The hash of this commit. */
    public String getMessage() {
        return logMessage;
    }

    /** Return the parent commit of this commit.
     * @return The parent Commit object. */
    public Commit getParent() {
        return parent;
    }

    /** Return the Date of this commit.
     * @return The Date object of this commit. */
    public Date getTime() {
        return timestamp;
    }

    /** Saves all staged files to the .gitlet/objects/ directory
     * named by their hash. */
    public void commit() {
        if (!files.isEmpty()) {
            for (String name : files.keySet()) {

                File committed = new File(name);
                File com = new File(OBJECTDIR + files.get(name));
                Utils.writeContents(com, Utils.readContents(committed));

            }
        }
    }

    /** Return the path to the file named "file".
     * @param file The name of the file searched for.
     * @return The File path to the given file. */
    public File getFile(String file) {
        if (files == null || files.isEmpty()) {
            return null;
        }
        if (files.containsKey(file)) {
            return new File(OBJECTDIR + files.get(file));
        }
        return null;
    }

    /** Turns this commit to a string for output.
     * @return String of this commits information. */
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("commit ");
        s.append(this.getHash());
        if (this.isMerge) {
            s.append("\nMerge: ");
            s.append(this.getMergeParents()[0]);
            s.append(" ");
            s.append(this.getMergeParents()[1]);
        }
        s.append("\nDate: ");
        String date = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z")
                .format(this.getTime());
        s.append(date);
        s.append("\n");
        s.append(this.getMessage());
        s.append("\n\n");
        return s.toString();
    }

}
