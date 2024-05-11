// Testcase 7:
// Handling Inheritance with more objects.

class Test7
{
	Test7 f1;
	Test7 f2;
	public static Test7 global;
	public static void main(String[] args) {
		Test7 o1 = new Test7();
		Test7 o2 = new Test7();
		Test7 o3 = new Test7();
		int a;
		a = args.length;
		Test7 o4 = o1.foo(o2, o3 , a);
		o4.f2 = new Test7();
	}
	
	Test7 foo(Test7 p1, Test7 p2, int val ) {
		Test7 a1 = new Test7();
		if(val == 0) {
			a1.f1 = new Test7();
		} else if (val == 1 ) {
			a1.f1 = new Child();
			global = a1.f1;
		} else {
			a1.f1 = new Child2();
		}
		Test7 o5 = new Test7();
		a1.f1.f2= new Test7();
		o5.f1 = a1.f1;
		p2.f1 = new Test7();
		return o5.f1;
	}
}

class Child extends Test7 {
	Test7 foo(Test7 p1, Test7 p2, int val ) {
		return new Test7();
	}
}

class Child2 extends Test7 {
	Test7 foo(Test7 p1, Test7 p2, int val ) {
		return new Test7();
	}
}