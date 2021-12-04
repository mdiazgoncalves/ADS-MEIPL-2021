package pt.iul.ista.ads.owl;

public class IndividualNotFoundOntologyException extends OntologyException {

	private static final long serialVersionUID = 1L;

	public IndividualNotFoundOntologyException(String s) {
		super("Individual " + s + " not found in ontology");
	}

}
