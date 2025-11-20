package device.sdk.sample.msr;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.util.Log;
import device.common.MsrIndex;
import device.common.MsrResult;
import device.common.MsrResultCallback;
import device.sdk.MsrManager;

public class MsrSampleManager {
	private static final String TAG = "MSR";
	
	Context mContext;
	/* read state */
	public static final int READ_SUCCESS = 0;
	public static final int READ_FAIL = 1;
	public static final int READ_READY = 2;

	private static MsrManager mMsr = null;
	private MsrResult mDetectResult = null;

	private int mSuccessCount = 0;
	private int mPartCount = 0;
	private int mFailCount = 0;
	private int mTrack1Count = 0;
	private int mTrack2Count = 0;
	private int mTrack3Count = 0;
	private int mTotalCount = 0;

	private String mTrack1;
	private String mTrack2;
	private String mTrack3;

	private String mResult = null;

	MsrResultCallback mCallbcak = new MsrResultCallback() {
		@Override
		public void onResult(int cmd, int status) {
			mTotalCount++;

			if (mListener != null) {
				String str;
				int track1result = (status >> 8) & 0x1;
				int track2result = (status >> 8) & 0x2;
				int track3result = (status >> 8) & 0x4;
				int readstatus = status & 0xff;
				if (readstatus == 0) {
					str = "read[" + errormsg(status, readstatus) + "]"
						+ " , track1=" + ((track1result==0)? "[success]":"[fail]")
						+ " , track2=" + ((track2result==0)? "[success]":"[fail]")
						+ " , track3=" + ((track3result==0)? "[success]":"[fail]");

					if (track1result == 0 && track2result == 0 && track3result == 0)
						mSuccessCount++;
					else
						mPartCount++;
					if (track1result == 0)		mTrack1Count++;
					if (track2result == 0)		mTrack2Count++;
					if (track3result == 0)		mTrack3Count++;
				} else {
					str = "read[" + errormsg(status, readstatus) + "]";
					mFailCount++;
				}

				mResult = "[All read/Part read/Fail/Total] = [" + Integer.toString(mSuccessCount) + "/" + Integer.toString(mPartCount) + "/" + Integer.toString(mFailCount) + "/" + Integer.toString(mTotalCount) + "]\n"
					+  "Track1Count = " + Integer.toString(mTrack1Count)
					+ "\nTrack2Count = " + Integer.toString(mTrack2Count)
					+ "\nTrack3Count = " + Integer.toString(mTrack3Count);

				mListener.printCardInformation(str, status);
			}
		}
	};

	public MsrSampleManager(Context context) {
		mContext = context;
		mDetectResult = new MsrResult();
 
		mMsr = new MsrManager();
		if (mMsr != null) {
			mMsr.DeviceMsrOpen(mCallbcak);
		}
		mTrack1 = new String();
		mTrack2 = new String();
		mTrack3 = new String();
	}

	private static EventListener mListener;

	public static interface EventListener {
		int printCardInformation(String result, int state);
		int setResult(String track1, String track2, String track3, String result);
		int setDelayedRepeat(int delay);
	}

	public static void setListner(EventListener l) {
		mListener = l;
	}

	public void ReadStart() {
		if (mMsr != null) {
			mMsr.DeviceMsrStartRead();
		}
	}

	public void ReadStop() {
		if (mMsr != null) {
			mMsr.DeviceMsrStopRead();
		}
	}
	
	public void PowerDown() throws InterruptedException {
		if (mMsr != null) {
			Log.d(TAG, "Msr Power off (DESTROY)");
			mMsr.DeviceMsrClose();
			Thread.sleep(1000);
		}
	}
	
    public void GetResult() {
        if (mMsr != null) {
            byte[] encryptionData = getEncryptionData();
            if (encryptionData != null) {
                String s = "";
                for (byte b : encryptionData) {
                    s += String.format("%02X", b);
                }
                // Encrypted data.
                mTrack1 = s;

                byte[] decryptionData = getDecryptionData();
                s = "";
                if (decryptionData != null) {
                    for (byte b : decryptionData) {
                        s += String.format("%02X", b);
                    }
                }
                // Decrypted data.
                mTrack2 = s;
            } else {
                mDetectResult = mMsr.DeviceMsrGetData(0x07);
                mTrack1 = mDetectResult.getMsrTrack1();
                mTrack2 = mDetectResult.getMsrTrack2();
                mTrack3 = mDetectResult.getMsrTrack3();
            }
            if (mListener != null) {
                mListener.setResult(mTrack1, mTrack2, mTrack3, mResult);
            }
        }
    }

	public void SetRepeat() {
		if (mListener != null) {
			mListener.setDelayedRepeat(1000);
		}
	}

	String errormsg(int track, int status)
	{
		String msg = new String();
		if (status == MsrIndex.MMD1000_READ_OK) {
			msg = "Success";
			if ((track&0x600) == 0x600) {
				msg += " part(Track2,3 fail)";
			} else if ((track&0x500) == 0x500) {
				msg += " part(Track1,3 fail)";
			} else if ((track&0x300) == 0x300) {
				msg += " part(Track1,2 fail)";
			} else if ((track&0x100) == 0x100) {
				msg += " part(track1 fail)";
			} else if ((track&0x200) == 0x200) {
				msg += " part(track2 fail)";
			} else if ((track&0x400) == 0x400) {
				msg += " part(track3 fail)";
			}
			else {
				msg += " all";
			}
		}
		else {
			switch(status) {
			case MsrIndex.MMD1000_READ_ERROR:				msg = "Read failed";		break;
			case MsrIndex.MMD1000_CRC_ERROR:				msg = "CRC error in encryption related information stored in OTP";		break;
			case MsrIndex.MMD1000_NOINFOSTORE:				msg = "No information stored in OTP related to encryption";		break;
			case MsrIndex.MMD1000_AES_INIT_NOT_SET:			msg = "AES initial vector is not set yet";		break;
			case MsrIndex.MMD1000_READ_PREAMBLE_ERROR:		msg = "Preamble error in card read data";		break;
			case MsrIndex.MMD1000_READ_POSTAMBLE_ERROR:		msg = "Postamble error in card read data";		break;
			case MsrIndex.MMD1000_READ_LRC_ERROR:			msg = "LRC error in card read data";		break;
			case MsrIndex.MMD1000_READ_PARITY_ERROR:		msg = "Parity error in card read data";		break;
			case MsrIndex.MMD1000_BLANK_TRACK:				msg = "Black track";		break;
			case MsrIndex.MMD1000_CMD_STXETX_ERROR:			msg = "STX/ETX error in command communication";		break;
			case MsrIndex.MMD1000_CMD_UNRECOGNIZABLE:		msg = "Class/Function un-recognizable in command";		break;
			case MsrIndex.MMD1000_CMD_BCC_ERROR:			msg = "BCC error in command communication";		break;
			case MsrIndex.MMD1000_CMD_LENGTH_ERROR:			msg = "Length error in command communication";		break;
			case MsrIndex.MMD1000_READ_NO_DATA:				msg = "No data available to re-read";		break;
			case MsrIndex.MMD1000_DEVICE_READ_TIMEOUT:		msg = "Read command timeout";		break;
			case MsrIndex.MMD1000_DEVICE_POWER_DISABLE:		msg = "MMD1000 power is disable";		break;
			case MsrIndex.MMD1000_DEVICE_NOT_OPENED:		msg = "MMD1000 function is not opened";		break;
			case MsrIndex.MMD1000_DEVICE_DATA_CLEARED:		msg = "MMD1000 device result is cleared";		break;
			default:		msg = "error"; 		break;
			}
		}

		return msg;
	}

	public void clearResult() {
		mSuccessCount = 0;
		mPartCount = 0;
		mFailCount = 0;
		mTrack1Count = 0;
		mTrack2Count = 0;
		mTrack3Count = 0;
		mTotalCount = 0;
	}

    public boolean setUsedEncryption() {
        byte[] keySerialNumber = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x00, 0x00, 0x00};
        byte[] initialKey = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10};
        return mMsr.setUsedEncryption(keySerialNumber, initialKey);
    }

    private byte[] getDukptKey() {
        byte[] data = getEncryptionData();
        byte[] initialKey = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10};
        byte[] ksn = new byte[10];
        for (int i = 0; i < 10; i++) {
            ksn[i] = data[5 + i];
        }
        long tc = ((ksn[7] & 0x1f) << 16) | (ksn[8] & 0xff) <<8 | ksn[9] & 0xff;
        return Dukpt.getKey(initialKey, ksn, tc);
    }

    private byte[] getDataFromEncryptionData() {
        byte[] data = getEncryptionData();
        int padLength = data[2] & 0xFF;
        int dataLength = (((data[3] & 0xFF) << 8) | (data[4] & 0xFF)) + padLength - 10;
        byte[] encryptedData = new byte[dataLength];
        System.arraycopy(data, 15, encryptedData, 0, dataLength);
        return encryptedData;
    }

    public byte[] getEncryptionData() {
        return mMsr.getEncryptionData();
    }

    public byte[] getDecryptionData() {
        byte[] decoded = null;
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            Key keySpec = new SecretKeySpec(getDukptKey(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            decoded = cipher.doFinal(getDataFromEncryptionData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decoded;
    }
}
