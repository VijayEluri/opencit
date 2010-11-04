/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.opencit.core.projectmanager.model;

import java.io.Serializable;

import org.openengsb.core.common.descriptor.ServiceDescriptor;


public class Project implements Serializable {


    public enum State {
        OK,
        IN_PROGRESS,
        FAILURE;
    }

    private ServiceDescriptor scmDescriptor;
    private ServiceDescriptor buildDescriptor;
    private ServiceDescriptor notificationDescriptor;

    private State state;
    private String id;

    public Project() {

    }


    public Project(String id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public ServiceDescriptor getScmDescriptor() {
        return scmDescriptor;
    }

    public void setScmDescriptor(ServiceDescriptor scmDescriptor) {
        this.scmDescriptor = scmDescriptor;
    }

    public ServiceDescriptor getBuildDescriptor() {
        return buildDescriptor;
    }

    public void setBuildDescriptor(ServiceDescriptor buildDescriptor) {
        this.buildDescriptor = buildDescriptor;
    }

    public ServiceDescriptor getNotificationDescriptor() {
        return notificationDescriptor;
    }

    public void setNotificationDescriptor(ServiceDescriptor notificationDescriptor) {
        this.notificationDescriptor = notificationDescriptor;
    }
}
