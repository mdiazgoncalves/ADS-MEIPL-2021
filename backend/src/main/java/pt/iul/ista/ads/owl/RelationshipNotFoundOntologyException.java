package pt.iul.ista.ads.owl;

public class RelationshipNotFoundOntologyException extends OntologyException {

	private static final long serialVersionUID = 1L;

	public RelationshipNotFoundOntologyException(String s) {
		super("Relationship " + s + " does not exist in ontology");
	}

}
