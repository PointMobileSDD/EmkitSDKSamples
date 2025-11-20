package device.sdk.sample.rfid.adapter.item;

public class RfidListItem
{
    private String nName;
    private int mCount;

    public RfidListItem(String nName, int mCount) {
        this.nName = nName;
        this.mCount = mCount;
    }

    public String getName() {
        return nName;
    }

    public void setName(String nName) {
        this.nName = nName;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int mCount) {
        this.mCount = mCount;
    }
}
