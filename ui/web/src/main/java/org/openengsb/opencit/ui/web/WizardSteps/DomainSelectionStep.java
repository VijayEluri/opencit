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

package org.openengsb.opencit.ui.web.WizardSteps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardStep;
import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.openengsb.core.common.Domain;
import org.openengsb.domain.build.BuildDomain;
import org.openengsb.domain.deploy.DeployDomain;
import org.openengsb.domain.notification.NotificationDomain;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.domain.test.TestDomain;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class DomainSelectionStep extends DynamicWizardStep {

    private String domainDropDown = "";

    private Project project;
    private Map<String, Class<? extends Domain>> managersMap = new HashMap<String, Class<? extends Domain>>();

    public DomainSelectionStep(Project project) {
        super(new CreateProjectStep(project), new ResourceModel("selectDomain.title"),
            new ResourceModel("selectDomain.summary"), new Model<Project>(project));
        this.project = project;

        managersMap.put("SCM Domain", ScmDomain.class);
        managersMap.put("Notification Domain", NotificationDomain.class);
        managersMap.put("Build Domain", BuildDomain.class);
        managersMap.put("Test Domain", TestDomain.class);
        managersMap.put("Deploy Domain", DeployDomain.class);
        managersMap.put("Report Domain", ReportDomain.class);

        Map<Class<? extends Domain>, String> services = project.getServices();
        Set<String> toRemove = new HashSet<String>();
        for (Entry<String, Class<? extends Domain>> entry : managersMap.entrySet()) {
            if (services.containsKey(entry.getValue())) {
                toRemove.add(entry.getKey());
            }
        }
        for (String s : toRemove) {
            managersMap.remove(s);
        }
        domainDropDown = managersMap.keySet().iterator().next();

        DropDownChoice<String> descriptorDropDownChoice = initSCMDomains();
        add(descriptorDropDownChoice);
    }

    private DropDownChoice<String> initSCMDomains() {

        IModel<List<String>> dropDownModel = new LoadableDetachableModel<List<String>>() {
            @Override
            protected List<String> load() {
                Set<String> domains = new HashSet<String>(managersMap.keySet());
                return new ArrayList<String>(domains);
            }
        };

        DropDownChoice<String> descriptorDropDownChoice =
            new DropDownChoice<String>("domainDropDown", new IModel<String>() {
                public String getObject() {
                    return domainDropDown;
                }

                public void setObject(String object) {
                    domainDropDown = object;
                }

                public void detach() {
                }
            }, dropDownModel);
        return descriptorDropDownChoice;
    }

    @Override
    public boolean isLastStep() {
        return false;
    }

    @Override
    public IDynamicWizardStep next() {
        Class<? extends Domain> domain = managersMap.get(domainDropDown);
        return new SelectServiceStep(project, domain);
    }

    @Override
    public boolean isComplete() {
        return domainDropDown != null && !"".equals(domainDropDown);
    }
}
