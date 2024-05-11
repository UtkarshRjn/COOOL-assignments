class Test27
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
	static void test1(Node n)
	{
		Element e3=new Element();
		Element e4=new Element(14,e3);
		Node n1=new Node();
		Node n2=new Node(12,n1);
		n1.link=e3;
		n.node=n2;
	}
	static void test()
	{
		Element e1=new Element();
		Element e2=new Element(10,e1);
		Node n1=new Node(11);
		e2.node=n1;
		n1.link=e1;
		test1(n1);

	}
	public static void main(String[] args) {
		test();
	}
}

//47,49,50, 56, 58
