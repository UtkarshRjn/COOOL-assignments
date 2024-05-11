// Testcase 9:
// Handling Maps

import java.util.HashMap;

class Test9
{
	Test9 f1;
	public static Test9 global;

	public static void main(String[] args) {
		HashMap<Integer, Test9> map = new HashMap<>();
		Test9 o1 = new Test9();
		Test9 o2 = new Test9();
		for(int i = 1; i < 10; i++) {
			map.put(i, new Test9());
		}
		o1.foo(o2);
	}
	
	void foo(Test9 p1) {
		Test9 o3 = new Test9();
		Test9 o4 = new Test9();
		o3.f1 = o4;
		o4.f1 = new Test9();
		global = o3;
	}
}
