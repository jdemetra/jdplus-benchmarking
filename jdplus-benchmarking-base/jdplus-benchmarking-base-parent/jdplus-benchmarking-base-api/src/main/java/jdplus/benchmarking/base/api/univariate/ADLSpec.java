/*
 * Copyright 2022 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.benchmarking.base.api.univariate;

import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParameterType;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.util.Validatable;

/**
 * Simple auto-regressive distributed lags model. More complex models could be
 * considered, but they are less relevant in the context of temporal
 * disaggregation We consider the model y(t)= phi * y(t-1) + m + g*t + x(t)*b0 +
 * x(t-1)*b1 + e(t), with phi in ]-1,1]
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Getter
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class ADLSpec implements ProcSpecification, Validatable<ADLSpec> {

    public static final String VERSION = "1.0.0";

    public static final String FAMILY = "temporaldisaggregation";
    public static final String METHOD = "adl";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    public static final SsfInitialization DEF_ALGORITHM = SsfInitialization.SqrtDiffuse;
    public static final boolean DEF_FAST = true, DEF_RESCALE = true, DEF_LOG = false, DEF_DIFFUSE = false;

    public static final double DEF_EPS = 1e-5;

    public static final AggregationType DEF_AGGREGATION = AggregationType.Sum;

    /**
     * constraints on the coefficients of the lagged regression variables
     */
    public static enum XAR {
        /**
         * No lag on x
         */
        NONE,
        /**
         * b1 = -phi * b0 (same auto-regressive polynomial on y and x)
         */
        SAME,
        /**
         * No constraint on the coefficient b1
         */
        FREE;
    }

    public static final ADLSpec CHOWLIN = builder()
            .estimationSpan(TimeSelector.all())
            .aggregationType(AggregationType.Sum)
            .mean(true)
            .trend(false)
            .xar(XAR.SAME)
            .phi(Parameter.undefined())
            .truncation(0.0)
            .estimationPrecision(DEF_EPS)
            .rescale(DEF_RESCALE)
            .algorithm(DEF_ALGORITHM)
            .build();

    public static final ADLSpec FERNANDEZ = builder()
            .estimationSpan(TimeSelector.all())
            .aggregationType(AggregationType.Sum)
            .mean(false)
            .trend(false)
            .xar(XAR.SAME)
            .rescale(DEF_RESCALE)
            .algorithm(DEF_ALGORITHM)
            .build();
    
    public static final ADLSpec ADL_11 = builder().build();

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    @lombok.NonNull
    private AggregationType aggregationType;
    private int observationPosition;

    private boolean mean;
    private boolean trend;
    private XAR xar;
    private Parameter phi;
    @lombok.NonNull
    private TimeSelector estimationSpan;
    private boolean log, diffuseRegressors;
    private Double truncation;

    private double estimationPrecision;
    private SsfInitialization algorithm;
    private boolean rescale;

    public boolean isParameterEstimation() {
        return phi.getType() != ParameterType.Fixed;
    }

    public static class Builder implements Validatable.Builder<ADLSpec> {
    }

    public static Builder builder() {
        return new Builder()
                .aggregationType(DEF_AGGREGATION)
                .mean(true)
                .trend(false)
                .xar(XAR.FREE)
                .estimationSpan(TimeSelector.all())
                .algorithm(DEF_ALGORITHM)
                .rescale(DEF_RESCALE)
                .truncation(0.0)
                .phi(Parameter.undefined())
                .estimationPrecision(DEF_EPS);
    }

    @Override
    public ADLSpec validate() throws IllegalArgumentException {
        return this;
    }

    @Override
    public String display() {
        if (aggregationType == AggregationType.Average || aggregationType == AggregationType.Sum) {
            StringBuilder builder = new StringBuilder();
            builder.append("Disaggregation [")
                    .append(']');
            return builder.toString();

        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Interpolation [")
                    .append(']');
            return builder.toString();
        }

    }

}
