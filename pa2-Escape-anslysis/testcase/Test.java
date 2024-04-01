package testcase;
class Node {
	Node f;
	Node g;
}

public class Test {
	public static Node global;
	public Node field;

	public static void main(String[] args) {
		foo();
	}

	// [this]--field-->[dummyfield]--f-->
	// (global, esp = true)----""----->[dummyglobal]---f--->

	// x.f

	public static Node foo(Node n){	// 15 16 17 18
		
		// n.f = new Node()
		// global.f = new Node()
		// this.field.f = new Node()
		
		Node x = new Node();
		x.f = new Node();
		x.f.g = new Node(); 
		Node y = new Node(); 
		Node z = new Node();
		y.f = z;
		bar(x.f, y);
		return y.f;
	}

	public static Node bar(Node p1, Node p2){	// 26
		Node w = new Node();
		w.f = new Node(); 
		p2.f = w.f;		

		return p1;
	}

	public Node f1(){			// 33 35
		global = new Node();
		Node x = global.f;
		global.f = new Node();
		x.g = global.f;

		return x.g;
	}

	public Node f2(Node p){		// 43
		Node x = f1();
		x.f = new Node();

		return p;
	}

	public void f3(){			// 50
		Node x = new Node();
		Node y = new Node();

		x.g = y;
		y.f = x.g.f;

		field.f = x.g;
	}

	public void f4(){			// 67
        Node x = new Node();
        
        while (x != null){
            x = x.f;
        }

        Node y = x.f;
        x.g = x.f;
        y.g = new Node();
        x.g = f2(x.g);

        x = x.g;

		global = x.g;
	}

	public void f5(Node p){		// 79 82
		Node x = new Node();
		Node y = x;

		y.f = new Node();

		if (x.f != null){
			y.g = new Node();
		} else if (x.g != null){
			x.g = y.g;
		} else {
			x.g = y.f;
		}

		p.g = x.g;
	}

	public Node f6(){			// 93 94 98 99 100 101 107 108 112 115
		Node x = new Node();
		x.f = new Node();
		Node y = new Node();
		y.f = new Node();
		//comment out line 17 and check again.
		y = new Node();
		y.g = new Node();
		y.f = new Node();
		Node z = new Node();
		Node w = bar(y,z);
		//comment out line 23 and check again.
		w = new Node();
		w.f = new Node();

		global.f.g.g.f.f = new Node();
		global = new Node();
		
		Node a[] = new Node[10];
		if(a[1]==y){
			a[0] = new Node();//escapes.
		}
		else{
			a[2] = new Node();//escapes
		}
		
		global = a[1];
		return x;
	}

	public static Node[] arrtest(){		// 123 124 125
		Node[] global = new Node[5];
		global[1] = new Node();
		global[1].f = new Node();
		return global;
	}

	public Node[] arrParam(Node[] p){	// 132 133
		Node arr[] = new Node[5];

		arr[0] = new Node();
		arr[1].f = new Node();
	
		p[0] = arr[2];
		return p;
	}

	protected Node f7(){				// 141 142
		Node x = new Node();
		x.f = new Node();
		x.f.g = new Node();

		Node y = new Node();
		Node z = new Node();

		if (true){
			y.f = x.f;
		} else {
			y.f = z;
		}

		f7();
		return y.f;
	}

	protected void f8(Node p){			// 158	
		Node x = new Node();

		field = x;
		f8(p);
	}

	public static Node f9(){			// 166 167 169
		Node x = new Node();
		x.f = new Node();
		x.f.g = new Node(); 
		Node y = new Node();
		Node z = new Node();
		
		f10(x.f, y.f);
		
		y.f = z;

		x.f = new Node();

		return y.f;	
	}

	public static void f10(Node p1, Node p2){	// 182
		Node w = new Node();
		w.f = new Node(); 
		p2.f = w.f;
	}

	public static Node f11(){					// 187 188
		Node x = new Node();
		Node y = new Node();
		global.f = x;

		return y;
	}

	public static void f12(){					// 195 196 197 205
		Node x = new Node();
		x.f = new Node();
		x.f.g = new Node(); 
		Node y = new Node();
		Node z = new Node();
		
		global = x.f;
		
		y.f = z;

		x.f = new Node();

		global = x;
	}

}