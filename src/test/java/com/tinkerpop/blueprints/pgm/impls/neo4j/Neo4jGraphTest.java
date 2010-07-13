package com.tinkerpop.blueprints.pgm.impls.neo4j;

import java.io.File;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneIndexService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.tinkerpop.blueprints.pgm.EdgeTestSuite;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.GraphTestSuite;
import com.tinkerpop.blueprints.pgm.IndexTestSuite;
import com.tinkerpop.blueprints.pgm.ModelTestSuite;
import com.tinkerpop.blueprints.pgm.SuiteConfiguration;
import com.tinkerpop.blueprints.pgm.VertexTestSuite;
import com.tinkerpop.blueprints.pgm.parser.GraphMLReaderTestSuite;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jGraphTest extends TestCase
{

    private static final SuiteConfiguration config = new SuiteConfiguration();

    static
    {
        config.allowsDuplicateEdges = true;
        config.allowsSelfLoops = false;
        config.requiresRDFIds = false;
        config.isRDFModel = false;
        config.supportsVertexIteration = true;
        config.supportsEdgeIteration = true;
        config.supportsVertexIndex = true;
        config.supportsEdgeIndex = false;
        config.ignoresSuppliedIds = true;
    }

    public void testVertexSuite() throws Exception
    {
        doSuiteTest( new VertexTestSuite( config ) );
    }

    public void testEdgeSuite() throws Exception
    {
        doSuiteTest( new EdgeTestSuite( config ) );
    }

    public void testGraphSuite() throws Exception
    {
        doSuiteTest( new GraphTestSuite( config ) );
    }

    public void testIndexSuite() throws Exception
    {
        doSuiteTest( new IndexTestSuite( config ) );
    }

    public void testGraphMLReaderSuite() throws Exception
    {
        doSuiteTest( new GraphMLReaderTestSuite( config ) );
    }

    private void doSuiteTest( final ModelTestSuite suite ) throws Exception
    {
        String doTest = System.getProperty( "testNeo4j" );
        if ( doTest == null || doTest.equals( "true" ) )
        {
            String directory = System.getProperty( "neo4jDirectory" );
            if ( directory == null ) directory = "/tmp/blueprints_test";
            for ( Method method : suite.getClass().getDeclaredMethods() )
            {
                if ( method.getName().startsWith( "test" ) )
                {
                    Graph graph = new Neo4jGraph( directory );
                    graph.clear();
                    System.out.println( "Testing " + method.getName() + "..." );
                    method.invoke( suite, graph );
                    graph.shutdown();
                    deleteGraphDirectory( new File( directory ) );
                }
            }
        }
    }

    public void testLongIdConversions()
    {
        String id1 = "100"; // good 100
        String id2 = "100.0"; // good 100
        String id3 = "100.1"; // good 100
        String id4 = "one"; // bad

        try
        {
            Double.valueOf( id1 ).longValue();
            assertTrue( true );
        }
        catch ( NumberFormatException e )
        {
            assertFalse( true );
        }
        try
        {
            Double.valueOf( id2 ).longValue();
            assertTrue( true );
        }
        catch ( NumberFormatException e )
        {
            assertFalse( true );
        }
        try
        {
            Double.valueOf( id3 ).longValue();
            assertTrue( true );
        }
        catch ( NumberFormatException e )
        {
            assertFalse( true );
        }
        try
        {
            Double.valueOf( id4 ).longValue();
            assertTrue( false );
        }
        catch ( NumberFormatException e )
        {
            assertFalse( false );
        }
    }

    protected static Neo4jGraph doNeo4jGraphTest()
    {
        String doTest = System.getProperty( "testNeo4j" );
        if ( doTest == null || doTest.equals( "true" ) )
        {
            String directory = System.getProperty( "neo4jDirectory" );
            if ( directory == null )
            {
                directory = "/tmp/blueprints_test";
                Neo4jGraphTest.deleteGraphDirectory( new File( directory ) );
                Neo4jGraph graph = new Neo4jGraph( directory );
                graph.clear();
                return graph;
            }
        }
        return null;
    }

    /**
     * Try using an injected neo4j instance.
     * 
     * @return
     */
    protected static Neo4jGraph doInjectedNeo4jGraphTest()
    {
        String doTest = System.getProperty( "testNeo4j" );
        if ( doTest == null || doTest.equals( "true" ) )
        {
            String directory = "/tmp/blueprints_test";
            Neo4jGraphTest.deleteGraphDirectory( new File( directory ) );

            GraphDatabaseService dbInstance = new EmbeddedGraphDatabase(
                    directory );
            IndexService indexService = new LuceneIndexService( dbInstance );
            Neo4jGraph graph = new Neo4jGraph( dbInstance, indexService );

            // Remove reference node
            graph.removeVertex( graph.getVertex( 0 ) );

            return graph;
        }
        return null;
    }

    protected static void deleteGraphDirectory( final File directory )
    {
        if ( directory.exists() )
        {
            for ( File file : directory.listFiles() )
            {
                if ( file.isDirectory() )
                {
                    deleteGraphDirectory( file );
                }
                else
                {
                    file.delete();
                }
            }
            directory.delete();
        }
    }

}
