package gitlet;

import java.io.File;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;

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
    public static final File HeadFile = join(GITLET_DIR,"Head");

    String initialBranch = "Mater";
    Commit Head;
    String currentCommit;
    public static Commit glInit() {
        if (GITLET_DIR.mkdir()) {
            commitFolder.mkdir();
        } else {
            throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        }
        Commit initialCommit = new Commit("initial commit", null, null);
        changeHead(initialCommit);
        commitFolder.mkdir();
        initialCommit.saveCommit();
        return initialCommit;

    }
    public static Commit getHead() {
        return Commit.fromFile(readContentsAsString(HeadFile));
    }

    public static void changeHead(Commit cm) {
        writeContents(HeadFile,sha1(cm));
    }

    public static String stagedForAddition(String fileName) {
        File thisAddFile = join(CWD, fileName);
        if(!thisAddFile.exists()) {
            return null;
        }
        File StageFolder = join(GITLET_DIR, ".stage");

        File addition = join(StageFolder, "addition", sha1(thisAddFile));
        writeContents(addition,readContentsAsString(thisAddFile));
        Stage sd =  Stage.fromFile();
        Commit Head = getHead();
        if (sd.addition.containsValue(sha1(thisAddFile)) || Head.blobs.containsValue(sha1(thisAddFile))) {
            sd.addition.remove(fileName, sha1(thisAddFile));
            return null;
        }
        sd.addition.put(fileName, sha1(thisAddFile));
        sd.saveStage();
        return sha1(thisAddFile);
        /* TODO: fill in the rest of this class. */
    }



        /**
         * 获取当前 UTC 时间的格式化字符串
         * 格式为：HH:mm:ss UTC, EEEE, d MMMM yyyy
         * 示例：15:42:18 UTC, Tuesday, 4 June 2025
         */
    public static String getFormattedUTCTimestamp() {
        ZonedDateTime now = Instant.now().atZone(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "HH:mm:ss 'UTC,' EEEE, d MMMM yyyy", Locale.ENGLISH);
        return now.format(formatter);
    }
    public static String commitCommands(String message) {

        Commit newCommit = new Commit(message,getFormattedUTCTimestamp(),sha1(getHead()));
        return null;
    }

}
