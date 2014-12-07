package uk.ac.ebi.pride.spectral_library_builder.util;

import java.util.Collections;
import java.util.Set;

/**
 * Created by jg on 05.12.14.
 */
public class PSM {
    private final String sequence;
    private final Set<PTM> ptms;
    private int count;

    public PSM(String sequence, Set<PTM> ptms, int count) {
        this.sequence = sequence;
        this.ptms = (ptms == null) ? Collections.EMPTY_SET : ptms;
        this.count = count;
    }

    public String getSequence() {
        return sequence;
    }

    public Set<PTM> getPtms() {
        return Collections.unmodifiableSet(ptms);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PSM psm = (PSM) o;

        if (ptms != null ? !ptms.equals(psm.ptms) : psm.ptms != null) return false;
        if (!sequence.equals(psm.sequence)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sequence.hashCode();
        result = 31 * result + (ptms != null ? ptms.hashCode() : 0);
        return result;
    }

    public static class PTM {
        private final int position;
        private final String accession;

        public PTM(int position, String accession) {
            this.position = position;
            this.accession = accession;
        }

        public int getPosition() {
            return position;
        }

        public String getAccession() {
            return accession;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PTM ptm = (PTM) o;

            if (position != ptm.position) return false;
            if (!accession.equals(ptm.accession)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = position;
            result = 31 * result + accession.hashCode();
            return result;
        }
    }
}
