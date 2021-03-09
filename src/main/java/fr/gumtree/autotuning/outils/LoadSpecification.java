package fr.gumtree.autotuning.outils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.gumtree.autotuning.domain.CategoricalParameterDomain;
import fr.gumtree.autotuning.domain.DoubleParameterDomain;
import fr.gumtree.autotuning.domain.IntParameterDomain;
import fr.gumtree.autotuning.domain.ParameterDomain;

/**
 * 
 * @author Matias Martinez
 *
 */
public class LoadSpecification {

	@SuppressWarnings("deprecation")
	public List<ParameterDomain> retrieveParameters(File fXmlFile) throws Exception {
		List<ParameterDomain> params = new ArrayList<>();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("parameter");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				String id = eElement.getAttribute("name");
				String type = eElement.getAttribute("type");

				String defaultValue = eElement.getAttribute("default");
				String minValue = eElement.getAttribute("min");
				String maxValue = eElement.getAttribute("max");
				String rangeValue = eElement.getAttribute("range");

				if (type == null || type.trim().isEmpty()) {
					throw new IllegalArgumentException("Missing type");
				}

				List<String> values = new ArrayList();
				NodeList items = eElement.getElementsByTagName("value");
				if (items.getLength() > 0) {

					for (int i = 0; i < items.getLength(); i++) {
						Node iNode = items.item(i);
						if (nNode.getNodeType() == Node.ELEMENT_NODE) {
							Element iElement = (Element) iNode;
							String valuei = iElement.getTextContent();
							values.add(valuei);
						}

					}
				}

				if ("string".equals(type.toString())) {

					String[] valuesS = new String[values.size()];
					for (int i = 0; i < values.size(); i++) {
						valuesS[i] = (values.get(i));
					}
					CategoricalParameterDomain c = new CategoricalParameterDomain(id, String.class, defaultValue,
							valuesS);
					params.add(c);
				} else {
					if ("integer".equals(type.toString())) {
						if (values.size() > 0) {
							Integer[] valuesS = new Integer[values.size()];
							for (int i = 0; i < values.size(); i++) {
								valuesS[i] = new Integer(values.get(i));
							}

							IntParameterDomain ip = new IntParameterDomain(id, Integer.class, new Integer(defaultValue),
									valuesS);
							params.add(ip);
						} else {
							IntParameterDomain ip = new IntParameterDomain(id, Integer.class, new Integer(defaultValue),
									new Integer(minValue), new Integer(maxValue), new Integer(rangeValue));
							params.add(ip);
						}
					} else if ("double".equals(type.toString())) {

						Double[] valuesS = new Double[values.size()];
						for (int i = 0; i < values.size(); i++) {
							valuesS[i] = new Double(values.get(i));
						}
						if (values.size() > 0) {
							DoubleParameterDomain ip = new DoubleParameterDomain(id, Double.class,
									new Double(defaultValue), valuesS);
							params.add(ip);
						} else {
							DoubleParameterDomain ip = new DoubleParameterDomain(id, Double.class,
									new Double(defaultValue), new Double(minValue), new Double(maxValue),
									new Double(rangeValue));
							params.add(ip);
						}
					}
				}

			}
		}

		return params;
	}

}
