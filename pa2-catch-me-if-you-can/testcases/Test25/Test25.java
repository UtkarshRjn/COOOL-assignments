class Test25
{
	static class Element
	{
		int data;
		Element next;
		Element()
		{
			data=10;
		}
		Element(int d, Element e)
		{
			data=d;
			next=e;
		}
	}
	static void test( Element obj)
	{
		int i=5;
		Element t=new Element();;
		while(i>0)
		{
			Element e=new Element();
			t.next=e;
			t=e;
			i--;
		}
		obj.next=t;
	}

	public static void main(String[] args) {
		Element obj=new Element();
		test(obj);
	}
}
