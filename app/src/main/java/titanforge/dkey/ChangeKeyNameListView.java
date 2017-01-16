package titanforge.dkey;

/**
 * Created by Nicky Fandino on 12/6/2016.
 */

public class ChangeKeyNameListView {
    private String keyName;
    private long keyID;

    public ChangeKeyNameListView(String keyName, long keyID){
        this.keyName = keyName;
        this.keyID = keyID;
    }

    public String getKeyName() {
        return keyName;
    }

    public long getKeyID() {
        return keyID;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public void setKeyID(long keyID) {
        this.keyID = keyID;
    }
}
