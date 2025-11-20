package device.sdk.sample.rfid.custom;

import device.common.rfid.AccessTag;
import device.common.rfid.RFIDConst;

public class OperationResult extends AccessTag {
    private boolean isSuccess = false;
    private int commandResult = RFIDConst.CommandErr.COMM_ERR;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public int getCommandResult() {
        return commandResult;
    }

    public void setCommandResult(int commandResult) {
        this.commandResult = commandResult;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public int getErrOp() {
        return errOp;
    }

    public void setErrOp(int errOp) {
        this.errOp = errOp;
    }

    public int getErrTag() {
        return errTag;
    }

    public void setErrTag(int errTag) {
        this.errTag = errTag;
    }
}
