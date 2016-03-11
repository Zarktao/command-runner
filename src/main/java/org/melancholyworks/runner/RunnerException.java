package org.melancholyworks.runner;

/**
 * @author Yang Tao sx-9027
 * @version 1.0.0
 * @date 2016-01-13 16:11
 * @company DTDream All rights reserved
 */
public class RunnerException extends Exception {
    public RunnerException(String s) {
        super(s);
    }

    public RunnerException(String s, Exception e) {
        super(s, e);
    }
}
