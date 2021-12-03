package pt.iul.ista.ads.owl;

public class RelationshipAlreadyExistsOntologyException extends OntologyException {

	private static final long serialVersionUID = 1L;

	public RelationshipAlreadyExistsOntologyException(String s) {
		super("Relationship " + s + " already exists in ontology");
	}

}
