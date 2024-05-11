class Test18
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
	static void L(int p,int q,Element obj)
	{
		Element u=new Element();
		Element t=u;
		if(p>q)
		{
			t.next=new Element(q);
			t.data=p;
			t=t.next;
		}
		obj.next=t;
	}
	public static void main(String[] args) {
		Element obj=new Element(10);
		Element o=new Element();
		L(10,20,obj);
	}
}

//20,24,31