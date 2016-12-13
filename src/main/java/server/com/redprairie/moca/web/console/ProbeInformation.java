/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.web.console;

import java.util.Map;
import java.util.Set;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.MetricName;

/**
 * Retrieve probe information as MocaResults.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author mdobrini
 */
public class ProbeInformation {
    
    private ProbeInformation() {
    }

    public static MocaResults getProbesMocaResults() {
        EditableResults res = new SimpleResults();
        res.addColumn("group", MocaType.STRING);
        res.addColumn("type", MocaType.STRING);
        res.addColumn("scope", MocaType.STRING);
        res.addColumn("name", MocaType.STRING);
        res.addColumn("attribute",MocaType.STRING);
        res.addColumn("value",MocaType.STRING);
        
        Set<Map.Entry<MetricName,Metric>> metricMap = Metrics.defaultRegistry().getAllMetrics().entrySet();
        for (Map.Entry<MetricName, Metric> entry : metricMap){
            Metric metric = entry.getValue();
            MetricName metricName = entry.getKey();
            
            // counter
            if (metric instanceof Counter) {
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "count",
                    ((Counter) metric).getCount() + "");
            }
            // histogram
            else if (metric instanceof Histogram) {
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "min",
                    ((Histogram) metric).getMin() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "max",
                    ((Histogram) metric).getMax() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "mean",
                    ((Histogram) metric).getMean() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "std dev",
                    ((Histogram) metric).getStdDev() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "sum",
                    ((Histogram) metric).getSum() + "");
            }
            // meter
            else if (metric instanceof Meter) {
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "count",
                    ((Meter) metric).getCount() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "1m rate",
                    ((Meter) metric).getOneMinuteRate() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "5m rate",
                    ((Meter) metric).getFiveMinuteRate() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "15m rate",
                    ((Meter) metric).getFifteenMinuteRate() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "mean rate",
                    ((Meter) metric).getMeanRate() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "event type",
                    ((Meter) metric).getEventType());
            }
            // timer
            else if (metric instanceof Timer) {
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "count",
                    ((Timer) metric).getCount() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "duration unit",
                    ((Timer) metric).getDurationUnit().toString());
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "1m rate",
                    ((Timer) metric).getOneMinuteRate() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "5m rate",
                    ((Timer) metric).getFiveMinuteRate() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "15m rate",
                    ((Timer) metric).getFifteenMinuteRate() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "mean rate",
                    ((Timer) metric).getMeanRate() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "min",
                    ((Timer) metric).getMin() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "max",
                    ((Timer) metric).getMax() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "mean",
                    ((Timer) metric).getMean() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "rate unit",
                    ((Timer) metric).getRateUnit().toString());
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "std dev",
                    ((Timer) metric).getStdDev() + "");
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "sum",
                    ((Timer) metric).getSum() + "");
            }
            // gauge and its sub-classes
            else if (Gauge.class.isAssignableFrom(metric.getClass())) {
                String value = "";
                Gauge<?> gaugeMetric = ((Gauge<?>) metric);
                if (gaugeMetric.getValue() != null) {
                    value = gaugeMetric.getValue().toString();
                }
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "value",
                    value);
            }
            // unsupported type
            else {
                addRow(res, metricName.getGroup(), metricName.getType(), metricName.getScope(),
                    metricName.getName(), "Unknown Gauge Type Attribute",
                    "Unknown Gauge Type Value");
            }
        }
        
        return res;
    }
    
    public static MocaResults addRow(EditableResults toChange, String group, String type, String scope, String name, String attribute, String value) {
        toChange.addRow();
        toChange.setStringValue("group", group); 
        toChange.setStringValue("type",  type);
        toChange.setStringValue("scope", scope); 
        toChange.setStringValue("name", name); 
        toChange.setStringValue("attribute", attribute);
        toChange.setStringValue("value", value);
        return toChange;
    }
    
}
