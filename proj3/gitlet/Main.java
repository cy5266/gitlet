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
                if (args[1] != null) {
                    Repo.commit(args[1]);
                }
                break;
            case "log":
                Repo.log();
                break;
            case "checkout":
                Repo.checkout(args[1]);
                break;
        }
        //error for bad command
        return;
    }

}
