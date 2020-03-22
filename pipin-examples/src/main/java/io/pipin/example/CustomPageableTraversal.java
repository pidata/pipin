package io.pipin.example;

import akka.actor.ActorSystem;
import io.pipin.core.poll.SimpleTraversal;
import io.pipin.core.poll.TokenAuthorizator;
import io.pipin.core.settings.PollSettings;
import org.bson.Document;
import org.slf4j.Logger;
import scala.collection.Iterator;

import java.util.Map;

/**
 * Created by libin on 2020/3/22.
 */
public class CustomPageableTraversal extends SimpleTraversal {
    public CustomPageableTraversal(String uri, String pageParameter, int pageStartFrom, PollSettings pollSettings, ActorSystem actorSystem, Logger log) {
        super(uri, pageParameter, pageStartFrom, pollSettings, actorSystem, log);
    }

    @Override
    public String getBody() {
        Map<String,String> extraSettings = settings().extraSettings();
        return "{\n" +
                "  \"_projectId\": \""+extraSettings.get("projectId")+"\"\n" +
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
                return "---";
            }
        };
    }

    @Override
    public String getContentField() {
        return "result";
    }


}
