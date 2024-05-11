// Testcase 5:
// Handling while loop.

class Test8
{
	Test8 f1;
	Test8 f2;
	public static void main(String[] args) {
		Test8 o1 = new Test8();
		Test8 o2 = new Test8();
		while (args.length > 0) {
			Test8 o3 = new Test8();
			Test8 o4 = o1.foo(o2);
			o3.f1 = o4;
			o4.f1 = new Test8();
		}
	}
	Test8 foo(Test8 p1) {
		Test8 o5 = new Test8();
		Test8 o6 = new Test8();
		bar(new Test8());
		o5.f1 = o6;
 		return new Test8();
	}
	void bar(Test8 p2) {
		Test8 o7 = new Test8();
	}

}
