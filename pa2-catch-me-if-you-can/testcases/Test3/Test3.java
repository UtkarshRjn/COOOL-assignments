// Testcase 3:
// Handling storing to parameter's field.

class Test3
{
	Test3 f1;
	public static void main(String[] args) {
		Test3 o1 = new Test3();
		Test3 o2 = new Test3();
		Test3 o3 = new Test3();
		o1.foo(o2, o3);
	}
	void foo (Test3 p1, Test3 p2) {
		Test3 o4 = new Test3();
		Test3 o5 = new Test3();
		p1.f1 = o4;
		p2.f1 = o5;
	}
}
