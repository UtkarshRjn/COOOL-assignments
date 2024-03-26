import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;

import soot.*;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JNewExpr;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.*;
import soot.jimple.*;
import soot.jimple.internal.JArrayRef;
import soot.jimple.StaticFieldRef;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;
import soot.Unit;

class ObjectNode {
    private String line;
    private Boolean esp;

    public ObjectNode(String line) {
        this.line = line;
        this.esp = (line == "return");
    }

    public ObjectNode(String line, Boolean esp) {
        this.line = line;
        this.esp = esp;
    }

    public String getLine() {
        return line;
    }

    public void setEscape(Boolean esp){
        this.esp = esp;
    }

    public Boolean escape(){
        return esp;
    }
}

class PointsToGraph {

    private Map<String, ObjectNode> nodes; // All the objectNodes
    private Map<String, Map<String, List<ObjectNode>>> edges; // Edges among objectNodes

    public PointsToGraph() {
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
    }

    public void addNode(ObjectNode node) {
        nodes.put(node.getLine(), node);
        edges.put(node.getLine(), new HashMap<>());
    }

    public void addEdge(String sourceKey, String fieldKey, List<ObjectNode> nodesArg) {
        
        if (!nodes.containsKey(sourceKey)) {
            ObjectNode varNode = new ObjectNode(sourceKey);
            addNode(varNode);
        }
        
        if(edges.get(sourceKey).containsKey(fieldKey)){
            edges.get(sourceKey).get(fieldKey).addAll(nodesArg);
        }else{
            edges.get(sourceKey).put(fieldKey, nodesArg);
        }
        
        for(ObjectNode node : nodesArg){
            if(!nodes.containsKey(node.getLine())){
                addNode(node);
            }
        }
    
    }

    // Given a sourceKey (Needs to be line No or param Name) and fieldKey (edge Label f,g, ""), return all the objectNodes
    public List<ObjectNode> getObjects(String sourceKey, String fieldKey) {
        return edges.get(sourceKey).get(fieldKey);
    }

    public ObjectNode getNode(String key) {
        return nodes.get(key);
    }

    public void union(PointsToGraph otherGraph) {

        // Merge nodes
        otherGraph.nodes.forEach((key, value) -> {
            if (!this.nodes.containsKey(key)) {
                this.addNode(value);
            }else if(this.nodes.get(key).escape() != value.escape()){
                this.nodes.get(key).setEscape(true);
            }
        });

        // Merge edges
        otherGraph.edges.forEach((sourceKey, fieldMap) -> {
            fieldMap.forEach((fieldKey, nodeList) -> {
                if (this.edges.containsKey(sourceKey)) {
                    if (this.edges.get(sourceKey).containsKey(fieldKey)) {
                        List<ObjectNode> existingNodeList = this.edges.get(sourceKey).get(fieldKey);
                        nodeList.forEach(newNode -> {
                            // Check if a node with the same line already exists in the List
                            boolean exists = existingNodeList.stream().anyMatch(existingNode -> existingNode.getLine().equals(newNode.getLine()));
                            if (!exists) {                            
                                this.edges.get(sourceKey).get(fieldKey).add(newNode);
                            }
                        });
                    } else {
                        this.edges.get(sourceKey).put(fieldKey, new ArrayList<>(nodeList));
                    }
                

                } else {
                    this.edges.put(sourceKey, new HashMap<>(Map.of(fieldKey, new ArrayList<>(nodeList))));
                }
            });
        });

    }

    public boolean isEqual(PointsToGraph otherGraph) {
        // Check if the number of nodes is the same
        if (this.nodes.size() != otherGraph.nodes.size()) {
            return false;
        }
    
        // Check if the number of edges is the same
        if (this.edges.size() != otherGraph.edges.size()) {
            return false;
        }
    
        // Check if all nodes in this graph are present in the other graph
        for (String key : this.nodes.keySet()) {
            if (!otherGraph.nodes.containsKey(key)) {
                return false;
            }
        }
    
        // Check if all edges in this graph are present in the other graph
        for (String sourceKey : this.edges.keySet()) {
            if (!otherGraph.edges.containsKey(sourceKey)) {
                return false;
            }
    
            Map<String, List<ObjectNode>> thisFieldMap = this.edges.get(sourceKey);
            Map<String, List<ObjectNode>> otherFieldMap = otherGraph.edges.get(sourceKey);
    
            if (thisFieldMap.size() != otherFieldMap.size()) {
                return false;
            }
    
            for (String fieldKey : thisFieldMap.keySet()) {
                if (!otherFieldMap.containsKey(fieldKey)) {
                    return false;
                }
    
                List<ObjectNode> thisNodeList = thisFieldMap.get(fieldKey);
                List<ObjectNode> otherNodeList = otherFieldMap.get(fieldKey);
    
                if (thisNodeList.size() != otherNodeList.size()) {
                    return false;
                }
    
                // Check if all nodes in the list are the same
                for (ObjectNode node : thisNodeList) {
                    if (!otherNodeList.contains(node)) {
                        return false;
                    }
                }
            }
        }
    
        return true;
    }

    public List<ObjectNode> dfs() {
        List<String> visited = new ArrayList<>();
        List<ObjectNode> numericalNodes = new ArrayList<>();
        List<ObjectNode> startNodes = new ArrayList<>();

        for(String key : nodes.keySet()){
            if(nodes.get(key).escape()){
                startNodes.add(nodes.get(key));
            }
        }

        for (ObjectNode startNode : startNodes) {
            dfsRecursive(startNode, visited, numericalNodes);
        }

        return numericalNodes;
    }

    private void dfsRecursive(ObjectNode currentNode, List<String> visited, List<ObjectNode> numericalNodes) {
        String currentNodeKey = currentNode.getLine();
        visited.add(currentNodeKey);

        if (isNumerical(currentNode.getLine())) {
            numericalNodes.add(currentNode);
        }

        Map<String, List<ObjectNode>> neighbors = edges.get(currentNodeKey);
        if (neighbors != null) {
            for (List<ObjectNode> nodeList : neighbors.values()) {
                for (ObjectNode neighbor : nodeList) {
                    if (!visited.contains(neighbor.getLine())) {
                        dfsRecursive(neighbor, visited, numericalNodes);
                    }
                }
            }
        }
    }

    private boolean isNumerical(String line) {
        // Assuming a simple check if the line consists only of digits
        return line.matches("\\d+");
    }

    public void print() {
        System.out.println("Nodes:");
        for (String nodeKey : nodes.keySet()) {
            ObjectNode node = nodes.get(nodeKey);
            System.out.println("Node: " + nodeKey + ", Escape: " + node.escape());
        }

        System.out.println("\nEdges:");
        for (String sourceKey : edges.keySet()) {
            Map<String, List<ObjectNode>> sourceEdges = edges.get(sourceKey);
            for (String fieldKey : sourceEdges.keySet()) {
                List<ObjectNode> fieldNodes = sourceEdges.get(fieldKey);
                System.out.println("Source Node: " + sourceKey + ", Field Key: " + fieldKey + ", Connected Nodes:");
                for (ObjectNode connectedNode : fieldNodes) {
                    System.out.println("- " + connectedNode.getLine());
                }
            }
        }
    }

}



public class EscapeAnalysisTransformer extends BodyTransformer {

    private Map<String, List<String>> EscapingObjects = new HashMap<>();

    private static String[] extractBaseAndField(Value expr) {
        
        String base = "";
        String field = "";

        // System.out.println(expr.getClass().getName());
        if (expr instanceof JInstanceFieldRef) {
            JInstanceFieldRef instanceFieldRef = (JInstanceFieldRef) expr;
            SootFieldRef fieldRef = instanceFieldRef.getFieldRef();
    
            // Access field information
            field = fieldRef.name();
            base = instanceFieldRef.getBase().toString();
        } else if (expr instanceof JimpleLocal) {
            JimpleLocal jimpleLocal = (JimpleLocal) expr;
            base = jimpleLocal.getName();
            // System.out.println("JimpleLocal: " + base);
        } else if(expr instanceof StaticFieldRef){
            StaticFieldRef staticFieldRef = (StaticFieldRef) expr;
            SootFieldRef fieldRef = staticFieldRef.getFieldRef();
    
            base = fieldRef.name();
        } else if(expr instanceof JArrayRef){
            JArrayRef arrayRef = (JArrayRef) expr;
            base = arrayRef.getBase().toString();
            // field = arrayRef.getIndex().toString();
            field = "[]";
        }else if(expr instanceof JCastExpr){
            JCastExpr castExpr = (JCastExpr) expr;
            base = castExpr.getOp().toString();
        }

        return new String[]{base, field};
    }

    private static void escapeArgs(List<Value> args, PointsToGraph inPTG){
                
        for(Value arg : args){
            String[] rPair = extractBaseAndField(arg);
            String rBase = rPair[0];
            String rField = rPair[1];
            
            List<ObjectNode> TargetObjectNodes = new ArrayList<>();
            if(!rField.isEmpty()){

                // We assume here that lhs would only have a base and no field
                List<ObjectNode> IntermediateObjectNodes = inPTG.getObjects(rBase, "");

                for(ObjectNode intermediateObjectNode : IntermediateObjectNodes){

                    if(inPTG.getObjects(intermediateObjectNode.getLine(), rField) == null)
                        continue;

                    TargetObjectNodes.addAll(inPTG.getObjects(intermediateObjectNode.getLine(), rField));   
                }
                
            }else{
                TargetObjectNodes.addAll(inPTG.getObjects(rBase, ""));
            }
            
            for(ObjectNode targetObjectNode : TargetObjectNodes){
                targetObjectNode.setEscape(true);
            }
            
        }

    }

    private static void analyseStatement(Unit u, PointsToGraph inPTG, PointsToGraph outPTG, SootClass sootClass){
        
        outPTG.union(inPTG);

        // type of u
        // System.out.println( "Type of u is " + u.getClass().getName());

        // x.f.g = y

        // $r0 = x
        // $r1 = x.f
        // $r2 = y
        // $r1.g = $2

        // x.f = y -> 
        // x = y.f
        // x = y

        if(u instanceof JIdentityStmt){
            JIdentityStmt stmt = (JIdentityStmt) u;
            Value rhs = stmt.getRightOp();
            Value lhs = stmt.getLeftOp();

            String[] lPair = extractBaseAndField(lhs);
            String lBase = lPair[0];
            String lField = lPair[1];

            String[] rPair = extractBaseAndField(rhs);
            String rBase = rPair[0];
            String rField = rPair[1];

            // System.out.println("LHS is " + lBase + " and the field reference is: " + lField);
            // System.out.println("RHS is " + rBase + " and the field reference is: " + rField);
            // System.out.println("");

            // Print the type of the right hand side
            
            
            if(rhs instanceof ThisRef){
            
                // assign all node attached to r2 to escape
                List<ObjectNode> TargetObjectNodes = inPTG.getObjects(lBase, "");
                for(ObjectNode targetObjectNode : TargetObjectNodes){
                    targetObjectNode.setEscape(true);
                }

                for(SootField field : sootClass.getFields()){
                    String fieldName = field.getName();
                    if(!field.isStatic()){
                        
                        List<ObjectNode> dummyObjectList = new ArrayList<>();
                        dummyObjectList.add(new ObjectNode("gdummy" + fieldName));
                        
                        outPTG.addEdge("dummy" + lBase, fieldName, dummyObjectList);
                    }
                }

            }

            // System.out.println("Type of lhs is " + lhs.getClass().getName());
            // System.out.println("Type of rhs is " + rhs.getClass().getName());

        }else if(u instanceof JAssignStmt){

            JAssignStmt stmt = (JAssignStmt) u;
            Value rhs = stmt.getRightOp();
            Value lhs = stmt.getLeftOp();

            String[] lPair = extractBaseAndField(lhs);
            String lBase = lPair[0];
            String lField = lPair[1];

            String[] rPair = extractBaseAndField(rhs);
            String rBase = rPair[0];
            String rField = rPair[1];

            // System.out.println("LHS is " + lBase + " and the field reference is: " + lField);
            // System.out.println("RHS is " + rBase + " and the field reference is: " + rField);
            // System.out.println("");

            // Print the type of the right hand side
            // System.out.println("Type of lhs is " + lhs.getClass().getName());
            // System.out.println("Type of rhs is " + rhs.getClass().getName());

            if (rhs instanceof JNewExpr || rhs instanceof JNewArrayExpr) {
                int line = u.getJavaSourceStartLineNumber();
                ObjectNode NewNode = new ObjectNode(String.valueOf(line));

                if(!lField.isEmpty()){
                    List<ObjectNode>  SourceObjectNodes = inPTG.getObjects(lBase, "");
                    
                    for(ObjectNode sourceObjectNode : SourceObjectNodes){

                        List<ObjectNode>  NewNodeList = new ArrayList<>();
                        NewNodeList.add(NewNode);
                        outPTG.addEdge(sourceObjectNode.getLine(), lField, NewNodeList);
                    }

                }else{

                    List<ObjectNode>  NewNodeList = new ArrayList<>();
                    NewNodeList.add(NewNode);
                    outPTG.addEdge(lBase, "", NewNodeList);
                }

            }
            else if(rhs instanceof JInstanceFieldRef || rhs instanceof JimpleLocal || rhs instanceof StaticFieldRef || rhs instanceof JArrayRef || rhs instanceof JCastExpr){
            
                if(!rField.isEmpty()){

                    // We assume here that lhs would only have a base and no field
                    List<ObjectNode> IntermediateObjectNodes = inPTG.getObjects(rBase, "");
                    List<ObjectNode> TargetObjectNodes = new ArrayList<>();

                    for(ObjectNode intermediaObjectNode : IntermediateObjectNodes){

                        // [THINK HERE]
                        if(inPTG.getObjects(intermediaObjectNode.getLine(), rField) == null){
                            // create a dummy object
                            
                            // check if intermediaObjectNode is a dummy object
                            if(intermediaObjectNode.getLine().contains("gdummy")){
                                String fieldName = rField;
                                ObjectNode dummyObject = new ObjectNode("gdummy_" + rField + "_" + intermediaObjectNode.getLine());
                                List<ObjectNode> dummyObjectList = new ArrayList<>();
                                dummyObjectList.add(dummyObject);
                                outPTG.addEdge(intermediaObjectNode.getLine(), rField, dummyObjectList);
                            }
                            

                        }

                        if(outPTG.getObjects(intermediaObjectNode.getLine(), rField) != null){
                            TargetObjectNodes.addAll(outPTG.getObjects(intermediaObjectNode.getLine(), rField));   
                        }
                         
                    }
                    
                    outPTG.addEdge(lBase, "", TargetObjectNodes);

               }else{

                    List<ObjectNode> TargetObjectNodes = inPTG.getObjects(rBase, "");

                    if(!lField.isEmpty()){
                        List<ObjectNode>  SourceObjectNodes = inPTG.getObjects(lBase, "");
                        
                        for(ObjectNode sourceObjectNode : SourceObjectNodes){
                            outPTG.addEdge(sourceObjectNode.getLine(), lField, TargetObjectNodes);
                        }
                    }else{
                        outPTG.addEdge(lBase, "", TargetObjectNodes);
                    }

               }    
                
            }else if(rhs instanceof JVirtualInvokeExpr){
                
                JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) rhs;
                // System.out.println("Virtual invoke expression is " + virtualInvokeExpr);

                List<Value> args = virtualInvokeExpr.getArgs();

                escapeArgs(args, inPTG);

                // We let the lhs objects to escape [THINK HERE]
                if(!lField.isEmpty()){
                    List<ObjectNode>  SourceObjectNodes = inPTG.getObjects(lBase, lField);
                    
                    for(ObjectNode sourceObjectNode : SourceObjectNodes){
                        sourceObjectNode.setEscape(true);
                    }
                }else{
                    outPTG.getNode(lBase).setEscape(true);
                }
                
            }

        }else if(u instanceof JInvokeStmt){
            
            JInvokeStmt invokeStmt = (JInvokeStmt) u;

            // Get the parameters of the invoke statement
            List<Value> args = invokeStmt.getInvokeExpr().getArgs();
            escapeArgs(args, inPTG); 

        }else if(u instanceof JReturnStmt){
            
            JReturnStmt returnStmt = (JReturnStmt) u;
            Value returnValue = returnStmt.getOp();

            escapeArgs(List.of(returnValue), inPTG);
        }
    
    } 

    private void worklistAlgo(UnitGraph unitGraph, Map<Unit, PointsToGraph> InPTG, Map<Unit, PointsToGraph> OutPTG, SootClass sootClass){

        List<Unit> worklist = new ArrayList<>();

        // Add all the units to the worklist
        for(Unit unit : unitGraph){
            worklist.add(unit);
        }

        while(!worklist.isEmpty()){
            Unit currentUnit = worklist.remove(0);
            
            // System.out.println("Processing unit: " + currentUnit);

            for(Unit predecessor : unitGraph.getPredsOf(currentUnit)){
                
                // System.out.println("Predecessor: " + predecessor);
                // Take the union of all the predecessors ingraph
                InPTG.get(currentUnit).union(OutPTG.get(predecessor));
            }

            // System.out.println("InPTG for unit: ");
            // InPTG.get(currentUnit).print();

            // Perform the Update based on the statement
            analyseStatement(currentUnit, InPTG.get(currentUnit), OutPTG.get(currentUnit), sootClass);

            // System.out.println("OutPTG for unit: ");
            // OutPTG.get(currentUnit).print();

            // System.out.println(InPTG.get(currentUnit).isEqual(OutPTG.get(currentUnit)));

            if(!InPTG.get(currentUnit).isEqual(OutPTG.get(currentUnit))){
                for(Unit successor : unitGraph.getSuccsOf(currentUnit)){
                    if(!worklist.contains(successor)){
                        worklist.add(successor);
                    }
                }
            }
        }

    }

    public void printEscapingObjects(){

        // Print the map EscapingObjects
        List<String> sortedKeys = new ArrayList<>(EscapingObjects.keySet());
        Collections.sort(sortedKeys);

        // Print the map EscapingObjects with keys and lines in lexicographic order
        for (String key : sortedKeys) {
            System.out.print(key);
            List<String> linesSet = EscapingObjects.get(key);
            
            // Convert set to sorted list
            List<String> sortedLines = new ArrayList<>(linesSet);
            Collections.sort(sortedLines);
            
            // Print sorted lines
            for (String line : sortedLines) {
                System.out.print(" " + line);
            }
            System.out.println();
        }


    }

    @Override
    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
        
        // Get the class name from body
        String className = body.getMethod().getDeclaringClass().getName();

        SootClass testClass = Scene.v().getSootClass(className);
        // Bring class name from the body
        String methodName = body.getMethod().getName();

        // System.out.println("-----------------------------------------------------------");
        // System.out.println("Analyzing method: " + methodName + " in class: " + className);
        
        // Construct CFG for the current method's body
        UnitGraph unitGraph = new BriefUnitGraph(body);

        // Initalize all INs and OUTs
        Map<Unit, PointsToGraph> InPTG = new HashMap<>();
        Map<Unit, PointsToGraph> OutPTG = new HashMap<>();

        for (Unit unit : unitGraph) {
            InPTG.put(unit, new PointsToGraph());
            OutPTG.put(unit, new PointsToGraph());
        }

        // get the variable from body that points to the class object (this)
        

        // Initialize the dummy objects for globals
        for(SootField field : testClass.getFields()){
            String fieldName = field.getName();

            // Check if the field is static global or not
            if(field.isStatic()){
                ObjectNode dummyObject = new ObjectNode("gdummy" + fieldName);

                for(Unit unit : unitGraph){
                    List<ObjectNode> dummyObjectList = new ArrayList<>();
                    dummyObjectList.add(dummyObject);
                    InPTG.get(unit).addEdge(fieldName, "", dummyObjectList);
                    InPTG.get(unit).getNode(fieldName).setEscape(true);
                }
            }
        }

        // Initialize dummy objects for locals
        for(Local local : body.getLocals()){
            String localName = local.getName();

            // Create a dummy object
            ObjectNode dummyObject = new ObjectNode("dummy" + localName);

            // Check if this local variable is a parameter, if so set the escape to true
            if(body.getParameterLocals().contains(local)){
                dummyObject.setEscape(true);
            }

            for(Unit unit : unitGraph){

                List<ObjectNode> dummyObjectList = new ArrayList<>();
                dummyObjectList.add(dummyObject);

                InPTG.get(unit).addEdge(localName, "", dummyObjectList);
            }

        }

        // PointsToGraph graph1 = new PointsToGraph();

        // List<ObjectNode> dummyObjectList = new ArrayList<>();
        // dummyObjectList.add(new ObjectNode("Node2"));

        // graph1.addEdge("r1", "", dummyObjectList);
        // graph1.addEdge("Node2", "f", Arrays.asList(new ObjectNode("Node3"), new ObjectNode("Node4")));

        // PointsToGraph graph2 = new PointsToGraph();

        // List<ObjectNode> dummyObjectList2 = new ArrayList<>();
        // dummyObjectList2.add(new ObjectNode("Node3"));

        // graph2.addEdge("r1", "", dummyObjectList);
        // graph2.addEdge("r1", "", dummyObjectList2);

        // graph2.addEdge("Node2", "f", Arrays.asList(new ObjectNode("Node3"), new ObjectNode("Node4")));


        // PointsToGraph graph3 = new PointsToGraph();
        // graph3.union(graph1);
        // graph3.union(graph2);
        
        // graph1.print();
        // graph2.print();
        // graph3.print();

        // Perform worklist algorithm
        worklistAlgo(unitGraph, InPTG, OutPTG, testClass);

        // System.out.println("Worklist algorithm completed.");
        
        // Peform dfs on the final OUT graph         
        PointsToGraph finalOutPTG = OutPTG.get(unitGraph.getTails().get(0));
        // finalOutPTG.print();
        List<ObjectNode> numericalNodes = finalOutPTG.dfs(); 

        // Add the escaping objects to the map
        List<String> numericalLines = new ArrayList<>();
        for (ObjectNode node : numericalNodes) {
            numericalLines.add(node.getLine());
        }
        
        // Add the escaping objects to the map if the list is not empty
        if (!numericalLines.isEmpty()) {
            EscapingObjects.put(className + ":" + methodName, numericalLines);
        }
        
    }
}
