public class Test23
{
	static class Node
	{
		double value;
		Node next;
		Node()
		{
			value=10;
			next=null;
		}
		Node(double d)
		{
			value=d;
		}
		Node(double d, Node n)
		{
			value=d;
			next=n;
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
	static Element test()
	{
		Element e1=new Element(10);
		Element e2=new Element(11,e1);
		Element e3=new Element(12,e2);
		Element e4=new Element(13,e3);
		Node n1=new Node(14);
		e4.node=n1;
		Node n2=new Node(15);
		n1.next=n2;
		e3.node=n2;
		e2.node=n2;
		Node n3=new Node(16);
		n2.next=n3;
		e1.link=e4;
		return e2;

	}
	public static void main(String[] args) {
		Element e=test();;
		
	}

}

//45,46,47,48,49,51,55
