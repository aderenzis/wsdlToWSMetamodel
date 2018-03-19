package edu.uncoma.fai.WsdlToSoaML.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.giisco.SoaML.metamodel.ArrayType;
import edu.giisco.SoaML.metamodel.Attribute;
import edu.giisco.SoaML.metamodel.ComplexType;
import edu.giisco.SoaML.metamodel.Input;
import edu.giisco.SoaML.metamodel.Interface;
import edu.giisco.SoaML.metamodel.Operation;
import edu.giisco.SoaML.metamodel.Output;
import edu.giisco.SoaML.metamodel.Parameter;
import edu.giisco.SoaML.metamodel.SimpleType;
import edu.giisco.SoaML.metamodel.Type;

public class WsdlToSoaML {

	// llegado el momento las clases pueden ser Input y Output,
	// pero por una cuestión de uniformidad y de que a esta altura
	// no se sabe si es Input u Output
	private static HashMap<String, Input> inputOutputContainer = new HashMap<>();

	public static Interface createSoaMLInterface(String wsdlPath) throws ParserConfigurationException, SAXException, IOException {
		Interface soaMLInterface = new Interface();
		// "C:\\Users\\lenovo1\\Desktop\\absolutedrinks.wsdl"
			File inputFile = new File(wsdlPath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputFile);
			doc.getDocumentElement().normalize();
//			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

			Element root = doc.getDocumentElement();
			NodeList childs = root.getChildNodes();
			soaMLInterface.setName(inputFile.getName());
//			System.out.println("Name interface: " + soaMLInterface.getName());
			ArrayList<Operation> soaMLOperations = new ArrayList<Operation>();
			soaMLInterface.setOperations(soaMLOperations);

			for (int i = 0; i < childs.getLength(); i++) {
				Node child = childs.item(i);
//				System.out.println(i + "  " + child.getNodeName());
				if (child.getNodeName().equalsIgnoreCase("types")) {
//					System.out.println("entreeeeeeeeeeeeeeeeeeeeeeeeeee" + i);
					NodeList types = child.getChildNodes();
					for (int j = 0; j < types.getLength(); j++) {
						Node type = types.item(j);
//						System.out.println("probando para ver donde estoy: " + type.getNodeName());
						if (!type.getNodeName().equalsIgnoreCase("#text")) {
							NodeList elements = type.getChildNodes();
							for (int h = 1; h < elements.getLength(); h++) {
								Node element = elements.item(h);
								if (element.getNodeType() == Node.ELEMENT_NODE) {
//									System.out.println("*** " + element.getNodeName() + " ");
									Element realElement = (Element) element;
//									System.out.println(realElement.getAttribute("name"));
									// Type soaMLType =
									// WsdlToSoaML.getType(element);
									Input soaMLInput = WsdlToSoaML.createSoaMLInputOutput(element);
									inputOutputContainer.put(soaMLInput.getName(), soaMLInput);
//									System.out.println(soaMLInput.toString());
								}
							}
						}
					}
				} else {// aca me quedé estoy por implementar la parte de
						// operation que tienen input/output/fault
					if (child.getNodeName().equalsIgnoreCase("interface")) {
						NodeList operations = child.getChildNodes();
						// estoy parado en "operation"
						for (int j = 0; j < operations.getLength(); j++) {
							Node nodeSoaMLOperation = operations.item(j);
//							System.out.println("Entrando en la parte nueva " + nodeSoaMLOperation.getNodeName());
							if (!nodeSoaMLOperation.getNodeName().equalsIgnoreCase("#text")) {
								Operation soaMLOperation = new Operation();
//								System.out.print("*** " + nodeSoaMLOperation.getNodeName() + " ");
								Element realOperation = (Element) nodeSoaMLOperation;
								soaMLOperation.setName(realOperation.getAttribute("name"));
								// tengo que evaluar si lo que sigue es un input o un output
								// si es un input asociarlo y si es un output
								// crearlo en base al input y luego asociarlo
								NodeList listOfInputOrOutPut = nodeSoaMLOperation.getChildNodes();
								for (int k = 0; k < listOfInputOrOutPut.getLength(); k++) {
									Node inputOrOutput = listOfInputOrOutPut.item(k);
									if (inputOrOutput != null && !inputOrOutput.getNodeName().equalsIgnoreCase("#text")) {
										if (inputOrOutput.getNodeName().equalsIgnoreCase("input")) {
											Element elementInput = (Element) inputOrOutput;
											String nameInput = elementInput.getAttribute("messageLabel");
											String nameElement = elementInput.getAttribute("element");
											if (nameElement.contains(":"))
												nameElement = nameElement.substring(nameElement.indexOf(":") + 1);
											Input soaMLInput = inputOutputContainer.get(nameElement);
											// seteo el nombre verdadero del input
											soaMLInput.setName(nameInput);
											soaMLOperation.setInput(soaMLInput);
//											System.out.println("probando para input" + soaMLInput.toString());
										} else if (inputOrOutput.getNodeName().equalsIgnoreCase("output")) {
											Element elementOutput = (Element) inputOrOutput;
											String nameOutput = elementOutput.getAttribute("messageLabel");
											String nameElement = elementOutput.getAttribute("element");
											if (nameElement.contains(":"))
												nameElement = nameElement.substring(nameElement.indexOf(":") + 1);
											Input soaMLInput = inputOutputContainer.get(nameElement);
											// creo output con los datos que obtuve de input
											Output soaMLOutput = new Output();
											soaMLOutput.setParameters(soaMLInput.getParameters());
											// seteo el nombre verdadero del output
											soaMLOutput.setName(nameOutput);
											soaMLOperation.setOutput(soaMLOutput);
//											System.out.println("probando para output" + soaMLOutput.toString());
										}
									}

								}
								soaMLOperations.add(soaMLOperation);
//								System.out.println("***** " + soaMLOperations.toString());
							}
						}
					}

				}
			}
			System.out.println(soaMLInterface.toString());
		return soaMLInterface;
	}

	public static Input createSoaMLInputOutput(Node element) {
		Element realElement = (Element) element;
		Input soaMLInput = null;
		// siempre la primera vez que entra es complejo porque es un contenedor de muchos datos
		if (isComplexType(realElement)) {
			// ComplexType soamlComplexType = new ComplexType();
			soaMLInput = new Input();
			soaMLInput.setName(realElement.getAttribute("name"));
			NodeList childs = element.getChildNodes();
			if (childs != null) {
				// ArrayList<Attribute> attributes= new ArrayList<Attribute>();
				ArrayList<Parameter> soaMLParameters = new ArrayList<Parameter>();
				Node complexType = childs.item(1);
				// obtengo los hijos que solo tiene sequence
				Node sequence = complexType.getChildNodes().item(1);
				// son una serie de elements/complex types/arrays
				if (sequence != null) {
					NodeList complexTypeElements = sequence.getChildNodes();

					for (int i = 0; i < complexTypeElements.getLength(); i++) {
						if (!complexTypeElements.item(i).getNodeName().equalsIgnoreCase("#text")) {
							// Attribute soamlAttribute= new Attribute();
							Parameter soaMLParameter = new Parameter();
							Element element2 = (Element) complexTypeElements.item(i);
							soaMLParameter.setName(element2.getAttribute("name"));
							soaMLParameter.setType(getType(complexTypeElements.item(i)));
							soaMLParameters.add(soaMLParameter);
						}
					}
					soaMLInput.setParameters(soaMLParameters);
				}
			}
		}
		return soaMLInput;
	}

	public static Type getType(Node element) {
		Element realElement = (Element) element;
		String isArray = realElement.getAttribute("maxOccurs");
		if (isSimpleType(realElement)) {
			SimpleType tipoSimple = new SimpleType(returnTypeString(realElement));
			if (isArray.equalsIgnoreCase("unbounded")) {
				ArrayType array = new ArrayType();
				array.setContentType(tipoSimple);
				array.setName("ArrayOf" + tipoSimple.getName().substring(0, 1).toUpperCase()
						+ tipoSimple.getName().substring(1));
				return array;
			} else
				return tipoSimple;
		} else {
			if (isComplexType(realElement)) {
				// ya se que es complexType luego despues
				ComplexType soamlComplexType = new ComplexType();
				soamlComplexType.setName(realElement.getAttribute("name"));
				NodeList childs = element.getChildNodes();
				if (childs != null) {
					ArrayList<Attribute> attributes = new ArrayList<Attribute>();
					Node complexType = childs.item(1);
					// obtengo los hijos que solo tiene sequence
					Node sequence = complexType.getChildNodes().item(1);
					// son una serie de elements/complex types/arrays
					if (sequence != null) {
						NodeList complexTypeElements = sequence.getChildNodes();

						for (int i = 0; i < complexTypeElements.getLength(); i++) {
							if (!complexTypeElements.item(i).getNodeName().equalsIgnoreCase("#text")) {
								Attribute soamlAttribute = new Attribute();
								Element element2 = (Element) complexTypeElements.item(i);
								soamlAttribute.setName(element2.getAttribute("name"));
								soamlAttribute.setType(getType(complexTypeElements.item(i)));
								attributes.add(soamlAttribute);
							}
						}
						soamlComplexType.setAttributes(attributes);
					}
				}
				if (isArray.equalsIgnoreCase("unbounded"))
				{
					ArrayType array = new ArrayType();
					array.setContentType(soamlComplexType);
					array.setName("ArrayOf" + soamlComplexType.getName().substring(0, 1).toUpperCase()
							+ soamlComplexType.getName().substring(1));
					return array;
				} else
					return soamlComplexType;
			}
		}
		return null;
	}

	private static boolean isComplexType(Element element) {
		boolean retorno = false;
		NodeList childs = element.getChildNodes();
		if (childs != null) {
			Node complexType = childs.item(1);
			if (complexType != null && complexType.getNodeName().toLowerCase().contains("complextype"))
				retorno = true;
		}

		return retorno;
	}

	private static boolean isSimpleType(Element element) {
		// TODO Auto-generated method stub
		boolean retorno = false;
		String type = element.getAttribute("type");
		if (type != null) {
			if (type.contains(":"))
				type = type.substring(type.indexOf(":") + 1);
			if (SimpleType.isSimpleType(type))
				retorno = true;
		}
		return retorno;
	}

	private static String returnTypeString(Element element) {
		// TODO Auto-generated method stub
		String type = element.getAttribute("type");
		if (type != null)
			if (type.contains(":"))
				type = type.substring(type.indexOf(":") + 1);
		return type;
	}

}