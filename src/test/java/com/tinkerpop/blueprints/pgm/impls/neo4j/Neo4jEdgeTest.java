package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.BaseTest;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jEdgeTest extends BaseTest
{

    public void testTransactionsForEdgesUsingInjectedGraph()
    {
        this.testTransactionsForEdges( Neo4jGraphTest.doInjectedNeo4jGraphTest() );
    }

    public void testTransactionsForEdgesUsingInternalGraph()
    {
        this.testTransactionsForEdges( Neo4jGraphTest.doNeo4jGraphTest() );
    }

    public void testBruteEdgeTransactionsUsingInjectedGraph()
    {
        this.testBruteEdgeTransactions( Neo4jGraphTest.doInjectedNeo4jGraphTest() );
    }

    public void testBruteEdgeTransactionsUsingInternalGraph()
    {
        this.testBruteEdgeTransactions( Neo4jGraphTest.doNeo4jGraphTest() );
    }

    //
    // INTERNALS
    //

    protected void testBruteEdgeTransactions( Neo4jGraph graph )
    {
        if ( null != graph )
        {
            graph.setAutoTransactions( false );
            for ( int i = 0; i < 100; i++ )
            {
                graph.startTransaction();
                Vertex v = graph.addVertex( null );
                Vertex u = graph.addVertex( null );
                graph.addEdge( null, v, u, "test" );
                graph.stopTransaction( true );
            }
            graph.startTransaction();
            assertEquals( count( graph.getVertices() ), 200 );
            assertEquals( count( graph.getEdges() ), 100 );
            graph.stopTransaction( true );
            for ( int i = 0; i < 100; i++ )
            {
                graph.startTransaction();
                Vertex v = graph.addVertex( null );
                Vertex u = graph.addVertex( null );
                graph.addEdge( null, v, u, "test" );
                graph.stopTransaction( false );
            }
            graph.startTransaction();
            assertEquals( count( graph.getVertices() ), 200 );
            assertEquals( count( graph.getEdges() ), 100 );
            graph.stopTransaction( true );
        }
    }

    protected void testTransactionsForEdges( Neo4jGraph graph )
    {

        if ( null != graph )
        {
            Vertex v = graph.addVertex( null );
            Vertex u = graph.addVertex( null );
            graph.setAutoTransactions( false );
            try
            {
                graph.addEdge( null, v, u, "test" );
                assertTrue( false );
            }
            catch ( Exception e )
            {
                assertTrue( true );
            }
            graph.startTransaction();
            try
            {
                graph.addEdge( null, v, u, "test" );
                assertTrue( true );
            }
            catch ( Exception e )
            {
                assertTrue( false );
            }
            graph.stopTransaction( false );
            graph.startTransaction();
            assertEquals( count( graph.getVertices() ), 2 );
            assertEquals( count( graph.getEdges() ), 0 );
            try
            {
                graph.addEdge( null, u, v, "test" );
                assertTrue( true );
            }
            catch ( Exception e )
            {
                assertTrue( false );
            }
            graph.stopTransaction( true );
            graph.startTransaction();
            assertEquals( count( graph.getVertices() ), 2 );
            assertEquals( count( graph.getEdges() ), 1 );
            graph.stopTransaction( true );

        }
    }
}
