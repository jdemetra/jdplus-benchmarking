/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.desktop.plugin.benchmarking.actions;

import jdplus.benchmarking.desktop.plugin.benchmarking.documents.CholetteDocumentManager;
import jdplus.benchmarking.desktop.plugin.benchmarking.documents.DentonDocumentManager;
import jdplus.benchmarking.desktop.plugin.disaggregation.documents.TemporalDisaggregationDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.nodes.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

@lombok.experimental.UtilityClass
public class Actions {

    @ActionID(category = "Edit",
            id = "demetra.desktop.workspace.nodes.RenameAction")
    @ActionReferences({
        @ActionReference(path = TemporalDisaggregationDocumentManager.ITEMPATH, position = 1050),
        @ActionReference(path = CholetteDocumentManager.ITEMPATH, position = 1050),
        @ActionReference(path = DentonDocumentManager.ITEMPATH, position = 1050)
    })
    public static RenameAction renameAction() {
        return new RenameAction();
    }

    @ActionID(category = "Edit",
            id = "demetra.desktop.workspace.nodes.CommentAction")
    @ActionReferences({
        @ActionReference(path = TemporalDisaggregationDocumentManager.ITEMPATH, position = 1150),
        @ActionReference(path = CholetteDocumentManager.ITEMPATH, position = 1150),
        @ActionReference(path = DentonDocumentManager.ITEMPATH, position = 1150)
    })
    public static CommentAction commentAction() {
        return new CommentAction();
    }

    @ActionID(category = "Edit",
            id = "demetra.desktop.workspace.nodes.DeleteAction")
    @ActionReferences({
        @ActionReference(path = TemporalDisaggregationDocumentManager.ITEMPATH, position = 1100),
        @ActionReference(path = CholetteDocumentManager.ITEMPATH, position = 1100),
        @ActionReference(path = DentonDocumentManager.ITEMPATH, position = 1100)
    })
    public static DeleteAction deleteAction() {
        return new DeleteAction();
    }

    @ActionID(category = "Edit",
            id = "demetra.desktop.workspace.nodes.NewAction")
    @ActionReferences({
        @ActionReference(path = TemporalDisaggregationDocumentManager.PATH, position = 1000),
        @ActionReference(path = CholetteDocumentManager.PATH, position = 1000),
        @ActionReference(path = DentonDocumentManager.PATH, position = 1000)
    })
    public static NewAction newAction() {
        return new NewAction();
    }

    @ActionID(
            category = "Edit",
            id = "demetra.desktop.disaggregation.actions.SortItems")
    @ActionReferences({
//        @ActionReference(path = "Shortcuts", name = "S"),
        @ActionReference(path = TemporalDisaggregationDocumentManager.PATH, position = 1200),
        @ActionReference(path = CholetteDocumentManager.PATH, position = 1200),
        @ActionReference(path = DentonDocumentManager.PATH, position = 1200)
    })
    public static SortAction sortAction() {
        return new SortAction();
    }

}
