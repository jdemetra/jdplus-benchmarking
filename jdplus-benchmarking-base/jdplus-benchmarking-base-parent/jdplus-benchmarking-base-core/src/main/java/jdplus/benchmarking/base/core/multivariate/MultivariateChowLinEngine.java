/*
 * Copyright 2025 National Bank of Belgium.
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
package jdplus.benchmarking.base.core.multivariate;

import internal.ssf.FastStateSmoother2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.benchmarking.base.api.benchmarking.multivariate.ContemporaneousConstraint;
import jdplus.benchmarking.base.api.benchmarking.multivariate.TemporalConstraint;
import jdplus.benchmarking.base.api.multivariate.ModelComposition;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinSpec;
import jdplus.benchmarking.base.api.univariate.AlgorithmSpec;
import jdplus.benchmarking.base.api.univariate.ModelSpec;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.TsEstimationSpec;
import jdplus.benchmarking.base.core.benchmarking.multivariate.Constraint;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLin;
import jdplus.benchmarking.base.core.ssf.MultivariateSsfChowLinWithoutRegressors;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationProcessor;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationResults;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.TsException;
import jdplus.toolkit.base.api.timeseries.TsPeriod;
import jdplus.toolkit.base.api.timeseries.TsUnit;
import jdplus.toolkit.base.api.util.WeightedItem;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.DataBlockStorage;
import jdplus.toolkit.base.core.data.transformation.Cumulator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.multivariate.SsfMatrix;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.ISsfData;

/**
 *
 * @author LEMASSO
 */
public class MultivariateChowLinEngine {   
    
    private final ArrayList<String> seriesNames = new ArrayList<>(); 
    private Map<String, TsData> y = new HashMap<>();
    private Map<String, TsData[]> indicators = new HashMap<>();
    private Map<String, TsData> constraints = new HashMap<>(); // contemporaneous constraints
    private HashMap<Integer, FastMatrix> Xo = new HashMap<>(); // original regressors
    // private HashMap<Integer, FastMatrix> X = new HashMap<>(); // rescaled regressors -> TO DO
    private HashMap<Integer, FastMatrix> Xc = new HashMap<>(); // cumulated regressors
    private final List<ContemporaneousConstraint> contemporaneousConstraints = new ArrayList<>();    
    private Constraint[] cs;
    private double[][] Zo; // original contemporaneous constraints
    private double[][] Zc; // cumulated contemporaneous constraints
    private TsDomain lDomain, hDomain;
    private TsUnit aggUnit;
    private int lfreq, hfreq, ratio;
    private double[] rhos;  
    private boolean[] isConstant, isTrend;
    
    public Map<String, TsData> process(Map<String, TsData> y, Map<String, TsData[]> indicators, Map<String, TsData> constraints, MultivariateChowLinSpec spec)  {
        
        // TO DO 
        // - Change output -> not only disaggregated series but an object of class MultivariateChowLinResults incl. stdev, model, etc.
        // - Rescaling
        // - Checks inputs
        // - Matching domains
        
        Map<String, TsData> rslts = new HashMap<>();
        
        for (String s : y.keySet()) {
            seriesNames.add(s);
        } 
        this.y = y;
        this.indicators = indicators;
        this.constraints = constraints;  
        this.rhos = spec.getRhos();       
        if(rhos.length != seriesNames.size()){
            throw new IllegalArgumentException("Mismatch between the number of series and the number of declared rho's");
        }
        this.isConstant = spec.getConstant();
        this.isTrend = spec.getTrend();
        
        buildDomains(spec.getDefaultPeriod());   
        buildRegressors();
             
        if (constraints == null || constraints.isEmpty()) {
            processIndependentConstraints(rslts);
            return rslts;
        } else{
            buildContemporaneousConstraints(spec.getContemporaneousConstraints());
        }
        
        compute(rslts);     
               
        return rslts;
    }
    
    private void buildDomains(int defFreq) {

        lDomain = TsDomain.DEFAULT_EMPTY;    
        for (int i = 0; i < seriesNames.size(); ++i) {           
            TsDomain d = y.get(seriesNames.get(i)).getDomain();
            if (lDomain.isEmpty()) {
                lDomain = d;
            } else if (!lDomain.getTsUnit().equals(d.getTsUnit())) {
                throw new TsException(TsException.INCOMPATIBLE_FREQ);
            } else {
                lDomain = lDomain.intersection(d);
                if (lDomain.isEmpty()) {
                    throw new TsException(TsException.DOMAIN_EMPTY);
                }
            }
        }    

        hDomain = TsDomain.DEFAULT_EMPTY;
        for (int i = 0; i < seriesNames.size(); ++i) {
            TsData[] xi = indicators.get(seriesNames.get(i));
            if (!(xi == null) && xi.length > 0){
                for (int k = 0; k < xi.length; ++k) {   
                    if (hDomain.isEmpty()) {
                        hDomain = xi[k].getDomain();
                    } else{
                        hDomain = hDomain.intersection(xi[k].getDomain());
                    }
                }
            }
        }
        if (!constraints.isEmpty()) {
            for (int i = 0; i < constraints.size(); ++i) {
                for (String s : constraints.keySet()) {
                    if (hDomain.isEmpty()) {
                        hDomain = constraints.get(s).getDomain();
                    } else{
                        hDomain = hDomain.intersection(constraints.get(s).getDomain());
                    }
                }
            }
        } 
        if (hDomain.isEmpty()) {
            int len = lDomain.getLength() * defFreq;
            TsPeriod start = TsPeriod.of(TsUnit.ofAnnualFrequency(defFreq), lDomain.start());
            hDomain = TsDomain.of(start, len);
        }

        this.lfreq = lDomain.getAnnualFrequency();
        this.hfreq = hDomain.getAnnualFrequency();    
        if (lfreq >= hfreq || hfreq % lfreq != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        this.ratio = hfreq / lfreq;
    }

    private void buildRegressors() {   

        for (int i = 0; i < seriesNames.size(); ++i) { 

            TsData[] ind = indicators.get(seriesNames.get(i));

            int nx = ind == null ? 0 : ind.length;
            int n = nx;
            if (isConstant[i]) {
                ++n;
            }
            if (isTrend[i]) {
                ++n;
            }

            if (n > 0) {
                FastMatrix xi = FastMatrix.make(hDomain.getLength(), n);
                FastMatrix xiC = FastMatrix.make(hDomain.getLength(), n);                   

                // xi
                DataBlockIterator xcols = xi.columnsIterator();
                if (isConstant[i]) {
                    xcols.next().set(1);
                }
                if (isTrend[i]) {
                    xcols.next().set(a -> a);
                }          
                if (nx > 0) {                        
                    for (int k = 0; k < ind.length; ++k) {
                        TsData xk = ind[k];   
                        if (xk == null){
                            throw new IllegalArgumentException("Indicator data not found: " + seriesNames.get(i));
                        }
                        if (aggUnit == null) {
                            aggUnit = xk.getTsUnit();
                        } else if (!aggUnit.equals(xk.getTsUnit())) {
                            throw new TsException(TsException.INCOMPATIBLE_FREQ);
                        } 
                        xcols.next().copy(ind[k].getValues());
                    }     
                }
                Xo.put(i, xi);

                // xiC
                xiC = xi.deepClone();
                Cumulator cumul = new Cumulator(ratio);
                DataBlockIterator cXc = xiC.columnsIterator();
                while (cXc.hasNext()) {
                    cumul.transform(cXc.next());
                }
                Xc.put(i, xiC);

            } else {
                Xo.put(i, FastMatrix.EMPTY);
                Xc.put(i, FastMatrix.EMPTY);
            }
        }
    }

    private void buildContemporaneousConstraints(List<ContemporaneousConstraint> lcnt) {

        for (ContemporaneousConstraint cnt : lcnt) {
            contemporaneousConstraints.add(cnt);

            if (seriesNames.contains(cnt.getConstraint())) {
                throw new IllegalArgumentException("Binding constraint cannot be used in definitions: " + cnt.getConstraint());
            }
            for (WeightedItem<String> wc : cnt.getComponents()) {
                if (cnt.getConstraint().contains(wc.getItem())) {
                    throw new IllegalArgumentException("Component definition cannot be a constraint: " + wc.getItem());
                }
            }   
        }

        // we create the actual constraints
        cs = new Constraint[contemporaneousConstraints.size()];
        int pos = 0;
        for (ContemporaneousConstraint desc : contemporaneousConstraints) {
            HashMap<Integer, Double> constraint = new HashMap<>();
            for (WeightedItem<String> cur : desc.getComponents()) {
                constraint.put(seriesNames.indexOf(cur.getItem()), cur.getWeight());
            }
            Constraint acnt = new Constraint(constraint);
            cs[pos++] = acnt;
        }

        // Zo, Zc
        Zo = new double[contemporaneousConstraints.size()][];
        Zc = new double[contemporaneousConstraints.size()][];
        for (int i = 0; i < contemporaneousConstraints.size(); ++i) {
            ContemporaneousConstraint desc = contemporaneousConstraints.get(i);
            double[] Zi;
            if (desc.getConstraint() != null) {
                TsData s = constraints.get(desc.getConstraint());
                Zi = s.getValues().toArray();   
            } else {
                Zi = new double[]{desc.getConstant()};
            }
            Zo[i] = Zi;

            double[] ZiC = Zi.clone();
            Cumulator cumul = new Cumulator(ratio);
            cumul.transform(DataBlock.of(ZiC));
            Zc[i] = ZiC;
        }
    }        

    private void processIndependentConstraints(Map<String, TsData> rslts) {
        
        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();
        
        TemporalDisaggregationSpec speci;      
        for (int i = 0; i < seriesNames.size(); ++i){
            if(rhos[i] == 1){  
                speci = TemporalDisaggregationSpec.FERNANDEZ.toBuilder()
                    .algorithmSpec(aspec)
                    .estimationSpec(espec)   
                    .build();
            }else{
                ModelSpec mspeci = ModelSpec.builder()
                    .parameter(Parameter.fixed(rhos[i]))
                    .constant(isConstant[i])
                    .trend(isTrend[i])
                    .build();
                
                speci = TemporalDisaggregationSpec.CHOWLIN.toBuilder()
                    .algorithmSpec(aspec)
                    .estimationSpec(espec)
                    .modelSpec(mspeci)
                    .build();
            }
            
            TsData yi = y.get(seriesNames.get(i));           
            TsData[] xi = indicators.get(seriesNames.get(i));
            
            TemporalDisaggregationResults r;
            if(xi.length == 0){
                r = TemporalDisaggregationProcessor.process(yi, 0, 0, speci);
            }else {
                r = TemporalDisaggregationProcessor.process(yi, xi, speci);
            }
                   
            if (r != null) {
                rslts.put(seriesNames.get(i), r.getDisaggregatedSeries());
            }
        } 
    }
    
    private void compute(Map<String, TsData> rslts) {

        int c = hfreq;
        int nvars = seriesNames.size(), ncnts = cs.length;  
        int len = hDomain.getLength();
        
        IMultivariateSsf ssf = MultivariateSsfChowLin.builder(nvars)
                .conversion(c)
                .rho(rhos)
                .xc(Xc)
                .constraints(cs)
                .build();

        // build the observations
        FastMatrix M = FastMatrix.make(len, nvars + ncnts);
        M.set(Double.NaN);

        // fill the matrix: first rows with temporal constraints, last rows 
        // with contemporeneous constraint(s) 
        for (int i = 0; i < nvars; ++i) {
            TsData a = y.get(seriesNames.get(i));
            DataBlock b = M.column(i).extract(c - 1, a.length(), c);
            b.copy(a.getValues());
        }
        for (int i = 0; i < ncnts; ++i) {
            DataBlock row = M.column(i + nvars);
            row.copyFrom(Zc[i], 0);
        }

        ISsf adapter = M2uAdapter.of(ssf);
        ISsfData data = M2uAdapter.of(new SsfMatrix(M));

        FastStateSmoother2 smoother = new FastStateSmoother2(adapter);
        DataBlockStorage states = smoother.process(data);
        
        int neq = nvars + ncnts;
        int nxc = 0; 
        for (int i = 0; i < seriesNames.size(); ++i) {
            double[] r = new double[len];
            int ip = 2 * i + nxc;
            int nx = Xo.get(i).getColumnsCount();
            DoubleSeq t = states.item(ip + 1);
            for (int j = 0; j < len; ++j) {
                r[j] += t.get(j * neq);
                if(nx > 0) {
                    for (int k = 0; k < nx; ++k) {
                        DoubleSeq bk = states.item(ip + 2 + k);
                        r[j] += bk.get(0) * Xo.get(i).get(j, k);
                    }
                }
            }
            nxc += nx;
            rslts.put(seriesNames.get(i), TsData.ofInternal(hDomain.getStartPeriod(), r));
        }           
    }
    
}
