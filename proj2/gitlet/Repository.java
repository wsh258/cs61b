package gitlet;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Sihao Wong
 */
public class Repository {
    /**
     * <p>
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    static final File commitFolder = join(GITLET_DIR, "commits");
    static final File blobsFolder = join(GITLET_DIR, "blobs");
    HashMap<String, String> commitSHA = new HashMap<>();
    public static final File HeadFile = join(GITLET_DIR,"Head");
    /** 保存分支名 -> commit ID 的映射 */
    private static HashMap<String, String> branches;

    /** 当前分支名（如 "master"） */
    private static String currentBranch = "master";

    static final File branch = join(GITLET_DIR, "branches","branchMap");
    static final File currentBranchfile = join(GITLET_DIR, "branches","currentBranch");

    Commit Head;
    String currentCommit;


    public static void glInit() {
        if (GITLET_DIR.mkdir()) {
            commitFolder.mkdir();
            blobsFolder.mkdir();
            // 新增创建 branches 目录
            File branchesDir = join(GITLET_DIR, "branches");
            branchesDir.mkdir();

            // 如果currentBranchfile是个文件，也要保证所在目录存在
            currentBranchfile.getParentFile().mkdirs();
        } else {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        Commit initialCommit = new Commit("initial commit", null, null);
        changeHead(initialCommit);
        changeBranchCommitAndSave(initialCommit);
    }



    private static Commit getHead() {
        return Commit.fromFile(readContentsAsString(HeadFile));
    }

    private static void changeHead(Commit cm) {
        writeContents(HeadFile,cm.getSha());
    }

    public static String stagedForAddition(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            message("File does not exist.");
            System.exit(0);
        }

        String fileContent = readContentsAsString(file);
        String fileSha1 = sha1(fileContent);
        File blobFile = join(blobsFolder, fileSha1);

        Commit head = getHead();
        Stage stage = Stage.fromFile();

        // 如果当前文件内容与 HEAD 中版本相同，则不应添加到暂存区
        if (fileSha1.equals(head.getBlobs().get(fileName))) {
            // 如果之前已被暂存，则移除
            stage.addition.remove(fileName);
            stage.removal.remove(fileName);  // 若之前标记为删除，也移除
            stage.saveStage();
            return null;
        }

        // 否则，写入 blob，并记录在 addition 中
        writeContents(blobFile, fileContent);
        stage.addition.put(fileName, fileSha1);
        stage.removal.remove(fileName); // 若之前标记为删除，取消删除
        stage.saveStage();
        return fileSha1;
    }



    private static String getFormattedTimestamp() {
        ZonedDateTime now = ZonedDateTime.now();  // 使用本地时区
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "E MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return now.format(formatter);
    }
    public static String commitCommands(String message) {

        if (Stage.fromFile().addition.isEmpty() && Stage.fromFile().removal.isEmpty()) {
            message("No changes added to the commit.");
            System.exit(0);
        }

        Commit Head = getHead();
        Commit newCommit = new Commit(message,getFormattedTimestamp(),Head.getSha());
        newCommit.addBlobs(Head.getBlobs(),null);
        newCommit.addBlobs(Stage.fromFile().addition,Stage.fromFile().removal);

        Stage stage = new Stage();
        stage.saveStage();//清空Stage

        changeHead(newCommit);

        changeBranchCommitAndSave(newCommit);

        return newCommit.saveCommit();
    }

    public static void stagedForRemoval(String fileName) {
        if (!getHead().getBlobs().containsKey(fileName) && !Stage.fromFile().addition.containsKey(fileName)) {
            message("No reason to remove the file.");
            System.exit(0);
        }
        File thisremoveFile = join(CWD,fileName);
        //如果文件已被暂存添加（staged for addition） → 把它从暂存区移除。
        Stage sd =  Stage.fromFile();
        sd.addition.remove(fileName);
        sd.saveStage();
        //如果文件在当前提交中被跟踪（tracked）：
        //把它标记为待删除（staged for removal）。
        //如果用户尚未删除该文件（即工作目录中还存在该文件）：
        //👉 从工作目录中将其物理删除（即 File.delete()）。
        Commit Head = getHead();
        if (Head.getBlobs().containsKey(fileName)) {
            Stage sdd =  Stage.fromFile();
            sdd.removal.put(fileName,getHead().getBlobs().get(fileName));
            sdd.saveStage();
            thisremoveFile.delete();
        }
    }

    public static void printLog() {
        Commit currentCommit = getHead();
        while (currentCommit != null){
            String sb;
            if (!currentCommit.isMergeCommit()) {
                sb = "===\n" +
                        "commit " + currentCommit.getSha() + "\n" +
                        "Date: " + currentCommit.getTimestamp() + "\n" +
                        currentCommit.getMessage();
            }
            else {
                sb = "===\n" +
                        "commit " + currentCommit.getSha() + "\n" + "Merge: " +
                        currentCommit.getParents() + "\n" +
                        "Date: " + currentCommit.getTimestamp() + "\n" +
                        currentCommit.getMessage();
            }

            message(sb);
            System.out.println(); // 再打一个空行

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
                            "commit " + currentCommit.getSha() + "\n" +
                            "Date: " + currentCommit.getTimestamp() + "\n" +
                            currentCommit.getMessage();
                } else {
                    sb = "===\n" +
                            "commit " + currentCommit.getSha() + "\n" + "Merge: " +
                            currentCommit.getParents() + "\n" +
                            "Date: " + currentCommit.getTimestamp() + "\n" +
                            currentCommit.getMessage();
                }
                message(sb);
                System.out.println(); // 再打一个空行

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



    private static void saveBranchesMap() {
        HashMap<String, String> dataToSave = new HashMap<>(branches);
        writeObject(branch, dataToSave);
    }

    @SuppressWarnings("unchecked")
    private static void BranchesMapFromFile() {
        if (branch.exists()) {
            branches = (HashMap<String, String>)readObject(branch, HashMap.class);
        } else {
            branches = new HashMap<>();  // 如果文件不存在，初始化为空
        }
    }

    private static void saveCurrentBranch() {
        String dataToSave = currentBranch;
        writeObject(currentBranchfile, dataToSave);
    }

    private static void CurrentBranchFromFile() {
        if (currentBranchfile.exists()) {
            currentBranch = readObject(currentBranchfile, String.class);
        } else {
            currentBranch = "master";  // 如果文件不存在，初始化为默认Master
        }
    }

    private static void changeBranchCommitAndSave(Commit commit) {
        BranchesMapFromFile();
        branches.put(currentBranch,commit.saveCommit());
        saveBranchesMap();
    }

    private static void changeCurrentBranch(String branch) {
        currentBranch = branch;
        changeHead(Commit.fromFile(branches.get(currentBranch)));
        saveCurrentBranch();
    }

/*
=== Branches ===
*master
other-branch

=== Staged Files ===
wug.txt
wug2.txt

=== Removed Files ===
goodbye.txt

=== Modifications Not Staged For Commit ===
junk.txt (deleted)
wug3.txt (modified)

=== Untracked Files ===
random.stuff

🟡 类型一：
Tracked in current commit，工作目录中已被修改，但 没有重新添加（未暂存）处理方式：遍历当前 commit 的 blobs（tracked 文件）
如果某文件存在于 working directory，但内容改变，且没有被暂存添加（staging area 中没有它或它的内容是旧的） ➤ 标记为 (modified)
🟡 类型二：
被暂存添加（staged for addition），但内容与工作目录中不同  处理方式：staging area for addition
比较文件在工作目录和暂存区中的内容 ➤ 内容不同则标记为 (modified)
🟡 类型三：
被暂存添加，但在工作目录中已被删除  处理方式：遍历 staging area for addition如果文件在工作目录中不存在➤ 标记为 (deleted)
🟡 类型四：
当前 commit 中被追踪的文件（blobs）未被标记删除、但在工作目录中已删除，且没有被暂存删除   处理方式：遍历当前 commit 的 blobs文件在 working directory 中不存在并且没有在 staged for removal 中
➤ 标记为 (deleted)
*/
    public static void status() {
        BranchesMapFromFile();
        CurrentBranchFromFile();
        StringBuilder message = new StringBuilder("=== Branches ===\n*" + currentBranch);
        for (String k : branches.keySet()) {
            if (!k.equals(currentBranch)) {
                message.append("\n").append(k);
            }
        }
        message(message + "\n");

        Stage sd = Stage.fromFile();
        message = new StringBuilder("=== Staged Files ===");
        for (String k : sd.addition.keySet()) {
            message.append("\n").append(k);
        }
        message(message + "\n");

        message = new StringBuilder("=== Removed Files ===");
        for (String k : sd.removal.keySet()) {
            message.append("\n").append(k);
        }
        message(message + "\n");

        message = new StringBuilder("=== Modifications Not Staged For Commit ===");

        for (String fileName : getHead().getBlobs().keySet()) {
            File file = join(CWD,fileName);
            boolean isInStagedAdd = sd.addition.containsKey(fileName);
            boolean isInStagedRemove = sd.removal.containsKey(fileName);

            if (file.exists()) {
                String workingContent = readContentsAsString(file);
                String trackedContent = readContentsAsString(join(blobsFolder,getHead().getBlobs().get(fileName)));

                if (!workingContent.equals(trackedContent) && !isInStagedAdd) {
                    message.append(fileName).append(" (modified)");
                }
            } else if (!isInStagedRemove) {
                message.append(fileName).append(" (deleted)");
            }
        }

        for (String fileName : sd.addition.keySet()) {
            File file = join(CWD,fileName);
            if (!file.exists()) {
                message.append(fileName).append(" (deleted)");
            } else {
                String workingContent = readContentsAsString(file);
                String stagedContent = readContentsAsString(join(blobsFolder,sd.addition.get(fileName)));
                if (!workingContent.equals(stagedContent)) {
                    message.append(fileName).append(" (modified)");
                }
            }
        }
        message(message + "\n");

//        “未跟踪的文件”：
//        是指：存在于工作目录中，但既没有被暂存添加，也没有被当前提交追踪。
//        包括：之前标记为删除的文件，但后来在工作目录中被重新创建，Gitlet 并不知情。

        message = new StringBuilder("=== Untracked Files ===");

        List<String> allFiles = plainFilenamesIn(CWD);

        if (allFiles != null) {
            for (String fileName : allFiles) {
                if (!getHead().getBlobs().containsKey(fileName) && !sd.addition.containsKey(fileName)) {
                    message.append("\n").append(fileName);
                }
            }
        }
        message(message + "\n");
    }
    // 从当前分支的 head commit 中取出 fileName 并覆盖工作目录中该文件
    public static void checkoutFileFromHead(String filename) {
        Commit cm = getHead();
        HashMap<String, String> thisBlobs = cm.getBlobs();
        if (!thisBlobs.containsKey(filename)) {
            message("File does not exist in that commit.");
            System.exit(0);
        } else {
            File thisFile = join(CWD,filename);
            File replaceFile = join(blobsFolder,thisBlobs.get(filename));
            writeContents(thisFile,readContentsAsString(replaceFile));
        }
    }

    private static String findCommit(String CommitSHA) {
        List<String> allCommitID = plainFilenamesIn(commitFolder);
        if (allCommitID != null) {
            for (String commitID : allCommitID) {
                if (commitID.startsWith(CommitSHA)) {
                    return commitID;
                }
            }
        }
        return null;
    }
//    从指定的 commit 中取出该文件版本，覆盖当前工作目录中的对应文件。也不会添加到暂存区。
    public static void checkoutFileFromCommit(String CommitSHA,String filename) {
        List<String> allCommit = plainFilenamesIn(commitFolder);
        if (findCommit(CommitSHA) == null) {
            message("No commit with that id exists.");
            System.exit(0);
        }
        String completeSHA = findCommit(CommitSHA);

        Commit thisCommit = Commit.fromFile(completeSHA);
        HashMap<String, String> thisBlobs = thisCommit.getBlobs();
        if (!thisBlobs.containsKey(filename)) {
            message("File does not exist in that commit.");
            System.exit(0);
        } else {
            File thisFile = join(CWD,filename);
            File replaceFile = join(blobsFolder,thisBlobs.get(filename));
            writeContents(thisFile,readContentsAsString(replaceFile));

        }
    }

//    将指定分支的最新提交中所有文件复制到工作目录（覆盖旧文件）。
//    同时，将当前分支指向该分支。
//    删除当前分支中被追踪、但在目标分支中不存在的文件。
//    清空暂存区（除非目标分支就是当前分支）。 如果当前分支中有未被追踪的文件？

    public static void checkoutBranch(String branchName) {
        BranchesMapFromFile();
        if (!branches.containsKey(branchName)) {
            message("No such branch exists.");
            System.exit(0);
        }
        CurrentBranchFromFile();
        if (currentBranch.equals(branchName)) {
            message("No need to checkout the current branch.");
            System.exit(0);
        }
        List<String> allFilesInCWD = plainFilenamesIn(CWD);
        Commit commitBeforeChange = getHead();
        Commit newBranchCommit = Commit.fromFile(branches.get(currentBranch));

        if (allFilesInCWD != null) {
            for (String fileName : allFilesInCWD) {
                boolean isTrackedInHEAD = getHead().getBlobs().containsKey(fileName);
                boolean isInStagedAdd = Stage.fromFile().addition.containsKey(fileName);
                boolean isInStagedRemove = Stage.fromFile().removal.containsKey(fileName);
                boolean isUntracked = !isTrackedInHEAD && !isInStagedAdd && !isInStagedRemove;

                if (isUntracked && newBranchCommit.getBlobs().containsKey(fileName)) {
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

            }
        }
        changeCurrentBranch(branchName);

        for (String fileName : commitBeforeChange.getBlobs().keySet()) {
            if (!newBranchCommit.getBlobs().containsKey(fileName)) {
                File fileInCWD = join(CWD, fileName);
                fileInCWD.delete();
            }
        }


        for (String newCommitBlob : newBranchCommit.getBlobs().keySet()) {
            File blobFile = join(blobsFolder,newBranchCommit.getBlobs().get(newCommitBlob));
            File copyFile = join(CWD,newCommitBlob);
            writeContents(copyFile,readContentsAsString(blobFile));
        }
        Stage.clear();
    }
    public static void Branch(String branchName) {
        BranchesMapFromFile();
        if (branches.containsKey(branchName)) {
            message("A branch with that name already exists.");
            System.exit(0);
        }
        branches.put(branchName,getHead().getSha());
        saveBranchesMap();
    }

    public static void rmBranch(String branchName) {
        BranchesMapFromFile();
        CurrentBranchFromFile();
        if (!branches.containsKey(branchName)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(currentBranch)) {
            message("Cannot remove the current branch.");
            System.exit(0);
        }
        branches.remove(branchName);
        saveBranchesMap();
    }


    public static void reset(String commitID) {
        List<String> allCommit = plainFilenamesIn(commitFolder);
        if (findCommit(commitID) == null) {
            message("No commit with that id exists.");
            System.exit(0);
        }
        String completeSHA = findCommit(commitID);

        Commit targetCommit = Commit.fromFile(completeSHA);
        HashMap<String, String> thisBlobs = targetCommit.getBlobs();
        BranchesMapFromFile();
        CurrentBranchFromFile();

        List<String> allFilesInCWD = plainFilenamesIn(CWD);
        Commit commitBeforeChange = getHead();

        if (allFilesInCWD != null) {
            for (String fileName : allFilesInCWD) {
                boolean isTrackedInHEAD = commitBeforeChange.getBlobs().containsKey(fileName);
                boolean isInStagedAdd = Stage.fromFile().addition.containsKey(fileName);
                boolean isInStagedRemove = Stage.fromFile().removal.containsKey(fileName);
                boolean isUntracked = !isTrackedInHEAD && !isInStagedAdd && !isInStagedRemove;

                if (isUntracked && targetCommit.getBlobs().containsKey(fileName)) {
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        for (String fileName : commitBeforeChange.getBlobs().keySet()) {
            if (!targetCommit.getBlobs().containsKey(fileName)) {
                File fileInCWD = join(CWD, fileName);
                fileInCWD.delete();  // 删除工作目录里旧分支特有的文件
            }
        }


        for (String newCommitBlob : targetCommit.getBlobs().keySet()) {
            File blobFile = join(blobsFolder, targetCommit.getBlobs().get(newCommitBlob));
            File copyFile = join(CWD,newCommitBlob);
            writeContents(copyFile,readContentsAsString(blobFile));
        }
        Stage.clear();

        changeHead(targetCommit);
        branches.put(currentBranch,completeSHA);
        saveBranchesMap();
    }




}
