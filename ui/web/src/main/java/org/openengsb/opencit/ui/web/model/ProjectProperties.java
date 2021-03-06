/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.opencit.ui.web.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class ProjectProperties implements Serializable {
    private String id;
    private String notificationRecipient;

    private Map<String, Map<String, String>> cfgs =
        new HashMap<String, Map<String, String>>();
    private Map<String, String> connectors =
        new HashMap<String, String>();

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDomainConnector(String domain, String con) {
        connectors.put(domain, con);
    }

    public String getDomainConnector(String domain) {
        return connectors.get(domain);
    }

    public void setDomainConfig(String domain, Map<String, String> cfg) {
        cfgs.put(domain, cfg);
    }

    public Map<String, String> getDomainConfig(String domain) {
        Map<String, String> ret = cfgs.get(domain);
        if (ret == null) {
            ret = new HashMap<String, String>();
            cfgs.put(domain, ret);
        }
        return ret;
    }

    public void setNotificationRecipient(String notificationRecipient) {
        this.notificationRecipient = notificationRecipient;
    }

    public String getNotificationRecipient() {
        return notificationRecipient;
    }
}
