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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.domain.report.ReportDomain;
import org.openengsb.opencit.core.projectmanager.NoSuchProjectException;
import org.openengsb.opencit.core.projectmanager.ProjectManager;
import org.openengsb.opencit.core.projectmanager.model.Project;

public class IndexPageTest extends AbstractCitPageTest {

    private ProjectManager projectManager;
    private WicketTester wicketTester;

    @Before
    public void setUp() throws NoSuchProjectException {
        wicketTester = getTester();
        when(projectManager.getProject("test")).thenReturn(new Project("test"));
    }

    @Override
    protected Map<String, Object> getBeansForAppContextAsMap() {
        Map<String, Object> mockedBeansMap = new HashMap<String, Object>();

        projectManager = Mockito.mock(ProjectManager.class);

        mockedBeansMap.put("contextCurrentService", mock(ContextCurrentService.class));
        mockedBeansMap.put("projectManager", projectManager);
        mockedBeansMap.put("reportDomain", mock(ReportDomain.class));
        mockedBeansMap.put("domainService", mock(DomainService.class));
        mockedBeansMap.put("workflowService", mock(WorkflowService.class));

        return mockedBeansMap;
    }

    @Test
    public void testProjectlistHeaderPresent_shouldWork() {
        Page indexPage = wicketTester.startPage(new Index());
        getTester().assertContains(indexPage.getString("projectlist.title"));
    }

    @Test
    public void testNoProjects_shouldShowLabel() {
        Page indexPage = wicketTester.startPage(new Index());
        getTester().assertContains(indexPage.getString("noProjectsAvailable"));
    }

    @Test
    public void testProjectsAvailable_shouldShowProjectId() throws NoSuchProjectException {
        when(projectManager.getAllProjects()).thenReturn(Arrays.asList(new Project[]{ new Project("test") }));
        when(projectManager.getProject("test")).thenReturn(new Project("test"));
        getTester().startPage(new Index());
        getTester().assertContains("test");
        String item = "projectlistPanel:projectlist:0";
        Image image = (Image) getTester().getComponentFromLastRenderedPage(item + ":project.state");
        Link<?> link = (Link<?>) getTester().getComponentFromLastRenderedPage(item + ":project.details");
        assertThat(image.isVisible(), is(true));
        assertThat(link.isVisible(), is(true));
        getTester().clickLink(item + ":project.details");
        String expectedPage = ProjectDetails.class.getName();
        assertThat(getTester().getLastRenderedPage().getClass().getName(), is(expectedPage));
    }

    @Test
    public void testDeleteProject_shouldDeleteProject() throws NoSuchProjectException {
        when(projectManager.getAllProjects()).thenReturn(Arrays.asList(new Project[]{ new Project("test") }));
        when(projectManager.getProject("test")).thenReturn(new Project("test"));
        Page indexPage = getTester().startPage(new Index());
        when(projectManager.getAllProjects()).thenReturn(Arrays.asList(new Project[]{}));
        getTester().clickLink("projectlistPanel:projectlist:0:deleteProject", true);
        Mockito.verify(projectManager).deleteProject("test");
        getTester().assertContains(indexPage.getString("noProjectsAvailable"));
    }

    @Test
    public void testCreateProjectLink_shouldBeCreateNewProject() {
        Page indexPage = wicketTester.startPage(new Index());
        wicketTester.assertContains(indexPage.getString("newProject.title"));
    }

    @Test
    public void testCreateProjectLink_shouldReturnFirstPageForWizzard() {
        Page indexPage = wicketTester.startPage(new Index());
        wicketTester.assertContains(indexPage.getString("newProject.title"));
        wicketTester.debugComponentTrees();
        wicketTester.clickLink("newProject", true);
        wicketTester.assertContains(indexPage.getString("newProject.summary"));
    }
}
