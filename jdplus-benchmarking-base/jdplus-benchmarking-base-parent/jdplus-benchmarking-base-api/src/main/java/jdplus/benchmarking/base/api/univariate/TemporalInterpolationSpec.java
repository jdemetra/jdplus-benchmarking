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
import nbbrd.design.Development;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Getter
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class TemporalInterpolationSpec implements ProcSpecification, Validatable<TemporalInterpolationSpec> {

    public static final String VERSION = "3.0.0";

    public static final String FAMILY = "temporalinterpolation";
    public static final String METHOD = "generic";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    public static final SsfInitialization DEF_ALGORITHM = SsfInitialization.SqrtDiffuse;
    public static final boolean DEF_FAST = true, DEF_RESCALE = true, DEF_LOG = false, DEF_DIFFUSE = false;

    public static final double DEF_EPS = 1e-5;

    public static final int DEF_PERIOD = 4, DEF_OBSPOS = -1;

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }


    /**
     * 0 for first, -1 for last
     */
    private int observationPosition;
    private int defaultPeriod;

    @lombok.NonNull
    ModelSpec modelSpec;

    @lombok.NonNull
    TsEstimationSpec estimationSpec;

    @lombok.NonNull
    AlgorithmSpec algorithmSpec;

    public static class Builder implements Validatable.Builder<TemporalInterpolationSpec> {
    }

    public static Builder builder() {
        return new Builder()
                .defaultPeriod(DEF_PERIOD)
                .observationPosition(DEF_OBSPOS)
                .estimationSpec(TsEstimationSpec.DEFAULT)
                .algorithmSpec(AlgorithmSpec.DEFAULT)
                .modelSpec(ModelSpec.DEFAULT);
    }

    @Override
    public TemporalInterpolationSpec validate() throws IllegalArgumentException {
        modelSpec.check();
        return this;
    }

    @Override
    public String display() {
        return switch (modelSpec.getResidualsModel()) {
            case Ar1 ->
                "Chow-Lin";
            case Rw ->
                "Fernandez";
            case RwAr1 ->
                "Litterman";
            case Wn ->
                "Ols";
            default ->
                "regression";
        };
    }
}
