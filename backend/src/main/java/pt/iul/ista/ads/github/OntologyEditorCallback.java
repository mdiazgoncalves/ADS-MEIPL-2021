package pt.iul.ista.ads.github;

import pt.iul.ista.ads.owl.Ontology;
import pt.iul.ista.ads.owl.OntologyException;

public interface OntologyEditorCallback {

	void execute(Ontology ontology) throws OntologyException;
	
}
