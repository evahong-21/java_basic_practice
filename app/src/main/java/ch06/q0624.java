package ch06;

public class q0624 {

  static int abs(int value) {
    return Math.abs(value);
  }

  public static void main(String[] args)
  {
    int value = 5;
    System.out.println(value+"의 절대값 :"+abs(value)); 
    value = -10;
    System.out.println(value+"의 절대값 :"+abs(value));
  }
}
