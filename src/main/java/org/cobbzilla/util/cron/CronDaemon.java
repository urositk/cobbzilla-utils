package org.cobbzilla.util.cron;

public interface CronDaemon {

    public void start () throws Exception;

    public void stop() throws Exception;

    public void addJob(final CronJob job) throws Exception;

    public void removeJob(final String id) throws Exception;
}