package pt.iul.ista.ads.owl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.sqwrl.values.SQWRLResultValue;

import pt.iul.ista.ads.models.ClassDetailResponseModel;
import pt.iul.ista.ads.models.ClassesResponseModel;
import pt.iul.ista.ads.models.IndividualModel;
import pt.iul.ista.ads.models.IndividualRelationshipModel;
import pt.iul.ista.ads.models.QueryResponseModel;
import pt.iul.ista.ads.models.RelationshipDetailResponseModel;
import pt.iul.ista.ads.models.RelationshipsResponseModel;

public class Ontology {
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory factory;
	private IRI documentIRI;
	private PrefixManager prefixManager;
	
	private static final Charset charset = StandardCharsets.UTF_8;
	
	public Ontology(String owl) throws OntologyException {
		try {
			manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromOntologyDocument(new ByteArrayInputStream(owl.getBytes(charset)));
			factory = manager.getOWLDataFactory();
			documentIRI = ontology.getOntologyID().getOntologyIRI().get();
			prefixManager = new DefaultPrefixManager(documentIRI.toString());
		} catch(OWLOntologyCreationException e) {
			throw new OntologyException(e);
		}
	}
	
	@Override
	public String toString() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			manager.saveOntology(ontology, baos);
			return new String(baos.toByteArray(), charset);
		} catch(OWLOntologyStorageException e) {
			throw new RuntimeException(e);
		}
	}

	public void addClass(String className, String superclassName) throws ClassNotFoundOntologyException, ClassAlreadyExistsOntologyException, InvalidNameOntologyException {
		checkValidNewClass(className);
		OWLClass newClass = factory.getOWLClass(":#" + className, prefixManager);
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(newClass));
		if(superclassName != null && !superclassName.isEmpty()) {
			checkClassExists(superclassName);
			OWLClass superclass = factory.getOWLClass(":#" + superclassName, prefixManager);
			manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(newClass, superclass));
		}
	}
	
	private void checkClassExists(String className) throws ClassNotFoundOntologyException {
		if(!ontology.containsClassInSignature(IRI.create(documentIRI.toString() + "#" + className)))
			throw new ClassNotFoundOntologyException(className);
	}
	
	private void checkValidNewClass(String className) throws ClassAlreadyExistsOntologyException, InvalidNameOntologyException {
		if(!isValidName(className))
			throw new InvalidNameOntologyException(className);
		if(ontology.containsClassInSignature(IRI.create(documentIRI.toString() + "#" + className)))
			throw new ClassAlreadyExistsOntologyException(className);
	}
	
	public List<ClassesResponseModel.ClassTreeModel> listClasses() {
		Set<OWLClass> classes = ontology.getClassesInSignature();
		List<ClassesResponseModel.ClassTreeModel> res = new ArrayList<ClassesResponseModel.ClassTreeModel>();
		for(OWLClass cls : classes) {
			Set<OWLSubClassOfAxiom> superclassAxioms = ontology.getSubClassAxiomsForSubClass(cls);
			if(superclassAxioms.isEmpty())
				res.add(classModel(cls));
		}
		return res;
	}
	
	private ClassesResponseModel.ClassTreeModel classModel(OWLClass cls) {
		ClassesResponseModel.ClassTreeModel res = new ClassesResponseModel.ClassTreeModel();
		res.setClassName(owlEntityToString(cls));
		res.setSubclasses(new ArrayList<ClassesResponseModel.ClassTreeModel>());
		for(OWLSubClassOfAxiom subclassAxiom : ontology.getSubClassAxiomsForSuperClass(cls)) {
			res.getSubclasses().add(classModel(subclassAxiom.getSubClass().asOWLClass()));
		}
		return res;
	}
	
	private String owlEntityToString(OWLEntity entity) {
		return entity.toStringID().replace(documentIRI, "").replace("#", "");
	}
	
	public ClassDetailResponseModel.ClassModel detailClass(String className) throws ClassNotFoundOntologyException {
		checkClassExists(className);
		
		ClassDetailResponseModel.ClassModel res = new ClassDetailResponseModel.ClassModel();
		
		res.setClassName(className);
		
		OWLClass cls = factory.getOWLClass(":#" + className, prefixManager);
		Set<OWLSubClassOfAxiom> superclassAxioms = ontology.getSubClassAxiomsForSubClass(cls);
		if(!superclassAxioms.isEmpty())
			res.setSuperClass(owlEntityToString(superclassAxioms.iterator().next().getSuperClass().asOWLClass()));
		
		res.setIndividuals(new ArrayList<String>());
		Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
		for(OWLNamedIndividual individual : individuals) {
			OWLClassExpression classExpression = EntitySearcher.getTypes(individual, ontology).iterator().next();
			if(owlEntityToString(classExpression.asOWLClass()).equals(className))
				res.getIndividuals().add(owlEntityToString(individual));
		}
		
		return res;
	}
	
	public void alterClass(String className, String newClassName, String newSuperclassName) throws ClassNotFoundOntologyException, ClassAlreadyExistsOntologyException, InvalidNameOntologyException {
		// validar os par??metros primeiro
		checkClassExists(className);
		if(newClassName != null) {
			checkValidNewClass(newClassName);
		}
		if(newSuperclassName != null) {
			checkClassExists(newSuperclassName);
		}
		
		// par??metros validados; efetuar altera????es
		OWLClass cls = factory.getOWLClass(":#" + className, prefixManager);
		if(newClassName != null) {
			OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(ontology));
			OWLClass newCls = factory.getOWLClass(":#" + newClassName, prefixManager);
			manager.applyChanges(renamer.changeIRI(cls.getIRI(), newCls.getIRI()));
			cls = newCls; // alterar cls para que c??digo abaixo se refira ao novo nome da classe
		}
		if(newSuperclassName != null) {
			for(OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSubClass(cls))
				manager.removeAxiom(ontology, axiom);
			
			if(!newSuperclassName.isEmpty()) {
				OWLClass superClass = factory.getOWLClass(":#" + newSuperclassName, prefixManager);
				manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(cls, superClass));
			}
		}

	}
	
	public void deleteClass(String className) throws ClassNotFoundOntologyException {
		checkClassExists(className);
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
		OWLClass cls = factory.getOWLClass(":#" + className, prefixManager);
		cls.accept(remover);
		manager.applyChanges(remover.getChanges());
	}
	
	public List<RelationshipsResponseModel.RelationshipModel> listRelationships() {
		List<RelationshipsResponseModel.RelationshipModel> res = new ArrayList<RelationshipsResponseModel.RelationshipModel>();
		for(OWLObjectProperty property : ontology.getObjectPropertiesInSignature()) {
			RelationshipsResponseModel.RelationshipModel relationshipModel = new RelationshipsResponseModel.RelationshipModel();
			relationshipModel.setName(owlEntityToString(property));
			OWLClass class1 = ontology.getObjectPropertyDomainAxioms(property).stream().findFirst().get().getDomain().asOWLClass();
			relationshipModel.setClassName1(owlEntityToString(class1));
			OWLClass class2 = ontology.getObjectPropertyRangeAxioms(property).stream().findFirst().get().getRange().asOWLClass();
			relationshipModel.setClassName2(owlEntityToString(class2));
			res.add(relationshipModel);
		}
		return res;
	}
	
	public List<RelationshipDetailResponseModel.RelationshipInstanceModel> detailRelationship(String relationship) throws RelationshipNotFoundOntologyException {
		checkRelationshipExists(relationship);
		List<RelationshipDetailResponseModel.RelationshipInstanceModel> res = new ArrayList<RelationshipDetailResponseModel.RelationshipInstanceModel>();
		OWLObjectProperty property = factory.getOWLObjectProperty(":#" + relationship, prefixManager);
		for(OWLObjectPropertyAssertionAxiom axiom : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION, Imports.EXCLUDED)) {
			if(axiom.getObjectPropertiesInSignature().contains(property)) {
				RelationshipDetailResponseModel.RelationshipInstanceModel relationshipModel = new RelationshipDetailResponseModel.RelationshipInstanceModel();
				relationshipModel.setIndividual1(owlEntityToString((OWLNamedIndividual) axiom.getSubject()));
				relationshipModel.setIndividual2(owlEntityToString((OWLNamedIndividual) axiom.getObject()));
				res.add(relationshipModel);
			}
		}
		return res;
	}
	
	public void createRelationship(String relationship, String className1, String className2) throws ClassNotFoundOntologyException, RelationshipAlreadyExistsOntologyException, InvalidNameOntologyException {
		checkValidNewRelationship(relationship);
		checkClassExists(className1);
		checkClassExists(className2);
		OWLObjectProperty property = factory.getOWLObjectProperty(":#" + relationship, prefixManager);
		OWLClass class1 = factory.getOWLClass(":#" + className1, prefixManager);
		OWLClass class2 = factory.getOWLClass(":#" + className2, prefixManager);
		OWLAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(property, class1);
		OWLAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(property, class2);
		OWLAxiom declarationAxiom = factory.getOWLDeclarationAxiom(property);
		manager.addAxiom(ontology, declarationAxiom);
		manager.addAxiom(ontology, domainAxiom);
		manager.addAxiom(ontology, rangeAxiom);
	}

	private void checkValidNewRelationship(String relationship) throws RelationshipAlreadyExistsOntologyException, InvalidNameOntologyException {
		if(!isValidName(relationship))
			throw new InvalidNameOntologyException(relationship);
		if(ontology.containsObjectPropertyInSignature(IRI.create(documentIRI.toString() + "#" + relationship)))
			throw new RelationshipAlreadyExistsOntologyException(relationship);
	}
	
	private void checkRelationshipExists(String relationship) throws RelationshipNotFoundOntologyException {
		if(!ontology.containsObjectPropertyInSignature(IRI.create(documentIRI.toString() + "#" + relationship)))
			throw new RelationshipNotFoundOntologyException(relationship);
	}
	
	public void alterRelationship(String relationship, String newRelationshipName, String className1, String className2) throws RelationshipNotFoundOntologyException, RelationshipAlreadyExistsOntologyException, ClassNotFoundOntologyException, InvalidNameOntologyException {
		// validar os par??metros primeiro
		checkRelationshipExists(relationship);
		if(newRelationshipName != null)
			checkValidNewRelationship(newRelationshipName);
		if(className1 != null)
			checkClassExists(className1);
		if(className2 != null)
			checkClassExists(className2);
		
		// par??metros validados; efetuar altera????es
		OWLObjectProperty property = factory.getOWLObjectProperty(":#" + relationship, prefixManager);
		if(newRelationshipName != null) {
			OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(ontology));
			OWLObjectProperty newProperty = factory.getOWLObjectProperty(":#" + newRelationshipName, prefixManager);
			manager.applyChanges(renamer.changeIRI(property.getIRI(), newProperty.getIRI()));
			property = newProperty; // reassign para c??digo abaixo se referir ?? nova property
		}
		if(className1 != null) {
			for(OWLAxiom axiom : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN, Imports.EXCLUDED)) {
				if(axiom.getObjectPropertiesInSignature().contains(property)) {
					manager.removeAxiom(ontology, axiom);
					break;
				}
			}
			OWLClass cls = factory.getOWLClass(":#" + className1, prefixManager);
			OWLAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(property, cls);
			manager.addAxiom(ontology, domainAxiom);
		}
		if(className2 != null) {
			for(OWLAxiom axiom : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE, Imports.EXCLUDED)) {
				if(axiom.getObjectPropertiesInSignature().contains(property)) {
					manager.removeAxiom(ontology, axiom);
					break;
				}
			}
			OWLClass cls = factory.getOWLClass(":#" + className2, prefixManager);
			OWLAxiom rangeAxiom = factory.getOWLObjectPropertyRangeAxiom(property, cls);
			manager.addAxiom(ontology, rangeAxiom);
		}
	}
	
	public void deleteRelationship(String relationship) throws RelationshipNotFoundOntologyException {
		checkRelationshipExists(relationship);
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
		OWLObjectProperty property = factory.getOWLObjectProperty(":#" + relationship, prefixManager);
		property.accept(remover);
		manager.applyChanges(remover.getChanges());
	}
	
	public List<IndividualModel> listIndividuals() {
		List<IndividualModel> res = new ArrayList<IndividualModel>();
		for(OWLNamedIndividual individual : ontology.getIndividualsInSignature()) {
			try {
				res.add(detailIndividual(owlEntityToString(individual)));
			} catch (IndividualNotFoundOntologyException e) {
				// n??o ?? suposto o fluxo entrar aqui
				throw new RuntimeException(e);
			}
		}
		return res;
	}
	
	public IndividualModel detailIndividual(String individualName) throws IndividualNotFoundOntologyException {
		checkIndividualExists(individualName);
		IndividualModel res = new IndividualModel();
		res.setIndividualName(individualName);
		OWLNamedIndividual individual = factory.getOWLNamedIndividual(":#" + individualName, prefixManager);
		OWLClass cls = ontology.getClassAssertionAxioms(individual).iterator().next().getClassesInSignature().iterator().next();
		res.setClassName(owlEntityToString(cls));
		res.setRelationships(new ArrayList<IndividualRelationshipModel>());
		for(OWLObjectPropertyAssertionAxiom axiom : ontology.getObjectPropertyAssertionAxioms(individual)) {
			IndividualRelationshipModel individualRelationship = new IndividualRelationshipModel();
			individualRelationship.setIndividual2(owlEntityToString((OWLNamedIndividual) axiom.getObject()));
			individualRelationship.setRelationshipName(owlEntityToString(axiom.getProperty().getNamedProperty()));
			res.getRelationships().add(individualRelationship);
		}
		return res;
	}
	
	private void checkIndividualExists(String individual) throws IndividualNotFoundOntologyException {
		if(!ontology.containsIndividualInSignature(IRI.create(documentIRI.toString() + "#" + individual)))
			throw new IndividualNotFoundOntologyException(individual);
	}
	
	private void checkValidNewIndividual(String individual) throws IndividualAlreadyExistsOntologyException, InvalidNameOntologyException {
		if(!isValidName(individual))
			throw new InvalidNameOntologyException(individual);
		if(ontology.containsIndividualInSignature(IRI.create(documentIRI.toString() + "#" + individual)))
			throw new IndividualAlreadyExistsOntologyException(individual);
	}
	
	public void createIndividual(String individualName, String className, List<IndividualRelationshipModel> relationships) throws IndividualAlreadyExistsOntologyException, ClassNotFoundOntologyException, RelationshipNotFoundOntologyException, IndividualNotFoundOntologyException, InvalidNameOntologyException {
		checkValidNewIndividual(individualName);
		checkClassExists(className);
		checkValidIndividualRelationshipModels(relationships);
		
		OWLNamedIndividual individual = factory.getOWLNamedIndividual(":#" + individualName, prefixManager);
		OWLClass cls = factory.getOWLClass(":#" + className, prefixManager);
		OWLAxiom individualAxiom = factory.getOWLClassAssertionAxiom(cls, individual);
		manager.addAxiom(ontology, individualAxiom);
		replaceIndividualRelationships(individual, relationships);
	}
	
	private void checkValidIndividualRelationshipModels(List<IndividualRelationshipModel> relationships) throws RelationshipNotFoundOntologyException, IndividualNotFoundOntologyException {
		for(IndividualRelationshipModel relationship : relationships) {
			checkRelationshipExists(relationship.getRelationshipName());
			checkIndividualExists(relationship.getIndividual2());
		}
	}
	
	// este m??todo n??o valida se as rela????es existem nem se os indiv??duos existem
	// necess??rio invocar checkValidIndividualRelationshipModels primeiro
	private void replaceIndividualRelationships(OWLNamedIndividual individual, List<IndividualRelationshipModel> relationships) {
		for(OWLObjectPropertyAssertionAxiom axiom : ontology.getObjectPropertyAssertionAxioms(individual)) {
			manager.removeAxiom(ontology, axiom);
		}
		for(IndividualRelationshipModel relationship : relationships) {
			OWLObjectProperty property = factory.getOWLObjectProperty(":#" + relationship.getRelationshipName(), prefixManager);
			OWLNamedIndividual individual2 = factory.getOWLNamedIndividual(":#" + relationship.getIndividual2(), prefixManager);
			OWLAxiom axiom = factory.getOWLObjectPropertyAssertionAxiom(property, individual, individual2);
			manager.addAxiom(ontology, axiom);
		}
	}
	
	public void alterIndividual(String individualName, String newIndividualName, String newClassName, List<IndividualRelationshipModel> relationships) throws IndividualNotFoundOntologyException, IndividualAlreadyExistsOntologyException, ClassNotFoundOntologyException, RelationshipNotFoundOntologyException, InvalidNameOntologyException {
		checkIndividualExists(individualName);
		if(newIndividualName != null)
			checkValidNewIndividual(newIndividualName);
		if(newClassName != null)
			checkClassExists(newClassName);
		if(relationships != null)
			checkValidIndividualRelationshipModels(relationships);
		
		OWLNamedIndividual individual = factory.getOWLNamedIndividual(":#" + individualName, prefixManager);
		if(newIndividualName != null) {
			OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(ontology));
			OWLNamedIndividual newIndividual = factory.getOWLNamedIndividual(":#" + newIndividualName, prefixManager);
			manager.applyChanges(renamer.changeIRI(individual.getIRI(), newIndividual.getIRI()));
			individual = newIndividual; // reassign para c??digo abaixo se referir ao novo indiv??duo
		}
		if(newClassName != null) {
			OWLAxiom classAxiom = ontology.getClassAssertionAxioms(individual).iterator().next();
			manager.removeAxiom(ontology, classAxiom);
			OWLClass cls = factory.getOWLClass(":#" + newClassName, prefixManager);
			OWLAxiom individualAxiom = factory.getOWLClassAssertionAxiom(cls, individual);
			manager.addAxiom(ontology, individualAxiom);
		}
		if(relationships != null) {
			replaceIndividualRelationships(individual, relationships);
		}
	}
	
	public void deleteIndividual(String individualName) throws IndividualNotFoundOntologyException {
		checkIndividualExists(individualName);
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
		OWLNamedIndividual individual = factory.getOWLNamedIndividual(":#" + individualName, prefixManager);
		individual.accept(remover);
		manager.applyChanges(remover.getChanges());
	}
	
	public List<QueryResponseModel.QueryResponse> query(List<String> queries) throws OntologyException {
		try {
			List<QueryResponseModel.QueryResponse> res = new ArrayList<QueryResponseModel.QueryResponse>();
			int i = 1;
			for(String query : queries) {
				SQWRLQueryEngine queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology);
				String variableName = extractVariableNameFromQuery(query);
				SQWRLResult queryResult = queryEngine.runSQWRLQuery("q" + i, query);
				++i;
				List<String> results = new ArrayList<String>();
				for(SQWRLResultValue value : queryResult.getColumn(0)) {
					String qualifiedName = value.toString();
					String shortName = qualifiedName.substring(qualifiedName.indexOf(":") + 1);
					results.add(shortName);
				}
				QueryResponseModel.QueryResponse queryResponse = new QueryResponseModel.QueryResponse();
				queryResponse.setVariableName(variableName);
				queryResponse.setResults(results);
				res.add(queryResponse);
			}
			return res;
		} catch(SQWRLException | SWRLParseException e) {
			throw new OntologyException(e);
		}
	}
	
	private String extractVariableNameFromQuery(String query) throws InvalidQueryOntologyException {
		Pattern p = Pattern.compile("sqwrl:select\\(\\?(.+)\\)");
		Matcher m = p.matcher(query);
		if(!m.find())
			throw new InvalidQueryOntologyException(query);
		return m.group(1);
	}
	
	private static ReentrantLock vowlLock = new ReentrantLock();
	
	public String toVOWLString() throws IOException {
		vowlLock.lock();
		try {
			String owl = this.toString();
			BufferedWriter writer;
			String owlFilename = "knowledge-base.owl";
			String vowlFilename = "knowledge-base.json";
			writer = new BufferedWriter(new FileWriter(owlFilename));
		    writer.write(owl);
			writer.close();
			Process process = Runtime.getRuntime().exec("java -jar owl2vowl.jar -file " + owlFilename);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s = null;
			while ((s = stdInput.readLine()) != null) {
			    System.out.println(s);
			}
			process.waitFor();
			String vowl = new String(Files.readAllBytes(Paths.get(vowlFilename)));
			return vowl;
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			vowlLock.unlock();
		}
	}
	
	private boolean isValidName(String name) {
		return !name.contains(" ");
	}
}
