package io.pipin.example;

import io.pipin.core.ext.Converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by libin on 2020/3/22.
 */
public class TaskConverter implements Converter {
    public Map<String, Map<String, Object>> convert(Map<String, Object> doc) {
        Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>(2);
        result.put("task", doc);
        return result;
    }
}
