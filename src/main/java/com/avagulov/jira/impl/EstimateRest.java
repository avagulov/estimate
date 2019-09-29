package com.avagulov.jira.impl;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/hist")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class EstimateRest {
    private static final Logger log = LogManager.getLogger("atlassian.plugin");

    private final SearchService searchService;
    private final CustomFieldManager customFieldManager;

    public EstimateRest(
            @ComponentImport SearchService searchService,
            @ComponentImport CustomFieldManager customFieldManager) {

        this.searchService = searchService;
        this.customFieldManager = customFieldManager;
    }


    @GET
    public Response get() {

        try {

            JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
            Query query = builder.buildQuery();
            List<Issue> issues = new LinkedList<>();

            final SearchResults results = searchService.search(ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser(),
                    query, PagerFilter.getUnlimitedFilter());
            issues = results.getIssues();

            Optional<CustomField> trcf = customFieldManager.getCustomFieldObjectsByName(MyPluginComponentImpl.TASK_RATE)
                    .stream().findFirst();

            if (trcf.isPresent()) {

                Object data = issues.stream()
                        .map(i ->
                                {
                                    Double d = (Double) i.getCustomFieldValue(trcf.get());
                                    if (d != null)
                                        return Math.round(d);
                                    return 0;
                                }
                        ).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                return Response.ok(data).build();
            }
        } catch (Exception ex) {
            log.error(ex);
        }

        return Response.ok(null).build();
    }
}