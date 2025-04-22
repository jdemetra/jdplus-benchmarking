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
package jdplus.benchmarking.base.information;

import jdplus.benchmarking.base.api.univariate.AlgorithmSpec;
import jdplus.benchmarking.base.api.univariate.ModelSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.information.InformationSetSerializer;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.TsEstimationSpec;
import jdplus.toolkit.base.api.timeseries.TimeSelector;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class TemporalDisaggregationSpecMapping {

    public final String SPAN = "span", MODEL = "model", PARAMETER = "parameter", AGGTYPE = "aggregation",
            CONSTANT = "constant", TREND = "trend", ZEROINIT = "zeroinit", DIFFUSEREGS = "diffuseregs",
            EPS = "precision", LOG = "log", SSF = "ssfoption", FREQ = "defaultfrequency", FAST = "fast", RESCALING = "rescaling", TRUNCATED = "truncatedrho";

    public static final InformationSetSerializer<TemporalDisaggregationSpec> SERIALIZER = new InformationSetSerializer<TemporalDisaggregationSpec>() {
        @Override
        public InformationSet write(TemporalDisaggregationSpec object, boolean verbose) {
            return TemporalDisaggregationSpecMapping.write(object, verbose);
        }

        @Override
        public TemporalDisaggregationSpec read(InformationSet info) {
            return TemporalDisaggregationSpecMapping.read(info);
        }

        @Override
        public boolean match(DemetraVersion version) {
            return version == DemetraVersion.JD3;
        }
    };

    public TemporalDisaggregationSpec read(InformationSet info) {
        if (info == null) {
            return TemporalDisaggregationSpec.CHOWLIN;
        }
        AlgorithmDescriptor desc = info.get(ProcSpecification.ALGORITHM, AlgorithmDescriptor.class);
        if (!desc.isCompatible(TemporalDisaggregationSpec.DESCRIPTOR)) {
            throw new IllegalArgumentException();
        }
        TemporalDisaggregationSpec.Builder builder = TemporalDisaggregationSpec.builder();
        ModelSpec.Builder mbuilder=ModelSpec.builder();
        TsEstimationSpec.Builder ebuilder=TsEstimationSpec.builder();
        AlgorithmSpec.Builder abuilder=AlgorithmSpec.builder();
        TimeSelector sel = info.get(SPAN, TimeSelector.class);
        if (sel != null) {
            ebuilder.estimationSpan(sel);
        }
        String n = info.get(MODEL, String.class);
        if (n != null) {
            mbuilder.residualsModel(ResidualsModel.valueOf(n));
        } else {
            mbuilder.residualsModel(ResidualsModel.Ar1)
                    .constant(true);
        }
        Integer i = info.get(FREQ, Integer.class);
        if (i != null) {
            builder.defaultPeriod(i);
        }
        Parameter p = info.get(PARAMETER, Parameter.class);
        if (p != null) {
            mbuilder.parameter(p);
        }
        n = info.get(AGGTYPE, String.class);
        if (n != null) {
            builder.aggregationType(AggregationType.valueOf(n));
        }
        Boolean b = info.get(CONSTANT, Boolean.class);
        if (b != null) {
            mbuilder.constant(b);
        }
        b = info.get(TREND, Boolean.class);
        if (b != null) {
            mbuilder.trend(b);
        }
        n = info.get(SSF, String.class);
        if (n != null) {
            abuilder.algorithm(SsfInitialization.valueOf(n));
        }
        b = info.get(ZEROINIT, Boolean.class);
        if (b != null) {
            mbuilder.zeroInitialization(b);
        }
//        b = info.get(LOG, Boolean.class);
//        if (b != null) {
//            mbuilder.log(b);
//        }
        b = info.get(DIFFUSEREGS, Boolean.class);
        if (b != null) {
            mbuilder.diffuseRegressors(b);
        }
        b = info.get(FAST, Boolean.class);
        if (b != null) {
            abuilder.fast(b);
        }
        b = info.get(RESCALING, Boolean.class);
        if (b != null) {
            abuilder.rescale(b);
        }
        Double t = info.get(TRUNCATED, Double.class);
        if (t != null) {
            ebuilder.truncatedParameter(t);
        }
        Double e = info.get(EPS, Double.class);
        if (e != null) {
            ebuilder.estimationPrecision(e);
        }
        return builder
                .modelSpec(mbuilder.build())
                .estimationSpec(ebuilder.build())
                .algorithmSpec(abuilder.build())
                .build();
    }

    public InformationSet write(TemporalDisaggregationSpec spec, boolean verbose) {
        InformationSet info = new InformationSet();
        info.set(ProcSpecification.ALGORITHM, TemporalDisaggregationSpec.DESCRIPTOR);
        TimeSelector span = spec.getEstimationSpec().getEstimationSpan();
        if (span.getType() != TimeSelector.SelectionType.All) {
            info.set(SPAN, span);
        }
        info.set(MODEL, spec.getModelSpec().getResidualsModel().name());
        Parameter p = spec.getModelSpec().getParameter();
        if (p != null && p.isDefined()) {
            info.set(PARAMETER, p);
        }
        if (spec.getAggregationType() != TemporalDisaggregationSpec.DEF_AGGREGATION || verbose) {
            info.set(AGGTYPE, spec.getAggregationType().name());
        }
        info.set(CONSTANT, spec.getModelSpec().isConstant());
        info.set(TREND, spec.getModelSpec().isTrend());
        info.set(FREQ, spec.getDefaultPeriod());
        if (spec.getAlgorithmSpec().getAlgorithm() != TemporalDisaggregationSpec.DEF_ALGORITHM || verbose) {
            info.set(SSF, spec.getAlgorithmSpec().getAlgorithm().name());
        }
        if (spec.getModelSpec().isZeroInitialization() || verbose) {
            info.set(ZEROINIT, spec.getModelSpec().isZeroInitialization());
        }
//        if (spec.isLog() != TemporalDisaggregationSpec.DEF_LOG || verbose) {
//            info.set(LOG, spec.isLog());
//        }
        if (spec.getModelSpec().isDiffuseRegressors() != TemporalDisaggregationSpec.DEF_DIFFUSE || verbose) {
            info.set(DIFFUSEREGS, spec.getModelSpec().isDiffuseRegressors());
        }
        if (spec.getAlgorithmSpec().isFast() != TemporalDisaggregationSpec.DEF_FAST || verbose) {
            info.set(FAST, spec.getAlgorithmSpec().isFast());
        }
        if (spec.getAlgorithmSpec().isRescale() != TemporalDisaggregationSpec.DEF_RESCALE || verbose) {
            info.set(RESCALING, spec.getAlgorithmSpec().isRescale());
        }
        if (spec.getEstimationSpec().getEstimationPrecision() != TemporalDisaggregationSpec.DEF_EPS || verbose) {
            info.set(EPS, spec.getEstimationSpec().getEstimationPrecision());
        }
        if (spec.getEstimationSpec().getTruncatedParameter() != 0 || verbose) {
            info.set(TRUNCATED, spec.getEstimationSpec().getTruncatedParameter());
        }
        return info;
    }

}
