package uk.ac.ebi.pride.spectracluster.cluster;

import java.util.EventListener;

/**
 * uk.ac.ebi.uk.ac.ebi.pride.spectracluster.cluster.SpectrumHolderListener
 * User: Steve
 * Date: 7/10/13
 */
public interface ISpectrumHolderListener extends EventListener {
    /**
     * handle update notification
     *
     * @param event
     */
    public void update(SpectrumHolderEvent event);

}
