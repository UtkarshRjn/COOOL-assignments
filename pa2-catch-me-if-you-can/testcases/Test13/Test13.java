// Testcase 5:
// Handling conditionals.

class Test13
{
	Test13 f1;
	public static void main(String[] args) {
		Test13 o1 = new Test13();
		Test13 o2 = new Test13();
		int i = 0;
		o1.foo(o2, i);
	}
	
	void foo(Test13 p1, int i) {
		Test13 o3 = new Test13();
		if(i == 0) {
			return;
		}
		foo(new Test13(), i++);
	}
}
