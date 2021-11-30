package pt.iul.ista.ads.owl;

public class ClassAlreadyExistsOntologyException extends OntologyException {

	private static final long serialVersionUID = 1L;

	public ClassAlreadyExistsOntologyException(String className) {
		super("Class " + className + " already exists in ontology");
	}

}
