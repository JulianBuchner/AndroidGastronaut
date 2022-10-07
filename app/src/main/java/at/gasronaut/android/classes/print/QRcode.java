package at.gasronaut.android.classes.print;

import java.io.UnsupportedEncodingException;

public class QRcode {
    public QRcode() {
    }

    public static byte[] POS_PrtQRcode(String strCodedata, int nVersion, int nErrorLevel, int nMagnification) {
        if (nVersion < 0 | nVersion > 19 | nErrorLevel < 0 | nErrorLevel > 3 | nMagnification < 1 | nMagnification > 8) {
            return null;
        } else {
            Object var4 = null;

            byte[] bCodeData;
            try {
                bCodeData = strCodedata.getBytes("GBK");
            } catch (UnsupportedEncodingException var6) {
                return null;
            }

            Command.GS_k_m_v_r_nL_nH[2] = (byte) nVersion;
            Command.GS_k_m_v_r_nL_nH[3] = (byte) nErrorLevel;
            Command.GS_k_m_v_r_nL_nH[4] = (byte) nMagnification;
            Command.GS_k_m_v_r_nL_nH[5] = (byte) (bCodeData.length & 255);
            Command.GS_k_m_v_r_nL_nH[6] = (byte) ((bCodeData.length & '\uff00') >> 8);
            byte[] data = Other.byteArraysToBytes(new byte[][]{Command.GS_k_m_v_r_nL_nH, bCodeData});
            return data;
        }
    }

    public static byte[] POS_PrtBarcode(String strCodedata, int nOrgx, int nType, int nWidthX, int nHeight, int nHriFontType, int nHriFontPosition) {
        if (nOrgx < 0 | nOrgx > 255 | nType < 65 | nType > 73 | nWidthX < 2 | nWidthX > 6 | nHeight < 1 | nHeight > 255) {
            return null;
        } else {
            Object var7 = null;

            byte[] bCodeData;
            try {
                bCodeData = strCodedata.getBytes("GBK");
            } catch (UnsupportedEncodingException var9) {
                return null;
            }

            Command.GS_x[2] = (byte) nOrgx;
            Command.GS_w[2] = (byte) nWidthX;
            Command.GS_h[2] = (byte) nHeight;
            Command.GS_f[2] = (byte) (nHriFontType & 1);
            Command.GS_H[2] = (byte) (nHriFontPosition & 3);
            Command.GS_k[2] = (byte) nType;
            Command.GS_k[3] = (byte) bCodeData.length;
            byte[] data = Other.byteArraysToBytes(new byte[][]{Command.GS_x, Command.GS_w, Command.GS_h, Command.GS_f, Command.GS_H, Command.GS_k, bCodeData});
            return data;
        }
    }
}
