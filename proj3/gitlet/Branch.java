package gitlet;
import java.io.Serializable;
import java.io.File;

/** Internal structure for Gitlet. Each branch is represented by
 * a directory of its name in the branches directory. This folder
 * contains a file of the head commit named by hash.
 *  @author Brandon Griffin */
public class Branch implements Serializable {

    /** Holds the head commit of this branch. */
    private Commit head;

    /** The current stage of this branch. */
    private Stage stage;

    /** The name of this branch and the directory it stores commits in. */
    private final String name;

    /** Directory that holds all branch directories. */
    private static final String BRANCHDIR = ".gitlet" + File.separator
            + "branches" + File.separator;

    /** Constructor for a Branch object.
     * @param name1 Name of the branch to construct.
     * @param head1 Head commit of the new branch. */
    public Branch(String name1, Commit head1) {
        this.name = name1;
        this.head = head1;
        this.stage = new Stage(head);
        File f = new File(BRANCHDIR + this.name);
        f.mkdirs();
    }

    /** Returns the current head commit of this branch.
     * @return The Commit at the head of this branch. */
    public Commit getHEAD() {
        return this.head;
    }

    /** Sets the current head branch of this commit.
     * @param head1 New head commit of this branch. */
    public void setHEAD(Commit head1) {
        this.head = head1;
    }

    /** Removes the given file from the staging area of this branch.
     * @param f The name of the file to be removed. */
    public void remove(String f) {
        stage.remove(f);
    }

    /** Adds the given file from the staging area of this branch.
     * @param f The name of the file to be added. */
    public void add(String f) {
        stage.add(f);
    }

    /** Returns the name of this branch.
     * @return Name String of this branch.*/
    public String getName() {
        return this.name;
    }

    /** Returns the staging area of this branch as a Stage object.
     * @return The current Stage object. */
    public Stage getStage() {
        return this.stage;
    }

    /** Changes the staging area of this branch to s.
     * @param s The new staging area of this branch. */
    public void setStage(Stage s) {
        this.stage = s;
    }
}
