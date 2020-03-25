package io.pipin.example;

import io.pipin.web.server.ManagementServer;
import io.pipin.web.server.PipinServer;
import io.redlion.pipin.scheduler.quarz.Scheduler;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeoutException;

/**
 * Created by libin on 2020/3/25.
 */
public class ApplicationServer {
    public static void main(String[] args){
        new ManagementServer().start();
        new PipinServer().start();
        Scheduler scheduler = new Scheduler();
        try {
            Await.ready(scheduler.scheduleAll(), Duration.apply("1 minutes"));
            scheduler.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }
}
