// Testcase 2:
// Handling return and arguments

class Test2
{
	Test2 f1;
	public static void main(String[] args) {
		Test2 o1 = new Test2();
		Test2 o2 = new Test2();
		Test2 o3 = new Test2();
		o1.f1 = o2;
		o2.f1 = o3;
		Test2 o4 = o1.bar(o1.f1); //
		o4.f1 = new Test2();
	}
	Test2 bar (Test2 p1) {
		Test2 o5 = new Test2();
		return o5;
	}
}
