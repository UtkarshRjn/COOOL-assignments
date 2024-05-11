// Testcase 5:
// Handling conditionals.

class Test5
{
	Test5 f1;
	Test5 f2;
	public static void main(String[] args) {
		Test5 o1 = new Test5();
		Test5 o2 = new Test5();
		Test5 o3 = new Test5();
		boolean b;
		b = false;
		Test5 o4 = o1.foo(o2, o3 , b);
		o4.f2 = new Test5();
	}
	
	Test5 foo(Test5 p1, Test5 p2, Boolean b ) {
		if(b) {
			p1.f1 = new Test5();
			return  p1.f1;
		} else {
			p1.f1 = new Test5();
		}
		p1.f1.f2= new Test5();
		p2.f1 = new Test5();
		return p2.f1;
	}
}
