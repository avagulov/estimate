define('dashboard-item/estimate', ['underscore', 'jquery', 'wrm/context-path'], function (_, $, contextPath) {
    var DashboardItem = function (API) {
        this.API = API;
        this.issues = [];
    };

    DashboardItem.prototype.render = function (context, preferences) {
        var $element = this.$element = $(context).find("#dynamic-content");

        var self = this;
        this.requestData().done(function (data) {

            $.getScript( "https://code.highcharts.com/highcharts.src.js", function() {
                var newData = _.map(data, function(num, key){ return {
                    name: key,
                    y: num
                } });

                Highcharts.chart('dynamic-content', {
                    chart: {
                        plotBackgroundColor: null,
                        plotBorderWidth: null,
                        plotShadow: false,
                        type: 'pie'
                    },
                    title: {
                        text: 'Task Rates'
                    },
                    tooltip: {
                        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: true,
                                format: '<b>{point.name}</b>: {point.percentage:.1f} %'
                            },
                            showInLegend: true
                        }
                    },
                    series: [{
                        name: 'Rates',
                        colorByPoint: true,
                        data: newData
                    }],
                    legend: {
                        enabled: true
                    }
                });

            });

            $(context).find(".submit").click(function (event) {
                event.preventDefault();
                self.render($element.parent(), preferences);
            });

        });

    };

    DashboardItem.prototype.requestData = function () {
        return $.ajax({
            method: "GET",
            url: contextPath() + "/rest/estimate/1.0/hist"
        });
    };

    return DashboardItem;
});