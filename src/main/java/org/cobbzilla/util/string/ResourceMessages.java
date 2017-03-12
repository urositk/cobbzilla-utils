package org.cobbzilla.util.string;

import lombok.Getter;

import java.util.ResourceBundle;

public abstract class ResourceMessages  {

    protected abstract String getBundleName();

    @Getter(lazy=true) private final ResourceBundle bundle = ResourceBundle.getBundle(getBundleName());

    public String translate(String messageTemplate) {

        // strip leading/trailing curlies if they are there
        while (messageTemplate.startsWith("{")) messageTemplate = messageTemplate.substring(1);
        while (messageTemplate.endsWith("}")) messageTemplate = messageTemplate.substring(0, messageTemplate.length()-1);

        return getBundle().getString(messageTemplate);
    }

}
