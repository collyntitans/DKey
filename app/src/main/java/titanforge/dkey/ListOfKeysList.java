package titanforge.dkey;

/**
 * Created by nicky on 11/15/16.
 */

public class ListOfKeysList {
    private String roomName;
    private String availableFrom;
    private String availableUntil;
    private String dateRedeemed;

    public String getRoomName(){
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getAvailableFrom() {
        return availableFrom;
    }

    public void setAvailableFrom(String availableFrom) {
        this.availableFrom = availableFrom;
    }

    public String getAvailableUntil() {
        return availableUntil;
    }

    public void setAvailableUntil(String availableUntil) {
        this.availableUntil = availableUntil;
    }

    public String getDateRedeemed() {
        return dateRedeemed;
    }

    public void setDateRedeemed(String dateRedeemed) {
        this.dateRedeemed = dateRedeemed;
    }


    public ListOfKeysList(String roomName, String availableFrom, String availableUntil, String dateRedeemed) {
        this.roomName = ": " + roomName;
        availableFrom = availableFrom.substring(0,2) + "-" + availableFrom.substring(2,4) + "-" + availableFrom.substring(4,8);
        this.availableFrom = ": " + availableFrom;
        availableUntil = availableUntil.substring(0,2) + "-" + availableUntil.substring(2,4) + "-" + availableUntil.substring(4,8);
        this.availableUntil = ": " + availableUntil;
        this.dateRedeemed = ": " + dateRedeemed;

    }

}
