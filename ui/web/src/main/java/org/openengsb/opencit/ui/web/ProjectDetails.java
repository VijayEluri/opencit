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

package org.openengsb.opencit.ui.web;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.resource.ContextRelativeResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.domain.report.model.Report;
import org.openengsb.opencit.core.projectmanager.CITTask;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;
import org.openengsb.opencit.core.projectmanager.model.ProjectStateInfo;
import org.openengsb.opencit.ui.web.model.ProjectModel;
import org.openengsb.opencit.ui.web.model.ReportModel;
import org.openengsb.opencit.ui.web.model.SpringBeanProvider;
import org.openengsb.opencit.ui.web.util.StateUtil;

public class ProjectDetails extends BasePage implements SpringBeanProvider<ProjectManager> {

    private static Log log = LogFactory.getLog(BasePage.class);

    private ProjectModel projectModel;

    @SpringBean
    private ContextCurrentService contextService;

    @SpringBean
    private WorkflowService workflowService;

    @SpringBean
    private ProjectManager projectManager;

    @SpringBean
    private ReportDomain reportDomain;

    private Image projectStateImage;

    private Button flowButton;

    private WebMarkupContainer projectPanel;

    public ProjectDetails() {
        String projectId = contextService.getThreadLocalContext();
        this.projectModel = new ProjectModel(projectId);
        init();
    }

    public ProjectDetails(ProjectModel projectModel) {
        this.projectModel = projectModel;
        init();
    }

    @SuppressWarnings("serial")
    private void init() {
        this.projectModel.setProjectManagerProvider(this);

        projectPanel = new WebMarkupContainer("projectPanel");
        projectPanel.setOutputMarkupId(true);
        add(projectPanel);

        Project project = projectModel.getObject();
        projectPanel.add(new Label("project.id", project.getId()));
        projectPanel.add(new AjaxEditableLabel<String>("project.notification", new IModel<String>() {
            @Override
            public void detach() {
                // do nothing
            }

            @Override
            public String getObject() {
                return ProjectDetails.this.projectModel.getObject().getNotificationRecipient();
            }

            @Override
            public void setObject(String recipient) {
                Project p = ProjectDetails.this.projectModel.getObject();
                p.setNotificationRecipient(recipient);
                try {
                    projectManager.updateProject(p);
                } catch (NoSuchProjectException e) {
                    log.error(e);
                }
            }
        }));
        String image = StateUtil.getImage(project, projectManager.getProjectState(project.getId()));
        ContextRelativeResource stateResource = new ContextRelativeResource(image);
        stateResource.setCacheable(false);
        projectStateImage = new Image("project.state", stateResource);
        projectStateImage.setOutputMarkupId(true);

        projectPanel.add(projectStateImage);

        Date lastpollDate = projectManager.getProjectState(project.getId())
            .getLastpollDate();
        String dateString;
        if (lastpollDate == null) {
            dateString = "-";
        } else {
            dateString = lastpollDate.toString();
        }

        Label pollerStateLabel =
            new Label("pollerState", new Model<String>(dateString));
        projectPanel.add(pollerStateLabel);

        projectPanel.add(new Link<Index>("back") {
            @Override
            public void onClick() {
                setResponsePage(Index.class);
            }
        });

        Form<Project> form = new Form<Project>("workflowForm");
        form.setModel(projectModel);
        form.setOutputMarkupId(true);

        flowButton = new Button("flowButton") {

            @Override
            public void onSubmit() {
                Project project = ProjectDetails.this.projectModel.getObject();
                ProjectStateInfo state = projectManager.getProjectState(project.getId());
                String contextId = project.getId();
                log.info("CIT workflow for project '" + contextId + "' started manually through UI");
                contextService.setThreadLocalContext(contextId);

                CITTask citTask = new CITTask(workflowService, project.getId(), state);
                new Thread(citTask).start();
                setResponsePage(ProjectDetails.class);
            }

        };
        flowButton.setOutputMarkupId(true);

        flowButton.setEnabled(!projectManager.getProjectState(project.getId()).isBuilding());
        form.add(flowButton);
        projectPanel.add(form);

        FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        projectPanel.add(feedbackPanel);

        initReportPanel();
    }

    private void initReportPanel() {
        WebMarkupContainer reportsPanel = new WebMarkupContainer("reportsPanel");
        reportsPanel.setOutputMarkupId(true);

        IModel<List<Report>> reportsModel = createReportsModel();

        Label noReports =
            new Label("noReports", new StringResourceModel("noReportsAvailable", this, null));
        noReports.setVisible(false);
        noReports.setOutputMarkupId(true);

        reportsPanel.add(createReportListView(reportsModel, "reportlist"));
        reportsPanel.add(noReports);

        add(reportsPanel);
        if (reportsModel.getObject().isEmpty()) {
            noReports.setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    private IModel<List<Report>> createReportsModel() {
        return new LoadableDetachableModel<List<Report>>() {
            @Override
            protected List<Report> load() {
                String projectId = projectModel.getObject().getId();
                contextService.setThreadLocalContext(projectId);
                List<Report> reports = new ArrayList<Report>(reportDomain.getAllReports(projectId));
                Comparator<Report> comparator = Collections.reverseOrder(new Comparator<Report>() {
                    @Override
                    public int compare(Report report1, Report report2) {
                        String name1 = report1.getName();
                        String name2 = report2.getName();
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
                            Date date1 = format.parse(name1);
                            Date date2 = format.parse(name2);
                            return date1.compareTo(date2);
                        } catch (ParseException pe) {
                            return name1.compareTo(name2);
                        }
                    }
                });
                Collections.sort(reports, comparator);
                return reports;
            }
        };
    }

    @SuppressWarnings("serial")
    private ListView<Report> createReportListView(IModel<List<Report>> projectsModel,
            String id) {
        return new ListView<Report>(id, projectsModel) {

            @Override
            protected void populateItem(ListItem<Report> item) {
                Report report = item.getModelObject();

                ContextRelativeResource stateResource = new ContextRelativeResource(StateUtil.getImage(report));
                stateResource.setCacheable(false);
                Image projectStateImage = new Image("report.state", stateResource);
                projectStateImage.setOutputMarkupId(true);

                item.add(projectStateImage);

                item.add(new Label("report.name", report.getName()));
                item.add(new Link<Report>("report.link", item.getModel()) {
                    @Override
                    public void onClick() {
                        ReportModel reportModel = new ReportModel(projectModel.getObject()
                            .getId(), getModelObject());
                        reportModel.setReportDomainProvider(new SpringBeanProvider<ReportDomain>() {

                            @Override
                            public ReportDomain getSpringBean() {
                                return reportDomain;
                            }
                        });
                        reportModel.setContextServiceProvider(new SpringBeanProvider<ContextCurrentService>() {
                            @Override
                            public ContextCurrentService getSpringBean() {
                                return contextService;
                            }
                        });
                        setResponsePage(new ReportViewPage(projectModel, reportModel));
                    }
                });
            }

        };
    }

    @Override
    public ProjectManager getSpringBean() {
        return projectManager;
    }

}
