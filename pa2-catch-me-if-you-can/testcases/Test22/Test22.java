public class Test22
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
	static Element recursiveCall(int times)
	{
		if(times>5) return new Element(times);
		else
		{
			Element obj=new Element(times);
			return recursiveCall(times+1);
		}
	}
	public static void main(String[] args) {
		Element obj = recursiveCall(0);
	}
}

//20
