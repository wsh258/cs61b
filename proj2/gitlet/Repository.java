package gitlet;

import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Sihao Wong
 */
public class Repository {
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    static final File COMMITFOLDER = join(GITLET_DIR, "commits");
    static final File BLOBSFOLDER = join(GITLET_DIR, "blobs");
    public static final File HEADFILE = join(GITLET_DIR, "Head");
    /**
     * 保存分支名 -> commit ID 的映射
     */
    private static HashMap<String, String> branches;

    /**
     * 当前分支名（如 "master"）
     */
    private static String currentBranch;

    static final File BRANCH = join(GITLET_DIR, "branches", "branchMap");
    static File currentBranchfile = join(GITLET_DIR, "branches", "currentBranch");

    public static void glInit() {
        if (GITLET_DIR.mkdir()) {
            COMMITFOLDER.mkdir();
            BLOBSFOLDER.mkdir();
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
        return Commit.fromFile(readContentsAsString(HEADFILE));
    }

    private static void changeHead(Commit cm) {
        writeContents(HEADFILE, cm.getSha());
    }

    public static void stagedForAddition(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            message("File does not exist.");
            System.exit(0);
        }

        String fileContent = readContentsAsString(file);
        String fileSha1 = sha1(fileContent);
        File blobFile = join(BLOBSFOLDER, fileSha1);

        Commit head = getHead();
        Stage stage = Stage.fromFile();

        // 如果当前文件内容与 HEAD 中版本相同，则不应添加到暂存区
        if (fileSha1.equals(head.getBlobs().get(fileName))) {
            // 如果之前已被暂存，则移除
            stage.getAddition().remove(fileName);
            stage.getRemoval().remove(fileName);  // 若之前标记为删除，也移除
            stage.saveStage();
            return;
        }
        // 否则，写入 blob，并记录在 addition 中
        writeContents(blobFile, fileContent);
        stage.getAddition().put(fileName, fileSha1);
        stage.getRemoval().remove(fileName); // 若之前标记为删除，取消删除
        stage.saveStage();
    }


    private static String getFormattedTimestamp() {
        ZonedDateTime now = ZonedDateTime.now();  // 使用本地时区
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "E MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return now.format(formatter);
    }

    public static void commitCommands(String message) {

        if (Stage.fromFile().getAddition().isEmpty()
                && Stage.fromFile().getRemoval().isEmpty()) {
            message("No changes added to the commit.");
            System.exit(0);
        }

        Commit head = getHead();
        Commit newCommit = new Commit(message, getFormattedTimestamp(), head.getSha());

        newCommit.addBlobs(head.getBlobs(), null);  // 这样 newCommit 改的只是自己的 blobs
        Stage sd = Stage.fromFile();
        newCommit.addBlobs(sd.getAddition(), sd.getRemoval());
        Stage stage = new Stage();
        stage.saveStage();
        changeHead(newCommit);
        changeBranchCommitAndSave(newCommit);
        newCommit.saveCommit();
    }

    public static void stagedForRemoval(String fileName) {
        if (!getHead().getBlobs().containsKey(fileName)
                && !Stage.fromFile().getAddition().containsKey(fileName)) {
            message("No reason to remove the file.");
            System.exit(0);
        }
        File thisremoveFile = join(CWD, fileName);
        //如果文件已被暂存添加（staged for addition） → 把它从暂存区移除。
        Stage sd = Stage.fromFile();
        sd.getAddition().remove(fileName);
        sd.saveStage();
        //如果文件在当前提交中被跟踪（tracked）：
        //把它标记为待删除（staged for removal）。
        //如果用户尚未删除该文件（即工作目录中还存在该文件）：
        //👉 从工作目录中将其物理删除（即 File.delete()）。
        Commit head = getHead();
        if (head.getBlobs().containsKey(fileName)) {
            Stage sdd = Stage.fromFile();
            sdd.getRemoval().put(fileName, getHead().getBlobs().get(fileName));
            sdd.saveStage();
            thisremoveFile.delete();
        }
    }

    public static void printLog() {
        Commit currentCommit = getHead();
        while (currentCommit != null) {
            String sb;
            if (!currentCommit.isMergeCommit()) {
                sb = "===\n" + "commit " + currentCommit.getSha() + "\n"
                        + "Date: " + currentCommit.getTimestamp() + "\n"
                        + currentCommit.getMessage();
            } else {
                sb = "===\n"
                        + "commit " + currentCommit.getSha() + "\n" + "Merge: "
                        + currentCommit.getParents() + "\n" + "Date: "
                        + currentCommit.getTimestamp() + "\n"
                        + currentCommit.getMessage();
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
        List<String> allCommit = plainFilenamesIn(COMMITFOLDER);
        if (allCommit != null) {
            for (String currentCommitStr : allCommit) {
                String sb;
                Commit currentCommit = Commit.fromFile(currentCommitStr);
                if (!currentCommit.isMergeCommit()) {
                    sb = "===\n"
                            + "commit " + currentCommit.getSha() + "\n"
                            + "Date: " + currentCommit.getTimestamp() + "\n"
                            + currentCommit.getMessage();
                } else {
                    sb = "===\n" + "commit " + currentCommit.getSha() + "\n" + "Merge: "
                            + currentCommit.getParents() + "\n"
                            + "Date: " + currentCommit.getTimestamp() + "\n"
                            + currentCommit.getMessage();
                }
                message(sb);
                System.out.println(); // 再打一个空行

            }
        }
    }

    public static void find(String message) {
        List<String> allCommit = plainFilenamesIn(COMMITFOLDER);
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
        writeObject(BRANCH, dataToSave);
    }

    @SuppressWarnings("unchecked")
    private static void branchesMapFromFile() {
        if (BRANCH.exists()) {
            branches = (HashMap<String, String>) readObject(BRANCH, HashMap.class);
        } else {
            branches = new HashMap<>();  // 如果文件不存在，初始化为空
        }
    }

    private static void saveCurrentBranch() {
        String dataToSave = currentBranch;
        writeObject(currentBranchfile, dataToSave);
    }

    private static void currentBranchFromFile() {
        if (currentBranchfile.exists()) {
            currentBranch = readObject(currentBranchfile, String.class);
        } else {
            currentBranch = "master";  // 如果文件不存在，初始化为默认Master
        }
    }

    private static void changeBranchCommitAndSave(Commit commit) {
        branchesMapFromFile();
        currentBranchFromFile();  // ✅ 加上这行！
        branches.put(currentBranch, commit.saveCommit());
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
    当前 commit 中被追踪的文件（blobs）未被标记删除、但在工作目录中已删除，且没有被暂存删除
    处理方式：遍历当前 commit 的 blobs文件在 working directory 中不存在并且没有在 staged for removal 中
    ➤ 标记为 (deleted)
    */
    public static void status() {
        branchesMapFromFile();
        currentBranchFromFile();
        StringBuilder message = new StringBuilder("=== Branches ===\n*" + currentBranch);
        for (String k : branches.keySet()) {
            if (!k.equals(currentBranch)) {
                message.append("\n").append(k);
            }
        }
        message(message + "\n");

        Stage sd = Stage.fromFile();
        message = new StringBuilder("=== Staged Files ===");
        for (String k : sd.getAddition().keySet()) {
            message.append("\n").append(k);
        }
        message(message + "\n");

        message = new StringBuilder("=== Removed Files ===");
        for (String k : sd.getRemoval().keySet()) {
            message.append("\n").append(k);
        }
        message(message + "\n");

        message = new StringBuilder("=== Modifications Not Staged For Commit ===");

        for (String fileName : getHead().getBlobs().keySet()) {
            File file = join(CWD, fileName);
            boolean isInStagedAdd = sd.getAddition().containsKey(fileName);
            boolean isInStagedRemove = sd.getRemoval().containsKey(fileName);

            if (file.exists()) {
                String workingContent = readContentsAsString(file);
                String trackedContent = readContentsAsString(join(BLOBSFOLDER,
                        getHead().getBlobs().get(fileName)));

                if (!workingContent.equals(trackedContent) && !isInStagedAdd) {
                    message.append("\n").append(fileName).append(" (modified)");
                }
            } else if (!isInStagedRemove) {
                message.append(fileName).append(" (deleted)");
            }
        }

        for (String fileName : sd.getAddition().keySet()) {
            File file = join(CWD, fileName);
            if (!file.exists()) {
                message.append(fileName).append(" (deleted)");
            } else {
                String workingContent = readContentsAsString(file);
                String stagedContent = readContentsAsString(join(BLOBSFOLDER,
                        sd.getAddition().get(fileName)));
                if (!workingContent.equals(stagedContent)) {
                    message.append(fileName).append(" (modified)");
                }
            }
        }
        message(message + "\n");

        //“未跟踪的文件”：
        //是指：存在于工作目录中，但既没有被暂存添加，也没有被当前提交追踪。
        //包括：之前标记为删除的文件，但后来在工作目录中被重新创建，Gitlet 并不知情。

        message = new StringBuilder("=== Untracked Files ===");

        List<String> allFiles = plainFilenamesIn(CWD);

        if (allFiles != null) {
            for (String fileName : allFiles) {
                if (!getHead().getBlobs().containsKey(fileName)
                        && !sd.getAddition().containsKey(fileName)) {
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
            File thisFile = join(CWD, filename);
            File replaceFile = join(BLOBSFOLDER, thisBlobs.get(filename));
            writeContents(thisFile, readContentsAsString(replaceFile));
        }
    }

    private static String findCommit(String commitSHA) {
        List<String> allCommitID = plainFilenamesIn(COMMITFOLDER);
        if (allCommitID != null) {
            for (String commitID : allCommitID) {
                if (commitID.startsWith(commitSHA)) {
                    return commitID;
                }
            }
        }
        return null;
    }

    //从指定的 commit 中取出该文件版本，覆盖当前工作目录中的对应文件。也不会添加到暂存区。
    public static void checkoutFileFromCommit(String commitSHA, String filename) {
        List<String> allCommit = plainFilenamesIn(COMMITFOLDER);
        if (findCommit(commitSHA) == null) {
            message("No commit with that id exists.");
            System.exit(0);
        }
        String completeSHA = findCommit(commitSHA);

        Commit thisCommit = Commit.fromFile(completeSHA);
        HashMap<String, String> thisBlobs = thisCommit.getBlobs();
        if (!thisBlobs.containsKey(filename)) {
            message("File does not exist in that commit.");
            System.exit(0);
        } else {
            File thisFile = join(CWD, filename);
            File replaceFile = join(BLOBSFOLDER, thisBlobs.get(filename));
            writeContents(thisFile, readContentsAsString(replaceFile));

        }
    }

    //    将指定分支的最新提交中所有文件复制到工作目录（覆盖旧文件）。
    //    同时，将当前分支指向该分支。
    //    删除当前分支中被追踪、但在目标分支中不存在的文件。
    //    清空暂存区（除非目标分支就是当前分支）。 如果当前分支中有未被追踪的文件？
    public static void checkoutBranch(String branchName) {
        branchesMapFromFile();
        if (!branches.containsKey(branchName)) {
            message("No such branch exists.");
            System.exit(0);
        }
        currentBranchFromFile();
        if (currentBranch.equals(branchName)) {
            message("No need to checkout the current branch.");
            System.exit(0);
        }
        List<String> allFilesInCWD = plainFilenamesIn(CWD);
        Commit commitBeforeChange = getHead();
        Commit newBranchCommit = Commit.fromFile(branches.get(branchName));
        if (allFilesInCWD != null) {
            Stage sd = Stage.fromFile();
            for (String fileName : allFilesInCWD) {
                boolean isTrackedInHEAD = commitBeforeChange.getBlobs().containsKey(fileName);
                boolean isInStagedAdd = sd.getAddition().containsKey(fileName);
                boolean isInStagedRemove = sd.getRemoval().containsKey(fileName);
                boolean isUntracked = !isTrackedInHEAD && !isInStagedAdd && !isInStagedRemove;

                if (isUntracked
                        && newBranchCommit.getBlobs().containsKey(fileName)) {
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

            }
        }


        for (String fileName : commitBeforeChange.getBlobs().keySet()) {
            if (!newBranchCommit.getBlobs().containsKey(fileName)) {
                File fileInCWD = join(CWD, fileName);
                fileInCWD.delete();
            }
        }


        for (String newCommitBlob : newBranchCommit.getBlobs().keySet()) {
            File blobFile = join(BLOBSFOLDER, newBranchCommit.getBlobs().get(newCommitBlob));
            File copyFile = join(CWD, newCommitBlob);
            writeContents(copyFile, readContentsAsString(blobFile));
        }
        currentBranch = branchName;
        saveCurrentBranch();
        changeHead(newBranchCommit);
        Stage.clear();
    }


    public static void branch(String branchName) {
        branchesMapFromFile();
        if (branches.containsKey(branchName)) {
            message("A branch with that name already exists.");
            System.exit(0);
        }
        branches.put(branchName, getHead().getSha());
        saveBranchesMap();
    }

    public static void rmBranch(String branchName) {
        branchesMapFromFile();
        currentBranchFromFile();
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
        List<String> allCommit = plainFilenamesIn(COMMITFOLDER);
        if (findCommit(commitID) == null) {
            message("No commit with that id exists.");
            System.exit(0);
        }
        String completeSHA = findCommit(commitID);

        Commit targetCommit = Commit.fromFile(completeSHA);
        HashMap<String, String> thisBlobs = targetCommit.getBlobs();
        branchesMapFromFile();
        currentBranchFromFile();

        List<String> allFilesInCWD = plainFilenamesIn(CWD);
        Commit commitBeforeChange = getHead();


        if (allFilesInCWD != null) {
            for (String fileName : allFilesInCWD) {
                boolean isTrackedInHEAD = commitBeforeChange.getBlobs().containsKey(fileName);
                boolean isInStagedAdd = Stage.fromFile().getAddition().containsKey(fileName);
                boolean isInStagedRemove = Stage.fromFile().getRemoval().containsKey(fileName);
                boolean isUntracked = !isTrackedInHEAD && !isInStagedAdd && !isInStagedRemove;

                if (isUntracked && targetCommit.getBlobs().containsKey(fileName)) {
                    message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
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
            File blobFile = join(BLOBSFOLDER, targetCommit.getBlobs().get(newCommitBlob));
            File copyFile = join(CWD, newCommitBlob);
            writeContents(copyFile, readContentsAsString(blobFile));
        }
        Stage.clear();

        changeHead(targetCommit);
        branches.put(currentBranch, completeSHA);
        saveBranchesMap();
    }


    public static void merge(String targetBranch) {
        currentBranchFromFile();
        branchesMapFromFile();
        boolean hasConflict = false;
        if (!Stage.fromFile().getAddition().isEmpty() || !Stage.fromFile().getRemoval().isEmpty()) {
            message("You have uncommitted changes.");
            System.exit(0);
        }
        if (!branches.containsKey(targetBranch)) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (currentBranch.equals(targetBranch)) {
            message("Cannot merge a branch with itself.");
            System.exit(0);
        }
        List<String> allFilesInCWD = plainFilenamesIn(CWD);
        HashMap<String, String> targetBlobs = Commit.fromFile(branches.get(targetBranch)).getBlobs();
        if (allFilesInCWD != null) {
            for (String fileName : allFilesInCWD) {
                if (!getHead().getBlobs().containsKey(fileName)
                        && targetBlobs.containsKey(fileName)) {
                    message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        Commit targetCommit = Commit.fromFile(branches.get(targetBranch));
        Commit splitPoint = getSplitPoint(targetBranch);
        if (splitPoint.getSha().equals(branches.get(targetBranch))) {
            message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPoint.getSha().equals(getHead().getSha())) {
            checkoutBranch(targetBranch);
            message("Current branch fast-forwarded.");
            System.exit(0);
        }
        HashMap<String, String> splitPointBlobs = splitPoint.getBlobs();
        HashMap<String, String> currentBlobs = getHead().getBlobs();
        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(splitPointBlobs.keySet());
        allFiles.addAll(currentBlobs.keySet());
        allFiles.addAll(targetBlobs.keySet());
        hasConflict = processMergeFiles(splitPoint, targetCommit, splitPointBlobs, currentBlobs, targetBlobs);
        if (!hasConflict) {
            commitCommands("Merged " + targetBranch + " into " + currentBranch + ".");
        } else {
            Commit head = getHead();
            String message = "Merged " + targetBranch + " into " + currentBranch + ".";
            Commit newCommit = new Commit(message, getFormattedTimestamp(), head.getSha(), branches.get(targetBranch));
            newCommit.addBlobs(head.getBlobs(), null);  // 这样 newCommit 改的只是自己的 blobs
            newCommit.addBlobs(Stage.fromFile().getAddition(), Stage.fromFile().getRemoval());
            Stage.clear();
            changeHead(newCommit);
            changeBranchCommitAndSave(newCommit);
            newCommit.saveCommit();
        }
    }
    private static boolean processMergeFiles(Commit splitPoint, Commit targetCommit,
                                             HashMap<String, String> splitPointBlobs,
                                             HashMap<String, String> currentBlobs,
                                             HashMap<String, String> targetBlobs) {
        boolean hasConflict = false;
        HashSet<String> allFiles = new HashSet<>();
        allFiles.addAll(splitPointBlobs.keySet());
        allFiles.addAll(currentBlobs.keySet());
        allFiles.addAll(targetBlobs.keySet());

        for (String blobsName : allFiles) {
            String splitBlob = splitPointBlobs.get(blobsName);
            String currentBlob = currentBlobs.get(blobsName);
            String targetBlob = targetBlobs.get(blobsName);

            // Case 1: current 没变，target 改了 -> 使用 target 内容
            if (!Objects.equals(targetBlob, currentBlob)
                    && Objects.equals(currentBlob, splitBlob)) {

                // 如果 current 已删除，但 split 有，而 target 恢复了 -> 冲突！
                if (currentBlob == null && splitBlob != null && targetBlob != null) {
                    hasConflict = true;
                    handleConflict(blobsName, currentBlobs, targetBlobs);
                    System.out.println("Encountered a merge conflict.");
                } else if (targetBlob != null) {
                    checkoutFileFromCommit(targetCommit.getSha(), blobsName);
                    stagedForAddition(blobsName);
                } else {
                    stagedForRemoval(blobsName);
                }

                // Case 2: target 没变，current 改了 -> 忽略
            } else if (!Objects.equals(currentBlob, splitBlob)
                    && Objects.equals(targetBlob, splitBlob)) {
                continue;

                // Case 3: target 和 current 都未改 -> 忽略
            } else if (Objects.equals(targetBlob, currentBlob)) {
                continue;

                // Case 4: split 有，current 和 target 都删了 -> 移除文件
            } else if (splitBlob != null && currentBlob == null && targetBlob == null) {
                stagedForRemoval(blobsName);

                // Case 5: 有冲突 -> 生成冲突内容
            } else {
                hasConflict = true;
                handleConflict(blobsName, currentBlobs, targetBlobs);
                System.out.println("Encountered a merge conflict.");
            }
        }
        return hasConflict;
    }


    private static void handleConflict(String fileName, Map<String, String> currentBlobs,
                                       Map<String, String> targetBlobs) {
        String currentContent = "";
        String targetContent = "";

        if (currentBlobs.containsKey(fileName)) {
            currentContent = readContentsAsString(join(BLOBSFOLDER, currentBlobs.get(fileName)));
        }
        if (targetBlobs.containsKey(fileName)) {
            targetContent = readContentsAsString(join(BLOBSFOLDER, targetBlobs.get(fileName)));
        }
        String merged = "<<<<<<< HEAD\n" + currentContent
                + "=======\n" + targetContent + ">>>>>>>\n";
        writeContents(join(CWD, fileName), merged);
        stagedForAddition(fileName);
    }


    private static Set<String> getAllAncestors(String startSha) {
        Set<String> result = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(startSha);
        while (!queue.isEmpty()) {
            String sha = queue.poll();
            if (sha == null || result.contains(sha)) {
                continue;
            }
            result.add(sha);
            Commit c = Commit.fromFile(sha);
            if (c.getParent() != null) {
                queue.offer(c.getParent());
            }
            if (c.get2Parent() != null) {
                queue.offer(c.get2Parent());
            }
        }
        return result;

    }

    /**
     * 找到当前分支 HEAD 与 targetBranch 的最近公共祖先（split point）。
     */
    private static int getAbsoluteDepth(String commitSha, Map<String, Integer> cache) {
        if (commitSha == null) {
            return -1;
        }
        if (cache.containsKey(commitSha)) {
            return cache.get(commitSha);
        }

        Commit commit = Commit.fromFile(commitSha);
        // 递归调用时，必须把cache传下去
        int depth1 = getAbsoluteDepth(commit.getParent(), cache);
        int depth2 = getAbsoluteDepth(commit.get2Parent(), cache);

        int currentDepth = 1 + Math.max(depth1, depth2);
        cache.put(commitSha, currentDepth);
        return currentDepth;
    }


    // 在你的getSplitPoint方法里使用它：
    private static Commit getSplitPoint(String targetBranch) {
        // 1. 找到所有共同祖先
        currentBranchFromFile();
        branchesMapFromFile();

        // 获取两个分支所有祖先 SHA
        String headSha = getHead().getSha();
        String targetSha = branches.get(targetBranch);
        Set<String> ancestorsA = getAllAncestors(headSha);
        Set<String> ancestorsB = getAllAncestors(targetSha);
        Set<String> commonAncestors = new HashSet<>(ancestorsA);
        commonAncestors.retainAll(ancestorsB);

        // 2. 遍历共同祖先，找到绝对深度最大的那个
        int maxDepth = -1;
        String lcaSha = null;
        Map<String, Integer> depthCache = new HashMap<>();

        for (String sha : commonAncestors) {
            int depth = getAbsoluteDepth(sha,depthCache);
            if (depth > maxDepth) {
                maxDepth = depth;
                lcaSha = sha;
            }
        }

        // 清空缓存，为下次调用做准备（或者你可以让它一直存在，作为全局缓存）
        // depthCache.clear();

        return Commit.fromFile(lcaSha);
    }




}
