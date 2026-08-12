/*
 * Copyright 2026 JDemetra+.
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
package jdplus.benchmarking.base.core.multivariate;

import java.util.Random;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.math.matrices.MatrixNorms;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Jean Palate
 */
public class CovarianceEstimatorTest {

    public CovarianceEstimatorTest() {
    }

    @Test
    public void testSomeMethod() {
        FastMatrix X = FastMatrix.make(300, 30);
        Random rnd = new Random(0);
        X.set((i, j) -> rnd.nextDouble());

        FastMatrix cov1 = CovarianceEstimator.sampleCovariance(X);
        FastMatrix cov2 = CovarianceEstimator.sampleCovariance2(X);
        cov1.sub(cov2);
        assertTrue(MatrixNorms.norm1(cov1) < 1e-9);
    }

    public static void main(String[] args) {

        FastMatrix X = FastMatrix.make(30, 10);
        Random rnd = new Random(0);
        X.set((i, j) -> rnd.nextDouble());
        int K = 100000;
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            CovarianceEstimator.sampleCovariance(X);
        }
        long t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
        t0 = System.currentTimeMillis();
        for (int i = 0; i < K; ++i) {
            CovarianceEstimator.sampleCovariance2(X);
        }

        t1 = System.currentTimeMillis();
        System.out.println(t1 - t0);
    }

}
