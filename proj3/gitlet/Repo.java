package gitlet;

//

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Repo implements Serializable {

    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");

    static final File STAGING_FOLDER = Utils.join(GITLET_FOLDER, "/staging");

    static final File COMMIT_FOLDER = Utils.join(GITLET_FOLDER, "/commits");

    static final File BLOB_FOLDER = Utils.join(GITLET_FOLDER, "/blobs");

//    static final File BRANCHES_FOLDER = Utils.join(GITLET_FOLDER, "/branches");

    /**files for serialization*/
    static final File HEAD_FILE = Utils.join(GITLET_FOLDER, "/head");
    static final File STAGE_FILE = Utils.join(GITLET_FOLDER, "/stage_add");
    static final File BRANCH_FILE = Utils.join(GITLET_FOLDER, "/branches");
    static final File CURRENT_BRANCH_FILE = Utils.join(GITLET_FOLDER, "/currentBranch");

    static final File COMMIT_HISTORY_FILE = Utils.join(GITLET_FOLDER, "/commitHistory");

    public static Commit HEAD;

    private static String MASTER =  "master";

    private static StagingArea stage;

    private static String currentBranch = "";

    private static HashMap <String, String> branchesHash = new HashMap<>();

    private static LinkedHashMap<String, Commit> commitHistory = new LinkedHashMap<>();

    public static void init() {
        if (!GITLET_FOLDER.exists()) {
            setupPersistence();
            try {
                HEAD_FILE.createNewFile();
                CURRENT_BRANCH_FILE.createNewFile();
                BRANCH_FILE.createNewFile();
                STAGE_FILE.createNewFile();
                COMMIT_HISTORY_FILE.createNewFile();
            }
            catch (IOException err) {
                return;
            }

            Commit initialCommit = new Commit();
            String initCommitSHA1 = Utils.sha1(Utils.serialize(initialCommit));
            File _initialCommitFile = Utils.join(COMMIT_FOLDER, (initCommitSHA1));

            if (!_initialCommitFile.exists()) {
                try {
                    _initialCommitFile.createNewFile();
                } catch (IOException err) {
                    return;
                }
            }
            Utils.writeObject(_initialCommitFile, initialCommit);

            branchesHash.put("master", initCommitSHA1);

            Utils.writeObject(BRANCH_FILE, branchesHash);
            Utils.writeObject(HEAD_FILE, initialCommit);
            Utils.writeObject(CURRENT_BRANCH_FILE, "master");

            commitHistory.put(initCommitSHA1, initialCommit);
            Utils.writeObject(COMMIT_HISTORY_FILE, commitHistory);

            stage = new StagingArea();
            Utils.writeObject(STAGE_FILE, new StagingArea());
        }
        else {
            System.out.println("Gitlet already exists");
        }
    }

    public static void add(String fileName) {

        File toAdd = Utils.join(CWD, fileName);

        HEAD = getHeadCommit();
        stage = getStage();

//        if (!toAdd.exists()) {
//            System.out.println("file doesn't exist");
//            return;
//        }

        if (toAdd.exists()) {
            byte[] currentFile = HEAD.get_Blobs().get(fileName);
            byte[] newFile = Utils.readContents(toAdd);
//            String fileSHA = Utils.sha1(newFile);

            if (Arrays.equals(newFile, currentFile)) {
                if (stage.get_stageAddition().containsKey(fileName)) {
                    stage.get_stageAddition().remove(fileName);
                }
                if (stage.get_stageRemoval().containsKey(fileName)) {
                    stage.get_stageRemoval().remove(fileName);
                }

                setStage(stage);
                return;
            }


            if (stage.get_stageRemoval().containsKey(fileName)) {
                try {
                    toAdd.createNewFile();
                }
                catch (IOException exc) {
                    return;
                }

                Utils.writeContents(toAdd, stage.get_stageRemoval().get(fileName));
                stage.get_stageRemoval().remove(fileName);
                setStage(stage);
            }


            stage.get_stageAddition().put(fileName, newFile);
            setHeadCommit(HEAD);
            setStage(stage);

        }
        else {
            System.out.println("file does not exist");
            return;
        }



        /**Case 1: If the current working version of the file is identical to the version in the current commit,
         * do not stage it to be added, and remove it from the staging area if it is already there*/


    }

    public static void setStage(StagingArea a) {
        Utils.writeObject(STAGE_FILE, a);

    }
    public static StagingArea getStage() {
        return Utils.readObject(STAGE_FILE, StagingArea.class);
    }

    public static Commit getHeadCommit() {
        return Utils.readObject(HEAD_FILE, Commit.class);
    }

    public static void setHeadCommit(Commit c) {
        Utils.writeObject(HEAD_FILE, c);
    }

    public static HashMap<String, String> getBranches()
    {
        return Utils.readObject(BRANCH_FILE, HashMap.class);
    }


    public static LinkedHashMap<String, Commit> getCommitHistory(){
        return Utils.readObject(COMMIT_HISTORY_FILE, LinkedHashMap.class);
    }

    public static void setCommitHistory(LinkedHashMap l){
        Utils.writeObject(COMMIT_HISTORY_FILE, l);
    }

    public static void setBranches(HashMap h)
    {
        Utils.writeObject(BRANCH_FILE, h);
    }

    public static String getBranchName() {
        return Utils.readObject(CURRENT_BRANCH_FILE, String.class);
    }

    public static void setupPersistence() {
        if (!GITLET_FOLDER.exists()) {
            GITLET_FOLDER.mkdir();
            STAGING_FOLDER.mkdir();
            COMMIT_FOLDER.mkdir();
        }
    }

    public static void commit(String message) {


        if (message == "") {
            System.out.println("please enter commit message");
            return;
        }

        HEAD = getHeadCommit();
        stage = getStage();
        commitHistory = getCommitHistory();

        if (stage.get_stageAddition().isEmpty() && stage.get_stageRemoval().isEmpty()) {
            System.out.print("no changes to the commit");
            return;
        }

        HashMap<String, byte[]> tempClone = new HashMap<>();

        if (HEAD.get_Blobs() != null) {
            tempClone.putAll(HEAD.get_Blobs());
        }

        ArrayList<String> filesToAdd = new ArrayList<>(stage.get_stageAddition().keySet());
        ArrayList<String> filesToRemove = new ArrayList<>(stage.get_stageRemoval().keySet());

        /**the new commit*/
        Commit commitClone = new Commit(message, tempClone, Utils.sha1(Utils.serialize(HEAD)));

        for (String s: filesToAdd) {
            commitClone.setBlobs(s, stage.get_stageAddition().get(s), "add");
        }

        for (String s: filesToRemove) {
            commitClone.setBlobs(s, stage.get_stageRemoval().get(s), "rm");
        }

        stage.clear();
        setStage(stage);
        setHeadCommit(commitClone);

        File newFile = Utils.join(COMMIT_FOLDER, Utils.sha1(Utils.serialize(commitClone)));
        try {
            newFile.createNewFile();
        } catch (IOException excp) {
            return;
        }
        String newCommitSHA1 = Utils.sha1(Utils.serialize(commitClone));

        /**
        String currentBranch = Utils.readObject(CURRENT_BRANCH_FILE, String.class);


        branchesHash = getBranches();
        branchesHash.put(currentBranch, newCommitSHA1);

        setBranches(branchesHash);
*/
        Utils.writeObject(newFile, commitClone);


//        Utils.writeObject(newFile, Utils.serialize(commitClone));

        commitHistory.put(newCommitSHA1, commitClone);
//        Utils.writeObject(COMMIT_HISTORY_FILE, commitHistory);
        setCommitHistory(commitHistory);

        // read from my computer the HEAD commit and staging area

        // clone the HEAD commit
        // modify its message and timestamp according to user input
        // use staging area in order to modify the files tracked by the new commit

        // ask the computer: did we make any new objects that need to be saved onto the computer? or do we need to modify them?
        // write back any new objects made or modified
    }

    public static void checkout (String fileName) {
        HEAD = getHeadCommit(); //current branch
        byte[] currentBlobContents = HEAD.get_Blobs().get(fileName);

        if (!HEAD.get_Blobs().containsKey(fileName)) {
            System.out.println("file doesn't exist in the commit");
            return;
        }
        //get the byte array that the sha returns
        // maintain sha1 id to byte array

        /**Make sure that you are using Utils.writeContents() instead of Utils.writeObject()
         * when you are writing a string into a file!*/
        Utils.writeContents(Utils.join(CWD, fileName), currentBlobContents);

    }

    public static void checkout2 (String commitID, String fileName) {

        LinkedHashMap<String, Commit> tempHistory =  new LinkedHashMap<>();
        tempHistory.putAll(getCommitHistory());
//
//        System.out.println(Utils.readObject(COMMIT_HISTORY_FILE, LinkedHashMap.class));

        File newFile = Utils.join(COMMIT_FOLDER, commitID);
        if (!tempHistory.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
         {
            try {
                Commit newCommit = Utils.readObject(newFile, Commit.class);
                if (!newCommit.get_Blobs().containsKey(fileName)) {
                    System.out.println("File does not exist in that commit.");
                    return;
                }
//                String blobSHA = Utils.sha1(Utils.serialize(newCommit.get_Blobs().get(fileName)));
                Utils.writeContents(Utils.join(CWD, fileName), newCommit.get_Blobs().get(fileName));
            } catch (IllegalArgumentException excp) {
                return;
            }
        }
    }

    static void log() {
        Commit tempHead = getHeadCommit();
        LinkedHashMap<String, Commit> tempCommitHist = getCommitHistory();
//        System.out.println("commit history:" + tempCommitHist);

        if (tempHead == null) {
            return;
        }
        while (tempHead.getParentSHA() != null) {
            System.out.println("===");
            System.out.println("commit " + Utils.sha1(Utils.serialize(tempHead)));
            System.out.println("Date: " + tempHead.get_time());
            System.out.println(tempHead.get_message() + "\n");

//            tempHead = tempCommitHist.get(tempHead.getParentSHA());
            tempHead = Utils.readObject(Utils.join(COMMIT_FOLDER, tempHead.getParentSHA()), Commit.class);
        }

        System.out.println("===");
        System.out.println("commit " + Utils.sha1(Utils.serialize(tempHead)));
        System.out.println("Date: " + tempHead.get_time());
        System.out.println(tempHead.get_message() + "\n");

    }

}
