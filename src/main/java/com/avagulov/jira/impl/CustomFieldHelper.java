package com.avagulov.jira.impl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomFieldHelper {

    private static final Logger log = LogManager.getLogger("atlassian.plugin");

    @Autowired
    private CustomFieldManager customFieldManager;
    @Autowired
    private FieldScreenManager fieldScreenManager;

    public void createCustomNumericField(String fieldname, boolean required) {
        try {
            if (customFieldManager.getCustomFieldObjectsByName(fieldname).size() == 0) {
                List<IssueType> issueTypes = new ArrayList<IssueType>();
                issueTypes.add(null);

                //Create a list of project contexts for which the custom field needs to be available
                List<JiraContextNode> contexts = new ArrayList<JiraContextNode>();
                contexts.add(GlobalIssueContext.getInstance());
                CustomFieldType cft = customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:float");


                //Add custom field
                CustomField cField = customFieldManager.createCustomField(fieldname, fieldname,
                        customFieldManager.getCustomFieldType("com.atlassian.jira.plugin.system.customfieldtypes:float"),
                        customFieldManager.getDefaultSearcher(cft),
                        contexts, issueTypes);

                // Add field to default Screen
                for (FieldScreen fs : fieldScreenManager.getFieldScreens()) {
                    if (!fs.containsField(cField.getId())) {
                        FieldScreenTab firstTab = fs.getTab(0);
                        firstTab.addFieldScreenLayoutItem(cField.getId());
                    }
                }

                if(required) {
                    FieldLayoutItem layoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout().getFieldLayoutItem(cField);
                    EditableDefaultFieldLayout fieldLayout = ComponentAccessor.getFieldLayoutManager().getEditableDefaultFieldLayout();
                    fieldLayout.makeRequired(layoutItem);
                    ComponentAccessor.getFieldLayoutManager().storeEditableDefaultFieldLayout(fieldLayout);
                }
            }
        } catch (Exception ex) {
            log.error("CreateCustomNumericField", ex);
        }
    }


}
