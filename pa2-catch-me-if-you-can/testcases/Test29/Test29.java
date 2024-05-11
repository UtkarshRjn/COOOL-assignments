class Test29
{
	static class Student
	{
		String name;
		Student(String n)
		{
			name=n;
		}
	}
	static class Course
	{
		Student[] enrolledStudents;
		String name;
		int studentCount;
		void printStudents()
		{
			 System.out.println("Enrolled students in course " + name + ":");
    		for (int i = 0; i < studentCount; i++) 
    		{
        		System.out.println(enrolledStudents[i].name);
    		}

		}
	}
	static void BusyCourse(Course c)
	{
		c.printStudents();
	}
	public static void main(String[] args) {
		Student s1=new Student("Student1");
		Student s2=new Student("Student2");
		Student s3=new Student("Student3");
		Student s4=new Student("Student4");
		Student s5=new Student("Student5");

		Course c1=new Course();
		c1.enrolledStudents = new Student[10];
		c1.name = "Course1";
		c1.studentCount = 10;

		Course c2=new Course();
		c2.enrolledStudents = new Student[10];
		c2.name = "Course2";
		c2.studentCount = 10;

		c1.enrolledStudents[c1.studentCount++]=s2;
		c1.enrolledStudents[c1.studentCount++]=s3;
		c1.enrolledStudents[c1.studentCount++]=s4;

		c2.enrolledStudents[c2.studentCount++]=s1;
		c2.enrolledStudents[c2.studentCount++]=s2;
		c2.enrolledStudents[c2.studentCount++]=s3;
		c2.enrolledStudents[c2.studentCount++]=s5;
		BusyCourse(c2);
	}
}

//37,38,39,41,44