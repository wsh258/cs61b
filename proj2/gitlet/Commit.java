package gitlet;

// TODO: any imports you need here

import static gitlet.Utils.*;

import java.io.File;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

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
    private HashMap <String,String> blobs = new HashMap<>();
    private final String parent;
    static final File commitFolder = join(Repository.GITLET_DIR,"commits");
    static final File blobsFolder = join(Repository.GITLET_DIR,"blobs");

    public Commit(String message, String timestamp, String parent) {
        this.message = message;
        this.timestamp = timestamp;
        this.parent = parent;
        if (this.parent == null) {
            Instant epoch = Instant.ofEpochMilli(0);
            // 转换为本地时区时间
            ZonedDateTime dateTime = epoch.atZone(ZoneId.systemDefault());
            // 格式化输出
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                    "E MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
            this.timestamp = dateTime.format(formatter);
        }
    }

    public String saveCommit() {
        File f = join(commitFolder, Utils.sha1(this));
        writeObject(f,this);
        return sha1(this);
    }

    public static Commit fromFile(String SHA) {
        File existCommit = join(commitFolder, SHA);
        return readObject(existCommit, Commit.class);
    }

    public static void removeSamekey(HashMap<String,String> map1, HashMap<String,String> map2) {
        for (String key : map2.keySet()) {
            map1.remove(key);
        }
    }

    public HashMap <String,String> getBlobs() {
        return this.blobs;
    }

    public String getMessage() {
        return this.message;
    }

    public String getParent() {
        return parent;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void addBlobs(HashMap<String,String> stageAddition, HashMap<String,String> stageRemovement) {
        blobs.putAll(stageAddition);
        if(stageRemovement != null) {
            removeSamekey(blobs, stageRemovement);
        }
        saveCommit();
    }

}
