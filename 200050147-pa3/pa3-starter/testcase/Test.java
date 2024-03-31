class Base {
	void goo(){}
}

class Node /* extends Base */ {
	Node f;
	Node g;
	Node() {}

	void goo(){}
}

public class Test {
	// public static Node global;
	public static void main(String[] args) {
		// Node ret = foo();
		Node y = new Node();
		Node z = new Node();
		if (y != null){
			Node a = z.f;
			// z = new Node();
		} else {
			Node b = z.f;
			// z = new Node();
		}
	}
	public static Node foo(){
		// Node x = new Node();
		Node y = new Node();
		// y.f = new Node();
		// y = new Node();
		// bar(x, y);

		Node z = new Node();
		if (y != null){
			Node a = z.f;
			// z = new Node();
		} else {
			Node b = z.f;
			// z = new Node();
		}
		// Node a = x.f;
		// z.goo();
		return y;
	}

	public static void bar(Node p1, Node p2){
		Node v = new Node();
		p1.f = v;	
		Node x = baz();
		x.f = new Node();			
		baz();						// ignoring ret val
		x = f2(p2);					// catching ret val
		x.f = f2(p2);
	}

	public static Node baz(){
		Node x = new Node();
		x.f = new Node();
		Node y = new Node();
		y = f1(y);
		y.f = new Node();
		return x;
	}

	public static Node f1(Node p){
		Node x = new Node();
		return x;
	}

	public static Node f2(Node p){
		Node x = new Node();
		return x;
	}
}

/* What I get
Node:goo
Test:bar 42:42 50:43 51:43 64:46
Test:baz 52:53 54:54 59:54
Test:f1
Test:f2
Test:foo 19:33 20:21 21:21 25:30 28:34 31:34 39:33
Test:main 22:16
Node:goo
Test:bar 42:42 49:43 50:43 63:45
Test:baz 51:52 53:53 58:53
Test:f1
Test:f2
Test:foo 19:33 20:21 21:21 25:30 28:34 31:34 39:33
Test:main 22:16
*/