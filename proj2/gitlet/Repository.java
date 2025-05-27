package gitlet;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     * <p>
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */
    static final File commitFolder = join(Repository.GITLET_DIR, "commits");
    static final File blobsFolder = join(Repository.GITLET_DIR, "blobs");
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    HashMap<String, String> commitSHA = new HashMap<>();

    String initialHead = "mater";
    Commit Head;

    public static Commit glInit() {
        if (GITLET_DIR.mkdir()) {
            commitFolder.mkdir();
        } else {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        Commit initialCommit = new Commit("initial commit", null, null);
        commitFolder.mkdir();
        initialCommit.saveCommit();
        return initialCommit;

    }

    public void addTempBlobs(String fileName) {
        File thisAddFile = join(CWD, fileName);
        String SHA1 = sha1(thisAddFile);
        File newBlobs = join(blobsFolder, sha1(thisAddFile));

        /* TODO: fill in the rest of this class. */
    }
}
