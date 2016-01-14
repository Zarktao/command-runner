package org.melancholyworks.runner;

import org.codehaus.jackson.annotate.JsonCreator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZarkTao
 */
public enum Status {
    WAITING("Waiting"),
    RUNNING("Running"),
    SUSPENDED("Suspended"),
    SUCCESS("Success"),
    FAILED("Failed"),
    KILLED("Killed");

    private static final Map<String, Status> valuesMap = new HashMap<>();

    static {
        for (Status t : values()) {
            valuesMap.put(t.toString(), t);
        }
    }

    private String value;

    Status(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator
    public static Status fromString(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return valuesMap.get(value);
    }

    public boolean isEnd() {
        return this.equals(SUCCESS) || this.equals(FAILED) || this.equals(KILLED);
    }
}
