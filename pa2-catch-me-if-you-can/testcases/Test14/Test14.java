// Testcase 5:
// Handling conditionals.

class Test14
{
	public static void main(String[] args) {
		Test14 o1 = new Test14();
		Test14 o2 = new Test14();
		o1.foo(o2, true, false);
	}
	
	Test14 foo(Test14 p1,boolean b,  boolean d ) {
		Test14 t1 = new Test14();
		Test14 t2 = new Test14();
		Test14 t3 = new Test14();

		while (b) {
			if (b && d) {
				return t1;
			} else {
				return t2;
			}
		}

		return t3;
	}
}
