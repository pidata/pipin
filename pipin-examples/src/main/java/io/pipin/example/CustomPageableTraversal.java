package io.pipin.example;

import akka.actor.ActorSystem;
import io.pipin.core.poll.SimpleTraversal;
import io.pipin.core.poll.TokenAuthorizator;
import org.bson.Document;
import org.slf4j.Logger;
import scala.collection.Iterator;

/**
 * Created by libin on 2020/3/22.
 */
public class CustomPageableTraversal extends SimpleTraversal {
    public CustomPageableTraversal(String startUri, String pageParameter, int pageStartFrom, String method, ActorSystem actorSystem, Logger log) {
        super(startUri, pageParameter, pageStartFrom, method, actorSystem, log);
    }

    @Override
    public String getBody() {

        return "{\n" +
                "  \"_projectId\": \"---\"\n" +
                "}";
    }

    @Override
    public String[][] headers() {
        return new String[][]{{"Content-Type","application/json"},
                {"X-Tenant-Id","---"},
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
