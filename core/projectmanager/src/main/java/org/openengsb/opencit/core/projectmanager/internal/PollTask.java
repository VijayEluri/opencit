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

package org.openengsb.opencit.core.projectmanager.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.context.ContextHolder;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.core.security.BundleAuthenticationToken;
import org.openengsb.domain.scm.ScmDomain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class PollTask implements Runnable {

    private Log log = LogFactory.getLog(PollTask.class);

    private WorkflowService workflowService;
    private AuthenticationManager authenticationManager;
    private ScmDomain scm;
    private String projectId;

    public PollTask(WorkflowService workflowService, AuthenticationManager authenticationManager,
            ScmDomain scm, String projectId) {
        this.workflowService = workflowService;
        this.authenticationManager = authenticationManager;
        this.scm = scm;
        this.projectId = projectId;
    }

    public PollTask() {
    }

    @Override
    public void run() {
        try {
            authenticate();
            doRun();
        } catch (Exception e) { // just swallow it here for now
            log.error("error when polling scm ", e);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
        log.info("poller done done");
    }

    private void authenticate() {
        Authentication token =
            authenticationManager.authenticate(new BundleAuthenticationToken(
                "opencit-core-projectmanager", ""));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private void doRun() throws InterruptedException, WorkflowException {
        log.info("running pollertask");
        ContextHolder.get().setCurrentContextId(projectId);
        log.debug("ContextHolder now has " + ContextHolder.get().getCurrentContextId());
        if (scm.poll()) {
            log.info("running flow");
            runFlow();
        }
    }

    private void runFlow() throws InterruptedException, WorkflowException {
        log.info("starting workflow \"CI\"");
        long pid = workflowService.startFlow("ci");
        workflowService.waitForFlowToFinish(pid);
    }

}
