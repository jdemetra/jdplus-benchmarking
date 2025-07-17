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
package jdplus.benchmarking.base.api.multivariate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import nbbrd.design.Development;

/**
 *
 * @author LEMASSO
 */
@Development(status = Development.Status.Beta)
@lombok.Value
public class ModelComposition {
    
    @lombok.NonNull
    private String series;
    private List<String> indicators;
    
    public static ModelComposition parse(String s) {
        try {
            Scanner scanner = new Scanner(s).useDelimiter("\\s*~\\s*");
            String n = scanner.next();
            String y = n;
            n = scanner.next();
            List<String> indic = new ArrayList<>();
            if (!parseIndicators(n, indic)) {
                return null;
            }
            return new ModelComposition(y, indic);
        } catch (Exception err) {
            return null;
        }
    }
    
    private static boolean parseIndicators(String str, List<String> indic) {           
        int pos = 0, ppos = 0;        
        while (pos < str.length()) {
            char c = str.charAt(pos);
            
            if (Character.isWhitespace(c)) {
                ++pos;
            } else {
                if (ppos >= 0) {
                    ppos = str.indexOf('+', pos + 1);
                }
                int npos;
                if (ppos < 0) {
                    npos = str.length();
                }else{
                    npos = ppos;
                }   
                if (c == '+') {
                    ++pos;
                }
                String cur = str.substring(pos, npos);
                String curT = cur.trim();
                if(!curT.equals("0")){
                    indic.add(curT); 
                }          
                pos = npos;
            }
        }
        
        return true;
    }    

   
//   @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        builder.append(aggregate).append("=sum(")
//                .append(detail).append(')');
//        return builder.toString();
//    }
}
