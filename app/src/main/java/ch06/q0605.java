package ch06;

public class q0605 {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    Student1 s = new Student1("홍길동",1,1,100,60,76);
    System.out.println(s.info());
  }
}

class Student1 {
  String name;
  int ban;
  int no;
  int kor;
  int eng;
  int math;

  Student1(String name, int ban, int no, int kor, int eng, int math) {
    this.name = name;
    this.ban = ban;
    this.no = no;
    this.kor = kor;
    this.eng = eng;
    this.math = math;
  }

  int getTotal() {
    return this.kor + this.eng + this.math;
  }

  float getAverage() {
    float aver = (getTotal()/(float)3);
    //return aver;
    return (Math.round(aver*10)/10.0f);
  }

  String info() {
    return name + ","+ ban+ ","+ no + ","+ kor + "," + eng + "," + math + ","+ getTotal() + ","+ getAverage();
  }
}