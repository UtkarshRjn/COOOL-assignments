// Testcase 11:
// Handling more fields.

class Test11Node {
	Test11Node f;
	Test11Node g;
}
public class Test11 {
	public static Test11Node global;
	public static void main(String[] args) {
		foo();
	}
	public static Test11Node foo(){ // 14 15 16 17
		Test11Node x = new Test11Node();
		x.f = new Test11Node();
		x.f.g = new Test11Node();
		Test11Node y = new Test11Node();
		Test11Node z = new Test11Node();
		y.f = z;
		bar(x.f, y);
		return y.f;
	}
	public static void bar(Test11Node p1, Test11Node p2){ // 24
		Test11Node w = new Test11Node();
		w.f = new Test11Node();
		p2.f = w.f;
	}
}
