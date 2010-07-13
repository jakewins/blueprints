package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.BaseTest;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertexTest extends BaseTest
{

    public void testTransactionsForVerticesUsingInjectedGraph()
    {
        this.testTransactionsForVertices( Neo4jGraphTest.doInjectedNeo4jGraphTest() );
    }

    public void testTransactionsForVerticesUsingInternalGraph()
    {
        this.testTransactionsForVertices( Neo4jGraphTest.doNeo4jGraphTest() );
    }

    public void testBruteVertexTransactionsUsingInjectedGraph()
    {
        this.testBruteVertexTransactions( Neo4jGraphTest.doInjectedNeo4jGraphTest() );
    }

    public void testBruteVertexTransactionsUsingInternalGraph()
    {
        this.testBruteVertexTransactions( Neo4jGraphTest.doNeo4jGraphTest() );
    }

    //
    // INTERNALS
    //

    protected void testTransactionsForVertices( Neo4jGraph graph )
    {
        if ( null != graph )
        {
            graph.addVertex( null );
            graph.setAutoTransactions( false );
            try
            {
                graph.addVertex( null );
                assertTrue( false );
            }
            catch ( Exception e )
            {
                assertTrue( true );
            }
            graph.startTransaction();
            try
            {
                graph.addVertex( null );
                assertTrue( true );
            }
            catch ( Exception e )
            {
                assertTrue( false );
            }
            graph.stopTransaction( false );
            graph.startTransaction();
            assertEquals( count( graph.getVertices() ), 1 );
            graph.startTransaction();
            try
            {
                graph.addVertex( null );
                assertTrue( true );
            }
            catch ( Exception e )
            {
                assertTrue( false );
            }
            graph.stopTransaction( true );
            assertEquals( count( graph.getVertices() ), 2 );

        }
    }

    protected void testBruteVertexTransactions( Neo4jGraph graph )
    {
        if ( null != graph )
        {
            graph.setAutoTransactions( false );
            for ( int i = 0; i < 100; i++ )
            {
                graph.startTransaction();
                graph.addVertex( null );
                graph.stopTransaction( true );
            }
            graph.startTransaction();
            assertEquals( count( graph.getVertices() ), 100 );
            graph.stopTransaction( true );
            for ( int i = 0; i < 100; i++ )
            {
                graph.startTransaction();
                graph.addVertex( null );
                graph.stopTransaction( false );
            }
            graph.startTransaction();
            assertEquals( count( graph.getVertices() ), 100 );
            graph.stopTransaction( true );
        }
    }
}
