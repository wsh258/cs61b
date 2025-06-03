package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Repository.CWD;
import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;
import static gitlet.Utils.readContentsAsString;
import static gitlet.Utils.sha1;

public class Stage implements Serializable {
    public HashMap<String, String> addition= new HashMap<>();
    public HashMap<String, String> removal= new HashMap<>();
    public static final File StageFolder = join(GITLET_DIR, ".stage");

    public void saveStage() {
        File f = join(StageFolder, "sd");
        writeObject(f,this);
    }

    public static Stage fromFile() {
        File existStage = join(StageFolder, "sd");
        if (!existStage.exists()) {
            return new Stage();
        }
        return readObject(existStage, Stage.class);
    }

}
