/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.benchmarking.desktop.plugin.disaggregation.actions;

import jdplus.benchmarking.desktop.plugin.disaggregation.documents.TemporalDisaggregationDocumentManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItem;
import jdplus.toolkit.desktop.plugin.workspace.nodes.WsNode;
import jdplus.toolkit.desktop.plugin.workspace.DocumentUIServices;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationDocument;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(category = "Tools",
id = "demetra.desktop.benchmarking.actions.OpenTemporalDisaggregationDoc")
@ActionRegistration(displayName = "#CTL_OpenTemporalDisaggregationDoc")
@ActionReferences({
    @ActionReference(path = TemporalDisaggregationDocumentManager.ITEMPATH, position = 1600, separatorBefore = 1590)
})
@NbBundle.Messages("CTL_OpenTemporalDisaggregationDoc=Open")
public class OpenTemporalDisaggregationDoc implements ActionListener {

    private final WsNode context;

    public OpenTemporalDisaggregationDoc(WsNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        WorkspaceItem<TemporalDisaggregationDocument> doc = context.getWorkspace().searchDocument(context.lookup(), TemporalDisaggregationDocument.class);
        DocumentUIServices ui = DocumentUIServices.forDocument(TemporalDisaggregationDocument.class);
        if (ui != null) {
            ui.showDocument(doc);
        }
    }
}
