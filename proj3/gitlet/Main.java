package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        // FILL THIS IN
        switch (args[0]) {
            case "init":
                Repo.init();
                break;
            case "add":
                Repo.add(args[1]);
                break;
            case "commit":
                Repo.commit(args[1]);
                break;
            case "log":
                Repo.log();
                break;
            case "checkout":
                if (args.length == 3) {
                    Repo.checkout(args[2]);
                    break;
                } else if (args.length == 4) {
                    Repo.checkout2(args[1], args[3]);
                }
                break;
            case "rm":
                Repo.rm(args[2]);
                break;
            case "global-log":
                Repo.globalLog();
                break;
        }
        //error for bad command
        return;
    }

}
