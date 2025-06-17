package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Utils.*;

public class Stage implements Serializable {
    private HashMap<String, String> addition = new HashMap<>();
    private HashMap<String, String> removal = new HashMap<>();
    public static final File STAGE_FOLDER = join(GITLET_DIR, ".stage");

    public void saveStage() {
        if (!STAGE_FOLDER.exists()) {
            STAGE_FOLDER.mkdir();
        }
        File f = join(STAGE_FOLDER, "sd");
        writeObject(f, this);
    }

    public static Stage fromFile() {
        File existStage = join(STAGE_FOLDER, "sd");
        if (!existStage.exists()) {
            return new Stage();
        }
        return readObject(existStage, Stage.class);
    }

    public HashMap<String, String> getAddition() {
        return addition;
    }

    public HashMap<String, String> getRemoval() {
        return removal;
    }

    public static void clear() {
        Stage stage = new Stage();
        stage.saveStage(); //清空Stage
    }
}
