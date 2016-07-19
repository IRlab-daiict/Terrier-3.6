/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original is in 'ArrayOfFieldIterablePosting.java'
 *
 * The Original Code is Copyright (C) 2004-2014 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.structures.postings;

/** An instance of IterablePostings that works with passed arrays of ids and frequencies
 * for each field.
 * @author Craig Macdonald */
public class ArrayOfFieldIterablePosting 
	extends ArrayOfBasicIterablePosting
	implements FieldPosting
{

	int[][] tff;
	int[][] lf;
	
	public ArrayOfFieldIterablePosting(int[] _ids, int[] _freqs, int[] _lens,
			int[][] _tff, int[][] _lf) {
		super(_ids, _freqs, _lens);
		tff = _tff;
		lf = _lf;
	}

	@Override
	public int[] getFieldFrequencies() {
		return tff[indice];
	}

	@Override
	public int[] getFieldLengths() {
		return lf[indice];
	}

	@Override
	public void setFieldLengths(int[] newLengths) {
		throw new UnsupportedOperationException();
	}

}
