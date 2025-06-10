package gitlet;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        if (sd.addition.containsValue(sha1(thisAddFile)) || Head.getBlobs().containsValue(sha1(thisAddFile))) {
            sd.addition.remove(fileName, sha1(thisAddFile));
            return null;
        }
        sd.addition.put(fileName, sha1(thisAddFile));
        sd.saveStage();
        return sha1(thisAddFile);
    }


        public static String getFormattedTimestamp() {
            ZonedDateTime now = ZonedDateTime.now();  // 使用本地时区
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                    "E MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
            return now.format(formatter);
        }
    public static String commitCommands(String message) {
        if (message.isEmpty()) {
            message("Please enter a commit message.");
            System.exit(0);
        }
        Commit Head = getHead();
        Commit newCommit = new Commit(message,getFormattedTimestamp(),sha1(Head));
        newCommit.addBlobs(Head.getBlobs(),null);
        if (Stage.fromFile().addition.isEmpty() && Stage.fromFile().removal.isEmpty()) {
            message("No changes added to the commit.");
            System.exit(0);
        }
        newCommit.addBlobs(Stage.fromFile().addition,Stage.fromFile().removal);
        changeHead(newCommit);
        return newCommit.saveCommit();
    }

    public static String stagedForRemoval(String fileName) {
        if (!getHead().getBlobs().containsKey(fileName) && !Stage.fromFile().removal.containsKey(fileName)) {
            message("No reason to remove the file.");
            System.exit(0);
        }
        File thisremoveFile = join(CWD, fileName);
        String removeSHA = sha1(thisremoveFile);
        //如果文件已被暂存添加（staged for addition） → 把它从暂存区移除。
        Stage sd =  Stage.fromFile();
        sd.removal.remove(fileName);
        sd.saveStage();
        //如果文件在当前提交中被跟踪（tracked）：
        //把它标记为待删除（staged for removal）。
        //如果用户尚未删除该文件（即工作目录中还存在该文件）：
        //👉 从工作目录中将其物理删除（即 File.delete()）。
        Commit Head = getHead();
        if (Head.getBlobs().containsKey(fileName)) {
            Stage sdd =  Stage.fromFile();
            sdd.removal.put(fileName,sha1(thisremoveFile));
            sdd.saveStage();
            restrictedDelete(thisremoveFile);
        }
        return removeSHA;
    }

    public static void printLog() {
        Commit currentCommit = getHead();
        /*TODO：For merge commits (those that have two parent commits), add a line just below the first, as in Merge: 4975af1 2c1ead1
        其中，“Merge:”后面的两个十六进制数由第一个和第二个父提交的前七位提交 ID 组成，
        顺序为先第一个父提交，后第二个父提交。第一个父提交是你进行合并时所在的分支；第二个父提交是被合并进来的分支。这与常规的 Git 一致。*/
        while (currentCommit != null){
            String sb;
            if (!currentCommit.isMergeCommit()) {
                sb = "===\n" +
                        "commit " + sha1(currentCommit) + "\n" +
                        "Date: " + currentCommit.getTimestamp() + "\n" +
                        currentCommit.getMessage();
            }
            else {
                sb = "===\n" +
                        "commit " + sha1(currentCommit) + "\n" + "Merge: " +
                        currentCommit.getParents() + "\n" +
                        "Date: " + currentCommit.getTimestamp() + "\n" +
                        currentCommit.getMessage();
            }

            message(sb);

            String parentSha = currentCommit.getParent();
            if (parentSha == null) {
                break;
            }
            currentCommit = Commit.fromFile(parentSha);
        }
    }
    public static void printGlobalLog() {
        List<String> allCommit = plainFilenamesIn(commitFolder);
        if (allCommit != null) {
            for (String currentCommitStr : allCommit) {
                String sb;
                Commit currentCommit = Commit.fromFile(currentCommitStr);
                if (!currentCommit.isMergeCommit()) {
                    sb = "===\n" +
                            "commit " + sha1(currentCommit) + "\n" +
                            "Date: " + currentCommit.getTimestamp() + "\n" +
                            currentCommit.getMessage();
                } else {
                    sb = "===\n" +
                            "commit " + sha1(currentCommit) + "\n" + "Merge: " +
                            currentCommit.getParents() + "\n" +
                            "Date: " + currentCommit.getTimestamp() + "\n" +
                            currentCommit.getMessage();
                }
            }
        }
    }
    public static void find(String message) {
        List<String> allCommit = plainFilenamesIn(commitFolder);
        if (allCommit != null) {
            boolean havaMatch = false;
            for (String currentCommitStr : allCommit) {
                Commit currentCommit = Commit.fromFile(currentCommitStr);
                if (currentCommit.getMessage().equals(message)) {
                    message(currentCommitStr);
                    havaMatch = true;
                }
            }
            if (!havaMatch) {
                message("Found no commit with that message.");
            }
        }

    }

}
