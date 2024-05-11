class Test26
{
	static class Node
	{
		double value;
		Element link;
		Node node;

		Node()
		{
			value=10;
			node=null;
		}
		Node(double d)
		{
			value=d;
		}
		Node(double d, Node n)
		{
			value=d;
			node=n;
		}
	}
	static class Element
	{
		int data;
		Element link;
		Node node;
		Element()
		{
			data=10;
			link=null;
		}
		Element(int d)
		{
			data=d;
			link=null;
		}
		Element(int d, Element e)
		{
			data=d;
			link=e;
		}
	}
	static Element global;
	static void test(Element obj)
	{
		Element e1=new Element(10);
		Element e2=new Element(11);
		Node n1=new Node(12);
		e1.node=n1;
		e1.link=e2;
		Element e3=new Element(13);
		e2.link=e3;
		n1.link=e3;
		Node n2=new Node(14);
		n1.node=n2;
		Element e4=new Element(15);
		e4.node=n2;
		Element e5=new Element(16);
		e4.link=e5;
		Node n3=new Node();
		n2.node=n3;
		e5.node=n3;
		n3.node=n1;
		obj.node=n3;
	}
	public static void main(String[] args) {
		Element e1=new Element();
		test(e1);
	}
}

// 50,53,56,58,60,62 , 69
