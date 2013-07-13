/*
 * Copyright 2013 European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.pride.spectracluster.consensus;

import uk.ac.ebi.pride.spectracluster.cluster.ISpectrumHolder;
import uk.ac.ebi.pride.spectracluster.spectrum.IPeaksSpectrum;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrumQuality;

/**
 * IConsensusSpectrum is the default interface for objects used to
 * build consensus spectra more efficiently. The consensus spectrum
 * is made available just like a normal spectrum. It is generated through
 * adding and / or removing spectra from the consensus spectrum.
 * @author jg
 */
public interface IConsensusSpectrum extends IPeaksSpectrum, ISpectrumQuality, ISpectrumHolder {

}