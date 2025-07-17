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
import jdplus.toolkit.base.core.data.DataBlockStorage;
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
    /**
     * Inputs
     */
    private final LinkedHashMap<String, TsData> inputs = new LinkedHashMap<>();
    /**
     * Map of the temporal constraints. The map constains pairs of 
     * (disaggregated series, aggregated series)
     */
    private final Map<String, String> temporalConstraints = new HashMap<>();
    /**
     * Map of the model compositions. The map constains pairs of 
     * (disaggregated series, indicators)
     */
    private final Map<String, List<String>> modelCompositions = new HashMap<>();
    /**
     * List of the contemporaneous constraints
     */
    private final List<ContemporaneousConstraint> contemporaneousConstraints = new ArrayList<>();   
    /**
     * List of exogeneous series (not disaggregated) used in the contemporaneous
     * constraints, which appear in the left-side of contemporaneous definitions
     */
    private final ArrayList<String> ccNames = new ArrayList<>();
    /**
     * Data of the series in the ccNames list. The length of each array is equal to
     * the length of hdomain
     */
    private double[][] ccData;
    /**
     * List of disaggregated series. Those are also the series used in the 
     * contemporaneous constraints on the right-side of definitions.
     */
    private final ArrayList<String> sNames = new ArrayList<>();
    private final HashMap<String, TsData> tcData = new HashMap<>();
    private final HashMap<String, TsData[]> indicData = new HashMap<>();
    private boolean isAnyIndic, isAnyCst;
    private Constraint[] cs;
    private double[] rhos;
    private boolean[] isCsts;
    private TsDomain lDomain, hDomain;
    private TsUnit aggUnit;
    private int lfreq, hfreq;
    
    public Map<String, TsData> process(Map<String, TsData> inputs, MultivariateChowLinSpec spec)  {
        
        // CURRENTLY WORKING: case without any indicator or constant...
        
        // TO DO 
        // - Change output -> not only disaggregated series but an object of class MultivariateChowLinResults
        
        Map<String, TsData> rslts = new HashMap<>();
        
        loadInfo(inputs, spec);
        
        buildIndicators();
        buildContemporaneousConstraints();
        buildTemporalConstraints();        
        
        buildDomains(spec.getDefaultPeriod());
             
        if (contemporaneousConstraints.isEmpty()) {
            processIndependentConstraints(rslts, spec.isFixedRhos());
            return rslts;
        }
        
        if (!isAnyIndic){
            lfreq = lDomain.getAnnualFrequency();
            hfreq = spec.getDefaultPeriod();
            if (lfreq >= hfreq) {
                return null;
            }
            
            if(!isAnyCst){
                computeWithoutIndicator(rslts);
            }
        }
                
        return rslts;
    }
    
    private void loadInfo(Map<String, TsData> data, MultivariateChowLinSpec spec) {
        // inputs
        inputs.putAll(data);
        
        // model composition
        for (ModelComposition desc : spec.getModelCompositions()) {
            addModelComposition(desc);
            sNames.add(desc.getSeries());
        }
        
        // temporal constraints
        for (TemporalConstraint desc : spec.getTemporalConstraints()) {
            addTemporalConstraint(desc);
        }
        
        // contemporaneous constraints
        for (ContemporaneousConstraint desc : spec.getContemporaneousConstraints()) {
            addContemporaneousConstraint(desc);
            ccNames.add(desc.getConstraint());
        }
        
        rhos = spec.getRhos();
        if(rhos.length != sNames.size()){
            throw new IllegalArgumentException("Mismatch between the number of series and the number of declared rho's");
        }
    }
    
    private void addModelComposition(ModelComposition mc) {
        
        for (int i = 0; i < mc.getIndicators().size(); ++i){
            String si = mc.getIndicators().get(i);
            if (si == null) {
                throw new IllegalArgumentException("Invalid model composition: " + mc.getSeries());
            }           
        }
        modelCompositions.put(mc.getSeries(), mc.getIndicators());
    }
        
    private void addTemporalConstraint(TemporalConstraint cnt) {
        TsData sagg = inputs.get(cnt.getAggregate());
        if (sagg == null) {
            throw new IllegalArgumentException("Invalid temporal constraint: " + cnt.getAggregate());
        }
        temporalConstraints.put(cnt.getDetail(), cnt.getAggregate());
    }   

    private void addContemporaneousConstraint(ContemporaneousConstraint cnt) {
        if (cnt.getConstraint() != null && !inputs.containsKey(cnt.getConstraint())) {
            throw new IllegalArgumentException("Invalid contemporaneous constraint: " + cnt.getConstraint());
        }
        if (cnt.hasWildCards()) {
            cnt = cnt.expand(inputs.keySet());
        } else {
            for (WeightedItem<String> ws : cnt.getComponents()) {
                if (!sNames.contains(ws.getItem())){
                        //inputs.containsKey(ws.getItem())) {
                    throw new IllegalArgumentException("Invalid contemporaneous constraint: " + ws.getItem());
                }
            }
        }
        contemporaneousConstraints.add(cnt);
    }
    
    private void buildIndicators() {   
        int n = sNames.size();
        isCsts = new boolean[n];
        aggUnit = null;
        isAnyIndic = false; 
        isAnyCst = false;
        
        for (int i = 0; i < n; ++i) {            
            String sName = sNames.get(i);             
            List<String> indicNames = modelCompositions.get(sName);
            
            int nx = 0;
            for (String indicName : indicNames){
                if(!indicName.equals("1")){
                    nx++;
                }
            }
            
            TsData[] sIndic = new TsData[nx];
            for(int k = 0, p = 0; k < indicNames.size(); ++k, ++p){
                String indicName = indicNames.get(k);               
                if (indicName.equals("1")){
                    if (rhos[i] != 1) {
                        isCsts[i] = true;
                    }
                    --p;
                    isAnyCst = true;
                } else{
                    TsData sk = inputs.get(indicName);   
                    if (sk == null){
                        throw new IllegalArgumentException("Indicator data not found: " + indicName);
                    }
                    if (aggUnit == null) {
                        aggUnit = sk.getTsUnit();
                    } else if (!aggUnit.equals(sk.getTsUnit())) {
                        throw new TsException(TsException.INCOMPATIBLE_FREQ);
                    }
                    sIndic[p] = sk;
                    isAnyIndic = true;
                }                      
            }
            indicData.put(sName, sIndic);
        }     
    }
    
    private void buildContemporaneousConstraints() {
        // first of all, we go through the constraints to get information on the used series
        for (ContemporaneousConstraint desc : contemporaneousConstraints) {
            if (sNames.contains(desc.getConstraint())) {
                throw new IllegalArgumentException("Binding constraint cannot be used in definitions: " + desc.getConstraint());
            }
            // TODO Deal with such cases. Use "extended names" and modify the current constraint
            if (!ccNames.contains(desc.getConstraint())) {
                ccNames.add(desc.getConstraint());
            }
            for (WeightedItem<String> wc : desc.getComponents()) {
                if (ccNames.contains(wc.getItem())) {
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
                constraint.put(sNames.indexOf(cur.getItem()), cur.getWeight());
            }
            Constraint acnt = new Constraint(constraint);
            cs[pos++] = acnt;
        }

        ccData = new double[contemporaneousConstraints.size()][];
        for (int i = 0; i < contemporaneousConstraints.size(); ++i) {
            ContemporaneousConstraint desc = contemporaneousConstraints.get(i);
            if (desc.getConstraint() != null) {
                TsData s = inputs.get(desc.getConstraint());
                ccData[i] = s.getValues().toArray();
            } else {
                ccData[i] = new double[]{desc.getConstant()};
            }
        }
    }
    
    private void buildTemporalConstraints() {
        aggUnit = null;
        for (int i = 0; i < sNames.size(); ++i) {
            String n = sNames.get(i);
            TsData s = inputs.get(temporalConstraints.get(sNames.get(i)));
            if (aggUnit == null) {
                aggUnit = s.getTsUnit();
            } else if (!aggUnit.equals(s.getTsUnit())) {
                throw new TsException(TsException.INCOMPATIBLE_FREQ);
            }
            tcData.put(n, s);
        }     
    }
    
    private void buildDomains(int defFreq) {
        
        lDomain = TsDomain.DEFAULT_EMPTY;
        for (int i = 0; i < sNames.size(); ++i) {           
            TsDomain d = inputs.get(temporalConstraints.get(sNames.get(i))).getDomain();
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
        if (isAnyIndic) {
            for (int i = 0; i < sNames.size(); ++i) {
                TsData[] indic = indicData.get(sNames.get(i));
                if (indic.length > 0){
                    for (int k = 0; k < indic.length; ++k) {   
                        if (hDomain.isEmpty()) {
                            hDomain = indic[k].getDomain();
                        } else{
                            hDomain = hDomain.intersection(indic[k].getDomain());
                        }
                    }
                }
            }
        } else{
            if (!contemporaneousConstraints.isEmpty()) {
                for (int i = 0; i < contemporaneousConstraints.size(); ++i) {
                    ContemporaneousConstraint desc = contemporaneousConstraints.get(i);
                    if (desc.getConstraint() != null) {
                        if (hDomain.isEmpty()) {
                            hDomain = inputs.get(desc.getConstraint()).getDomain();
                        } else{
                            hDomain = hDomain.intersection(inputs.get(desc.getConstraint()).getDomain());
                        }    
                    }
                }
            }             
            if (hDomain.isEmpty()) {
                int len = lDomain.getLength() * defFreq;
                TsPeriod start = TsPeriod.of(TsUnit.ofAnnualFrequency(defFreq), lDomain.start());
                hDomain = TsDomain.of(start, len);
            }   
        }   
    }
     
    private void processIndependentConstraints(Map<String, TsData> rslts, boolean fixedrhos) {
        
        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .fast(true)
                .rescale(true)
                .algorithm(SsfInitialization.SqrtDiffuse)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .estimationPrecision(1e-9)
                .build();
        
        TemporalDisaggregationSpec speci;      
        for (int i = 0; i < sNames.size(); ++i){
            if(rhos[i] == 1){  
                speci = TemporalDisaggregationSpec.FERNANDEZ.toBuilder()
                    .algorithmSpec(aspec)
                    .estimationSpec(espec)   
                    .build();
            }else{
                ModelSpec mspeci = ModelSpec.builder()
                    .parameter(fixedrhos ? Parameter.fixed(rhos[i]) : Parameter.initial(rhos[i]))
                    .constant(isCsts[i])
                    .build();
                
                speci = TemporalDisaggregationSpec.CHOWLIN.toBuilder()
                    .algorithmSpec(aspec)
                    .estimationSpec(espec)
                    .modelSpec(mspeci)
                    .build();
            }
            
            TsData yi = inputs.get(temporalConstraints.get(sNames.get(i)));            
            TsData[] xi = indicData.get(sNames.get(i));
            
            TemporalDisaggregationResults rslti;
            if(xi.length == 0){
                rslti = TemporalDisaggregationProcessor.process(yi, 0, 0, speci);
            }else {
                rslti = TemporalDisaggregationProcessor.process(yi, xi, speci);
            }
                   
            if (rslti != null) {
                rslts.put(sNames.get(i), rslti.getDisaggregatedSeries());
            }
        } 
    }
    
    private void computeWithoutIndicator(Map<String, TsData> rslts) {
        
        int c = hfreq;
        int nvars = sNames.size(), ncnts = cs.length;  
        int hfreq = hDomain.getAnnualFrequency(), lfreq = lDomain.getAnnualFrequency();
        if (lfreq >= hfreq || hfreq % lfreq != 0) {
            throw new TsException(TsException.INCOMPATIBLE_FREQ);
        }
        int len = hDomain.getLength();
        
        IMultivariateSsf ssf = MultivariateSsfChowLin.builder(nvars)
                .conversion(c)
                .rho(rhos)
                .constraints(cs)
                .build();
        
        // build the observations
        FastMatrix M = FastMatrix.make(len, nvars + ncnts);
        M.set(Double.NaN);

        // fill the matrix: first rows with temporal constraints, last rows 
        // with contemporeneous constraint(s) 
        for (int i = 0; i < nvars; ++i) {
            if (temporalConstraints.containsKey(sNames.get(i))) {
                TsData a = tcData.get(sNames.get(i));
                DataBlock b = M.column(i).extract(c - 1, a.length(), c);
                b.copy(a.getValues());
            }
        }
        for (int i = 0; i < ncnts; ++i) {
            DataBlock row = M.column(i + nvars);
            row.copyFrom(ccData[i], 0);
        }
        
        ISsf adapter = M2uAdapter.of(ssf);
        ISsfData data = M2uAdapter.of(new SsfMatrix(M));
        
        FastStateSmoother2 smoother = new FastStateSmoother2(adapter);
        DataBlockStorage states = smoother.process(data);
        
        int neq = nvars + ncnts;
        for (int i = 0; i < sNames.size(); ++i) {
            double[] y = new double[len];
            DoubleSeq t = states.item(2 * i + 1);
            for (int j = 0; j < len; ++j) {
                y[j] += t.get(j * neq);
            }
            rslts.put(sNames.get(i), TsData.ofInternal(hDomain.getStartPeriod(), y));
        }
    }
    
}
