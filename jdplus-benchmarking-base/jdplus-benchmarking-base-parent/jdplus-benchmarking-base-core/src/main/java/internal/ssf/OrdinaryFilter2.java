/*
 * Copyright 2013-2014 National Bank copyOf Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package internal.ssf;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.math.matrices.MatrixNorms;
import jdplus.toolkit.base.core.ssf.ISsfDynamics;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.SsfException;
import jdplus.toolkit.base.core.ssf.State;
import jdplus.toolkit.base.core.ssf.StateInfo;
import jdplus.toolkit.base.core.ssf.UpdateInformation;
import jdplus.toolkit.base.core.ssf.univariate.IFilteringResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;
import jdplus.toolkit.base.core.ssf.univariate.ISsfError;
import jdplus.toolkit.base.core.ssf.univariate.OrdinaryFilter;
import jdplus.toolkit.base.core.stats.samples.Population;
import jdplus.toolkit.base.core.stats.samples.Sample;

/**
 * Ordinary Kalman filter for univariate time series
 *
 * @author Jean Palate
 */
public class OrdinaryFilter2 {
    
    private static final double ZERO=1e-6, ZERO2=ZERO*ZERO;

    private double scale(DoubleSeq data) {
        Sample sample=Sample.build(data, true, Population.UNKNOWN);
        return sample.standardDeviation();
    }

    private final OrdinaryFilter.Initializer initializer;
    private State state;
    private UpdateInformation updinfo;
    private ISsfLoading loading;
    private ISsfError error;
    private ISsfDynamics dynamics;
    private boolean missing;
    
    private double vscale, yscale;

    /**
     *
     * @param initializer
     */
    public OrdinaryFilter2(OrdinaryFilter.Initializer initializer) {
        this.initializer = initializer;
    }

    public OrdinaryFilter2() {
        this.initializer = null;
    }

    /**
     * Computes the update information:
     *
     * @param t
     * @param data
     * @return
     */
    protected boolean error(int t, ISsfData data) {
        missing = data.isMissing(t);
        if (missing) {
            // pe_ = null;
            updinfo.setMissing();
            return false;
        } else {
            // K = PZ'/f
            // computes PZ' (=ZP)'  in M.  
            DataBlock M = updinfo.M();
            loading.ZM(t, state.P(), M);
            // compute ZPZ'
            double v = loading.ZX(t, M);
            // check that ZPZ' is non negative (P semi-definite positive)
            // Theoretically, that should never happen, by construction. 
            // But for numerical reasons, it could appears. Difficult to find a meaningful 
            // limit. The next statement tries to correct the problem
//            if (v < -Constants.getEpsilon()) {
//                throw new SsfException();
//            }
            if (v < vscale*ZERO2) {
                v = 0;
            }
            if (error != null) {
                v += error.at(t);
            }
            updinfo.setVariance(v);
            double y = data.get(t);
            double e = y - loading.ZX(t, state.a());
            if (v == 0) {
                if (Math.abs(e) < yscale*ZERO) {
                    e = 0;
                } else {
                    throw new SsfException(SsfException.INCONSISTENT);
                }
            }
            updinfo.set(e, data.isConstraint(t));
            return true;
        }
    }

    /**
     * Retrieves the final state (which is a(N|N-1))
     *
     * @return
     */
    public State getFinalState() {
        return state;
    }

    private int initialize(ISsf ssf, ISsfData data) {
        loading = ssf.loading();
        error = ssf.measurementError();
        dynamics = ssf.dynamics();
        updinfo = new UpdateInformation(ssf.getStateDim());
        if (initializer == null) {
            state = State.of(ssf);
            return state == null ? -1 : 0;
        } else {
            state = new State(ssf.getStateDim());
            return initializer.initializeFilter(state, ssf, data);
        }
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data, final IFilteringResults rslts) {
        // intialize the state with a(0|-1)
        int t = initialize(ssf, data);
        if (t < 0) {
            return false;
        }
        int end = data.length();
        vscale=MatrixNorms.frobeniusNorm(state.P());
        yscale=scale(data);
        while (t < end) {
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Forecast);
            }
            if (error(t, data)) {
                if (rslts != null) {
                    rslts.save(t, updinfo);
                }
                state.update(updinfo);
            } else if (rslts != null) {
                rslts.save(t, updinfo);
            }
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Concurrent);
            }
            state.next(t++, dynamics);
        }
        return true;
    }

}
