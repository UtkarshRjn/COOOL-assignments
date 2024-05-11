class Test20
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
	static Element global;

	static void L(int p,int q)
	{
		Element u=new Element();
		Element t=new Element(21);
		u.next=t;
		if(p>q)
		{
			global=t;
		}
	}
	public static void main(String[] args) {
		Element obj=new Element(10);
		Element ob=new Element();
		obj.next=ob;
		L(30,60);
	}
}

//23
