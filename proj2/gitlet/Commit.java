package gitlet;

// TODO: any imports you need here

import static gitlet.Utils.*;

import java.io.File;
import java.io.Serializable;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
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
    private String parent;
    static final File commitFolder = join(Repository.GITLET_DIR,"commits");

    public Commit(String message, String timestamp, String parent) {
        this.message = message;
        this.timestamp = timestamp;
        this.parent = parent;
        if (this.parent == null) {
            this.timestamp = "00:00:00 UTC, Thursday, 1 January 1970";
        }
    }

    public void saveCommit() {
        File f = join(commitFolder, Utils.sha1(this));
        writeObject(f,this);
    }
    public static Commit fromFile(String SHA) {
        File existCommit = join(commitFolder, SHA);
        return readObject(existCommit, Commit.class);
    }

}
