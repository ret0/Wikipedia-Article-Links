package util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Utils to simplify HTTP/Wikipedia API operations
 */
public final class HTTPUtil {

    private HTTPUtil() { }

    public static String urlEncode(final String element) {
        try {
            return URLEncoder.encode(element, Const.ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "-";
    }
}
