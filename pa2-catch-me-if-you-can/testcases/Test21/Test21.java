class Test21
{
	static class Element
	{
		int data;
		Element next;
		Element()
		{
			data=0;
			next=null;
		}
		Element(int d)
		{
			data=d;
			next=null;
		}
	}
	static Element L(Element obj)
	{
		Element x=new Element();
		Element y=new Element();
		Element z=new Element();
		x.next=obj;
		obj.next=y;
		return z;
	} 
	public static void main(String[] args) {
		Element obj=new Element();
		Element res= L(obj);
	}
}

//21,22,28
