package pt.iul.ista.ads.owl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import pt.iul.ista.ads.models.ClassDetailResponseModel;
import pt.iul.ista.ads.models.ClassesResponseModel;
import pt.iul.ista.ads.models.RelationshipDetailResponseModel;

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
			return baos.toString(charset);
		} catch(OWLOntologyStorageException e) {
			e.printStackTrace();
			return "Error in OWL generation";
		}
	}

	public void addClass(String className, String superclassName) throws ClassNotFoundOntologyException, ClassAlreadyExistsOntologyException {
		checkClassNotExists(className);
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
	
	private void checkClassNotExists(String className) throws ClassAlreadyExistsOntologyException {
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
			OWLClassExpression classExpression = EntitySearcher.getTypes(individual, ontology).findFirst().get();
			if(owlEntityToString(classExpression.asOWLClass()).equals(className))
				res.getIndividuals().add(owlEntityToString(individual));
		}
		
		return res;
	}
	
	public void alterClass(String className, String newClassName, String newSuperclassName) throws ClassNotFoundOntologyException, ClassAlreadyExistsOntologyException {
		// validar os parâmetros primeiro
		checkClassExists(className);
		if(newClassName != null) {
			checkClassNotExists(newClassName);
		}
		if(newSuperclassName != null) {
			checkClassExists(newSuperclassName);
		}
		
		// parâmetros validados; efetuar alterações
		OWLClass cls = factory.getOWLClass(":#" + className, prefixManager);
		if(newClassName != null) {
			OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(ontology));
			OWLClass newCls = factory.getOWLClass(":#" + newClassName, prefixManager);
			manager.applyChanges(renamer.changeIRI(cls.getIRI(), newCls.getIRI()));
			cls = newCls; // alterar cls para que código abaixo se refira ao novo nome da classe
		}
		if(newSuperclassName != null) {
			for(OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSubClass(cls))
				ontology.removeAxiom(axiom);
			
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
	
	public List<String> listRelationships() {
		return ontology.getObjectPropertiesInSignature().stream()
				.map(property -> owlEntityToString(property))
				.collect(Collectors.toList());
	}
	
	public List<RelationshipDetailResponseModel.RelationshipModel> detailRelationship(String relationship) throws RelationshipNotFoundOntologyException {
		checkRelationshipExists(relationship);
		List<RelationshipDetailResponseModel.RelationshipModel> res = new ArrayList<RelationshipDetailResponseModel.RelationshipModel>();
		OWLObjectProperty property = factory.getOWLObjectProperty(":#" + relationship, prefixManager);
		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
			if(axiom.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)
					&& axiom.getObjectPropertiesInSignature().contains(property)) {
				RelationshipDetailResponseModel.RelationshipModel relationshipModel = new RelationshipDetailResponseModel.RelationshipModel();
				OWLObjectPropertyAssertionAxiom x = (OWLObjectPropertyAssertionAxiom) axiom;
				relationshipModel.setIndividual1(owlEntityToString((OWLNamedIndividual) x.getSubject()));
				relationshipModel.setIndividual2(owlEntityToString((OWLNamedIndividual) x.getObject()));
				res.add(relationshipModel);
			}
		}
		return res;
	}
	
	public void createRelationship(String relationship, String className1, String className2) throws ClassNotFoundOntologyException, RelationshipAlreadyExistsOntologyException {
		checkRelationshipNotExists(relationship);
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

	private void checkRelationshipNotExists(String relationship) throws RelationshipAlreadyExistsOntologyException {
		if(ontology.containsObjectPropertyInSignature(IRI.create(documentIRI.toString() + "#" + relationship)))
			throw new RelationshipAlreadyExistsOntologyException(relationship);
	}
	
	private void checkRelationshipExists(String relationship) throws RelationshipNotFoundOntologyException {
		if(!ontology.containsObjectPropertyInSignature(IRI.create(documentIRI.toString() + "#" + relationship)))
			throw new RelationshipNotFoundOntologyException(relationship);
	}
	
	public void alterRelationship(String relationship, String newRelationshipName, String className1, String className2) throws RelationshipNotFoundOntologyException, RelationshipAlreadyExistsOntologyException, ClassNotFoundOntologyException {
		// validar os parâmetros primeiro
		checkRelationshipExists(relationship);
		if(newRelationshipName != null)
			checkRelationshipNotExists(newRelationshipName);
		if(className1 != null)
			checkClassExists(className1);
		if(className2 != null)
			checkClassExists(className2);
		
		// parâmetros validados; efetuar alterações
		OWLObjectProperty property = factory.getOWLObjectProperty(":#" + relationship, prefixManager);
		if(newRelationshipName != null) {
			OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(ontology));
			OWLObjectProperty newProperty = factory.getOWLObjectProperty(":#" + newRelationshipName, prefixManager);
			manager.applyChanges(renamer.changeIRI(property.getIRI(), newProperty.getIRI()));
			property = newProperty; // reassign para código abaixo se referir à nova property
		}
		if(className1 != null) {
			for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
				if(axiom.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN)
						&& axiom.getObjectPropertiesInSignature().contains(property)) {
					ontology.removeAxiom(axiom);
					break;
				}
			}
			OWLClass cls = factory.getOWLClass(":#" + className1, prefixManager);
			OWLAxiom domainAxiom = factory.getOWLObjectPropertyDomainAxiom(property, cls);
			manager.addAxiom(ontology, domainAxiom);
		}
		if(className2 != null) {
			for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()) {
				if(axiom.isOfType(AxiomType.OBJECT_PROPERTY_RANGE)
						&& axiom.getObjectPropertiesInSignature().contains(property)) {
					ontology.removeAxiom(axiom);
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
	
}
