package jdplus.benchmarking.base.core.multivariate;

import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import nbbrd.design.Development;

@lombok.Value
@lombok.Builder
@Development(status = Development.Status.Beta)
public class ShrinkageResults {
    FastMatrix covariance;
    double lambda;
}
