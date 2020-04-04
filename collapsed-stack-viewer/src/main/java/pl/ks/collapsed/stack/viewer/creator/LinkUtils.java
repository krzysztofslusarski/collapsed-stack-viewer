package pl.ks.collapsed.stack.viewer.creator;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import lombok.experimental.UtilityClass;

@UtilityClass
class LinkUtils {
    String getFlameGraphHref(String collapsed) {
        return "/flame-graph?collapsed=" + collapsed;
    }

    String getNoThreadFlameGraphHref(String collapsed) {
        return "/flame-graph-no-thread?collapsed=" + collapsed;
    }

    String getHotspotFlameGraphHref(String collapsed) {
        return "/flame-graph-hotspot?collapsed=" + collapsed;
    }

    String getHotspotLLimitedFlameGraphHref(String collapsed, int limit) {
        return "/flame-graph-hotspot-limited?limit=" +limit + "&collapsed=" + collapsed;
    }

    String getFromMethodHref(String collapsed, String methodName) {
        return "/from-method?collapsed=" + collapsed + "&method=" + URLEncoder.encode(methodName, Charset.defaultCharset());
    }

    String getToMethodHref(String collapsed, String methodName) {
        return "/to-method?collapsed=" + collapsed + "&method=" + URLEncoder.encode(methodName, Charset.defaultCharset());
    }
}
