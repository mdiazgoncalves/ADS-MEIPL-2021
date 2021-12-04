package pt.iul.ista.ads.owl;

public class IndividualAlreadyExistsOntologyException extends OntologyException {

	private static final long serialVersionUID = 1L;

	public IndividualAlreadyExistsOntologyException(String s) {
		super("Individual " + s + " already exists in ontology");
	}

}
