package io.pipin.core.ext;

import java.util.Map;

/**
 * Created by libin on 2020/4/5.
 */
public interface Converter {
    Map<String,Map<String, Object>> convert(Map<String, Object> doc);
}
