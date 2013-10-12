package org.cobbzilla.util.cron;

import java.util.Map;
import java.util.Properties;

public interface CronCommand {

    public void init (Properties properties);

    public void exec (Map<String, Object> context) throws Exception;

}
