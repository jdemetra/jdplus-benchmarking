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

import jdplus.toolkit.desktop.plugin.DemetraUI;
import jdplus.toolkit.desktop.plugin.descriptors.EnhancedPropertyDescriptor;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import jdplus.benchmarking.base.api.univariate.AlgorithmSpec;
import jdplus.benchmarking.base.api.univariate.ModelSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
import jdplus.benchmarking.base.api.univariate.TsEstimationSpec;

/**
 *
 * @author Jean
 */
public class AdvancedSpecUI extends BaseTemporalDisaggregationSpecUI {

    public static final String DISPLAYNAME = "Advanced options";
    public static final String EPS_NAME = "Precision", KF_NAME = "Method", FAST_NAME = "Fast", ALGORITHM_NAME = "Algorithm",
            ZERO_NAME = "Zero initialization", TRUNCATED_NAME = "Truncated rho", DREGS_NAME = "Diffuse regression coefficients", RESCALE_NAME = "Rescale";
    public static final String EPS_DESC = "Precision", KF_DESC = "Kalman filter used for estimation", FAST_DESC = "Fast processing (Kohn-Ansley)", ALGORITHM_DESC = "Algorithm",
            ZERO_DESC = "Zero initialization", TRUNCATED_DESC = "Lower bound for the estimated coefficient", DREGS_DESC = "Diffuse regression coefficients", RESCALE_DESC = "Rescale the model";
    public static final int EPS_ID = 0, KF_ID = 10, FAST_ID = 15, ZERO_ID = 20, TRUNCATED_ID = 25, DREGS_ID = 30, RESCALE_ID = 40, ALGORITHM_ID = 50;

    @Override
    public String toString() {
        return "";
    }

    public AdvancedSpecUI(TemporalDisaggregationSpecRoot root) {
        super(root);
    }

    public double getEpsilon() {
        return core().getEstimationSpec().getEstimationPrecision();
    }

    public void setEpsilon(double eps) {
        TsEstimationSpec espec = core().getEstimationSpec().toBuilder()
                .estimationPrecision(eps)
                .build();
        update(core()
                .toBuilder()
                .estimationSpec(espec)
                .build());
    }

    public boolean isZeroInitialization() {
        return core().getModelSpec().isZeroInitialization();
    }

    public void setZeroInitialization(boolean t) {
        ModelSpec.Builder builder = core().getModelSpec().toBuilder()
                .zeroInitialization(t);
        if (!core().getModelSpec().getResidualsModel().isStationary() && !t) {
            builder.constant(false);
        }

        update(core()
                .toBuilder()
                .modelSpec(builder.build())
                .build());
     }

    public double getTruncatedRho() {
        return core().getEstimationSpec().getTruncatedParameter();
    }

    public void setTruncatedRho(double t) {
         TsEstimationSpec espec = core().getEstimationSpec().toBuilder()
                .truncatedParameter(t)
                .build();
        update(core()
                .toBuilder()
                .estimationSpec(espec)
                .build());
    }

    public SsfInitialization getAlgorithm() {
        return core().getAlgorithmSpec().getAlgorithm();
    }

    public void setAlgorithm(SsfInitialization initialization) {
        AlgorithmSpec aspec = core().getAlgorithmSpec().toBuilder()
                .algorithm(initialization)
                .build();
        update(core()
                .toBuilder()
                .algorithmSpec(aspec)
                .build());
    }

    public boolean isDiffuseRegression() {
        return core().getModelSpec().isDiffuseRegressors();
    }

    public void setDiffuseRegression(boolean t) {
       ModelSpec mspec = core().getModelSpec().toBuilder()
                .diffuseRegressors(t)
                .build();
        update(core()
                .toBuilder()
                .modelSpec(mspec)
                .build());
    }

    public boolean isFast() {
        return core().getAlgorithmSpec().isFast();
    }

    public void setFast(boolean t) {
        AlgorithmSpec aspec = core().getAlgorithmSpec().toBuilder()
                .fast(t)
                .build();
        update(core()
                .toBuilder()
                .algorithmSpec(aspec)
                .build());
    }

    public boolean isRescale() {
        return core().getAlgorithmSpec().isRescale();
    }

    public void setRescale(boolean t) {
        AlgorithmSpec aspec = core().getAlgorithmSpec().toBuilder()
                .rescale(t)
                .build();
        update(core()
                .toBuilder()
                .algorithmSpec(aspec)
                .build());
    }

    @Override
    public List<EnhancedPropertyDescriptor> getProperties() {
        ArrayList<EnhancedPropertyDescriptor> props = new ArrayList<>();
        EnhancedPropertyDescriptor desc = epsDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = algorithmDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = fastDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = rescaleDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = zeroDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = truncatedDesc();
        if (desc != null) {
            props.add(desc);
        }
        desc = dregsDesc();
        if (desc != null) {
            props.add(desc);
        }
        return props;
    }

    private EnhancedPropertyDescriptor epsDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Epsilon", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, EPS_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(EPS_NAME);
            desc.setShortDescription(EPS_DESC);
            edesc.setReadOnly(isRo() || !core().getModelSpec().getResidualsModel().hasParameter());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor zeroDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("ZeroInitialization", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ZERO_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(ZERO_NAME);
            desc.setShortDescription(ZERO_DESC);
            edesc.setReadOnly(isRo() || core().getModelSpec().getResidualsModel() == ResidualsModel.Wn
                    || core().getModelSpec().getResidualsModel().getDifferencingOrder() > 1);
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor truncatedDesc() {
        try {
            PropertyDescriptor desc = new PropertyDescriptor("TruncatedRho", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, TRUNCATED_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(TRUNCATED_NAME);
            desc.setShortDescription(TRUNCATED_DESC);
            edesc.setReadOnly(isRo() || (core().getModelSpec().getResidualsModel() != ResidualsModel.Ar1 || core().getModelSpec().getParameter().isFixed()));
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor fastDesc() {
        if (!DemetraUI.get().isLowLevelOptions()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Fast", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, FAST_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(FAST_NAME);
            desc.setShortDescription(FAST_DESC);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor algorithmDesc() {
        if (!DemetraUI.get().isLowLevelOptions()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Algorithm", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, ALGORITHM_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(ALGORITHM_NAME);
            desc.setShortDescription(ALGORITHM_DESC);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor rescaleDesc() {
        if (!DemetraUI.get().isLowLevelOptions()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("Rescale", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, RESCALE_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(RESCALE_NAME);
            desc.setShortDescription(RESCALE_DESC);
            edesc.setReadOnly(isRo());
            return edesc;
        } catch (IntrospectionException ex) {
            return null;
        }
    }

    private EnhancedPropertyDescriptor dregsDesc() {
        if (!core().getModelSpec().isParameterEstimation()) {
            return null;
        }
        try {
            PropertyDescriptor desc = new PropertyDescriptor("DiffuseRegression", this.getClass());
            EnhancedPropertyDescriptor edesc = new EnhancedPropertyDescriptor(desc, DREGS_ID);
            edesc.setRefreshMode(EnhancedPropertyDescriptor.Refresh.All);
            desc.setDisplayName(DREGS_NAME);
            desc.setShortDescription(DREGS_DESC);
            edesc.setReadOnly(isRo() || !core().getModelSpec().getResidualsModel().hasParameter());
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
