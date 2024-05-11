// Testcase 4:
// Handling combination of all cases for escaping.

class Test4
{
	Test4 f1;
	Test4 f2;
	public static void main(String[] args) {
		Test4 o1 = new Test4();
		Test4 o2 = new Test4();
		Test4 o3 = new Test4();
		Test4 o4 = o1.foo(o2, o3);
		Test4 o5 = o1.bar(o4);
	}
	
	Test4 foo(Test4 p1, Test4 p2) {
		p1.f1 = new Test4();
		p1.f1.f2 = new Test4();
		p2.f1 = new Test4();
		return p1;
	}
	
	Test4 bar (Test4 p3) {
		Test4 o5 = new Test4();
		p3.f1 = new Test4();	
		return o5;
	}
}
