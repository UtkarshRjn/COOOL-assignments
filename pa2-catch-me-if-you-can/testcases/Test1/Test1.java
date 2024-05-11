// Testcase 1:
// Handling storing to globals

class Test1
{
	Test1 f1;
	static Test1 global;
	public static void main(String[] args) {
		Test1 o1 = new Test1();
		Test1 o2 = new Test1();
		Test1 o3 = new Test1();
		o1.f1 = o2;
		o2.f1 = o3;
		global = o1;
	}
}
