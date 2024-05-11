package testcase;
class Node {
	Node f;
	Node g;
}

public class Test {
	public static Node global;
	public static void main(String[] args) {
		foo();
	}

	public static void foo(){
		
		Node x = new Node();
		x.f = new Node();
		x.f.g = new Node(); 
		Node y = new Node();
		Node z = new Node();
		
		// bar(x.f, y.f);
		global = x.f;
		
		y.f = z;

		x.f = new Node();

		global = x;

		// return y;
	}

	public static Node foo2(){
		Node[] x = new Node[10];
		x[0] = new Node();
		x[1] = new Node();
		return x[0];
	}

	// public static void bar(Node p1, Node p2){
	// 	Node w = new Node();
	// 	w.f = new Node(); 
	// 	p2.f = w.f;
	// }
}
