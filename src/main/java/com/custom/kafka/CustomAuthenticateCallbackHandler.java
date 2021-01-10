package com.custom.kafka;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.plain.PlainAuthenticateCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.util.List;
import java.util.Map;

public class CustomAuthenticateCallbackHandler implements AuthenticateCallbackHandler {
    private final Logger logger = LoggerFactory.getLogger(CustomAuthenticateCallbackHandler.class);

    private List<AppConfigurationEntry> jaasConfigEntries;

    public void configure(Map<String, ?> configs,
                          String mechanism,
                          List<AppConfigurationEntry> jaasConfigEntries) {
        this.jaasConfigEntries = jaasConfigEntries;
    }

    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        logger.info("Incoming auth request");

        String username = null;
        for (Callback callback : callbacks) {
            if (callback instanceof NameCallback) {
                username = ((NameCallback) callback).getDefaultName();

            } else if (callback instanceof PlainAuthenticateCallback) {
                PlainAuthenticateCallback plainCallback = (PlainAuthenticateCallback) callback;
                boolean authenticated = authenticate(username, plainCallback.password());
                if (authenticated) {
                    logger.info("Successfully authenticated using static credentials");
                } else {
                    logger.warn("Authentication was not successful");
                }
                plainCallback.authenticated(authenticated);
            } else
                throw new UnsupportedCallbackException(callback);
        }
    }

    protected boolean authenticate(String username, char[] password) {
        return username != null && username.equals("test") && new String(password).equals("testpw");
    }

    public void close() throws KafkaException {
    }
}
