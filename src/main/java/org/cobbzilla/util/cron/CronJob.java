package org.cobbzilla.util.cron;

import lombok.Getter;
import lombok.Setter;

import java.util.Properties;

public class CronJob {

    @Getter @Setter private String id;

    @Getter @Setter private String cronTimeString;

    @Getter @Setter private boolean startNow = false;

    @Getter @Setter private String commandClass;
    @Getter @Setter private Properties properties = new Properties();

    // todo
//    @Getter @Setter private String user;
//    @Getter @Setter private String shellCommand;

    public CronCommand getCommandInstance() throws Exception {
        CronCommand command = (CronCommand) Class.forName(commandClass).newInstance();
        command.init(properties);
        return command;
    }
}
