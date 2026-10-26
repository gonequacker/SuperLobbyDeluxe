package com.pedrojm96.superlobby;

import java.util.ArrayList;
import java.util.List;

public class RegionRule {

    private final String id;
    private boolean disableHubMode = false;
    private boolean removeHubItems = false;
    private boolean restoreHubItemsOnLeave = true;
    private List<String> enterCommands = new ArrayList<String>();
    private List<String> leaveCommands = new ArrayList<String>();

    public RegionRule(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }
    ////
    public boolean isDisableHubMode(){
        return disableHubMode;
    }
    public void setDisableHubMode(boolean disableHubMode) {
        this.disableHubMode = disableHubMode;
    }

    ////
    public boolean isRemoveHubItems() {
        return removeHubItems;
    }

    public void setRemoveHubItems(boolean removeHubItems) {
        this.removeHubItems = removeHubItems;
    }

    ////
    public boolean isRestoreHubItemsOnLeave() {
        return restoreHubItemsOnLeave;
    }

    public void setRestoreHubItemsOnLeave(boolean restoreHubItemsOnLeave) {
        this.restoreHubItemsOnLeave = restoreHubItemsOnLeave;
    }

    ////
    public List<String> getEnterCommands(){
        return enterCommands;
    }

    public void setEnterCommands(List<String> enterCommands){
        this.enterCommands = (enterCommands != null) ? enterCommands : new ArrayList<String>();
    }

    ////
    public List<String> getLeaveCommands(){
        return leaveCommands;
    }

    public void setLeaveCommands(List<String> leaveCommands){
        this.leaveCommands = (leaveCommands != null) ? leaveCommands : new ArrayList<String>();
    }
}

