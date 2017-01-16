package titanforge.dkey;

import java.util.Objects;

/**
 * Created by nicky on 10/28/16.
 */


public class CustListContent {
    private String unlockDoorName;
    private String desc;
    private String status;
    private String deviceAddress;
    private Boolean isCheckIn;

    public CustListContent(String unlockDoorName, String status, String deviceAddress, String date) {
        this.unlockDoorName = unlockDoorName;
        if(status.equals("notCheckIn")) {
            this.status = "Status : Need Check-In";
            isCheckIn = false;
        }else{
            this.status = "Status : Check-In";
            isCheckIn = true;
        }
        this.desc = date;
        this.deviceAddress = deviceAddress;
    }

    public String getUnlockDoorName() {
        return unlockDoorName;
    }

    public void setUnlockDoorName(String name) {
        this.unlockDoorName = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDeviceAddress(){
        return deviceAddress;
    }

    public Boolean getCheckIn() {
        return isCheckIn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
