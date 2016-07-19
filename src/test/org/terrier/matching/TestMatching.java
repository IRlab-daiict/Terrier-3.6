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
 * The Original Code is TestMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2014 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gnu.trove.TIntHashSet;

import org.junit.Before;
import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.models.DLH13;
import org.terrier.querying.Request;
import org.terrier.querying.parser.Query;
import org.terrier.querying.parser.QueryParser;
import org.terrier.querying.parser.QueryParserException;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.postings.FieldIterablePosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.terms.BaseTermPipelineAccessor;
import org.terrier.terms.TermPipelineAccessor;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.TagSet;

public abstract class TestMatching extends ApplicationSetupBasedTest {

	public static class TestTAATFullMatching extends TestMatching
	{
		@Override
		protected Matching makeMatching(Index i)
		{
			return new org.terrier.matching.taat.Full(i);
		}
	}
	
	public static class TestDAATFullMatching extends TestMatching
	{
		@Override
		protected Matching makeMatching(Index i)
		{
			return new org.terrier.matching.daat.Full(i);
		}
	}
	
	
	public static class TestTAATFullNoPLMMatching extends TestMatching
	{
		@Override
		protected Matching makeMatching(Index i)
		{
			return new org.terrier.matching.taat.FullNoPLM(i);
		}
		
		@Override
		public void testThreeDocumentsSynonymIndexMatching() throws Exception {}
	}
	
	public static class TestDAATFullNoPLMMatching extends TestMatching
	{
		@Override
		protected Matching makeMatching(Index i)
		{
			return new org.terrier.matching.daat.FullNoPLM(i);
		}

		@Override
		public void testThreeDocumentsSynonymIndexMatching() throws Exception {}
	}
	
	
	@Before public void setIndexerProperties()
	{
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "");
		ApplicationSetup.setProperty("termpipelines", "");
	}
	
	protected abstract Matching makeMatching(Index i);

	@Test public void testSingleDocumentIndexMatching() throws Exception
	{
		_testSingleDocumentIndexMatching();
	}
	
	ResultSet _testSingleDocumentIndexMatching() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1"}, 
				new String[]{"The quick brown fox jumps over the lazy dog"});
		System.err.println("testSingleDocumentIndexMatching: " + index.toString());
		assertNotNull(index);
		assertEquals(1, index.getCollectionStatistics().getNumberOfDocuments());
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setTermProperty("quick", 1);
		mqt.setDefaultTermWeightingModel(new DLH13());
		ResultSet rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(1, rs.getResultSize());
		assertEquals(0, rs.getDocids()[0]);
		assertTrue(rs.getScores()[0] > 0);
		return rs;
	}
	
	@Test public void testTwoDocumentsIndexMatching() throws Exception
	{
		_testTwoDocumentsIndexMatching();
	}
	
	ResultSet _testTwoDocumentsIndexMatching() throws Exception {
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1", "doc2"}, 
				new String[]{
						"The quick brown fox jumps over the lazy dog",
						"how much is that dog in the window"});
		System.err.println("testTwoDocumentsIndexMatching: " + index.toString());
		assertNotNull(index);
		assertEquals(2, index.getCollectionStatistics().getNumberOfDocuments());
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		MatchingQueryTerms mqt;
		ResultSet rs;
		
		mqt = new MatchingQueryTerms();
		mqt.setTermProperty("quick", 1);
		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(1, rs.getResultSize());
		assertEquals(0, rs.getDocids()[0]);
		assertTrue(rs.getScores()[0] > 0);
		
		mqt = new MatchingQueryTerms();
		mqt.setTermProperty("dog", 1);
		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query2", mqt);
		assertNotNull(rs);
		assertEquals(2, rs.getResultSize());		
		assertTrue(rs.getScores()[0] > 0);
		assertTrue(rs.getScores()[1] > 0);
		return rs;
	}
	
	@Test public void testFourDocumentsFieldIndexMatching() throws Exception
	{
		_testFourDocumentsFieldIndexMatching();
	}
	
	ResultSet _testFourDocumentsFieldIndexMatching() throws Exception {
		
		ApplicationSetup.setProperty("FieldTags.process", "TITLE,BODY");
		ApplicationSetup.setProperty("TrecDocTags.process", "DOCNO,TITLE,BODY");
		
		Index index = IndexTestUtils.makeIndexFields(
				new String[]{"doc1", "doc2", "doc3","doc4"}, 
				new String[]{
						"<DOCNO>1</DOCNO> <TITLE> Simple fox example 1</TITLE> <BODY> The quick brown fox jumps over the lazy dog </BODY>",
						"<DOCNO>2</DOCNO> <TITLE> Simple dog example 1 </TITLE> <BODY> how much is that dog in the window </BODY>",
						"<DOCNO>3</DOCNO> <TITLE> Simple dog example 2 </TITLE> <BODY> For example, what type of terrier is it? </BODY>",
						"<DOCNO>4</DOCNO> <TITLE> Copyright Statement </TITLE> <BODY> Terrier.org </BODY>"});
		System.err.println("testTwoDocumentsFieldIndexMatching: " + index.toString());
		assertNotNull(index);
		assertEquals(4, index.getCollectionStatistics().getNumberOfDocuments());
		
		// Check the document contents
		//for (int i =0; i<index.getCollectionStatistics().getNumberOfUniqueTerms(); i++) {
		//	System.err.println(index.getLexicon().getIthLexiconEntry(i).getKey());
		//}
		
		//assertEquals(1,index.getLexicon().getLexiconEntry("1").getFrequency());
		/*FieldIterablePosting termPosting = (FieldIterablePosting)index.getInvertedIndex().getPostings(index.getLexicon().getLexiconEntry("simple"));
		while (!termPosting.endOfPostings()) {
			int docid = termPosting.next();
			System.err.print(" "+termPosting.getId()+" "+termPosting.getFrequency()+" "+termPosting.getDocumentLength()+" [");
			for (int f : termPosting.getFieldFrequencies()) System.err.print(f+" ");
			System.err.print("] [");
			for (int f : termPosting.getFieldLengths()) System.err.print(f+" ");
			System.err.println("]");
			
		}*/
		
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		MatchingQueryTerms mqt;
		ResultSet rs;
		
		String query1 = "Fox";
		
		Request q = new Request();
		q.setQueryID("query1");
		q.setIndex(index);
		try{
			QueryParser.parseQuery(query1, q);	
		} catch (QueryParserException qpe) {
			System.err.println(qpe);
		}
		q.setOriginalQuery(query1);
		Query query = q.getQuery();
		
		String[] pipes = ApplicationSetup.getProperty(
				"termpipelines", "Stopwords,PorterStemmer").trim()
				.split("\\s*,\\s*");
		TermPipelineAccessor tpa = new BaseTermPipelineAccessor(pipes);
		
		
		synchronized(this) {
			assertEquals(true,query.applyTermPipeline(tpa));
			tpa.resetPipeline();
		}
		
		mqt = new MatchingQueryTerms(q.getOriginalQuery(), q);
		
		query.obtainQueryTerms(mqt);
		q.setMatchingQueryTerms(mqt);
		
		//mqt = new MatchingQueryTerms();
		//mqt.setTermProperty("TITLE:quick", 1);
		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(1, rs.getResultSize());
		assertEquals(0, rs.getDocids()[0]);
		assertTrue(rs.getScores()[0] > 0);
		
		double foxScoreAll = rs.getScores()[0];
		
		String query2 = "BODY:Fox";
		
		q = new Request();
		q.setQueryID("query2");
		q.setIndex(index);
		try{
			QueryParser.parseQuery(query2, q);	
		} catch (QueryParserException qpe) {
			System.err.println(qpe);
		}
		q.setOriginalQuery(query2);
		query = q.getQuery();
		mqt = new MatchingQueryTerms(q.getOriginalQuery(), q);
		
		query.obtainQueryTerms(mqt);
		q.setMatchingQueryTerms(mqt);

		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query2", mqt);
		assertNotNull(rs);
		assertEquals(1, rs.getResultSize());
		assertEquals(0, rs.getDocids()[0]);
		assertTrue(rs.getScores()[0] > 0);
		
		double foxScoreBody = rs.getScores()[0];
		
		assertTrue(foxScoreBody==foxScoreAll); // Intuitively, foxScoreBody should be less than foxScoreAll, but Terrier does not currently consider fields when scoring
		
		
		String query3 = "BODY:example";
		
		q = new Request();
		q.setQueryID("query3");
		q.setIndex(index);
		try{
			QueryParser.parseQuery(query3, q);	
		} catch (QueryParserException qpe) {
			System.err.println(qpe);
		}
		q.setOriginalQuery(query3);
		query = q.getQuery();
		mqt = new MatchingQueryTerms(q.getOriginalQuery(), q);
		
		query.obtainQueryTerms(mqt);
		q.setMatchingQueryTerms(mqt);

		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query3", mqt);
		assertNotNull(rs);
		assertEquals(3, rs.getResultSize());

		assertTrue(rs.getScores()[0] > 0);
		assertEquals(Double.NEGATIVE_INFINITY,rs.getScores()[1],0.0);
		assertEquals(Double.NEGATIVE_INFINITY,rs.getScores()[2],0.0);
		
		String query4 = "TITLE:(example dog)";
		
		q = new Request();
		q.setQueryID("query4");
		q.setIndex(index);
		try{
			QueryParser.parseQuery(query4, q);	
		} catch (QueryParserException qpe) {
			System.err.println(qpe);
		}
		q.setOriginalQuery(query4);
		query = q.getQuery();
		System.err.println(query.parseTree());
		mqt = new MatchingQueryTerms(q.getOriginalQuery(), q);
		
		query.obtainQueryTerms(mqt);
		q.setMatchingQueryTerms(mqt);

		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query4", mqt);
		assertNotNull(rs);
		assertEquals(3, rs.getResultSize());
		
		assertTrue(rs.getScores()[0] > 0);
		assertTrue(rs.getScores()[1] > 0);
		assertEquals(Double.NEGATIVE_INFINITY,rs.getScores()[2],0.0);
		
		return rs;
	}
	
	@Test public void testThreeDocumentsSynonymIndexMatching() throws Exception
	{
		_testThreeDocumentsSynonymIndexMatching();
	}
	
	ResultSet _testThreeDocumentsSynonymIndexMatching() throws Exception {
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1", "doc2", "doc3"}, 
				new String[]{
						"The quick brown fox jumps over the lazy dog",
						"how much is that dog in the window",
						"the one with the waggily tail"});
		System.err.println("testThreeDocumentsSynonymIndexMatching: " + index.toString());
		assertNotNull(index);
		assertEquals(3, index.getCollectionStatistics().getNumberOfDocuments());
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		MatchingQueryTerms mqt;
		ResultSet rs;
		
		mqt = new MatchingQueryTerms();
		mqt.setTermProperty("quick|waggily");
		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(2, rs.getResultSize());
		TIntHashSet docids = new TIntHashSet(rs.getDocids());
		System.err.println("" + rs.getDocids()[0] + " "+ rs.getScores()[0]);
		System.err.println("" + rs.getDocids()[1] + " "+ rs.getScores()[1]);
		assertTrue(docids.contains(0));
		assertTrue(docids.contains(2));
		assertEquals(2, rs.getDocids()[0]);
		assertEquals(0, rs.getDocids()[1]);
		assertTrue(rs.getScores()[0] > 0);
		assertTrue(rs.getScores()[1] > 0);
		return rs;
		
	}
	
	
	
	@Test public void testMatchingNonStatisticsOverwrite() throws Exception
	{
		_testMatchingNonStatisticsOverwrite();
	}
	
	ResultSet _testMatchingNonStatisticsOverwrite() throws Exception {
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1"}, 
				new String[]{"The quick brown fox jumps over the lazy dog"});
		assertNotNull(index);
		System.err.println("testMatchingNonStatisticsOverwrite: " + index.toString());
		assertEquals(1, index.getCollectionStatistics().getNumberOfDocuments());
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setDefaultTermWeightingModel(new DLH13());
		LexiconEntry le = index.getLexicon().getLexiconEntry("quick");
		assertNotNull(le);
		le.setStatistics(1, 40);
		mqt.setTermProperty("quick", le);
		ResultSet rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(1, rs.getResultSize());
		assertEquals(0, rs.getDocids()[0]);
		
		//check that statistics havent been overwritten
		assertEquals(40, mqt.getStatistics("quick").getFrequency());
		
		return rs;
	}

	@Test public void testTwoDocumentsTwoTerms() throws Exception
	{
		_testTwoDocumentsTwoTerms();
	}
	
	ResultSet _testTwoDocumentsTwoTerms() throws Exception {
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1", "doc2"}, 
				new String[]{
						"The quick brown fox jumps over the lazy dog",
						"how much is that dog in the window"});
		System.err.println("testThreeDocumentsSynonymIndexMatching: " + index.toString());
		assertNotNull(index);
		assertEquals(2, index.getCollectionStatistics().getNumberOfDocuments());
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		MatchingQueryTerms mqt;
		ResultSet rs;
		
		mqt = new MatchingQueryTerms();
		mqt.setTermProperty("dog");
		mqt.setTermProperty("window");
		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(2, rs.getResultSize());
		TIntHashSet docids = new TIntHashSet(rs.getDocids());
		System.err.println("" + rs.getDocids()[0] + " "+ rs.getScores()[0]);
		System.err.println("" + rs.getDocids()[1] + " "+ rs.getScores()[1]);
		assertTrue(docids.contains(0));
		assertTrue(docids.contains(1));
		assertEquals(1, rs.getDocids()[0]);
		assertEquals(0, rs.getDocids()[1]);
		assertTrue(rs.getScores()[0] > 0);
		assertTrue(rs.getScores()[1] > 0);
		return rs;
		
	}

	
}
