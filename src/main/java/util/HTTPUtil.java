package util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class HTTPUtil {

    public static final String URLEncode(final String element) {
        try {
            return URLEncoder.encode(element, Const.ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "-";
    }
}
