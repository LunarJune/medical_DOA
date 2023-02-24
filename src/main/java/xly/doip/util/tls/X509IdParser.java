package xly.doip.util.tls;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

/**
 * Utility methods to parse a (handle) identity from an X509 certificate.
 * The identity will be taken from the subject's distinguished name; it will
 * use the first available UID attribute if present,
 * otherwise the first available CN attribute if present,
 * otherwise the first available O attribute if present.
 */
public class X509IdParser {

    /**
     * Parses the identity handle from the distinguished name.
     *
     * @param dn the distinguished name
     * @return the identity handle
     */
    private static String parseIdentityHandleFromRfc2253Dn(String dn) {
        return new Rfc2253DnParser(dn).getHandle();
    }

    /**
     * Parses the identity handle from the certificate.
     *
     * @param cert the certificate
     * @return the identity handle
     */
    public static String parseIdentityHandle(X509Certificate cert) {
        if (cert == null) return null;
        return parseIdentityHandleFromRfc2253Dn(cert.getSubjectX500Principal().getName());
    }

    /**
     * Parses the identity handle from the first certificate in a chain.
     *
     * @param cert the certificate chain
     * @return the identity handle
     */
    public static String parseIdentityHandle(X509Certificate[] cert) {
        if (cert == null || cert.length == 0) return null;
        return parseIdentityHandle(cert[0]);
    }

    private static class Rfc2253DnParser {
        private final String dn;
        private int index;

        Rfc2253DnParser(String dn) {
            this.dn = dn;
        }

        private static String trim(String s) {
            int start = 0, end = 0;
            for (start = 0; start < s.length(); start++) {
                char ch = s.charAt(start);
                if (ch == ' ') continue;
                else break;
            }
            boolean escaped = false;
            for (int i = start; i < s.length(); i++) {
                char ch = s.charAt(i);
                boolean isSpace = false;
                if (escaped) escaped = false;
                else if (ch == '\\') escaped = true;
                else if (ch == ' ') isSpace = true;
                if (!isSpace) end = i;
            }
            if (start > end) return "";
            return s.substring(start, end + 1);
        }

        private String getType() {
            int start = index;
            index = dn.indexOf('=', start);
            if (index < 0) return null;
            else {
                String type = dn.substring(start, index);
                index++;
                return trim(type);
            }
        }

        private char findSeparator() {
            boolean quoted = false;
            boolean escaped = false;
            for (; index < dn.length(); index++) {
                char ch = dn.charAt(index);
                if (!quoted && !escaped && (ch == '+' || ch == ',' || ch == ';')) return ch;
                else if (escaped) escaped = false;
                else if (ch == '\\') escaped = true;
                else if (ch == '"') quoted = !quoted;
            }
            return ',';
        }

        private static boolean isHexChar(byte ch) {
            return ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F'));
        }

        private static int nibbleDecode(byte b) {
            if (b >= '0' && b <= '9') return b - '0';
            if (b >= 'a' && b <= 'f') return 10 + b - 'a';
            if (b >= 'A' && b <= 'F') return 10 + b - 'A';
            return b;
        }

        private static byte hexDecode(byte b1, byte b2) {
            return (byte) ((nibbleDecode(b1) << 4 | nibbleDecode(b2)) & 0xFF);
        }

        private static String unescape(String value) {
            boolean quoted = false;
            boolean escaped = false;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                if (escaped) {
                    escaped = false;
                    if (isHexChar(b)) {
                        i++;
                        if (i >= bytes.length) break;
                        byte b2 = bytes[i];
                        bout.write(hexDecode(b, b2));
                    } else {
                        bout.write(b);
                    }
                } else if (b == '\\') escaped = true;
                else if (b == '"') quoted = !quoted;
                else bout.write(b);
            }
            return new String(bout.toByteArray(), StandardCharsets.UTF_8);
        }

        private String getValue() {
            int start = index;
            findSeparator();
            String value = dn.substring(start, index);
            index++;
            value = trim(value);
            if (value.startsWith("#")) return null;
            return unescape(value);
        }

        String getHandleOrValueReference() {
            String uid = null, cn = null, o = null;
            while (index >= 0 && index < dn.length()) {
                String type = getType();
                if (type == null) break;
                String value = getValue();
                if (value == null) continue;
                if ("UID".equalsIgnoreCase(type) && uid == null) uid = value;
                if ("CN".equalsIgnoreCase(type) && cn == null) cn = value;
                if ("O".equalsIgnoreCase(type) && o == null) o = value;
            }
            if (uid != null) return uid;
            if (cn != null) return cn;
            return o;
        }

        private static boolean isDigits(String s) {
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch < '0' || ch > '9') return false;
            }
            return true;
        }

        String getHandle() {
            String handleOr = getHandleOrValueReference();
            if (handleOr == null) return null;
            int colon = handleOr.indexOf(':');
            if (colon < 0) return handleOr;
            String maybeIndex = handleOr.substring(0, colon);
            if (isDigits(maybeIndex)) {
                String handle = handleOr.substring(colon + 1);
                return handle;
            }
            return handleOr;
        }
    }
}
