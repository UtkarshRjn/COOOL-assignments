import java.util.*;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.SimpleLiveLocals;
import soot.toolkits.scalar.LiveLocals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;

import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.internal.*;
import soot.jimple.*;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

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

    public void deleteEdge(String sourceKey, String fieldKey, String destinationKey) {
        if (edges.containsKey(sourceKey)) {
            Map<String, List<ObjectNode>> fieldMap = edges.get(sourceKey);
            if (fieldMap.containsKey(fieldKey)) {
                List<ObjectNode> nodeList = fieldMap.get(fieldKey);
                Iterator<ObjectNode> iterator = nodeList.iterator();
                while (iterator.hasNext()) {
                    ObjectNode node = iterator.next();
                    if (node.getLine().equals(destinationKey)) {
                        iterator.remove();
                        break;
                    }
                }
                if (nodeList.isEmpty()) {
                    fieldMap.remove(fieldKey);
                    if (fieldMap.isEmpty()) {
                        edges.remove(sourceKey);
                        nodes.remove(sourceKey); // Remove from nodes map if no more edges are connected to the sourceKey
                    }
                }
            }
        }
    }

    // Given a sourceKey (Needs to be line No or param Name) and fieldKey (edge Label f,g, ""), return all the objectNodes
    public List<ObjectNode> getObjects(String sourceKey, String fieldKey) { // [THINK] Remember the change made here
        
        if (!edges.containsKey(sourceKey)) {
            // Create a dummy node for the missing source key
            ObjectNode dummyNode = new ObjectNode("d_" + sourceKey);
        
            List<ObjectNode> dummyObjectList = new ArrayList<>();
            dummyObjectList.add(new ObjectNode("d_" + sourceKey));
                        
            addEdge(sourceKey, "", dummyObjectList);
            
        }
        
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

    public void assign(PointsToGraph otherGraph) {
        // Clear existing nodes and edges
        this.nodes.clear();
        this.edges.clear();
    
        // Copy nodes from the other graph
        for (ObjectNode node : otherGraph.nodes.values()) {
            this.addNode(new ObjectNode(node.getLine()));
        }
    
        // Copy edges from the other graph
        for (Map.Entry<String, Map<String, List<ObjectNode>>> entry : otherGraph.edges.entrySet()) {
            String sourceKey = entry.getKey();
            Map<String, List<ObjectNode>> fieldMap = entry.getValue();
    
            for (Map.Entry<String, List<ObjectNode>> fieldEntry : fieldMap.entrySet()) {
                String fieldKey = fieldEntry.getKey();
                List<ObjectNode> nodeList = fieldEntry.getValue();
    
                this.addEdge(sourceKey, fieldKey, new ArrayList<>(nodeList));
            }
        }
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

    public void deleteEdgesFromSource(String sourceKey, List<String[]> deadEdges, List<ObjectNode> deletedNumericalNodes) {
        
        
        if (!edges.containsKey(sourceKey)){
            // System.out.println(sourceKey + " not found");
            return;
        }
        
        // System.out.println("Deleting " + sourceKey);
        // print();

        // Process edges originating from the sourceKey
        Map<String, List<ObjectNode>> fieldMap = edges.get(sourceKey);
        for (Map.Entry<String, List<ObjectNode>> entry : fieldMap.entrySet()) {
            String fieldKey = entry.getKey();
            List<ObjectNode> nodeList = entry.getValue();
            
            // Add deleted edges to deadEdges list
            for (ObjectNode node : nodeList) {
                deadEdges.add(new String[]{sourceKey, fieldKey, node.getLine()});
            }
            
            // Remove edges
            fieldMap.remove(fieldKey);
            
            // System.out.println("Deleted edges from source: " + sourceKey + " with field: " + fieldKey);
            // print();

            // Process nodes to remove further edges with indegree node as 0
            for (ObjectNode node : nodeList) {
                // Check if node is numerical and has indegree node as 0
                
                // System.out.println("Node: " + node.getLine() + " Indegree: " + getIndegree(node.getLine()));

                if (isNumerical(node.getLine()) && getIndegree(node.getLine()) == 0) {
                    deletedNumericalNodes.add(node);
                    List<ObjectNode> connectedNodes = getConnectedNodes(node.getLine());
                    deleteEdgesFromSource(node.getLine(), deadEdges, deletedNumericalNodes);
                    nodes.remove(node.getLine()); // Remove node from nodes map
                    edges.remove(node.getLine()); // Remove node from edges map
                    deletedNumericalNodes.addAll(connectedNodes);
                }
            }
        }
                
        // Remove sourceKey if it has no more outgoing edges
        if (fieldMap.isEmpty()) {
            edges.remove(sourceKey);
            nodes.remove(sourceKey);
        }

        // print();
    }
    
    public void deleteDummySource(List<String[]> deadEdges){

        // Iderate over a copy of the edges map to avoid concurrent modification exception
        for (String sourceKey : new HashMap<>(edges).keySet()) {
            if (sourceKey.contains("d_")) {
                deleteEdgesFromSource(sourceKey, deadEdges, new ArrayList<>());                
            }
        }
        
    }

    // Method to get the indegree of a node
    private int getIndegree(String nodeKey) {
        int indegree = 0;
        for (Map<String, List<ObjectNode>> fieldMap : edges.values()) {
            for (List<ObjectNode> nodeList : fieldMap.values()) {
                indegree += nodeList.stream().filter(node -> node.getLine().equals(nodeKey)).count();
            }
        }
        return indegree;
    }
    
    // Method to get connected nodes to a given node
    private List<ObjectNode> getConnectedNodes(String nodeKey) {
        List<ObjectNode> connectedNodes = new ArrayList<>();
        for (Map<String, List<ObjectNode>> fieldMap : edges.values()) {
            for (List<ObjectNode> nodeList : fieldMap.values()) {
                for (ObjectNode node : nodeList) {
                    if (node.getLine().equals(nodeKey)) {
                        connectedNodes.add(node);
                    }
                }
            }
        }
        return connectedNodes;
    }

    public void print() { // [Update] Removed the printing of node


        // System.out.println("Nodes:");
        // for (String nodeKey : nodes.keySet()) {
        //     ObjectNode node = nodes.get(nodeKey);
        //     System.out.println("Node: " + nodeKey + ", Escape: " + node.escape());
        // }

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

public class AnalysisTransformer extends SceneTransformer {
    static CallGraph cg;

    private Map<String, List<String>> GCedObjects = new HashMap<>();

    // Initalize all INs and OUTs
    Map<Unit, PointsToGraph> InPTG = new HashMap<>();
    Map<Unit, PointsToGraph> OutPTG = new HashMap<>();
    
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

    private static void escapeArgs(List<Value> args, PointsToGraph inPTG, String methodId){
                
        for(Value arg : args){
            String[] rPair = extractBaseAndField(arg);
            String rBase = rPair[0];
            String rField = rPair[1];
            
            List<ObjectNode> TargetObjectNodes = new ArrayList<>();
            if(!rField.isEmpty()){

                // We assume here that lhs would only have a base and no field
                List<ObjectNode> IntermediateObjectNodes = inPTG.getObjects(methodId + '_' + rBase, "");

                for(ObjectNode intermediateObjectNode : IntermediateObjectNodes){

                    if(inPTG.getObjects(intermediateObjectNode.getLine(), rField) == null)
                        continue;

                    TargetObjectNodes.addAll(inPTG.getObjects(intermediateObjectNode.getLine(), rField));   
                }
                
            }else{
                TargetObjectNodes.addAll(inPTG.getObjects(methodId + '_' + rBase, ""));
            }
            
            for(ObjectNode targetObjectNode : TargetObjectNodes){
                targetObjectNode.setEscape(true);
            }
            
        }

    }

    private void analyseStatement(Unit u, PointsToGraph inPTG, PointsToGraph outPTG, SootClass sootClass, String methodId, String CallerReturnValue){ // Removed static from here

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
                List<ObjectNode> TargetObjectNodes = inPTG.getObjects(methodId + '_' + lBase, "");
                for(ObjectNode targetObjectNode : TargetObjectNodes){
                    targetObjectNode.setEscape(true);
                }

                for(SootField field : sootClass.getFields()){
                    String fieldName = field.getName();
                    if(!field.isStatic()){
                        
                        List<ObjectNode> dummyObjectList = new ArrayList<>();
                        dummyObjectList.add(new ObjectNode("gdummy" + fieldName));
                        
                        outPTG.addEdge("d_" + methodId + '_' +  lBase, fieldName, dummyObjectList);
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

            System.out.println("LHS is " + lBase + " and the field reference is: " + lField);
            System.out.println("RHS is " + rBase + " and the field reference is: " + rField);
            System.out.println("");

            // Print the type of the right hand side
            System.out.println("Type of lhs is " + lhs.getClass().getName());
            System.out.println("Type of rhs is " + rhs.getClass().getName());

            if (rhs instanceof JNewExpr || rhs instanceof JNewArrayExpr) {
                int line = u.getJavaSourceStartLineNumber();
                ObjectNode NewNode = new ObjectNode(String.valueOf(line));

                if(!lField.isEmpty()){
                    List<ObjectNode>  SourceObjectNodes = inPTG.getObjects(methodId + '_' + lBase, "");
                    
                    for(ObjectNode sourceObjectNode : SourceObjectNodes){

                        List<ObjectNode>  NewNodeList = new ArrayList<>();
                        NewNodeList.add(NewNode);
                        outPTG.addEdge(sourceObjectNode.getLine(), lField, NewNodeList);
                    }

                }else{

                    List<ObjectNode>  NewNodeList = new ArrayList<>();
                    NewNodeList.add(NewNode);
                    outPTG.addEdge(methodId + '_' + lBase, "", NewNodeList);
                }

            }
            else if(rhs instanceof JInstanceFieldRef || rhs instanceof JimpleLocal || rhs instanceof StaticFieldRef || rhs instanceof JArrayRef || rhs instanceof JCastExpr){
            
                if(!rField.isEmpty()){

                    // We assume here that lhs would only have a base and no field
                    List<ObjectNode> IntermediateObjectNodes = inPTG.getObjects(methodId + '_' + rBase, "");
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
                    
                    outPTG.addEdge(methodId + '_' + lBase, "", TargetObjectNodes);

               }else{

                    List<ObjectNode> TargetObjectNodes = inPTG.getObjects(methodId + '_' + rBase, "");

                    if(!lField.isEmpty()){
                        List<ObjectNode>  SourceObjectNodes = inPTG.getObjects(methodId + '_' + lBase, "");
                        
                        for(ObjectNode sourceObjectNode : SourceObjectNodes){
                            outPTG.addEdge(sourceObjectNode.getLine(), lField, TargetObjectNodes);
                        }
                    }else{
                        outPTG.addEdge(methodId + '_' + lBase, "", TargetObjectNodes);
                    }

               }    
                
            }else if(rhs instanceof JVirtualInvokeExpr){
                
                JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) rhs;
                System.out.println("Virtual invoke expression is " + virtualInvokeExpr);

                List<Value> args = virtualInvokeExpr.getArgs();

                escapeArgs(args, outPTG, methodId);

                // We let the lhs objects to escape [THINK HERE]
                if(!lField.isEmpty()){
                    List<ObjectNode>  SourceObjectNodes = inPTG.getObjects(methodId + '_' + lBase, lField);
                    
                    for(ObjectNode sourceObjectNode : SourceObjectNodes){
                        sourceObjectNode.setEscape(true);
                    }
                }else{
                    outPTG.getNode(methodId + '_' + lBase).setEscape(true);
                }
                
            }else if(rhs instanceof JStaticInvokeExpr){

                JStaticInvokeExpr staticInvokeExpr = (JStaticInvokeExpr) rhs;
                System.out.println("Static invoke expression is " + staticInvokeExpr);

                // System.out.println("Return variable: " + lhs);
                String returnValue = methodId + '_' + lhs.toString();

                List<Value> args = staticInvokeExpr.getArgs();

                List<String> argsList = new ArrayList<>();
                for(Value arg : args){
                    argsList.add(methodId + '_' + arg.toString());
                }

                // Get the method being invoked
                SootMethod invokedMethod = staticInvokeExpr.getMethod();

                outPTG.assign(processCFG(invokedMethod, inPTG, argsList, String.valueOf(u.getJavaSourceStartLineNumber()), returnValue));

                escapeArgs(args, outPTG, methodId);

                // We let the lhs objects to escape [THINK HERE]
                if(!lField.isEmpty()){
                    List<ObjectNode>  SourceObjectNodes = inPTG.getObjects(methodId + '_' + lBase, lField);
                    
                    for(ObjectNode sourceObjectNode : SourceObjectNodes){
                        sourceObjectNode.setEscape(true);
                    }
                }else{
                    outPTG.getNode(methodId + '_' + lBase).setEscape(true);
                }

            }

        }else if(u instanceof JInvokeStmt){
            
            JInvokeStmt invokeStmt = (JInvokeStmt) u;

            // Get the parameters of the invoke statement
            List<Value> args = invokeStmt.getInvokeExpr().getArgs();
                
            List<String> argsList = new ArrayList<>();
            for(Value arg : args){
                argsList.add(methodId + '_' + arg.toString());
            }
            
            // print args
            for(Value arg : args){
                System.out.println("Arg: " + arg);
            }
            
            PointsToGraph newPTG = new PointsToGraph();
            
            // InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
            // SootMethod invokedMethod = invokeExpr.getMethod();
            // newPTG.union(processCFG(invokedMethod, inPTG, argsList, methodId + "nullret"));

            Iterator<Edge> edgeIterator = cg.edgesOutOf(u);
            
            while (edgeIterator.hasNext()) {
                Edge edge = edgeIterator.next();
                SootMethod calleeMethod = edge.tgt();
                       
                if(calleeMethod.toString().contains("init") || calleeMethod.isJavaLibraryMethod()) { continue; }

                System.out.println("Function called by invoking unit: " + calleeMethod);
                newPTG.union(processCFG(calleeMethod, inPTG, argsList, String.valueOf(u.getJavaSourceStartLineNumber()) ,methodId + "_nullret"));
            }

            if(!newPTG.isEqual(new PointsToGraph())){
                outPTG.assign(newPTG);
            }

            escapeArgs(args, outPTG, methodId); // [UPDATE] made it outPTG instead of inPTG

        }else if(u instanceof JReturnStmt){
            
            JReturnStmt returnStmt = (JReturnStmt) u;
            Value returnValue = returnStmt.getOp();

            escapeArgs(List.of(returnValue), outPTG, methodId); // [UPDATE] made it outPTG instead of inPTG

            if(!CallerReturnValue.isEmpty()){
                List<ObjectNode> TargetObjectNodes = new ArrayList<>();
                TargetObjectNodes.addAll(outPTG.getObjects(methodId + '_' + returnValue.toString(), ""));
                outPTG.addEdge(CallerReturnValue, "", TargetObjectNodes);
            }
            // System.out.println("Return variable: " + CallerReturnValue);
        
        }
    
    } 

    private void worklistAlgo(UnitGraph unitGraph, Map<Unit, PointsToGraph> InPTG, Map<Unit, PointsToGraph> OutPTG, SootClass sootClass, String methodId, String ReturnValue){

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

            // System.out.println("InPTG for unit: " + currentUnit);
            // InPTG.get(currentUnit).print();

            // Perform the Update based on the statement
            analyseStatement(currentUnit, InPTG.get(currentUnit), OutPTG.get(currentUnit), sootClass, methodId, ReturnValue);

            // System.out.println("OutPTG for unit: " + currentUnit);
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

    @Override
    protected void internalTransform(String arg0, Map<String, String> arg1) {
        Set<SootMethod> methods = new HashSet <>();
        cg = Scene.v().getCallGraph();
        // Get the main method
        SootMethod mainMethod = Scene.v().getMainMethod();
        // Get the list of methods reachable from the main method
        // Note: This can be done bottom up manner as well. Might be easier to model.
        
        processCFGMain(mainMethod);
        
        // getlistofMethods(mainMethod, methods);
        
        GarbageCollect(mainMethod, "0" , new ArrayList<>());
    }

    protected void processCFGMain(SootMethod method){ // I have removed static here

        // Get the class name from body
        String className = method.getDeclaringClass().getName();

        SootClass testClass = Scene.v().getSootClass(className);
        // Bring class name from the body
        String methodName = method.getName();
        String methodId = "0_" + methodName;

        System.out.println("-----------------------------------------------------------");
        System.out.println("Analyzing method: " + methodId + " in class: " + className);

        Body body = method.getActiveBody();  

        // Construct CFG for the current method's body
        UnitGraph unitGraph = new BriefUnitGraph(body);

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
            ObjectNode dummyObject = new ObjectNode("d_" + methodId + "_" + localName);

            // Check if this local variable is a parameter, if so set the escape to true
            if(body.getParameterLocals().contains(local)){
                dummyObject.setEscape(true);
            }

            for(Unit unit : unitGraph){

                List<ObjectNode> dummyObjectList = new ArrayList<>();
                dummyObjectList.add(dummyObject);

                InPTG.get(unit).addEdge(methodId + "_" + localName, "", dummyObjectList);
            }

        }

        // Perform worklist algorithm
        worklistAlgo(unitGraph, InPTG, OutPTG, testClass, methodId, null);

        // Peform dfs on the final OUT graph         
        PointsToGraph finalOutPTG = OutPTG.get(unitGraph.getTails().get(0));
        // finalOutPTG.print();

    }

    protected PointsToGraph processCFG(SootMethod method, PointsToGraph inPTGCaller, List<String> args, String CallerLineNumber , String ReturnValue){

        if(method.toString().contains("init") || method.isJavaLibraryMethod()) { return inPTGCaller; }

        // Get the class name from body
        String className = method.getDeclaringClass().getName();

        SootClass testClass = Scene.v().getSootClass(className);
        // Bring class name from the body
        String methodName = method.getName();
        String methodId = CallerLineNumber + "_" + methodName;

        System.out.println("-----------------------------------------------------------");
        System.out.println("Analyzing method: " + methodId + " in class: " + className);

        Body body = method.getActiveBody();  

        // Construct CFG for the current method's body
        UnitGraph unitGraph = new BriefUnitGraph(body);

        for (Unit unit : unitGraph) {
            InPTG.put(unit, new PointsToGraph());
            InPTG.get(unit).union(inPTGCaller);
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
            ObjectNode dummyObject = new ObjectNode("d_" + methodId + "_" + localName);

            // Check if this local variable is a parameter, if so set the escape to true
            if(body.getParameterLocals().contains(local)){                
                dummyObject.setEscape(true);

                // Find index of this local
                int index = body.getParameterLocals().indexOf(local);

                // run code similar to foo_r1 = main_r1 (code matches that of assignment statement)
                String callerlocalName = args.get(index).toString();

                List<ObjectNode> TargetObjectNodes = inPTGCaller.getObjects(callerlocalName, "");
                TargetObjectNodes.add(dummyObject);

                for(Unit unit : unitGraph){
                    InPTG.get(unit).addEdge(methodId + '_' + localName, "", TargetObjectNodes);
                }

            }else{
                
                for(Unit unit : unitGraph){
    
                    List<ObjectNode> dummyObjectList = new ArrayList<>();
                    dummyObjectList.add(dummyObject);
    
                    InPTG.get(unit).addEdge(methodId + "_" + localName, "", dummyObjectList);
                }
            }


        }

        // Perform worklist algorithm
        worklistAlgo(unitGraph, InPTG, OutPTG, testClass, methodId, ReturnValue);

        // Peform dfs on the final OUT graph         
        PointsToGraph finalOutPTG = OutPTG.get(unitGraph.getTails().get(0));
        
        // finalOutPTG.print();
        return finalOutPTG;

    }

    protected void GarbageCollect(SootMethod method, String CallerLineNumber, List<String[]> deadEdges) {
        if(method.toString().contains("init") || method.isJavaLibraryMethod()) { return; }
        Body body = method.getActiveBody();

        String className = method.getDeclaringClass().getName();

        // Get the callgraph 
        UnitGraph cfg = new BriefUnitGraph(body);
        // Get live local using Soot's exiting analysis
        LiveLocals liveLocals = new SimpleLiveLocals(cfg);
        // Units for the body
        PatchingChain<Unit> units = body.getUnits();
        System.out.println("\n----- " + body.getMethod().getName() + "-----");

        // create a list of deadEdges to be removed, edge is represented by sourceKey, fieldKey and destinationKey

        // Iterate over the units
        for (Unit u : units) {
            System.out.println("Unit " + u.getJavaSourceStartLineNumber() + ": " + u);
            // List<Local> before = liveLocals.getLiveLocalsBefore(u);
            
            if(u instanceof JIdentityStmt){
                continue;
            }else if(u instanceof JAssignStmt){

                JAssignStmt stmt = (JAssignStmt) u;
                Value rhs = stmt.getRightOp();
                Value lhs = stmt.getLeftOp();

                if(rhs instanceof JStaticInvokeExpr){
                    
                    JStaticInvokeExpr staticInvokeExpr = (JStaticInvokeExpr) rhs;
                    // System.out.println("static invoke expression is " + staticInvokeExpr);

                    // Get the method being invoked
                    SootMethod invokedMethod = staticInvokeExpr.getMethod();
                    
                    GarbageCollect(invokedMethod, String.valueOf(u.getJavaSourceStartLineNumber()) , deadEdges);

                }else if(rhs instanceof JVirtualInvokeExpr){
                    
                    JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) rhs;

                    // Get the method being invoked
                    SootMethod invokedMethod = virtualInvokeExpr.getMethod();

                    GarbageCollect(invokedMethod, String.valueOf(u.getJavaSourceStartLineNumber()), deadEdges);

                }            

            }else if(u instanceof InvokeStmt){
                InvokeStmt invokeStmt = (InvokeStmt) u;
                
                InvokeExpr invokeExpr = invokeStmt.getInvokeExpr();
                SootMethod invokedMethod = invokeExpr.getMethod();
                if(invokedMethod.toString().contains("init") || invokedMethod.isJavaLibraryMethod()) { continue; }
                
                // GarbageCollect(invokedMethod, deadEdges);
                
                cg = Scene.v().getCallGraph();
                Iterator<Edge> edgeIterator = cg.edgesOutOf(u);
                
                // Create a copy of deadEdges
                List<String[]> deadEdgesCopy = new ArrayList<>(deadEdges);

                while (edgeIterator.hasNext()) {
                    // Create a fresh copy of deadEdges for each iteration
                    List<String[]> currentDeadEdgesCopy = new ArrayList<>(deadEdgesCopy);
                    
                    Edge edge = edgeIterator.next();
                    SootMethod calleeMethod = edge.tgt();
                    
                    // if(calleeMethod.toString().contains("init") || calleeMethod.isJavaLibraryMethod()) { continue; }

                    System.out.println("Garbage collecting the invoked function: " + calleeMethod);
                    GarbageCollect(calleeMethod, String.valueOf(u.getJavaSourceStartLineNumber()), currentDeadEdgesCopy);
                    
                    // Update deadEdges by taking the union of the current copy
                    deadEdges.addAll(currentDeadEdgesCopy);
                }

                // Update deadEdges by taking the union of all changes
                deadEdges.addAll(deadEdgesCopy);

            }

            // Assume all the locals are live before the unit
            List<Local> before = new ArrayList<>(body.getLocals());
            
            // Create a new Local object with the name "garbage" and type int
            Local newLocal = Jimple.v().newLocal("nullret", IntType.v());
            before.add(newLocal);
            
            List<Local> after = liveLocals.getLiveLocalsAfter(u);
            
            System.out.println("Live locals before: " + before);
            System.out.println("Live locals after: " + after);

            // OutPTG.get(u).print();

            PointsToGraph ptg = new PointsToGraph();
            ptg.union(OutPTG.get(u)); // Here we are not making a new object take a note

            // [THINK] delete all nodes that have dummy source
            ptg.deleteDummySource(deadEdges);
            
            // ptg.print();
            
            // print deadEdges
            // for(String[] deadEdge : deadEdges){
            //     System.out.println(deadEdge[0] + "," + deadEdge[1] + "," + deadEdge[2]);
            // }

            deadEdges.forEach(edge -> ptg.deleteEdge(edge[0], edge[1], edge[2]));
            
            // ptg.print();

            // Find the livelocals that is not in the after list
            for (Local local : before) {
                if (!after.contains(local)) {
                    // System.out.println("Dead local: " + local);
                    String localName = local.getName();
                    String methodName = method.getName();
                    String methodId = CallerLineNumber + "_" + methodName;
                    String sourceKey = methodId + "_" + localName;
                    // Remove all the edges that has the sourceKey and correspondingly all nodes that have inNodeDegree = 0, use dfs
                    
                    // print sourceKey and deadEdges
                    // System.out.println("SourceKey: " + sourceKey);
                    // System.out.println("DeadEdges before Garbage Collection: ");
                    // deadEdges.forEach(edge -> System.out.println(edge[0] + " " + edge[1] + " " + edge[2]));

                    List<ObjectNode> GCed_nodes = new ArrayList<>();
                    ptg.deleteEdgesFromSource(sourceKey, deadEdges, GCed_nodes);
                    
                    // System.out.println("DeadEdges after Garbage Collection: ");
                    // deadEdges.forEach(edge -> System.out.println(edge[0] + " " + edge[1] + " " + edge[2]));

                    for(ObjectNode node : GCed_nodes){
                        System.out.println("Garbage Collected Node: " + node.getLine());
                    }

                    // get line number of the unit u
                    int line = u.getJavaSourceStartLineNumber();

                    // Add the GCed nodes to the list of GCed objects
                    List<String> GCedObjectsList = new ArrayList<>();
                    for(ObjectNode node : GCed_nodes){
                        GCedObjectsList.add( node.getLine() + ":" + line);
                    }

                    // System.out.println("GCed Objects: " + GCedObjectsList);
                    // System.out.println(className + ":" + methodName);

                    // Add the list to the existing GCedObjects
                    if(GCedObjects.containsKey(className + ":" + methodName)){
                        GCedObjects.get(className + ":" + methodName).addAll(GCedObjectsList);
                    }else{
                        GCedObjects.put(className + ":" + methodName, GCedObjectsList);
                    }

                }
            }

            ptg.print();

            System.out.println();

        }
    }

    private static void getlistofMethods(SootMethod method, Set<SootMethod> reachableMethods) {
        // Avoid revisiting methods
        if (reachableMethods.contains(method)) {
            return;
        }
        // Add the method to the reachable set
        reachableMethods.add(method);

        // Iterate over the edges originating from this method
        Iterator<Edge> edges = Scene.v().getCallGraph().edgesOutOf(method);
        while (edges.hasNext()) {
            Edge edge = edges.next();
            SootMethod targetMethod = edge.tgt();
            // Recursively explore callee methods
            if (!targetMethod.isJavaLibraryMethod()) {
                getlistofMethods(targetMethod, reachableMethods);
            }
        }
    }

    public void printGCedObjects() {
        for (Map.Entry<String, List<String>> entry : GCedObjects.entrySet()) {
            String key = entry.getKey();
            List<String> GCedObjectsList = entry.getValue();

            System.out.print(key + " ");
            for (String object : GCedObjectsList) {
                System.out.print(object + " ");
            }
            System.out.println();
        }
    }

}
