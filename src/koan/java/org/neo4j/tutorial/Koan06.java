package org.neo4j.tutorial;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//import org.neo4j.graphdb.Traverser;

/**
 * In this Koan we start using the simple traversal framework to find
 * interesting information from the graph.
 */
public class Koan06
{

    private static EmbeddedDoctorWhoUniverse universe;

    @BeforeClass
    public static void createDatabase() throws Exception
    {
        universe = new EmbeddedDoctorWhoUniverse( new DoctorWhoUniverseGenerator() );
    }

    @AfterClass
    public static void closeTheDatabase()
    {
        universe.stop();
    }

    @Test
    public void shouldFindAllCompanions()
    {
        Node theDoctor = universe.theDoctor();
        Traverser t = null;



        // YOUR CODE GOES HERE
        TraversalDescription td = Traversal.description()
                .depthFirst()
                .relationships(DoctorWhoRelationships.COMPANION_OF, Direction.INCOMING)
                .evaluator(Evaluators.toDepth(1))
                .evaluator(Evaluators.excludeStartPosition())
                ;
        t = td.traverse(theDoctor);

        Collection<Node> foundCompanions = new HashSet<Node>();
        for (Node node : t.nodes())
            foundCompanions.add(node);

        int knownNumberOfCompanions = 47;
        assertEquals( knownNumberOfCompanions, foundCompanions.size() );
    }

    @Test
    public void shouldFindAllDalekProps()
    {
        Node theDaleks = universe.getDatabase()
                .index()
                .forNodes( "species" )
                .get( "species", "Dalek" )
                .getSingle();

        TraversalDescription td = Traversal.description()
                .depthFirst()
                .relationships(DoctorWhoRelationships.APPEARED_IN)
                .relationships(DoctorWhoRelationships.USED_IN)
                .relationships(DoctorWhoRelationships.MEMBER_OF)
                .evaluator(new Evaluator() {
                    @Override
                    public Evaluation evaluate(Path propertyContainers) {
                        if (propertyContainers.endNode().hasProperty("prop"))
                            return Evaluation.INCLUDE_AND_PRUNE;
                        return Evaluation.EXCLUDE_AND_CONTINUE;
                    }
                });


        Collection<Node> nodes = new HashSet<Node>();
        for (Node node : td.traverse(theDaleks).nodes()) {
            nodes.add(node);
        }
        assertCollectionContainsAllDalekProps( nodes );
    }

    private void assertCollectionContainsAllDalekProps( Collection<Node> nodes )
    {
        String[] dalekProps = new String[] { "Dalek One-7", "Imperial 4", "Imperial 3", "Imperial 2", "Imperial 1",
                "Supreme Dalek", "Remembrance 3", "Remembrance 2", "Remembrance 1", "Dalek V-VI", "Goon IV", "Goon II",
                "Goon I", "Dalek Six-5", "Dalek Seven-2", "Dalek V-5", "Dalek Seven-V", "Dalek Six-Ex",
                "Dalek Seven-8", "Dalek 8", "Dalek 7", "Dalek Five-6", "Dalek Two-1", "Dalek 2", "Dalek 1", "Dalek 6",
                "Dalek 5", "Dalek 4", "Dalek 3", "Dalek IV-Ex", "Dalek Seven-II", "Necros 3", "Necros 2", "Necros 1",
                "Goon III", "Goon VII", "Goon VI", "Goon V", "Gold Movie Dalek", "Dalek Six-7", "Dalek One-5" };

        List<String> propList = new ArrayList<String>();
        for ( Node n : nodes )
        {
            propList.add( n.getProperty( "prop" )
                    .toString() );
        }

        assertEquals( dalekProps.length, propList.size() );
        for ( String prop : dalekProps )
        {
            assertTrue( propList.contains( prop ) );
        }
    }

    @Test
    public void shouldFindAllTheEpisodesTheMasterAndDavidTennantWereInTogether()
    {
        Node theMaster = universe.getDatabase()
                .index()
                .forNodes( "characters" )
                .get( "character", "Master" )
                .getSingle();


        TraversalDescription td = Traversal.description()
                .depthFirst()
                .relationships(DoctorWhoRelationships.APPEARED_IN, Direction.OUTGOING)
                .evaluator(new Evaluator() {
                    @Override
                    public Evaluation evaluate(Path propertyContainers) {
                        Node episode = propertyContainers.endNode();
                        for (Relationship relationship : episode.getRelationships(DoctorWhoRelationships.APPEARED_IN, Direction.INCOMING)) {
                            Node appearee =  relationship.getStartNode();
                            if ( appearee.hasProperty("actor") &&
                                    "David Tennant".equals(appearee.getProperty("actor")))
                                return Evaluation.INCLUDE_AND_PRUNE;
                        }

                        return Evaluation.EXCLUDE_AND_CONTINUE;
                    }
                });


        Traverser t = td.traverse(theMaster);






        // YOUR CODE GOES HERE
        Collection<Node> nodes= new HashSet<Node>();
        for (Node node : t.nodes()) {
            nodes.add(node);
        }

        int numberOfEpisodesWithTennantVersusTheMaster = 4;
        assertEquals(numberOfEpisodesWithTennantVersusTheMaster, nodes.size());
    }
}
