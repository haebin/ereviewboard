package org.review_board.ereviewboard.ui.util;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * AutoCompleteField is a class which attempts to auto-complete a user's
 * keystrokes by activating a popup that filters a list of proposals according
 * to the content typed by the user.
 * 
 * @see ContentProposalAdapter
 * @see SimpleContentProposalProvider
 * 
 * @since 3.3
 */
public class RealTimeAutoCompleteField {

    private RealTimeContentProposalProvider proposalProvider;
    private ContentProposalAdapter adapter;

    /**
     * Construct an AutoComplete field on the specified control, whose
     * completions are characterized by the specified array of Strings.
     * 
     * @param control the control for which autocomplete is desired. May not be
     *            <code>null</code>.
     * @param controlContentAdapter the <code>IControlContentAdapter</code> used
     *            to obtain and update the control's contents. May not be
     *            <code>null</code>.
     * @param proposals the array of Strings representing valid content
     *            proposals for the field.
     */
    public RealTimeAutoCompleteField(final Control control,
            IControlContentAdapter controlContentAdapter, String[] proposals, final boolean isMulti) {
        proposalProvider = new RealTimeContentProposalProvider(proposals, isMulti);
        proposalProvider.setFiltering(true);
        adapter = new ContentProposalAdapter(control, controlContentAdapter, proposalProvider,
                null, null);
        adapter.setPropagateKeys(true);
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
        adapter.addContentProposalListener(new IContentProposalListener() {
            public void proposalAccepted(IContentProposal proposal) {
                String id = proposal.getContent().trim();
                int sepIdx = -1;
                if ((sepIdx = id.lastIndexOf(" ")) != -1)
                    id = proposal.getContent().substring(sepIdx + 1).trim();
                
                if (isMulti) {
                    String text = ((Text) control).getText();
                    int comIdx = text.lastIndexOf(",");
                    
                    if (comIdx != -1)
                        id = text.substring(0, comIdx + 1) + " " + id;

                    ((Text) control).setText(id + ", ");
                    ((Text) control).setSelection((id + ", ").length());
                } else {
                    ((Text) control).setText(id);
                }
            }
        });
    }

    /**
     * Set the Strings to be used as content proposals.
     * 
     * @param proposals the array of Strings to be used as proposals.
     */
    public void setProposals(String[] proposals) {
        proposalProvider.setProposals(proposals);
    }
}
