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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;

public class Index extends BasePage {

    public Index() {
        WebMarkupContainer connectingServicePanel = new WebMarkupContainer("projectlistPanel");
        connectingServicePanel.setOutputMarkupId(true);

        @SuppressWarnings("serial")
        IModel<List<String>> projectsModel = new LoadableDetachableModel<List<String>>() {
            @Override
            protected List<String> load() {
                List<String> projects = new ArrayList<String>();
                projects.add("testProject");
                return projects;
            }
        };

        Label noConServices =
            new Label("noProjects", new StringResourceModel("noProjectsAvailable", this, null).getString());
        noConServices.setVisible(false);
        noConServices.setOutputMarkupId(true);

        connectingServicePanel.add(createServiceListView(projectsModel, "projectlist"));
        connectingServicePanel.add(noConServices);

        add(connectingServicePanel);
        if (projectsModel.getObject().isEmpty()) {
            noConServices.setVisible(true);
        }
    }

    @SuppressWarnings("serial")
    private ListView<String> createServiceListView(IModel<List<String>> projectsModel,
            String id) {
        return new ListView<String>(id, projectsModel) {

            @Override
            protected void populateItem(ListItem<String> item) {
                String project = item.getModelObject();
                item.add(new Label("project.name", project));
            }
        };
    }

}
