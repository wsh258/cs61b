package gitlet;

// TODO: any imports you need here

import static gitlet.Utils.*;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private List<String> parents = new ArrayList<>();;
    static final File commitFolder = join(Repository.GITLET_DIR,"commits");
    static final File blobsFolder = join(Repository.GITLET_DIR,"blobs");

    public Commit(String message, String timestamp, String parent) {
        this.message = message;
        if (parent != null) {
            this.parents = new ArrayList<>();
            this.parents.add(parent);
            this.timestamp = timestamp;
        } else {
            // initial commit
            Instant epoch = Instant.ofEpochMilli(0);
            ZonedDateTime dateTime = epoch.atZone(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                    "E MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
            this.timestamp = dateTime.format(formatter);
        }
    }

    public Commit(String message, String timestamp, String parent1, String parent2) {
        this.message = message;
        this.timestamp = timestamp;
        this.parents = new ArrayList<>();
        this.parents.add(parent1);
        this.parents.add(parent2);
    }

    public boolean isMergeCommit() {
        return parents.size() > 1;
    }


    public String saveCommit() {
        File f = join(commitFolder, getSha());
        writeObject(f,this);
        return getSha();
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
        if (parents == null || parents.isEmpty()) {
            return null; // 如果没有父节点，安全地返回 null
        }
        return parents.get(0);
    }

    public String getParents() {
        if (parents == null || parents.isEmpty()) {
            return null; // 如果没有父节点，安全地返回 null
        }
        if (!isMergeCommit()) {
            return parents.get(0);
        }
        return parents.get(0).substring(0, 7) + " " + parents.get(1).substring(0,7);

    }

    public String getTimestamp() {
        return timestamp;
    }

    public void addBlobs(HashMap<String,String> stageAddition, HashMap<String,String> stageRemovement) {
        blobs.putAll(stageAddition);
        if(stageRemovement != null) {
            removeSamekey(blobs, stageRemovement);
        }
    }

    public String getSha() {
        List<Object> shaInput = new ArrayList<>();
        shaInput.add(message);
        shaInput.add(timestamp);

        // 多个 parent 要保持顺序一致
        shaInput.addAll(parents);

        // 将 blobs 按文件名排序后拼接，确保顺序一致
        List<String> sortedBlobKeys = new ArrayList<>(blobs.keySet());
        Collections.sort(sortedBlobKeys);
        for (String fileName : sortedBlobKeys) {
            shaInput.add(fileName);
            shaInput.add(blobs.get(fileName));
        }

        return Utils.sha1(shaInput);
    }


}
