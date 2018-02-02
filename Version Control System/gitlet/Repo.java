package gitlet;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.Serializable;

/** Maintains the overall Repository for this Gitlet instance.
 * Current repo version is stored in .gitlet/repo/[name].txt.
 * All files are stored in objects directory after being
 * committed.
 *  @author Brandon Griffin
 */
public class Repo implements Serializable {

    /** Directory to store all file objects in by hash. */
    private static final String OBJECTSDIR = ".gitlet"
            + File.separator + "objects" + File.separator;

    /** The directory holding the staging area. */
    private static final String STAGEDDIR = ".gitlet"
            + File.separator + "staged" + File.separator;

    /** Directory holding all branches (as folders). */
    private static final String BRANCHDIR = ".gitlet"
            + File.separator + "branches" + File.separator;

    /** Directory to hold the repo's state. */
    private static final String REPODIR = ".gitlet"
            + File.separator + "repo" + File.separator;

    /** Holds all commits (as files) in this repo by hash. */
    private static final String COMMITDIR = ".gitlet"
            + File.separator + "commits" + File.separator;


    /** Commit hash to Commit object. */
    private HashMap<String, Commit> commitsByHash;

    /** Commit message to Commit object. */
    private HashMap<String, ArrayList<Commit>> commitsByMessage;

    /** Branch name to branch object. */
    private HashMap<String, Branch> branches;

    /** The name of the current branch. */
    private Branch branch;

    /** General constructor for a Repo. */
    public Repo() {
        commitsByMessage = new HashMap<>();
        commitsByHash = new HashMap<>();
        branches = new HashMap<>();
    }

    /** Creates a new gitlet repo in the current directory.
     * @return The newly initialized Repo object. */
    public static Repo init() {
        Repo gitlet = new Repo();
        Commit newCom = new Commit(null, "initial commit");
        Branch newBranch = new Branch("master", newCom);
        gitlet.branches.put("master", newBranch);
        gitlet.commitsByHash.put(newCom.getHash(), newCom);
        ArrayList<Commit> c = new ArrayList<>();
        c.add(newCom);
        gitlet.commitsByMessage.put("initial commit", c);
        gitlet.branch = newBranch;


        new File(Repo.OBJECTSDIR).mkdirs();
        new File(Repo.BRANCHDIR).mkdirs();
        new File(Repo.STAGEDDIR).mkdirs();
        new File(Repo.REPODIR).mkdirs();
        new File(Repo.COMMITDIR).mkdirs();



        File initrepo = new File(REPODIR + "gitlet.txt");
        Utils.writeObject(initrepo, gitlet);

        File initcommit = new File(BRANCHDIR + "master"
                + File.separator + newCom.getHash() + ".txt");
        Utils.writeObject(initcommit, newCom);

        File commit = new File(COMMITDIR + File.separator
                + newCom.getHash() + ".txt");
        Utils.writeObject(commit, newCom);

        return gitlet;
    }

    /** Returns the current branch in the Repo.
     * @return The current branch. */
    public Branch currentBranch() {
        return this.branch;
    }

    /** Sets the current branch of the Repo to b.
     * @param b The new current Branch. */
    public void setBranch(Branch b) {
        this.branch = b;
    }

    /** Prints all commit hashes with the given message.
     * @param  message The commit message searched for.
     * @return String of all commit hashes found. */
    public String find(String message) {
        StringBuilder sb = new StringBuilder();
        if (!commitsByMessage.containsKey(message)) {
            throw new GitletException("Found no commit with that message.");
        } else {
            ArrayList<Commit> s = findCommits(message);
            for (Commit comma : s) {
                sb.append(comma.getHash());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**  Prints all commits in the Repo in any order. Format:
     *   Commit id, shortids of parents (if merge), date, commit message.
     *   @return A string of all commits in this repo. */
    public String globalLog() {
        StringBuilder s = new StringBuilder();
        if (commitsByHash.size() > 0) {
            for (String commit : commitsByHash.keySet()) {
                s.append("===\n");
                s.append(commitsByHash.get(commit).toString());
            }
        } else {
            throw new GitletException("Must initialize a repo first.");
        }
        return s.toString();
    }

    /**  Prints all commits in the current branch in order from head backwards.
     * Format: Commit id, shortids of parents (if merge), date, commit message.
     * @return A String of all the commits in the current branch. */
    public String toLog() {
        StringBuilder s = new StringBuilder();
        Commit current = currentBranch().getHEAD();
        s.append("===\n");
        s.append(current.toString());
        current = current.getParent();
        while (current != null) {
            s.append("===\n");
            s.append(current.toString());
            current = current.getParent();
        }

        return s.toString();
    }

    /** Finds all commits with the given message.
     *   @param message The commit message.
     *   @return ArrayList of all the commit hashes with the given message. */
    public ArrayList<Commit> findCommits(String message) {
        ArrayList<Commit> result = new ArrayList<>();
        for (String s : commitsByMessage.keySet()) {
            if (s.equals(message)) {
                result.addAll(commitsByMessage.get(s));
            }
        }
        return result;
    }

    /**  Saves this commit as a txt file to the current branch directory,
     *  named by its hash. Calls Commit.commit() to save the files.
     *   @param message The commit message. */
    public void commit(String message) {
        if (message.length() == 0) {
            throw new GitletException("Please enter a commit message.");
        }
        if (currentBranch().getStage().getStaged().isEmpty()
                && currentBranch().getStage().getRemovedFiles().isEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }
        Commit c = new Commit(currentBranch().getStage(), message);
        currentBranch().setHEAD(c);
        currentBranch().setStage(new Stage(currentBranch().getHEAD()));
        File f = new File(BRANCHDIR + branch.getName()
                + File.separator + c.getHash());
        Utils.writeObject(f, c);
        File commit = new File(COMMITDIR + File.separator + c.getHash());
        Utils.writeObject(commit, c);
        commitsByHash.put(c.getHash(), c);
        if (commitsByMessage.containsKey(message)) {
            commitsByMessage.get(message).add(c);
        } else {
            ArrayList<Commit> commas = new ArrayList<>();
            commas.add(c);
            commitsByMessage.put(message, commas);
        }
        c.commit();
    }

    /** Checkout for case 1, changing files in working directory from
    * a single file "name" from the head commit of the current branch.
    * @param name the name of the file to be checked out. */
    public void checkout1(String name) {
        if (!this.branch.getHEAD().files().containsKey(name)) {
            throw new GitletException("File does not exist in that commit.");
        }
        File check = new File(OBJECTSDIR
                + this.branch.getHEAD().files().get(name));
        File work = new File("." + File.separator + name);
        Utils.writeContents(work, Utils.readContents(check));

        if (this.branch.getStage().getStaged().containsKey(name)) {
            this.branch.getStage().remove(name);
        }

    }

    /** Checkout case 2, takes file "name" from commit with hash commitID.
    *  @param commitID Id of the commit to get the file from.
    *  @param name Name of the file to be checked out. */
    public void checkout2(String commitID, String name) {
        if (!commitsByHash.containsKey(commitID)) {
            throw new GitletException("No commit with that id exists.");
        }

        if (commitsByHash.get(commitID).getFile(name) == null
                || !commitsByHash.get(commitID).getFile(name).exists()) {
            throw new GitletException("File does not exist in that commit.");
        }
        File f = commitsByHash.get(commitID).getFile(name);
        File working = new File("." + File.separator + name);
        Utils.writeContents(working, Utils.readContents(f));
    }

    /** Checkout for case 3, changing files in working directory from
    * all the files in the head commit of the given branch.
    * @param name The name of the branch to be checked out. */
    public void checkout3(String name) {
        if (branches.containsKey(name) && name.equals(branch.getName())) {
            throw new GitletException("No need to checkout the current "
                    + "branch.");
        }
        if (!branches.containsKey(name)) {
            throw new GitletException("No such branch exists.");
        }

        File works = new File("." + File.separator);
        for (String f : branches.get(name).getHEAD().files().keySet()) {
            if (!currentBranch().getHEAD().contains(f)
                    && Utils.plainFilenamesIn(works).contains(f)) {
                throw new GitletException("There is an untracked file in "
                        + "the way; delete it or add it first.");
            }
        }

        for (String s : Utils.plainFilenamesIn(works)) {
            if (currentBranch().getHEAD().contains(s)
                    && !branches.get(name).getHEAD().contains(s)) {
                Utils.restrictedDelete(new File("." + File.separator + s));
            }
        }

        for (String nam : branches.get(name).getHEAD().files().keySet()) {
            File f = new File("." + File.separator + nam);
            Utils.writeContents(f, Utils.readContents(branches
                    .get(name).getHEAD().getFile(nam)));
        }

        this.setBranch(branches.get(name));
    }

    /** Checks out all files tracked by the given commit.
     * @param commitID Commit to be checked out. */
    public void reset(String commitID) {
        if (!commitsByHash.containsKey(commitID)) {
            throw new GitletException("No commit with that id exists");
        }

        Commit c = commitsByHash.get(commitID);
        branch.setHEAD(c);
        for (String fileName : c.files().keySet()) {
            checkout1(fileName);
        }
        branch.getStage().clear();
    }

    /** Creates a new branch with "name" pointing to current
     * head node.
     * @param name Name of the branch to be created. */
    public void branch(String name) {
        if (branches.containsKey(name)) {
            throw new GitletException("A branch with that name already "
                    + "exists.");
        }
        Commit head = branch.getHEAD();
        Branch b = new Branch(name, head);
        branches.put(name, b);
        File n = new File(BRANCHDIR + name);
        n.mkdirs();
    }

    /** Deletes the pointer to the given Branch.
     * @param name Name of the branch to be removed. */
    public void rmBranch(String name) {
        if (!branches.containsKey(name)) {
            throw new GitletException("A branch with that name does not exist");
        }
        if (branch.getName().equals(name)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        branches.remove(name);
    }

    /** Gets the status of the whole repo.
     * Format: Branches, Staged, Removed.
     * @return The Status of the Repo as a String. */
    public String status() {
        StringBuilder s = new StringBuilder();
        s.append("=== Branches ===\n*");
        s.append(this.currentBranch().getName());
        s.append("\n");
        for (String name : branches.keySet()) {
            if (!name.equals(this.currentBranch().getName())) {
                s.append(name);
                s.append("\n");
            }
        }
        s.append(branch.getStage().toString());
        return s.toString();
    }
}
