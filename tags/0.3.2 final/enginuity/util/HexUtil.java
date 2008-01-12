package enginuity.util;

public final class HexUtil {

    private HexUtil() {
    }

    public static String asHex(byte in[]) {
        return bytesToHex(in).toUpperCase();
    }

    public static byte[] asBytes(String hex) {
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }
        return hexToBytes(hex);
    }

    public static String bytesToHex(byte[] bs, int off, int length) {
        StringBuffer sb = new StringBuffer(length * 2);
        bytesToHexAppend(bs, off, length, sb);
        return sb.toString();
    }

    public static void bytesToHexAppend(byte[] bs, int off, int length, StringBuffer sb) {
        sb.ensureCapacity(sb.length() + length * 2);
        for (int i = off; (i < (off + length)) && (i < bs.length); i++) {
            sb.append(Character.forDigit((bs[i] >>> 4) & 0xf, 16));
            sb.append(Character.forDigit(bs[i] & 0xf, 16));
        }
    }

    public static String bytesToHex(byte[] bs) {
        return bytesToHex(bs, 0, bs.length);
    }

    public static byte[] hexToBytes(String s) {
        return hexToBytes(s, 0);
    }

    public static byte[] hexToBytes(String s, int off) {
        byte[] bs = new byte[off + (1 + s.length()) / 2];
        hexToBytes(s, bs, off);
        return bs;
    }

    public static void hexToBytes(String s, byte[] out, int off) throws NumberFormatException, IndexOutOfBoundsException {
        int slen = s.length();
        if ((slen % 2) != 0) {
            s = '0' + s;
        }
        if (out.length < off + slen / 2) {
            throw new IndexOutOfBoundsException("Output buffer too small for input (" + out.length + "<" + off + slen / 2 + ")");
        }
        // Safe to assume the string is even length
        byte b1, b2;
        for (int i = 0; i < slen; i += 2) {
            b1 = (byte) Character.digit(s.charAt(i), 16);
            b2 = (byte) Character.digit(s.charAt(i + 1), 16);
            if ((b1 < 0) || (b2 < 0)) {
                throw new NumberFormatException();
            }
            out[off + i / 2] = (byte) (b1 << 4 | b2);
        }
    }
}
