// Testcase 6:
// Handling Inheritance

class Test6
{
	Test6 f1;
	Test6 f2;
	public static void main(String[] args) {
		Test6 o1 = new Test6();
		foo(o1);
	}
	static void foo(Test6 p1) {
		if(p1 instanceof Child) {
			p1.f1 = new Child();
		} else {
			p1.f1 = new Test6();
		}
		p1.f1.f2 = new Test6();
		p1.f1.bar();
	}
	void bar() {
		this.f1 = new Test6();
	}
}
class Child extends Test6 {
	void bar() {
		this.f1 = new Child();
	}
}
