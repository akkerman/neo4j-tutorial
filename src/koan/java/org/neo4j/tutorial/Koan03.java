package org.neo4j.tutorial;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import static org.junit.Assert.*;
import static org.neo4j.tutorial.matchers.ContainsOnlySpecificSpecies.containsOnlySpecies;
import static org.neo4j.tutorial.matchers.ContainsSpecificCompanions.contains;

/**
 * This Koan will introduce indexing based on the built-in index framework based
 * on Lucene. It'll give you a feeling for the wealth of bad guys the Doctor has
 * faced.
 */
public class Koan03 {

    private static EmbeddedDoctorWhoUniverse universe;

    @BeforeClass
    public static void createDatabase() throws Exception {
        universe = new EmbeddedDoctorWhoUniverse(new DoctorWhoUniverseGenerator());
    }

    @AfterClass
    public static void closeTheDatabase() {
        universe.stop();
    }

    @Test
    public void shouldRetrieveCharactersIndexFromTheDatabase() {
        Index<Node> characters;

        // YOUR CODE GOES HERE
        characters = universe.getDatabase().index().forNodes("characters");


        assertNotNull(characters);
        assertThat(
                characters,
                contains("Master", "River Song", "Rose Tyler", "Adam Mitchell", "Jack Harkness", "Mickey Smith",
                        "Donna Noble", "Martha Jones"));
    }

    @Test
    public void addingToAnIndexShouldBeHandledAsAMutatingOperation() {
        GraphDatabaseService db = universe.getDatabase();
        Node abigailPettigrew = createAbigailPettigrew(db);

        assertNull(db.index()
                .forNodes("characters")
                .get("character", "Abigail Pettigrew")
                .getSingle());

        // YOUR CODE GOES HERE
        Transaction tx = db.beginTx();
        try {
            Index<Node> characters = db.index().forNodes("characters");
            characters.add(abigailPettigrew, "character", "Abigail Pettigrew");
            tx.success();
        } finally {
            tx.finish();
        }

        assertNotNull(db.index()
                .forNodes("characters")
                .get("character", "Abigail Pettigrew")
                .getSingle());
    }

    @Test
    public void shouldFindSpeciesBeginningWithCapitalLetterSAndEndingWithLowerCaseLetterNUsingLuceneQuery() throws Exception {
        IndexHits<Node> species;

        // YOUR CODE GOES HERE
        Index<Node> speciesIndex = universe.getDatabase().index().forNodes("species");
        species = speciesIndex.query("species","S*n");

        assertThat(species, containsOnlySpecies("Silurian", "Slitheen", "Sontaran", "Skarasen"));
    }

    /**
     * In this example, it's more important to understand what you *don't* have
     * to do, rather than the work you explicitly have to do. Sometimes indexes
     * just do the right thing...
     */
    @Test
    public void shouldEnsureDatabaseAndIndexInSyncWhenCyberleaderIsDeleted() throws Exception {
        GraphDatabaseService db = universe.getDatabase();
        Node cyberleader = retrieveCyberleaderFromIndex(db);

        // YOUR CODE GOES HERE
        Transaction tx = db.beginTx();
        try {
            for (Relationship relationship : cyberleader.getRelationships()) {
                relationship.delete();
            }
            cyberleader.delete();
            tx.success();
        } finally {
            tx.finish();
        }

        assertNull("Cyberleader has not been deleted from the characters index.", retrieveCyberleaderFromIndex(db));

        try {
            db.getNodeById(cyberleader.getId());
            fail("Cyberleader has not been deleted from the database.");
        } catch (NotFoundException nfe) {
        }
    }

    private Node retrieveCyberleaderFromIndex(GraphDatabaseService db) {
        return db.index()
                .forNodes("characters")
                .get("character", "Cyberleader")
                .getSingle();
    }

    private Node createAbigailPettigrew(GraphDatabaseService db) {
        Node abigailPettigrew;
        Transaction tx = db.beginTx();
        try {
            abigailPettigrew = db.createNode();
            abigailPettigrew.setProperty("character", "Abigail Pettigrew");
            tx.success();
        } finally {
            tx.finish();
        }
        return abigailPettigrew;
    }
}
