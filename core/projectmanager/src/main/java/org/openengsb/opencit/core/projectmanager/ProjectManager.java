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

package org.openengsb.opencit.core.projectmanager;

import java.util.List;
import java.util.UUID;

import javax.jms.JMSException;

import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.domain.dependency.DependencyDomain;
import org.openengsb.domain.notification.Notification;
import org.openengsb.opencit.core.projectmanager.model.BuildFeedback;
import org.openengsb.opencit.core.projectmanager.model.BuildReason;
import org.openengsb.opencit.core.projectmanager.model.DependencyProperties;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.Project.State;

public interface ProjectManager {

    void createProject(Project project) throws ProjectAlreadyExistsException, ConnectorValidationFailedException;

    List<Project> getAllProjects();

    Project getProject(String projectId) throws NoSuchProjectException;

    void updateProject(Project project) throws NoSuchProjectException;

    void updateCurrentContextProjectState(State state) throws NoSuchProjectException;

    Project getCurrentContextProject() throws NoSuchProjectException;

    void deleteProject(String projectId) throws NoSuchProjectException;

    boolean isRemotingAvailable();
    UUID storeBuild(Project project, BuildReason reason);
    void sendUpdateNotification(Project project, UUID storedBuild, String location) throws JMSException;
    void sendFeedback(String channel, BuildFeedback feedback);

    void addProjectDependency(Project project, DependencyProperties dependency) throws ConnectorValidationFailedException;
    
    // FIXME: Remove this and load the EkbService properly into the workflow
    Notification createNotification();
    // FIXME: Isn't there a nicer way to get a connector instance???
    DependencyDomain getDependencyConnector(Project project, String depname);
}
