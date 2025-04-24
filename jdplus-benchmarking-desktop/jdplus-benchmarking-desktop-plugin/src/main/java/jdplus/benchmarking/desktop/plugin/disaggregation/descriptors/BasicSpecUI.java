/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.desktop.plugin.disaggregation.descriptors;

import jdplus.toolkit.base.api.data.Parameter;
import jdplus.benchmarking.desktop.plugin.benchmarking.descriptors.Utility;
import jdplus.toolkit.desktop.plugin.descriptors.DateSelectorUI;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.benchmarking.base.api.univariate.ModelSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
import jdplus.benchmarking.base.api.univariate.TsEstimationSpec;

/**
 *
 * @author Jean Palate
 */
public class BasicSpecUI extends BaseTemporalDisaggregationSpecUI {

    public static final String DISPLAYNAME = "Basic";
    public static final String ERROR_NAME = "Error", PARAM_NAME = "Parameter", CONSTANT_NAME = "Constant", TREND_NAME = "Trend", AVERAGE_NAME="Average", TYPE_NAME = "Type", SPAN_NAME = "Estimation span", DEFFREQ_NAME = "Default frequency";
    public static final String ERROR_DESC = "Model of the regression error", PARAM_DESC = "Parameter", CONSTANT_DESC = "Constant", TREND_DESC = "Trend", TYPE_DESC = "Type", AVERAGE_DESC= "Average", SPAN_DESC = "Estimation span", DEFFREQ_DESC = "Default frequency";
    public static final int SPAN_ID = 0, ERROR_ID = 5, PARAM_ID = 10, CONSTANT_ID = 15, TREND_ID = 20, TYPE_ID = 30, AVERAGE_ID = 31, DEFFREQ_ID = 40;

    @Override
    public String toString() {
        return "";
    }

    public BasicSpecUI(TemporalDisaggregationSpecRoot root) {
        super(root);
    }

    public ResidualsModel getErrorModel() {
        return core().getModelSpec().getResidualsModel();
    }

    public void setErrorModel(ResidualsModel model) {
        ModelSpec mspec = core().getModelSpec();
        ModelSpec.Builder builder = mspec.toBuilder()
                .residualsModel(model);
        if (model.getDifferencingOrder() == 1 && !mspec.isZeroInitialization()) {
            builder.constant(false);
        }
        if (model.getDifferencingOrder() > 1) {
            builder.zeroInitialization(false)
                    .constant(false)
                    .trend(false);
        }
        update(core().toBuilder()
                .modelSpec(builder.build())
                .build());
    }

    public Parameter[] getParameter() {
        return new Parameter[]{core().getModelSpec().getParameter()};
    }

    public void setParameter(Parameter[] p) {
        ModelSpec mspec = core().getModelSpec().toBuilder()
                .parameter(p[0])
                .build();
        update(core().toBuilder()
                .modelSpec(mspec)
                .build());
    }

    public boolean isConstant() {
        return core().getModelSpec().isConstant();
    }

    public void setConstant(boolean cnt) {
        ModelSpec mspec = core().getModelSpec().toBuilder()
                .constant(cnt)
                .build();
        update(core().toBuilder()
                .modelSpec(mspec)
                .build());
    }

    public boolean isTrend() {
        return core().getModelSpec().isTrend();
    }

    public void setTrend(boolean t) {
        ModelSpec mspec = core().getModelSpec().toBuilder()
                .trend(t)
                .build();
        update(core().toBuilder()
                .modelSpec(mspec)
                .build());
    }

    public boolean isAverage() {
        return core().isAverage();
    }

    public void setAverage(boolean average) {
        update(core().toBuilder()
                .average(average)
                .build());
    }

    public int getFrequency() {
        return core().getDefaultPeriod();
    }

    public void setFrequency(int freq) {
        update(core().toBuilder().defaultPeriod(freq).build());
    }

    public DateSelectorUI getSpan() {
        return new DateSelectorUI(core().getEstimationSpec().getEstimationSpan(), isRo(), span -> updateSpan(span));
    }

    public void updateSpan(TimeSelector span) {
        TsEstimationSpec espec = core().getEstimationSpec().toBuilder()
                .estimationSpan(span)
                .build();
        update(core().toBuilder()
                .estimationSpec(espec)
                .build());
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> props = new ArrayList<>();
        EnhancedPropertyDescriptor desc = spanDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = errorDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = parameterDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = cntDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = trendDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = averageDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = freqDesc();
        if (desc != null) {
            props.add(desc);
        }
        return props;
    }

    private EnhancedPropertyDescriptor spanDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("span", this.getClass(), "getSpan", null);
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, SPAN_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setShortDescription(SPAN_DESC);
            desc.setDisplayName(SPAN_NAME);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor errorDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("ErrorModel", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ERROR_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(ERROR_NAME);
            desc.setShortDescription(ERROR_DESC);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor cntDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Constant", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, CONSTANT_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(CONSTANT_NAME);
            desc.setShortDescription(CONSTANT_DESC);
            edesc.setReadOnly(isRo() || (core().getModelSpec().getResidualsModel().getDifferencingOrder() == 1 && !core().getModelSpec().isZeroInitialization())
                    || core().getModelSpec().getResidualsModel().getDifferencingOrder() > 1);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor parameterDesc() {
        if (!core().getModelSpec().getResidualsModel().hasParameter()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Parameter", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, PARAM_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(PARAM_NAME);
            desc.setShortDescription(PARAM_DESC);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor trendDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Trend", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TREND_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(TREND_NAME);
            desc.setShortDescription(TREND_DESC);
            edesc.setReadOnly(isRo() || core().getModelSpec().getResidualsModel().getDifferencingOrder() > 1);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor averageDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Average", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, AVERAGE_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(AVERAGE_NAME);
            desc.setShortDescription(AVERAGE_DESC);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor freqDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Frequency", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, DEFFREQ_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(DEFFREQ_NAME);
            desc.setShortDescription(DEFFREQ_DESC);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    @Override
    public String getDisplayName() {
        return DISPLAYNAME;
    }
}
