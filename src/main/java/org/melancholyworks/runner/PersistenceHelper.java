package org.melancholyworks.runner;

import java.util.List;

/**
 * @author ZarkTao
 */
public interface PersistenceHelper {
    List<String> getInstanceIDList();

    String getInstance(String instanceID);

    void save(String serializedInstance);
}
