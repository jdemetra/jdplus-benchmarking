/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.benchmarking.base.api.univariate;

import jdplus.toolkit.base.api.timeseries.TimeSelector;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Getter
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public final class TsEstimationSpec {

    public static final double DEF_TRUNCATED = 0.0;
    public static final double DEF_EPS = 1e-6;

    public static final TsEstimationSpec DEFAULT = builder().build();

     @lombok.NonNull
    private TimeSelector estimationSpan;
    private Double truncatedParameter;
    private double estimationPrecision;

    public static Builder builder() {
        return new Builder()
                .estimationSpan(TimeSelector.all())
                .truncatedParameter(DEF_TRUNCATED)
                .estimationPrecision(DEF_EPS);
    }

}
