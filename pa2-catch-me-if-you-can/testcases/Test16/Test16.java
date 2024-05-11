class Test16
{
	static class Element
	{
		int data;
		Element link;
		Element()
		{
			data=10;
		}
	}
	static Element globalE;
	static void bar(Element e)
	{
		System.out.println(e.data);
		Element e5=new Element();
		e.link=e5;
	}
	static Element foo()
	{
		Element e1=new Element();
		Element e2=new Element();
		Element e3=new Element();
		Element e4=new Element();

		globalE=e1;
		bar(e3);
		return e2;
	}
	public static void main(String[] args) {
		Element e= foo();
	}
}

//16,21,22,23
