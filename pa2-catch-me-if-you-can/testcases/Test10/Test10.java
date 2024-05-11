// Testcase 5:
// Handling Arrays and list

import java.util.ArrayList;
import java.util.List;

class Test10 {
	Test10 f1;
	Test10 f2;

	public static void main(String[] args) {
		Test10[] arr1 = new Test10[10];
		Test10[] arr2 = new Test10[5];
		for (int i = 0; i < 10; i++) {
			arr1[i] = new Test10();
		}
		arr2[0] = new Test10();
		arr2[1] = new Test10();
		arr2[2] = new Test10();
		arr2[3] = new Test10();
		arr2[4] = new Test10();
		arr1[0].foo(arr2[0]);
	}

	void foo(Test10 p1) {
		List<Test10> l1 = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			l1.add(new Test10());
		}
	}
}
