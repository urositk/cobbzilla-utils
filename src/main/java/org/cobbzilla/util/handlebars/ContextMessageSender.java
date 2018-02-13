package org.cobbzilla.util.handlebars;

public interface ContextMessageSender {

    void send(String recipient, String message, String contentType);

}
