package jdplus.benchmarking.base.api.multivariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.Development;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Corentin Lemasson <corentin.lemasson@nbb.be>
 */
@lombok.Value
@lombok.Builder
@Development(status = Development.Status.Beta)
public class MultivariateChowLinResults implements GenericExplorable {

    /**
     * Regression variables
     */
    // Map<String, Variable[]> indicators;

    @lombok.NonNull
    Map<String, TsData> disaggregatedSeries;

    @lombok.NonNull
    Map<String, TsData> stdevDisaggregatedSeries;

    Map<String, TsData> regressionEffects;

    Map<String, FastMatrix> regressors;

    Map<String, List<String>> regressorsNames;

    Map<String, DoubleSeq> coefficients;

    Map<String, DoubleSeq> coefficientsVariance;

    @lombok.NonNull
    TsDomain disaggregationDomain;

    int disaggregationRatio;
}
