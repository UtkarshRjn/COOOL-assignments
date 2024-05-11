public class Test24
{
	static class Element
	{
		int data;
		Element next;
		Element()
		{
			data=10;
		}
	}
	static Element global;
	static void test()
	{
		Element e1=new Element();
		e1.next=new Element();
		e1.next.next=e1;

		Element e2=new Element();
		e2.next=e1;
		global=e2;
	}

	public static void main(String[] args) {
		test();
		
	}
}

//15,16,19
