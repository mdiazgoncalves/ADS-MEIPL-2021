package pt.iul.ista.ads.owl;

public class ClassNotFoundOntologyException extends OntologyException {

	private static final long serialVersionUID = 1L;

	public ClassNotFoundOntologyException(String className) {
		super("Class " + className + " does not exist in ontology");
	}
	
}
