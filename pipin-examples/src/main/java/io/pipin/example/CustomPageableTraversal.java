package io.pipin.example;

import akka.actor.ActorSystem;
import io.pipin.core.poll.SimpleTraversal;
import io.pipin.core.poll.TokenAuthorizator;
import io.pipin.core.settings.PollSettings;
import org.bson.Document;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created by libin on 2020/3/22.
 */
public class CustomPageableTraversal extends SimpleTraversal {

    private String pageToken = "";

    public CustomPageableTraversal(String uri, String pageParameter, int pageStartFrom, PollSettings pollSettings, ActorSystem actorSystem, Logger log) {
        super(uri, pageParameter, pageStartFrom, pollSettings, actorSystem, log);
    }

    @Override
    public String getBody() {
        Map<String,String> extraSettings = settings().extraSettings();
        return "{\n" +
                "  \"_projectId\": \""+extraSettings.get("projectId")+"\",\n" +
                "  \"pageToken\": \""+pageToken+"\",\n" +
                "  \"pageSize\": 1000\n" +
                "}";
    }

    @Override
    public Map<String, String> extraParamsMap() {
        Map<String,String> extraSettings = settings().extraSettings();
        extraSettings.put("pageToken",pageToken);
        return extraSettings;
    }

    @Override
    public String[][] headers() {
        Map<String,String> extraSettings = settings().extraSettings();
        return new String[][]{{"Content-Type","application/json"},
                {"X-Tenant-Id",extraSettings.get("tenantId")},
                {"X-Tenant-Type","organization"}};
    }

    @Override
    public TokenAuthorizator getTokenAuthorizator() {
        return new TokenAuthorizator(){
            public String getToken() {
                return "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJfYXBwSWQiOiI1ZTFkMWQzZTNkYzA1ZDAwMDFmYmVlNDUiLCJleHAiOjE1ODQ5Nzk1NzAsImlhdCI6MTU4NDk0MzU3MH0.s-POSjP1Xdmsqm7P-TdQLREa6_4E2choUjikFmYjxRA";
            }
        };
    }

    @Override
    public String getContentField() {
        return "result";
    }

    @Override
    public boolean endPage(Document doc) {
        String nextPageToken = doc.getString("nextPageToken");
        return null==nextPageToken || "".equals(nextPageToken);
    }

    @Override
    public void onPageNext(Document doc) {
        pageToken = doc.getString("nextPageToken");
        super.onPageNext(doc);
    }
}
