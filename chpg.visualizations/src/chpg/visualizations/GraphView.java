package chpg.visualizations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import chpg.graph.Edge;
import chpg.graph.Graph;
import chpg.graph.Node;
import chpg.graph.schema.SchemaGraph;
import io.github.spencerpark.ijava.runtime.Display;

public class GraphView {
	
	private static boolean debug = false;
	
	public static void setDebug(boolean enabled) {
		debug = enabled;
	}
	
	public static final int DEFAULT_VERTICAL_SIZE = 600;
	public static final int DEFAULT_NAVIGATOR_NODES_SIZE = 20;
	public static final boolean DEFAULT_PANZOOM = true;
	
	public enum Layout {
		DAGRE, KLAY
	}
	
	public enum Menu {
		NONE, TEXT, WHEEL
	}
	
	public enum PanZoom {
		ENABLED, DISABLED
	}
	
	public enum Navigator {
		DEFAULT, ENABLED, DISABLED
	}

	public static void show(Graph graph) {
		show(graph, "", DEFAULT_VERTICAL_SIZE, true, Layout.DAGRE, Menu.NONE, PanZoom.ENABLED, Navigator.DEFAULT);
	}
	
	public static void show(Graph graph, String name) {
		show(graph, name, DEFAULT_VERTICAL_SIZE, true, Layout.DAGRE, Menu.NONE, PanZoom.ENABLED, Navigator.DEFAULT);
	}
	
	public static void show(Graph graph, String name, boolean extend) {
		show(graph, name, DEFAULT_VERTICAL_SIZE, extend, Layout.DAGRE, Menu.NONE, PanZoom.ENABLED, Navigator.DEFAULT);
	}
	
	public static void show(Graph graph, String name, boolean extend, int verticalSize) {
		show(graph, name, verticalSize, extend, Layout.DAGRE, Menu.NONE, PanZoom.ENABLED, Navigator.DEFAULT);
	}
	
	public static void show(Graph graph, String name, int verticalSize, boolean extend, Layout layout, Menu menu, PanZoom panzoom, Navigator navigator) {
		try {
			if(graph.isEmpty()) {
				Display.display("<html><style></style><body><h1>Empty graph</h1></body></html>", "text/html");
			} else {
				if(name == null) {
					name = "";
				}
				Graph containsGraph = graph.toGraph(graph.edges(SchemaGraph.Contains));
				containsGraph = graph.toGraph(graph.nodes()).induce(containsGraph);
				File graphViewerDirectory = Files.createTempDirectory("graph-viewer").toFile();
				if(debug) {
					System.out.println("DEBUG: " + graphViewerDirectory.getAbsolutePath());
				}
				for(String resource : Resources.getResources()) {
					File resourceFile = new File(graphViewerDirectory.getAbsolutePath() + File.separator + resource.replace("/", File.separator).replaceFirst("templates/", ""));
					resourceFile.getParentFile().mkdirs();
					if(resource.equals("templates/index.html")) {
						FileWriter fw = new FileWriter(resourceFile);
						String index = readResource("/" + resource + ".template");
						
						// graph name
						index = index.replace("TEMPLATE_GRAPH_NAME", name);
						
						// nodes
						StringBuilder nodeList = new StringBuilder();
						for(Node node : graph.nodes()) {
							if(nodeList.length() != 0) {
								nodeList.append(",");
							}
							String nodeName = node.hasName() ? node.getName() : "";
							// TODO: better escaping (probably use a json library)
							nodeName = nodeName.replace("\\", "\\\\");
							nodeName = nodeName.replace("\"", "\\\"");
							nodeName = nodeName.replace("\b", "\\b");
						    nodeName = nodeName.replace("\f", "\\f");
						    nodeName = nodeName.replace("\n", "\\n");
						    nodeName = nodeName.replace("\r", "\\r");
						    nodeName = nodeName.replace("\t", "\\t");
							Node parentNode = containsGraph.predecessors(node).one();
							if(!extend || parentNode == null) {
								nodeList.append("{ data: { id: \"" + "n" + node.getAddress() + "\", name: \"" + nodeName + "\" } }");
							} else {
								nodeList.append("{ data: { id: \"" + "n" + node.getAddress() + "\", name: \"" + nodeName + "\", parent: \"" + "n" + parentNode.getAddress() + "\" }, classes: ['container'] }");
							}
						}
						index = index.replace("TEMPLATE_NODES", nodeList.toString());
						
						// edges
						StringBuilder edgeList = new StringBuilder();
						for(Edge edge : graph.edges()) {
							if(!containsGraph.edges().contains(edge)) {
								String edgeName = edge.hasName() ? edge.getName() : "";
								// TODO: better escaping (probably use a json library)
								edgeName = edgeName.replace("\\", "\\\\");
								edgeName = edgeName.replace("\"", "\\\"");
								edgeName = edgeName.replace("\b", "\\b");
								edgeName = edgeName.replace("\f", "\\f");
								edgeName = edgeName.replace("\n", "\\n");
								edgeName = edgeName.replace("\r", "\\r");
								edgeName = edgeName.replace("\t", "\\t");
								edgeList.append(",{");
							    edgeList.append("data: {");
							    edgeList.append("id: \"" + edge.getAddress() + "\",");
							    edgeList.append("name: \"" + edgeName + "\",");
							    edgeList.append("source: \"" + "n" + edge.from().getAddress() + "\",");
							    edgeList.append("target: \"" + "n" + edge.to().getAddress() + "\"");
							    edgeList.append("}");
							    edgeList.append("}");
							}
						}
						index = index.replace("TEMPLATE_EDGES", edgeList.toString());
						
						// dagre layout
						if(layout.equals(Layout.DAGRE)) {
							// requires style, jquery, and options
							index = index.replace("TEMPLATE_LAYOUT_DAGRE", "<script src=\"js/dagre.min.js\"></script><script src=\"js/cytoscape-dagre.js\"></script>");
							
							StringBuilder dagreOptions = new StringBuilder();
							dagreOptions.append("name: \"dagre\",");

							dagreOptions.append("nodeSep: undefined,");
							dagreOptions.append("edgeSep: undefined,");
							dagreOptions.append("rankSep: undefined,");
							dagreOptions.append("rankDir: 'TB',");
							dagreOptions.append("ranker: undefined,");
							dagreOptions.append("minLen: function( edge ){ return 1; },");
							dagreOptions.append("edgeWeight: function( edge ){ return 1; },");
							
							index = index.replace("TEMPLATE_LAYOUT_OPTIONS_DAGRE", dagreOptions.toString());
						} else {
							index = index.replace("TEMPLATE_LAYOUT_DAGRE", "");
							index = index.replace("TEMPLATE_LAYOUT_OPTIONS_DAGRE", "");
						}
						
						// klay layout
						if(layout.equals(Layout.KLAY)) {
							// requires style, jquery, and options
							index = index.replace("TEMPLATE_LAYOUT_KLAY", "<script src=\"js/klay.min.js\"></script><script src=\"js/cytoscape-klay.js\"></script>");
							
							StringBuilder klayOptions = new StringBuilder();
							klayOptions.append("name: \"klay\",");
							
							klayOptions.append("klay: {");
							klayOptions.append("addUnnecessaryBendpoints: false,");
							klayOptions.append("aspectRatio: 1.6, ");
							klayOptions.append("borderSpacing: 20, ");
							klayOptions.append("compactComponents: false, ");
							klayOptions.append("crossingMinimization: 'LAYER_SWEEP',");
							klayOptions.append("cycleBreaking: 'GREEDY', ");
							klayOptions.append("direction: 'DOWN',");
							klayOptions.append("edgeRouting: 'ORTHOGONAL', ");
							klayOptions.append("edgeSpacingFactor: 0.5,");
							klayOptions.append("feedbackEdges: false, ");
							klayOptions.append("fixedAlignment: 'NONE', ");
							klayOptions.append("inLayerSpacingFactor: 1.0, ");
							klayOptions.append("layoutHierarchy: true,");
							klayOptions.append("linearSegmentsDeflectionDampening: 0.3, ");
							klayOptions.append("mergeEdges: false, ");
							klayOptions.append("mergeHierarchyCrossingEdges: true, ");
							klayOptions.append("nodeLayering:'NETWORK_SIMPLEX', ");
							klayOptions.append("nodePlacement:'BRANDES_KOEPF', ");
							klayOptions.append("randomizationSeed: 1, ");
							klayOptions.append("routeSelfLoopInside: false,");
							klayOptions.append("separateConnectedComponents: true,");
							klayOptions.append("spacing: 20, ");
							klayOptions.append("thoroughness: 7 ");
							klayOptions.append("},");
							klayOptions.append("priority: function( edge ){ return null; },");
							index = index.replace("TEMPLATE_LAYOUT_OPTIONS_KLAY", klayOptions.toString());
						} else {
							index = index.replace("TEMPLATE_LAYOUT_KLAY", "");
							index = index.replace("TEMPLATE_LAYOUT_OPTIONS_KLAY", "");
						}
						
						// general layout options
						StringBuilder generalLayoutOptions = new StringBuilder();
						generalLayoutOptions.append("fit: true,");
						generalLayoutOptions.append("padding: 30,");
						generalLayoutOptions.append("spacingFactor: undefined,");
						generalLayoutOptions.append("nodeDimensionsIncludeLabels: false,");
						generalLayoutOptions.append("animate: false,");
						generalLayoutOptions.append("animateFilter: function( node, i ){ return true; },");
						generalLayoutOptions.append("animationDuration: 500,");
						generalLayoutOptions.append("animationEasing: undefined,");
						generalLayoutOptions.append("boundingBox: undefined,");
						generalLayoutOptions.append("transform: function( node, pos ){ return pos; },");
						generalLayoutOptions.append("ready: function(){},");
						generalLayoutOptions.append("stop: function(){}");
						index = index.replace("TEMPLATE_LAYOUT_OPTIONS_GENERAL", generalLayoutOptions.toString());
						
						// text menu
						if(menu.equals(Menu.TEXT)) {
							// requires style, jquery, and options
							index = index.replace("TEMPLATE_STYLE_TEXT_CONTEXT", "<link href=\"css/cytoscape-context-menus.css\" rel=\"stylesheet\" type=\"text/css\" />");
							index = index.replace("TEMPLATE_JQUERY", "<script src=\"js/jquery-2.0.3.min.js\"></script>");
							index = index.replace("TEMPLATE_JS_TEXT_CONTEXT", "<script src=\"js/cytoscape-context-menus.js\"></script>");
							index = index.replace("TEMPLATE_OPTIONS_CONTEXT_TEXT", "// TODO: TEXT MENU OPTIONS");
						} else {
							index = index.replace("TEMPLATE_STYLE_TEXT_CONTEXT", "");
							index = index.replace("TEMPLATE_JS_TEXT_CONTEXT", "");
							index = index.replace("TEMPLATE_OPTIONS_CONTEXT_TEXT", "");
						}
						
						// wheel menu
						if(menu.equals(Menu.WHEEL)) {
							// requires font awesome, jquery, and options
							index = index.replace("TEMPLATE_STYLE_FONT_AWESOME", "<link href=\"font-awesome-4.0.3/css/font-awesome.min.css\" rel=\"stylesheet\" type=\"text/css\" />");
							index = index.replace("TEMPLATE_JQUERY", "<script src=\"js/jquery-2.0.3.min.js\"></script>");
							index = index.replace("TEMPLATE_JS_WHEEL_CONTEXT", "<script src=\"js/cytoscape-cxtmenu.js\"></script>");
							index = index.replace("TEMPLATE_OPTIONS_CONTEXT_WHEEL", "// TODO: WHEEL MENU OPTIONS");
						} else {
							index = index.replace("TEMPLATE_JS_WHEEL_CONTEXT", "");
							index = index.replace("TEMPLATE_OPTIONS_CONTEXT_WHEEL", "");
						}
						
						// panzoom
						if(panzoom.equals(PanZoom.ENABLED)) {
							// requires font awesome, style, jquery, and options
							index = index.replace("TEMPLATE_STYLE_FONT_AWESOME", "<link href=\"font-awesome-4.0.3/css/font-awesome.min.css\" rel=\"stylesheet\" type=\"text/css\" />");
							index = index.replace("TEMPLATE_STYLE_PANZOOM", "<link href=\"css/cytoscape.js-panzoom.css\" rel=\"stylesheet\" type=\"text/css\" />");
							index = index.replace("TEMPLATE_JQUERY", "<script src=\"js/jquery-2.0.3.min.js\"></script>");
							index = index.replace("TEMPLATE_JS_PANZOOM_CONTEXT", "<script src=\"js/cytoscape-panzoom.js\"></script>");
							StringBuilder panzoomOptions = new StringBuilder();
							panzoomOptions.append("cy.panzoom({");
							panzoomOptions.append("});");
							index = index.replace("TEMPLATE_OPTIONS_PANZOOM", panzoomOptions.toString());
						} else {
							index = index.replace("TEMPLATE_STYLE_PANZOOM", "");
							index = index.replace("TEMPLATE_JS_PANZOOM_CONTEXT", "");
							index = index.replace("TEMPLATE_OPTIONS_PANZOOM", "");
						}
						
						// navigator
						if(navigator.equals(Navigator.ENABLED) || (navigator.equals(Navigator.DEFAULT) && graph.nodes().size() >= DEFAULT_NAVIGATOR_NODES_SIZE)) {
							// requires font awesome, style, jquery, and options
							index = index.replace("TEMPLATE_STYLE_FONT_AWESOME", "<link href=\"font-awesome-4.0.3/css/font-awesome.min.css\" rel=\"stylesheet\" type=\"text/css\" />");
							index = index.replace("TEMPLATE_STYLE_NAVIGATOR", "<link href=\"css/cytoscape.js-navigator.css\" rel=\"stylesheet\" type=\"text/css\" />");
							index = index.replace("TEMPLATE_JQUERY", "<script src=\"js/jquery-2.0.3.min.js\"></script>");
							index = index.replace("TEMPLATE_JS_NAVIGATOR_CONTEXT", "<script src=\"js/cytoscape-navigator.js\"></script>");
							StringBuilder navigatorOptions = new StringBuilder();
							navigatorOptions.append("cy.navigator({");
							navigatorOptions.append("});");
							index = index.replace("TEMPLATE_OPTIONS_NAVIGATOR", navigatorOptions.toString());
						} else {
							index = index.replace("TEMPLATE_STYLE_NAVIGATOR", "");
							index = index.replace("TEMPLATE_JS_NAVIGATOR_CONTEXT", "");
							index = index.replace("TEMPLATE_OPTIONS_NAVIGATOR", "");
						}
						
						// remove unused dependencies
						index = index.replace("TEMPLATE_STYLE_FONT_AWESOME", "");
						index = index.replace("TEMPLATE_JQUERY", "");
						
						fw.write(index);
						fw.close();
					} else {
						InputStream inputStream = GraphView.class.getResourceAsStream("/" + resource); 
						if(inputStream == null) {
							throw new IOException("Unable to access resource at path: " + resource);
						}
						Files.copy(inputStream, resourceFile.toPath());
					}
				}
				// work around for unresolved security policies: https://stackoverflow.com/a/52338192/475329
				Display.display("<html><iframe src='" + graphViewerDirectory.getAbsolutePath() + File.separator 
						+ "index.html' width=\"100%\", height=\"" + verticalSize + "px\" frameBorder=\"0\"></iframe></html>", "text/html");


	public static JsonArray createNodeJson(GraphElementSet<Node> nodes, Graph containsGraph, boolean extend) {
		// Create a list of JSON representations of the graph nodes
		JsonArray nodesArray = new JsonArray();

		//start project node
		Node tempNode = nodes.iterator().next();
		String tempNodePath = nodeGetPath(tempNode);
		String projectName = nodeGetProjectName(tempNodePath);
		
		JsonObject projectNodeObject = new JsonObject();
		
		JsonObject projectDataObject = new JsonObject();
		projectDataObject.addProperty("id", "projectNode");
		projectDataObject.addProperty("name", projectName);
		projectDataObject.addProperty("shape", "round-rectangle");
		
		JsonArray classesJson = new JsonArray();
		classesJson.add("container");
		projectNodeObject.add("classes", classesJson);
				
		projectNodeObject.add("data", projectDataObject);

		nodesArray.add(projectNodeObject);
		//end project node

		//start function (file) node
		String tempFileName = nodeGetFileName(tempNodePath);
		
		JsonObject functionNodeObject = new JsonObject();
		
		JsonObject functionDataObject = new JsonObject();
		projectDataObject.addProperty("id", tempFileName);
		projectDataObject.addProperty("name", tempFileName);
		projectDataObject.addProperty("shape", "round-rectangle");
		projectDataObject.addProperty("parent", "projectNode");
		classesJson = new JsonArray();
		classesJson.add("container");
		projectNodeObject.add("classes", classesJson);
				
		projectNodeObject.add("data", projectDataObject);

		nodesArray.add(functionNodeObject);
		//end function (file) node
		
		String lastFunctionName = tempFileName;
		
		for (Node node : nodes) {
			// Get the escaped name of the nodes if it has a name
			String nodeName = node.hasName() ? escapeSchemaChars(node.getName()) : "";
			
			String nodePath = nodeGetPath(node);
			String fileName = nodeGetFileName(nodePath);
			
			// Get the parent of the node if it exists
			// NOTE: this is looking for the predecessor, not the container node
			//Node parentNode = containsGraph.predecessors(node).one();
			
			//Node parentNode = fileName;
			
			// Create the JSON for the node
			JsonObject nodeObject = new JsonObject();

			// Add the basic data attribute
			JsonObject dataObject = new JsonObject();
			dataObject.addProperty("id", "n" + node.getAddress());
			dataObject.addProperty("name", nodeName);

			// Set styles dependent on node type
			if (node.tags().contains("XCSG.ControlFlowLoopCondition")
					|| node.tags().contains("XCSG.ControlFlowIfCondition")) {
				// Set diamond shape for loop conditions and if statements
				dataObject.addProperty("shape", "diamond");
				// Fix width issue for diamond nodes
				dataObject.addProperty("width", nodeName.length() * 10);
			} else {
				// Set default node style
				dataObject.addProperty("shape", "round-rectangle");
				dataObject.addProperty("width", "label");
			}

			// If extend is set to true and this node has, add parent attribute to data and
			// add classes attribute
//			if (extend && parentNode == null) {
//				dataObject.addProperty("parent", "n" + node.getAddress());
//
//				//JsonArray classesJson = new JsonArray();
//				classesJson = new JsonArray();
//				classesJson.add("container");
//				nodeObject.add("classes", classesJson);
//			}			
			dataObject.addProperty("parent", lastFunctionName);

			//JsonArray classesJson = new JsonArray();
			classesJson = new JsonArray();
			
			classesJson.add("container");
			nodeObject.add("classes", classesJson);
			nodeObject.add("data", dataObject);

			// Add the node to the array of nodes
			nodesArray.add(nodeObject);
		}

		return nodesArray;
	}

	public static JsonArray createEdgesJson(GraphElementSet<Edge> edges, Graph containsGraph) {
		// Create a list of JSON representations of the graph edges
		JsonArray edgesArray = new JsonArray();

		for (Edge edge : edges) {
			// If the edge is in containsGraph, add it to the list of edges
			if (!containsGraph.edges().contains(edge)) {
				// Get the escaped name of the edge if it has a name
				String edgeName = edge.hasName() ? escapeSchemaChars(edge.getName()) : "";

				// Create the JSON for the edge
				JsonObject edgeObject = new JsonObject();

				// Create the data attribute for the edge and add it to edgeJson
				JsonObject dataJson = new JsonObject();
				dataJson.addProperty("id", edge.getAddress());
				dataJson.addProperty("name", edgeName);
				dataJson.addProperty("source", "n" + edge.from().getAddress());
				dataJson.addProperty("target", "n" + edge.to().getAddress());
				edgeObject.add("data", dataJson);

				// Add the edge to the array of edges
				edgesArray.add(edgeObject);

			}
		} catch (Throwable t) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			Display.display("<html><body><h1>Error could not display graph</h1><h2>" + t.getMessage() + "</h2><pre>" + sw.toString() + "</pre></body></html>", "text/html");
		}
	}
	

	//Gets the path of a node
	private static String nodeGetPath(Node node) {
		
		String sourceAttribute = "XCSG.ModelElement.sourceCorrespondence";
		
		if(node.attributes().containsKey(sourceAttribute)) {
			String nodePath = (String) node.attributes().get(sourceAttribute);
			return nodePath;
		}
		else {
			return null;
		}
	}
	
	//Parses a node's path to find the name of the file it is in
	private static String nodeGetFileName(String nodePath) {
		
		int end = nodePath.indexOf(",");
		nodePath = nodePath.substring(0,end);
		String filename = new File(nodePath).getName();
			
		return filename;
		
	}
	//Parses a node's path to find the name of the project it is in
	//This assumes the file name is the name of the function the node is in. Need a better solution.
	private static String fileGetProjectName(String nodePath) {

		int start = nodePath.indexOf("/");
		String temp = nodePath.substring(start+1,nodePath.length());
		int end = temp.indexOf("/");
		String projectName = temp.substring(0,end);
		
		return projectName;
		
	}
	
	//Parses a node's path to find the name of the project it is in
	//This assumes that the every node in the graph is in the same project
	private static String nodeGetProjectName(String nodePath) {

		int start = nodePath.indexOf("/");
		String temp = nodePath.substring(start+1,nodePath.length());
		int end = temp.indexOf("/");
		String projectName = temp.substring(0,end);
		
		return projectName;
		
	}
	

	private static String readResource(String path) throws IOException {
		InputStream inputStream = GraphView.class.getResourceAsStream(path); 
		if(inputStream == null) {
			throw new IOException("Unable to access resource at path: " + path);
		}
		return convertStreamToString(inputStream);
	}
	
	private static String convertStreamToString(InputStream is) throws IOException {
	   StringBuilder sb = new StringBuilder(2048);
	   char[] read = new char[128];
	   try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
	     for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i));
	   }
	   return sb.toString();
	}
	
}
