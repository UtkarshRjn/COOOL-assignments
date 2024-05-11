// Testcase 5:
// Handling conditionals.

class Test15
{
	Test15 f;
	public static void main(String[] args) {
		Test15 ret = foo();
	}
	
	static Test15 foo() {
		Test15 x = new Test15();
		Test15 y = x;
		for (int i = 0; i < 10; i++) {
			y.f = new Test15();
			y = y.f;
		}
		return x;
	}
}
