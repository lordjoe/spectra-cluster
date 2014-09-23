package uk.ac.ebi.pride.tools.cluster.utils;

import uk.ac.ebi.pride.archive.repo.assay.Assay;
import uk.ac.ebi.pride.archive.repo.assay.AssaySampleCvParam;
import uk.ac.ebi.pride.archive.repo.assay.instrument.Instrument;
import uk.ac.ebi.pride.archive.repo.assay.instrument.InstrumentModel;
import uk.ac.ebi.pride.archive.repo.assay.software.Software;
import uk.ac.ebi.pride.archive.repo.project.Project;
import uk.ac.ebi.pride.archive.repo.project.ProjectTag;
import uk.ac.ebi.pride.jmztab.model.Modification;
import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.ebi.pride.jmztab.model.SplitList;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ICluster;
import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.tools.cluster.model.AssaySummary;
import uk.ac.ebi.pride.tools.cluster.model.ClusterSummary;
import uk.ac.ebi.pride.tools.cluster.model.PSMSummary;
import uk.ac.ebi.pride.tools.cluster.model.SpectrumSummary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author Rui Wang
 * @version $Id$
 */
public final class SummaryFactory {

    public static AssaySummary summariseAssay(Project project, Assay assay) {
        AssaySummary assaySummary = new AssaySummary();

        assaySummary.setAccession(assay.getAccession());
        assaySummary.setProjectAccession(project.getAccession());
        assaySummary.setProjectTitle(project.getTitle());
        assaySummary.setAssayTitle(assay.getTitle());

        Collection<AssaySampleCvParam> samples = assay.getSamples();
        String species = "";
        String taxonomyIds = "";
        String disease = "";
        String tissue = "";
        int speciesCount = 0;

        for (AssaySampleCvParam sample : samples) {
            String cvLabel = sample.getCvLabel();
            String accession = sample.getAccession();
            String name = sample.getName();
            if (Constants.NEWT.equals(cvLabel)) {
                // species
                species += name + Constants.COMMA;
                taxonomyIds += accession + Constants.COMMA;
                speciesCount++;
            } else if (Constants.BRENDA.equals(cvLabel)) {
                // tissues
                tissue += name + Constants.COMMA;
            } else if (Constants.DISEASE.equals(cvLabel)) {
                // diseases
                disease += name + Constants.COMMA;
            }
        }

        // species
        if (species.length() > 1)
            assaySummary.setSpecies(species.substring(0, species.length() - 1));

        // multi-species count
        assaySummary.setMultiSpecies(speciesCount > 1);

        // species taxonomy id
        if (taxonomyIds.length() > 1)
            assaySummary.setTaxonomyId(taxonomyIds.substring(0, taxonomyIds.length() - 1));

        // disease
        if (disease.length() > 1)
            assaySummary.setDisease(disease.substring(0, disease.length() - 1));

        // tissue
        if (tissue.length() > 1)
            assaySummary.setTissue(tissue.substring(0, tissue.length() - 1));

        // search engine
        Collection<Software> softwares = assay.getSoftwares();
        if (softwares != null && softwares.size() > 0) {
            String searchEngine = "";
            for (Software software : softwares) {
                searchEngine += software.getName() + Constants.COMMA;
            }

            if (searchEngine.length() > 1)
                assaySummary.setSearchEngine(searchEngine.substring(0, searchEngine.length() - 1));
        }

        // instrument
        Collection<Instrument> instruments = assay.getInstruments();
        if (instruments != null && instruments.size() > 0) {
            String instrument = "";
            for (Instrument intru : instruments) {
                InstrumentModel model = intru.getModel();
                if (model != null) {
                    String value = model.getValue();
                    instrument += (value  == null ? model.getName() : value) + Constants.COMMA;
                }
            }

            if (instrument.length() > 1)
                assaySummary.setInstrument(instrument.substring(0, instrument.length() - 1));
        }

        // biomedical
        Collection<ProjectTag> projectTags = project.getProjectTags();
        if (projectTags != null && !projectTags.isEmpty()) {
            for (ProjectTag projectTag : projectTags) {
                if (projectTag.getTag().toLowerCase().contains("biomedical")) {
                    assaySummary.setBioMedical(true);
                }
            }
        }

        return assaySummary;
    }

    /**
     * Create a new summary on PSM
     *
     * NOTE: spectrum id is not set, this needs to be added before persisted into the database
     *
     * @param psm        PSM generated by mzTab reader
     * @param assayId    assay id is the primary key assigned at the data store level
     * @return
     */
    public static PSMSummary summarisePSM(PSM psm, String projectAccession,
                                          Long assayId, String assayAccession,
                                          int numOfMsRun) {
        PSMSummary psmSummary = new PSMSummary();

        psmSummary.setAssayId(assayId);

        // archive psm id
        String archivePSMId = buildArchivePSMId(projectAccession, assayAccession, psm);
        psmSummary.setArchivePSMId(archivePSMId);

        // peptide sequence
        psmSummary.setSequence(psm.getSequence());

        // modification
        SplitList<Modification> modifications = psm.getModifications();
        if (modifications != null && !modifications.isEmpty())
            psmSummary.setModifications(modifications.toString());

        // search engine
        String searchEngine = "";
        SplitList<Param> searchEngines = psm.getSearchEngine();
        if (searchEngines != null) {
            for (Param param : searchEngines) {
                searchEngine += param.getName() + Constants.COMMA;
            }
        }
        if (searchEngine .length() > 1)
            psmSummary.setSearchEngine(searchEngine.substring(0, searchEngine.length() - 1));

        //search engine scores
        String searchEngineScores = "";
        for (int i = 0; i < numOfMsRun; i++) {
            Double searchEngineScore = psm.getSearchEngineScore(i + 1);
            if (searchEngineScore != null)
                searchEngineScores += searchEngineScore + Constants.COMMA;
        }
        if (searchEngineScores.length() > 1)
            psmSummary.setSearchEngineScores(searchEngineScores.substring(0, searchEngineScores.length() - 1));

        // search database
        psmSummary.setSearchDatabase(psm.getDatabase());

        // protein accession
        psmSummary.setProteinAccession(psm.getAccession());

        // protein group
        //todo: protein group is at the protein level

        // protein name
        //todo: protein name are not given at the psm level

        // start position
        Integer start = psm.getStart();
        if (start != null)
            psmSummary.setStartPosition(start);

        // stop position
        Integer end = psm.getEnd();
        if (end != null)
            psmSummary.setStopPosition(end);

        // pre amino acid
        psmSummary.setPreAminoAcid(psm.getPre());

        // post amino acid
        psmSummary.setPostAminoAcid(psm.getPost());

        // delta mass
        Double calcMassToCharge = psm.getCalcMassToCharge();
        Double expMassToCharge = psm.getExpMassToCharge();
        if (calcMassToCharge != null && calcMassToCharge > 0 && expMassToCharge != null && expMassToCharge > 0) {
            psmSummary.setDeltaMZ((float)(calcMassToCharge - expMassToCharge));
        }

        // quantification label
        // todo: to be implemented, quantification information is at the peptide level rather than psm level

        return psmSummary;
    }

    private static String buildArchivePSMId(String projectAccession, String assayAccession, PSM psm) {
        return projectAccession + "_"
                + assayAccession + "_"
                + psm.getPSM_ID() + "_"
                + psm.getAccession() + "_"
                + PeptideUtils.cleanPeptideSequence(psm.getSequence());
    }


    public static SpectrumSummary summariseSpectrum(ISpectrum spectrum, Long assayId, boolean identified) {
        SpectrumSummary spectrumSummary = new SpectrumSummary();

        spectrumSummary.setAssayId(assayId);
        spectrumSummary.setReferenceId(spectrum.getId());
        spectrumSummary.setPrecursorMz(spectrum.getPrecursorMz());
        spectrumSummary.setPrecursorCharge(spectrum.getPrecursorCharge());
        spectrumSummary.setIdentified(identified);

        return spectrumSummary;
    }


    public static ClusterSummary summariseCluster(ICluster cluster) throws IOException {
        ClusterSummary clusterSummary = new ClusterSummary();

        clusterSummary.setAveragePrecursorMz(cluster.getAvPrecursorMz());

        float averagePrecursorCharge = calculateAveragePrecursorCharge(cluster);
        clusterSummary.setAveragePrecursorCharge(averagePrecursorCharge);

        List<Float> consensusMzValues = cluster.getConsensusMzValues();
        clusterSummary.setConsensusSpectrumMz(convertToByteArray(consensusMzValues));

        List<Float> consensusIntensValues = cluster.getConsensusIntensValues();
        clusterSummary.setConsensusSpectrumIntensity(convertToByteArray(consensusIntensValues));

        clusterSummary.setNumberOfSpectra(cluster.getSpecCount());

        clusterSummary.setMaxPeptideRatio(cluster.getMaxRatio());



        return clusterSummary;
    }

    private static float calculateAveragePrecursorCharge(ICluster cluster) {
        List<ISpectrumReference> spectrumReferences = cluster.getSpectrumReferences();
        int chargeSum = 0;
        for (ISpectrumReference spectrumReference : spectrumReferences) {
            int charge = spectrumReference.getCharge();
            if (charge <= 0)
                return 0;
            chargeSum += charge;
        }
        return chargeSum/spectrumReferences.size();
    }

    private static byte[] convertToByteArray(Collection<Float> content) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(content);
        return bos.toByteArray();
    }
}