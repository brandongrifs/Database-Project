package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;


/** The suite of all JUnit tests for the gitlet package.
 *  @author Brandon Griffin
 */
public class UnitTest {

    private static final String OBJECTSDIR = ".gitlet"
            + File.separator + "objects" + File.separator;

    private static final String STAGEDDIR = ".gitlet"
            + File.separator + "staged" + File.separator;

    private static final String BRANCHDIR = ".gitlet"
            + File.separator + "branches" + File.separator;

    private static final String REPODIR = ".gitlet"
            + File.separator + "repo" + File.separator;

    private static final String COMMITDIR = ".gitlet"
            + File.separator + "commits" + File.separator;


    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** Testing initialization of the repo. */
    @Test
    public void initTest() {
        Repo gitlet = Repo.init();
        assertEquals(gitlet.currentBranch().getName(), "master");
        assert (new File(REPODIR + "gitlet.txt").exists());
        assert (new File(BRANCHDIR + "master/").exists());

    }

    /** Tests the add and remove functions for staging files. */
    @Test
    public void addRemoveTest() {
        Repo gitlet = Repo.init();
        gitlet.currentBranch().add("f.txt");
        assertEquals(Utils.readContentsAsString(new File(STAGEDDIR
                + "f.txt")), "This is a wug.");
        gitlet = Main.load();
        gitlet.currentBranch().remove("f.txt");
        assert (!new File(".gitlet" + File.separator + "staged"
                + File.separator + "f.txt").exists());

        gitlet.currentBranch().add("wag.txt");
        gitlet.currentBranch().remove("wag.txt");
    }

    /** Tests for commits. */
    @Test
    public void commitTest() {
        Repo gitlet = Repo.init();
        gitlet.currentBranch().add("f.txt");
        gitlet.commit("added wug");

        assert (new File(BRANCHDIR + "master/"
                + gitlet.currentBranch().getHEAD().getHash()).exists());
    }

}


