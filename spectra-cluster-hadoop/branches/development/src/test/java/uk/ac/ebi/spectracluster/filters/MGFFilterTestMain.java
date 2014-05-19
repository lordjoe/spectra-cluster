package uk.ac.ebi.spectracluster.filters;

import com.lordjoe.filters.*;

import java.io.*;
import java.util.*;

/**
 * uk.ac.ebi.spectracluster.filters.MGFFilterTestMain
 *
 * @author Steve Lewis
 * @date 19/05/2014
 */
public class MGFFilterTestMain {
    public static final String MGF_FILTER =
              "<Filters>\n" +
             "<And>\n" +
              "<FileFilter extension=\"mgf\" />\n" +
              "<FileFilter minimumLength=\"10k\" />\n" +
              "<FileFilter maximumLength=\"100m\" />\n" +
              "</And>\n" +
              "</Filters>\n";
  
      public static void main(String[] args) {
          File startDir = new File(args[0]);
          //    File filterFile = new File(args[1]);
          final TypedFilterCollection filters = TypedFilterCollection.parse(MGF_FILTER);
  
          List<File> passing = FileFilters.applyFilters(startDir, filters);
          for (File file : passing) {
              
          }
  
      }
 
    
}
