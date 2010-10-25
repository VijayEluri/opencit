package org.openengsb.opencit.ui.web;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.domains.scm.ScmDomain;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.ui.web.model.WicketStringLocalizer;

public class ProjectWizard extends Wizard {

    @SpringBean
    private ContextCurrentService contextSerice;

    @SpringBean
    private DomainService domainService;

    private Project project;

    private ServiceDescriptor scmDescriptor;

    private Log log = LogFactory.getLog(this.getClass());

    private final class CreateProjectStep extends WizardStep {

        public CreateProjectStep() {
            super(new ResourceModel("newProject.title"), new ResourceModel("newProject.summary"),
                new Model<Project>(project));
            add(new RequiredTextField<String>("project.id"));
        }

        @Override
        public void applyState() {
            super.applyState();
            getDefaultModelObject();
        }
    }

    private final class SetSCMStep extends WizardStep {

        public SetSCMStep() {
            super(new ResourceModel("setUpSCM.title"), new ResourceModel("setUpSCM.summary"),
                new Model<Project>(project));
            List<ServiceManager> manager = domainService.serviceManagersForDomain(ScmDomain.class);
            DropDownChoice<ServiceDescriptor> descriptorDropDownChoice = initSCMDomains(manager, "scmDescriptor");
            add(descriptorDropDownChoice);
        }

        private DropDownChoice<ServiceDescriptor> initSCMDomains(List<ServiceManager> managers, String dropDownID) {
            final List<ServiceDescriptor> descritors = new ArrayList<ServiceDescriptor>();

            for (ServiceManager sm : managers) {
                descritors.add(sm.getDescriptor());
            }

            IModel<List<ServiceDescriptor>> dropDownModel = new LoadableDetachableModel<List<ServiceDescriptor>>() {
                @Override
                protected List<ServiceDescriptor> load() {
                    return descritors;
                }
            };


            DropDownChoice<ServiceDescriptor> descriptorDropDownChoice = new DropDownChoice<ServiceDescriptor>(
                dropDownID, dropDownModel, new IChoiceRenderer<ServiceDescriptor>() {

                    public String getDisplayValue(ServiceDescriptor object) {
                        return object.getName().toString();
                    }

                    public String getIdValue(ServiceDescriptor object, int index) {
                        return object.getImplementationType().getSimpleName();
                    }


                });

            return descriptorDropDownChoice;
        }
    }


    private final class SetAttributesStep extends WizardStep {

         public SetAttributesStep() {
             super(new ResourceModel("scmAttribute.title"), new ResourceModel("scmAttribute.summary"),
                 new Model<Project>(project));
             IModel<List<AttributeDefinition>> attributeDefinitions = new LoadableDetachableModel<List<AttributeDefinition>>(){
                 @Override
                 public List<AttributeDefinition> load(){
                     return buildAttributeList();
                 }
             }  ;
         }

         private List<AttributeDefinition> buildAttributeList() {
             AttributeDefinition.Builder builder = AttributeDefinition.builder(new WicketStringLocalizer(this));
             AttributeDefinition id = builder.id("id").name("attribute.id.name").description("attribute.id.description")
                 .required().build();
             List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
             attributes.add(id);
             attributes.addAll(project.getScmDescriptor().getAttributes());
             return attributes;
         }

     }


    private final class FinalStep extends WizardStep {
        public FinalStep() {
            super(new ResourceModel("final.title"), new ResourceModel("final.summary"), new Model<Project>(project));
            add(new Label("projectId.confirm", new PropertyModel(project, "id")));
        }
    }


    public ProjectWizard(String id) {
        super(id);
        project = new Project();
        setDefaultModel(new CompoundPropertyModel<ProjectWizard>(this));
        WizardModel model = new WizardModel();
        model.add(new CreateProjectStep());
        model.add(new SetSCMStep());
        model.add(new SetAttributesStep());
        model.add(new FinalStep());
        init(model);
    }

    @Override
    public void onCancel() {
        setResponsePage(Index.class);
    }

    /**
     * @see org.apache.wicket.extensions.wizard.Wizard#onFinish()
     */
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


}

