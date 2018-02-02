package gitlet;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.Serializable;
/** Stores currently staged files (removals and additions)
 *  since last commit, named by their String name.
 *  @author Brandon Griffin
 */
public class Stage implements Serializable {

    /** String of the path to the directory containing staged files. */
    private static final String STAGEDDIR = ".gitlet" + File.separator
            + "staged" + File.separator;

    /** Mapping of file name to commit id (file hash?) for all staged files. */
    private HashMap<String, String> staged;

    /** The last commit in the current branch. */
    private Commit lastCom;

    /** List of file names to be removed. */
    private ArrayList<String> toRemove;

    /** List of file names to be added. */
    private ArrayList<String> toAdd;

    /** Constructor for stage.
     * @param com The last commit in the current branch. */
    public Stage(Commit com) {
        toRemove = new ArrayList<>();
        this.lastCom = com;
        staged = new HashMap<>();
        if (com != null && !com.files().isEmpty()) {
            staged.putAll(com.files());
        }
        toAdd = new ArrayList<>();
    }

    /** Blank constructor for stage. */
    public Stage() {
        this(null);
    }

    /** Returns files to be added.
     * @return Returns files to be added. */
    public ArrayList<String> getAddedFiles() {
        return toAdd;
    }

    /** Returns files to be removed.
     * @return Returns files to be removed. */
    public ArrayList<String> getRemovedFiles() {
        return toRemove;
    }

    /** Returns HashMap of all staged files.
     * @return Returns HashMap of all staged files. */
    public HashMap<String, String> getStaged() {
        return staged;
    }

    /** Returns the last commit of the current branch.
     * @return Returns the last commit of this stage. */
    public Commit getLastCommit() {
        return lastCom;
    }

    /** Not sure if needed, but determines if any files have been added or
     * removed in this stage.
     * @return boolean value denoting whether the stage has changed. */
    public boolean changedStage() {
        return (toRemove.size() + toAdd.size() != 0);
    }

    /** Determines if a file has been changed since last commit.
     * @param name Name of file to be checked.
     * @return boolean value denoting whether the file is changed. */
    public boolean changedFile(String name) {
        if (!lastCom.contains(name)) {
            return true;
        }
        File file = new File("." + File.separator + name);
        String f = Utils.sha1(Utils.readContentsAsString(file) + name);
        String f2 = lastCom.files().get(name);
        return (!f.equals(f2));
    }

    /** Adds the given file to the staging area.
     * @param name Name of file to be added. */
    public void add(String name) {
        File file = new File("." + File.separator + name);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        } else if (!changedFile(name) && new File(STAGEDDIR + name).exists()) {
            new File(STAGEDDIR + name).delete();
            return;
        }
        if (toRemove.contains(name)) {
            toRemove.remove(name);
        }
        if (!lastCom.files().containsKey(name) || changedFile(name)) {
            toAdd.add(name);
            staged.put(name, Utils.sha1(
                    Utils.readContentsAsString(file) + name));
            File blob = new File(STAGEDDIR + name);
            Utils.writeContents(blob, Utils.readContents(file));
        }

    }

    /** Removes the given file from the staging area.
     * @param name Name of file to be removed. */
    public void remove(String name) {
        if (!staged.containsKey(name) && !this.lastCom.contains(name)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (staged.containsKey(name)) {
            staged.remove(name);
            toAdd.remove(name);
            new File(STAGEDDIR + name).delete();
        }
        if (this.lastCom.contains(name)) {
            Utils.restrictedDelete(name);
            toRemove.add(name);
        }
    }

    /** Prints the staging area for the "status" command.
     * @return String version of the staging area. */
    public String toString() {
        StringBuilder currentStage = new StringBuilder();
        currentStage.append("\n=== Staged Files ===\n");
        for (String name : toAdd) {
            currentStage.append(name);
            currentStage.append("\n");
        }
        currentStage.append("\n=== Removed Files ===\n");
        for (String name : toRemove) {
            currentStage.append(name);
            currentStage.append("\n");
        }
        currentStage.append("\n=== Modifications Not Staged For Commit ===\n");
        currentStage.append("\n=== Untracked Files ===\n\n");

        return currentStage.toString();
    }

    /** Clears the current staging area, deleting any files remaining. */
    public void clear() {
        this.toAdd.clear();
        this.toRemove.clear();
        for (String name : staged.keySet()) {
            new File(STAGEDDIR + name).delete();
        }
        this.staged.clear();
    }
}
