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
                "  \"_pageToken\": \""+pageToken+"\",\n" +
                "  \"pageSize\": 1000\n" +
                "}";
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
                return "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJfYXBwSWQiOiI1ZTFkMWQzZTNkYzA1ZDAwMDFmYmVlNDUiLCJleHAiOjE1ODQ4OTgwNTgsImlhdCI6MTU4NDg2MjA1OH0.jVP5pW-T0QUwvq-boQc09VbF_H0C5dnJ43K6NLy7J9g";
            }
        };
    }

    @Override
    public String getContentField() {
        return "result";
    }

    @Override
    public boolean endPage(Document doc) {
        return "".equals(doc.getString("nextPageToken"));
    }

    @Override
    public void onPageNext(Document doc) {
        pageToken = doc.getString("nextPageToken");
        super.onPageNext(doc);
    }
}
