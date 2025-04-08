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
package jdplus.benchmarking.base.core.univariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import nbbrd.design.Development;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
@Development(status = Development.Status.Alpha)
public class RawModelBasedDentonResults implements GenericExplorable{

    @lombok.NonNull
    DoubleSeq target;

    @lombok.NonNull
    DoubleSeq indicator;

    @lombok.NonNull
    DoubleSeq aggregatedBiRatios;

    @lombok.NonNull
    DoubleSeq disaggregatedSeries;
    
    @lombok.NonNull
    DoubleSeq stdevDisaggregatedSeries;
    
    @lombok.NonNull
    DoubleSeq biRatios;
    
    @lombok.NonNull
    DoubleSeq stdevBiRatios;
    
    LikelihoodStatistics likelihood;
    ResidualsDiagnostics residualsDiagnostics;
    DoubleSeq residuals;
    
}
