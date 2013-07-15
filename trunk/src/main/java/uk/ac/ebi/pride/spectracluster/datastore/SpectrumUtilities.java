package uk.ac.ebi.pride.spectracluster.datastore;

import org.proteios.io.*;
import uk.ac.ebi.pride.spectracluster.spectrum.*;

import java.util.*;

/**
 * uk.ac.ebi.pride.spectracluster.datastore.SpectrumUtilities
 * functions for encoding a list of peaks as a single base64 string
 * User: Steve
 * Date: 7/15/13
 */
public class SpectrumUtilities {


    /**
     * encode a list of peaks as would be done by mzml
     * @param peaks !null list of peaks
     * @return  !null String
     */
    public static List<IPeak> dataStringToPeaks(String dataStr)
    {
        List<IPeak> holder = new ArrayList<IPeak>();
        double[] data = decodeDataString(dataStr );
        for (int i = 0; i < data.length; i += 2) {
            double mz = data[i];
            double intensity = data[i+ 1];
            IPeak peak = new  Peak((float)mz,(float)intensity);
            holder.add(peak);
        }

        return holder;
    }


    /**
     * encode a list of peaks as would be done by mzml
     * @param peaks !null list of peaks
     * @return  !null String
     */
    public static String peaksToDataString(List<IPeak> peaks)
    {
        double[] data = new double[ 2 * peaks.size()] ;
        for (int i = 0; i <  peaks.size(); i++) {
             IPeak pk = peaks.get(i);
            data[2 * i]  = pk.getMz();
            data[(2 * i) + 1]  = pk.getIntensity();

        }
        String ret = encode(data);
        return ret;
    }


    /**
     * decode base64 as doubles
     * @param pDataString
      * @return  !null data
     */
    protected static double[] decodeDataString(final String pDataString ) {
         return decodeDataString( pDataString,false) ;
    }

    /**
     * decode base64 as doubles
     * @param pDataString
     * @param is32Bit  if true encoded as 32 bit
     * @return  !null data
     */
    protected static double[] decodeDataString(final String pDataString,boolean is32Bit) {
        int len = pDataString.length();
        boolean isMultipleOf4 = (len % 4) == 0;
        boolean doublePrecision = !is32Bit;
        boolean bigEndian = false;
        List<Double> dataList = Base64Util.decode(doublePrecision, bigEndian, pDataString);
        int size = dataList.size();
        double[] realData = new double[size];
        for (int i = 0; i < size; i++) {
            realData[i] = dataList.get(i);
            //XTandemUtilities.outputLine("" + realData[i] + ",");
        }
        return realData;
    }


    /**
     * encode as base 64
     * @param data !null data
     * @return  base 64 string
     */
    protected static String encode(double[] data) {
        List<Double> dataList = new ArrayList<Double>();
        for (int q = 0; q < data.length; q++) {
            double v = data[q];
            dataList.add(v);
        }
        boolean doublePrecision = false;
        boolean bigEndian = false;
        return Base64Util.encode(doublePrecision, bigEndian, dataList);

    }


}