package org.neo4j.tutorial;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.cypher.ExecutionResult;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.impl.util.StringLogger;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.neo4j.helpers.collection.IteratorUtil.asIterable;
import static org.neo4j.tutorial.matchers.ContainsOnlySpecificTitles.containsOnlyTitles;

/**
 * In this Koan we learn the basics of the Cypher query language, focusing on the
 * matching capabilities to RETURN subgraphs of information about the Doctor Who
 * universe.
 */
public class Koan08b
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
    public void shouldFindAndReturnTheDoctor()
    {
        ExecutionEngine engine = new ExecutionEngine( universe.getDatabase(), StringLogger.DEV_NULL );
        String cql = "START doctor = node:characters(character='Doctor') return doctor";

        // YOUR CODE GOES HERE

        ExecutionResult result = engine.execute( cql );
        Iterator<Node> episodes = result.javaColumnAs( "doctor" );

        assertEquals( episodes.next(), universe.theDoctor() );
    }

    @Test
    public void shouldFindAllTheEpisodesUsingIndexesOnly()
    {
        // The number of episodes is not the same as the highest episode number.
        // Some episodes are two-parters with the same episode number, others use schemes like
        // 218a and 218b as their episode numbers seemingly just to be difficult!

        ExecutionEngine engine = new ExecutionEngine( universe.getDatabase(), StringLogger.DEV_NULL );
        String cql = "start episodes = node:episodes('episode:*') return episodes";

        // YOUR CODE GOES HERE

        ExecutionResult result = engine.execute( cql );

        assertEquals( 252l, result.length() );
    }


    @Test
    public void shouldFindAllTheEpisodesInWhichTheCybermenAppeared() throws Exception
    {
        ExecutionEngine engine = new ExecutionEngine( universe.getDatabase(), StringLogger.DEV_NULL );
        String cql = "start c = node:species(species='Cyberman') MATCH (c)-[:APPEARED_IN]->episode return episode";

        // YOUR CODE GOES HERE

        ExecutionResult result = engine.execute( cql );
        Iterator<Node> episodes = result.javaColumnAs( "episode" );

        assertThat( asIterable( episodes ), containsOnlyTitles( "Closing Time",
                "A Good Man Goes to War",
                "The Pandorica Opens",
                "The Next Doctor",
                "Doomsday",
                "Army of Ghosts",
                "The Age of Steel",
                "Rise of the Cybermen",
                "Silver Nemesis",
                "Earthshock",
                "Revenge of the Cybermen",
                "The Wheel in Space",
                "The Tomb of the Cybermen",
                "The Moonbase" ) );
    }

    @Test
    public void shouldFindEpisodesWhereTennantAndRoseBattleTheDaleks() throws Exception
    {
        ExecutionEngine engine = new ExecutionEngine( universe.getDatabase(), StringLogger.DEV_NULL );
        String cql = "start ";
        cql+= "tennant=node:actors(actor='David Tennant'), rose=node:characters(character='Rose Tyler'), daleks=node:species(species='Dalek')";
        cql+=  "match ";
        cql+= "(tennant)-[:APPEARED_IN]->(episode), (rose)-[:APPEARED_IN]->(episode), (daleks)-[:APPEARED_IN]->(episode)";
        cql+= " return episode";

        // YOUR CODE GOES HERE

        ExecutionResult result = engine.execute( cql );
        Iterator<Node> episodes = result.javaColumnAs( "episode" );

        assertThat( asIterable( episodes ),
                containsOnlyTitles( "Journey's End", "The Stolen Earth", "Doomsday", "Army of Ghosts",
                        "The Parting of the Ways" ) );
    }

    @Test
    public void shouldFindIndividualCompanionsAndEnemiesOfTheDoctor()
    {
        ExecutionEngine engine = new ExecutionEngine( universe.getDatabase(), StringLogger.DEV_NULL );
        String cql = " start ";
        cql+="doctor=node:characters(character='Doctor')";
        cql+=" match ";
        cql+="(eoc)-[:ENEMY_OF|COMPANION_OF]->(doctor) where has(eoc.character)";
        cql+="return distinct eoc";

        // YOUR CODE GOES HERE


        ExecutionResult result = engine.execute( cql );

        assertEquals( 156, result.size() );
    }
}
