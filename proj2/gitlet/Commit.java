package gitlet;

import static gitlet.Utils.*;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Sihao Wong
 */
public class Commit implements Serializable {
    /*
      List all instance variables of the Commit class here with a useful
      comment above them describing what that variable represents and how that
      variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String timestamp;

    private HashMap<String, String> blobs = new HashMap<>();
    private List<String> parents = new ArrayList<>();
    static final File COMMITFOLDER = join(Repository.GITLET_DIR, "commits");
    static final File BLOBSFOLDER = join(Repository.GITLET_DIR, "blobs");

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
        File f = join(COMMITFOLDER, this.getSha());
        writeObject(f, this);
        return getSha();
    }

    public static Commit fromFile(String sha) {
        File existCommit = join(COMMITFOLDER, sha);
        return readObject(existCommit, Commit.class);
    }

    public static void removeSamekey(HashMap<String, String> map1, HashMap<String, String> map2) {
        for (String key : map2.keySet()) {
            map1.remove(key);
        }
    }

    public HashMap<String, String> getBlobs() {
        return new HashMap<>(blobs); // ✅ 返回副本，避免外部修改
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

    public String get2Parent() {
        if (!isMergeCommit()) {
            return null;
        }
        return parents.get(1);
    }

    public String getParents() {
        if (parents == null || parents.isEmpty()) {
            return null; // 如果没有父节点，安全地返回 null
        }
        if (!isMergeCommit()) {
            return parents.get(0);
        }
        return parents.get(0).substring(0, 7) + " " + parents.get(1).substring(0, 7);

    }

    public String getTimestamp() {
        return timestamp;
    }

    public void addBlobs(HashMap<String, String> stageAddition,
                         HashMap<String, String> stageRemoval) {
        blobs.putAll(stageAddition);
        if (stageRemoval != null) {
            removeSamekey(blobs, stageRemoval);
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
