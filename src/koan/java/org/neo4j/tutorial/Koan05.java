package org.neo4j.tutorial;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.neo4j.tutorial.matchers.ContainsOnlyHumanCompanions.containsOnlyHumanCompanions;
import static org.neo4j.tutorial.matchers.ContainsOnlySpecificTitles.containsOnlyTitles;

/**
 * In this Koan we start to mix indexing and core API to perform more targeted
 * graph operations. We'll mix indexes and core graph operations to explore the
 * Doctor's universe.
 */
public class Koan05
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
    public void shouldCountTheNumberOfDoctorsRegeneratedForms()
    {

        Index<Node> actorsIndex = universe.getDatabase()
                .index()
                .forNodes( "actors" );
        int numberOfRegenerations = 1;

        // YOUR CODE GOES HERE
        Node firstDoctor = actorsIndex.get( "actor", "William Hartnell" ).getSingle();

        Node regeneration = firstDoctor;


        do {
            Relationship rel = regeneration.getSingleRelationship(DoctorWhoRelationships.REGENERATED_TO, Direction.OUTGOING);
            regeneration = rel == null ? null : rel.getEndNode();
            numberOfRegenerations = regeneration == null ? numberOfRegenerations : numberOfRegenerations+1;
        } while (regeneration != null);


            assertEquals(11, numberOfRegenerations);
    }

    @Test
    public void shouldFindHumanCompanionsUsingCoreApi()
    {
        HashSet<Node> humanCompanions = new HashSet<Node>();

        // YOUR CODE GOES HERE
        Node human = universe.getDatabase()
                .index()
                .forNodes( "species" )
                .get( "species", "Human" )
                .getSingle();

        for (Relationship companionRel : universe.theDoctor().getRelationships(Direction.INCOMING, DoctorWhoRelationships.COMPANION_OF)) {
            Node companionNode = companionRel.getStartNode();
            for (Relationship isaRel : companionNode.getRelationships(Direction.OUTGOING, DoctorWhoRelationships.IS_A)) {
                if (human.equals(isaRel.getEndNode())) {
                    humanCompanions.add(companionNode);
                    break;
                }
            }
        }

        int numberOfKnownHumanCompanions = 40;
        assertEquals( numberOfKnownHumanCompanions, humanCompanions.size() );
        assertThat( humanCompanions, containsOnlyHumanCompanions() );
    }

    @Test
    public void shouldFindAllEpisodesWhereRoseTylerFoughtTheDaleks()
    {
        Index<Node> friendliesIndex = universe.getDatabase()
                .index()
                .forNodes( "characters" );
        Index<Node> speciesIndex = universe.getDatabase()
                .index()
                .forNodes( "species" );
        HashSet<Node> episodesWhereRoseFightsTheDaleks = new HashSet<Node>();

        // YOUR CODE GOES HERE
        Node rose = friendliesIndex.get("character", "Rose Tyler").getSingle();
        Node dalek = speciesIndex.get("species", "Dalek").getSingle();

        for (Relationship relationship : rose.getRelationships(Direction.OUTGOING, DoctorWhoRelationships.APPEARED_IN)) {
            episodesWhereRoseFightsTheDaleks.add(relationship.getEndNode());
        }

        HashSet<Node> episodesWithDaleks = new HashSet<Node>();
        for (Relationship relationship : dalek.getRelationships(Direction.OUTGOING, DoctorWhoRelationships.APPEARED_IN)) {
            episodesWithDaleks.add(relationship.getEndNode());
        }

        episodesWhereRoseFightsTheDaleks.retainAll(episodesWithDaleks);

        assertThat(
                episodesWhereRoseFightsTheDaleks,
                containsOnlyTitles("Army of Ghosts", "The Stolen Earth", "Doomsday", "Journey's End", "Bad Wolf",
                        "The Parting of the Ways", "Dalek"));
    }
}
