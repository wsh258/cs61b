package gitlet;

// TODO: any imports you need here

import static gitlet.Utils.*;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Sihao Wong
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String timestamp;
    /* TODO: fill in the rest of this class. */
    //标记该commit的保存对象的变量
    HashMap <String,String> blobs = new HashMap<>();
    private String parent;
    static final File commitFolder = join(Repository.GITLET_DIR,"commits");
    static final File blobsFolder = join(Repository.GITLET_DIR,"blobs");

    public Commit(String message, String timestamp, String parent) {
        this.message = message;
        this.timestamp = timestamp;
        this.parent = parent;
        if (this.parent == null) {
            this.timestamp = "00:00:00 UTC, Thursday, 1 January 1970";
        }
    }

    public String saveCommit() {
        File f = join(commitFolder, Utils.sha1(this));
        writeObject(f,this);
        return Utils.sha1(this);
    }

    public static Commit fromFile(String SHA) {
        File existCommit = join(commitFolder, SHA);
        return readObject(existCommit, Commit.class);
    }

    public void addBlobs(String filename) {
        String fileSHA = sha1(filename);
        blobs.put(filename,fileSHA);

    }

}
