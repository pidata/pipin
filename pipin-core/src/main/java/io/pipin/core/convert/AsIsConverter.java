package io.pipin.core.convert;

import io.pipin.core.ext.Converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by libin on 2020/3/30.
 */
public class AsIsConverter implements Converter {

    private String entity;
    private String[] fields;

    public AsIsConverter(String entity, String[] fields) {
        this.entity = entity;
        this.fields = fields;
    }


    public Map<String, Map<String, Object>> convert(Map<String, Object> doc) {
        Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
        result.put(entity, doc);
        return result;
    }
}
