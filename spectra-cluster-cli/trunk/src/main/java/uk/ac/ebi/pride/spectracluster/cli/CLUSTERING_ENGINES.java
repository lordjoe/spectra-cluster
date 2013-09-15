package uk.ac.ebi.pride.spectracluster.cli;

import uk.ac.ebi.pride.spectracluster.cluster.IClusteringEngine;
import uk.ac.ebi.pride.spectracluster.cluster.PublishedClusteringEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jg
 * Date: 9/15/13
 * Time: 11:44 AM
 * To change this template use File | Settings | File Templates.
 */
public enum CLUSTERING_ENGINES {
    PUBLISHED_CLUSTERING_ENGINE("PublishedClusteringEngine", "Reimplementation of the originally published clustering engine.");

    private final String name;
    private final String description;

    private CLUSTERING_ENGINES(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public IClusteringEngine getClusteringEngine() {
        switch(this) {
            case PUBLISHED_CLUSTERING_ENGINE:
                return new PublishedClusteringEngine();
            default:
                throw new IllegalStateException("Unknown clustering engine requested.");
        }
    }

    public static List<String> getAvailableEngines() {
        List<String> availableEngines = new ArrayList<String>(values().length);

        for (CLUSTERING_ENGINES e : values()) {
            availableEngines.add(e.getName());
        }

        return Collections.unmodifiableList(availableEngines);
    }

    public static CLUSTERING_ENGINES getClusteringEngineFromName(String name) {
        for (CLUSTERING_ENGINES e : values()) {
            if (e.getName().equals(name))
                return e;
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
