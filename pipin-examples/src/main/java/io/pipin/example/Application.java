package io.pipin.example;

import io.pipin.core.domain.Project;
import io.pipin.core.poll.PollWorker;
import io.pipin.core.settings.ConvertSettings;
import io.pipin.core.settings.PollSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by libin on 2020/3/22.
 */
public class Application {

    public static void main(String[] args){
        Project project = new Project("", "", "poll");
        PollSettings pollSettings = new PollSettings("https://www.teambitionapis.com/tbs/core/v2/tasks:query", "page", 1
            ,"POST", "", "io.pipin.example.CustomPageableTraversal");

        project.convertSettings_$eq(new ConvertSettings(new TaskConverter()));

        project.pollSettings_$eq(pollSettings);
        List<String> keys = new ArrayList<String>();
        keys.add("_id");
        project.mergeSettings().keyMap().put("task", keys);
        new PollWorker(project).execute();
    }
}
