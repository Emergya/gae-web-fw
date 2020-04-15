package com.emergya.spring.gae.utils;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Class that includes a method that allows retrying with a backoff.
 *
 * @author lroman
 */
public final class BackoffTryer {

    private static final Logger LOG = Logger.getLogger(BackoffTryer.class.getName());

    private static final int WAIT_MSECS = 1000;
    private static final int MAX_RETRIES = 3;

    private BackoffTryer() {
    }

    /**
     * Tries an operation with backoff.
     *
     * @param <V> The type the callable returns
     * @param r The function to be called.
     * @return The result of the operation if succedes after backoff.
     */
    public static <V> V tryWithBackoff(Callable<V> r) {
        int attempts = 0;
        int delay = 1;

        V result;
        while (true) {
            try {
                try {
                    result = r.call();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } catch (RuntimeException e) {
                if (++attempts < MAX_RETRIES) {
                    try {
                        // retrying
                        Thread.sleep(delay * WAIT_MSECS);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    delay *= 2; // easy exponential backoff
                    LOG.info("Retrying operation in " + delay + ". Attempt " + attempts);
                    continue;
                } else {
                    throw e; // otherwise throw
                }
            }
            break;
        }

        return result;
    }
}
