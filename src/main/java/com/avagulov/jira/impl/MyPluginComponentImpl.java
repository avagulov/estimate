package com.avagulov.jira.impl;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.GlobalIssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.avagulov.jira.api.MyPluginComponent;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@ExportAsService ({MyPluginComponent.class})
@Component("myPluginComponent")
public class MyPluginComponentImpl implements MyPluginComponent, InitializingBean, DisposableBean {
    private static final Logger log = LogManager.getLogger("atlassian.plugin");

    private final ApplicationProperties applicationProperties;
    private final EventPublisher eventPublisher;
    private final CustomFieldManager customFieldManager;
    private final FieldScreenManager fieldScreenManager;
    private final IssueManager issueManager;

    @Autowired
    private CustomFieldHelper customFieldHelper;

    @Autowired
    public MyPluginComponentImpl(@ComponentImport ApplicationProperties applicationProperties,
                                 @ComponentImport EventPublisher eventPublisher,
                                 @ComponentImport CustomFieldManager customFieldManager,
                                 @ComponentImport FieldScreenManager fieldScreenManager,
                                 @ComponentImport IssueManager issueManager) {
        this.applicationProperties = applicationProperties;
        this.eventPublisher = eventPublisher;
        this.customFieldManager = customFieldManager;
        this.fieldScreenManager = fieldScreenManager;
        this.issueManager = issueManager;
    }

    public String getName() {
        if (null != applicationProperties) {
            return "myComponent:" + applicationProperties.getDisplayName();
        }

        return "myComponent";
    }

    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("@@@@");
        eventPublisher.register(this);

    }

    public static final String BUSINESS_WEIGHT = "Business Weight";
    public static final String DEVELOPMENT_WEIGHT = "Development Weight";
    public static final String TASK_RATE = "Task Rate";

    @EventListener
    public void onPluginEnable(PluginEnabledEvent pluginEnabledEvent) {
        log.info("~Init plugin~");

        customFieldHelper.createCustomNumericField(BUSINESS_WEIGHT, true);
        customFieldHelper.createCustomNumericField(DEVELOPMENT_WEIGHT, true);
        customFieldHelper.createCustomNumericField(TASK_RATE, false);
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {

        Long eventTypeId = issueEvent.getEventTypeId();
        Issue issue = issueEvent.getIssue();

        if (EventType.ISSUE_UPDATED_ID.equals(eventTypeId) || EventType.ISSUE_CREATED_ID.equals(eventTypeId)) {

            Optional<CustomField> bwcf = customFieldManager.getCustomFieldObjectsByName(BUSINESS_WEIGHT).stream().findFirst();
            Optional<CustomField> dwcf = customFieldManager.getCustomFieldObjectsByName(DEVELOPMENT_WEIGHT).stream().findFirst();
            Optional<CustomField> trcf = customFieldManager.getCustomFieldObjectsByName(TASK_RATE).stream().findFirst();

            if (bwcf.isPresent() && dwcf.isPresent() && trcf.isPresent()) {

                long businessWeight =
                        Math.round((Double) issue.getCustomFieldValue(bwcf.get()));

                long developmentWeight =
                        Math.round(((Double) issue.getCustomFieldValue(dwcf.get())));

                if (businessWeight < 1) businessWeight = 1;
                if (businessWeight > 5) businessWeight = 5;
                if (developmentWeight < 1) developmentWeight = 1;
                if (developmentWeight > 5) developmentWeight = 5;

                MutableIssue mutableIssue = issueManager.getIssueByKeyIgnoreCase(issue.getKey());
                mutableIssue.setCustomFieldValue(bwcf.get(), businessWeight * 1d);
                mutableIssue.setCustomFieldValue(dwcf.get(), developmentWeight * 1d);
                mutableIssue.setCustomFieldValue(trcf.get(), (businessWeight - 1) * 5 + developmentWeight * 1d);
                issueManager.updateIssue(issueEvent.getUser(), mutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false);
            }

        }

    }
}