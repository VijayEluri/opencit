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
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardStep;
import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.ServiceManagerModel;
import org.openengsb.ui.web.editor.ServiceEditorPanel;
import org.openengsb.ui.web.model.WicketStringLocalizer;

public class SetAttributesStep extends DynamicWizardStep {
    private Project project;
    private ServiceEditorPanel editorPanel;
    private FeedbackPanel feedbackPanel;
    private ServiceManagerModel serviceManager;
    private String serviceId;

    public SetAttributesStep(final Project project, final ServiceManagerModel serviceManager) {
        super(new CreateProjectStep(project), new ResourceModel("setAttribute.title"),
            new ResourceModel("setAttribute.summary"), new Model<Project>(project));
        this.project = project;
        this.serviceManager = serviceManager;
        feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
        setComplete(false);

        IModel<List<AttributeDefinition>> attributes = new LoadableDetachableModel<List<AttributeDefinition>>() {
            @Override
            protected List<AttributeDefinition> load() {
                return buildAttributeList(serviceManager.getObject());
            }
        };
        Map<String, String> values = new HashMap<String, String>();

        editorPanel = new ServiceEditorPanel("editor", attributes.getObject(), values,
            serviceManager.getObject().getDescriptor().getFormValidator()) {
            @Override
            public void onSubmit() {
                CheckBox component = (CheckBox) editorPanel.get("form:validate");
                boolean checkBoxValue = component.getModelObject();
                if (checkBoxValue) {
                    MultipleAttributeValidationResult updateWithValidation = serviceManager.getObject()
                        .update(getValues().get("id"), getValues());
                    if (!updateWithValidation.isValid()) {
                        Map<String, String> attributeErrorMessages = updateWithValidation.getAttributeErrorMessages();
                        for (String value : attributeErrorMessages.values()) {
                            error(new StringResourceModel(value, this, null).getString());
                        }
                    } else {
                        serviceId = getValues().get("id");
                        info("connector.succeeded");
                        setComplete(true);
                    }
                } else {
                    serviceId = getValues().get("id");
                    serviceManager.getObject().update(serviceId, getValues());
                    setComplete(true);
                }
            }
        };
        add(editorPanel);
    }

    private List<AttributeDefinition> buildAttributeList(ServiceManager service) {
        AttributeDefinition.Builder builder = AttributeDefinition.builder(new WicketStringLocalizer(this));
        AttributeDefinition id = builder.id("id").name("attribute.id.name").description("attribute.id.description")
            .required().build();
        ServiceDescriptor descriptor = service.getDescriptor();
        List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
        attributes.add(id);
        attributes.addAll(descriptor.getAttributes());
        return attributes;
    }

    @Override
    public boolean isLastStep() {
        return false;
    }

    @Override
    public IDynamicWizardStep next() {
        project.addService(serviceManager.getObject().getDescriptor().getServiceType(), serviceId);
        if (project.getServices().size() == 6) {
            return new FinalStep(this, project);
        }
        return new DomainSelectionStep(project);
    }

}
