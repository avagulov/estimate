package com.avagulov.jira.impl;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.ContextProvider;
import com.atlassian.query.Query;
import com.google.common.collect.Maps;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Scanned
public class DashboardItemContextProvider implements ContextProvider {
    private static final Logger log = LogManager.getLogger("atlassian.plugin");

    private final PluginAccessor pluginAccessor;
    private final IssueManager issueManager;
    private final SearchService searchService;
    private final CustomFieldManager customFieldManager;

    public DashboardItemContextProvider(
            @ComponentImport PluginAccessor pluginAccessor,
            @ComponentImport IssueManager issueManager,
            @ComponentImport SearchService searchService,
            @ComponentImport CustomFieldManager customFieldManager) {
        this.pluginAccessor = pluginAccessor;
        this.issueManager = issueManager;
        this.searchService = searchService;
        this.customFieldManager = customFieldManager;
    }

    @Override
    public void init(final Map<String, String> params) throws PluginParseException {
    }

    @Override
    public Map<String, Object> getContextMap(final Map<String, Object> context) {

        Plugin plugin = pluginAccessor.getEnabledPlugin("com.avagulov.jira.estimate");

        final Map<String, Object> newContext = Maps.newHashMap(context);



        newContext.put("version", plugin.getPluginInformation().getVersion());
        newContext.put("pluginName", plugin.getName());



        return newContext;
    }
}