package io.pipin.example;

import io.pipin.web.server.ManagementServer;

/**
 * Created by libin on 2020/3/25.
 */
public class ApplicationServer {
    public static void main(String[] args){
        new ManagementServer().start();
    }
}
