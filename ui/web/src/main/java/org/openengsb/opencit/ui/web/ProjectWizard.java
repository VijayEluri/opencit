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

package org.openengsb.opencit.ui.web;


import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.WizardSteps.CreateProjectStep;

public class ProjectWizard extends Wizard {

    @SpringBean
    private ContextCurrentService contextSerice;

    private Project project;

    private String domainDropDown;

    private String serviceDescriptor;

    public ProjectWizard(String id) {
        super(id);
        project = new Project();
        setDefaultModel(new CompoundPropertyModel<ProjectWizard>(this));
        DynamicWizardModel model = new DynamicWizardModel(new CreateProjectStep(project));
        init(model);
    }

    @Override
    public void onCancel() {
        setResponsePage(Index.class);
    }

    @Override
    public void onFinish() {
        contextSerice.createContext(project.getId());
        setResponsePage(Index.class);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getServiceDescriptor() {
        return serviceDescriptor;
    }

    public void setServiceDescriptor(String serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }

    public String getDomainDropDown() {
        return domainDropDown;
    }

    public void setDomainDropDown(String domainDropDown) {
        this.domainDropDown = domainDropDown;
    }
}

